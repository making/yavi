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
package am.ik.yavi.core;

import am.ik.yavi.builder.ConflictStrategy;
import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh253Test {

	ValidatorBuilder<Car> baseValidatorBuilder = ValidatorBuilder.<Car>of()
		.constraint(Car::getManufacturer, "manufacturer", c -> c.notNull())
		.constraint(Car::getSeatCount, "seatCount", c -> c.greaterThanOrEqual(2));

	@Test
	void overrideConstraint() {
		final Car target = new Car("foo", "AAA", 2);
		final Validator<Car> baseValidator = baseValidatorBuilder.build();
		final ConstraintViolations violations1 = baseValidator.validate(target);
		assertThat(violations1.isValid()).isTrue();

		final Validator<Car> overwrittenValidator = baseValidatorBuilder
			.constraint(Car::getManufacturer, "manufacturer", c -> c.isNull())
			.conflictStrategy(ConflictStrategy.OVERRIDE)
			.build();
		final ConstraintViolations violations2 = overwrittenValidator.validate(target);
		assertThat(violations2.isValid()).isFalse();
		assertThat(violations2).hasSize(1);
		assertThat(violations2.get(0).message()).isEqualTo("\"manufacturer\" must be null");
	}

	public static class Car {

		private final String manufacturer;

		private final String licensePlate;

		private final int seatCount;

		public Car(String manufacturer, String licencePlate, int seatCount) {
			this.manufacturer = manufacturer;
			this.licensePlate = licencePlate;
			this.seatCount = seatCount;
		}

		public String getManufacturer() {
			return manufacturer;
		}

		public String getLicensePlate() {
			return licensePlate;
		}

		public int getSeatCount() {
			return seatCount;
		}

	}

}
