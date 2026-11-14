/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.time.LocalTime;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.LocalTimeValidator;
import am.ik.yavi.constraint.LocalTimeConstraint;
import am.ik.yavi.core.Validator;

/**
 * Builds {@link LocalTimeValidator} instances that validate a single {@link LocalTime}
 * value.
 *
 * <p>
 * The value is validated under the name given to {@link #of} and violations are reported
 * with that name and the message key of the violated constraint, for example
 * {@code temporal.future}. A {@code null} value is a violation: the one of a constraint
 * that rejects it, or else {@code object.notNull}. It is accepted with
 * {@code nullable()}, {@code optional()} or {@code defaultValue(...)} of the validator.
 *
 * <p>
 * Example: <pre>{@code
 * LocalTimeValidator<LocalTime> validator = LocalTimeValidatorBuilder
 *     .of("startTime", c -> c.future())
 *     .build();
 * }</pre>
 *
 * @since 0.14.0
 */
public class LocalTimeValidatorBuilder implements ValueValidatorBuilder<LocalTime, LocalTime> {

	private final Function<ValidatorBuilder<Arguments1<LocalTime>>, ValidatorBuilder<Arguments1<LocalTime>>> builder;

	/**
	 * Returns a builder that validates the value under the given name with the given
	 * constraints.
	 *
	 * <p>
	 * The name is used as the violation name of every resulting violation. A {@code null}
	 * value is a violation: the one of a constraint that rejects it, or else
	 * {@code object.notNull}. It is accepted with {@code nullable()}, {@code optional()}
	 * or {@code defaultValue(...)} of the validator.
	 * @param name the property name used as the violation name
	 * @param constraints the function that configures the constraints
	 * @return a new builder
	 */
	public static LocalTimeValidatorBuilder of(String name,
			Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * Returns a builder that applies the given function to a new {@link ValidatorBuilder}
	 * to define the validation rules of a single value.
	 *
	 * <p>
	 * This method is useful when the rules need builder-level configuration, such as
	 * conditional constraints or a custom message formatter, in addition to value
	 * constraints.
	 * @param builder the function that configures the underlying builder
	 * @return a new builder
	 * @since 0.11.3
	 */
	public static LocalTimeValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<LocalTime>>, ValidatorBuilder<Arguments1<LocalTime>>> builder) {
		return new LocalTimeValidatorBuilder(builder);
	}

	LocalTimeValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<LocalTime>>, ValidatorBuilder<Arguments1<LocalTime>>> builder) {
		this.builder = builder;
	}

	/**
	 * Builds a validator that maps the validated value with the given function.
	 *
	 * <p>
	 * The function is applied only when validation succeeds.
	 * @param <T> the type of the value produced after mapping
	 * @param mapper the function applied to the validated value
	 * @return a new validator
	 */
	@Override
	public <T extends @Nullable Object> LocalTimeValidator<T> build(Function<? super LocalTime, ? extends T> mapper) {
		final Validator<Arguments1<LocalTime>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new LocalTimeValidator<>(validator, mapper::apply);
	}

	/**
	 * Builds a validator whose result type is the validated value. The validator knows
	 * its reverse by its type: the argument that a value is read from is that value.
	 * @return a new validator
	 */
	@Override
	public LocalTimeValidator.Reversible build() {
		return new LocalTimeValidator.Reversible(this.builder.apply(ValidatorBuilder.of()).build());
	}

}
