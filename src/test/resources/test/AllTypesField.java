package test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import am.ik.yavi.meta.ConstraintTarget;

public class AllTypesField {

	final String stringValue;

	final Boolean booleanValue;

	final boolean booleanPrimitiveValue;

	final Character characterValue;

	final char characterPrimitiveValue;

	final Byte byteValue;

	final byte bytePrimitiveValue;

	final Short shortValue;

	final short shortPrimitiveValue;

	final Integer integerValue;

	final int integerPrimitiveValue;

	final Long longValue;

	final long longPrimitiveValue;

	final Float floatValue;

	final float floatPrimitiveValue;

	final Double doubleValue;

	final double doublePrimitiveValue;

	final BigInteger bigIntegerValue;

	final BigDecimal bigDecimalValue;

	final LocalDate localDateValue;

	public AllTypesField(@ConstraintTarget(field = true) String stringValue,
			@ConstraintTarget(field = true) Boolean booleanValue,
			@ConstraintTarget(field = true) boolean booleanPrimitiveValue,
			@ConstraintTarget(field = true) Character characterValue,
			@ConstraintTarget(field = true) char characterPrimitiveValue,
			@ConstraintTarget(field = true) Byte byteValue,
			@ConstraintTarget(field = true) byte bytePrimitiveValue,
			@ConstraintTarget(field = true) Short shortValue,
			@ConstraintTarget(field = true) short shortPrimitiveValue,
			@ConstraintTarget(field = true) Integer integerValue,
			@ConstraintTarget(field = true) int integerPrimitiveValue,
			@ConstraintTarget(field = true) Long longValue,
			@ConstraintTarget(field = true) long longPrimitiveValue,
			@ConstraintTarget(field = true) Float floatValue,
			@ConstraintTarget(field = true) float floatPrimitiveValue,
			@ConstraintTarget(field = true) Double doubleValue,
			@ConstraintTarget(field = true) double doublePrimitiveValue,
			@ConstraintTarget(field = true) BigInteger bigIntegerValue,
			@ConstraintTarget(field = true) BigDecimal bigDecimalValue,
			@ConstraintTarget(field = true) LocalDate localDateValue) {
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
