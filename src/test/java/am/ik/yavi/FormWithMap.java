/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

import org.jspecify.annotations.Nullable;
import java.util.Map;

public class FormWithMap {

	private final @Nullable Map<String, Address> addresses;

	public FormWithMap(@Nullable Map<String, Address> addresses) {
		this.addresses = addresses;
	}

	public @Nullable Map<String, Address> getAddresses() {
		return addresses;
	}

}
