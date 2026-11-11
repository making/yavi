/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

import am.ik.yavi.meta.ConstraintTarget;

public class CarBean {
	private String name;
	private int gas;

	public CarBean(String name, int gas) {
		this.name = name;
		this.gas = gas;
	}

	@ConstraintTarget
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ConstraintTarget
	public int getGas() {
		return this.gas;
	}

	public void setGas(int gas) {
		this.gas = gas;
	}
}
