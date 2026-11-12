/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

public class _AllTypesBeanMeta {

	public static final am.ik.yavi.meta.StringConstraintMeta<test.AllTypesBean> STRINGVALUE = new am.ik.yavi.meta.StringConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "stringValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable String> toValue() {
			return test.AllTypesBean::getStringValue;
		}
	};

	public static final am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesBean> BOOLEANVALUE = new am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "booleanValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Boolean> toValue() {
			return test.AllTypesBean::getBooleanValue;
		}
	};

	public static final am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesBean> BOOLEANPRIMITIVEVALUE = new am.ik.yavi.meta.BooleanConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "booleanPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Boolean> toValue() {
			return test.AllTypesBean::isBooleanPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesBean> CHARACTERVALUE = new am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "characterValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Character> toValue() {
			return test.AllTypesBean::getCharacterValue;
		}
	};

	public static final am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesBean> CHARACTERPRIMITIVEVALUE = new am.ik.yavi.meta.CharacterConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "characterPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Character> toValue() {
			return test.AllTypesBean::getCharacterPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesBean> BYTEVALUE = new am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "byteValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Byte> toValue() {
			return test.AllTypesBean::getByteValue;
		}
	};

	public static final am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesBean> BYTEPRIMITIVEVALUE = new am.ik.yavi.meta.ByteConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "bytePrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Byte> toValue() {
			return test.AllTypesBean::getBytePrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesBean> SHORTVALUE = new am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "shortValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Short> toValue() {
			return test.AllTypesBean::getShortValue;
		}
	};

	public static final am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesBean> SHORTPRIMITIVEVALUE = new am.ik.yavi.meta.ShortConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "shortPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Short> toValue() {
			return test.AllTypesBean::getShortPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesBean> INTEGERVALUE = new am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "integerValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Integer> toValue() {
			return test.AllTypesBean::getIntegerValue;
		}
	};

	public static final am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesBean> INTEGERPRIMITIVEVALUE = new am.ik.yavi.meta.IntegerConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "integerPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Integer> toValue() {
			return test.AllTypesBean::getIntegerPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.LongConstraintMeta<test.AllTypesBean> LONGVALUE = new am.ik.yavi.meta.LongConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "longValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Long> toValue() {
			return test.AllTypesBean::getLongValue;
		}
	};

	public static final am.ik.yavi.meta.LongConstraintMeta<test.AllTypesBean> LONGPRIMITIVEVALUE = new am.ik.yavi.meta.LongConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "longPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Long> toValue() {
			return test.AllTypesBean::getLongPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesBean> FLOATVALUE = new am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "floatValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Float> toValue() {
			return test.AllTypesBean::getFloatValue;
		}
	};

	public static final am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesBean> FLOATPRIMITIVEVALUE = new am.ik.yavi.meta.FloatConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "floatPrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Float> toValue() {
			return test.AllTypesBean::getFloatPrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesBean> DOUBLEVALUE = new am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "doubleValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Double> toValue() {
			return test.AllTypesBean::getDoubleValue;
		}
	};

	public static final am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesBean> DOUBLEPRIMITIVEVALUE = new am.ik.yavi.meta.DoubleConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "doublePrimitiveValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.lang.@org.jspecify.annotations.Nullable Double> toValue() {
			return test.AllTypesBean::getDoublePrimitiveValue;
		}
	};

	public static final am.ik.yavi.meta.BigIntegerConstraintMeta<test.AllTypesBean> BIGINTEGERVALUE = new am.ik.yavi.meta.BigIntegerConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "bigIntegerValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.math.@org.jspecify.annotations.Nullable BigInteger> toValue() {
			return test.AllTypesBean::getBigIntegerValue;
		}
	};

	public static final am.ik.yavi.meta.BigDecimalConstraintMeta<test.AllTypesBean> BIGDECIMALVALUE = new am.ik.yavi.meta.BigDecimalConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "bigDecimalValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.math.@org.jspecify.annotations.Nullable BigDecimal> toValue() {
			return test.AllTypesBean::getBigDecimalValue;
		}
	};

	public static final am.ik.yavi.meta.LocalDateConstraintMeta<test.AllTypesBean> LOCALDATEVALUE = new am.ik.yavi.meta.LocalDateConstraintMeta<test.AllTypesBean>() {

		@Override
		public String name() {
			return "localDateValue";
		}

		@Override
		public java.util.function.Function<test.AllTypesBean, java.time.@org.jspecify.annotations.Nullable LocalDate> toValue() {
			return test.AllTypesBean::getLocalDateValue;
		}
	};
}
