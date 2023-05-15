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
package am.ik.yavi.arguments;

import am.ik.yavi.builder.IntegerValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;

public class Car {
	private final String manufacturer;

	private final String licensePlate;

	private final int seatCount;

	private static final StringValidator<String> manufacturerValidator = StringValidatorBuilder
			.of("manufacturer", c -> c.notNull()).build();

	private static final StringValidator<String> licensePlateValidator = StringValidatorBuilder
			.of("licensePlate",
					c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
			.build();

	private static final IntegerValidator<Integer> seatCountValidator = IntegerValidatorBuilder
			.of("seatCount", c -> c.greaterThanOrEqual(2)).build();

	private static final Arguments3Validator<String, String, Integer, Car> carValidator = ArgumentsValidators
			.split(manufacturerValidator, licensePlateValidator, seatCountValidator)
			.apply(Car::new);

	public Car(String manufacturer, String licensePlate, int seatCount) {
		carValidator.lazy().validated(manufacturer, licensePlate, seatCount);
		this.manufacturer = manufacturer;
		this.licensePlate = licensePlate;
		this.seatCount = seatCount;
	}

	@Override
	public String toString() {
		return "Car{" + "manufacturer='" + manufacturer + '\'' + ", licensePlate='"
				+ licensePlate + '\'' + ", seatCount=" + seatCount + '}';
	}
}
