# Write a list that is split by a pattern back as the text of its field

Difficulty: Medium

## Problem

A list with `splitBy(Pattern)` is one text field, such as a textarea with one value per
line. `ParameterTree.of(params).toMap(description)` returns the text that was sent, but
`toMap(x)` of a reversible validator writes a `List`, so a stored object gives the same
key another shape and the template shows `[a, b]`:

```java
ObjectInputValidator.Reversible<Feed> feedSchema = Yavi.reversibleSchema()
        ._string("title")
        ._list("urls", Yavi._string(c -> c.url())).splitBy(Pattern.compile("\\s*\\R\\s*"))
        .defaultValue(List.of())
        .fromMapReversible(Feed::new, Feed::title, Feed::urls);

ParameterTree.of(Map.of("title", "News", "urls", "https://a.example\nhttps://b.example"))
        .toMap(feedSchema.description());
// {title=News, urls="https://a.example\nhttps://b.example"}
ParameterTree.ofValues(feedSchema.toMap(feed)).toMap(feedSchema.description());
// {title=News, urls=[https://a.example, https://b.example]}   <textarea>{{form.urls}}</textarea>
```

The application joins the list by hand for every such field, so the first display and
the form that is shown again are not one code path. `splitBy(",")` has no such gap: it
writes the joined text.

## Proposal

Give the pattern the text that joins the elements when they are written:
`splitBy(Pattern.compile("\\s*\\R\\s*"), "\n")`. `toMap` then writes the joined text, as
`splitBy(",")` does, with the same rule for an empty list.

## To decide

* Whether `splitBy(Pattern)` without the text stays in `Yavi.reversibleSchema()`. A list
  that is read from one text and written as a list is not what a form shows; rejecting
  it by type there would follow the rule of the reversible schema.
* The joining text must be split by the pattern (`"\n"` by `\R`). Check it when the
  schema is built.
