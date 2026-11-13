/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_EQUAL_TO;
import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_IS_NULL;
import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_NOT_NULL;
import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_NOT_ONE_OF;
import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_ONE_OF;

/**
 * Fluent API for defining constraints on a property value.
 *
 * <p>
 * An implementation accumulates {@link ConstraintPredicate}s that a {@link Validator}
 * evaluates when validating an object. Constraints are normally obtained from the
 * {@code constraint(...)} methods of {@link am.ik.yavi.builder.ValidatorBuilder}, not
 * created directly.
 * </p>
 *
 * <p>
 * Unless stated otherwise, a constraint treats a {@code null} value as valid and skips
 * the predicate for it. Use {@link #notNull()} or {@link #isNull()} to constrain
 * {@code null} itself.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <V> the type of the value the constraints apply to
 * @param <C> the concrete constraint type returned for method chaining
 * @since 0.0.1
 */
public interface Constraint<T, V, C extends Constraint<T, V, C>> {

	/**
	 * Returns this constraint as the concrete constraint type.
	 * @return this constraint
	 */
	C cast();

	/**
	 * Requires the value to be {@code null}.
	 *
	 * <p>
	 * A non-null value is reported with the message key {@code object.isNull} and the
	 * default message {@code "{0}" must be null}.
	 * </p>
	 *
	 * <p>
	 * A validator that produces a value, such as an argument of {@code Yavi.schema()} or
	 * {@code Yavi._string(...)}, requires the value, so this constraint alone can never
	 * be satisfied there: {@code null} is rejected with {@code object.notNull} and any
	 * other value with {@code object.isNull}. Apply {@code nullable()} to the validator
	 * to say that the value must be {@code null}:
	 * {@code _string("id", c -> c.isNull()).nullable()}.
	 * </p>
	 * @return this constraint
	 */
	default C isNull() {
		this.predicates()
			.add(ConstraintPredicate.of(Objects::isNull, OBJECT_IS_NULL, () -> new Object[] {}, NullAs.INVALID));
		return this.cast();
	}

	/**
	 * Requires the value to be equal to {@code other}.
	 *
	 * <p>
	 * A {@code null} value is considered valid and the comparison is skipped. A violation
	 * is reported with the message key {@code object.equalTo} and the default message
	 * {@code "{0}" must be equal to {1}}, where {@code {1}} is {@code other}.
	 * </p>
	 * @param other the value the input has to be equal to; may be {@code null}
	 * @return this constraint
	 * @since 0.10.0
	 */
	default C equalTo(@Nullable V other) {
		this.predicates()
			.add(ConstraintPredicate.of(Predicate.isEqual(other), OBJECT_EQUAL_TO, () -> new Object[] { other },
					NullAs.VALID));
		return this.cast();
	}

	/**
	 * Requires the value to be one of the given {@code values}.
	 *
	 * <p>
	 * A {@code null} value is considered valid and the membership check is skipped. A
	 * violation is reported with the message key {@code object.oneOf} and the default
	 * message {@code "{0}" must be one of the following values: {1}}, where {@code {1}}
	 * is {@code values}.
	 * </p>
	 * @param values the allowed values; must not be {@code null}
	 * @return this constraint
	 * @since 0.10.0
	 */
	default C oneOf(Collection<V> values) {
		this.predicates()
			.add(ConstraintPredicate.of(values::contains, OBJECT_ONE_OF, () -> new Object[] { values }, NullAs.VALID));
		return this.cast();
	}

	/**
	 * Requires the value not to be one of the given {@code values}.
	 *
	 * <p>
	 * A {@code null} value is considered valid and the membership check is skipped. A
	 * violation is reported with the message key {@code object.notOneOf} and the default
	 * message {@code "{0}" must not be one of the following values: {1}}, where
	 * {@code {1}} is {@code values}.
	 * </p>
	 * @param values the forbidden values; must not be {@code null}
	 * @return this constraint
	 * @since 0.14.1
	 */
	default C notOneOf(Collection<V> values) {
		Predicate<V> notOneOf = v -> !values.contains(v);
		this.predicates()
			.add(ConstraintPredicate.of(notOneOf, OBJECT_NOT_ONE_OF, () -> new Object[] { values }, NullAs.VALID));
		return this.cast();
	}

