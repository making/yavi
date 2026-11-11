/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

import java.util.Map;

public class FormWithMap {

	private final Map<String, Address> addresses;

	public FormWithMap(Map<String, Address> addresses) {
		this.addresses = addresses;
	}

	public Map<String, Address> getAddresses() {
		return addresses;
	}

}
