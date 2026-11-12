/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.time.Instant;
import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;
import org.jspecify.annotations.Nullable;

/**
 * Validates a single {@link Instant} value and maps it to a result.
 *
 * <p>
 * Validators of this type are usually created with
 * {@link am.ik.yavi.builder.InstantValidatorBuilder}, which applies a
 * {@link am.ik.yavi.constraint.InstantConstraint} under a given name. Violations carry
 * that name and the message key of the violated constraint, for example
 * {@code temporal.pastOrPresent}. A {@code null} value is accepted unless the configured
 * constraints require otherwise (for example {@code notNull()}).
 *
 * <p>
 * The mapper passed to the constructor or to the builder's {@code build(Function)} method
 * is applied only when validation succeeds.
 *
 * @param <T> the type of the value produced from the validated input
 * @since 0.10.0
 */
public class InstantValidator<T> extends DefaultArguments1Validator<Instant, T> {

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
	public <T2> InstantValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new InstantValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)), super.transform);
	}

	/**
	 * Creates a validator that applies the given {@link Validator} to {@code Arguments1}
	 * and maps the validated value with the given function.
	 * @param validator the validator applied to the value
	 * @param mapper the function applied to the validated value
	 */
	public InstantValidator(Validator<Arguments1<Instant>> validator, Function1<? super Instant, ? extends T> mapper) {
		super(validator, mapper, Instant.class);
	}

	private InstantValidator(Validator<Arguments1<Instant>> validator, Function1<? super Instant, ? extends T> mapper,
			@Nullable Function<? super Instant, ? extends @Nullable Instant> transform) {
		super(validator, mapper, Instant.class, transform);
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
	public InstantValidator<T> preprocess(Function<? super Instant, ? extends @Nullable Instant> transform) {
		return new InstantValidator<>(super.validator, super.mapper, composed(transform, super.transform));
	}

}
