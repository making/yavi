/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.core.Validator;

/**
 * Builds {@link StringValidator} instances that validate a single {@link String} value.
 *
 * <p>
 * The value is validated under the name given to {@link #of} and violations are reported
 * with that name and the message key of the violated constraint, for example
 * {@code charSequence.notBlank}. A {@code null} value is accepted unless the configured
 * constraints require otherwise (for example {@code notNull()}).
 *
 * <p>
 * Example: <pre>{@code
 * StringValidator<String> validator = StringValidatorBuilder
 *     .of("licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
 *     .build();
 * }</pre>
 *
 * @since 0.7.0
 */
public class StringValidatorBuilder implements ValueValidatorBuilder<String, String> {

	private final Function<ValidatorBuilder<Arguments1<String>>, ValidatorBuilder<Arguments1<String>>> builder;

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
	public static StringValidatorBuilder of(String name,
			Function<CharSequenceConstraint<Arguments1<String>, String>, CharSequenceConstraint<Arguments1<String>, String>> constraints) {
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
	public static StringValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<String>>, ValidatorBuilder<Arguments1<String>>> builder) {
		return new StringValidatorBuilder(builder);
	}

	StringValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<String>>, ValidatorBuilder<Arguments1<String>>> builder) {
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
	public <T> StringValidator<T> build(Function<? super String, ? extends T> mapper) {
		final Validator<Arguments1<String>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new StringValidator<>(validator, mapper::apply);
	}

	/**
	 * Builds a validator whose result type is the validated value.
	 * @return a new validator
	 */
	@Override
	public StringValidator<String> build() {
		return build(x -> x);
	}

}
