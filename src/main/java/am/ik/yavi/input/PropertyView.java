/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import am.ik.yavi.core.ValueDescription;
import org.jspecify.annotations.Nullable;

/**
 * Some of the properties of the map that a reversible validator writes an object back as:
 * a list shows the {@code id} and the {@code name}, a response leaves out the
 * {@code password}, an {@code UPDATE} binds the columns that may change.
 *
 * <pre>{@code
 * ObjectInputValidator.Reversible<User> userValidator = Yavi.reversibleSchema()
 *     ._integer("id")
 *     ._string("name")
 *     ._string("password")
 *     ._object("address", addressValidator) // city, street
 *     .fromMapReversible(User::new, User::id, User::name, User::password, User::address);
 *
 * // built once, next to the validator: a name that the validator does not define fails here
 * static final PropertyView<User> PUBLIC = userValidator.excluding("password");
 * static final PropertyView<User> SUMMARY = userValidator.including("id", "name", "address.city");
 *
 * PUBLIC.toMap(user); // {id=1, name=Taro, address={city=Tokyo, street=1-1}}
 * SUMMARY.toMap(user); // {id=1, name=Taro, address={city=Tokyo}}
 * }</pre>
 *
 * <p>
 * A view is built from a validator with
 * {@link ObjectInputValidator.Reversible#including(String, String...) including} or
 * {@link ObjectInputValidator.Reversible#excluding(String, String...) excluding}, and is
 * made narrower with {@link #including(String, String...)} and
 * {@link #excluding(String, String...)}. The names are checked against what the validator
 * {@linkplain am.ik.yavi.core.ValueValidator#description() describes} when the view is
 * built, not when an object is written: a name that the validator does not define, that
 * the view does not write or that has no effect is an {@link IllegalArgumentException}.
 * Build a view once, as a constant next to its validator, so that a mistyped name fails
 * when the class is loaded and not on the path that writes.
 *
 * <p>
 * A property is named as its violations are:
 * <ul>
 * <li>a property of a nested object by the path to it, {@code address.city}. The name of
 * the nested object, {@code address}, stands for all its properties;</li>
 * <li>a property of the elements of a list and of the values of a map by the same path,
 * without an index or a key: {@code addresses.city};</li>
 * <li>a property of an embedded validator by its own name, as it is in the same map;</li>
 * <li>a property of a union if an alternative defines it. An alternative that does not
 * define it is written without it.</li>
 * </ul>
 * A path names the properties at its depth only: {@code children.memo} of a validator
 * that refers to itself is the {@code memo} of the children, not of their children.
 *
 * <p>
 * What is written:
 * <ul>
 * <li>A property that is not in the view has no entry: it is not written as {@code null},
 * so a view is for a statement with fewer parameters, not a way to bind {@code null}. The
 * accessor of a property of the validator that the view is built from is not called if
 * the property is not written.</li>
 * <li>The rest of the properties ({@code _rest}), whose names are not known when the view
 * is built, is written by a view that excludes and not by one that includes.</li>
 * <li>The discriminator of a union is always written, by the view of a union and for a
 * union that is nested: it is neither included nor excluded. The discriminator of an
 * embedded union is a property of the object that embeds it, and is named as any
 * other.</li>
 * </ul>
 *
 * <p>
 * A view only writes. It is not a validator and not a
 * {@link am.ik.yavi.core.ReversibleValueValidator}: a map without a required property is
 * not read back, so the round trip that defines
 * {@link ObjectInputValidator.Reversible#toMap(Object)} does not hold for it, and it does
 * not change what the validator reads.
 *
 * @param <X> the type of the written object
 * @since 0.18.0
 * @see ObjectInputValidator.Reversible#including(String, String...)
 * @see DiscriminatedUnionValidator.Reversible#including(String, String...)
 */
public final class PropertyView<X> {

	private static final Shape WHOLE = new Whole();

	private static final Shape NOTHING = new Nothing();

	private final Writer<X> writer;

	private final ValueDescription description;

	private final Shape shape;

	/**
	 * Writes an object as the map that a validator reads it from.
	 *
	 * @param <X> the type of the written object
	 */
	@FunctionalInterface
	interface Writer<X> {

		/**
		 * Returns the map of the given object.
		 * @param object the object to write
		 * @param names tells whether the property of the validator with the given name is
		 * written; a property that is not may be left out
		 */
		Map<String, Object> write(X object, Predicate<String> names);

	}

	/**
	 * Which of the properties of a written value are kept.
	 */
	private sealed interface Shape {

	}

	/** The value as it is written. */
	private record Whole() implements Shape {
	}

	/** No entry. */
	private record Nothing() implements Shape {
	}

	/** The elements of a list or the values of a map, each with the given shape. */
	private record Each(Shape element) implements Shape {
	}

	/**
	 * An object: the named properties with their shapes, and the others as a whole or not
	 * at all.
	 */
	private record Obj(Map<String, Shape> named, boolean others) implements Shape {

		private Shape of(String name) {
			final Shape shape = this.named.get(name);
			return shape != null ? shape : this.others ? WHOLE : NOTHING;
		}

	}

