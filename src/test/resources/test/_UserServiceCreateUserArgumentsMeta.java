/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

public class _UserServiceCreateUserArgumentsMeta {

	public static final am.ik.yavi.meta.ObjectConstraintMeta<am.ik.yavi.arguments.Arguments3<test.UserService, java.lang.String, java.lang.String>, test.UserService> USERSERVICE = new am.ik.yavi.meta.ObjectConstraintMeta<am.ik.yavi.arguments.Arguments3<test.UserService, java.lang.String, java.lang.String>, test.UserService>() {

		@Override
		public String name() {
			return "userService";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.arguments.Arguments3<test.UserService, java.lang.String, java.lang.String>, test.@org.jspecify.annotations.Nullable UserService> toValue() {
			return am.ik.yavi.arguments.Arguments3::arg1;
		}
	};

	public static final am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments3<test.UserService, java.lang.String, java.lang.String>> EMAIL = new am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments3<test.UserService, java.lang.String, java.lang.String>>() {

		@Override
		public String name() {
			return "email";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.arguments.Arguments3<test.UserService, java.lang.String, java.lang.String>, java.lang.@org.jspecify.annotations.Nullable String> toValue() {
			return am.ik.yavi.arguments.Arguments3::arg2;
		}
	};

	public static final am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments3<test.UserService, java.lang.String, java.lang.String>> NAME = new am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments3<test.UserService, java.lang.String, java.lang.String>>() {

		@Override
		public String name() {
			return "name";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.arguments.Arguments3<test.UserService, java.lang.String, java.lang.String>, java.lang.@org.jspecify.annotations.Nullable String> toValue() {
			return am.ik.yavi.arguments.Arguments3::arg3;
		}
	};
}