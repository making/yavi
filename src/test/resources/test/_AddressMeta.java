/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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

public class _AddressMeta {

	public static final am.ik.yavi.meta.ObjectConstraintMeta<test.Address, test.Address.Country> COUNTRY = new am.ik.yavi.meta.ObjectConstraintMeta<test.Address, test.Address.Country>() {

		@Override
		public String name() {
			return "country";
		}

		@Override
		public java.util.function.Function<test.Address, test.Address.Country> toValue() {
			return test.Address::country;
		}
	};

	public static final am.ik.yavi.meta.StringConstraintMeta<test.Address> STREET = new am.ik.yavi.meta.StringConstraintMeta<test.Address>() {

		@Override
		public String name() {
			return "street";
		}

		@Override
		public java.util.function.Function<test.Address, java.lang.String> toValue() {
			return test.Address::street;
		}
	};

	public static final am.ik.yavi.meta.ObjectConstraintMeta<test.Address, test.Address.PhoneNumber> PHONENUMBER = new am.ik.yavi.meta.ObjectConstraintMeta<test.Address, test.Address.PhoneNumber>() {

		@Override
		public String name() {
			return "phoneNumber";
		}

		@Override
		public java.util.function.Function<test.Address, test.Address.PhoneNumber> toValue() {
			return test.Address::phoneNumber;
		}
	};
}
