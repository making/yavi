/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.EnumValidator;
import am.ik.yavi.constraint.EnumConstraint;
import am.ik.yavi.core.Validator;

/**
 * Builds {@link EnumValidator} instances that validate a single enum constant.
 *
 * <p>
 * The value is validated under the name given to {@link #of} and violations are reported
 * with that name and the message key of the violated constraint, for example
 * {@code object.oneOf}. A {@code null} value is accepted unless the configured
 * constraints require otherwise (for example {@code notNull()}).
 *
 * <p>
 * Example: <pre>{@code
 * EnumValidator<Status, Status> validator = EnumValidatorBuilder.<Status>of("status",
 *     c -> c.oneOf(Status.ACTIVE)).build();
 * }</pre>
 *
 * @param <E> the enum type whose constants are validated
 * @since 0.14.0
 */
public class EnumValidatorBuilder<E extends Enum<E>> implements ValueValidatorBuilder<E, E> {

	private final Function<ValidatorBuilder<Arguments1<E>>, ValidatorBuilder<Arguments1<E>>> builder;

	private final @Nullable Class<E> type;

	/**
	 * Returns a builder that validates the value under the given name with the given
	 * constraints.
	 *
	 * <p>
	 * The name is used as the violation name of every resulting violation. A {@code null}
	 * value is accepted unless the constraints require otherwise.
	 * @param <E> the enum type whose constants are validated
	 * @param name the property name used as the violation name
	 * @param constraints the function that configures the constraints
	 * @return a new builder
	 */
	public static <E extends Enum<E>> EnumValidatorBuilder<E> of(String name,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * Creates a builder like {@link #of(String, Function)} that also knows the enum type
	 * of the value. The built validator reports it in its description and can validate an
	 * untyped input with {@code untyped()}.
	 * @param name the property name used in violation names and messages
	 * @param type the enum type of the value
	 * @param constraints function that configures the constraints
	 * @param <E> the type of the value to validate
	 * @return a new builder
	 * @since 0.18.0
	 */
	public static <E extends Enum<E>> EnumValidatorBuilder<E> of(String name, Class<E> type,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return new EnumValidatorBuilder<>(b -> b.constraint(Arguments1::arg1, name, constraints), type);
	}

	/**
	 * Returns a builder that applies the given function to a new {@link ValidatorBuilder}
	 * to define the validation rules of a single value.
	 *
	 * <p>
	 * This method is useful when the rules need builder-level configuration, such as
	 * conditional constraints or a custom message formatter, in addition to value
	 * constraints.
	 * @param <E> the enum type whose constants are validated
	 * @param builder the function that configures the underlying builder
	 * @return a new builder
	 */
	public static <E extends Enum<E>> EnumValidatorBuilder<E> wrap(
			Function<ValidatorBuilder<Arguments1<E>>, ValidatorBuilder<Arguments1<E>>> builder) {
		return new EnumValidatorBuilder<>(builder);
	}

	EnumValidatorBuilder(Function<ValidatorBuilder<Arguments1<E>>, ValidatorBuilder<Arguments1<E>>> builder) {
		this(builder, null);
	}

	private EnumValidatorBuilder(Function<ValidatorBuilder<Arguments1<E>>, ValidatorBuilder<Arguments1<E>>> builder,
			@Nullable Class<E> type) {
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
	public <T> EnumValidator<E, T> build(Function<? super E, ? extends @Nullable T> mapper) {
		final Validator<Arguments1<E>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		final Class<E> type = this.type;
		return type == null ? new EnumValidator<>(validator, mapper::apply)
				: new EnumValidator<>(validator, mapper::apply, type);
	}

	/**
	 * Builds a validator whose result type is the validated enum type.
	 * @return a new validator
	 */
	@Override
	// A validated value may be null; the type parameter does not express it.
	@SuppressWarnings("NullAway")
	public EnumValidator<E, E> build() {
		return build(e -> e);
	}

}