	/**
	 * The objects that a value is read as.
	 *
	 * @param objects the descriptions of the object, one for every way to read it
	 * @param discriminators the names of the properties that select an alternative
	 * @param depth the number of lists and maps that the objects are the elements of
	 */
	private record Level(List<ValueDescription.Properties> objects, Set<String> discriminators, int depth) {

		private Set<String> names() {
			final Set<String> names = new LinkedHashSet<>();
			this.objects.forEach(object -> object.properties().forEach(property -> names.add(property.name())));
			return names;
		}

		private List<ValueDescription> valuesOf(String name) {
			final List<ValueDescription> values = new ArrayList<>();
			this.objects.forEach(object -> object.properties().forEach(property -> {
				if (property.name().equals(name)) {
					values.add(property.value());
				}
			}));
			return values;
		}

	}

	private PropertyView(Writer<X> writer, ValueDescription description, Shape shape) {
		this.writer = writer;
		this.description = description;
		this.shape = shape;
	}

	/**
	 * Returns the view of all that the given writer writes.
	 * @param writer writes an object as its map
	 * @param description the description of the validator that reads the map
	 */
	static <X> PropertyView<X> of(Writer<X> writer, ValueDescription description) {
		return new PropertyView<>(writer, description, WHOLE);
	}

	/**
	 * Returns a view that writes only the given properties of those that this view
	 * writes.
	 * @param name the name of a property, or the path to a property of a nested object
	 * @param names the names of the other properties
	 * @return a narrower view
	 * @throws IllegalArgumentException if the validator does not define a property of a
	 * given name, if this view does not write it, if a name is given twice or with a
	 * property of the same object, or if a name is the discriminator of a union
	 */
	public PropertyView<X> including(String name, String... names) {
		return this.narrowed(true, name, names);
	}

	/**
	 * Returns a view that writes the properties that this view writes except the given
	 * ones.
	 * @param name the name of a property, or the path to a property of a nested object
	 * @param names the names of the other properties
	 * @return a narrower view
	 * @throws IllegalArgumentException if the validator does not define a property of a
	 * given name, if this view does not write it, if a name is given twice or with a
	 * property of the same object, or if a name is the discriminator of a union
	 */
	public PropertyView<X> excluding(String name, String... names) {
		return this.narrowed(false, name, names);
	}

	/**
	 * Returns the properties of this view of the map that the validator reads the given
	 * object from.
	 * @param object an object that the validator creates
	 * @return a new map of the properties in the order of the validator, which the caller
	 * may change. A property that is not in the view has no entry
	 * @throws IllegalArgumentException as {@code toMap} of the validator does
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> toMap(X object) {
		final Predicate<String> names = this.shape instanceof Obj properties
				? name -> !(properties.of(name) instanceof Nothing) : name -> true;
		final Object written = prune(this.writer.write(object, names), this.shape);
		return (Map<String, Object>) Objects.requireNonNull(written);
	}

	private PropertyView<X> narrowed(boolean including, String name, String... names) {
		final List<String> paths = new ArrayList<>(names.length + 1);
		paths.add(Objects.requireNonNull(name, "'name' must not be null."));
		for (String other : names) {
			paths.add(Objects.requireNonNull(other, "'names' must not contain null."));
		}
		return new PropertyView<>(this.writer, this.description,
				narrow(this.shape, List.of(this.description), paths, including, ""));
	}

	/**
	 * Returns the given shape of a value with the given descriptions, without the
	 * properties of the given paths or with them only.
	 * @param prefix the path to the value, which ends with a period unless it is empty
	 */
	private static Shape narrow(Shape shape, List<ValueDescription> descriptions, List<String> paths, boolean including,
			String prefix) {
		final Level level = level(descriptions, prefix);
		Shape inner = shape;
		for (int i = 0; i < level.depth() && inner instanceof Each each; i++) {
			inner = each.element();
		}
		final Obj current = inner instanceof Obj properties ? properties : new Obj(Map.of(), true);
		final Set<String> defined = level.names();
		final Set<String> whole = new LinkedHashSet<>();
		final Map<String, List<String>> nested = new LinkedHashMap<>();
		for (String path : paths) {
			final String name = nameOf(path, defined);
			if (name == null) {
				throw new IllegalArgumentException(
						"The property '" + prefix + path + "' is not defined by the validator. The properties "
								+ (prefix.isEmpty() ? "" : "of '" + prefix.substring(0, prefix.length() - 1) + "' ")
								+ "are " + defined + ".");
			}
			if (level.discriminators().contains(name)) {
				throw new IllegalArgumentException("The discriminator '" + prefix + name
						+ "' is always written: it is neither included nor excluded.");
			}
			if (current.of(name) instanceof Nothing) {
				throw new IllegalArgumentException(
						"The property '" + prefix + name + "' is not written by the view that is made narrower.");
			}
			if (path.length() > name.length()) {
				nested.computeIfAbsent(name, key -> new ArrayList<>()).add(path.substring(name.length() + 1));
			}
			else if (!whole.add(name)) {
				throw new IllegalArgumentException("The property '" + prefix + name + "' is given twice.");
			}
		}
		final Map<String, Shape> named = new LinkedHashMap<>();
		if (including) {
			level.discriminators().forEach(name -> named.put(name, current.of(name)));
		}
		else {
			named.putAll(current.named());
		}
		whole.forEach(name -> {
			if (nested.containsKey(name)) {
				throw new IllegalArgumentException("The property '" + prefix + name
						+ "' is given as a whole and with the paths to its properties " + nested.get(name) + ".");
			}
			named.put(name, including ? current.of(name) : NOTHING);
		});
		nested.forEach((name, rest) -> named.put(name,
				narrow(current.of(name), level.valuesOf(name), rest, including, prefix + name + ".")));
		Shape narrowed = new Obj(Collections.unmodifiableMap(named), !including && current.others());
		for (int i = 0; i < level.depth(); i++) {
			narrowed = new Each(narrowed);
		}
		return narrowed;
	}

