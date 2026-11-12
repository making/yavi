/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

public class _AllTypesImmutableMeta {

	public static final am.ik.yavi.meta.StringConstraintMeta<test.AllTypesImmutable> STRINGVALUE = new am.ik.yavi.meta.StringConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "stringValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable String> toValue() {
			return test.AllTypesImmutable::stringValue;
		}
	};

	public static final am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesImmutable> BOOLEANVALUE = new am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "booleanValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Boolean> toValue() {
			return test.AllTypesImmutable::booleanValue;
		}
	};

	public static final am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesImmutable> BOOLEANPRIMITIVEVALUE = new am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "booleanPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Boolean> toValue() {
			return test.AllTypesImmutable::booleanPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesImmutable> CHARACTERVALUE = new am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "characterValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Character> toValue() {
			return test.AllTypesImmutable::characterValue;
		}
	};

	public static final am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesImmutable> CHARACTERPRIMITIVEVALUE = new am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "characterPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Character> toValue() {
			return test.AllTypesImmutable::characterPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesImmutable> BYTEVALUE = new am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "byteValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Byte> toValue() {
			return test.AllTypesImmutable::byteValue;
		}
	};

	public static final am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesImmutable> BYTEPRIMITIVEVALUE = new am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "bytePrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Byte> toValue() {
			return test.AllTypesImmutable::bytePrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesImmutable> SHORTVALUE = new am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "shortValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Short> toValue() {
			return test.AllTypesImmutable::shortValue;
		}
	};

	public static final am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesImmutable> SHORTPRIMITIVEVALUE = new am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "shortPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Short> toValue() {
			return test.AllTypesImmutable::shortPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesImmutable> INTEGERVALUE = new am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "integerValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Integer> toValue() {
			return test.AllTypesImmutable::integerValue;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesImmutable> INTEGERPRIMITIVEVALUE = new am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "integerPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Integer> toValue() {
			return test.AllTypesImmutable::integerPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.LongConstraintMeta<test.AllTypesImmutable> LONGVALUE = new am.ik.yavi.meta.LongConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "longValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Long> toValue() {
			return test.AllTypesImmutable::longValue;
		}
	};

	public static final am.ik.yavi.meta.LongConstraintMeta<test.AllTypesImmutable> LONGPRIMITIVEVALUE = new am.ik.yavi.meta.LongConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "longPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Long> toValue() {
			return test.AllTypesImmutable::longPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesImmutable> FLOATVALUE = new am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "floatValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Float> toValue() {
			return test.AllTypesImmutable::floatValue;
		}
	};

	public static final am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesImmutable> FLOATPRIMITIVEVALUE = new am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "floatPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Float> toValue() {
			return test.AllTypesImmutable::floatPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesImmutable> DOUBLEVALUE = new am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "doubleValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Double> toValue() {
			return test.AllTypesImmutable::doubleValue;
		}
	};

	public static final am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesImmutable> DOUBLEPRIMITIVEVALUE = new am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "doublePrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.lang.@org.jspecify.annotations.Nullable Double> toValue() {
			return test.AllTypesImmutable::doublePrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.BigIntegerConstraintMeta<test.AllTypesImmutable> BIGINTEGERVALUE = new am.ik.yavi.meta.BigIntegerConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "bigIntegerValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.math.@org.jspecify.annotations.Nullable BigInteger> toValue() {
			return test.AllTypesImmutable::bigIntegerValue;
		}
	};

	public static final am.ik.yavi.meta.BigDecimalConstraintMeta<test.AllTypesImmutable> BIGDECIMALVALUE = new am.ik.yavi.meta.BigDecimalConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "bigDecimalValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.math.@org.jspecify.annotations.Nullable BigDecimal> toValue() {
			return test.AllTypesImmutable::bigDecimalValue;
		}
	};

	public static final am.ik.yavi.meta.LocalDateConstraintMeta<test.AllTypesImmutable> LOCALDATEVALUE = new am.ik.yavi.meta.LocalDateConstraintMeta<test.AllTypesImmutable>() {

		@Override
		public String name() {
			return "localDateValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesImmutable, java.time.@org.jspecify.annotations.Nullable LocalDate> toValue() {
			return test.AllTypesImmutable::localDateValue;
		}
	};
}
