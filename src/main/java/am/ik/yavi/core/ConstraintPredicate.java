/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * A single constraint on a value: a predicate that decides whether the value is valid,
 * the violation message to report when it is not, the message arguments, and the
 * {@link NullAs} setting that determines how a {@code null} value is treated.
 *
 * <p>
 * Instances are created by the constraint builders in {@code am.ik.yavi.constraint} and
 * consumed by {@link Validator}. They are immutable; {@code overrideMessage} returns a
 * new instance.
 *
 * @param <V> the type of the value the constraint applies to
 * @since 0.0.1
 * @see ConstraintPredicates
 * @see ViolationMessage
 */
public class ConstraintPredicate<V> {

	private final Supplier<Object[]> args;

	private final String defaultMessageFormat;

	private final String messageKey;

	private final NullAs nullAs;

	private final List<String> paramNames;

	private final Predicate<V> predicate;

	private ConstraintPredicate(Predicate<V> predicate, ViolationMessage violationMessage, Supplier<Object[]> args,
			NullAs nullAs) {
		this(predicate, violationMessage.messageKey(), violationMessage.defaultMessageFormat(),
				violationMessage.paramNames(), args, nullAs);
	}

	private ConstraintPredicate(Predicate<V> predicate, String messageKey, String defaultMessageFormat,
			List<String> paramNames, Supplier<Object[]> args, NullAs nullAs) {
		this.predicate = predicate;
		this.messageKey = messageKey;
		this.defaultMessageFormat = defaultMessageFormat;
		this.paramNames = paramNames;
		this.args = args;
		this.nullAs = nullAs;
	}

	/**
	 * Creates a constraint predicate that uses the given predicate to decide validity.
	 * @param predicate the predicate, which returns {@code true} when the value is valid
	 * @param violationMessage the violation message to report
	 * @param args supplier of the message arguments
	 * @param nullAs the handling of {@code null} values
	 * @param <V> the type of the value the constraint applies to
	 * @return a constraint predicate
	 */
	public static <V> ConstraintPredicate<V> of(Predicate<V> predicate, ViolationMessage violationMessage,
			Supplier<Object[]> args, NullAs nullAs) {
		return new ConstraintPredicate<>(predicate, violationMessage, args, nullAs);
	}

	/**
	 * Creates a constraint predicate that obtains the violated value from the given
	 * function. The function returns an {@link Optional} containing the violated value
	 * when the value violates the constraint, or an empty optional when the value is
	 * valid.
	 * @param violatedValue function that returns the violated value for an invalid value
	 * @param violationMessage the violation message to report
	 * @param args supplier of the message arguments
	 * @param nullAs the handling of {@code null} values
	 * @param <V> the type of the value the constraint applies to
	 * @return a constraint predicate
	 */
	public static <V> ConstraintPredicate<V> withViolatedValue(Function<V, Optional<ViolatedValue>> violatedValue,
			ViolationMessage violationMessage, Supplier<Object[]> args, NullAs nullAs) {
		return new ConstraintPredicate<V>(v -> !violatedValue.apply(v).isPresent(), violationMessage, args, nullAs) {
			@Override
			@SuppressWarnings("NullAway") // the function is only invoked for constraint
											// predicates that handle null
			public Optional<ViolatedValue> violatedValue(@Nullable V target) {
				return violatedValue.apply(target);
			}
		};
	}

	/**
	 * Returns the supplier of the message arguments.
	 * @return the supplier of the message arguments
	 */
	public Supplier<Object[]> args() {
		return this.args;
	}

	/**
	 * Returns the default message format used when no localized message is found.
	 * @return the default message format
	 */
	public final String defaultMessageFormat() {
		return this.defaultMessageFormat;
	}

	/**
	 * Returns the message key used to look up a localized message.
	 * @return the message key
	 */
	public String messageKey() {
		return this.messageKey;
	}

	/**
	 * Returns the names of the message arguments, which are those of the violation
	 * message (see {@link ViolationMessage#paramNames()}).
	 * @return the unmodifiable names of the arguments
	 * @since 0.18.0
	 */
	public final List<String> paramNames() {
		return this.paramNames;
	}

	/**
	 * Returns the handling of {@code null} values.
	 * @return the handling of {@code null} values
	 */
	public final NullAs nullValidity() {
		return this.nullAs;
	}

	/**
	 * Returns a new constraint predicate with the given violation message.
	 *
	 * <p>
	 * The arguments stay those of this constraint, so their names are kept unless the
	 * given message names them itself.
	 * @param message the new violation message
	 * @return a constraint predicate with the given violation message
	 */
	public ConstraintPredicate<V> overrideMessage(ViolationMessage message) {
		final List<String> names = message.paramNames().isEmpty() ? this.paramNames : message.paramNames();
		return new ConstraintPredicate<V>(this.predicate, message.messageKey(), message.defaultMessageFormat(), names,
				this.args, this.nullAs) {
			@Override
			public Optional<ViolatedValue> violatedValue(@Nullable V target) {
				return ConstraintPredicate.this.violatedValue(target);
			}
		};
	}

	/**
	 * Returns a new constraint predicate with the given default message format. The
	 * message key is unchanged.
	 * @param message the new default message format
	 * @return a constraint predicate with the given default message format
	 */
	public ConstraintPredicate<V> overrideMessage(String message) {
		return new ConstraintPredicate<V>(this.predicate, this.messageKey, message, this.paramNames, this.args,
				this.nullAs) {
			@Override
			public Optional<ViolatedValue> violatedValue(@Nullable V target) {
				return ConstraintPredicate.this.violatedValue(target);
			}
		};
	}

	/**
	 * Returns the predicate that decides validity.
	 * @return the predicate, which returns {@code true} when the value is valid
	 */
	public final Predicate<V> predicate() {
		return this.predicate;
	}

	/**
	 * Returns the violation of the given value, if it violates this constraint.
	 * @param target the value to test, possibly {@code null}
	 * @return an optional containing the violated value when the value violates this
	 * constraint, or an empty optional when it is valid
	 */
	public Optional<ViolatedValue> violatedValue(@Nullable V target) {
		Predicate<V> predicate = this.predicate();
		if (predicate.test(target)) {
			return Optional.empty();
		}
		else {
			// violated
			return Optional.of(new ViolatedValue(target));
		}
	}

}
