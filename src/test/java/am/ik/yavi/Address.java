/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

public class Address {

	private final Country country;

	private final PhoneNumber phoneNumber;

	private final String street;

	public Address(Country country, String street, PhoneNumber phoneNumber) {
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

	@Override
	public String toString() {
		return "Address{" + "country=" + country + ", phoneNumber=" + phoneNumber + ", street='" + street + '\'' + '}';
	}

}
