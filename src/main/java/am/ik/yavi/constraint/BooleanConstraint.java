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
package am.ik.yavi.constraint;

import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

public class BooleanConstraint<T>
		extends ConstraintBase<T, Boolean, BooleanConstraint<T>> {

	public BooleanConstraint<T> isTrue() {
		this.predicates().add(new ConstraintPredicate<>(x -> x, "boolean.isTrue",
				"\"{0}\" must be true", () -> new Object[] {}, NULL_IS_VALID));
		return this;
	}

	public BooleanConstraint<T> isFalse() {
		this.predicates().add(new ConstraintPredicate<>(x -> !x, "boolean.isFalse",
				"\"{0}\" must be false", () -> new Object[] {}, NULL_IS_VALID));
		return this;
	}

	@Override
	public BooleanConstraint<T> cast() {
		return this;
	}
}
