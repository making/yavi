# Document what a reversible schema does not do for a form and a row

Difficulty: Low

Notes for `docs/NewFeatures_0.18.0.md` (and `.ja.md`), found by using the reversible
schema in an application.

## A form whose type is not the stored type

The form is read as `ArticleInput(title, body)`, the stored object is
`Article(id, title, body, createdAt)` or a class of a library. The appendix shares the
properties with `_embedded`, which needs a stored type that holds the shared object.
Where that type cannot be changed, the application still writes
`new ArticleInput(article.title(), article.body())` and gives it to `toMap`. State this
case: what is saved is the text of each value, `null` and the checkboxes, not the
mapping between the two types.

## A column that the driver does not store as its type

SQLite stores a `java.sql.Timestamp` as epoch milliseconds. `_instant("created_at")`
writes an `Instant`, which the driver stores as ISO-8601 text, and reads the existing
rows as `object.type`. Tests that insert their own rows do not find it. Add the reverse
mapping as the example of `map(f, g)` for a row:

```java
._long(Yavi._long("created_at").map(Instant::ofEpochMilli, Instant::toEpochMilli))
```

## An empty form with initial values

Show how a new form gets its initial values (`sortOrder=0`, a checked checkbox):
`ParameterTree.ofValues(Map.of("sortOrder", 0, "agreed", true)).toMap(description)`.
Verify that a partial map works before writing it.
