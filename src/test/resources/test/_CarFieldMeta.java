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
