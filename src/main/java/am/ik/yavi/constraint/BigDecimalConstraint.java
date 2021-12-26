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
package am.ik.yavi.constraint;

import java.math.BigDecimal;
import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

public class BigDecimalConstraint<T>
		extends NumericConstraintBase<T, BigDecimal, BigDecimalConstraint<T>> {
	@Override
	public BigDecimalConstraint<T> cast() {
		return this;
	}

	@Override
	protected Predicate<BigDecimal> isGreaterThan(BigDecimal min) {
		return x -> x.compareTo(min) > 0;
	}

	@Override
	protected Predicate<BigDecimal> isGreaterThanOrEqual(BigDecimal min) {
		return x -> x.compareTo(min) >= 0;
	}

	@Override
	protected Predicate<BigDecimal> isLessThan(BigDecimal max) {
		return x -> x.compareTo(max) < 0;
	}

	@Override
	protected Predicate<BigDecimal> isLessThanOrEqual(BigDecimal max) {
		return x -> x.compareTo(max) <= 0;
	}

	@Override
	protected BigDecimal zeroValue() {
		return BigDecimal.ZERO;
	}
}
