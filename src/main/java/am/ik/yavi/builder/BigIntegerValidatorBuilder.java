/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.math.BigInteger;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.BigIntegerValidator;
import am.ik.yavi.constraint.BigIntegerConstraint;
import am.ik.yavi.core.Validator;

/**
 * Builds {@link BigIntegerValidator} instances that validate a single numeric value.
 *
 * <p>
 * The value is validated under the name given to {@link #of} and violations are reported
 * with that name and the message key of the violated constraint, for example
 * {@code numeric.positive}. A {@code null} value is accepted unless the configured
 * constraints require otherwise (for example {@code notNull()}).
 *
 * <p>
 * Example: <pre>{@code
 * BigIntegerValidator<BigInteger> validator = BigIntegerValidatorBuilder
 *     .of("count", c -> c.positive())
 *     .build();
 * }</pre>
 *
 * @since 0.7.0
 */
public class BigIntegerValidatorBuilder implements ValueValidatorBuilder<BigInteger, BigInteger> {

	private final Function<ValidatorBuilder<Arguments1<BigInteger>>, ValidatorBuilder<Arguments1<BigInteger>>> builder;

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
	public static BigIntegerValidatorBuilder of(String name,
			Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
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
	public static BigIntegerValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<BigInteger>>, ValidatorBuilder<Arguments1<BigInteger>>> builder) {
		return new BigIntegerValidatorBuilder(builder);
	}

	BigIntegerValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<BigInteger>>, ValidatorBuilder<Arguments1<BigInteger>>> builder) {
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
	public <T> BigIntegerValidator<T> build(Function<@Nullable BigInteger, ? extends @Nullable T> mapper) {
		final Validator<Arguments1<BigInteger>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new BigIntegerValidator<>(validator, value -> mapper.apply(value));
	}

	/**
	 * Builds a validator whose result type is the validated value.
	 * @return a new validator
	 */
	@Override
	public BigIntegerValidator<BigInteger> build() {
		return build(x -> x);
	}

}
