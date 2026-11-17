# Overview

YAVI (pronounced jɑ-vάɪ) is a lambda based type safe validation for Java.

[View project on GitHub](https://github.com/making/yavi)

## Why YAVI?

YAVI sounds as same as a Japanese slang "YABAI (ヤバイ)" that means awesome or awful depending on the context (like "Crazy").
If you use YAVI, you will surely understand that it means the former.

The concepts are

* No reflection!
* No (runtime) annotation!
* Not only Java Beans!
* Zero dependency!

If you are not a fan of [Bean Validation](https://beanvalidation.org/), YAVI will be an awesome alternative.

YAVI has the following features:

* Type-safe constraints, unsupported constraints cannot be applied to the wrong type
* Fluent and intuitive API
* Constraints on any object. Java Beans, [Records](https://openjdk.java.net/jeps/395), [Protocol Buffers](https://developers.google.com/protocol-buffers), [Immutables](https://immutables.github.io/) and anything else.
* Lots of powerful built-in constraints, including temporal and enum constraints
* Easy custom constraints
* Validation for constraint groups and contexts, conditional validation, and conflict strategy
* Validation for arguments before creating an object, and the "Always Valid model" that makes invalid objects impossible to create
* Kotlin integration, including coroutine extensions
* Support for API and combination of validation results and validators that incorporate the concept of functional programming

## Next steps

* [Introduction](introduction.md) describes the ideas behind YAVI and where it comes from.
* [Getting Started](getting-started.md) adds the dependency and validates the first object.
* [Using YAVI](using-yavi.md) covers the core `Validator` API.
* [Built-in Constraints](built-in-constraints.md) lists the constraint rules and their messages.
