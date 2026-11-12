/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

public class AllTypesBean {

	private @Nullable String stringValue;

	private @Nullable Boolean booleanValue;

	private boolean booleanPrimitiveValue;

	private @Nullable Character characterValue;

	private char characterPrimitiveValue;

	private @Nullable Byte byteValue;

	private byte bytePrimitiveValue;

	private @Nullable Short shortValue;

	private short shortPrimitiveValue;

	private @Nullable Integer integerValue;

	private int integerPrimitiveValue;

	private @Nullable Long longValue;

	private long longPrimitiveValue;

	private @Nullable Float floatValue;

	private float floatPrimitiveValue;

	private @Nullable Double doubleValue;

	private double doublePrimitiveValue;

	private @Nullable BigInteger bigIntegerValue;

	private @Nullable BigDecimal bigDecimalValue;

	private @Nullable LocalDate localDateValue;

	@ConstraintTarget
	public @Nullable String getStringValue() {
		return stringValue;
	}

	public void setStringValue(@Nullable String stringValue) {
		this.stringValue = stringValue;
	}

	@ConstraintTarget
	public @Nullable Boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(@Nullable Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	@ConstraintTarget
	public boolean isBooleanPrimitiveValue() {
		return booleanPrimitiveValue;
	}

	public void setBooleanPrimitiveValue(boolean booleanPrimitiveValue) {
		this.booleanPrimitiveValue = booleanPrimitiveValue;
	}

	@ConstraintTarget
	public @Nullable Character getCharacterValue() {
		return characterValue;
	}

	public void setCharacterValue(@Nullable Character characterValue) {
		this.characterValue = characterValue;
	}

	@ConstraintTarget
	public char getCharacterPrimitiveValue() {
		return characterPrimitiveValue;
	}

	public void setCharacterPrimitiveValue(char characterPrimitiveValue) {
		this.characterPrimitiveValue = characterPrimitiveValue;
	}

	@ConstraintTarget
	public @Nullable Byte getByteValue() {
		return byteValue;
	}

	public void setByteValue(@Nullable Byte byteValue) {
		this.byteValue = byteValue;
	}

	@ConstraintTarget
	public byte getBytePrimitiveValue() {
		return bytePrimitiveValue;
	}

	public void setBytePrimitiveValue(byte bytePrimitiveValue) {
		this.bytePrimitiveValue = bytePrimitiveValue;
	}

	@ConstraintTarget
	public @Nullable Short getShortValue() {
		return shortValue;
	}

	public void setShortValue(@Nullable Short shortValue) {
		this.shortValue = shortValue;
	}

	@ConstraintTarget
	public short getShortPrimitiveValue() {
		return shortPrimitiveValue;
	}

	public void setShortPrimitiveValue(short shortPrimitiveValue) {
		this.shortPrimitiveValue = shortPrimitiveValue;
	}

	@ConstraintTarget
	public @Nullable Integer getIntegerValue() {
		return integerValue;
	}

	public void setIntegerValue(@Nullable Integer integerValue) {
		this.integerValue = integerValue;
	}

	@ConstraintTarget
	public int getIntegerPrimitiveValue() {
		return integerPrimitiveValue;
	}

	public void setIntegerPrimitiveValue(int integerPrimitiveValue) {
		this.integerPrimitiveValue = integerPrimitiveValue;
	}

	@ConstraintTarget
	public @Nullable Long getLongValue() {
		return longValue;
	}

	public void setLongValue(@Nullable Long longValue) {
		this.longValue = longValue;
	}

	@ConstraintTarget
	public long getLongPrimitiveValue() {
		return longPrimitiveValue;
	}

	public void setLongPrimitiveValue(long longPrimitiveValue) {
		this.longPrimitiveValue = longPrimitiveValue;
	}

	@ConstraintTarget
	public @Nullable Float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(@Nullable Float floatValue) {
		this.floatValue = floatValue;
	}

	@ConstraintTarget
	public float getFloatPrimitiveValue() {
		return floatPrimitiveValue;
	}

	public void setFloatPrimitiveValue(float floatPrimitiveValue) {
		this.floatPrimitiveValue = floatPrimitiveValue;
	}

	@ConstraintTarget
	public @Nullable Double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(@Nullable Double doubleValue) {
		this.doubleValue = doubleValue;
	}

	@ConstraintTarget
	public double getDoublePrimitiveValue() {
		return doublePrimitiveValue;
	}

	public void setDoublePrimitiveValue(double doublePrimitiveValue) {
		this.doublePrimitiveValue = doublePrimitiveValue;
	}

	@ConstraintTarget
	public @Nullable BigInteger getBigIntegerValue() {
		return bigIntegerValue;
	}

	public void setBigIntegerValue(@Nullable BigInteger bigIntegerValue) {
		this.bigIntegerValue = bigIntegerValue;
	}

	@ConstraintTarget
	public @Nullable BigDecimal getBigDecimalValue() {
		return bigDecimalValue;
	}

	public void setBigDecimalValue(@Nullable BigDecimal bigDecimalValue) {
		this.bigDecimalValue = bigDecimalValue;
	}

	@ConstraintTarget
	public @Nullable LocalDate getLocalDateValue() {
		return localDateValue;
	}

	public void setLocalDateValue(@Nullable LocalDate localDateValue) {
		this.localDateValue = localDateValue;
	}

}
