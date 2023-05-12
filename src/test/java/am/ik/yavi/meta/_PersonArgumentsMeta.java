/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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

// Generated at 2020-02-10T11:56:30.484098+09:00
public class _PersonArgumentsMeta {

	public static final am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>> FIRSTNAME = new am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>>() {

		@Override
		public String name() {
			return "firstName";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>, java.lang.String> toValue() {
			return am.ik.yavi.arguments.Arguments1::arg1;
		}
	};

	public static final am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>> LASTNAME = new am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>>() {

		@Override
		public String name() {
			return "lastName";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>, java.lang.String> toValue() {
			return am.ik.yavi.arguments.Arguments2::arg2;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>> AGE = new am.ik.yavi.meta.IntegerConstraintMeta<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>>() {

		@Override
		public String name() {
			return "age";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>, java.lang.Integer> toValue() {
			return am.ik.yavi.arguments.Arguments3::arg3;
		}
	};
}
