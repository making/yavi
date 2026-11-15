#!/usr/bin/env python3
# SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
# SPDX-License-Identifier: Apache-2.0
"""Runs the Java examples of a "New features" document with jshell.

    python3 docs/verify-new-features.py [docs/NewFeatures_0.18.0.md]

The examples are run against the 0.18.0-SNAPSHOT that this repository builds
(target/classes of `./mvnw compile`), not against a released jar. The test dependencies
are on the class path, so an example may use Jackson.

The document is not a program: a section shows the API of one topic and leaves out the
context of a real application. This script therefore:

* gives the imports, the types and the values that the examples take for granted
  (PRELUDE), and
* runs each section (## heading) in its own jshell session, so that a record that a
  section redefines does not change the examples of another section.

An example whose errors are all about an unknown variable or class is not reported: the
document did not give its context (a controller, a row of a database, the violations of a
validation shown above). Any other error, such as a method that does not exist, a type
that does not match or an exception, is a failure.

The comments of the document are not compared with the output here; they often describe
the example rather than print it. verify-examples.py does that for the Raoh tutorial.
"""
import os
import re
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MARKER = "@@statement "

PRELUDE = """```java
import am.ik.yavi.arguments.*;
import am.ik.yavi.builder.*;
import am.ik.yavi.core.*;
import am.ik.yavi.input.*;
import am.ik.yavi.message.*;
import am.ik.yavi.validator.*;
import am.ik.yavi.validator.Yavi;
import org.jspecify.annotations.*;
import java.time.*;
import java.util.*;
import java.math.*;
import java.net.*;
import java.util.regex.*;
import tools.jackson.databind.*;
import tools.jackson.core.*;

// Types that the examples refer to without defining them.
sealed interface Shape permits Circle, Square {}
record Circle(Integer radius) implements Shape {}
record Square(Integer side) implements Shape {}
record PasswordForm(String current, String newPassword, String confirm) {}
record Nickname(String value) {}
record Age(int value) {}
record Product(String id) {}
record Feed(String title, List<String> urls) {}
record Tree(String name, List<Tree> children) {}
record Venue(Integer id, Address address) {}
ObjectValidator.Reversible<Product> productValidator = Yavi._object(Product.class);

// Values that the examples refer to without defining them.
ObjectInputValidator<Address> addressValidator = Yavi.schema()
        ._string("street", c -> c.notBlank())
        ._string("city", c -> c.notBlank())
        .fromMap(Address::new);
ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._string("nickname", c -> c.notBlank()).optional()
        ._integer("age", c -> c.greaterThanOrEqual(0)).defaultValue(20)
        ._enum("role", Role.class)
        ._object("address", addressValidator)
        ._list("tags", Yavi._string(c -> c.notBlank())).defaultValue(List.of())
        .fromMap(User::new);
MessageFormatter formatter = new MessageSourceMessageFormatter(
        (code, args, defaultMessage, locale) -> defaultMessage);
MessageSourceMessageFormatter.MessageSource messageSource = (code, args, defaultMessage, locale) -> defaultMessage;
JsonMapper jsonMapper = JsonMapper.builder().build();
String json = "{\\"email\\":\\"jd@example.com\\",\\"age\\":30,\\"role\\":\\"ADMIN\\"}";
Map<String, Object> map = Map.of("email", "jd@example.com", "role", "ADMIN");

void save(Object value) {
}

void report(String name, String message) {
}

class Request {

    Map<String, String[]> getParameterMap() {
        return Map.of("name", new String[] { " Alice " }, "age", new String[] { "abc" });
    }

}

Request request = new Request();

class JsonNodeReader implements CoercibleInputReader<JsonNode> {

    static final JsonNodeReader INSTANCE = new JsonNodeReader();

    @Override
    public boolean isNull(JsonNode node) {
        return node.isNull() || node.isMissingNode();
    }

    @Override
    public Object value(JsonNode node) {
        if (node.isString()) {
            return node.stringValue();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return node;
    }

    @Override
    public boolean isObject(JsonNode node) {
        return node.isObject();
    }

    @Override
    public JsonNode property(JsonNode object, String name) {
        return object.get(name);
    }

    @Override
    public Iterable<Map.Entry<String, JsonNode>> properties(JsonNode object) {
        return object.properties();
    }

    @Override
    public boolean isArray(JsonNode node) {
        return node.isArray();
    }

    @Override
    public Iterable<JsonNode> elements(JsonNode array) {
        return array;
    }

}
```
"""


