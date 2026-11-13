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
 * Validates a single {@link String} value and maps it to a result.
 *
 * <p>
 * Validators of this type are usually created with
 * {@link am.ik.yavi.builder.StringValidatorBuilder}, which applies a
 * {@link am.ik.yavi.constraint.CharSequenceConstraint} under a given name. Violations
 * carry that name and the message key of the violated constraint, for example
 * {@code charSequence.notBlank}. A {@code null} value is a violation: the one of a
 * constraint that rejects it, or else {@code object.notNull}. It is accepted with
 * {@code nullable()}, {@code optional()} or {@code defaultValue(...)} of the validator.
 *
 * <p>
 * A reader that trims text ({@link am.ik.yavi.core.StringCoercion.Strict#trimming()
 * StringCoercion.strict().trimming()}) trims the value too; {@link #untrimmed()} keeps
 * the white space around a value that consists of it, such as a password.
 *
 * <p>
 * The mapper passed to the constructor or to the builder's {@code build(Function)} method
 * is applied only when validation succeeds.
 *
 * @param <T> the type of the value produced from the validated input
 * @since 0.7.0
 */
public class StringValidator<T extends @Nullable Object> extends DefaultArguments1Validator<String, T> {

	private final boolean untrimmed;

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
	public <T2 extends @Nullable Object> StringValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new StringValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)), super.transform,
				this.untrimmed);
	}

	/**
	 * Creates a validator that applies the given {@link Validator} to {@code Arguments1}
	 * and maps the validated value with the given function.
	 * @param validator the validator applied to the value
	 * @param mapper the function applied to the validated value
	 */
	public StringValidator(Validator<Arguments1<String>> validator, Function1<? super String, ? extends T> mapper) {
		super(validator, mapper, String.class);
		this.untrimmed = false;
	}

	private StringValidator(Validator<Arguments1<String>> validator, Function1<? super String, ? extends T> mapper,
			@Nullable Function<? super String, ? extends @Nullable String> transform, boolean untrimmed) {
		super(validator, mapper, String.class, transform);
		this.untrimmed = untrimmed;
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
	public StringValidator<T> preprocess(Function<? super String, ? extends @Nullable String> transform) {
		return new StringValidator<>(super.validator, super.mapper, composed(transform, super.transform),
				this.untrimmed);
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
	public StringValidator<T> named(String name) {
		return new StringValidator<>(super.renamed(name), super.mapper, super.transform, this.untrimmed);
	}

	/**
	 * Returns a validator that produces a {@link Supplier} of the mapped value.
	 * @return a validator that produces a supplier of the validated value
	 */
	@Override
	public StringValidator<Supplier<T>> lazy() {
		final Function1<String, Supplier<T>> lazy = s -> () -> super.mapper.apply(s);
		return new StringValidator<>(super.validator, lazy, super.transform, this.untrimmed);
	}

	/**
	 * Returns a new validator that reads a string from an untyped input with the white
	 * space around it, also with a reader that trims text
	 * ({@link am.ik.yavi.core.StringCoercion.Strict#trimming()
	 * StringCoercion.strict().trimming()}). It is for the value whose white space is a
	 * part of it: a password, a text whose first line is indented.
	 *
	 * <pre>{@code
	 * Yavi.schema()
	 *     ._string("name", c -> c.notBlank())
	 *     ._string(Yavi._string("password", c -> c.greaterThanOrEqual(8)).untrimmed())
	 *     .fromMap(Account::new)
	 *     .readingParameters(StringCoercion.strict().trimming());
	 * // name=%20Alice%20&password=%20secret%20%20 is Account[name=Alice, password= secret  ]
	 * }</pre>
	 *
	 * <p>
	 * A blank string is still not given under such a reader, which tells so before the
	 * value is read. With a reader that does not trim, and for a typed value, the
	 * validator is the same as this one. The transforms of the constraints, such as
	 * {@code c.trim()}, are applied as before.
	 *
	 * <p>
	 * The returned validator is not a {@code StringValidator}, so this is said once. It
	 * keeps the white space through {@code map}, {@code preprocess}, {@code named} and
	 * {@code lazy}, which can be written before this method.
	 * @return a new validator that reads a string as it is given
	 * @since 0.18.0
	 */
	public Arguments1Validator<String, T> untrimmed() {
		return new StringValidator<>(super.validator, super.mapper, super.transform, true);
	}

	/**
	 * Returns a validator that accepts an untyped input read with the given reader, as
	 * {@link DefaultArguments1Validator#readingWith(InputReader)} states, and takes a
	 * string as the reader holds it if this validator is {@link #untrimmed()}.
	 * @param reader the reader of the nodes
	 * @param <N> the type of the nodes
	 * @return a value validator for an untyped input
	 * @since 0.18.0
	 */
	@Override
	public <N> ValueValidator<N, T> readingWith(InputReader<N> reader) {
		final ValueValidator<N, T> read = super.readingWith(reader);
		if (!this.untrimmed) {
			return read;
		}
		return new ValueValidator<>() {
			@Override
			public Validated<T> validate(@Nullable N node, Locale locale, ConstraintContext constraintContext) {
				// the value of the node, not the text the reader takes it for
				if (node != null && !reader.isNull(node) && reader.value(node) instanceof String string) {
					return StringValidator.this.validate(string, locale, constraintContext);
				}
				return read.validate(node, locale, constraintContext);
			}

			@Override
			public ValueDescription description() {
				return StringValidator.this.description();
			}

			@Override
			public <M> ValueValidator<M, T> readingWith(InputReader<M> other) {
				return StringValidator.this.readingWith(other);
			}

			@Override
			public ValueValidator<N, T> named(String name) {
				return StringValidator.this.named(name).readingWith(reader);
			}
		};
	}

}
