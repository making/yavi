/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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

public class AllTypesBean {

    private String stringValue;

    private Boolean booleanValue;

    private boolean booleanPrimitiveValue;

    private Character characterValue;

    private char characterPrimitiveValue;

    private Byte byteValue;

    private byte bytePrimitiveValue;

    private Short shortValue;

    private short shortPrimitiveValue;

    private Integer integerValue;

    private int integerPrimitiveValue;

    private Long longValue;

    private long longPrimitiveValue;

    private Float floatValue;

    private float floatPrimitiveValue;

    private Double doubleValue;

    private double doublePrimitiveValue;

    private BigInteger bigIntegerValue;

    private BigDecimal bigDecimalValue;

    private LocalDate localDateValue;

    @ConstraintTarget
    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    @ConstraintTarget
    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
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
    public Character getCharacterValue() {
        return characterValue;
    }

    public void setCharacterValue(Character characterValue) {
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
    public Byte getByteValue() {
        return byteValue;
    }

    public void setByteValue(Byte byteValue) {
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
    public Short getShortValue() {
        return shortValue;
    }

    public void setShortValue(Short shortValue) {
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
    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
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
    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
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
    public Float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(Float floatValue) {
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
    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
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
    public BigInteger getBigIntegerValue() {
        return bigIntegerValue;
    }

    public void setBigIntegerValue(BigInteger bigIntegerValue) {
        this.bigIntegerValue = bigIntegerValue;
    }

    @ConstraintTarget
    public BigDecimal getBigDecimalValue() {
        return bigDecimalValue;
    }

    public void setBigDecimalValue(BigDecimal bigDecimalValue) {
        this.bigDecimalValue = bigDecimalValue;
    }

    @ConstraintTarget
    public LocalDate getLocalDateValue() {
        return localDateValue;
    }

    public void setLocalDateValue(LocalDate localDateValue) {
        this.localDateValue = localDateValue;
    }
}
