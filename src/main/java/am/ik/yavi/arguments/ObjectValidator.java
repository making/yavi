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
	public ObjectValidator<X, T> named(String name) {
		return new ObjectValidator<>(super.renamed(name), super.mapper, this.type, super.transform);
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

	/**
	 * An {@link ObjectValidator} whose value is the validated argument itself, which is
	 * what the builder builds without a mapper. It knows its reverse by its type: the
	 * argument that a value is read from is that value. {@link #named(String)} and
	 * {@link #preprocess(Function)} keep the type; {@link #map(Function)} returns a
	 * validator that does not know its reverse, unlike {@link #map(Function, Function)}.
	 *
	 * @param <X> the type of the validated value
	 * @since 0.18.0
	 */
	public static final class Reversible<X> extends ObjectValidator<X, X>
			implements ReversibleArguments1Validator<X, X> {

		/**
		 * Creates a validator that produces the validated value itself.
		 * @param validator the validator applied to the wrapped argument
		 */
		public Reversible(Validator<Arguments1<X>> validator) {
			this(validator, null, null);
		}

		/**
		 * Creates a validator that produces the validated value itself and knows its
		 * type.
		 * @param validator the validator applied to the wrapped argument
		 * @param type the type of the validated value
		 */
		public Reversible(Validator<Arguments1<X>> validator, Class<X> type) {
			this(validator, type, null);
		}

		private Reversible(Validator<Arguments1<X>> validator, @Nullable Class<X> type,
				@Nullable Function<? super X, ? extends @Nullable X> transform) {
			super(validator, value -> value, type, transform);
		}

		@Override
		public X reverse(X value) {
			return value;
		}

		@Override
		public Reversible<X> preprocess(Function<? super X, ? extends @Nullable X> transform) {
			return new Reversible<>(super.validator, super.type, composed(transform, super.transform));
		}

		@Override
		public Reversible<X> named(String name) {
			return new Reversible<>(super.renamed(name), super.type, super.transform);
		}

	}

}
