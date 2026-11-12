/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

public class _Address_PhoneNumberMeta {

	public static final am.ik.yavi.meta.StringConstraintMeta<test.Address.PhoneNumber> VALUE = new am.ik.yavi.meta.StringConstraintMeta<test.Address.PhoneNumber>() {

		@Override
		public String name() {
			return "value";
		}

		@Override
		public java.util.function.Function<test.Address.PhoneNumber, java.lang.@org.jspecify.annotations.Nullable String> toValue() {
			return test.Address.PhoneNumber::value;
		}
	};
}
