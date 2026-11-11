/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence;

import java.nio.charset.Charset;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.BYTE_SIZE_FIXED_SIZE;
import static am.ik.yavi.core.ViolationMessage.Default.BYTE_SIZE_GREATER_THAN;
import static am.ik.yavi.core.ViolationMessage.Default.BYTE_SIZE_GREATER_THAN_OR_EQUAL;
import static am.ik.yavi.core.ViolationMessage.Default.BYTE_SIZE_LESS_THAN;
import static am.ik.yavi.core.ViolationMessage.Default.BYTE_SIZE_LESS_THAN_OR_EQUAL;

import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.core.ConstraintPredicate;

/**
 * Constraint that measures a character sequence in bytes of a charset instead of Unicode
 * code points.
 *
 * <p>
 * This constraint is obtained from {@link CharSequenceConstraint#asByteArray()}. The size
 * is the length of the value encoded with the charset as given: no normalization is
 * applied and no variation selector is removed. An unencodable character is replaced
 * according to the charset's default replacement, usually one byte for {@code '?'} in
 * single-byte charsets.
 * </p>
 *
 * <p>
 * A {@code null} value is valid. A violation is reported with the message key
 * {@code byteSize.fixedSize}, {@code byteSize.greaterThan},
 * {@code byteSize.greaterThanOrEqual}, {@code byteSize.lessThan} or
 * {@code byteSize.lessThanOrEqual}, where {@code {1}} is the required byte size and
 * {@code {2}} is the actual byte size.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <E> the type of the character sequence the constraints apply to
 * @since 0.0.10
 */
public class ByteSizeConstraint<T, E extends CharSequence> extends CharSequenceConstraint<T, E> {

	private final Charset charset;

	/**
	 * Creates the constraint. The predicates added to {@code delegate} are copied to the
	 * new constraint.
	 * @param delegate the constraint whose predicates are copied
	 * @param charset the charset used to encode the value
	 */
	public ByteSizeConstraint(CharSequenceConstraint<T, E> delegate, Charset charset) {
		super();
		this.charset = charset;
		this.predicates().addAll(delegate.predicates());
	}

	/**
	 * Returns this constraint.
	 * @return this constraint
	 */
	@Override
	public ByteSizeConstraint<T, E> cast() {
		return this;
	}

	/**
	 * Requires the byte size of the value to be exactly {@code size}.
	 * @param size the required byte size
	 * @return this constraint
	 */
	@Override
	public ByteSizeConstraint<T, E> fixedSize(int size) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) == size, this::size),
					BYTE_SIZE_FIXED_SIZE, () -> new Object[] { size }, VALID));
		return this;
	}

	/**
	 * Requires the byte size of the value to be greater than {@code min}.
	 * @param min the exclusive lower bound of the byte size
	 * @return this constraint
	 */
	@Override
	public ByteSizeConstraint<T, E> greaterThan(int min) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) > min, this::size),
					BYTE_SIZE_GREATER_THAN, () -> new Object[] { min }, VALID));
		return this;
	}

	/**
	 * Requires the byte size of the value to be greater than or equal to {@code min}.
	 * @param min the inclusive lower bound of the byte size
	 * @return this constraint
	 */
	@Override
	public ByteSizeConstraint<T, E> greaterThanOrEqual(int min) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) >= min, this::size),
					BYTE_SIZE_GREATER_THAN_OR_EQUAL, () -> new Object[] { min }, VALID));
		return this;
	}

	/**
	 * Requires the byte size of the value to be less than {@code max}.
	 * @param max the exclusive upper bound of the byte size
	 * @return this constraint
	 */
	@Override
	public ByteSizeConstraint<T, E> lessThan(int max) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) < max, this::size),
					BYTE_SIZE_LESS_THAN, () -> new Object[] { max }, VALID));
		return this;
	}

	/**
	 * Requires the byte size of the value to be less than or equal to {@code max}.
	 * @param max the inclusive upper bound of the byte size
	 * @return this constraint
	 */
	@Override
	public ByteSizeConstraint<T, E> lessThanOrEqual(int max) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) <= max, this::size),
					BYTE_SIZE_LESS_THAN_OR_EQUAL, () -> new Object[] { max }, VALID));
		return this;
	}

	private int size(E x) {
		return x.toString().getBytes(charset).length;
	}

}
