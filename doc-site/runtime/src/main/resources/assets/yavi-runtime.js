/*
 * Java runtime for the documentation site's runnable cells.
 *
 * Implements the contract documented in doc-site/README.md:
 *
 *   window.YaviRuntime.run(source) -> Promise<string>
 *
 * `source` is one complete Java compilation unit. The promise resolves with the
 * program's standard output and error text (compiler diagnostics, run-time exceptions
 * and the message of a program stopped for exceeding its step budget are part of that
 * text). The heavy part -- javac and a JVM compiled to WebAssembly -- lives in
 * yavi-runtime-wasm.js beside this file and is loaded on the first Run, so a page
 * without runnable cells never downloads it.
 *
 * The image is started in a Web Worker. The interpreter needs host stack space and a
 * worker's stack is much deeper than the page's main thread (examples with records,
 * String.format or moderate recursion otherwise hit the WebAssembly stack limit).
 * Messages are queued, so several Run clicks are executed one after another.
 *
 * Both files are copied into assets/runtime/<version>/ by the site generator; the
 * wasm module is built for the YAVI version of that documentation tree.
 */
(function () {
	"use strict";

	var scriptUrl = document.currentScript && document.currentScript.src;
	var base = scriptUrl ? scriptUrl.replace(/[^/]*$/, "") : "";
	var PING = "<yavi-ping>";
	// The worker answers the handshake with this prefix; repeats that arrive after the
	// handshake are stale and must not be mistaken for a run's output.
	var PING_REPLY = /^yavi-runtime-ping:/;
	var started = null;
	var queue = Promise.resolve();
	var version = null;

	// Resolves with a function that runs one source in the worker. The generated
	// launcher starts the Java side asynchronously; the ping is repeated until the
	// worker answers, because messages sent before its message listener exists are lost.
	function start() {
		if (started) return started;
		started = new Promise(function (resolve, reject) {
			if (typeof Worker === "undefined") {
				startInPage(resolve, reject);
				return;
			}
			var worker;
			try {
				worker = new Worker(base + "yavi-runtime-wasm.js");
			} catch (e) {
				startInPage(resolve, reject);
				return;
			}
			var pending = null;
			var ready = false;
			worker.onmessage = function (e) {
				var text = String(e.data);
				if (!ready) {
					ready = true;
					clearInterval(ping);
					if (PING_REPLY.test(text)) {
						version = text.substring("yavi-runtime-ping:".length).trim();
					}
					resolve(function (source) {
						return new Promise(function (runResolve, runReject) {
							var timer = window.setTimeout(function () {
								if (pending === null) return;
								pending = null;
								runReject(new Error("the Java runtime did not answer"));
							}, 120000);
							pending = {
								resolve: function (output) {
									window.clearTimeout(timer);
									runResolve(output);
								}
							};
							worker.postMessage(source);
						});
					});
					return;
				}
				if (PING_REPLY.test(text)) {
					// A repeated handshake answer sent before the listener existed.
					return;
				}
				if (pending !== null) {
					var p = pending;
					pending = null;
					p.resolve(text);
				}
			};
			worker.onerror = function (e) {
				if (!ready) {
					clearInterval(ping);
					reject(new Error("the Java runtime failed to start: " + (e.message || e)));
				}
			};
			var ping = window.setInterval(function () {
				if (!ready) worker.postMessage(PING);
			}, 200);
			worker.postMessage(PING);
		});
		return started;
	}

	// Fallback for environments without workers: load the launcher into the page. Its
	// stack is shallower, so deep recursion fails earlier.
	function startInPage(resolve, reject) {
		var script = document.createElement("script");
		script.src = base + "yavi-runtime-wasm.js";
		script.onerror = function () {
			reject(new Error("failed to load " + script.src));
		};
		script.onload = function () {
			(function wait() {
				if (window.YaviJs) {
					version = window.YaviJs.yaviVersion;
					resolve(function (source) {
						return String(window.YaviJs.run(source));
					});
				} else {
					window.setTimeout(wait, 20);
				}
			})();
		};
		document.head.appendChild(script);
	}

	window.YaviRuntime = {
		/**
		 * Runs the given Java source and resolves with its output text.
		 * @param {string} source a complete Java compilation unit
		 * @returns {Promise<string>} the program's stdout/stderr text
		 */
		run: function (source) {
			// Start loading before the first call resolves so consecutive cells reuse
			// the same module. Runs are serialized: the binding is synchronous.
			var turn = queue.then(start).then(function (execute) {
				return execute(source);
			});
			queue = turn.then(
				function () {},
				function () {}
			);
			return turn;
		},
		/** The YAVI version this runtime was built with, once loaded. */
		version: function () {
			return version;
		}
	};
})();
