/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * A {@link ValueValidator} that refers to a validator supplied on first use, so that it
 * can stand for a validator that is not built yet. Everything that would need the
 * referred validator before it is used stays lazy: the description is a
 * {@link ValueDescription.Reference} and the validator that reads with another reader is
 * again a lazy validator.
 *
 * @param <T> the type of the object to be validated
 * @param <X> the type of the validated and transformed result
 */
final class LazyValueValidator<T, X extends @Nullable Object> implements ValueValidator<T, X> {

	private final Supplier<? extends @Nullable ValueValidator<T, X>> supplier;

	private final ValueDescription.Reference reference;

	private volatile @Nullable ValueValidator<T, X> resolved;

	/**
	 * The validator that reads with the reader it was last asked for. A validator that
	 * refers to itself asks for the same reader again when it is resolved, and is given
	 * the same validator, which closes the cycle.
	 */
	private volatile @Nullable Reading<?, X> reading;

	private record Reading<N, X> (InputReader<N> reader, ValueValidator<N, X> validator) {
	}

	LazyValueValidator(String name, Supplier<? extends @Nullable ValueValidator<T, X>> supplier) {
		this.supplier = supplier;
		this.reference = new ValueDescription.Reference(name, () -> this.resolve().description());
	}

	private LazyValueValidator(ValueDescription.Reference reference,
			Supplier<? extends @Nullable ValueValidator<T, X>> supplier) {
		this.supplier = supplier;
		this.reference = reference;
	}

	private ValueValidator<T, X> resolve() {
		ValueValidator<T, X> validator = this.resolved;
		if (validator == null) {
			validator = this.supplier.get();
			if (validator == null) {
				throw new IllegalStateException(
						"The validator that a lazy validator refers to is not built yet. Use the lazy validator only after the supplier can return the validator.");
			}
			this.resolved = validator;
		}
		return validator;
	}

	@Override
	public Validated<X> validate(@Nullable T t, Locale locale, ConstraintContext constraintContext) {
		return this.resolve().validate(t, locale, constraintContext);
	}

	@Override
	public ValueDescription description() {
		return this.reference;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
		final Reading<?, X> last = this.reading;
		if (last != null && last.reader() == reader) {
			return (ValueValidator<N, X>) last.validator();
		}
		final ValueValidator<N, X> validator = new LazyValueValidator<>(this.reference,
				() -> this.resolve().readingWith(reader));
		this.reading = new Reading<>(reader, validator);
		return validator;
	}

	@Override
	public ValueValidator<T, X> preprocess(Function<? super T, ? extends @Nullable T> transform) {
		return new LazyValueValidator<>(this.reference, () -> this.resolve().preprocess(transform));
	}

}
