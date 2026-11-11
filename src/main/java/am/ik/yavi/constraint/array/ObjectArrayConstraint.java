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

public class ObjectArrayConstraint<T, E> extends ContainerConstraintBase<T, E[], ObjectArrayConstraint<T, E>> {

	@Override
	public ObjectArrayConstraint<T, E> cast() {
		return this;
	}

	public ObjectArrayConstraint<T, E> contains(E s) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> Arrays.asList(x).contains(s), ARRAY_CONTAINS, () -> new Object[] { s },
					VALID));
		return this;
	}

	@Override
	protected ToIntFunction<E[]> size() {
		return x -> x.length;
	}

}
