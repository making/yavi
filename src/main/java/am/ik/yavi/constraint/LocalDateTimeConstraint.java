/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
import java.time.LocalDateTime;

import am.ik.yavi.constraint.base.ChronoLocalDateTimeConstraintBase;

/**
 * This is the actual class for constraints on LocalDateTime.
 *
 * @author Diego Krupitza
 * @since 0.10.0
 */
public class LocalDateTimeConstraint<T>
		extends ChronoLocalDateTimeConstraintBase<T, LocalDateTime, LocalDateTimeConstraint<T>> {

	@Override
	public LocalDateTimeConstraint<T> cast() {
		return this;
	}

	@Override
	protected LocalDateTime getNow(Clock clock) {
		return LocalDateTime.now(clock);
	}

}
