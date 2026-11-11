/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

public class FormWithArray {

	private final Address[] addresses;

	public FormWithArray(Address[] addresses) {
		this.addresses = addresses;
	}

	public Address[] getAddresses() {
		return addresses;
	}

}
