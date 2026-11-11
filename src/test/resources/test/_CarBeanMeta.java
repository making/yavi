/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

public class _CarBeanMeta {
	public static final am.ik.yavi.meta.StringConstraintMeta<test.CarBean> NAME = new am.ik.yavi.meta.StringConstraintMeta<test.CarBean>() {

		@Override
		public String name() {
			return "name";
		}

		@Override
		public java.util.function.Function<test.CarBean, java.lang.String> toValue() {
			return test.CarBean::getName;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<test.CarBean> GAS = new am.ik.yavi.meta.IntegerConstraintMeta<test.CarBean>() {

		@Override
		public String name() {
			return "gas";
		}

		@Override
		public java.util.function.Function<test.CarBean, java.lang.Integer> toValue() {
			return test.CarBean::getGas;
		}
	};
}
