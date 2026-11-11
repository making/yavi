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

public class ByteSizeConstraint<T, E extends CharSequence> extends CharSequenceConstraint<T, E> {

	private final Charset charset;

	public ByteSizeConstraint(CharSequenceConstraint<T, E> delegate, Charset charset) {
		super();
		this.charset = charset;
		this.predicates().addAll(delegate.predicates());
	}

	@Override
	public ByteSizeConstraint<T, E> cast() {
		return this;
	}

	@Override
	public ByteSizeConstraint<T, E> fixedSize(int size) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) == size, this::size),
					BYTE_SIZE_FIXED_SIZE, () -> new Object[] { size }, VALID));
		return this;
	}

	@Override
	public ByteSizeConstraint<T, E> greaterThan(int min) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) > min, this::size),
					BYTE_SIZE_GREATER_THAN, () -> new Object[] { min }, VALID));
		return this;
	}

	@Override
	public ByteSizeConstraint<T, E> greaterThanOrEqual(int min) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) >= min, this::size),
					BYTE_SIZE_GREATER_THAN_OR_EQUAL, () -> new Object[] { min }, VALID));
		return this;
	}

	@Override
	public ByteSizeConstraint<T, E> lessThan(int max) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) < max, this::size),
					BYTE_SIZE_LESS_THAN, () -> new Object[] { max }, VALID));
		return this;
	}

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
