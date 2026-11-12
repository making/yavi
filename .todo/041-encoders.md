# Decide whether YAVI provides encoders (a domain object to a map)

Difficulty: High

## Problem

Sections 29-32 of the Raoh tutorial are encoders, the reverse of decoders:
`object(property("id", Item::id, long_().contramap(ItemId::value)), ...)` produces a
`Map<String, Object>` for JDBC binding or JSON output, with `nested`, `list`,
`nullableProperty`, `propertyWithDefault` and `discriminate` for sealed types. Zod 4 has
codecs (`z.codec`, `z.encode`). YAVI has nothing; `docs/ComparisonWithRaoh.md` lists it
under "Not available".

## First step

Decide the scope before any design: YAVI is a validation library and an encoder validates
nothing. Arguments for: the property names and the discriminator of a
`MapInputValidator` / `DiscriminatedUnionValidator` are already written once and an
encoder repeats them, so a round trip defined in one place is the only form worth adding.
Arguments against: Jackson and record accessors already cover output, and 16-arity
generated code doubles.

If rejected, record the decision in `docs/ComparisonWithRaoh.md` ("Not available") and
`docs/ComparisonWithZod.md`, and close this item as cancelled.
