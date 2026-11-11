/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence;

import java.text.Normalizer;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_FIXED_SIZE;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_GREATER_THAN;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_GREATER_THAN_OR_EQUAL;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_LESS_THAN;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_LESS_THAN_OR_EQUAL;

import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.constraint.charsequence.variant.VariantOptions;
import am.ik.yavi.core.ConstraintPredicate;

public class EmojiConstraint<T, E extends CharSequence> extends CharSequenceConstraint<T, E> {

	public EmojiConstraint(CharSequenceConstraint<T, E> delegate, Normalizer.Form normalizerForm,
			VariantOptions variantOptions) {
		super(normalizerForm, variantOptions);
		this.predicates().addAll(delegate.predicates());
	}

	@Override
	public EmojiConstraint<T, E> fixedSize(int size) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) == size, this::size),
					CONTAINER_FIXED_SIZE, () -> new Object[] { size }, VALID));
		return this;
	}

	@Override
	public EmojiConstraint<T, E> greaterThan(int min) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) > min, this::size),
					CONTAINER_GREATER_THAN, () -> new Object[] { min }, VALID));
		return this;
	}

	@Override
	public EmojiConstraint<T, E> greaterThanOrEqual(int min) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) >= min, this::size),
					CONTAINER_GREATER_THAN_OR_EQUAL, () -> new Object[] { min }, VALID));
		return this;
	}

	@Override
	public EmojiConstraint<T, E> lessThan(int max) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) < max, this::size),
					CONTAINER_LESS_THAN, () -> new Object[] { max }, VALID));
		return this;
	}

	@Override
	public EmojiConstraint<T, E> lessThanOrEqual(int max) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) <= max, this::size),
					CONTAINER_LESS_THAN_OR_EQUAL, () -> new Object[] { max }, VALID));
		return this;
	}

	private int size(E x) {
		return Emoji.bestEffortCount(this.normalize(x.toString()));
	}

}
