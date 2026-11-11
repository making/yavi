/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

import am.ik.yavi.meta.ConstraintArguments;

public class Car2 {
	private final String name;
	private final int gas;

	@ConstraintArguments
	public Car2(String name, int gas) {
		this.name = name;
		this.gas = gas;
	}

	public String name() {
		return this.name;
	}

	public int gas() {
		return this.gas;
	}
}
