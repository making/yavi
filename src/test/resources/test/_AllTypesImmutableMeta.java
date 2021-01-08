/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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

public class _AllTypesImmutableMeta {

	public static final am.ik.yavi.meta.StringConstraintMeta<test.AllTypesImmutable> STRINGVALUE = new am.ik.yavi.meta.StringConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "stringValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.String> toValue() {
			return test.AllTypesImmutable::stringValue;
		}
	};

	public static final am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesImmutable> BOOLEANVALUE = new am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "booleanValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Boolean> toValue() {
			return test.AllTypesImmutable::booleanValue;
		}
	};

	public static final am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesImmutable> BOOLEANPRIMITIVEVALUE = new am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "booleanPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Boolean> toValue() {
			return test.AllTypesImmutable::booleanPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesImmutable> CHARACTERVALUE = new am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "characterValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Character> toValue() {
			return test.AllTypesImmutable::characterValue;
		}
	};

	public static final am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesImmutable> CHARACTERPRIMITIVEVALUE = new am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "characterPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Character> toValue() {
			return test.AllTypesImmutable::characterPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesImmutable> BYTEVALUE = new am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "byteValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Byte> toValue() {
			return test.AllTypesImmutable::byteValue;
		}
	};

	public static final am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesImmutable> BYTEPRIMITIVEVALUE = new am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "bytePrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Byte> toValue() {
			return test.AllTypesImmutable::bytePrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesImmutable> SHORTVALUE = new am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "shortValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Short> toValue() {
			return test.AllTypesImmutable::shortValue;
		}
	};

	public static final am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesImmutable> SHORTPRIMITIVEVALUE = new am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "shortPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Short> toValue() {
			return test.AllTypesImmutable::shortPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesImmutable> INTEGERVALUE = new am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "integerValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Integer> toValue() {
			return test.AllTypesImmutable::integerValue;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesImmutable> INTEGERPRIMITIVEVALUE = new am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "integerPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Integer> toValue() {
			return test.AllTypesImmutable::integerPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.LongConstraintMeta<test.AllTypesImmutable> LONGVALUE = new am.ik.yavi.meta.LongConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "longValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Long> toValue() {
			return test.AllTypesImmutable::longValue;
		}
	};

	public static final am.ik.yavi.meta.LongConstraintMeta<test.AllTypesImmutable> LONGPRIMITIVEVALUE = new am.ik.yavi.meta.LongConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "longPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Long> toValue() {
			return test.AllTypesImmutable::longPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesImmutable> FLOATVALUE = new am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "floatValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Float> toValue() {
			return test.AllTypesImmutable::floatValue;
		}
	};

	public static final am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesImmutable> FLOATPRIMITIVEVALUE = new am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "floatPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Float> toValue() {
			return test.AllTypesImmutable::floatPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesImmutable> DOUBLEVALUE = new am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "doubleValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Double> toValue() {
			return test.AllTypesImmutable::doubleValue;
		}
	};

	public static final am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesImmutable> DOUBLEPRIMITIVEVALUE = new am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "doublePrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.Double> toValue() {
			return test.AllTypesImmutable::doublePrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.BigIntegerConstraintMeta<test.AllTypesImmutable> BIGINTEGERVALUE = new am.ik.yavi.meta.BigIntegerConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "bigIntegerValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.math.BigInteger> toValue() {
			return test.AllTypesImmutable::bigIntegerValue;
		}
	};

	public static final am.ik.yavi.meta.BigDecimalConstraintMeta<test.AllTypesImmutable> BIGDECIMALVALUE = new am.ik.yavi.meta.BigDecimalConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "bigDecimalValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.math.BigDecimal> toValue() {
			return test.AllTypesImmutable::bigDecimalValue;
		}
	};

	public static final am.ik.yavi.meta.ObjectConstraintMeta<test.AllTypesImmutable, java.time.LocalDate> LOCALDATEVALUE = new am.ik.yavi.meta.ObjectConstraintMeta<test.AllTypesImmutable, java.time.LocalDate>() {

		@Override
		public String name() {
			return "localDateValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.time.LocalDate> toValue() {
			return test.AllTypesImmutable::localDateValue;
		}
	};
}
