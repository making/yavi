/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
 * A property that its schema {@linkplain am.ik.yavi.builder.Withholdable#withheld()
 * withholds} is written only by a view that names it, so that a view that excludes is not
 * a list of what must not leave that somebody has to keep complete:
 *
 * <pre>{@code
 * ObjectInputValidator.Reversible<User> userValidator = Yavi.reversibleSchema()
 *     ._integer("id")
 *     ._string("name")
 *     ._string("password_hash").withheld()
 *     .fromMapReversible(User::new, User::id, User::name, User::passwordHash);
 *
 * userValidator.toMap(user); // {id=1, name=Taro}
 * userValidator.excluding("name").toMap(user); // {id=1}
 * userValidator.including("id", "password_hash").toMap(user); // {id=1, password_hash=...}
 * orderValidator.excluding("memo").toMap(order); // {id=7, user={id=1, name=Taro}}
 * orderValidator.including("user", "user.password_hash"); // the user, and its hash too
 * }</pre>
 *
 * <ul>
 * <li>{@link ObjectInputValidator.Reversible#toMap(Object) toMap} of the validator and a
 * view that excludes do not write a withheld property, at any depth: not that of a nested
 * object, of the elements of a list or of an embedded validator. Excluding it is an
 * {@link IllegalArgumentException}, because it has no effect.</li>
 * <li>A view that includes writes it if it is named. The name of a nested object stands
 * for its properties that are not withheld; the path to a withheld one is given next to
 * it to write that one too.</li>
 * <li>A property of a union is withheld if an alternative that defines it withholds
 * it.</li>
 * <li>{@link ObjectInputValidator.Reversible#reverse(Object) reverse} of the validator
 * writes every property: it is what is read back.</li>
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

	private static final Shape ALL = new All();

	private static final Shape WHOLE = new Whole();

	private static final Shape NOTHING = new Nothing();

	private final Writer<X> writer;

	private final Described described;

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

	/**
	 * The value as the validator writes it, with the properties that are withheld. This
	 * is what a view that is not made narrower writes.
	 */
	private record All() implements Shape {
	}

	/** The value without the properties that are withheld, at any depth. */
	private record Whole() implements Shape {
	}

	/** No entry. */
	private record Nothing() implements Shape {
	}

	/** The elements of a list or the values of a map, each with the given shape. */
	private record Each(Shape element) implements Shape {
	}

	/**
	 * An object: the named properties with their shapes, and the others that are not
	 * withheld as a whole or not at all.
	 */
	private record Obj(Map<String, Shape> named, boolean others) implements Shape {
	}

	/**
	 * What the descriptions of a value tell about it, one for every way to read it. It is
	 * found when it is asked for and kept: a structure that contains itself is described
	 * as far as a written value goes.
	 */
	private static final class Described {

		private static final Described UNKNOWN = new Described(List.of());

		private final List<ValueDescription> descriptions;

		private volatile @Nullable Content content;

		private volatile @Nullable Boolean plain;

		private volatile @Nullable Described each;

		private volatile @Nullable Described others;

		/** The values of the properties that an object defines, by their name. */
		private final Map<String, Described> properties = new ConcurrentHashMap<>();

		private Described(List<ValueDescription> descriptions) {
			this.descriptions = descriptions;
		}

		/**
		 * What a value is read as.
		 *
		 * @param objects the descriptions of the value as an object
		 * @param discriminators the names of the properties that select an alternative
		 * @param elements the descriptions of the elements of the value as a list
		 * @param values the descriptions of the values of the value as a map
		 * @param withheld the names of the properties that a way to read the value as an
		 * object withholds
		 */
		private record Content(List<ValueDescription.Properties> objects, Set<String> discriminators,
				List<ValueDescription> elements, List<ValueDescription> values, Set<String> withheld) {

			private void add(ValueDescription description) {
				switch (description) {
					case ValueDescription.Properties properties -> this.objects.add(properties);
					case ValueDescription.AnyOf anyOf -> {
						final int before = this.objects.size();
						anyOf.alternatives().forEach(this::add);
						if (anyOf.discriminator() != null && this.objects.size() > before) {
							this.discriminators.add(anyOf.discriminator());
						}
					}
					case ValueDescription.NotNull notNull -> this.add(notNull.value());
					case ValueDescription.Optional optional -> this.add(optional.value());
					case ValueDescription.Presence presence -> this.add(presence.value());
					case ValueDescription.Required required -> this.add(required.value());
					case ValueDescription.Reference reference -> this.add(reference.target());
					case ValueDescription.Elements elements -> this.elements.add(elements.element());
					case ValueDescription.Entries entries -> this.values.add(entries.value());
					case ValueDescription.Value value -> {
					}
					case ValueDescription.Unknown unknown -> {
					}
					case ValueDescription.Never never -> {
					}
				}
			}

		}

		private Content content() {
			Content content = this.content;
			if (content == null) {
				final Set<String> withheld = new HashSet<>();
				content = new Content(new ArrayList<>(), new LinkedHashSet<>(), new ArrayList<>(), new ArrayList<>(),
						withheld);
				this.descriptions.forEach(content::add);
				content.objects().forEach(object -> object.properties().forEach(property -> {
					if (property.withheld()) {
						withheld.add(property.name());
					}
				}));
				this.content = content;
			}
			return content;
		}

		/** Returns the names of the properties that the value defines as an object. */
		private Set<String> names() {
			final Set<String> names = new LinkedHashSet<>();
			this.content()
				.objects()
				.forEach(object -> object.properties().forEach(property -> names.add(property.name())));
			return names;
		}

		private Set<String> discriminators() {
			return this.content().discriminators();
		}

		/**
		 * Returns whether a way to read the value as an object withholds the property of
		 * the given name.
		 */
		private boolean withholds(String name) {
			return this.content().withheld().contains(name);
		}

		/**
		 * Returns what is known about the value of the given key of the value as an
		 * object or a map.
		 */
		private Described property(String name) {
			final Described defined = this.properties.get(name);
			if (defined != null) {
				return defined;
			}
			final Content content = this.content();
			final List<ValueDescription> values = new ArrayList<>();
			for (ValueDescription.Properties object : content.objects()) {
				for (ValueDescription.Property property : object.properties()) {
					if (property.name().equals(name)) {
						values.add(property.value());
					}
				}
			}
			if (values.isEmpty()) {
				// a key of the rest or of a map, of which there are as many as the values
				// say
				Described others = this.others;
				if (others == null) {
					final List<ValueDescription> rest = new ArrayList<>(content.values());
					content.objects().forEach(object -> {
						if (object.rest() != null) {
							rest.add(object.rest());
						}
					});
					others = rest.isEmpty() ? UNKNOWN : new Described(rest);
					this.others = others;
				}
				return others;
			}
			values.addAll(content.values());
			return this.properties.computeIfAbsent(name, key -> new Described(values));
		}

		/**
		 * Returns what is known about the elements of the value as a list and about its
		 * values as a map.
		 */
		private Described each() {
			Described each = this.each;
			if (each == null) {
				final Content content = this.content();
				final List<ValueDescription> elements = new ArrayList<>(content.elements());
				elements.addAll(content.values());
				each = elements.isEmpty() ? UNKNOWN : new Described(elements);
				this.each = each;
			}
			return each;
		}

		/**
		 * Returns whether no property of the value is withheld, at any depth, so that it
		 * is written as it is.
		 */
		private boolean isPlain() {
			Boolean plain = this.plain;
			if (plain == null) {
				final Set<ValueDescription.Reference> seen = Collections.newSetFromMap(new IdentityHashMap<>());
				plain = this.descriptions.stream().noneMatch(description -> withholds(description, seen));
				this.plain = plain;
			}
			return plain;
		}

		private static boolean withholds(@Nullable ValueDescription description, Set<ValueDescription.Reference> seen) {
			return switch (description) {
				case null -> false;
				case ValueDescription.Properties properties -> withholds(properties.rest(), seen) || properties
					.properties()
					.stream()
					.anyMatch(property -> property.withheld() || withholds(property.value(), seen));
				case ValueDescription.AnyOf anyOf ->
					anyOf.alternatives().stream().anyMatch(alternative -> withholds(alternative, seen));
				case ValueDescription.NotNull notNull -> withholds(notNull.value(), seen);
				case ValueDescription.Optional optional -> withholds(optional.value(), seen);
				case ValueDescription.Presence presence -> withholds(presence.value(), seen);
				case ValueDescription.Required required -> withholds(required.value(), seen);
				// a target that is seen again adds nothing to what it told before
				case ValueDescription.Reference reference -> seen.add(reference) && withholds(reference.target(), seen);
				case ValueDescription.Elements elements -> withholds(elements.element(), seen);
				case ValueDescription.Entries entries -> withholds(entries.value(), seen);
				case ValueDescription.Value value -> false;
				case ValueDescription.Unknown unknown -> false;
				case ValueDescription.Never never -> false;
			};
		}

	}

	/**
	 * The objects that a value is, or that it is a list or a map of.
	 *
	 * @param object what is known about the objects
	 * @param depth the number of lists and maps that the objects are the elements of
	 */
	private record Level(Described object, int depth) {
	}

	private PropertyView(Writer<X> writer, Described described, Shape shape) {
		this.writer = writer;
		this.described = described;
		this.shape = shape;
	}

	/**
	 * Returns the view of all that the given writer writes.
	 * @param writer writes an object as its map
	 * @param description the description of the validator that reads the map
	 */
	static <X> PropertyView<X> of(Writer<X> writer, ValueDescription description) {
		return new PropertyView<>(writer, new Described(List.of(description)), ALL);
	}

	/**
	 * Returns the view of the properties that the writer of this view writes and that are
	 * not withheld.
	 */
	PropertyView<X> withoutWithheld() {
		return new PropertyView<>(this.writer, this.described, written(this.shape));
	}

	/**
	 * Returns a view that writes only the given properties of those that this view
	 * writes.
	 * @param name the name of a property, or the path to a property of a nested object
	 * @param names the names of the other properties
	 * @return a narrower view
	 * @throws IllegalArgumentException if the validator does not define a property of a
	 * given name, if this view does not write it, if a name is given twice or with a
	 * property of the same object that is not withheld, or if a name is the discriminator
	 * of a union
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
	 * given name, if this view does not write it, as it does not write a property that is
	 * withheld unless it includes it, if a name is given twice or with a property of the
	 * same object, or if a name is the discriminator of a union
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
		if ((this.shape instanceof All) || (this.shape instanceof Whole && this.described.isPlain())) {
			return this.writer.write(object, name -> true);
		}
		final Predicate<String> names = name -> !(shapeOf(this.shape, name, this.described) instanceof Nothing);
		final Object written = prune(this.writer.write(object, names), this.shape, this.described);
		return (Map<String, Object>) Objects.requireNonNull(written);
	}

	private PropertyView<X> narrowed(boolean including, String name, String... names) {
		final List<String> paths = new ArrayList<>(names.length + 1);
		paths.add(Objects.requireNonNull(name, "'name' must not be null."));
		for (String other : names) {
			paths.add(Objects.requireNonNull(other, "'names' must not contain null."));
		}
		return new PropertyView<>(this.writer, this.described,
				narrow(this.shape, this.described, paths, including, false, ""));
	}

	/**
	 * Returns the shape of the property with the given name of an object with the given
	 * shape.
	 */
	private static Shape shapeOf(Shape shape, String name, Described object) {
		return switch (shape) {
			case All all -> ALL;
			case Obj properties -> {
				final Shape named = properties.named().get(name);
				yield named != null ? named : properties.others() && !object.withholds(name) ? WHOLE : NOTHING;
			}
			default -> object.withholds(name) ? NOTHING : WHOLE;
		};
	}

	/**
	 * Returns the given shape of a value, without the properties of the given paths or
	 * with them only.
	 * @param whole whether the value is included as a whole, so that the paths add the
	 * properties of it that are withheld
	 * @param prefix the path to the value, which ends with a period unless it is empty
	 */
	private static Shape narrow(Shape shape, Described value, List<String> paths, boolean including, boolean whole,
			String prefix) {
		final Level level = level(value, prefix);
		final Described object = level.object();
		Shape inner = shape;
		for (int i = 0; i < level.depth() && inner instanceof Each each; i++) {
			inner = each.element();
		}
		final Shape current = inner;
		final Set<String> defined = object.names();
		final Set<String> discriminators = object.discriminators();
		final Set<String> named = new LinkedHashSet<>();
		final Map<String, List<String>> nested = new LinkedHashMap<>();
		for (String path : paths) {
			final String name = nameOf(path, defined);
			if (name == null) {
				throw new IllegalArgumentException(
						"The property '" + prefix + path + "' is not defined by the validator. The properties "
								+ (prefix.isEmpty() ? "" : "of '" + prefix.substring(0, prefix.length() - 1) + "' ")
								+ "are " + defined + ".");
			}
			if (discriminators.contains(name)) {
				throw new IllegalArgumentException("The discriminator '" + prefix + name
						+ "' is always written: it is neither included nor excluded.");
			}
			final boolean withheld = object.withholds(name);
			if (withheld && !including && current instanceof All) {
				throw new IllegalArgumentException("The property '" + prefix + name
						+ "' is withheld: a view writes it only if it includes it by its name, so excluding it has no effect.");
			}
			if (shapeOf(current, name, object) instanceof Nothing) {
				throw new IllegalArgumentException("The property '" + prefix + name
						+ "' is not written by the view that is made narrower" + (withheld
								? ": it is withheld, and written only by a view that includes it by its name." : "."));
			}
			if (path.length() > name.length()) {
				nested.computeIfAbsent(name, key -> new ArrayList<>()).add(path.substring(name.length() + 1));
			}
			else if (whole && !withheld) {
				throw new IllegalArgumentException("The property '" + prefix + name + "' is written as a part of '"
						+ prefix.substring(0, prefix.length() - 1) + "', which is given as a whole.");
			}
			else if (!named.add(name)) {
				throw new IllegalArgumentException("The property '" + prefix + name + "' is given twice.");
			}
		}
		final Map<String, Shape> shapes = new LinkedHashMap<>();
		final boolean others;
		if (including) {
			discriminators.forEach(name -> shapes.put(name, written(shapeOf(current, name, object))));
			others = whole;
		}
		else {
			others = !(current instanceof Obj properties) || properties.others();
			if (current instanceof Obj properties) {
				shapes.putAll(properties.named());
			}
		}
		final Set<String> wholes = new LinkedHashSet<>();
		named.forEach(name -> {
			if (!nested.containsKey(name)) {
				shapes.put(name, including ? written(shapeOf(current, name, object)) : NOTHING);
			}
			// a whole object with the paths to what it withholds
			else if (including && shapeOf(current, name, object) instanceof All) {
				wholes.add(name);
			}
			else {
				// a path to what the view does not write is what is wrong, rather than
				// the path being given
				narrow(shapeOf(current, name, object), object.property(name), nested.get(name), including, false,
						prefix + name + ".");
				throw new IllegalArgumentException("The property '" + prefix + name
						+ "' is given as a whole and with the paths to its properties " + nested.get(name) + ".");
			}
		});
		nested.forEach((name, rest) -> shapes.put(name, narrow(shapeOf(current, name, object), object.property(name),
				rest, including, wholes.contains(name) || (whole && !object.withholds(name)), prefix + name + ".")));
		Shape narrowed = new Obj(Collections.unmodifiableMap(shapes), others);
		for (int i = 0; i < level.depth(); i++) {
			narrowed = new Each(narrowed);
		}
		return narrowed;
	}

	/**
	 * Returns the given shape of a property as a view that is made narrower writes it:
	 * without what is withheld, unless it is named.
	 */
	private static Shape written(Shape shape) {
		return shape instanceof All ? WHOLE : shape;
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

	private static Level level(Described value, String prefix) {
		final Set<Integer> depths = new LinkedHashSet<>();
		value.descriptions.forEach(description -> collect(description, 0, depths));
		final String name = prefix.isEmpty() ? "The validator"
				: "The property '" + prefix.substring(0, prefix.length() - 1) + "'";
		if (depths.isEmpty()) {
			throw new IllegalArgumentException(
					name + " does not describe an object, so no property of it can be chosen. Name it as a whole.");
		}
		if (depths.size() > 1) {
			throw new IllegalArgumentException(name
					+ " is an object in one alternative and a list or a map of objects in another, so no property of it can be chosen. Name it as a whole.");
		}
		final int depth = depths.iterator().next();
		Described object = value;
		for (int i = 0; i < depth; i++) {
			object = object.each();
		}
		return new Level(object, depth);
	}

	/**
	 * Adds the numbers of lists and maps that the objects of the given description are
	 * the elements of.
	 */
	private static void collect(ValueDescription description, int depth, Set<Integer> depths) {
		switch (description) {
			case ValueDescription.Properties properties -> depths.add(depth);
			case ValueDescription.AnyOf anyOf ->
				anyOf.alternatives().forEach(alternative -> collect(alternative, depth, depths));
			case ValueDescription.NotNull notNull -> collect(notNull.value(), depth, depths);
			case ValueDescription.Optional optional -> collect(optional.value(), depth, depths);
			case ValueDescription.Presence presence -> collect(presence.value(), depth, depths);
			case ValueDescription.Required required -> collect(required.value(), depth, depths);
			case ValueDescription.Reference reference -> collect(reference.target(), depth, depths);
			case ValueDescription.Elements elements -> collect(elements.element(), depth + 1, depths);
			case ValueDescription.Entries entries -> collect(entries.value(), depth + 1, depths);
			case ValueDescription.Value value -> {
			}
			case ValueDescription.Unknown unknown -> {
			}
			case ValueDescription.Never never -> {
			}
		}
	}

	private static @Nullable Object prune(@Nullable Object value, Shape shape, Described described) {
		return switch (shape) {
			case All all -> value;
			case Nothing nothing -> throw new IllegalStateException("A value that is not written has no shape.");
			case Whole whole -> {
				if (described.isPlain()) {
					yield value;
				}
				if (value instanceof List<?> elements) {
					yield each(elements, WHOLE, described);
				}
				yield value instanceof Map<?, ?> properties ? properties(properties, WHOLE, described) : value;
			}
			case Each each -> {
				if (value instanceof List<?> elements) {
					yield each(elements, each.element(), described);
				}
				if (value instanceof Map<?, ?> entries) {
					final Described entry = described.each();
					final Map<Object, @Nullable Object> pruned = new LinkedHashMap<>();
					entries.forEach((key, element) -> pruned.put(key, prune(element, each.element(), entry)));
					yield pruned;
				}
				yield value;
			}
			case Obj object -> value instanceof Map<?, ?> properties ? properties(properties, object, described)
					: value;
		};
	}

	private static List<@Nullable Object> each(List<?> elements, Shape shape, Described described) {
		final Described element = described.each();
		final List<@Nullable Object> pruned = new ArrayList<>(elements.size());
		elements.forEach(value -> pruned.add(prune(value, shape, element)));
		return pruned;
	}

	private static Map<Object, @Nullable Object> properties(Map<?, ?> properties, Shape shape, Described described) {
		final Map<Object, @Nullable Object> pruned = new LinkedHashMap<>();
		properties.forEach((key, property) -> {
			final String name = String.valueOf(key);
			final Shape kept = shapeOf(shape, name, described);
			if (!(kept instanceof Nothing)) {
				pruned.put(key, prune(property, kept, described.property(name)));
			}
		});
		return pruned;
	}

}
