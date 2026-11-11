/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.array;

import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.ARRAY_CONTAINS;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

public class ByteArrayConstraint<T> extends ContainerConstraintBase<T, byte[], ByteArrayConstraint<T>> {

	@Override
	public ByteArrayConstraint<T> cast() {
		return this;
	}

	public ByteArrayConstraint<T> contains(byte v) {
		this.predicates().add(ConstraintPredicate.of(x -> {
			for (byte e : x) {
				if (e == v) {
					return true;
				}
			}
			return false;
		}, ARRAY_CONTAINS, () -> new Object[] { v }, VALID));
		return this;
	}

	@Override
	protected ToIntFunction<byte[]> size() {
		return x -> x.length;
	}

}
