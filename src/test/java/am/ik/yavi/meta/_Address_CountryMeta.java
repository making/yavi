/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
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

public class _Address_CountryMeta {

	public static final StringConstraintMeta<am.ik.yavi.meta.Address.Country> NAME = new StringConstraintMeta<am.ik.yavi.meta.Address.Country>() {

		@Override
		public String name() {
			return "name";
		}

		@Override
		public java.util.function.Function<am.ik.yavi.meta.Address.Country, String> toValue() {
			return am.ik.yavi.meta.Address.Country::name;
		}
	};

}
