/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.IntegerValidator;
import am.ik.yavi.constraint.IntegerConstraint;
import am.ik.yavi.core.Validator;

/**
 * Builds {@link IntegerValidator} instances that validate a single numeric value.
 *
 * <p>
 * The value is validated under the name given to {@link #of} and violations are reported
 * with that name and the message key of the violated constraint, for example
 * {@code numeric.greaterThanOrEqual}. A {@code null} value is a violation: the one of a
 * constraint that rejects it, or else {@code object.notNull}. It is accepted with
 * {@code nullable()}, {@code optional()} or {@code defaultValue(...)} of the validator.
 *
 * <p>
 * Example: <pre>{@code
 * IntegerValidator<Integer> validator = IntegerValidatorBuilder
 *     .of("seatCount", c -> c.greaterThanOrEqual(2))
 *     .build();
 * }</pre>
 *
 * @since 0.7.0
 */
public class IntegerValidatorBuilder implements ValueValidatorBuilder<Integer, Integer> {

	private final Function<ValidatorBuilder<Arguments1<Integer>>, ValidatorBuilder<Arguments1<Integer>>> builder;

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
	public static IntegerValidatorBuilder of(String name,
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
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
	public static IntegerValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<Integer>>, ValidatorBuilder<Arguments1<Integer>>> builder) {
		return new IntegerValidatorBuilder(builder);
	}

	IntegerValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<Integer>>, ValidatorBuilder<Arguments1<Integer>>> builder) {
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
	public <T extends @Nullable Object> IntegerValidator<T> build(Function<? super Integer, ? extends T> mapper) {
		final Validator<Arguments1<Integer>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new IntegerValidator<>(validator, mapper::apply);
	}

	/**
	 * Builds a validator whose result type is the validated value.
	 * @return a new validator
	 */
	@Override
	public IntegerValidator<Integer> build() {
		return build(x -> x);
	}

}
