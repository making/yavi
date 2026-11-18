# Violations

A `ConstraintViolation` is one failed rule. `ConstraintViolations` is the list of them; `messagesByName()`, `messagesByPointer()`, `details()` and `apply(...)` are the forms that a form and an API response need.

## ConstraintViolation

A violation knows where it is without parsing its name:

```java
ConstraintViolation violation = violations.get(0);
violation.name();      // tags[1]
violation.path();      // [Property[name=tags], Index[index=1]]
violation.pointer();   // /tags/1, a JSON Pointer (RFC 6901)
violation.messageKey();    // charSequence.notBlank
violation.message();       // "tags[1]" must not be blank
violation.args();          // positional message arguments
violation.params();        // arguments by name
violation.violatedValue(); // the value that failed
```

`path()` is a list of `PathSegment.Property` and `PathSegment.Index`. `label()` is the label given to the property, if any; the name, the path and the pointer are not changed by a label.

A violation is immutable. The collections it returns are unmodifiable.

## Names, paths and pointers

A name follows the structure of the input: `address.street`, `tags[0]`, `items[1].name`, `addresses[0].street`. A violation of a value without a name has the empty name and a message that says "The value". The pointer is the same path as a JSON Pointer: `/address/street`, `/tags/0`. `messagesByPointer()` uses it as the key, and it is what an `errors[].pointer` of an RFC 9457 problem detail holds.

A violation of an `anyOf` alternative is not mixed into the list. The result is one `ConstraintViolation.NoAlternativeMatched`, whose `alternatives()` holds one `ConstraintViolations` per alternative.

## ViolationDetail

`details()` returns `ViolationDetail` records that a JSON library serializes as they are:

```java
List<ViolationDetail> details = violations.details();
// [{"name": "items[1].quantity", "pointer": "/items/1/quantity", "messageKey": "numeric.lessThanOrEqual",
//   "message": "\"items[1].quantity\" must be less than or equal to 99", "params": {"max": 99}}]
```

`ViolationDetail` is a sealed interface of `ViolationDetail.Simple` and `ViolationDetail.NoAlternativeMatched`, which has `alternatives`. A `switch` over a detail needs no `default`.

`params` holds the arguments of the violation by name. The names are those of the parameters of the constraint method; what a constraint computes from the value has a name too (`actualSize`, `duplicates`, `missing`, `notAllowed`). A message of your own names its arguments from `{1}` on with `ViolationMessage.of(key, format, names...)`; without names, the violation has no parameters. The format keeps referring to the arguments by position.

| Message key | Names in `params` |
| --- | --- |
| `numeric.lessThanOrEqual` | `max` |
| `container.greaterThanOrEqual` | `min`, `actualSize` |
| `object.type` | `type` |
| `collection.unique` | `duplicates` |

The violated value is not in a detail. It may be a password or a token, and the client has sent it. `violation.args()` and `violation.violatedValue()` have it on the server.

## Message maps

```java
Map<String, List<String>> byName = violations.messagesByName();
// {"email": ["\"email\" must not be blank"], "items[1].name": ["..."]}

Map<String, List<String>> byPointer = violations.messagesByPointer();
// {"/email": [...], "/items/1/name": [...]}
```

The messages of the root are under the empty name in `messagesByName()`. Both maps are unmodifiable and hold the messages in the order of the violations.

## Immutability and alternatives

`details()` returns an unmodifiable list, and the maps and lists of `messagesByName()` and `messagesByPointer()` are unmodifiable too. Add what a response needs to a copy.

To show a specific message instead of the summary of an `anyOf`, pick the alternative with the fewest violations; the others are not shown:

```java
ConstraintViolations closest = unmatched.alternatives().stream()
        .min(Comparator.comparingInt(ConstraintViolations::size))
        .orElseThrow();
```
