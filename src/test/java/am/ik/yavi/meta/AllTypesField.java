/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

public class AllTypesField {

	final @Nullable String stringValue;

	final @Nullable Boolean booleanValue;

	final boolean booleanPrimitiveValue;

	final @Nullable Character characterValue;

	final char characterPrimitiveValue;

	final @Nullable Byte byteValue;

	final byte bytePrimitiveValue;

	final @Nullable Short shortValue;

	final short shortPrimitiveValue;

	final @Nullable Integer integerValue;

	final int integerPrimitiveValue;

	final @Nullable Long longValue;

	final long longPrimitiveValue;

	final @Nullable Float floatValue;

	final float floatPrimitiveValue;

	final @Nullable Double doubleValue;

	final double doublePrimitiveValue;

	final @Nullable BigInteger bigIntegerValue;

	final @Nullable BigDecimal bigDecimalValue;

	final @Nullable LocalDate localDateValue;

	public AllTypesField(@ConstraintTarget(field = true) String stringValue,
			@ConstraintTarget(field = true) Boolean booleanValue,
			@ConstraintTarget(field = true) boolean booleanPrimitiveValue,
			@ConstraintTarget(field = true) Character characterValue,
			@ConstraintTarget(field = true) char characterPrimitiveValue,
			@ConstraintTarget(field = true) Byte byteValue, @ConstraintTarget(field = true) byte bytePrimitiveValue,
			@ConstraintTarget(field = true) Short shortValue, @ConstraintTarget(field = true) short shortPrimitiveValue,
			@ConstraintTarget(field = true) Integer integerValue,
			@ConstraintTarget(field = true) int integerPrimitiveValue, @ConstraintTarget(field = true) Long longValue,
			@ConstraintTarget(field = true) long longPrimitiveValue, @ConstraintTarget(field = true) Float floatValue,
			@ConstraintTarget(field = true) float floatPrimitiveValue,
			@ConstraintTarget(field = true) Double doubleValue,
			@ConstraintTarget(field = true) double doublePrimitiveValue,
			@ConstraintTarget(field = true) BigInteger bigIntegerValue,
			@ConstraintTarget(field = true) BigDecimal bigDecimalValue,
			@ConstraintTarget(field = true) @Nullable LocalDate localDateValue) {
		this.stringValue = stringValue;
		this.booleanValue = booleanValue;
		this.booleanPrimitiveValue = booleanPrimitiveValue;
		this.characterValue = characterValue;
		this.characterPrimitiveValue = characterPrimitiveValue;
		this.byteValue = byteValue;
		this.bytePrimitiveValue = bytePrimitiveValue;
		this.shortValue = shortValue;
		this.shortPrimitiveValue = shortPrimitiveValue;
		this.integerValue = integerValue;
		this.integerPrimitiveValue = integerPrimitiveValue;
		this.longValue = longValue;
		this.longPrimitiveValue = longPrimitiveValue;
		this.floatValue = floatValue;
		this.floatPrimitiveValue = floatPrimitiveValue;
		this.doubleValue = doubleValue;
		this.doublePrimitiveValue = doublePrimitiveValue;
		this.bigIntegerValue = bigIntegerValue;
		this.bigDecimalValue = bigDecimalValue;
		this.localDateValue = localDateValue;
	}

}
