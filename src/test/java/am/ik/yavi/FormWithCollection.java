/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

import org.jspecify.annotations.Nullable;
import java.util.List;

public class FormWithCollection {

	private final @Nullable List<Address> addresses;

	public FormWithCollection(@Nullable List<Address> addresses) {
		this.addresses = addresses;
	}

	public @Nullable List<Address> getAddresses() {
		return addresses;
	}

}
