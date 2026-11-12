/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

public class _AllTypesFieldMeta {

	public static final am.ik.yavi.meta.StringConstraintMeta<test.AllTypesField> STRINGVALUE = new am.ik.yavi.meta.StringConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "stringValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable String> toValue() {
			return x -> x.stringValue;
		}
	};

	public static final am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesField> BOOLEANVALUE = new am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "booleanValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Boolean> toValue() {
			return x -> x.booleanValue;
		}
	};

	public static final am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesField> BOOLEANPRIMITIVEVALUE = new am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "booleanPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Boolean> toValue() {
			return x -> x.booleanPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesField> CHARACTERVALUE = new am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "characterValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Character> toValue() {
			return x -> x.characterValue;
		}
	};

	public static final am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesField> CHARACTERPRIMITIVEVALUE = new am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "characterPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Character> toValue() {
			return x -> x.characterPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesField> BYTEVALUE = new am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "byteValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Byte> toValue() {
			return x -> x.byteValue;
		}
	};

	public static final am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesField> BYTEPRIMITIVEVALUE = new am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "bytePrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Byte> toValue() {
			return x -> x.bytePrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesField> SHORTVALUE = new am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "shortValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Short> toValue() {
			return x -> x.shortValue;
		}
	};

	public static final am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesField> SHORTPRIMITIVEVALUE = new am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "shortPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Short> toValue() {
			return x -> x.shortPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesField> INTEGERVALUE = new am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "integerValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Integer> toValue() {
			return x -> x.integerValue;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesField> INTEGERPRIMITIVEVALUE = new am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "integerPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Integer> toValue() {
			return x -> x.integerPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.LongConstraintMeta<test.AllTypesField> LONGVALUE = new am.ik.yavi.meta.LongConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "longValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Long> toValue() {
			return x -> x.longValue;
		}
	};

	public static final am.ik.yavi.meta.LongConstraintMeta<test.AllTypesField> LONGPRIMITIVEVALUE = new am.ik.yavi.meta.LongConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "longPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Long> toValue() {
			return x -> x.longPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesField> FLOATVALUE = new am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "floatValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Float> toValue() {
			return x -> x.floatValue;
		}
	};

	public static final am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesField> FLOATPRIMITIVEVALUE = new am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "floatPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Float> toValue() {
			return x -> x.floatPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesField> DOUBLEVALUE = new am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "doubleValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Double> toValue() {
			return x -> x.doubleValue;
		}
	};

	public static final am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesField> DOUBLEPRIMITIVEVALUE = new am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "doublePrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.lang.@org.jspecify.annotations.Nullable Double> toValue() {
			return x -> x.doublePrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.BigIntegerConstraintMeta<test.AllTypesField> BIGINTEGERVALUE = new am.ik.yavi.meta.BigIntegerConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "bigIntegerValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.math.@org.jspecify.annotations.Nullable BigInteger> toValue() {
			return x -> x.bigIntegerValue;
		}
	};

	public static final am.ik.yavi.meta.BigDecimalConstraintMeta<test.AllTypesField> BIGDECIMALVALUE = new am.ik.yavi.meta.BigDecimalConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "bigDecimalValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.math.@org.jspecify.annotations.Nullable BigDecimal> toValue() {
			return x -> x.bigDecimalValue;
		}
	};

	public static final am.ik.yavi.meta.LocalDateConstraintMeta<test.AllTypesField> LOCALDATEVALUE = new am.ik.yavi.meta.LocalDateConstraintMeta<test.AllTypesField>() {

		@Override
		public String name() {
			return "localDateValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesField, java.time.@org.jspecify.annotations.Nullable LocalDate> toValue() {
			return x -> x.localDateValue;
		}
	};
}