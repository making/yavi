/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.time.LocalDateTime;
import java.util.function.Function;

import am.ik.yavi.fn.Function1;
import org.jspecify.annotations.Nullable;

import am.ik.yavi.arguments.DefaultArguments1Validator;
import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.LocalDateTimeValidator;
import am.ik.yavi.constraint.LocalDateTimeConstraint;
import am.ik.yavi.core.Validator;

/**
 * Builds {@link LocalDateTimeValidator} instances that validate a single
 * {@link LocalDateTime} value.
 *
 * <p>
 * The value is validated under the name given to {@link #of} and violations are reported
 * with that name and the message key of the violated constraint, for example
 * {@code temporal.pastOrPresent}. A {@code null} value is a violation: the one of a
 * constraint that rejects it, or else {@code object.notNull}. It is accepted with
 * {@code nullable()}, {@code optional()} or {@code defaultValue(...)} of the validator.
 *
 * <p>
 * Example: <pre>{@code
 * LocalDateTimeValidator<LocalDateTime> validator = LocalDateTimeValidatorBuilder
 *     .of("scheduledAt", c -> c.futureOrPresent())
 *     .build();
 * }</pre>
 *
 * @since 0.10.0
 */
public class LocalDateTimeValidatorBuilder implements ValueValidatorBuilder<LocalDateTime, LocalDateTime> {

	private final Function<ValidatorBuilder<Arguments1<LocalDateTime>>, ValidatorBuilder<Arguments1<LocalDateTime>>> builder;

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
	public static LocalDateTimeValidatorBuilder of(String name,
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
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
	public static LocalDateTimeValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<LocalDateTime>>, ValidatorBuilder<Arguments1<LocalDateTime>>> builder) {
		return new LocalDateTimeValidatorBuilder(builder);
	}

	LocalDateTimeValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<LocalDateTime>>, ValidatorBuilder<Arguments1<LocalDateTime>>> builder) {
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
	public <T extends @Nullable Object> LocalDateTimeValidator<T> build(
			Function<? super LocalDateTime, ? extends T> mapper) {
		return this.create(mapper::apply);
	}

	private <T extends @Nullable Object> LocalDateTimeValidator<T> create(
			Function1<? super LocalDateTime, ? extends T> mapper) {
		final Validator<Arguments1<LocalDateTime>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new LocalDateTimeValidator<>(validator, mapper);
	}

	/**
	 * Builds a validator whose result type is the validated value.
	 * @return a new validator
	 */
	@Override
	public LocalDateTimeValidator<LocalDateTime> build() {
		return this.create(DefaultArguments1Validator.unmapped());
	}

}
