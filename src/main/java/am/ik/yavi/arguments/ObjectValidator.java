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
 * {@code object.notNull}. A {@code null} value is accepted unless the configured
 * constraints require otherwise (for example {@code notNull()}).
 *
 * <p>
 * The mapper passed to the constructor or to the builder's {@code build(Function)} method
 * is applied only when validation succeeds.
 *
 * @param <X> the type of the value to validate
 * @param <T> the type of the value produced from the validated input
 * @since 0.8.0
 */
public class ObjectValidator<X, T> extends DefaultArguments1Validator<X, T> {

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
	public <T2> ObjectValidator<X, T2> map(Function<? super T, ? extends T2> mapper) {
		final Class<X> type = this.type;
		final Function1<X, T2> mapped = s -> mapper.apply(super.mapper.apply(s));
		return type == null ? new ObjectValidator<>(super.validator, mapped)
				: new ObjectValidator<>(super.validator, mapped, type);
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
