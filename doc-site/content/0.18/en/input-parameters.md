# Request Parameters and Forms

`readingParameters()` reads a flat map of strings, as a servlet request or a submitted form provides it, with the schema that reads a map or a JSON document. A string is text that stands for a value of the type of its property, and a key is the path of a nested value.

```java
record SearchForm(String q, Integer page, Boolean includeArchived, @Nullable LocalDate since, List<String> tags,
        @Nullable Address address) {}

ValueValidator<Map<String, ?>, SearchForm> searchFormValidator = Yavi.schema()
        ._string("q", c -> c.notBlank())
        ._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
        ._boolean("includeArchived").defaultValue(false)
        ._localDate("since").nullable()
        ._list("tags", Yavi._string(c -> c.notBlank())).defaultValue(List.of())
        ._object("address", addressValidator).nullable()
        .fromMap(SearchForm::new)
        .readingParameters();

// ?q=yavi&page=2&since=&tags=a&tags=b&address.city=Tokyo
Validated<SearchForm> validated = searchFormValidator.validate(params);
```

Every form of such a map is accepted: a `Map<String, String>`, a `Map<String, List<String>>` such as Spring's `MultiValueMap`, and the `Map<String, String[]>` of the Servlet API. The type is `ValueValidator<Map<String, ?>, X>` whatever it is assigned to.

## Keys and paths

* `address.city` and `address[city]` are the property `city` of the object `address`; `tags[]` is `tags`.
* A key with several values is a list (`tags=a&tags=b`). Several values for a scalar property are reported with `object.type`; none is picked.
* Keys with indexes are a list in the order of the indexes: `items[0].name=pen&items[1].name=ink`.
* A skipped index is an element that is `null`, so a violation keeps the index it was sent with (`items[2].name`).
* A name that holds a dot is written in brackets (`scores[a.b]`).
* A key that has both values and nested keys (`a=1&a.b=2`) is reported with `object.type`, and a key that is not written as a path (`a..b`, `a[0`) matches no property.
* With `rejectingUnknownProperties()`, an unknown key is reported by the validator of the object that has it (`address.zip`).

## Coercion and StringCoercion

The rules are strict by default:

* A number is a decimal number in the grammar of `new BigDecimal(String)`: an optional sign, digits, an optional fraction and an optional exponent, without surrounding white space or grouping separators. `"30"` and `"30.0"` are an `Integer`; `"30.5"`, `" 30"` and `"1,000"` are not.
* A boolean is `"true"` or `"false"`.
* An enum constant, a temporal value, a `UUID` and a `URI` are read from their string as they are in [a map](input-maps.md); coercion adds only the types that a map writes in a form of their own.
* A string that does not stand for the type is reported with `object.type` (`"page" must be of type integer`), never with an exception. Nothing is converted to a string.

`StringCoercion` relaxes the rules for text written by a person, such as the fields of a form:

```java
StringCoercion form = StringCoercion.strict().trimming().booleanWords();

ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
        ._integer("age")
        ._boolean("subscribed").defaultValue(false) // an unchecked checkbox sends nothing
        .fromMap(Signup::new)
        .readingParameters(form);

signupValidator.validate(Map.of("age", " 30 ", "subscribed", "on")); // Signup[age=30, subscribed=true]
```

* `trimming()` reads every text without its surrounding white space, whatever its property is, and validates a blank string like `null`. The constraints see the trimmed text.
* `booleanWords()` takes `true`, `1`, `yes`, `on` and `false`, `0`, `no`, `off` in any case. `booleanWords(trueWords, falseWords)` takes the words of the application instead.
* A `String` whose white space is part of it, such as a password, says so with `untrimmed()` of its validator: `._string(Yavi._string("password", c -> c.greaterThanOrEqual(8)).untrimmed())`.
* Each rule is given once: `reader.coercingStrings(rules).coercingStrings()` and `booleanWords(...).booleanWords()` do not compile.
* A validator reads with the last reader it is given: `readingWith(reader)` after `readingParameters()` coerces no string.

