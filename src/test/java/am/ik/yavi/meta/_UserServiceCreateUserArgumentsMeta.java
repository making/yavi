/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
			return am.ik.yavi.arguments.Arguments1::arg1;
		}
	};

	public static final StringConstraintMeta<am.ik.yavi.arguments.Arguments3<am.ik.yavi.meta.UserService, String, String>> EMAIL = new StringConstraintMeta<am.ik.yavi.arguments.Arguments3<am.ik.yavi.meta.UserService, String, String>>() {

		@Override
		public String name() {
			return "email";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.arguments.Arguments3<am.ik.yavi.meta.UserService, String, String>, String> toValue() {
			return am.ik.yavi.arguments.Arguments2::arg2;
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