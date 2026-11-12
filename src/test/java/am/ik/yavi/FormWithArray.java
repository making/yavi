/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

import org.jspecify.annotations.Nullable;

public class FormWithArray {

	private final Address @Nullable [] addresses;

	public FormWithArray(Address @Nullable [] addresses) {
		this.addresses = addresses;
	}

	public Address @Nullable [] getAddresses() {
		return addresses;
	}

}
