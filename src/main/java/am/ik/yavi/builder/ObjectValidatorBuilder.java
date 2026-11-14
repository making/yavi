/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import am.ik.yavi.fn.Function1;
import org.jspecify.annotations.Nullable;

import am.ik.yavi.arguments.DefaultArguments1Validator;
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
 * {@code object.notNull}. A {@code null} value is a violation: the one of a constraint
 * that rejects it, or else {@code object.notNull}. It is accepted with
 * {@code nullable()}, {@code optional()} or {@code defaultValue(...)} of the validator.
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

	private final @Nullable Class<X> type;

	/**
	 * Returns a builder that validates the value under the given name with the given
	 * constraints.
	 *
	 * <p>
	 * The name is used as the violation name of every resulting violation. A {@code null}
	 * value is a violation: the one of a constraint that rejects it, or else
	 * {@code object.notNull}. It is accepted with {@code nullable()}, {@code optional()}
	 * or {@code defaultValue(...)} of the validator.
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
	 * Creates a builder like {@link #of(String, Function)} that also knows the type of
	 * the value. The built validator reports it in its description and can validate an
	 * untyped input with {@code untyped()}.
	 * @param name the property name used in violation names and messages
	 * @param type the type of the value
	 * @param constraints function that configures the constraints
	 * @param <X> the type of the value to validate
	 * @return a new builder
	 * @since 0.18.0
	 */
	public static <X> ObjectValidatorBuilder<X> of(String name, Class<X> type,
			Function<ObjectConstraint<Arguments1<X>, X>, ObjectConstraint<Arguments1<X>, X>> constraints) {
		return new ObjectValidatorBuilder<>(b -> b.constraintOnObject(Arguments1::arg1, name, constraints), type);
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
		this(builder, null);
	}

	private ObjectValidatorBuilder(Function<ValidatorBuilder<Arguments1<X>>, ValidatorBuilder<Arguments1<X>>> builder,
			@Nullable Class<X> type) {
		this.builder = builder;
		this.type = type;
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
	public <T extends @Nullable Object> ObjectValidator<X, T> build(Function<? super X, ? extends T> mapper) {
		return this.create(mapper::apply);
	}

	private <T extends @Nullable Object> ObjectValidator<X, T> create(Function1<? super X, ? extends T> mapper) {
		final Validator<Arguments1<X>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		final Class<X> type = this.type;
		return type == null ? new ObjectValidator<>(validator, mapper) : new ObjectValidator<>(validator, mapper, type);
	}

	/**
	 * Builds a validator whose result type is the validated value.
	 * @return a new validator
	 */
	@Override

	public ObjectValidator<X, X> build() {
		return this.create(DefaultArguments1Validator.unmapped());
	}

}
