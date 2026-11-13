/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.time.ZonedDateTime;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.ZonedDateTimeValidator;
import am.ik.yavi.constraint.ZonedDateTimeConstraint;
import am.ik.yavi.core.Validator;

/**
 * Builds {@link ZonedDateTimeValidator} instances that validate a single
 * {@link ZonedDateTime} value.
 *
 * <p>
 * The value is validated under the name given to {@link #of} and violations are reported
 * with that name and the message key of the violated constraint, for example
 * {@code temporal.future}. A {@code null} value is accepted unless the configured
 * constraints require otherwise (for example {@code notNull()}).
 *
 * <p>
 * Example: <pre>{@code
 * ZonedDateTimeValidator<ZonedDateTime> validator = ZonedDateTimeValidatorBuilder
 *     .of("expiresAt", c -> c.future())
 *     .build();
 * }</pre>
 *
 * @since 0.10.0
 */
public class ZonedDateTimeValidatorBuilder implements ValueValidatorBuilder<ZonedDateTime, ZonedDateTime> {

	private final Function<ValidatorBuilder<Arguments1<ZonedDateTime>>, ValidatorBuilder<Arguments1<ZonedDateTime>>> builder;

	/**
	 * Returns a builder that validates the value under the given name with the given
	 * constraints.
	 *
	 * <p>
	 * The name is used as the violation name of every resulting violation. A {@code null}
	 * value is accepted unless the constraints require otherwise.
	 * @param name the property name used as the violation name
	 * @param constraints the function that configures the constraints
	 * @return a new builder
	 */
	public static ZonedDateTimeValidatorBuilder of(String name,
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
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
	public static ZonedDateTimeValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<ZonedDateTime>>, ValidatorBuilder<Arguments1<ZonedDateTime>>> builder) {
		return new ZonedDateTimeValidatorBuilder(builder);
	}

	ZonedDateTimeValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<ZonedDateTime>>, ValidatorBuilder<Arguments1<ZonedDateTime>>> builder) {
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
	public <T> ZonedDateTimeValidator<T> build(Function<? super ZonedDateTime, ? extends @Nullable T> mapper) {
		final Validator<Arguments1<ZonedDateTime>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new ZonedDateTimeValidator<>(validator, mapper::apply);
	}

	/**
	 * Builds a validator whose result type is the validated value.
	 * @return a new validator
	 */
	@Override
	public ZonedDateTimeValidator<ZonedDateTime> build() {
		return build(x -> x);
	}

}
