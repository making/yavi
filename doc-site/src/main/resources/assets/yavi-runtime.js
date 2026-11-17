/*
 * Placeholder Java runtime for the documentation site's runnable cells.
 *
 * This file defines the contract documented in doc-site/README.md:
 *
 *   window.YaviRuntime.run(source) -> Promise<string>
 *
 * `source` is one complete Java compilation unit. The promise resolves with the
 * program's standard output and error text. The generator publishes this stub as
 * assets/runtime/<version>/yavi-runtime.js for every version tree that has no built
 * runtime (see doc-site/runtime/ for the real one); the pages do not change.
 */
(function () {
	"use strict";

	window.YaviRuntime = {
		/**
		 * Runs the given Java source and resolves with its output text.
		 * @param {string} source a complete Java compilation unit
		 * @returns {Promise<string>} the program's stdout/stderr text
		 */
		run: function (source) {
			return Promise.resolve("Code execution is not available yet. The example above is the source code to run once the runtime ships.");
		}
	};
})();
