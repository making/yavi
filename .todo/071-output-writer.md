# Write an object back as the tree of a JSON library

Difficulty: Medium

## Problem

`ObjectInputValidator.Reversible#toMap` writes a `Map<String, Object>`. A validator reads
the tree of a JSON library without copying it to a map (`readingWith(InputReader<N>)`),
but there is no counterpart that writes: the application gives the map to the mapper of
the library, which builds its tree from it.

## Proposal

An `OutputWriter<N>`, the counterpart of `InputReader<N>`, which creates an object, an
array, a null node and a scalar of the tree model, and
`reversible.writingWith(writer).write(x)` that returns an `N`. The written tree is
defined by the round trip with `readingWith(reader)` of the same model.

## To decide

* Whether the gain over `mapper.valueToTree(validator.toMap(x))` justifies an interface
  that every tree model implements. If not, close this item as cancelled and say so in
  `docs/NewFeatures_0.18.0.md`.
* A `PropertyView` (`including`, `excluding` of a reversible validator) writes a map too.
  If an `OutputWriter` is added, give the view the same `writingWith(writer)`: the view
  holds the selection, the writer the target.
