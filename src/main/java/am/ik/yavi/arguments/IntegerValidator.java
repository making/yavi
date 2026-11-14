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
 * Validates a single numeric value and maps it to a result.
 *
 * <p>
 * Validators of this type are usually created with
 * {@link am.ik.yavi.builder.IntegerValidatorBuilder}, which applies a
 * {@link am.ik.yavi.constraint.IntegerConstraint} under a given name. Violations carry
 * that name and the message key of the violated constraint, for example
 * {@code numeric.greaterThanOrEqual}. A {@code null} value is a violation: the one of a
 * constraint that rejects it, or else {@code object.notNull}. It is accepted with
 * {@code nullable()}, {@code optional()} or {@code defaultValue(...)} of the validator.
 *
 * <p>
 * The mapper passed to the constructor or to the builder's {@code build(Function)} method
 * is applied only when validation succeeds.
 *
 * @param <T> the type of the value produced from the validated input
 * @since 0.7.0
 */
public class IntegerValidator<T extends @Nullable Object> extends DefaultArguments1Validator<Integer, T> {

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
	public <T2 extends @Nullable Object> IntegerValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new IntegerValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)), super.transform);
	}

	/**
	 * Creates a validator that applies the given {@link Validator} to {@code Arguments1}
	 * and maps the validated value with the given function.
	 * @param validator the validator applied to the value
	 * @param mapper the function applied to the validated value
	 */
	public IntegerValidator(Validator<Arguments1<Integer>> validator, Function1<? super Integer, ? extends T> mapper) {
		super(validator, mapper, Integer.class);
	}

	private IntegerValidator(Validator<Arguments1<Integer>> validator, Function1<? super Integer, ? extends T> mapper,
			@Nullable Function<? super Integer, ? extends @Nullable Integer> transform) {
		super(validator, mapper, Integer.class, transform);
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
	public IntegerValidator<T> preprocess(Function<? super Integer, ? extends @Nullable Integer> transform) {
		return new IntegerValidator<>(super.validator, super.mapper, composed(transform, super.transform));
	}

	/**
	 * Returns a validator with the same constraints that reports the violations of the
	 * value under the given name instead of the name it was built with, so that the rule
	 * of a value is defined once and used under several names.
	 * @param name the name under which the violations of the value are reported
	 * @return a validator of the value under the given name
	 * @since 0.18.0
	 * @see am.ik.yavi.core.ValueValidator#named(String)
	 */
	@Override
	public IntegerValidator<T> named(String name) {
		return new IntegerValidator<>(super.renamed(name), super.mapper, super.transform);
	}

	/**
	 * A {@link IntegerValidator} whose value is the validated argument itself, which is
	 * what the builder builds without a mapper. It knows its reverse by its type: the
	 * argument that a value is read from is that value. {@link #named(String)} and
	 * {@link #preprocess(Function)} keep the type; {@link #map(Function)} returns a
	 * validator that does not know its reverse, unlike {@link #map(Function, Function)}.
	 *
	 * @since 0.18.0
	 */
	public static final class Reversible extends IntegerValidator<Integer>
			implements ReversibleArguments1Validator<Integer, Integer> {

		/**
		 * Creates a validator that produces the validated value itself.
		 * @param validator the validator applied to the wrapped argument
		 */
		public Reversible(Validator<Arguments1<Integer>> validator) {
			this(validator, null);
		}

		private Reversible(Validator<Arguments1<Integer>> validator,
				@Nullable Function<? super Integer, ? extends @Nullable Integer> transform) {
			super(validator, value -> value, transform);
		}

		@Override
		public Integer reverse(Integer value) {
			return value;
		}

		@Override
		public Reversible preprocess(Function<? super Integer, ? extends @Nullable Integer> transform) {
			return new Reversible(super.validator, composed(transform, super.transform));
		}

		@Override
		public Reversible named(String name) {
			return new Reversible(super.renamed(name), super.transform);
		}

	}

}
