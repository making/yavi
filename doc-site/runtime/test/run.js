// Harness for the documentation runtime. It loads the Web Image binding built by
//   ./mvnw -f doc-site/runtime/pom.xml -Pweb package
// and drives the same entry point the pages use (globalThis.YaviJs.run, installed by
// WebMain; the browser wrapper in assets/yavi-runtime.js forwards to it).
//
//   node --experimental-wasm-exnref doc-site/runtime/test/run.js [runtime-dir]
//
// Every ```java runnable block in content/0.17/{en,ja} is a complete program followed by
// a hidden expectation:
//
//   ```java runnable
//   ... complete compilation unit ...
//   ```
//   <!-- run: expected output
//   ... exact standard output ...
//   -->
//
// The harness compiles and runs each block and asserts its output. The comment keeps the
// expectation next to the program it belongs to and is invisible on the page. It also
// checks that the English and Japanese trees carry the same programs. A block without an
// expectation, a changed runtime message or a program that fails to compile therefore
// fails the CI step that runs this file.
"use strict";

const fs = require("fs");
const path = require("path");

const repository = path.resolve(__dirname, "..", "..", "..");
const contentDir = path.join(repository, "doc-site", "content", "0.17");
const runtimeDir =
	process.argv[2] || path.join(repository, "doc-site", "runtime", "target", "web", "0.17");

let failures = 0;
function check(name, condition, detail) {
	if (condition) {
		console.log("ok   " + name);
	} else {
		failures++;
		console.log("FAIL " + name + (detail ? "\n" + detail : ""));
	}
}

const BLOCK = /```java runnable\n([\s\S]*?)\n```\n<!-- run: expected output\n([\s\S]*?)\n-->/g;
const RUNNABLE = /```java runnable\n([\s\S]*?)\n```/g;

// Every runnable block of a language tree, in page order: the page, the program and the
// expected output. The expectation must be the one directly after the fence.
function examples(language) {
	const root = path.join(contentDir, language);
	const found = [];
	for (const file of fs.readdirSync(root).sort()) {
		if (!file.endsWith(".md")) {
			continue;
		}
		const markdown = fs.readFileSync(path.join(root, file), "utf8");
		const total = [...markdown.matchAll(RUNNABLE)].length;
		const matched = [...markdown.matchAll(BLOCK)];
		if (total !== matched.length) {
			throw new Error(
				`${language}/${file}: ${total} java runnable block(s) but ${matched.length} expected-output comment(s)`,
			);
		}
		for (const match of matched) {
			found.push({ page: `${language}/${file}`, source: match[1] + "\n", expected: match[2] });
		}
	}
	return found;
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
	const text = result === null || result === undefined ? "" : String(result);
	return text.replace(/\r\n/g, "\n").replace(/\n+$/, "");
}

(async () => {
	// Ten example programs are compiled and interpreted; the first run also initializes
	// the in-image file system. The watchdog only guards against a hang.
	const watchdog = setTimeout(() => {
		console.error("FAIL the harness did not finish; the runtime is hung");
		process.exit(1);
	}, 900000);

	const english = examples("en");
	const japanese = examples("ja");
	check("the content tree carries ten runnable examples", english.length >= 10, String(english.length));
	check(
		"the English and Japanese trees carry the same programs",
		english.length === japanese.length &&
			english.every((example, index) => example.page.replace(/^en\//, "") === japanese[index].page.replace(/^ja\//, "") &&
				example.source === japanese[index].source &&
				example.expected === japanese[index].expected),
		`${english.length} vs ${japanese.length}`,
	);

	require(path.join(runtimeDir, "yavi-runtime-wasm.js"));
	const binding = await waitForBinding(60000);

	const numbers = new Map();
	for (const example of english) {
		const number = (numbers.get(example.page) || 0) + 1;
		numbers.set(example.page, number);
		const name = `${example.page} example ${number}`;
		const output = run(binding, example.source);
		check(`${name} matches its expected output`, output === example.expected, output);
		check(
			`${name} finishes within the step budget`,
			!output.includes("Execution stopped after") && !output.includes("Exception:"),
			output,
		);
	}

	// The same source runs again: each cell is independent and nothing leaks between runs.
	const first = english[0];
	const again = run(binding, first.source);
	check("example is repeatable", again === first.expected, again);

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
