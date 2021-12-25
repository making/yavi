/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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

import java.nio.charset.Charset;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.BYTE_SIZE_FIXED_SIZE;
import static am.ik.yavi.core.ViolationMessage.Default.BYTE_SIZE_GREATER_THAN;
import static am.ik.yavi.core.ViolationMessage.Default.BYTE_SIZE_GREATER_THAN_OR_EQUAL;
import static am.ik.yavi.core.ViolationMessage.Default.BYTE_SIZE_LESS_THAN;
import static am.ik.yavi.core.ViolationMessage.Default.BYTE_SIZE_LESS_THAN_OR_EQUAL;

import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.core.ConstraintPredicate;

public class ByteSizeConstraint<T, E extends CharSequence>
		extends CharSequenceConstraint<T, E> {
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
				.add(ConstraintPredicate.withViolatedValue(
						this.checkSizePredicate(x -> size(x) == size, this::size),
						BYTE_SIZE_FIXED_SIZE, () -> new Object[] { size }, VALID));
		return this;
	}

	@Override
	public ByteSizeConstraint<T, E> greaterThan(int min) {
		this.predicates()
				.add(ConstraintPredicate.withViolatedValue(
						this.checkSizePredicate(x -> size(x) > min, this::size),
						BYTE_SIZE_GREATER_THAN, () -> new Object[] { min }, VALID));
		return this;
	}

	@Override
	public ByteSizeConstraint<T, E> greaterThanOrEqual(int min) {
		this.predicates().add(ConstraintPredicate.withViolatedValue(
				this.checkSizePredicate(x -> size(x) >= min, this::size),
				BYTE_SIZE_GREATER_THAN_OR_EQUAL, () -> new Object[] { min }, VALID));
		return this;
	}

	@Override
	public ByteSizeConstraint<T, E> lessThan(int max) {
		this.predicates()
				.add(ConstraintPredicate.withViolatedValue(
						this.checkSizePredicate(x -> size(x) < max, this::size),
						BYTE_SIZE_LESS_THAN, () -> new Object[] { max }, VALID));
		return this;
	}

	@Override
	public ByteSizeConstraint<T, E> lessThanOrEqual(int max) {
		this.predicates()
				.add(ConstraintPredicate.withViolatedValue(
						this.checkSizePredicate(x -> size(x) <= max, this::size),
						BYTE_SIZE_LESS_THAN_OR_EQUAL, () -> new Object[] { max }, VALID));
		return this;
	}

	private int size(E x) {
		return x.toString().getBytes(charset).length;
	}
}
