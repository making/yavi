/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

public class _Address_CountryMeta {

	public static final StringConstraintMeta<am.ik.yavi.meta.Address.Country> NAME = new StringConstraintMeta<am.ik.yavi.meta.Address.Country>() {

		@Override
		public String name() {
			return "name";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.meta.Address.Country, @org.jspecify.annotations.Nullable String> toValue() {
			return am.ik.yavi.meta.Address.Country::name;
		}
	};

}
