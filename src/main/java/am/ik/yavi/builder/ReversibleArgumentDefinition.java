/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Presence;
import am.ik.yavi.core.ReversibleValueValidator;
import am.ik.yavi.core.StringCoercion;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.input.PropertyMessages;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The definition of one argument of a schema that is also written back: an
 * {@link ArgumentDefinition} with the function that goes back from the validated value to
 * the value that the argument is read from. The builders returned by
 * {@link am.ik.yavi.validator.Yavi#reversibleSchema()} hold one for every argument, so
 * that an argument without a reverse cannot be added.
 *
 * @param definition how the argument is read
 * @param reverse returns the value that the argument is read from: a value of a JDK type,
 * the name of an enum constant, a map for a nested or an embedded object and for the
 * rest, a list or a text for a list, or {@code null} for no value
 * @param <A> the type of the argument value
 * @param <R> the type of the validated value
 */
record ReversibleArgumentDefinition<A, R extends @Nullable Object> (ArgumentDefinition<A, R> definition,
		Function<? super R, ? extends @Nullable Object> reverse) {

	private static <A, R extends @Nullable Object> Function<? super R, ? extends @Nullable Object> reverseOf(
			ReversibleValueValidator<A, R> validator) {
		final Function<? super R, ? extends @Nullable Object> reverse = value -> written(validator.reverse(value));
		return reverse;
	}

	/**
	 * Returns the value as it is written: an enum constant by its name, which is what a
	 * validator of an enum reads.
	 */
	private static @Nullable Object written(@Nullable Object value) {
		return value instanceof Enum<?>constant ? constant.name() : value;
	}

	// a value that stands for null is null or a default value, which goes back as it is
	@SuppressWarnings({ "unchecked", "NullAway" })
	private <R2 extends @Nullable Object> Function<? super R2, ? extends @Nullable Object> nullSafe() {
		final Function<? super R, ? extends @Nullable Object> reverse = this.reverse;
		final Function<? super R2, ? extends @Nullable Object> nullSafe = value -> value == null ? null
				: reverse.apply((R) value);
		return nullSafe;
	}

	private static <R extends @Nullable Object> Function<? super List<R>, ? extends @Nullable Object> eachOf(
			Function<? super R, ? extends @Nullable Object> reverse) {
		final Function<? super List<R>, ? extends @Nullable Object> each = values -> {
			final List<@Nullable Object> elements = new ArrayList<>(values.size());
			values.forEach(value -> elements.add(reverse.apply(value)));
			return elements;
		};
		return each;
	}

	private static <R extends @Nullable Object> Function<? super Map<String, R>, ? extends @Nullable Object> valuesOf(
			Function<? super R, ? extends @Nullable Object> reverse) {
		final Function<? super Map<String, R>, ? extends @Nullable Object> each = values -> {
			final Map<String, @Nullable Object> entries = new LinkedHashMap<>();
			values.forEach((key, value) -> entries.put(key, reverse.apply(value)));
			return entries;
		};
		return each;
	}

	/**
	 * Returns the definition of an argument validated with the given validator, as
	 * {@link ArgumentDefinition#value(Class, ValueValidator)} does.
	 */
	static <A, R extends @Nullable Object> ReversibleArgumentDefinition<A, R> value(Class<?> type,
			ReversibleValueValidator<A, R> validator) {
		return new ReversibleArgumentDefinition<>(ArgumentDefinition.value(type, validator), reverseOf(validator));
	}

	/**
	 * Returns the definition of an argument of constraints, as
	 * {@link ArgumentDefinition#value(String, Class, ValueValidator)} does.
	 */
	static <A> ReversibleArgumentDefinition<A, A> value(String name, Class<A> type,
			ReversibleValueValidator<A, A> validator) {
		return new ReversibleArgumentDefinition<>(ArgumentDefinition.value(name, type, validator),
				reverseOf(validator));
	}

	/**
	 * Returns the definition of a nested argument, as
	 * {@link ArgumentDefinition#object(String, ValueValidator)} does.
	 */
	static <A, R extends @Nullable Object> ReversibleArgumentDefinition<A, R> object(String name,
			ReversibleValueValidator<A, R> validator) {
		return new ReversibleArgumentDefinition<>(ArgumentDefinition.object(name, validator), reverseOf(validator));
	}

	/**
	 * Returns the definition of a nested argument that accepts {@code null}, as
	 * {@link ArgumentDefinition#object(String, am.ik.yavi.core.NullAcceptingValueValidator)}
	 * does.
	 */
	static <A, R extends @Nullable Object> ReversibleArgumentDefinition<A, R> object(String name,
			ReversibleValueValidator.NullAccepting<A, R> validator) {
		return new ReversibleArgumentDefinition<>(ArgumentDefinition.object(name, validator), reverseOf(validator));
	}

	/**
	 * Returns the definition of a list, as {@link ArgumentDefinition#list} does. It is
	 * written as the list of what its elements are read from.
	 */
	static <E, R extends @Nullable Object> ReversibleArgumentDefinition<List<? extends E>, List<R>> list(String name,
			ReversibleValueValidator<E, R> elementValidator, ContainerConstraints<List<R>> constraints) {
		return new ReversibleArgumentDefinition<>(ArgumentDefinition.list(name, elementValidator, constraints),
				eachOf(reverseOf(elementValidator)));
	}

	/**
	 * Returns the definition of this list as one that is also read from text whose
	 * elements the given pattern separates. It is still written as a list: a pattern does
	 * not tell the text that separates the elements.
	 */
	ReversibleArgumentDefinition<A, R> separatedBy(Pattern separator) {
		return new ReversibleArgumentDefinition<>(this.definition.separatedBy(separator), this.reverse);
	}

	/**
	 * Returns the definition of this list as one that is also read from text whose
	 * elements the given text separates, and that is written as that text. An empty list
	 * is written as {@code null}: text without an element is not sent.
	 */
	ReversibleArgumentDefinition<A, R> separatedBy(String separator) {
		final Function<? super R, ? extends @Nullable Object> list = this.reverse;
		final Function<? super R, ? extends @Nullable Object> joined = values -> {
			if (!(list.apply(values) instanceof List<?>elements) || elements.isEmpty()) {
				return null;
			}
			final StringBuilder text = new StringBuilder();
			for (int i = 0; i < elements.size(); i++) {
				final Object element = elements.get(i);
				text.append(i == 0 ? "" : separator).append(element == null ? "" : StringCoercion.textOf(element));
			}
			return text.toString();
		};
		return new ReversibleArgumentDefinition<>(
				this.definition.separatedBy(Pattern.compile(Pattern.quote(separator))), joined);
	}

	/**
	 * Returns the definition of a map, as {@link ArgumentDefinition#map} does. It is
	 * written as the map of what its values are read from.
	 */
	static <V, R extends @Nullable Object> ReversibleArgumentDefinition<Map<String, ? extends V>, Map<String, R>> map(
			String name, ReversibleValueValidator<V, R> valueValidator,
			ContainerConstraints<Map<String, R>> constraints) {
		return new ReversibleArgumentDefinition<>(ArgumentDefinition.map(name, valueValidator, constraints),
				valuesOf(reverseOf(valueValidator)));
	}

	/**
	 * Returns the definition of an argument that is read from the map itself, as
	 * {@link ArgumentDefinition#embedded} does.
	 */
	static <R extends @Nullable Object> ReversibleArgumentDefinition<Map<String, Object>, R> embedded(
			ReversibleValueValidator<Map<String, Object>, R> validator) {
		return new ReversibleArgumentDefinition<>(ArgumentDefinition.embedded(validator), reverseOf(validator));
	}

	/**
	 * Returns the definition of the rest of the properties, as
	 * {@link ArgumentDefinition#rest(ValueValidator)} does.
	 */
	static <V, R extends @Nullable Object> ReversibleArgumentDefinition<Map<String, ? extends V>, Map<String, R>> rest(
			ReversibleValueValidator<V, R> valueValidator) {
		return new ReversibleArgumentDefinition<>(ArgumentDefinition.rest(valueValidator),
				valuesOf(reverseOf(valueValidator)));
	}

	/**
	 * Returns the definition of the rest of the properties as they are, as
	 * {@link ArgumentDefinition#rest()} does.
	 */
	static ReversibleArgumentDefinition<Map<String, ?>, Map<String, @Nullable Object>> rest() {
		final Function<@Nullable Object, @Nullable Object> itself = value -> value;
		return new ReversibleArgumentDefinition<>(ArgumentDefinition.rest(), valuesOf(itself));
	}

	@Nullable String name() {
		return this.definition.name();
	}

	ValueValidator<A, R> validator() {
		return this.definition.validator();
	}

	ArgumentDefinition.Source source() {
		return this.definition.source();
	}

	PropertyMessages messages() {
		return this.definition.messages();
	}

	ReversibleArgumentDefinition<A, R> withMessages(PropertyMessages messages) {
		return new ReversibleArgumentDefinition<>(this.definition.withMessages(messages), this.reverse);
	}

	ReversibleArgumentDefinition<A, R> asWithheld() {
		return new ReversibleArgumentDefinition<>(this.definition.asWithheld(), this.reverse);
	}

	ReversibleArgumentDefinition<A, Optional<@NonNull R>> asOptional() {
		final Function<? super R, ? extends @Nullable Object> reverse = this.reverse;
		final Function<? super Optional<@NonNull R>, ? extends @Nullable Object> optional = value -> value.isPresent()
				? reverse.apply(value.get()) : null;
		return new ReversibleArgumentDefinition<>(this.definition.asOptional(), optional);
	}

	ReversibleArgumentDefinition<A, @Nullable R> asNullable() {
		return new ReversibleArgumentDefinition<>(this.definition.asNullable(), this.nullSafe());
	}

	ReversibleArgumentDefinition<A, R> withDefault(R defaultValue) {
		return new ReversibleArgumentDefinition<>(this.definition.withDefault(defaultValue), this.nullSafe());
	}

	ReversibleArgumentDefinition<A, R> recovering(R fallback) {
		return new ReversibleArgumentDefinition<>(this.definition.recovering(fallback), this.nullSafe());
	}

	ReversibleArgumentDefinition<A, R> recoveringWith(Function<? super ConstraintViolations, ? extends R> fallback) {
		return new ReversibleArgumentDefinition<>(this.definition.recoveringWith(fallback), this.nullSafe());
	}

	ReversibleArgumentDefinition<Presence<? extends A>, Presence<@NonNull R>> withPresence() {
		final Function<? super R, ? extends @Nullable Object> reverse = this.reverse;
		final Function<? super Presence<@NonNull R>, ? extends @Nullable Object> present = value -> value instanceof Presence.Present<@NonNull R>held
				? reverse.apply(held.value()) : null;
		return new ReversibleArgumentDefinition<>(this.definition.withPresence(), present);
	}

	ReversibleArgumentDefinition<A, R> withStrictness(ObjectInputValidator.Strictness strictness) {
		return new ReversibleArgumentDefinition<>(this.definition.withStrictness(strictness), this.reverse);
	}

	/**
	 * Returns the part that reads this argument, as {@link ArgumentDefinition#part} does.
	 */
	ObjectInputValidator.Part<R> part(int position) {
		return this.definition.part(position);
	}

	/**
	 * Returns the part that reads this argument with what writes it back.
	 * @param position the position of the argument, from 1
	 * @param accessor returns the validated value of this argument from the created
	 * object
	 */
	<X> ObjectInputValidator.ReversiblePart<X, R> part(int position, Function<? super X, ? extends R> accessor) {
		return new ObjectInputValidator.ReversiblePart<>(this.definition.part(position), this.reverse, accessor);
	}

	/**
	 * Describes the arguments, as {@link ArgumentDefinition#describe} does.
	 */
	static ValueDescription describe(ReversibleArgumentDefinition<?, ?>... definitions) {
		return ArgumentDefinition.describe(Arrays.stream(definitions)
			.map(ReversibleArgumentDefinition::definition)
			.toArray(ArgumentDefinition<?, ?>[]::new));
	}

}
