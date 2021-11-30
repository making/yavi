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

import java.time.Year;
import java.util.function.Predicate;

import am.ik.yavi.constraint.base.TemporalConstraintBase;

public class YearConstraint<T>
		extends TemporalConstraintBase<T, Year, YearConstraint<T>> {
	@Override
	public YearConstraint<T> cast() {
		return this;
	}

	@Override
	protected Predicate<Year> isAfter(Year min) {
		return x -> x.isAfter(min);
	}

	@Override
	protected Predicate<Year> isOnOrAfter(Year min) {
		return x -> x.getValue() >= min.getValue();
	}

	@Override
	protected Predicate<Year> isBefore(Year max) {
		return x -> x.isBefore(max);
	}

	@Override
	protected Predicate<Year> isOnOrBefore(Year max) {
		return x -> x.getValue() <= max.getValue();
	}
}
