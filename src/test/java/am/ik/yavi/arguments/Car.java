/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import am.ik.yavi.validator.Yavi;

public class Car {

	private final String manufacturer;

	private final String licensePlate;

	private final int seatCount;

	private static final Arguments3Validator<String, String, Integer, Car> validator = Yavi.arguments() //
		._string("manufacturer", c -> c.notNull()) //
		._string("licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14)) //
		._integer("seatCount", c -> c.greaterThanOrEqual(2)) //
		.apply(Car::new);

	public Car(String manufacturer, String licensePlate, int seatCount) {
		validator.lazy().validated(manufacturer, licensePlate, seatCount);
		this.manufacturer = manufacturer;
		this.licensePlate = licensePlate;
		this.seatCount = seatCount;
	}

	@Override
	public String toString() {
		return "Car{" + "manufacturer='" + manufacturer + '\'' + ", licensePlate='" + licensePlate + '\''
				+ ", seatCount=" + seatCount + '}';
	}

}
