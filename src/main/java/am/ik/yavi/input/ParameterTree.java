/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.InputReader;
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
 * ValueValidator<Map<String, List<String>>, SearchForm> validator = Yavi.arguments()
 *     ._string("q", c -> c.notBlank())
 *     ._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
 *     ._list("tags", StringValidatorBuilder.of("", c -> c.notBlank()).build())
 *     ._object("address", addressValidator)
 *     .fromMap(SearchForm::new)
 *     .readingParameters();
 * // q=yavi&page=2&tags=a&tags=b&address.city=Tokyo
 * Validated<SearchForm> validated = validator.validate(parameters);
 * }</pre>
 *
 * {@code readingParameters()} of {@link MapInputValidator#readingParameters() a validator
 * of maps} is the usual way to read parameters. Any other validator reads a tree with
 * {@link #reader()}: {@code validator.readingWith(ParameterTree.reader())} validates
 * {@link #of(Map) ParameterTree.of(parameters)}.
 *
 * <h2>Keys</h2>
 *
 * A key is a name followed by any number of {@code .name} and {@code [name]}, which are
 * the same: {@code address.city} and {@code address[city]} are the property {@code city}
 * of the property {@code address}. A name does not contain {@code .} or {@code [} unless
 * it is written in brackets, where it holds anything but {@code ]}. A key may end with
 * {@code []}, which is ignored: {@code tags[]} is {@code tags}. A key that is not written
 * in this way, or that has more than {@value #MAX_DEPTH} names, is a property of the root
 * under its whole text, so it matches no argument and is reported as an unknown property
 * if they are rejected.
 *
 * <h2>Values</h2>
 *
 * A value of the map that is an {@link Iterable} or an array gives the values of its key,
 * and any other value is the only one. This reads a {@code Map<String, String>}, a
 * {@code Map<String, List<String>>} such as the {@code MultiValueMap} of Spring and the
 * {@code Map<String, String[]>} of the Servlet API. The values are read as
 * {@link InputReader#coercingStrings()} states: a string is text that stands for a value
 * of the type it is checked as, and an empty string is {@code null}.
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
 * <p>
 * A tree is immutable and does not see the later changes of the map it was created from.
 *
 * @since 0.18.0
 * @see MapInputValidator#readingParameters()
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
	 * {@link InputReader#coercingStrings()} states.
	 * @return the reader of parameter trees
	 */
	public static InputReader<ParameterTree> reader() {
		return READER;
	}

	/**
	 * Returns a validator of a map of parameters that validates its tree with the given
	 * validator.
	 */
	static <M extends Map<String, ?>, X> ValueValidator<M, X> reading(ValueValidator<?, X> validator) {
		final ValueValidator<ParameterTree, X> ofTree = validator.readingWith(READER);
		return new ValueValidator<>() {
			@Override
			public Validated<X> validate(@Nullable M parameters, Locale locale, ConstraintContext constraintContext) {
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
	private enum Reader implements InputReader<ParameterTree> {

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
