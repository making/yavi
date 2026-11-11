/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

public class _CarFieldMeta {
	public static final am.ik.yavi.meta.StringConstraintMeta<test.CarField> NAME = new am.ik.yavi.meta.StringConstraintMeta<test.CarField>() {

		@Override
		public String name() {
			return "name";
		}

		@Override
		public java.util.function.Function<test.CarField, java.lang.String> toValue() {
			return x -> x.name;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<test.CarField> GAS = new am.ik.yavi.meta.IntegerConstraintMeta<test.CarField>() {

		@Override
		public String name() {
			return "gas";
		}

		@Override
		public java.util.function.Function<test.CarField, java.lang.Integer> toValue() {
			return x -> x.gas;
		}
	};
}
