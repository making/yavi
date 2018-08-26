package am.ik.yavi.constraint.charsequence;

import java.nio.charset.Charset;

import static am.ik.yavi.constraint.ViolationMessage.Default.*;
import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.core.ConstraintPredicate;

public class ByteSizeConstraint<T, E extends CharSequence>
		extends CharSequenceConstraint<T, E> {
	private final Charset charset;

	public ByteSizeConstraint(CharSequenceConstraint<T, E> delete, Charset charset) {
		super();
		this.charset = charset;
		this.predicates().addAll(delete.predicates());
	}

	@Override
	public ByteSizeConstraint<T, E> cast() {
		return this;
	}

	private int size(E x) {
		return x.toString().getBytes(charset).length;
	}

	public ByteSizeConstraint<T, E> lessThan(int max) {
		this.predicates().add(ConstraintPredicate.of(x -> size(x) < max,
				BYTE_SIZE_LESS_THAN, () -> new Object[] { max }, NULL_IS_VALID));
		return this;
	}

	public ByteSizeConstraint<T, E> lessThanOrEqual(int max) {
		this.predicates().add(ConstraintPredicate.of(x -> size(x) <= max,
				BYTE_SIZE_LESS_THAN_OR_EQUAL, () -> new Object[] { max }, NULL_IS_VALID));
		return this;
	}

	public ByteSizeConstraint<T, E> greaterThan(int min) {
		this.predicates().add(ConstraintPredicate.of(x -> size(x) > min,
				BYTE_SIZE_GREATER_THAN, () -> new Object[] { min }, NULL_IS_VALID));
		return this;
	}

	public ByteSizeConstraint<T, E> greaterThanOrEqual(int min) {
		this.predicates()
				.add(ConstraintPredicate.of(x -> size(x) >= min,
						BYTE_SIZE_GREATER_THAN_OR_EQUAL, () -> new Object[] { min },
						NULL_IS_VALID));
		return this;
	}

	public ByteSizeConstraint<T, E> fixedSize(int size) {
		this.predicates().add(ConstraintPredicate.of(x -> size(x) == size,
				BYTE_SIZE_FIXED_SIZE, () -> new Object[] { size }, NULL_IS_VALID));
		return this;
	}
}