	/**
	 * Reports a violation of the most recently added constraint with the given text
	 * instead of its message.
	 *
	 * <pre>{@code
	 * c.notBlank().message("Enter your name")
	 * }</pre>
	 *
	 * <p>
	 * The text is the message: a message formatter that has a message for the key of the
	 * constraint does not replace it (see {@link ViolationMessage#withText(String)}). The
	 * message key and the arguments of the constraint are kept, so that the violation is
	 * still known by its key and message arguments such as {@code {0}} and {@code {1}}
	 * are still substituted. To let a resource bundle localize the message, give a
	 * {@link ViolationMessage} with a key of its own with
	 * {@link #message(ViolationMessage)} instead.
	 * </p>
	 *
	 * <p>
	 * A constraint made of several predicates, such as
	 * {@code url(url -> url.protocol("http", "https").build())}, which reports an invalid
	 * URL and another protocol each with its own key, is given the text for all of them.
	 * </p>
	 * @param message the text of the message
	 * @return this constraint
	 * @throws IllegalStateException if no constraint has been added yet
	 */
	default C message(String message) {
		return this.overrideMessageOfLast(predicate -> predicate.overrideMessage(message));
	}

	/**
	 * Overrides the message of the most recently added constraint with the given
	 * {@link ViolationMessage}.
	 *
	 * <p>
	 * Both the message key and the message format are replaced; the arguments of the
	 * constraint are kept. A constraint made of several predicates is given the message
	 * for all of them, as with {@link #message(String)}.
	 * </p>
	 * @param message the violation message to use
	 * @return this constraint
	 * @throws IllegalStateException if no constraint has been added yet
	 */
	default C message(ViolationMessage message) {
		return this.overrideMessageOfLast(predicate -> predicate.overrideMessage(message));
	}

	private C overrideMessageOfLast(UnaryOperator<ConstraintPredicate<V>> override) {
		final Deque<ConstraintPredicate<V>> predicates = this.predicates();
		final ConstraintPredicate<V> last = predicates.pollLast();
		if (last == null) {
			throw new IllegalStateException("no constraint found to override!");
		}
		final Deque<ConstraintPredicate<V>> overridden = new ArrayDeque<>();
		overridden.addFirst(override.apply(last));
		for (ConstraintPredicate<V> previous = predicates.peekLast(); previous != null
				&& previous.isPartOfSameConstraintAs(last); previous = predicates.peekLast()) {
			overridden.addFirst(override.apply(predicates.removeLast()));
		}
		predicates.addAll(overridden);
		return this.cast();
	}

	/**
	 * Requires the value not to be {@code null}.
	 *
	 * <p>
	 * A {@code null} value is reported with the message key {@code object.notNull} and
	 * the default message {@code "{0}" must not be null}.
	 * </p>
	 * @return this constraint
	 */
	default C notNull() {
		this.predicates()
			.add(ConstraintPredicate.of(Objects::nonNull, OBJECT_NOT_NULL, () -> new Object[] {}, NullAs.INVALID));
		return this.cast();
	}

	/**
	 * Adds a custom predicate as a constraint.
	 *
	 * <p>
	 * A {@code null} value is considered valid and the predicate is not called. If the
	 * predicate implements {@link ViolatedArguments}, the arguments it supplies are used
	 * to build the violation message; otherwise no additional argument is available.
	 * </p>
	 * @param predicate the predicate that has to return {@code true} for a valid value
	 * @param violationMessage the message key and default message of the violation
	 * @return this constraint
	 */
	default C predicate(Predicate<V> predicate, ViolationMessage violationMessage) {
		final Supplier<Object[]> arguments = ViolatedArguments.supplyArguments(predicate);
		this.predicates().add(ConstraintPredicate.of(predicate, violationMessage, arguments, NullAs.VALID));
		return this.cast();
	}

