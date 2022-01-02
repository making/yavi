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

public class Address {

	private final Country country;

	private final PhoneNumber phoneNumber;

	private final String street;

	public Address(@ConstraintTarget(getter = false) Country country,
			@ConstraintTarget(getter = false) String street,
			@ConstraintTarget(getter = false) PhoneNumber phoneNumber) {
		this.country = country;
		this.street = street;
		this.phoneNumber = phoneNumber;
	}

	public Country country() {
		return this.country;
	}

	public PhoneNumber phoneNumber() {
		return this.phoneNumber;
	}

	public String street() {
		return this.street;
	}

	public static class Country {

		private final String name;

		public Country(@ConstraintTarget(getter = false) String name) {
			this.name = name;
		}

		public String name() {
			return this.name;
		}
	}

	public static class PhoneNumber {

		private final String value;

		public PhoneNumber(@ConstraintTarget(getter = false) String value) {
			this.value = value;
		}

		public String value() {
			return this.value;
		}
	}
}