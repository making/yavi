/*
 * Copyright (C) 2018-2019 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

public class EmojiConstraint<T, E extends CharSequence>
		extends CharSequenceConstraint<T, E> {

	public EmojiConstraint(CharSequenceConstraint<T, E> delegate,
			Normalizer.Form normalizerForm, VariantOptions variantOptions) {
		super(normalizerForm, variantOptions);
		this.predicates().addAll(delegate.predicates());
	}

	public EmojiConstraint<T, E> fixedSize(int size) {
		this.predicates()
				.add(ConstraintPredicate.withViolatedValue(
						this.checkSizePredicate(x -> size(x) == size, this::size),
						CONTAINER_FIXED_SIZE, () -> new Object[] { size }, VALID));
		return this;
	}

	public EmojiConstraint<T, E> greaterThan(int min) {
		this.predicates()
				.add(ConstraintPredicate.withViolatedValue(
						this.checkSizePredicate(x -> size(x) > min, this::size),
						CONTAINER_GREATER_THAN, () -> new Object[] { min }, VALID));
		return this;
	}

	public EmojiConstraint<T, E> greaterThanOrEqual(int min) {
		this.predicates().add(ConstraintPredicate.withViolatedValue(
				this.checkSizePredicate(x -> size(x) >= min, this::size),
				CONTAINER_GREATER_THAN_OR_EQUAL, () -> new Object[] { min }, VALID));
		return this;
	}

	public EmojiConstraint<T, E> lessThan(int max) {
		this.predicates()
				.add(ConstraintPredicate.withViolatedValue(
						this.checkSizePredicate(x -> size(x) < max, this::size),
						CONTAINER_LESS_THAN, () -> new Object[] { max }, VALID));
		return this;
	}

	public EmojiConstraint<T, E> lessThanOrEqual(int max) {
		this.predicates()
				.add(ConstraintPredicate.withViolatedValue(
						this.checkSizePredicate(x -> size(x) <= max, this::size),
						CONTAINER_LESS_THAN_OR_EQUAL, () -> new Object[] { max }, VALID));
		return this;
	}

	private int size(E x) {
		return Emoji.bestEffortCount(this.normalize(x.toString()));
	}
}
