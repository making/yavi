/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

public class _Address_CountryMeta {

	public static final am.ik.yavi.meta.StringConstraintMeta<test.Address.Country> NAME = new am.ik.yavi.meta.StringConstraintMeta<test.Address.Country>() {

		@Override
		public String name() {
			return "name";
		}

		@Override
		public java.util.function.Function<test.Address.Country, java.lang.@org.jspecify.annotations.Nullable String> toValue() {
			return test.Address.Country::name;
		}
	};
}
