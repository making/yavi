/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.Validator;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.fn.Function1;
import org.jspecify.annotations.Nullable;

/**
 * Validates a single enum constant and maps it to a result.
 *
 * <p>
 * Validators of this type are usually created with
 * {@link am.ik.yavi.builder.EnumValidatorBuilder}, which applies a
 * {@link am.ik.yavi.constraint.EnumConstraint} under a given name. Violations carry that
 * name and the message key of the violated constraint, for example {@code object.oneOf}.
 * A {@code null} value is a violation: the one of a constraint that rejects it, or else
 * {@code object.notNull}. It is accepted with {@code nullable()}, {@code optional()} or
 * {@code defaultValue(...)} of the validator.
 *
 * <p>
 * The validator knows its enum type, so it reads the name of a constant from an untyped
 * input, and with {@link #ignoringCase()} whatever the case of the name is.
 *
 * <p>
 * The mapper passed to the constructor or to the builder's {@code build(Function)} method
 * is applied only when validation succeeds.
 *
 * @param <E> the enum type whose constants are validated
 * @param <T> the type of the value produced from the validated input
 * @since 0.14.0
 */
public class EnumValidator<E extends Enum<E>, T extends @Nullable Object> extends DefaultArguments1Validator<E, T> {

	private final Class<E> enumType;

	private final boolean ignoringCase;

	/**
	 * Returns a new validator that applies the given function to the result of this
	 * validator's mapper.
	 *
	 * <p>
	 * The function is applied only when validation succeeds.
	 * @param <T2> the type of the value produced after mapping
	 * @param mapper the function applied to the validated value
	 * @return a new validator with the mapped result type
	 */
	@Override
	public <T2 extends @Nullable Object> EnumValidator<E, T2> map(Function<? super T, ? extends T2> mapper) {
		final Function1<E, T2> mapped = s -> mapper.apply(super.mapper.apply(s));
		return new EnumValidator<>(super.validator, mapped, this.enumType, super.transform, this.ignoringCase);
	}

	/**
	 * Returns a new validator that applies the given function to a value other than
	 * {@code null} before the value is validated. The constraints and the mapper see the
	 * transformed value.
	 * @param transform the function applied to a value other than {@code null}
	 * @return a new validator that transforms its value first
	 * @since 0.18.0
	 * @see am.ik.yavi.core.ValueValidator#preprocess(Function)
	 */
	@Override
	public EnumValidator<E, T> preprocess(Function<? super E, ? extends @Nullable E> transform) {
		return new EnumValidator<>(super.validator, super.mapper, this.enumType, composed(transform, super.transform),
				this.ignoringCase);
	}

	/**
	 * Returns a validator that produces a {@link Supplier} of the mapped value.
	 * @return a validator that produces a supplier of the validated value
	 */
	@Override
	public EnumValidator<E, Supplier<T>> lazy() {
		final Function1<E, Supplier<T>> lazy = e -> () -> super.mapper.apply(e);
		return new EnumValidator<>(super.validator, lazy, this.enumType, super.transform, this.ignoringCase);
	}

	/**
	 * Returns a new validator that reads the name of a constant from an untyped input
	 * whatever its case is: {@code "admin"} and {@code "Admin"} are {@code ADMIN}. A name
	 * that differs from the names of several constants only by its case is not taken.
	 *
	 * <p>
	 * This is how the validator reads with any reader ({@link #untyped()},
	 * {@link #readingWith(InputReader)}, an argument of {@code fromMap}). The validation
	 * of a constant and the description do not change: the description, and so a JSON
	 * Schema, holds the names of the constants as they are declared.
	 *
	 * <pre>{@code
	 * Yavi.schema()
	 *     ._enum(Yavi._enum("role", Role.class).ignoringCase())
	 *     .fromMap(role -> role)
	 *     .validate(Map.of("role", "admin")); // ADMIN
	 * }</pre>
	 *
	 * <p>
	 * The returned validator is not an {@code EnumValidator}, so the case is ignored
	 * once. It keeps ignoring the case through {@code map}, {@code preprocess} and
	 * {@code lazy}, which can be written before or after this method.
	 * @return a new validator that ignores the case of the name of a constant
	 * @since 0.18.0
	 */
	public Arguments1Validator<E, T> ignoringCase() {
		return new EnumValidator<>(super.validator, super.mapper, this.enumType, super.transform, true);
	}

	/**
	 * Returns a validator that accepts an untyped input read with the given reader, as
	 * {@link DefaultArguments1Validator#readingWith(InputReader)} states, and takes the
	 * name of a constant in any case if this validator is {@link #ignoringCase()}.
	 * @param reader the reader of the nodes
	 * @param <N> the type of the nodes
	 * @return a value validator for an untyped input
	 * @since 0.18.0
	 */
	@Override
	public <N> ValueValidator<N, T> readingWith(InputReader<N> reader) {
		final ValueValidator<N, T> exact = super.readingWith(reader);
		final Class<E> type = this.enumType;
		if (!this.ignoringCase) {
			return exact;
		}
		return new ValueValidator<>() {
			@Override
			public Validated<T> validate(@Nullable N node, Locale locale, ConstraintContext constraintContext) {
				if (node != null && !reader.isNull(node) && reader.value(node, type) instanceof String name) {
					final E constant = constant(type, name);
					if (constant != null) {
						return EnumValidator.this.validate(constant, locale, constraintContext);
					}
				}
				return exact.validate(node, locale, constraintContext);
			}

			@Override
			public ValueDescription description() {
				return EnumValidator.this.description();
			}

			@Override
			public <M> ValueValidator<M, T> readingWith(InputReader<M> other) {
				return EnumValidator.this.readingWith(other);
			}
		};
	}

	/**
	 * Returns the only constant whose name is the given one without regard to case, or
	 * {@code null} if there is none or more than one.
	 */
	private static <E extends Enum<E>> @Nullable E constant(Class<E> type, String name) {
		E found = null;
		for (E constant : type.getEnumConstants()) {
			if (constant.name().equals(name)) {
				return constant;
			}
			if (constant.name().equalsIgnoreCase(name)) {
				if (found != null) {
					return null;
				}
				found = constant;
			}
		}
		return found;
	}

	private EnumValidator(Validator<Arguments1<E>> validator, Function1<? super E, ? extends T> mapper,
			Class<E> enumType, @Nullable Function<? super E, ? extends @Nullable E> transform, boolean ignoringCase) {
		super(validator, mapper, enumType, transform);
		this.enumType = enumType;
		this.ignoringCase = ignoringCase;
	}

	/**
	 * Creates a validator that applies the given {@link Validator} to {@code Arguments1}
	 * and maps the validated value with the given function. It knows the enum type, which
	 * is reported by {@link #description()} and lets {@link #untyped()} accept the name
	 * of a constant.
	 * @param validator the validator applied to the value
	 * @param mapper the function applied to the validated value
	 * @param enumType the enum type of the value
	 * @since 0.18.0
	 */
	public EnumValidator(Validator<Arguments1<E>> validator, Function1<? super E, ? extends T> mapper,
			Class<E> enumType) {
		super(validator, mapper, enumType);
		this.enumType = enumType;
		this.ignoringCase = false;
	}

}
