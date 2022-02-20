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
