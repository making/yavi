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
