/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.time.OffsetDateTime;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.OffsetDateTimeValidator;
import am.ik.yavi.constraint.OffsetDateTimeConstraint;
import am.ik.yavi.core.Validator;

/**
 * Builds {@link OffsetDateTimeValidator} instances that validate a single
 * {@link OffsetDateTime} value.
 *
 * <p>
 * The value is validated under the name given to {@link #of} and violations are reported
 * with that name and the message key of the violated constraint, for example
 * {@code temporal.after}. A {@code null} value is a violation: the one of a constraint
 * that rejects it, or else {@code object.notNull}. It is accepted with
 * {@code nullable()}, {@code optional()} or {@code defaultValue(...)} of the validator.
 *
 * <p>
 * Example: <pre>{@code
 * OffsetDateTimeValidator<OffsetDateTime> validator = OffsetDateTimeValidatorBuilder
 *     .of("acceptedAt", c -> c.after(() -> OffsetDateTime.now()))
 *     .build();
 * }</pre>
 *
 * @since 0.10.0
 */
public class OffsetDateTimeValidatorBuilder implements ValueValidatorBuilder<OffsetDateTime, OffsetDateTime> {

	private final Function<ValidatorBuilder<Arguments1<OffsetDateTime>>, ValidatorBuilder<Arguments1<OffsetDateTime>>> builder;

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
	public static OffsetDateTimeValidatorBuilder of(String name,
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
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
	public static OffsetDateTimeValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<OffsetDateTime>>, ValidatorBuilder<Arguments1<OffsetDateTime>>> builder) {
		return new OffsetDateTimeValidatorBuilder(builder);
	}

	OffsetDateTimeValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<OffsetDateTime>>, ValidatorBuilder<Arguments1<OffsetDateTime>>> builder) {
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
	public <T extends @Nullable Object> OffsetDateTimeValidator<T> build(
			Function<? super OffsetDateTime, ? extends T> mapper) {
		final Validator<Arguments1<OffsetDateTime>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new OffsetDateTimeValidator<>(validator, mapper::apply);
	}

	/**
	 * Builds a validator whose result type is the validated value. The validator knows
	 * its reverse by its type: the argument that a value is read from is that value.
	 * @return a new validator
	 */
	@Override
	public OffsetDateTimeValidator.Reversible build() {
		return new OffsetDateTimeValidator.Reversible(this.builder.apply(ValidatorBuilder.of()).build());
	}

}