## Empty values and defaults

* An empty string is validated like `null`, whatever the type of its property is. `optional()`, `nullable()` and `defaultValue(...)` apply to it. Give `defaultValue("")` to a string property that must receive `""`.
* Text has no `null`, so a property that would reject an empty string with `object.notNull` reports `object.required` (`"page" is required`), as it does for an absent key. The violation of a constraint that rejects `null`, such as `notBlank()`, is reported as it is.
* An empty list or object cannot be sent: its key is absent, which is validated like `null`. Give `defaultValue(List.of())` to a list that may be empty, such as checkboxes.
* `recover` suits a parameter that must not fail a request: `._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)`.

## Labels and messages

A form calls a field by its label. `label` changes `{0}` of every message of the property and of its elements; the name, the path and the pointer of a violation are unchanged. `typeMessage` replaces the message of `object.type`, and `required` the message of `object.required`.

```java
record Token(String name, Integer ttl, List<String> roles) {}

ValueValidator<Map<String, ?>, Token> tokenValidator = Yavi.schema()
        ._string("name", c -> c.notBlank()).label("Token name")
        ._integer("ttl", c -> c.positive()).typeMessage("\"{0}\" must be a number of seconds")
        ._list("roles", Yavi._string(c -> c.notBlank())).label("Roles").required("Select at least one role")
        .fromMap(Token::new)
        .readingParameters()
        .formattingWith(new MessageSourceMessageFormatter(messageSource::getMessage));
```

See [Messages and Localization](messages.md) for the formatter and the rule about messages written in code.

## A list in one parameter

`splitBy` after `_list` reads one parameter as a list: `?tags=java,yavi`, `?ids=1,2,3`, or one URI per line of a `textarea`.

```java
ValueValidator<Map<String, ?>, Article> articleValidator = Yavi.schema()
        ._string("title", c -> c.notBlank())
        ._list("tags", Yavi._string(c -> c.notBlank().lessThanOrEqual(10)), c -> c.unique()).splitBy(",")
        ._list("related", Yavi._integer(c -> c.positive())).splitBy(",").defaultValue(List.of())
        ._list("links", Yavi._object(URI.class)).splitBy(Pattern.compile("\\R")).defaultValue(List.of())
        .fromMap(Article::new)
        .readingParameters();
```

* The separator is a text taken literally or a `Pattern`. The elements are text that stands for a value, and their violations are located under their index (`tags[1]`).
* Every text is split, also the values of a repeated key: `tags=a,b&tags=c` is `[a, b, c]`.
* No element is dropped or trimmed. `a,,b` has three elements, and the second has no value. A separator at the end starts no element: `a,b,` is `[a, b]`.
* An empty parameter (`tags=`) has no value.
* `splitBy` is written after `_list`, before the other modifiers; only `label` and `typeMessage` may be written before it.
* A value that is neither text nor a list is reported with `object.type` (`"tags" must be of type array or string`), not taken for a list of one element.

## Limits and security

* An index is at most 255 (`ParameterTree.DEFAULT_MAX_INDEX`). A greater number is a name, so `tags[999999999]` is reported with `object.type` and allocates nothing.
* `ParameterTree.of(map, maxIndex)` gives another limit. Give the same limit to the reader if the validator reads a tree with another one.
* A skipped index is an element that is `null`; the list has the length of the largest index sent, so the limit bounds what an untrusted request can allocate.
* A key that is not a path matches no property and is ignored, or reported by `rejectingUnknownProperties()`.
* `readingParameters()` validates the `ParameterTree` of the map, which any validator reads with `readingWith(ParameterTree.reader())`.

## Writing parameters back

A form that is not valid is shown again with the text that was sent, without copying the fields by hand. `ParameterTree` returns the parameters in the shape of the schema, and the same call gives the empty form for a request that lacks a parameter. See [Writing an Object Back](writing-back.md#parameters-and-forms).
