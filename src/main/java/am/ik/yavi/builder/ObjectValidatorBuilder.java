/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.ObjectValidator;
import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.core.Validator;

/**
 * Builds {@link ObjectValidator} instances that validate a single object value.
 *
 * <p>
 * The value is validated under the name given to {@link #of} and violations are reported
 * with that name and the message key of the violated constraint, for example
 * {@code object.notNull}. A {@code null} value is accepted unless the configured
 * constraints require otherwise (for example {@code notNull()}).
 *
 * <p>
 * Example: <pre>{@code
 * ObjectValidator<Order, Order> validator = ObjectValidatorBuilder.<Order>of("order",
 *     c -> c.notNull()).build();
 * }</pre>
 *
 * @param <X> the type of the value to validate
 * @since 0.8.0
 */
public class ObjectValidatorBuilder<X> implements ValueValidatorBuilder<X, X> {

	private final Function<ValidatorBuilder<Arguments1<X>>, ValidatorBuilder<Arguments1<X>>> builder;

	/**
	 * Returns a builder that validates the value under the given name with the given
	 * constraints.
	 *
	 * <p>
	 * The name is used as the violation name of every resulting violation. A {@code null}
	 * value is accepted unless the constraints require otherwise.
	 * @param <X> the type of the value to validate
	 * @param name the property name used as the violation name
	 * @param constraints the function that configures the constraints
	 * @return a new builder
	 */
	public static <X> ObjectValidatorBuilder<X> of(String name,
			Function<ObjectConstraint<Arguments1<X>, X>, ObjectConstraint<Arguments1<X>, X>> constraints) {
		return wrap(b -> b.constraintOnObject(Arguments1::arg1, name, constraints));
	}

	/**
	 * Returns a builder that applies the given function to a new {@link ValidatorBuilder}
	 * to define the validation rules of a single value.
	 *
	 * <p>
	 * This method is useful when the rules need builder-level configuration, such as
	 * conditional constraints or a custom message formatter, in addition to value
	 * constraints.
	 * @param <X> the type of the value to validate
	 * @param builder the function that configures the underlying builder
	 * @return a new builder
	 * @since 0.11.3
	 */
	public static <X> ObjectValidatorBuilder<X> wrap(
			Function<ValidatorBuilder<Arguments1<X>>, ValidatorBuilder<Arguments1<X>>> builder) {
		return new ObjectValidatorBuilder<>(builder);
	}

	ObjectValidatorBuilder(Function<ValidatorBuilder<Arguments1<X>>, ValidatorBuilder<Arguments1<X>>> builder) {
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
	public <T> ObjectValidator<X, T> build(Function<? super X, ? extends T> mapper) {
		final Validator<Arguments1<X>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new ObjectValidator<>(validator, mapper::apply);
	}

	/**
	 * Builds a validator whose result type is the validated value.
	 * @return a new validator
	 */
	@Override
	public ObjectValidator<X, X> build() {
		return build(x -> x);
	}

}
