/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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
