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

import java.time.Clock;
import java.time.YearMonth;

import am.ik.yavi.constraint.base.TemporalConstraintBase;

/**
 * This is the actual class for constraints on YearMonth.
 *
 * @since 0.11.0
 */
public class YearMonthConstraint<T>
		extends TemporalConstraintBase<T, YearMonth, YearMonthConstraint<T>> {
	@Override
	protected boolean isAfter(YearMonth a, YearMonth b) {
		return a.isAfter(b);
	}

	@Override
	protected boolean isBefore(YearMonth a, YearMonth b) {
		return a.isBefore(b);
	}

	@Override
	protected YearMonth getNow(Clock clock) {
		return YearMonth.now(clock);
	}

	@Override
	public YearMonthConstraint<T> cast() {
		return this;
	}
}
