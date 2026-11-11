/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

import java.util.List;

public class FormWithCollection {

	private final List<Address> addresses;

	public FormWithCollection(List<Address> addresses) {
		this.addresses = addresses;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

}
