/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
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
import java.time.Year;

import am.ik.yavi.constraint.base.TemporalConstraintBase;

/**
 * This is the actual class for constraints on Year.
 *
 * @since 0.11.0
 */
public class YearConstraint<T> extends TemporalConstraintBase<T, Year, YearConstraint<T>> {

	@Override
	protected boolean isAfter(Year a, Year b) {
		return a.isAfter(b);
	}

	@Override
	protected boolean isBefore(Year a, Year b) {
		return a.isBefore(b);
	}

	@Override
	protected Year getNow(Clock clock) {
		return Year.now(clock);
	}

	@Override
	public YearConstraint<T> cast() {
		return this;
	}

}
