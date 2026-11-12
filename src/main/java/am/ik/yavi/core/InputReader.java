/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Tells how to read the nodes of a tree of untyped input, such as the tree model of a
 * JSON library. A validator reads its input through a reader once it is bound to one with
 * {@link ValueValidator#untyped(InputReader)}, so the tree is validated as it is, without
 * being copied to JDK types first.
 *
 * <p>
 * A reader is implemented once per tree model and has no state, so one instance serves
 * every validator of an application:
 *
 * <pre>{@code
 * ValueValidator<JsonNode, User> validator = userValidator.untyped(jsonNodeReader);
 * }</pre>
 *
 * <p>
 * A node is one of the following:
 * <ul>
 * <li>a null node ({@link #isNull}), which is validated like {@code null},</li>
 * <li>an object ({@link #isObject}), whose properties are read with {@link #property},
 * {@link #hasProperty} and {@link #properties},</li>
 * <li>an array ({@link #isArray}), whose elements are read with {@link #elements},</li>
 * <li>a scalar, which {@link #value} returns as a JDK type.</li>
 * </ul>
 * The methods of an object or an array are called only with a node that is one. A
 * {@code null} reference is never given to a reader.
 *
 * @param <N> the type of the nodes
 * @since 0.18.0
 * @see #ofJdkTypes()
 */
public interface InputReader<N> {

	/**
	 * Tells whether the node stands for {@code null}, like the null literal of a JSON
	 * document.
	 * @param node the node
	 * @return {@code true} if the node is validated like {@code null}
	 */
	boolean isNull(N node);

	/**
	 * Returns the value of a scalar node as the JDK type it is checked as: a
	 * {@code String}, a {@code Number} or a {@code Boolean}. Any other node is returned
	 * as it is; it represents only a type it is an instance of. The returned value is
	 * also the one reported in a violation.
	 * @param node the node, which is not a null node
	 * @return the value of the node
	 * @see ValueValidator#untyped(Class)
	 */
	Object value(N node);

	/**
	 * Tells whether the node is an object, that is a set of named properties.
	 * @param node the node, which is not a null node
	 * @return {@code true} if the node is an object
	 */
	boolean isObject(N node);

	/**
	 * Returns the value of a property of an object.
	 * @param object the object
	 * @param name the name of the property
	 * @return the value, which may be a null node; {@code null} if the object has no such
	 * property
	 */
	@Nullable N property(N object, String name);

	/**
	 * Tells whether an object has a property. Override this method if {@link #property}
	 * returns {@code null} for a property that is present.
	 * @param object the object
	 * @param name the name of the property
	 * @return {@code true} if the object has the property, whatever its value is
	 */
	default boolean hasProperty(N object, String name) {
		return this.property(object, name) != null;
	}

	/**
	 * Returns the properties of an object in their order.
	 * @param object the object
	 * @return the names and the values of the properties
	 */
	Iterable<? extends Map.Entry<String, ? extends @Nullable N>> properties(N object);

	/**
	 * Tells whether the node is an array.
	 * @param node the node, which is not a null node
	 * @return {@code true} if the node is an array
	 */
	boolean isArray(N node);

	/**
	 * Returns the elements of an array in their order.
	 * @param array the array
	 * @return the elements, which may be null nodes
	 */
	Iterable<? extends @Nullable N> elements(N array);

	/**
	 * Returns the reader of a tree of JDK types, which is what
	 * {@link ValueValidator#untyped()} reads: a {@link Map} whose keys are all strings is
	 * an object, an {@link Iterable} is an array, {@code null} is the only null node and
	 * any other object is its own value.
	 * @return the reader of JDK types
	 */
	static InputReader<Object> ofJdkTypes() {
		return JdkInputReader.INSTANCE;
	}

}
