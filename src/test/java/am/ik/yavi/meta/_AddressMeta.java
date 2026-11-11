/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

public class _AddressMeta {

	public static final ObjectConstraintMeta<am.ik.yavi.meta.Address, am.ik.yavi.meta.Address.Country> COUNTRY = new ObjectConstraintMeta<am.ik.yavi.meta.Address, am.ik.yavi.meta.Address.Country>() {

		@Override
		public String name() {
			return "country";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.meta.Address, am.ik.yavi.meta.Address.Country> toValue() {
			return am.ik.yavi.meta.Address::country;
		}
	};

	public static final StringConstraintMeta<am.ik.yavi.meta.Address> STREET = new StringConstraintMeta<am.ik.yavi.meta.Address>() {

		@Override
		public String name() {
			return "street";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.meta.Address, String> toValue() {
			return am.ik.yavi.meta.Address::street;
		}
	};

	public static final ObjectConstraintMeta<am.ik.yavi.meta.Address, am.ik.yavi.meta.Address.PhoneNumber> PHONENUMBER = new ObjectConstraintMeta<am.ik.yavi.meta.Address, am.ik.yavi.meta.Address.PhoneNumber>() {

		@Override
		public String name() {
			return "phoneNumber";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.meta.Address, am.ik.yavi.meta.Address.PhoneNumber> toValue() {
			return am.ik.yavi.meta.Address::phoneNumber;
		}
	};

}
