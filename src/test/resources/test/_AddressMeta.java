/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

public class _AddressMeta {

	public static final am.ik.yavi.meta.ObjectConstraintMeta<test.Address, test.Address.Country> COUNTRY = new am.ik.yavi.meta.ObjectConstraintMeta<test.Address, test.Address.Country>() {

		@Override
		public String name() {
			return "country";
		}

		@Override
		public java.util.function.Function<test.Address, test.Address.@org.jspecify.annotations.Nullable Country> toValue() {
			return test.Address::country;
		}
	};

	public static final am.ik.yavi.meta.StringConstraintMeta<test.Address> STREET = new am.ik.yavi.meta.StringConstraintMeta<test.Address>() {

		@Override
		public String name() {
			return "street";
		}

		@Override
		public java.util.function.Function<test.Address, java.lang.@org.jspecify.annotations.Nullable String> toValue() {
			return test.Address::street;
		}
	};

	public static final am.ik.yavi.meta.ObjectConstraintMeta<test.Address, test.Address.PhoneNumber> PHONENUMBER = new am.ik.yavi.meta.ObjectConstraintMeta<test.Address, test.Address.PhoneNumber>() {

		@Override
		public String name() {
			return "phoneNumber";
		}

		@Override
		public java.util.function.Function<test.Address, test.Address.@org.jspecify.annotations.Nullable PhoneNumber> toValue() {
			return test.Address::phoneNumber;
		}
	};
}