	/**
	 * Adds a custom predicate as a constraint with the given message key and default
	 * message format.
	 *
	 * <p>
	 * A {@code null} value is considered valid and the predicate is not called.
	 * </p>
	 * @param predicate the predicate that has to return {@code true} for a valid value
	 * @param messageKey the message key of the violation
	 * @param defaultMessageFormat the default message format of the violation
	 * @return this constraint
	 * @since 0.8.2
	 */
	default C predicate(Predicate<V> predicate, String messageKey, String defaultMessageFormat) {
		return this.predicate(predicate, ViolationMessage.of(messageKey, defaultMessageFormat));
	}

	/**
	 * Adds a {@link CustomConstraint} as a constraint, using its own message and
	 * arguments.
	 *
	 * <p>
	 * A {@code null} value is considered valid and the constraint is not evaluated.
	 * </p>
	 * @param constraint the custom constraint
	 * @return this constraint
	 */
	default C predicate(CustomConstraint<V> constraint) {
		return this.predicate(constraint, constraint);
	}

	/**
	 * Adds a custom predicate as a constraint that is also evaluated for a {@code null}
	 * value.
	 *
	 * <p>
	 * Unlike {@link #predicate(Predicate, ViolationMessage)}, the predicate receives
	 * {@code null} and has to handle it. If the predicate implements
	 * {@link ViolatedArguments}, the arguments it supplies are used to build the
	 * violation message.
	 * </p>
	 * @param predicate the predicate that has to return {@code true} for a valid value
	 * @param violationMessage the message key and default message of the violation
	 * @return this constraint
	 */
	default C predicateNullable(Predicate<V> predicate, ViolationMessage violationMessage) {
		final Supplier<Object[]> arguments = ViolatedArguments.supplyArguments(predicate);
		this.predicates().add(ConstraintPredicate.of(predicate, violationMessage, arguments, NullAs.INVALID));
		return this.cast();
	}

	/**
	 * Adds a custom predicate as a constraint that is also evaluated for a {@code null}
	 * value, with the given message key and default message format.
	 * @param predicate the predicate that has to return {@code true} for a valid value
	 * @param messageKey the message key of the violation
	 * @param defaultMessageFormat the default message format of the violation
	 * @return this constraint
	 * @since 0.8.2
	 */
	default C predicateNullable(Predicate<V> predicate, String messageKey, String defaultMessageFormat) {
		return this.predicateNullable(predicate, ViolationMessage.of(messageKey, defaultMessageFormat));
	}

	/**
	 * Adds a {@link CustomConstraint} as a constraint that is also evaluated for a
	 * {@code null} value.
	 * @param constraint the custom constraint
	 * @return this constraint
	 */
	default C predicateNullable(CustomConstraint<V> constraint) {
		return this.predicateNullable(constraint, constraint);
	}

	/**
	 * Returns the mutable queue of predicates collected by this constraint.
	 *
	 * <p>
	 * The returned queue is used directly by the fluent methods, so changes to it are
	 * reflected in this constraint.
	 * </p>
	 * @return the mutable queue of predicates
	 */
	Deque<ConstraintPredicate<V>> predicates();

	/**
	 * Adds the given predicates as one constraint: the value has to satisfy each of them,
	 * a violation is reported for each one that it does not satisfy, and
	 * {@link #message(String)} or {@link #message(ViolationMessage)} right after replaces
	 * the message of all of them instead of that of the last one only. This is how a
	 * constraint made of several predicates, such as
	 * {@code url(url -> url.protocol("http", "https").build())}, is added.
	 *
	 * <p>
	 * Adding them to {@link #predicates()} directly makes each a constraint of its own.
	 * </p>
	 * @param predicates the predicates of the constraint
	 * @return this constraint
	 * @since 0.18.0
	 */
	default C predicates(List<ConstraintPredicate<V>> predicates) {
		this.predicates().addAll(ConstraintPredicate.partsOfOne(predicates));
		return this.cast();
	}

}
