/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.time.OffsetDateTime;
import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;
import org.jspecify.annotations.Nullable;

/**
 * Validates a single {@link OffsetDateTime} value and maps it to a result.
 *
 * <p>
 * Validators of this type are usually created with
 * {@link am.ik.yavi.builder.OffsetDateTimeValidatorBuilder}, which applies a
 * {@link am.ik.yavi.constraint.OffsetDateTimeConstraint} under a given name. Violations
 * carry that name and the message key of the violated constraint, for example
 * {@code temporal.after}. A {@code null} value is a violation: the one of a constraint
 * that rejects it, or else {@code object.notNull}. It is accepted with
 * {@code nullable()}, {@code optional()} or {@code defaultValue(...)} of the validator.
 *
 * <p>
 * The mapper passed to the constructor or to the builder's {@code build(Function)} method
 * is applied only when validation succeeds.
 *
 * @param <T> the type of the value produced from the validated input
 * @since 0.10.0
 */
public class OffsetDateTimeValidator<T extends @Nullable Object> extends DefaultArguments1Validator<OffsetDateTime, T> {

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
	public <T2 extends @Nullable Object> OffsetDateTimeValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new OffsetDateTimeValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)),
				super.transform);
	}

	/**
	 * Creates a validator that applies the given {@link Validator} to {@code Arguments1}
	 * and maps the validated value with the given function.
	 * @param validator the validator applied to the value
	 * @param mapper the function applied to the validated value
	 */
	public OffsetDateTimeValidator(Validator<Arguments1<OffsetDateTime>> validator,
			Function1<? super OffsetDateTime, ? extends T> mapper) {
		super(validator, mapper, OffsetDateTime.class);
	}

	private OffsetDateTimeValidator(Validator<Arguments1<OffsetDateTime>> validator,
			Function1<? super OffsetDateTime, ? extends T> mapper,
			@Nullable Function<? super OffsetDateTime, ? extends @Nullable OffsetDateTime> transform) {
		super(validator, mapper, OffsetDateTime.class, transform);
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
	public OffsetDateTimeValidator<T> preprocess(
			Function<? super OffsetDateTime, ? extends @Nullable OffsetDateTime> transform) {
		return new OffsetDateTimeValidator<>(super.validator, super.mapper, composed(transform, super.transform));
	}

}
