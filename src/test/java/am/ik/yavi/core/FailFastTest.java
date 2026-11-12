/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import am.ik.yavi.builder.ValidatorBuilder;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class FailFastTest {

	@ParameterizedTest
	@MethodSource("carValidator")
	void failFast_valid(Validator<Car> validator) {
		final ConstraintViolations violations = validator.validate(new Car("xyz", true));
		assertThat(violations.isValid()).isTrue();
	}

	@ParameterizedTest
	@MethodSource("carValidator")
	void failFast_invalid(Validator<Car> validator) {
		final ConstraintViolations violations = validator.validate(new Car(null, false));
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
	}

	@ParameterizedTest
	@MethodSource("carOwnerValidator")
	void failFastNestedAndCollection_valid(Validator<CarOwner> validator) {
		final CarOwner carOwner = new CarOwner(new OwnerId("abc"), "John Doe",
				Arrays.asList(new Car("xyz1", true), new Car("xyz2", true)));
		final ConstraintViolations violations = validator.validate(carOwner);
		assertThat(violations.isValid()).isTrue();
	}

	@ParameterizedTest
	@MethodSource("carOwnerValidator")
	void failFastNested_invalid(Validator<CarOwner> validator) {
		final CarOwner carOwner = new CarOwner(new OwnerId(null), "John Doe",
				Arrays.asList(new Car("xyz1", true), new Car("xyz2", true)));
		final ConstraintViolations violations = validator.validate(carOwner);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
	}

	@ParameterizedTest
	@MethodSource("carOwnerValidator")
	void failFastCollection_invalid(Validator<CarOwner> validator) {
		final CarOwner carOwner = new CarOwner(new OwnerId("abc"), "John Doe",
				Arrays.asList(new Car(null, false), new Car(null, false)));
		final ConstraintViolations violations = validator.validate(carOwner);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
	}

	@ParameterizedTest
	@MethodSource("carOwnerValidator")
	void failFastAll_invalid(Validator<CarOwner> validator) {
		final CarOwner carOwner = new CarOwner(new OwnerId(null), null,
				Arrays.asList(new Car(null, false), new Car(null, false)));
		final ConstraintViolations violations = validator.validate(carOwner);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
	}

	static Stream<Validator<Car>> carValidator() {
		return Stream.of(
				ValidatorBuilder.<Car>of()
					.constraint(Car::getManufacturer, "manufacturer", c -> c.notNull().notBlank())
					.constraint(Car::isRegistered, "isRegistered", c -> c.notNull().isTrue())
					.failFast(true)
					.build(),
				ValidatorBuilder.<Car>of()
					.constraint(Car::getManufacturer, "manufacturer", c -> c.notNull().notBlank())
					.constraint(Car::isRegistered, "isRegistered", c -> c.notNull().isTrue())
					.build()
					.failFast(true));
	}

	static Stream<Validator<CarOwner>> carOwnerValidator() {
		return Stream.of(
				ValidatorBuilder.<CarOwner>of()
					.failFast(true)
					.nest(CarOwner::getOwnerId, "ownerId",
							b -> b.constraint(OwnerId::getValue, "value", c -> c.notNull().notBlank()))
					.constraint(CarOwner::getName, "name", c -> c.notNull().notBlank())
					.forEach(CarOwner::getCars, "cars",
							b -> b.constraint(Car::getManufacturer, "manufacturer", c -> c.notNull().notBlank())
								.constraint(Car::isRegistered, "isRegistered", c -> c.notNull().isTrue()))
					.build(),
				ValidatorBuilder.<CarOwner>of()
					.failFast(true)
					.nest(CarOwner::getOwnerId, "ownerId",
							b -> b.constraint(OwnerId::getValue, "value", c -> c.notNull().notBlank()))
					.constraintOnCondition((carOwner, group) -> true,
							b -> b.constraint(CarOwner::getName, "name", c -> c.notNull().notBlank()))
					.forEach(CarOwner::getCars, "cars",
							b -> b
								.constraintOnCondition((car, group) -> true,
										bb -> bb.constraint(Car::getManufacturer, "manufacturer",
												c -> c.notNull().notBlank()))
								.constraintOnCondition((car, group) -> true,
										bb -> bb.constraint(Car::isRegistered, "isRegistered",
												c -> c.notNull().isTrue())))
					.build());
	}

	public static class Car {

		private final @Nullable String manufacturer;

		private final boolean isRegistered;

		public Car(@Nullable String manufacturer, boolean isRegistered) {
			this.manufacturer = manufacturer;
			this.isRegistered = isRegistered;
		}

		@Nullable public String getManufacturer() {
			return manufacturer;
		}

		public boolean isRegistered() {
			return isRegistered;
		}

	}

	public static class CarOwner {

		private final OwnerId ownerId;

		private final @Nullable String name;

		private final List<Car> cars;

		public CarOwner(OwnerId ownerId, @Nullable String name, List<Car> cars) {
			this.ownerId = ownerId;
			this.name = name;
			this.cars = Collections.unmodifiableList(cars);
		}

		public OwnerId getOwnerId() {
			return ownerId;
		}

		@Nullable public String getName() {
			return name;
		}

		public List<Car> getCars() {
			return cars;
		}

	}

	public static class OwnerId {

		private final @Nullable String value;

		public OwnerId(@Nullable String value) {
			this.value = value;
		}

		@Nullable public String getValue() {
			return value;
		}

	}

}
