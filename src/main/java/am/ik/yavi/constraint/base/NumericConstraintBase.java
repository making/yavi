/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.constraint.base;

import java.util.function.Predicate;

import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.Constraint;
import am.ik.yavi.core.ConstraintPredicate;

public abstract class NumericConstraintBase<T, V, C extends Constraint<T, V, C>>
		extends ConstraintBase<T, V, C> {

	protected abstract Predicate<V> isGreaterThan(V min);

	protected abstract Predicate<V> isGreaterThanOrEqual(V min);

	protected abstract Predicate<V> isLessThan(V max);

	protected abstract Predicate<V> isLessThanOrEqual(V max);

	public C greaterThan(V min) {
		this.predicates()
				.add(new ConstraintPredicate<>(this.isGreaterThan(min),
						"number.greaterThan", "\"{0}\" must not be greater than {1}",
						() -> new Object[] { min }, NULL_IS_VALID));
		return cast();
	}

	public C greaterThanOrEqual(V min) {
		this.predicates()
				.add(new ConstraintPredicate<>(this.isGreaterThanOrEqual(min),
						"number.greaterThanOrEqual",
						"\"{0}\" must not be greater than or equal to {1}",
						() -> new Object[] { min }, NULL_IS_VALID));
		return cast();
	}

	public C lessThan(V max) {
		this.predicates()
				.add(new ConstraintPredicate<>(this.isLessThan(max), "number.lessThan",
						"\"{0}\" must not be less than {1}", () -> new Object[] { max },
						NULL_IS_VALID));
		return cast();
	}

	public C lessThanOrEqual(V max) {
		this.predicates().add(new ConstraintPredicate<>(this.isLessThanOrEqual(max),
				"number.lessThanOrEqual", "\"{0}\" must not be less than or equal to {1}",
				() -> new Object[] { max }, NULL_IS_VALID));
		return cast();
	}
}
