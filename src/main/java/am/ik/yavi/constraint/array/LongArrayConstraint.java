/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.array;

import java.util.Arrays;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.ARRAY_CONTAINS;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

public class LongArrayConstraint<T> extends ContainerConstraintBase<T, long[], LongArrayConstraint<T>> {

	@Override
	public LongArrayConstraint<T> cast() {
		return this;
	}

	public LongArrayConstraint<T> contains(long v) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> Arrays.stream(x).anyMatch(e -> e == v), ARRAY_CONTAINS,
					() -> new Object[] { v }, VALID));
		return this;
	}

	@Override
	protected ToIntFunction<long[]> size() {
		return x -> x.length;
	}

}
