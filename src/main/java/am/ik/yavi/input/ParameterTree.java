/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.CoercibleInputReader;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.StringCoercion;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import org.jspecify.annotations.Nullable;

/**
 * The tree that a flat map of parameters stands for, such as request parameters or a
 * submitted form. The keys of the map are paths and its values are text, possibly several
 * for one key, so the validators of nested objects, lists and maps read it like a parsed
 * JSON document:
 *
 * <pre>{@code
 * ValueValidator<Map<String, ?>, SearchForm> validator = Yavi.schema()
 *     ._string("q", c -> c.notBlank())
 *     ._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
 *     ._list("tags", Yavi._string(c -> c.notBlank()))
 *     ._object("address", addressValidator)
 *     .fromMap(SearchForm::new)
 *     .readingParameters();
 * // q=yavi&page=2&tags=a&tags=b&address.city=Tokyo
 * Validated<SearchForm> validated = validator.validate(parameters);
 * }</pre>
 *
 * {@code readingParameters()} of {@link ObjectInputValidator#readingParameters() a
 * validator of maps} is the usual way to read parameters. Any other validator reads a
 * tree with {@link #reader()}: {@code validator.readingWith(ParameterTree.reader())}
 * validates {@link #of(Map) ParameterTree.of(parameters)}.
 *
 * <h2>Keys</h2>
 *
 * A key is a name followed by any number of {@code .name} and {@code [name]}, which are
 * the same: {@code address.city} and {@code address[city]} are the property {@code city}
 * of the property {@code address}. A name does not contain {@code .} or {@code [} unless
 * it is written in brackets, where it holds anything but {@code ]}. A key may end with
 * {@code []}, which is ignored: {@code tags[]} is {@code tags}. A key that is not written
 * in this way, or that has more than {@value #MAX_DEPTH} names, is a property of the root
 * under its whole text, so it matches no property and is reported as an unknown property
 * if they are rejected.
 *
 * <h2>Values</h2>
 *
 * A value of the map that is an {@link Iterable} or an array gives the values of its key,
 * and any other value is the only one. This reads a {@code Map<String, String>}, a
 * {@code Map<String, List<String>>} such as the {@code MultiValueMap} of Spring and the
 * {@code Map<String, String[]>} of the Servlet API. The values are read as
 * {@link CoercibleInputReader#coercingStrings()} states: a string is text that stands for
 * a value of the type it is checked as, and an empty string is {@code null}.
 *
 * <h2>Shape</h2>
 *
 * Nothing in a map of parameters tells what a key is meant to be, so a node is whatever
 * the validator that reads it expects, as long as its content allows it:
 * <ul>
 * <li>A key with values is a scalar if it has one value and a list of its values whatever
 * their number is. Several values are reported with {@code object.type} where a scalar is
 * expected; none is picked.</li>
 * <li>A key with nested keys is an object. If all of its names are indexes, such as
 * {@code items[0].name} and {@code items[1].name}, it is also a list, whose elements are
 * in the order of the indexes whatever the order of the keys is. An index is a decimal
 * number without a leading zero, up to the largest index of the tree
 * ({@value #DEFAULT_MAX_INDEX} by default). A greater number is only a name, so
 * {@code tags[999999999]} is not a list and allocates nothing. An index that is skipped
 * is an element that is {@code null}, so the violations of the other elements are
 * reported at the index they were sent with.</li>
 * <li>A key that has both values and nested keys, such as {@code a=1&a.b=2}, is neither:
 * it is reported with {@code object.type} whatever is expected.</li>
 * </ul>
 * A list or an object that is empty cannot be sent: its key is absent, which is validated
 * like {@code null}. A list whose only value is an empty string is {@code null} too.
 *
 * <p>
 * The value reported in a violation, and given to a validator that takes an
 * {@code Object}, is the string of a scalar, an unmodifiable {@code List} or an
 * unmodifiable {@code Map<String, Object>}.
 *
 * <h2>Writing the parameters back</h2>
 *
 * {@link #toMap(ValueDescription)} returns the properties that a validator describes with
 * what was sent for them, in the shape that the validator reads them in, for example to
 * show a form again with the text that was sent:
 *
 * <pre>{@code
 * // name=+Alice+&tags=a&address[city]=Tokyo&utm_source=mail
 * ParameterTree tree = ParameterTree.of(parameters);
 * tree.toMap(validator.description());
 * // {name=" Alice ", age="", tags=[a], address={city="Tokyo"}}
 * tree.toParameters(validator.description());
 * // {name=" Alice ", age="", tags=[a], address.city="Tokyo"}
 * }</pre>
 *
 * The description is the one of the validator that validates the parameters, and the
 * largest index is the one that validator reads them with. {@link #parametersOf(Map)} is
 * the reverse of {@link #of(Map)} for any tree of JDK types.
 *
 * <p>
 * A tree is immutable and does not see the later changes of the map it was created from.
 *
 * @since 0.18.0
 * @see ObjectInputValidator#readingParameters()
 */
