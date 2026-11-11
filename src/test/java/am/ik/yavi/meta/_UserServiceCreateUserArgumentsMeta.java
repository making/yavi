/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

public class _UserServiceCreateUserArgumentsMeta {

	public static final ObjectConstraintMeta<am.ik.yavi.arguments.Arguments3<am.ik.yavi.meta.UserService, String, String>, am.ik.yavi.meta.UserService> USERSERVICE = new ObjectConstraintMeta<am.ik.yavi.arguments.Arguments3<am.ik.yavi.meta.UserService, String, String>, am.ik.yavi.meta.UserService>() {

		@Override
		public String name() {
			return "userService";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.arguments.Arguments3<am.ik.yavi.meta.UserService, String, String>, am.ik.yavi.meta.UserService> toValue() {
			return am.ik.yavi.arguments.Arguments3::arg1;
		}
	};

	public static final StringConstraintMeta<am.ik.yavi.arguments.Arguments3<am.ik.yavi.meta.UserService, String, String>> EMAIL = new StringConstraintMeta<am.ik.yavi.arguments.Arguments3<am.ik.yavi.meta.UserService, String, String>>() {

		@Override
		public String name() {
			return "email";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.arguments.Arguments3<am.ik.yavi.meta.UserService, String, String>, String> toValue() {
			return am.ik.yavi.arguments.Arguments3::arg2;
		}
	};

	public static final StringConstraintMeta<am.ik.yavi.arguments.Arguments3<am.ik.yavi.meta.UserService, String, String>> NAME = new StringConstraintMeta<am.ik.yavi.arguments.Arguments3<am.ik.yavi.meta.UserService, String, String>>() {

		@Override
		public String name() {
			return "name";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.arguments.Arguments3<am.ik.yavi.meta.UserService, String, String>, String> toValue() {
			return am.ik.yavi.arguments.Arguments3::arg3;
		}
	};

}