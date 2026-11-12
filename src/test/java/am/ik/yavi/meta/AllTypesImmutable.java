/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

public class AllTypesImmutable {

	private final @Nullable String stringValue;

	private final @Nullable Boolean booleanValue;

	private final boolean booleanPrimitiveValue;

	private final @Nullable Character characterValue;

	private final char characterPrimitiveValue;

	private final @Nullable Byte byteValue;

	private final byte bytePrimitiveValue;

	private final @Nullable Short shortValue;

	private final short shortPrimitiveValue;

	private final @Nullable Integer integerValue;

	private final int integerPrimitiveValue;

	private final @Nullable Long longValue;

	private final long longPrimitiveValue;

	private final @Nullable Float floatValue;

	private final float floatPrimitiveValue;

	private final @Nullable Double doubleValue;

	private final double doublePrimitiveValue;

	private final @Nullable BigInteger bigIntegerValue;

	private final @Nullable BigDecimal bigDecimalValue;

	private final @Nullable LocalDate localDateValue;

	public AllTypesImmutable(@Nullable String stringValue, @Nullable Boolean booleanValue,
			boolean booleanPrimitiveValue, @Nullable Character characterValue, char characterPrimitiveValue,
			@Nullable Byte byteValue, byte bytePrimitiveValue, @Nullable Short shortValue, short shortPrimitiveValue,
			@Nullable Integer integerValue, int integerPrimitiveValue, @Nullable Long longValue,
			long longPrimitiveValue, @Nullable Float floatValue, float floatPrimitiveValue,
			@Nullable Double doubleValue, double doublePrimitiveValue, @Nullable BigInteger bigIntegerValue,
			@Nullable BigDecimal bigDecimalValue, @Nullable LocalDate localDateValue) {
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

	@ConstraintTarget(getter = false)
	public @Nullable String stringValue() {
		return stringValue;
	}

	@ConstraintTarget(getter = false)
	public @Nullable Boolean booleanValue() {
		return booleanValue;
	}

	@ConstraintTarget(getter = false)
	public boolean booleanPrimitiveValue() {
		return booleanPrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public @Nullable Character characterValue() {
		return characterValue;
	}

	@ConstraintTarget(getter = false)
	public char characterPrimitiveValue() {
		return characterPrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public @Nullable Byte byteValue() {
		return byteValue;
	}

	@ConstraintTarget(getter = false)
	public byte bytePrimitiveValue() {
		return bytePrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public @Nullable Short shortValue() {
		return shortValue;
	}

	@ConstraintTarget(getter = false)
	public short shortPrimitiveValue() {
		return shortPrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public @Nullable Integer integerValue() {
		return integerValue;
	}

	@ConstraintTarget(getter = false)
	public int integerPrimitiveValue() {
		return integerPrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public @Nullable Long longValue() {
		return longValue;
	}

	@ConstraintTarget(getter = false)
	public long longPrimitiveValue() {
		return longPrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public @Nullable Float floatValue() {
		return floatValue;
	}

	@ConstraintTarget(getter = false)
	public float floatPrimitiveValue() {
		return floatPrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public @Nullable Double doubleValue() {
		return doubleValue;
	}

	@ConstraintTarget(getter = false)
	public double doublePrimitiveValue() {
		return doublePrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public @Nullable BigInteger bigIntegerValue() {
		return bigIntegerValue;
	}

	@ConstraintTarget(getter = false)
	public @Nullable BigDecimal bigDecimalValue() {
		return bigDecimalValue;
	}

	@ConstraintTarget(getter = false)
	public @Nullable LocalDate localDateValue() {
		return localDateValue;
	}

}
