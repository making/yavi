// Harness for the documentation runtime. It loads the Web Image binding built by
//   ./mvnw -f doc-site/runtime/pom.xml -Pweb package
// and drives the same entry point the pages use (globalThis.YaviJs.run, installed by
// WebMain; the browser wrapper in assets/yavi-runtime.js forwards to it).
//
//   node --experimental-wasm-exnref doc-site/runtime/test/run.js [runtime-dir]
//
// The shipped example is read from the content tree, so the test fails when the page's
// cell and the runtime disagree. Only output is asserted; the watchdog is a hang guard.
"use strict";

const fs = require("fs");
const path = require("path");

const repository = path.resolve(__dirname, "..", "..", "..");
const runtimeDir =
	process.argv[2] || path.join(repository, "doc-site", "runtime", "target", "web", "0.17");
const contentFile = path.join(repository, "doc-site", "content", "0.17", "en", "getting-started.md");

let failures = 0;
function check(name, condition, detail) {
	if (condition) {
		console.log("ok   " + name);
	} else {
		failures++;
		console.log("FAIL " + name + (detail ? "\n" + detail : ""));
	}
}

function exampleSource() {
	const markdown = fs.readFileSync(contentFile, "utf8");
	const match = /```java runnable\n([\s\S]*?)```/.exec(markdown);
	if (!match) {
		throw new Error("no ```java runnable block in " + contentFile);
	}
	return match[1];
}

function waitForBinding(timeoutMillis) {
	const deadline = Date.now() + timeoutMillis;
	return new Promise((resolve, reject) => {
		(function poll() {
			if (globalThis.YaviJs) {
				resolve(globalThis.YaviJs);
			} else if (Date.now() > deadline) {
				reject(new Error("the Web Image binding did not appear within " + timeoutMillis + "ms"));
			} else {
				setTimeout(poll, 20);
			}
		})();
	});
}

// One run, as text. The binding never throws: diagnostics, output and the stop message
// all come back as the promise's text.
function run(binding, source) {
	const result = binding.run(source);
	return result === null || result === undefined ? "" : String(result);
}

(async () => {
	const watchdog = setTimeout(() => {
		console.error("FAIL the harness did not finish; the runtime is hung");
		process.exit(1);
	}, 120000);

	require(path.join(runtimeDir, "yavi-runtime-wasm.js"));
	const binding = await waitForBinding(60000);

	const source = exampleSource();
	const output = run(binding, source);
	for (const line of [
		"Validation failed: 3 constraint violations",
		'- manufacturer: "manufacturer" must not be null',
		'- licensePlate: The size of "licensePlate" must be greater than or equal to 2. The given size is 1',
		'- seatCount: "seatCount" must be greater than or equal to 2',
	]) {
		check("example prints " + JSON.stringify(line), output.includes(line), output);
	}

	// The same source runs again: each cell is independent and nothing leaks between runs.
	const again = run(binding, source);
	check("example is repeatable", again === output, again);

	const compileError = run(binding, 'public class Main { public static void main(String[] a) { int x = "oops"; } }');
	check(
		"compile error is reported",
		compileError.includes("Main.java:1:") && compileError.includes("error: incompatible types"),
		compileError,
	);

	const loop = run(binding, "public class Main { public static void main(String[] a) { while (true) { } } }");
	check(
		"infinite loop is stopped",
		loop.includes("Execution stopped after") && loop.includes("steps"),
		loop,
	);

	const exception = run(
		binding,
		'public class Main { public static void main(String[] a) { throw new IllegalStateException("boom"); } }',
	);
	check(
		"run-time exception is reported",
		exception.includes("Exception: boom") && exception.includes("Main.main"),
		exception,
	);

	const after = run(binding, 'public class Main { public static void main(String[] a) { System.out.println("alive"); } }');
	check("runtime still works after a stopped program", after.includes("alive"), after);

	clearTimeout(watchdog);
	console.log(failures === 0 ? "all runtime checks passed" : failures + " runtime check(s) failed");
	process.exit(failures === 0 ? 0 : 1);
})().catch((error) => {
	clearTimeout(watchdog);
	console.error("FAIL " + (error && error.stack ? error.stack : error));
	process.exit(1);
});
