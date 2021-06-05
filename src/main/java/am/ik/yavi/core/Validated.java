package am.ik.yavi.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import am.ik.yavi.fn.Validation;
import am.ik.yavi.fn.Validations;

/**
 * a specialized {@code Validation} type that regards {@code List<E>} as
 * {@code ConstraintViolations}
 * @param <T> value type in the case of success
 * @since 0.6.0
 */
public class Validated<T> extends Validation<ConstraintViolation, T> {
	private final Validation<ConstraintViolation, T> delegate;

	public static <T> Validated<T> of(Validation<ConstraintViolation, T> delegate) {
		return new Validated<>(delegate);
	}

	Validated(Validation<ConstraintViolation, T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean isValid() {
		return this.delegate.isValid();
	}

	@Override
	public T value() {
		return this.delegate.value();
	}

	@Override
	public ConstraintViolations errors() {
		return ConstraintViolations.of(this.delegate.errors());
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <U, V extends Validation<ConstraintViolation, U>> V yieldSuccess(U value) {
		return (V) Validated.of(Validation.success(value));
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <U, V extends Validation<ConstraintViolation, U>> V yieldFailure(
			List<ConstraintViolation> errors) {
		return (V) Validated.of(Validation.failure(errors));
	}

	public static <T> Validated<List<T>> sequence(
			Iterable<Validated<? extends T>> values) {
		return of(Validations.sequence(values));
	}

	public static <T, U> Validated<List<U>> traverse(Iterable<T> values,
			Function<? super T, Validated<? extends U>> mapper) {
		return of(Validations.traverse(values, mapper));
	}

}
