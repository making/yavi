/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

import am.ik.yavi.meta.ConstraintTarget;

public class CarField {
	final String name;
	final int gas;

	public CarField(@ConstraintTarget(field = true) String name, @ConstraintTarget(field = true) int gas) {
		this.name = name;
		this.gas = gas;
	}
}