	/**
	 * Returns the longest of the given names that the path starts with, so that a name
	 * with a period is found, or {@code null} if there is none.
	 */
	private static @Nullable String nameOf(String path, Set<String> names) {
		String found = null;
		for (String name : names) {
			if ((path.equals(name) || path.startsWith(name + "."))
					&& (found == null || name.length() > found.length())) {
				found = name;
			}
		}
		return found;
	}

	private static Level level(List<ValueDescription> descriptions, String prefix) {
		final List<ValueDescription.Properties> objects = new ArrayList<>();
		final Set<String> discriminators = new LinkedHashSet<>();
		final Set<Integer> depths = new LinkedHashSet<>();
		descriptions.forEach(description -> collect(description, 0, objects, discriminators, depths));
		final String value = prefix.isEmpty() ? "The validator"
				: "The property '" + prefix.substring(0, prefix.length() - 1) + "'";
		if (objects.isEmpty()) {
			throw new IllegalArgumentException(
					value + " does not describe an object, so no property of it can be chosen. Name it as a whole.");
		}
		if (depths.size() > 1) {
			throw new IllegalArgumentException(value
					+ " is an object in one alternative and a list or a map of objects in another, so no property of it can be chosen. Name it as a whole.");
		}
		return new Level(objects, discriminators, depths.iterator().next());
	}

	private static void collect(ValueDescription description, int depth, List<ValueDescription.Properties> objects,
			Set<String> discriminators, Set<Integer> depths) {
		switch (description) {
			case ValueDescription.Properties properties -> {
				objects.add(properties);
				depths.add(depth);
			}
			case ValueDescription.AnyOf anyOf -> {
				final int before = objects.size();
				anyOf.alternatives()
					.forEach(alternative -> collect(alternative, depth, objects, discriminators, depths));
				if (anyOf.discriminator() != null && objects.size() > before) {
					discriminators.add(anyOf.discriminator());
				}
			}
			case ValueDescription.NotNull notNull -> collect(notNull.value(), depth, objects, discriminators, depths);
			case ValueDescription.Optional optional ->
				collect(optional.value(), depth, objects, discriminators, depths);
			case ValueDescription.Presence presence ->
				collect(presence.value(), depth, objects, discriminators, depths);
			case ValueDescription.Required required ->
				collect(required.value(), depth, objects, discriminators, depths);
			case ValueDescription.Reference reference ->
				collect(reference.target(), depth, objects, discriminators, depths);
			case ValueDescription.Elements elements ->
				collect(elements.element(), depth + 1, objects, discriminators, depths);
			case ValueDescription.Entries entries ->
				collect(entries.value(), depth + 1, objects, discriminators, depths);
			case ValueDescription.Value value -> {
			}
			case ValueDescription.Unknown unknown -> {
			}
			case ValueDescription.Never never -> {
			}
		}
	}

	private static @Nullable Object prune(@Nullable Object value, Shape shape) {
		return switch (shape) {
			case Whole whole -> value;
			case Nothing nothing -> throw new IllegalStateException("A value that is not written has no shape.");
			case Each each -> {
				if (value instanceof List<?> elements) {
					final List<@Nullable Object> pruned = new ArrayList<>(elements.size());
					elements.forEach(element -> pruned.add(prune(element, each.element())));
					yield pruned;
				}
				if (value instanceof Map<?, ?> entries) {
					final Map<Object, @Nullable Object> pruned = new LinkedHashMap<>();
					entries.forEach((key, entry) -> pruned.put(key, prune(entry, each.element())));
					yield pruned;
				}
				yield value;
			}
			case Obj object -> {
				if (!(value instanceof Map<?, ?> properties)) {
					yield value;
				}
				final Map<Object, @Nullable Object> pruned = new LinkedHashMap<>();
				properties.forEach((name, property) -> {
					final Shape kept = object.of(String.valueOf(name));
					if (!(kept instanceof Nothing)) {
						pruned.put(name, prune(property, kept));
					}
				});
				yield pruned;
			}
		};
	}

}
