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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import am.ik.yavi.meta.ConstraintTarget;

public class AllTypesImmutable {

	private final String stringValue;

	private final Boolean booleanValue;

	private final boolean booleanPrimitiveValue;

	private final Character characterValue;

	private final char characterPrimitiveValue;

	private final Byte byteValue;

	private final byte bytePrimitiveValue;

	private final Short shortValue;

	private final short shortPrimitiveValue;

	private final Integer integerValue;

	private final int integerPrimitiveValue;

	private final Long longValue;

	private final long longPrimitiveValue;

	private final Float floatValue;

	private final float floatPrimitiveValue;

	private final Double doubleValue;

	private final double doublePrimitiveValue;

	private final BigInteger bigIntegerValue;

	private final BigDecimal bigDecimalValue;

	private final LocalDate localDateValue;

	public AllTypesImmutable(String stringValue, Boolean booleanValue,
			boolean booleanPrimitiveValue, Character characterValue,
			char characterPrimitiveValue, Byte byteValue, byte bytePrimitiveValue,
			Short shortValue, short shortPrimitiveValue, Integer integerValue,
			int integerPrimitiveValue, Long longValue, long longPrimitiveValue,
			Float floatValue, float floatPrimitiveValue, Double doubleValue,
			double doublePrimitiveValue, BigInteger bigIntegerValue,
			BigDecimal bigDecimalValue, LocalDate localDateValue) {
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
	public String stringValue() {
		return stringValue;
	}

	@ConstraintTarget(getter = false)
	public Boolean booleanValue() {
		return booleanValue;
	}

	@ConstraintTarget(getter = false)
	public boolean booleanPrimitiveValue() {
		return booleanPrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public Character characterValue() {
		return characterValue;
	}

	@ConstraintTarget(getter = false)
	public char characterPrimitiveValue() {
		return characterPrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public Byte byteValue() {
		return byteValue;
	}

	@ConstraintTarget(getter = false)
	public byte bytePrimitiveValue() {
		return bytePrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public Short shortValue() {
		return shortValue;
	}

	@ConstraintTarget(getter = false)
	public short shortPrimitiveValue() {
		return shortPrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public Integer integerValue() {
		return integerValue;
	}

	@ConstraintTarget(getter = false)
	public int integerPrimitiveValue() {
		return integerPrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public Long longValue() {
		return longValue;
	}

	@ConstraintTarget(getter = false)
	public long longPrimitiveValue() {
		return longPrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public Float floatValue() {
		return floatValue;
	}

	@ConstraintTarget(getter = false)
	public float floatPrimitiveValue() {
		return floatPrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public Double doubleValue() {
		return doubleValue;
	}

	@ConstraintTarget(getter = false)
	public double doublePrimitiveValue() {
		return doublePrimitiveValue;
	}

	@ConstraintTarget(getter = false)
	public BigInteger bigIntegerValue() {
		return bigIntegerValue;
	}

	@ConstraintTarget(getter = false)
	public BigDecimal bigDecimalValue() {
		return bigDecimalValue;
	}

	@ConstraintTarget(getter = false)
	public LocalDate localDateValue() {
		return localDateValue;
	}
}
