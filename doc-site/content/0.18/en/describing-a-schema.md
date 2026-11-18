# Describing a Schema

A validator describes the input it expects. This page shows the description tree, the JSON Schema generated from it, and what cannot be described.

## Descriptions

`description()` returns a read-only `ValueDescription`: a tree of objects with properties, lists, maps, alternatives and values with their constraints. A tool can walk it without the classes the validator was built from.

```java
ValueDescription description = userValidator.description();

ValueDescription.Properties object = (ValueDescription.Properties) description;
for (ValueDescription.Property property : object.properties()) {
    System.out.println(property.name() + ": " + property.value());
}
```

A constraint is described by its message key and arguments, for example `container.lessThanOrEqual` with `[14]`. A property that is withheld is marked as such. The description carries the name of the property and, for a nested value, its structure.

The description is immutable and is built from the schema, not from an input. A schema that reads a map, a JSON tree and request parameters has one description, because the properties are the same.

## JSON Schema

`JsonSchema` is a consumer of the description. It needs no JSON library:

```java
Map<String, Object> schema = JsonSchema.of(userValidator.description());
String json = JsonSchema.toJson(userValidator.description());
```

The output follows JSON Schema draft 2020-12:

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "email": { "type": "string", "minLength": 1, "format": "email" },
    "nickname": { "type": ["string", "null"], "minLength": 1 },
    "age": { "type": ["integer", "null"], "minimum": 0, "default": 20 },
    "tags": { "type": ["array", "null"], "items": { "type": "string", "minLength": 1 }, "default": [] }
  },
  "required": ["email"]
}
```

* A property is `required` unless it accepts an absent key. A property with `presence()` is never `required`.
* A property that accepts `null` lists `null` in its type, and a `defaultValue(...)` or `recover(...)` is written as `default`.
* A required value whose constraints accept `null` is described with the constraint `object.notNull`.
* A validator that rejects unknown properties is written with `additionalProperties: false`, and one that reads the rest with the schema of its values.
* The constraints of a list or a map are written as `minItems`, `maxItems`, `uniqueItems`, `minProperties` and `maxProperties`.
* A withheld property is `writeOnly`.
* The alternatives of a discriminated union are an `AnyOf` whose `discriminator` is the name of the key that selects one.

The generated schema describes the input of the validator. It is not a schema of the object that the validator creates, so it has the key and type of a property before `map`.

## Recursive and shared definitions

A recursive structure is described with a `ValueDescription.Reference` where it contains itself. It is written as `$ref`, to `#` for the described validator itself, and otherwise to `$defs` under the name given to `ValueValidator.lazy(name, ...)` or `ValueValidator.recursive(name, ...)`:

```java
ObjectInputValidator<Comment> commentValidator = ValueValidator.recursive("comment", self -> Yavi.schema()
        ._string("body", c -> c.notBlank())
        ._list("replies", self).defaultValue(List.of())
        .fromMap(Comment::new));

JsonSchema.toJson(commentValidator.description());
// {"$defs":{"comment":{...}},"$ref":"#/$defs/comment"}
```

A reference to the described validator itself is written as `#`. A name given to `lazy` or `recursive` is used for a definition that is referred to from more than one place.

## Limitations

* A lambda and the result of `ValueValidator.compose` are unknown. Describe them with `describedAs(description)`.
* Conditional constraints and `constraintOnTarget` rules are not described. They are not part of the shape of the input, or they span several properties.
* A constraint without a JSON Schema keyword is omitted. For example, `unique(key)` has no keyword and is not written; `unique()` is written as `uniqueItems`.
* A bean `Validator` built with `ValidatorBuilder` is not described. Use `Yavi.schema()` for the input and the classic validator for the object.
* A `renaming` view and an `including` or `excluding` view are not part of the description of the validator. They write an object; the description still describes the input.
* A description reflects what the validator checks. It does not replace validation: a client that trusts the schema still needs the validator on the server.
