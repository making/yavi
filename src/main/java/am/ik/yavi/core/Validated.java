package am.ik.yavi.core;

import java.util.List;

import am.ik.yavi.fn.Validation;


/**
 * a specialized {@code Validation} type that regards {@code List<E>} as {@code ConstraintViolations}
 * @param <T> value type in the case of success
 * @since 0.6.0
 */
public class Validated<T> implements Validation<ConstraintViolation, T> {
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
	public <U, V extends Validation<ConstraintViolation, U>> V yieldSuccess(U value) {
		return (V) Validated.of(Validation.success(value));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U, V extends Validation<ConstraintViolation, U>> V yieldFailure(
			List<ConstraintViolation> errors) {
		return (V) Validated.of(Validation.failure(errors));
	}
}
