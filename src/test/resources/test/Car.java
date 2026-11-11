/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

import am.ik.yavi.meta.ConstraintTarget;

public class Car {
	private final String name;
	private final int gas;

	public Car(String name, int gas) {
		this.name = name;
		this.gas = gas;
	}

	@ConstraintTarget(getter = false)
	public String name() {
		return this.name;
	}

	@ConstraintTarget(getter = false)
	public int gas() {
		return this.gas;
	}
}
