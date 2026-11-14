/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import am.ik.yavi.message.MessageFormatter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A {@link ValueValidator} that also goes back from a value it produces to the input that
 * it reads that value from.
 *
 * <pre>{@code
 * ReversibleValueValidator<String, Email> emailValidator = Yavi._string("email", c -> c.notBlank().email())
 *     .map(Email::new, Email::value);
 * Email email = emailValidator.validated("taro@example.com");
 * emailValidator.reverse(email); // "taro@example.com"
 * }</pre>
 *
 * <p>
 * The reverse is defined by the round trip: for every value {@code x} that the validator
 * produces, {@code validate(reverse(x))} succeeds with a value equal to {@code x}. It is
 * not the inverse of the validation: the input that is returned is one that the value is
 * read from, not necessarily the one it was read from, so a text that the validator trims
 * is returned trimmed.
 *
 * <p>
 * Whether a validator knows its reverse is told by its type alone, so a place that needs
 * the reverse, such as a schema of {@link am.ik.yavi.validator.Yavi#reversibleSchema()},
 * takes this type and a validator that does not know its reverse is rejected by the
 * compiler. These are of this type:
 * <ul>
 * <li>the validators of single values that the builders build without a mapper, such as
 * {@code Yavi._string("name")}: the input of a value is the value itself;</li>
 * <li>the result of {@link ValueValidator#map(Function, Function)} and of
 * {@link #of(ValueValidator, Function)}, which state the reverse;</li>
 * <li>what {@link #named(String)}, {@link #describedAs(ValueDescription)},
 * {@link #preprocess(Function)}, {@link #prefixed(String)},
 * {@link #formattingWith(MessageFormatter)} and {@link #constraintOnTarget} make of a
 * validator of this type, since they do not change the value;</li>
 * <li>what {@link #optional()}, {@link #nullable()}, {@link #defaultValue(Object)},
 * {@link #recover(Object)} and {@link #recoverWith(Function)} make of one, which is also
 * a {@link NullAcceptingValueValidator}: a value that stands for {@code null} goes back
 * to {@code null};</li>
 * <li>what {@link #liftList()}, {@link #liftSet()}, {@link #liftCollection(Supplier)},
 * {@link #liftMap()}, {@link #liftOptional()} and {@link #liftPresence()} make of one:
 * every element goes back;</li>
 * <li>{@link #lazy(Supplier)} and {@link #recursive(Function)}, which refer to a
 * validator of this type.</li>
 * </ul>
 * A validator that changes its value without saying how to go back, such as
 * {@link ValueValidator#map(Function)}, {@link ValueValidator#andThen(ValueValidator)} or
 * a lambda, is a plain {@link ValueValidator}.
 *
 * @param <T> the type of the object to be validated
 * @param <X> the type of the validated and transformed result
 * @since 0.18.0
 * @see ValueValidator#map(Function, Function)
 */
public interface ReversibleValueValidator<T, X extends @Nullable Object> extends ValueValidator<T, X> {

	/**
	 * Returns the given validator as one that goes back from its value to its input with
	 * the given function. This states the reverse of a validator that does not know it,
	 * such as a lambda.
	 * @param validator the validator
	 * @param reverse the function that returns the input that the validator reads the
	 * given value from
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @return a validator that validates like the given one and knows its reverse
	 */
	static <T, X extends @Nullable Object> ReversibleValueValidator<T, X> of(ValueValidator<T, X> validator,
			Function<? super X, ? extends @Nullable T> reverse) {
		return new ReversedValueValidator<>(validator, reverse);
	}

	/**
	 * Returns the given validator as one that goes back from its value to its input as
	 * the other validator does. This is for a validator derived from a reversible one
	 * that produces the same values, such as one that reports its violations in another
	 * way.
	 * @param validator the validator
	 * @param reversible the validator that knows the reverse
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @return a validator that validates like the given one and knows its reverse
	 */
	static <T, X extends @Nullable Object> ReversibleValueValidator<T, X> of(ValueValidator<T, X> validator,
			ReversibleValueValidator<T, X> reversible) {
		return new ReversedValueValidator<>(validator, reversible.reversing());
	}

	/**
	 * Returns a validator that asks the given supplier for the validator to use when it
	 * validates or goes back, as {@link ValueValidator#lazy(Supplier)} does for a
	 * validator that only validates.
	 * @param supplier the supplier of the validator to refer to
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @return a validator that refers to the supplied validator
	 * @see #recursive(Function)
	 */
	static <T, X extends @Nullable Object> ReversibleValueValidator<T, X> lazy(
			Supplier<? extends @Nullable ReversibleValueValidator<T, X>> supplier) {
		return lazy("", supplier);
	}

	/**
	 * Returns a validator that asks the given supplier for the validator to use when it
	 * validates or goes back, and that is described under the given name, as
	 * {@link ValueValidator#lazy(String, Supplier)} does for a validator that only
	 * validates.
	 * @param name the name of the referred validator in a schema, or an empty string to
	 * derive one from its position
	 * @param supplier the supplier of the validator to refer to
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @return a validator that refers to the supplied validator
	 */
	static <T, X extends @Nullable Object> ReversibleValueValidator<T, X> lazy(String name,
			Supplier<? extends @Nullable ReversibleValueValidator<T, X>> supplier) {
		final Function<? super X, ? extends @Nullable T> reverse = value -> {
			final ReversibleValueValidator<T, X> validator = supplier.get();
			if (validator == null) {
				throw new IllegalStateException(
						"The validator that a lazy validator refers to is not built yet. Use the lazy validator only after the supplier can return the validator.");
			}
			return validator.reverse(value);
		};
		return new ReversedValueValidator<>(ValueValidator.lazy(name, supplier), reverse);
	}

	/**
	 * Builds a validator that refers to itself, as {@link ValueValidator#recursive} does,
	 * and that goes back: the reference given to the definition knows its reverse, so it
	 * is accepted where a reversible validator is needed.
	 *
	 * <pre>{@code
	 * ObjectInputValidator.Reversible<Tree> treeValidator = ReversibleValueValidator.recursive(self -> Yavi.reversibleSchema()
	 *     ._string("name")
	 *     ._list("children", self).defaultValue(List.of())
	 *     .fromMapReversible(Tree::new, Tree::name, Tree::children));
	 * }</pre>
	 * @param definition the function that builds the validator with a reference to it
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @param <V> the type of the built validator
	 * @return the built validator
	 */
	static <T, X extends @Nullable Object, V extends ReversibleValueValidator<T, X>> V recursive(
			Function<? super ReversibleValueValidator<T, X>, ? extends V> definition) {
		return recursive("", definition);
	}

	/**
	 * Builds a validator that refers to itself and goes back, as
	 * {@link #recursive(Function)} does, and describes the references under the given
	 * name.
	 * @param name the name of the validator in a schema, or an empty string to derive one
	 * from its position
	 * @param definition the function that builds the validator with a reference to it
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @param <V> the type of the built validator
	 * @return the built validator
	 */
	static <T, X extends @Nullable Object, V extends ReversibleValueValidator<T, X>> V recursive(String name,
			Function<? super ReversibleValueValidator<T, X>, ? extends V> definition) {
		final AtomicReference<@Nullable V> built = new AtomicReference<>();
		final V validator = definition.apply(lazy(name, built::get));
		built.set(validator);
		return validator;
	}

	/**
	 * Returns the input that this validator reads the given value from.
	 * @param value a value that this validator produces
	 * @return the input, which is {@code null} if the validator produces the value for
	 * {@code null}
	 */
	@Nullable T reverse(X value);

	private Function<? super X, ? extends @Nullable T> reversing() {
		final Function<? super X, ? extends @Nullable T> reverse = value -> this.reverse(value);
		return reverse;
	}

	/**
	 * Returns a validator described by the given description, as
	 * {@link ValueValidator#describedAs(ValueDescription)} states, that still knows its
	 * reverse.
	 * @param description the description of the value
	 * @return a validator with the given description
	 */
	@Override
	default ReversibleValueValidator<T, X> describedAs(ValueDescription description) {
		return of(ValueValidator.super.describedAs(description), this);
	}

	/**
	 * Returns a validator that transforms a non-null input before it is validated, as
	 * {@link ValueValidator#preprocess(Function)} states, and that still knows its
	 * reverse: the input that is returned for a value is the transformed one.
	 * @param transform the transformation of the input
	 * @return a validator of the transformed input
	 */
	@Override
	default ReversibleValueValidator<T, X> preprocess(Function<? super T, ? extends @Nullable T> transform) {
		return of(ValueValidator.super.preprocess(transform), this);
	}

	/**
	 * Returns a validator that reports the violations of the validated value under the
	 * given name, as {@link ValueValidator#named(String)} states, and that still knows
	 * its reverse.
	 * @param name the name under which the violations of the value are reported
	 * @return a validator of the value under the given name
	 */
	@Override
	default ReversibleValueValidator<T, X> named(String name) {
		return of(ValueValidator.super.named(name), this);
	}

	/**
	 * Returns a validator that prefixes the names of its violations, as
	 * {@link ValueValidator#prefixed(String)} states, and that still knows its reverse.
	 * @param name the prefix
	 * @return a validator whose violations are prefixed
	 */
	@Override
	default ReversibleValueValidator<T, X> prefixed(String name) {
		return of(ValueValidator.super.prefixed(name), this);
	}

	/**
	 * Returns a validator that formats the messages of its violations with the given
	 * formatter, as {@link ValueValidator#formattingWith(MessageFormatter)} states, and
	 * that still knows its reverse.
	 * @param messageFormatter the formatter of the messages
	 * @return a validator whose messages are formatted with the given formatter
	 */
	@Override
	default ReversibleValueValidator<T, X> formattingWith(MessageFormatter messageFormatter) {
		return of(ValueValidator.super.formattingWith(messageFormatter), this);
	}

	/**
	 * Returns a validator that additionally checks the produced value with the given
	 * predicate, as {@link ValueValidator#constraintOnTarget} states, and that still
	 * knows its reverse.
	 * @param predicate the rule that the produced value must satisfy
	 * @param name the name of the reported violation
	 * @param violationMessage the message key and the default message format of the
	 * reported violation
	 * @return a validator with the additional rule
	 */
	@Override
	default ReversibleValueValidator<T, X> constraintOnTarget(Predicate<? super X> predicate, String name,
			ViolationMessage violationMessage) {
		return of(ValueValidator.super.constraintOnTarget(predicate, name, violationMessage), this);
	}

	/**
	 * Returns a validator that accepts {@code null} as an empty {@code Optional}, as
	 * {@link ValueValidator#optional()} states, and that still knows its reverse: an
	 * empty {@code Optional} goes back to {@code null}.
	 * @return a validator of an optional value
	 */
	@Override
	default NullAccepting<T, Optional<@NonNull X>> optional() {
		return new ReversedNullAcceptingValueValidator<>(ValueValidator.super.optional(),
				ReversedValueValidator.optional(this.reversing()));
	}

	/**
	 * Returns a validator that accepts {@code null} as it is, as
	 * {@link ValueValidator#nullable()} states, and that still knows its reverse:
	 * {@code null} goes back to {@code null}.
	 * @return a validator of a nullable value
	 */
	@Override
	default NullAccepting<T, @Nullable X> nullable() {
		return new ReversedNullAcceptingValueValidator<>(ValueValidator.super.nullable(),
				ReversedValueValidator.nullSafe(this.reversing()));
	}

	/**
	 * Returns a validator that produces the given value for {@code null}, as
	 * {@link ValueValidator#defaultValue(Object)} states, and that still knows its
	 * reverse: the default value goes back as any other value does.
	 * @param defaultValue the value for {@code null}
	 * @return a validator with a default value
	 */
	@Override
	default NullAccepting<T, X> defaultValue(X defaultValue) {
		return new ReversedNullAcceptingValueValidator<>(ValueValidator.super.defaultValue(defaultValue),
				ReversedValueValidator.nullSafe(this.reversing()));
	}

	/**
	 * Returns a validator that produces the given value for {@code null} and for an
	 * invalid input, as {@link ValueValidator#recover(Object)} states, and that still
	 * knows its reverse.
	 * @param fallback the value for {@code null} and for an invalid input
	 * @return a validator that never fails
	 */
	@Override
	default NullAccepting<T, X> recover(X fallback) {
		return new ReversedNullAcceptingValueValidator<>(ValueValidator.super.recover(fallback),
				ReversedValueValidator.nullSafe(this.reversing()));
	}

	/**
	 * Returns a validator that produces the result of the given function for {@code null}
	 * and for an invalid input, as {@link ValueValidator#recoverWith(Function)} states,
	 * and that still knows its reverse.
	 * @param fallback the function that returns the value for the violations
	 * @return a validator that never fails
	 */
	@Override
	default NullAccepting<T, X> recoverWith(Function<? super ConstraintViolations, ? extends X> fallback) {
		return new ReversedNullAcceptingValueValidator<>(ValueValidator.super.recoverWith(fallback),
				ReversedValueValidator.nullSafe(this.reversing()));
	}

	/**
	 * Returns a validator of the elements of a collection, as
	 * {@link ValueValidator#liftCollection(Supplier)} states, that goes back to the list
	 * of the inputs of the elements.
	 * @param factory the factory of the collection of the validated elements
	 * @param <C> the type of the collection
	 * @return a validator of a collection
	 */
	@Override
	default <C extends Collection<X>> ReversibleValueValidator<Iterable<? extends T>, C> liftCollection(
			Supplier<C> factory) {
		final Function<? super C, ? extends @Nullable Iterable<? extends T>> reverse = values -> this.inputs(values);
		return of(ValueValidator.super.liftCollection(factory), reverse);
	}

	/**
	 * Returns a validator of the elements of a list, as {@link ValueValidator#liftList()}
	 * states, that goes back to the list of the inputs of the elements.
	 * @return a validator of a list
	 */
	@Override
	default ReversibleValueValidator<Iterable<? extends T>, List<X>> liftList() {
		final Function<? super List<X>, ? extends @Nullable Iterable<? extends T>> reverse = values -> this
			.inputs(values);
		return of(ValueValidator.super.liftList(), reverse);
	}

	/**
	 * Returns a validator of the elements of a list that also reads them from a text
	 * split by the given separator, as {@link ValueValidator#liftList(Pattern)} states,
	 * and that goes back to the list of the inputs of the elements, not to a text.
	 * @param separator the separator of the elements in a text
	 * @return a validator of a list
	 */
	@Override
	default ReversibleValueValidator<Iterable<? extends T>, List<X>> liftList(Pattern separator) {
		final Function<? super List<X>, ? extends @Nullable Iterable<? extends T>> reverse = values -> this
			.inputs(values);
		return of(ValueValidator.super.liftList(separator), reverse);
	}

	/**
	 * Returns a validator of the elements of a set, as {@link ValueValidator#liftSet()}
	 * states, that goes back to the list of the inputs of the elements.
	 * @return a validator of a set
	 */
	@Override
	default ReversibleValueValidator<Iterable<? extends T>, Set<X>> liftSet() {
		final Function<? super Set<X>, ? extends @Nullable Iterable<? extends T>> reverse = values -> this
			.inputs(values);
		return of(ValueValidator.super.liftSet(), reverse);
	}

	/**
	 * Returns a validator of the values of a map, as {@link ValueValidator#liftMap()}
	 * states, that goes back to the map of the inputs of the values.
	 * @return a validator of a map
	 */
	@Override
	default ReversibleValueValidator<Map<String, ? extends T>, Map<String, X>> liftMap() {
		final Function<? super Map<String, X>, ? extends @Nullable Map<String, ? extends T>> reverse = values -> {
			final Map<String, @Nullable T> inputs = new LinkedHashMap<>();
			values.forEach((key, value) -> inputs.put(key, this.reverse(value)));
			return ReversedValueValidator.nonNullValues(inputs);
		};
		return of(ValueValidator.super.liftMap(), reverse);
	}

	/**
	 * Returns a validator of the value of an {@code Optional}, as
	 * {@link ValueValidator#liftOptional()} states, that goes back to the
	 * {@code Optional} of the input of the value.
	 * @return a validator of an {@code Optional}
	 */
	@Override
	default ReversibleValueValidator<Optional<? extends T>, Optional<@NonNull X>> liftOptional() {
		final Function<? super Optional<@NonNull X>, ? extends @Nullable Optional<? extends T>> reverse = value -> value
			.isPresent() ? Optional.ofNullable(this.reverse(value.get())) : Optional.empty();
		return of(ValueValidator.super.liftOptional(), reverse);
	}

	/**
	 * Returns a validator of a value that may be absent, as
	 * {@link ValueValidator#liftPresence()} states, that goes back to the
	 * {@link Presence} of the input of the value.
	 * @return a validator of a {@code Presence}
	 */
	@Override
	default ReversibleValueValidator<Presence<? extends T>, Presence<@NonNull X>> liftPresence() {
		final Function<? super Presence<@NonNull X>, ? extends @Nullable Presence<? extends T>> reverse = value -> {
			if (value instanceof Presence.Present<@NonNull X>present) {
				return Presence.ofNullable(this.reverse(present.value()));
			}
			return value.isAbsent() ? Presence.absent() : Presence.presentNull();
		};
		return of(ValueValidator.super.liftPresence(), reverse);
	}

	private Iterable<? extends T> inputs(Collection<X> values) {
		final List<@Nullable T> inputs = new ArrayList<>(values.size());
		values.forEach(value -> inputs.add(this.reverse(value)));
		return ReversedValueValidator.nonNullElements(inputs);
	}

	/**
	 * A {@link ReversibleValueValidator} that is also known by its type to accept a
	 * {@code null} input: the result of {@link ReversibleValueValidator#optional()},
	 * {@link ReversibleValueValidator#nullable()},
	 * {@link ReversibleValueValidator#defaultValue(Object)},
	 * {@link ReversibleValueValidator#recover(Object)} and
	 * {@link ReversibleValueValidator#recoverWith(Function)}. A schema of
	 * {@link am.ik.yavi.validator.Yavi#reversibleSchema()} takes it as it takes a
	 * {@link NullAcceptingValueValidator}.
	 *
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @since 0.18.0
	 */
	sealed interface NullAccepting<T, X extends @Nullable Object> extends ReversibleValueValidator<T, X>,
			NullAcceptingValueValidator<T, X>permits ReversedNullAcceptingValueValidator {

		/**
		 * Returns a validator that reports the violations of the validated value under
		 * the given name, and that still accepts {@code null} and knows its reverse.
		 * @param name the name under which the violations of the value are reported
		 * @return a validator of the value under the given name
		 */
		@Override
		NullAccepting<T, X> named(String name);

	}

}