public final class ParameterTree {

	/**
	 * The largest index of a list unless another one is given to {@link #of(Map, int)}.
	 */
	public static final int DEFAULT_MAX_INDEX = 255;

	/**
	 * The largest number of names in a key.
	 */
	static final int MAX_DEPTH = 64;

	private static final InputReader<ParameterTree> READER = Reader.INSTANCE.coercingStrings();

	/** What a node is written as if it does not have the described shape. */
	private static final Object NONE = new Object();

	/** The values given for the key of this node. */
	private final List<@Nullable Object> values;

	/** The nodes of the nested keys by their name, in the order of their first key. */
	private Map<String, ParameterTree> children = Map.of();

	/**
	 * The number of elements if the names of all children are indexes; -1 as soon as one
	 * is not.
	 */
	private int length = 0;

	/** The value of this node as JDK types, created when it is first asked for. */
	private @Nullable Object value;

	private ParameterTree(List<@Nullable Object> values) {
		this.values = values;
	}

	/**
	 * Returns the tree of the given parameters. A list has at most
	 * {@value #DEFAULT_MAX_INDEX} as an index.
	 * @param parameters the parameters by their key; a value is one value or, if it is an
	 * {@link Iterable} or an array, the values of the key
	 * @return the root of the tree, which is an object
	 */
	public static ParameterTree of(Map<String, ?> parameters) {
		return of(parameters, DEFAULT_MAX_INDEX);
	}

	/**
	 * Returns the tree of the given parameters. The largest index bounds what one key can
	 * make a validator allocate: a key that is a few characters long creates a list of
	 * {@code maxIndex + 1} elements.
	 * @param parameters the parameters by their key; a value is one value or, if it is an
	 * {@link Iterable} or an array, the values of the key
	 * @param maxIndex the largest number that is taken as the index of a list
	 * @return the root of the tree, which is an object
	 * @throws IllegalArgumentException if the largest index is negative
	 */
	public static ParameterTree of(Map<String, ?> parameters, int maxIndex) {
		if (maxIndex < 0) {
			throw new IllegalArgumentException("'maxIndex' must not be negative.");
		}
		final ParameterTree root = new ParameterTree(new ArrayList<>(0));
		parameters.forEach((key, value) -> {
			final List<String> names = names(key);
			ParameterTree node = root;
			if (names == null) {
				node = node.child(key, maxIndex);
			}
			else {
				for (String name : names) {
					node = node.child(name, maxIndex);
				}
			}
			if (value instanceof Iterable<?>iterable) {
				iterable.forEach(node.values::add);
			}
			else if (value instanceof Object[]array) {
				node.values.addAll(Arrays.asList(array));
			}
			else {
				node.values.add(value);
			}
		});
		return root;
	}

