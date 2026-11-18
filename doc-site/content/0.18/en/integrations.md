# Integration Overview

YAVI has no runtime dependency other than JSpecify, and the interfaces that a framework needs are part of the library. A validator is a function: a schema reads an input, and the result is a `Validated` or its `ConstraintViolations`. Each integration below is a small adapter around that result.

| Integration | What it provides |
| --- | --- |
| [Spring](integrations-spring.md) | `BindingResult`, `MessageSource`, showing a form again, validators in an IoC container, WebFlux |
| [Jackson](integrations-jackson.md) | A `JsonNodeReader`, mapper settings for duplicate keys and number fidelity, serializing violations |
| [Kotlin](integrations-kotlin.md) | The `validator<T>` DSL, nullability with JSpecify, coroutine extensions |
| [NullAway](integrations-nullaway.md) | JSpecify annotations, common reports and fixes, limitations |

The input side is described in [JSON Trees](input-json.md) and [Request Parameters and Forms](input-parameters.md); the output side in [Writing an Object Back](writing-back.md).
