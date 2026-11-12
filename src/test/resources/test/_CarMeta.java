/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

public class _CarMeta {
	public static final am.ik.yavi.meta.StringConstraintMeta<test.Car> NAME = new am.ik.yavi.meta.StringConstraintMeta<test.Car>() {

		@Override
		public String name() {
			return "name";
		}

		@Override
		public java.util.function.Function<test.Car, java.lang.@org.jspecify.annotations.Nullable String> toValue() {
			return test.Car::name;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<test.Car> GAS = new am.ik.yavi.meta.IntegerConstraintMeta<test.Car>() {

		@Override
		public String name() {
			return "gas";
		}

		@Override
		public java.util.function.Function<test.Car, java.lang.@org.jspecify.annotations.Nullable Integer> toValue() {
			return test.Car::gas;
		}
	};
}
