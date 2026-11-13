# Document `BindingResult` with `readingParameters()` in Spring MVC

Difficulty: Low

## Problem

`docs/NewFeatures_0.18.0.md` ("Validating request parameters") validates a
`@RequestParam MultiValueMap<String, String>` and stops at the `Validated`. It does not
say how a form is shown again with the submitted values and the errors. Nothing in
`README.md` or `docs/` mentions `BindingResult` any more, although
`ConstraintViolations#apply(result::rejectValue)` is still there.

Found when `demo-spring-mvc-yavi` was rewritten from `Yavi.arguments()` + a form record +
`BindingResult` to `Yavi.schema()...fromMap(User::new).readingParameters()`. The first
rewrite dropped `BindingResult` for `messagesByName()`, which cost:

* `th:field`, `#fields.hasErrors` and `th:errors` of Thymeleaf, replaced with hand-written
  `id`, `name`, `value` and loops over a map of messages;
* the message keys: `messagesByName()` has the messages only, so the MockMvc test could
  no longer use `model().attributeHasFieldErrorCode(...)` and compared the wording.

## What works (verified in the demo, Spring Boot 4.1.2, Thymeleaf)

Spring creates a `BindingResult` only for an object it binds, so a map needs a
`MapBindingResult`. No library and no Spring module is needed (YAVI fits Spring through
the shape of its API, such as `apply(result::rejectValue)`, and stays that way): a
`@ModelAttribute` method of the controller puts the parameters and their
`MapBindingResult` into the model, and Spring then resolves a `BindingResult` argument of
the handler, because it is the last attribute of the model. The handler reads as it did
with a form object in 0.17, and the template (`th:object`, `th:field`, `#fields`) and the
MockMvc test (`attributeHasFieldErrorCode`) are unchanged from the 0.17 version:

```java
@ModelAttribute
void userForm(@RequestParam Map<String, String> params, Model model) {
    model.addAllAttributes(new MapBindingResult(params, "userForm").getModel());
}

@GetMapping("/")
public String users(Model model) { ... return "users"; }

@PostMapping("/")
public String createUser(@RequestParam MultiValueMap<String, String> params, BindingResult result, Model model) {
    return switch (formValidator.validate(params)) {
        case Validated.Valid<User>(User user) -> { this.users.add(user); yield "redirect:/"; }
        case Validated.Invalid<User>(ConstraintViolations violations) -> {
            violations.apply(result::rejectValue);
            yield this.users(model);
        }
    };
}
```

* The field error has the message key as its code (`object.type`), the arguments and the
  default message, so a `MessageSource` still overrides the wording.
* `th:field="*{age}"` shows the rejected text (`abc`), which a typed form object could
  not hold.
* The `@ModelAttribute` method runs for the GET handler too, so the empty form renders.
* The redirect after a success carries no query parameters (a map is not appended).
* The `BindingResult` argument must not follow an argument that adds a model attribute.

A first version built the `MapBindingResult` in the handler and copied `getModel()` into
the model in both handlers; the `@ModelAttribute` method replaces that.

See `UserController` in https://github.com/making/demo-spring-mvc-yavi.

## Scope

- A short subsection in "Validating request parameters" of `docs/NewFeatures_0.18.0.md`
  and `.ja.md` with the code above, and the same in the Spring MVC part of the reference
  documentation if it has one for 0.18.
- Check before writing: a nested name (`address.city`, `tags[1]`) as the field of a
  `MapBindingResult` over the flat parameter map. `getFieldValue("address.city")` is a
  plain `map.get`, which matches a key written as `address.city` but not `address[city]`
  or `tags[]`, and a list has several values while `toSingleValueMap()` keeps the first.
  State the limit, or show how to handle it.
- Decide whether `messagesByName()` deserves a sibling that keeps the message keys. Not
  needed for Spring (the code above); `details()` already has `name` and `messageKey` for
  an API response. Add nothing unless a real use is found.
