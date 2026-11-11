/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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
