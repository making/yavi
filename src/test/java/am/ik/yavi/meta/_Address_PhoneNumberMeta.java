/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

public class _Address_PhoneNumberMeta {

	public static final StringConstraintMeta<am.ik.yavi.meta.Address.PhoneNumber> VALUE = new StringConstraintMeta<am.ik.yavi.meta.Address.PhoneNumber>() {

		@Override
		public String name() {
			return "value";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.meta.Address.PhoneNumber, String> toValue() {
			return am.ik.yavi.meta.Address.PhoneNumber::value;
		}
	};

}
