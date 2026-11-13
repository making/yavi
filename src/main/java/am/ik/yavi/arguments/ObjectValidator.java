/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;
import org.jspecify.annotations.Nullable;

/**
 * Validates a single object value and maps it to a result.
 *
 * <p>
 * Validators of this type are usually created with
 * {@link am.ik.yavi.builder.ObjectValidatorBuilder}, which applies a
 * {@link am.ik.yavi.constraint.ObjectConstraint} under a given name. Violations carry
 * that name and the message key of the violated constraint, for example
 * {@code object.notNull}. A {@code null} value is a violation: the one of a constraint
 * that rejects it, or else {@code object.notNull}. It is accepted with
 * {@code nullable()}, {@code optional()} or {@code defaultValue(...)} of the validator.
 *
 * <p>
 * The mapper passed to the constructor or to the builder's {@code build(Function)} method
 * is applied only when validation succeeds.
 *
 * @param <X> the type of the value to validate
 * @param <T> the type of the value produced from the validated input
 * @since 0.8.0
 * @see am.ik.yavi.input.ObjectInputValidator the validator that reads the properties of
 * an object from an untyped input such as a map
 */
public class ObjectValidator<X, T extends @Nullable Object> extends DefaultArguments1Validator<X, T> {

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
	private final @Nullable Class<X> type;

	@Override
	public <T2 extends @Nullable Object> ObjectValidator<X, T2> map(Function<? super T, ? extends T2> mapper) {
		final Function1<X, T2> mapped = s -> mapper.apply(super.mapper.apply(s));
		return new ObjectValidator<>(super.validator, mapped, this.type, super.transform);
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
	public ObjectValidator<X, T> preprocess(Function<? super X, ? extends @Nullable X> transform) {
		return new ObjectValidator<>(super.validator, super.mapper, this.type, composed(transform, super.transform));
	}

	private ObjectValidator(Validator<Arguments1<X>> validator, Function1<? super X, ? extends T> mapper,
			@Nullable Class<X> type, @Nullable Function<? super X, ? extends @Nullable X> transform) {
		super(validator, mapper, type == null ? Object.class : type, transform);
		this.type = type;
	}

	/**
	 * Creates a validator that applies the given {@link Validator} to {@code Arguments1}
	 * and maps the validated value with the given function.
	 * @param validator the validator applied to the value
	 * @param mapper the function applied to the validated value
	 */
	public ObjectValidator(Validator<Arguments1<X>> validator, Function1<? super X, ? extends T> mapper) {
		super(validator, mapper, Object.class);
		this.type = null;
	}

	/**
	 * Creates a validator like {@link #ObjectValidator(Validator, Function1)} that also
	 * knows the type of the value, which is reported by {@link #description()} and lets
	 * {@link #untyped()} check an untyped input against it.
	 * @param validator the validator applied to the value
	 * @param mapper the function applied to the validated value
	 * @param type the type of the value
	 * @since 0.18.0
	 */
	public ObjectValidator(Validator<Arguments1<X>> validator, Function1<? super X, ? extends T> mapper,
			Class<X> type) {
		super(validator, mapper, type);
		this.type = type;
	}

}
