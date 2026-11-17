/*
 * Placeholder Java runtime for the documentation site's runnable cells.
 *
 * This file defines the contract documented in doc-site/README.md:
 *
 *   window.YaviRuntime.run(source) -> Promise<string>
 *
 * `source` is one complete Java compilation unit. The promise resolves with the
 * program's standard output and error text. A real runtime (for example one built
 * on GraalVM and preloading the YAVI jar) replaces only this file; the generator
 * and the pages do not change.
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
