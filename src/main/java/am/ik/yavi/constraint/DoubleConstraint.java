/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

public class DoubleConstraint<T>
		extends NumericConstraintBase<T, Double, DoubleConstraint<T>> {
	@Override
	public DoubleConstraint<T> cast() {
		return this;
	}

	@Override
	protected Predicate<Double> isGreaterThan(Double min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Double> isGreaterThanOrEqual(Double min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Double> isLessThan(Double max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Double> isLessThanOrEqual(Double max) {
		return x -> x <= max;
	}

	@Override
	protected Double zeroValue() {
		return 0.0;
	}
}