	/**
	 * Returns the reader of the nodes of a tree, which reads their strings as
	 * {@link CoercibleInputReader#coercingStrings()} states.
	 * @return the reader of parameter trees
	 */
	public static InputReader<ParameterTree> reader() {
		return READER;
	}

	/**
	 * Returns the reader of the nodes of a tree, which reads their strings by the given
	 * rules.
	 * @param coercion the rules by which a string is taken as text
	 * @return the reader of parameter trees
	 */
	public static InputReader<ParameterTree> reader(StringCoercion coercion) {
		return Reader.INSTANCE.coercingStrings(coercion);
	}

	/**
	 * Returns a validator of a map of parameters that validates its tree with the given
	 * validator, whose strings are read by the given rules.
	 */
	static <X> ValueValidator<Map<String, ?>, X> reading(ValueValidator<?, X> validator, StringCoercion coercion) {
		final ValueValidator<ParameterTree, X> ofTree = validator.readingWith(reader(coercion));
		return new ValueValidator<>() {
			@Override
			public Validated<X> validate(@Nullable Map<String, ?> parameters, Locale locale,
					ConstraintContext constraintContext) {
				return ofTree.validate(parameters == null ? null : of(parameters), locale, constraintContext);
			}

			@Override
			public ValueDescription description() {
				return validator.description();
			}

			@Override
			public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
				return validator.readingWith(reader);
			}
		};
	}

	/**
	 * Returns the parameters as the given description reads them: a tree of JDK types
	 * that holds the described properties with what was sent for them and nothing else,
	 * for example to show a form again with the text that was sent.
	 *
	 * <pre>{@code
	 * ValueValidator<Map<String, ?>, Signup> validator = Yavi.schema()
	 *     ._string("name", c -> c.notBlank())
	 *     ._integer("age")
	 *     ._list("tags", Yavi._string()).defaultValue(List.of())
	 *     ._object("address", addressValidator)
	 *     .fromMap(Signup::new)
	 *     .readingParameters(StringCoercion.strict().trimming());
	 * // name=+Alice+&tags=a&address[city]=Tokyo&utm_source=mail
	 * Map<String, Object> form = ParameterTree.of(parameters).toMap(validator.description());
	 * // {name=" Alice ", age="", tags=[a], address={city="Tokyo"}}
	 * }</pre>
	 *
	 * <ul>
	 * <li>A value is the one that was sent, as it was sent: it is not trimmed, not
	 * coerced and not validated, so {@code abc} is returned for an {@code Integer}.</li>
	 * <li>The shape is the described one, whatever was sent: an object is a
	 * {@code Map<String, Object>} of its described properties in their order, a list is a
	 * {@code List} whatever the number of its values is, and a map is a {@code Map} of
	 * its keys. A template or a caller finds the same entries for every request.</li>
	 * <li>A single value that was not sent is an empty string, which is what an empty
	 * text field sends and what the validator reads like an absent key. A list or a map
	 * that was not sent is empty, and an object has its properties. This is also what is
	 * returned for a value that does not have the described shape, such as several values
	 * for a single value; the validator reports it with {@code object.type}.</li>
	 * <li>The following have no entry unless they were sent: a {@code Boolean}, because a
	 * checkbox that is not checked sends nothing and an empty string is not {@code false}
	 * to every template engine; a property whose absence is told from {@code null}
	 * ({@code presence()}); a value whose shape is not described; and a structure that
	 * contains itself, where it does so.</li>
	 * <li>An index that is skipped is an element that is {@code null}, so the other
	 * elements keep the index that their violations are reported at.</li>
	 * <li>A list that may be given as one text ({@code splitBy}) is that text if one was
	 * sent, as a text field shows it, the list of the values if several were sent and an
	 * empty string if none was. Nothing is split.</li>
	 * <li>A key that no property is described for is left out, so that a page does not
	 * show a parameter that the application did not ask for. A validator that reads the
	 * rest of the properties ({@code _rest}) asks for them.</li>
	 * <li>The properties of alternatives ({@link ValueDescription.AnyOf}), such as those
	 * of a {@link DiscriminatedUnionValidator}, are the properties of all of them,
	 * because a form holds the fields of all and the parameters that are not valid do not
	 * tell which one was meant. A property that the alternatives describe differently is
	 * written as the first of them that it has the shape of.</li>
	 * <li>A value whose shape is not described ({@link ValueDescription.Unknown}, the
	 * property of a validator that is a lambda, or a value of type {@code Object}) is
	 * what the tree holds: a string for one value, a list for several and a map or a list
	 * for nested keys.</li>
	 * </ul>
	 *
	 * The description must be the one of the validator that validates these parameters;
	 * nothing tells if it is another one. The returned tree holds every described value,
	 * a password included, so a template decides what it writes into a page.
	 * @param description the description of the validator that reads the parameters
	 * ({@link ValueValidator#description()})
	 * @return a new tree of {@code Map<String, Object>}, {@code List} and the values that
	 * were sent, which the caller may change, for example to add what a page needs; empty
	 * if the description is not the one of an object
	 * @see #toParameters(ValueDescription)
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> toMap(ValueDescription description) {
		return shape(this, description) instanceof Map<?, ?>map ? (Map<String, Object>) map : new LinkedHashMap<>();
	}

	/**
	 * Returns the parameters as the given description reads them, as
	 * {@link #toMap(ValueDescription)} does, under the keys they would be sent with: the
	 * names of the violations, such as {@code address.city} and {@code items[0].name}.
	 * This is the map that a {@code MapBindingResult} of Spring looks a field up in. It
	 * is {@link #parametersOf(Map) parametersOf(toMap(description))}.
	 * @param description the description of the validator that reads the parameters
	 * @return a new map of the values by their key, which the caller may change; a value
	 * is a {@code List} for a list of single values
	 */
	@SuppressWarnings({ "unchecked", "NullAway" })
	public Map<String, Object> toParameters(ValueDescription description) {
		// the tree of a description has no null but the skipped indexes, which have no
		// key
		return (Map<String, Object>) (Map<String, ?>) parametersOf(this.toMap(description));
	}

	/**
	 * Returns the parameters that stand for the given tree of JDK types, which is the
	 * reverse of {@link #of(Map)}: the tree of the returned parameters has the values of
	 * the given one.
	 *
	 * <pre>{@code
	 * ParameterTree.parametersOf(Map.of("address", Map.of("city", "Tokyo"), "tags", List.of("a", "b"),
	 *     "items", List.of(Map.of("name", "pen"))));
	 * // {address.city=Tokyo, tags=[a, b], items[0].name=pen}
	 * }</pre>
	 *
	 * <ul>
	 * <li>A {@code Map} is an object and its keys are names, which are written as
	 * {@code .name}, or as {@code [name]} if they hold {@code .} or {@code [}.</li>
	 * <li>A {@code List} of single values is the values of its key. A list that holds a
	 * map, a list or {@code null} is written with indexes ({@code items[0]}); an element
	 * that is {@code null} has no key, so the others keep their index.</li>
	 * <li>Any other value is the value of its key as it is; nothing is converted to
	 * text.</li>
	 * <li>A map or a list that is empty has no key, because it cannot be sent.</li>
	 * </ul>
	 * @param tree the properties of the root by their name
	 * @return a new map of the values by their key, in the order of the tree, which the
	 * caller may change
	 */
	public static Map<String, @Nullable Object> parametersOf(Map<String, ?> tree) {
		final Map<String, @Nullable Object> parameters = new LinkedHashMap<>();
		tree.forEach((name, value) -> flatten(name, value, parameters));
		return parameters;
	}

	private static void flatten(String key, @Nullable Object node, Map<String, @Nullable Object> parameters) {
		if (node instanceof Map<?, ?>object) {
			object.forEach((name, value) -> {
				final String text = String.valueOf(name);
				// the values of a key that has nested keys too are under an empty name
				flatten(text.isEmpty() ? key
						: text.indexOf('.') < 0 && text.indexOf('[') < 0 ? key + "." + text : key + "[" + text + "]",
						value, parameters);
			});
		}
		else if (node instanceof List<?>array) {
			if (array.stream()
				.anyMatch(element -> element == null || element instanceof Map || element instanceof List)) {
				for (int i = 0; i < array.size(); i++) {
					if (array.get(i) != null) {
						flatten(key + "[" + i + "]", array.get(i), parameters);
					}
				}
			}
			else if (!array.isEmpty()) {
				parameters.put(key, array);
			}
		}
		else {
			parameters.put(key, node);
		}
	}

	/**
	 * Returns the given node as the given description reads it, or what the description
	 * reads when nothing is sent if the node is {@code null} or does not have the
	 * described shape. It is {@link #NONE} if that is no value.
	 */
	private static Object shape(@Nullable ParameterTree node, ValueDescription description) {
		final List<ValueDescription> alternatives = new ArrayList<>(1);
		alternatives(description, node != null, alternatives);
		final List<ValueDescription.Properties> objects = new ArrayList<>(alternatives.size());
		for (ValueDescription alternative : alternatives) {
			if (alternative instanceof ValueDescription.Properties properties) {
				objects.add(properties);
			}
		}
		if (node != null) {
			if (!objects.isEmpty() && node.values.isEmpty()) {
				return object(node, objects);
			}
			for (ValueDescription alternative : alternatives) {
				final Object value = switch (alternative) {
					case ValueDescription.Value single -> node.asValue(single.type());
					case ValueDescription.Unknown unknown -> node.asValue(Object.class);
					case ValueDescription.Elements elements -> node.asList(elements);
					case ValueDescription.Entries entries -> {
						if (!node.values.isEmpty()) {
							yield NONE;
						}
						final Map<String, Object> map = new LinkedHashMap<>();
						node.children.forEach((name, child) -> put(map, name, shape(child, entries.value())));
						yield map;
					}
					default -> NONE;
				};
				if (value != NONE) {
					return value;
				}
			}
		}
		if (!objects.isEmpty()) {
			return object(null, objects);
		}
		for (ValueDescription alternative : alternatives) {
			final Object value = switch (alternative) {
				// a checkbox that is not checked sends nothing
				case ValueDescription.Value single ->
					single.type() == Boolean.class || single.type() == Object.class ? NONE : "";
				case ValueDescription.Elements elements -> elements.separator() != null ? "" : new ArrayList<>();
				case ValueDescription.Entries entries -> new LinkedHashMap<>();
				default -> NONE;
			};
			if (value != NONE) {
				return value;
			}
		}
		return NONE;
	}

	/**
	 * Adds the descriptions that the given one stands for: itself, the value it wraps,
	 * the target it refers to or its alternatives. A property that is not sent is not
	 * looked into where its absence is told from {@code null} and where a structure
	 * contains itself, which would not end.
	 */
	private static void alternatives(ValueDescription description, boolean sent,
			List<ValueDescription> alternatives) {
		switch (description) {
			case ValueDescription.AnyOf anyOf ->
				anyOf.alternatives().forEach(alternative -> alternatives(alternative, sent, alternatives));
			case ValueDescription.Optional optional -> alternatives(optional.value(), sent, alternatives);
			case ValueDescription.NotNull notNull -> alternatives(notNull.value(), sent, alternatives);
			case ValueDescription.Required required -> alternatives(required.value(), sent, alternatives);
			case ValueDescription.Presence presence when sent -> alternatives(presence.value(), sent, alternatives);
			case ValueDescription.Reference reference when sent ->
				alternatives(reference.target(), sent, alternatives);
			default -> alternatives.add(description);
		}
	}

	/**
	 * Returns the given object, or one that is not sent, with the properties of all the
	 * given descriptions, and with the others if one of them reads the rest.
	 */
	private static Object object(@Nullable ParameterTree node, List<ValueDescription.Properties> objects) {
		final Map<String, Object> map = new LinkedHashMap<>();
		final Set<String> names = new HashSet<>();
		for (ValueDescription.Properties object : objects) {
			for (ValueDescription.Property property : object.properties()) {
				names.add(property.name());
				if (!map.containsKey(property.name())) {
					put(map, property.name(),
							shape(node == null ? null : node.children.get(property.name()), property.value()));
				}
			}
		}
		if (node != null) {
			for (ValueDescription.Properties object : objects) {
				final ValueDescription rest = object.rest();
				if (rest != null) {
					node.children.forEach((name, child) -> {
						if (!names.contains(name) && !map.containsKey(name)) {
							put(map, name, shape(child, rest));
						}
					});
				}
			}
		}
		return map;
	}

	/**
	 * Returns the only value of this node, or the tree it holds if that is an instance of
	 * the given type.
	 */
	private Object asValue(Class<?> type) {
		final Object value = copy(this.value());
		return value != null && ((this.isLeaf() && this.values.size() == 1) || type.isInstance(value)) ? value : NONE;
	}

	private Object asList(ValueDescription.Elements elements) {
		if (this.isLeaf() && elements.separator() != null) {
			// the text is the one to show again, and so are the values of a repeated key
			return this.values.size() > 1 ? new ArrayList<>(this.values) : this.asValue(String.class);
		}
		if (!this.isArray()) {
			return NONE;
		}
		final List<@Nullable Object> list = new ArrayList<>();
		for (ParameterTree element : this.elements()) {
			final Object value = element == null ? NONE : shape(element, elements.element());
			list.add(value == NONE ? null : value);
		}
		return list;
	}

	/**
	 * Returns a tree that the caller may change.
	 */
	private static @Nullable Object copy(@Nullable Object value) {
		if (value instanceof Map<?, ?>map) {
			final Map<Object, @Nullable Object> copy = new LinkedHashMap<>();
			map.forEach((name, child) -> copy.put(name, copy(child)));
			return copy;
		}
		if (value instanceof List<?>list) {
			final List<@Nullable Object> copy = new ArrayList<>(list.size());
			list.forEach(element -> copy.add(copy(element)));
			return copy;
		}
		return value;
	}

	private static void put(Map<String, Object> map, String name, Object value) {
		if (value != NONE) {
			map.put(name, value);
		}
	}

	/**
	 * Returns the names of a key, or {@code null} if it is not written as a path.
	 */
	private static @Nullable List<String> names(String key) {
		final int length = key.length();
		final List<String> names = new ArrayList<>(4);
		int position = 0;
		while (true) {
			if (position == length || key.charAt(position) != '[') {
				// a name up to the next '.' or '[', after a '.' unless it is the first
				final int start = names.isEmpty() ? 0 : position + 1;
				int end = start;
				while (end < length && key.charAt(end) != '.' && key.charAt(end) != '[') {
					end++;
				}
				if (end == start) {
					return null;
				}
				names.add(key.substring(start, end));
				position = end;
			}
			else {
				final int close = key.indexOf(']', position);
				if (close < 0 || names.isEmpty()) {
					return null;
				}
				if (close == position + 1) {
					// "[]" says nothing more than the key before it, at its end only
					return close == length - 1 ? names : null;
				}
				names.add(key.substring(position + 1, close));
				position = close + 1;
				if (position < length && key.charAt(position) != '.' && key.charAt(position) != '[') {
					return null;
				}
			}
			if (position == length) {
				return names;
			}
			if (names.size() == MAX_DEPTH) {
				return null;
			}
		}
	}

	/**
	 * Returns the index that a name stands for, or -1 if it is not an index.
	 */
	private static int index(String name, int maxIndex) {
		final int length = name.length();
		if (length == 0 || length > 9 || (length > 1 && name.charAt(0) == '0')) {
			return -1;
		}
		int index = 0;
		for (int i = 0; i < length; i++) {
			final char digit = name.charAt(i);
			if (digit < '0' || digit > '9') {
				return -1;
			}
			index = index * 10 + (digit - '0');
		}
		return index <= maxIndex ? index : -1;
	}

	private ParameterTree child(String name, int maxIndex) {
		if (this.children.isEmpty()) {
			this.children = new LinkedHashMap<>();
		}
		ParameterTree child = this.children.get(name);
		if (child == null) {
			child = new ParameterTree(new ArrayList<>(1));
			this.children.put(name, child);
			final int index = index(name, maxIndex);
			this.length = this.length < 0 || index < 0 ? -1 : Math.max(this.length, index + 1);
		}
		return child;
	}

	private boolean isLeaf() {
		return this.children.isEmpty() && !this.values.isEmpty();
	}

	private boolean isObject() {
		// the root has no values, and is an object even if it has no children
		return this.values.isEmpty();
	}

	private boolean isArray() {
		return this.isLeaf() || (this.values.isEmpty() && this.length > 0);
	}

	private List<@Nullable ParameterTree> elements() {
		final List<@Nullable ParameterTree> elements = new ArrayList<>();
		if (this.isLeaf()) {
			for (Object element : this.values) {
				elements.add(new ParameterTree(Collections.singletonList(element)));
			}
		}
		else {
			for (int i = 0; i < this.length; i++) {
				elements.add(this.children.get(Integer.toString(i)));
			}
		}
		return elements;
	}

	/**
	 * Returns the value of this node as JDK types. It is {@code null} only for a leaf
	 * whose value is.
	 */
	private @Nullable Object value() {
		if (this.isLeaf()) {
			return this.values.size() == 1 ? this.values.get(0) : Collections.unmodifiableList(this.values);
		}
		Object value = this.value;
		if (value == null) {
			if (this.isArray()) {
				final List<@Nullable Object> list = new ArrayList<>(this.length);
				for (ParameterTree element : this.elements()) {
					list.add(element == null ? null : element.value());
				}
				value = Collections.unmodifiableList(list);
			}
			else {
				final Map<String, @Nullable Object> map = new LinkedHashMap<>();
				if (!this.values.isEmpty()) {
					// the values of a key that has nested keys too
					map.put("",
							this.values.size() == 1 ? this.values.get(0) : Collections.unmodifiableList(this.values));
				}
				this.children.forEach((name, child) -> map.put(name, child.value()));
				value = Collections.unmodifiableMap(map);
			}
			this.value = value;
		}
		return value;
	}

	/**
	 * Returns the text of the value of this node as JDK types.
	 */
	@Override
	public String toString() {
		return String.valueOf(this.value());
	}

	/**
	 * Reads the nodes without coercing their values.
	 */
	private enum Reader implements CoercibleInputReader<ParameterTree> {

		INSTANCE;

		@Override
		public boolean isNull(ParameterTree node) {
			return node.isLeaf() && node.values.size() == 1 && node.values.get(0) == null;
		}

		@Override
		public Object value(ParameterTree node) {
			final Object value = node.value();
			if (value == null) {
				throw new IllegalArgumentException("A null node has no value.");
			}
			return value;
		}

		@Override
		public boolean isObject(ParameterTree node) {
			return node.isObject();
		}

		@Override
		public @Nullable ParameterTree property(ParameterTree object, String name) {
			return object.children.get(name);
		}

		@Override
		public Iterable<Map.Entry<String, ParameterTree>> properties(ParameterTree object) {
			return object.children.entrySet();
		}

		@Override
		public boolean isArray(ParameterTree node) {
			return node.isArray();
		}

		@Override
		public Iterable<@Nullable ParameterTree> elements(ParameterTree array) {
			return array.elements();
		}

	}

}
