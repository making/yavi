/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

import org.jspecify.annotations.Nullable;

public class Address {

	private final @Nullable Country country;

	private final @Nullable PhoneNumber phoneNumber;

	private final @Nullable String street;

	public Address(@Nullable Country country, @Nullable String street, @Nullable PhoneNumber phoneNumber) {
		this.country = country;
		this.street = street;
		this.phoneNumber = phoneNumber;
	}

	public @Nullable Country country() {
		return this.country;
	}

	public @Nullable PhoneNumber phoneNumber() {
		return this.phoneNumber;
	}

	public @Nullable String street() {
		return this.street;
	}

	@Override
	public String toString() {
		return "Address{" + "country=" + country + ", phoneNumber=" + phoneNumber + ", street='" + street + '\'' + '}';
	}

}
