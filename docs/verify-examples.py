#!/usr/bin/env python3
# SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
# SPDX-License-Identifier: Apache-2.0
"""Runs the Java examples of a document with jshell and compares what they print with
the comments of the document.

    python3 docs/verify-examples.py [docs/RaohTutorialInYavi.md]

The classes of YAVI are compiled first and jshell is given them with the test class
path, so an example may use a library that the tests use, such as Jackson.

Conventions of a document:

* A ```java block is run, in the order of the document, in one jshell session.
* A statement starts at a line that is not indented. A line that starts with `.`
  continues the line before it, which jshell does not accept by itself.
* `// ==> text` after a statement is the value that jshell shows for it. A note may
  follow after two spaces: `// ==> Valid[value=42]  (a number)`.
* Other comment lines that follow a statement up to a blank line are the lines that it
  prints.
* `...` in an expected text stands for any text. The entries of a printed map are
  compared in any order.
* A block whose first line is indented is a fragment and a block that contains an
  annotation at the start of a line (`@PostMapping`) needs a framework: neither is run.
* A ```properties block is written to `messages_ja.properties` on the class path.
"""
import os
import re
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
NOTE = re.compile(r"\s{2,}\(.*\)$")
MARKER = "@@statement "


def blocks(document, language):
    return re.findall(r"```" + language + r"\n(.*?)```", document, re.S)


def statements(document):
    """Returns the statements of the document as (code, expected lines)."""
    result = []
    for block in blocks(document, "java"):
        lines = block.rstrip("\n").split("\n")
        if lines[0].startswith(" ") or re.search(r"^@\w+", block, re.M):
            continue
        for index, line in enumerate(lines):
            text = line.strip()
            if not text:
                continue
            if text.startswith("//"):
                # the output of the statement before it, unless the comment is prose:
                # one that follows a blank line or stands inside a statement
                after = next((l.strip() for l in lines[index + 1:] if not l.strip().startswith("//")), "")
                before = lines[index - 1].strip() if index > 0 else ""
                if not result or not before or after.startswith(".") or (before.startswith("//") and not result[-1][1]):
                    continue
                expected = text[2:].strip()
                if expected.startswith("==>"):
                    expected = NOTE.sub("", expected[3:].strip())
                result[-1][1].append(expected)
                continue
            line = re.sub(r"\s+// .*$", "", line)
            if text.startswith(".") and result:
                result[-1][0][-1] += line.strip()
            elif line[0] in " })" and result:
                result[-1][0].append(line)
            else:
                result.append(([line], []))
    return [("\n".join(code), expected) for code, expected in result]


def class_path(resources):
    output = os.path.join(resources, "class-path.txt")
    subprocess.run(["./mvnw", "-q", "-o", "compile", "dependency:build-classpath", "-Dmdep.outputFile=" + output],
                   cwd=ROOT, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    with open(output) as file:
        return os.pathsep.join([os.path.join(ROOT, "target", "classes"), resources, file.read().strip()])


def run(script, path):
    process = subprocess.run(["jshell", "-J-Duser.language=en", "--class-path", path, "-q"], input=script,
                             capture_output=True, text=True, encoding="utf-8")
    chunks, current = {}, None
    for line in process.stdout.split("\n"):
        line = re.sub(r"^((jshell>|   \.\.\.>) ?)+", "", line)
        if line.startswith(MARKER):
            current = int(line[len(MARKER):])
            chunks[current] = []
        elif current is not None and line.strip() and not line.startswith("|  created "):
            chunks[current].append(line)
    return chunks


def tokens(text):
    return sorted(token for token in re.split(r"[{}\[\],]\s*", text) if token)


def matches(expected, actual):
    pattern = ".*".join(re.escape(part) for part in expected.split("..."))
    return re.fullmatch(pattern, actual) is not None or tokens(expected) == tokens(actual)


def main():
    path = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, "docs", "RaohTutorialInYavi.md")
    with open(path, encoding="utf-8") as file:
        document = file.read()
    all_statements = statements(document)
    script = "".join('System.out.println("%s%d");\n%s\n' % (MARKER, index, code)
                     for index, (code, _) in enumerate(all_statements))
    with tempfile.TemporaryDirectory() as resources:
        with open(os.path.join(resources, "messages_ja.properties"), "w", encoding="utf-8") as file:
            file.write("".join(blocks(document, "properties")))
        chunks = run(script, class_path(resources))
    failures = checked = 0
    for index, (code, expected) in enumerate(all_statements):
        output = chunks.get(index, [])
        errors = [line for line in output if line.startswith("|")]
        actual = [re.sub(r"^\S+ ==> ", "", line) for line in output if not line.startswith("|")]
        if expected:
            checked += 1
        ok = not errors and (not expected or (len(expected) == len(actual) and all(map(matches, expected, actual))))
        if not ok:
            failures += 1
            print("FAILED:\n" + code)
            for line in expected:
                print("  expected: " + line)
            for line in errors + actual:
                print("  actual:   " + line)
            print()
    print("%d statements, %d with an expected output, %d failed" % (len(all_statements), checked, failures))
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