def blocks(document, language):
    return re.findall(r"```" + language + r"\n(.*?)```", document, re.S)


def statements(document):
    """Returns the statements of a document as (code, raises), where raises is True when
    the comment of the document says that the statement throws an exception."""
    result, raises = [], []
    for block in blocks(document, "java"):
        lines = block.rstrip("\n").split("\n")
        if lines[0].startswith(" ") or re.search(r"^@\w+", block, re.M):
            continue
        for line in lines:
            text = line.strip()
            if not text or text.startswith("//"):
                if "Exception" in text and raises:
                    raises[-1] = True
                continue
            line = re.sub(r"\s+// .*$", "", line)
            if text.startswith(".") and result:
                result[-1] = result[-1].rstrip() + line.strip()
            elif text.startswith(":") and result:
                # jshell does not continue a ternary onto the next line
                result[-1] = result[-1].rstrip() + " " + text
            elif text.startswith(("else", "catch", "finally")) and result:
                # jshell takes the closing brace of the block above as the end of the
                # statement, so the keyword has to be on the same line
                previous = result[-1].rstrip()
                result[-1] = previous + " " + text if previous.endswith("}") else previous + "\n" + line
            elif line[0] in " })" and result:
                result[-1] += "\n" + line
            else:
                result.append(line)
                raises.append(False)
            if "Exception" in text:
                raises[-1] = True
    return list(zip(result, raises))


def sections(document):
    """Returns the header and the sections of the document."""
    parts = re.split(r"(?m)^(?=## )", document)
    return [part for part in parts if part.strip()]


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


def context_only(errors):
    """A statement whose errors are all about an unknown variable or class is an example
    that the document does not give the context of."""
    symbols = [line for line in errors if re.search(r"\bsymbol:\s", line)]
    return bool(symbols) and all(re.search(r"\bsymbol:\s+(variable|class)\b", line) for line in symbols)


def title(section):
    return section.split("\n", 1)[0].strip()


def main():
    path = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, "docs", "NewFeatures_0.18.0.md")
    with open(path, encoding="utf-8") as file:
        document = file.read()
    parts = sections(document)
    shared = statements(parts[0]) + statements(PRELUDE)
    with tempfile.TemporaryDirectory() as resources:
        with open(os.path.join(resources, "messages_ja.properties"), "w", encoding="utf-8") as file:
            file.write("".join(blocks(document, "properties")))
        path = class_path(resources)
        failures = checked = skipped = 0
        for section in parts[1:]:
            examples = shared + statements(section)
            script = "".join('System.out.println("%s%d");\n%s\n' % (MARKER, index, code)
                             for index, (code, _) in enumerate(examples))
            chunks = run(script, path)
            for index, (code, raises) in enumerate(examples):
                if index < len(shared):
                    continue
                checked += 1
                errors = [line for line in chunks.get(index, []) if line.startswith("|")]
                if not errors:
                    continue
                if raises and any("Exception" in line for line in errors):
                    skipped += 1
                    continue
                if context_only(errors):
                    skipped += 1
                    continue
                failures += 1
                print("FAILED in %s:\n%s" % (title(section), code))
                for line in errors:
                    print("  " + line)
                print()
        print("%d statements, %d skipped, %d failed" % (checked, skipped, failures))
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
