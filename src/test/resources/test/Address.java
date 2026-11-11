/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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