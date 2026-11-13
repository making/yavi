/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.constraint.StringValueConstraint;
import am.ik.yavi.core.Validator;

/**
 * Builds {@link StringValidator} instances that validate a single {@link String} value.
 *
 * <p>
 * The value is validated under the name given to {@link #of} and violations are reported
 * with that name and the message key of the violated constraint, for example
 * {@code charSequence.notBlank}. A {@code null} value is a violation: the one of a
 * constraint that rejects it, or else {@code object.notNull}. It is accepted with
 * {@code nullable()}, {@code optional()} or {@code defaultValue(...)} of the validator.
 *
 * <p>
 * The value can be transformed before it is constrained with the transforms of
 * {@link StringValueConstraint}, for example
 * {@code c -> c.trim().toLowerCase().notBlank().email()}. The constraints and the built
 * validator see the transformed value.
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

	private final @Nullable Function<String, @Nullable String> transform;

	/**
	 * Returns a builder that validates the value under the given name with the given
	 * constraints.
	 *
	 * <p>
	 * The name is used as the violation name of every resulting violation. A {@code null}
	 * value is a violation: the one of a constraint that rejects it, or else
	 * {@code object.notNull}. It is accepted with {@code nullable()}, {@code optional()}
	 * or {@code defaultValue(...)} of the validator.
	 *
	 * <p>
	 * The function is applied once, when this method is called. The transforms it adds
	 * are applied to a value other than {@code null} before the constraints.
	 * @param name the property name used as the violation name
	 * @param constraints the function that configures the transforms and the constraints
	 * @return a new builder
	 */
	public static StringValidatorBuilder of(String name,
			Function<? super StringValueConstraint<Arguments1<String>>, ? extends CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		final StringValueConstraint<Arguments1<String>> constraint = new StringValueConstraint<>();
		// not necessarily the given instance: normalizer() and variant() return a copy
		final CharSequenceConstraint<Arguments1<String>, String> configured = constraints.apply(constraint);
		return new StringValidatorBuilder(b -> b._string(Arguments1::arg1, name, unused -> configured),
				constraint.transformation());
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
		return new StringValidatorBuilder(builder, null);
	}

	StringValidatorBuilder(Function<ValidatorBuilder<Arguments1<String>>, ValidatorBuilder<Arguments1<String>>> builder,
			@Nullable Function<String, @Nullable String> transform) {
		this.builder = builder;
		this.transform = transform;
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
	public <T extends @Nullable Object> StringValidator<T> build(Function<? super String, ? extends T> mapper) {
		final Validator<Arguments1<String>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		final StringValidator<T> built = new StringValidator<>(validator, mapper::apply);
		return this.transform == null ? built : built.preprocess(this.transform);
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
