/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

public class _Car2ArgumentsMeta {

	public static final am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments2<java.lang.String, java.lang.Integer>> NAME = new am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments2<java.lang.String, java.lang.Integer>>() {

		@Override
		public String name() {
			return "name";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.arguments.Arguments2<java.lang.String, java.lang.Integer>, java.lang.String> toValue() {
			return am.ik.yavi.arguments.Arguments2::arg1;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<am.ik.yavi.arguments.Arguments2<java.lang.String, java.lang.Integer>> GAS = new am.ik.yavi.meta.IntegerConstraintMeta<am.ik.yavi.arguments.Arguments2<java.lang.String, java.lang.Integer>>() {

		@Override
		public String name() {
			return "gas";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.arguments.Arguments2<java.lang.String, java.lang.Integer>, java.lang.Integer> toValue() {
			return am.ik.yavi.arguments.Arguments2::arg2;
		}
	};
}