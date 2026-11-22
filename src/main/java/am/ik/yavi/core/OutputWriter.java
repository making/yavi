/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * Tells how to create the nodes of a tree of output, such as the tree model of a JSON
 * library: the counterpart of {@link InputReader}. {@link #write(Object)} writes a tree
 * of JDK types, such as the map that a reversible validator writes an object back as, as
 * a tree of nodes:
 *
 * <pre>{@code
 * JsonNode node = jsonNodeWriter.write(userValidator.toMap(user));
 * JsonNode summary = jsonNodeWriter.write(SUMMARY.toMap(user)); // a PropertyView
 * }</pre>
 *
 * <p>
 * The written tree is defined by the round trip with the reader of the same tree model:
 * for every object {@code x} that a reversible validator creates,
 * {@code validator.readingWith(reader).validate(writer.write(validator.toMap(x)))}
 * succeeds with an object equal to {@code x}, as long as the reader returns the value
 * that {@link #scalar(Object)} is given for the node it returns. A value that a JSON
 * document has no type for is written as the text that the validator reads it from: a
 * {@code LocalDate} is its ISO-8601 text, whatever the library would make of it.
 *
 * <p>
 * A writer is implemented once per tree model and has no state, so one instance serves
 * every validator of an application. It implements {@link #nullNode()},
 * {@link #scalar(Object)}, {@link #object(Map)} and {@link #array(List)}; the other
 * methods are given.
 *
 * @param <N> the type of the nodes
 * @since 0.18.1
 * @see InputReader
 */
public interface OutputWriter<N> {

	/**
	 * Returns the node that stands for {@code null}, like the null literal of a JSON
	 * document.
	 * @return a null node
	 */
	N nullNode();

	/**
	 * Returns the node of a scalar.
	 * @param value a {@code String}, a {@code Boolean} or a {@code Number}: an
	 * {@code Integer}, a {@code Long}, a {@code Short}, a {@code Byte}, a
	 * {@code BigInteger}, a {@code BigDecimal}, a {@code Double} or a {@code Float}. Keep
	 * a number as exactly as the tree model can: the reader is to return it as a value
	 * equal to it
	 * @return the node of the value
	 * @throws IllegalArgumentException if the tree model cannot hold the value
	 */
	N scalar(Object value);

	/**
	 * Returns an object of the given properties.
	 * @param properties the names and the nodes of the properties in their order, which
	 * the writer may keep
	 * @return the object
	 */
	N object(Map<String, N> properties);

	/**
	 * Returns an array of the given elements.
	 * @param elements the elements in their order, which the writer may keep
	 * @return the array
	 */
	N array(List<N> elements);

	/**
	 * Returns the node of a value of a tree that is neither {@code null}, a {@code Map}
	 * nor an {@code Iterable}. A {@code String}, a {@code Boolean} and a {@code Number}
	 * are given to {@link #scalar(Object)} as they are; an enum constant is its name and
	 * a {@code Year} is its number, as the validator reads them. A {@code LocalDate}, a
	 * {@code LocalTime}, a {@code LocalDateTime}, an {@code OffsetDateTime}, a
	 * {@code ZonedDateTime}, an {@code Instant}, a {@code YearMonth}, a {@code Duration},
	 * a {@code Period}, a {@code UUID} and a {@code URI} are the text that
	 * {@link ValueValidator#untyped(Class)} parses.
	 *
	 * <p>
	 * Override this method for a tree model that has a node of its own for a type, such
	 * as a date, and call {@code OutputWriter.super.value(value)} for the rest.
	 * @param value the value
	 * @return the node of the value
	 * @throws IllegalArgumentException if the value is of no type above
	 */
	default N value(Object value) {
		if (value instanceof String || value instanceof Boolean || value instanceof Number) {
			return this.scalar(value);
		}
		if (value instanceof Enum<?>constant) {
			return this.scalar(constant.name());
		}
		if (value instanceof Year year) {
			return this.scalar(year.getValue());
		}
		if (value instanceof LocalDate || value instanceof LocalTime || value instanceof LocalDateTime
				|| value instanceof OffsetDateTime || value instanceof ZonedDateTime || value instanceof Instant
				|| value instanceof YearMonth || value instanceof Duration || value instanceof Period
				|| value instanceof UUID || value instanceof URI) {
			return this.scalar(value.toString());
		}
		throw new IllegalArgumentException("A value of " + value.getClass().getName()
				+ " is not written by the writer. Override OutputWriter#value(Object) to write it.");
	}

	/**
	 * Writes a tree of JDK types as a tree of nodes: {@code null} is a
	 * {@linkplain #nullNode() null node}, a {@code Map} an {@linkplain #object(Map)
	 * object} whose keys are written with {@link String#valueOf(Object)}, an
	 * {@code Iterable} an {@linkplain #array(List) array} and any other value the node
	 * that {@link #value(Object)} returns. This method is not meant to be overridden.
	 * @param tree the tree, such as the map that
	 * {@code ObjectInputValidator.Reversible#toMap} returns
	 * @return the node of the tree
	 * @throws IllegalArgumentException if {@link #value(Object)} or
	 * {@link #scalar(Object)} rejects a value of the tree
	 */
	default N write(@Nullable Object tree) {
		if (tree == null) {
			return this.nullNode();
		}
		if (tree instanceof Map<?, ?>map) {
			final Map<String, N> properties = new LinkedHashMap<>();
			map.forEach((name, value) -> properties.put(String.valueOf(name), this.write(value)));
			return this.object(properties);
		}
		if (tree instanceof Iterable<?>iterable) {
			final List<N> elements = new ArrayList<>();
			iterable.forEach(element -> elements.add(this.write(element)));
			return this.array(elements);
		}
		return this.value(tree);
	}

}
