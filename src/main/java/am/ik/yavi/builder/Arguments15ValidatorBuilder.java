/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.builder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.arguments.Arguments15Validator;
import am.ik.yavi.constraint.BigDecimalConstraint;
import am.ik.yavi.constraint.BigIntegerConstraint;
import am.ik.yavi.constraint.BooleanConstraint;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.constraint.DoubleConstraint;
import am.ik.yavi.constraint.EnumConstraint;
import am.ik.yavi.constraint.FloatConstraint;
import am.ik.yavi.constraint.InstantConstraint;
import am.ik.yavi.constraint.IntegerConstraint;
import am.ik.yavi.constraint.LocalDateTimeConstraint;
import am.ik.yavi.constraint.LocalTimeConstraint;
import am.ik.yavi.constraint.LongConstraint;
import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.constraint.OffsetDateTimeConstraint;
import am.ik.yavi.constraint.ShortConstraint;
import am.ik.yavi.constraint.YearConstraint;
import am.ik.yavi.constraint.YearMonthConstraint;
import am.ik.yavi.constraint.ZonedDateTimeConstraint;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.fn.Function15;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.14.0
 */
public final class Arguments15ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15> {

	final ValueValidator<A1, R1> v1;

	final ValueValidator<A2, R2> v2;

	final ValueValidator<A3, R3> v3;

	final ValueValidator<A4, R4> v4;

	final ValueValidator<A5, R5> v5;

	final ValueValidator<A6, R6> v6;

	final ValueValidator<A7, R7> v7;

	final ValueValidator<A8, R8> v8;

	final ValueValidator<A9, R9> v9;

	final ValueValidator<A10, R10> v10;

	final ValueValidator<A11, R11> v11;

	final ValueValidator<A12, R12> v12;

	final ValueValidator<A13, R13> v13;

	final ValueValidator<A14, R14> v14;

	final ValueValidator<A15, R15> v15;

	public Arguments15ValidatorBuilder(ValueValidator<A1, R1> v1, ValueValidator<A2, R2> v2, ValueValidator<A3, R3> v3,
			ValueValidator<A4, R4> v4, ValueValidator<A5, R5> v5, ValueValidator<A6, R6> v6, ValueValidator<A7, R7> v7,
			ValueValidator<A8, R8> v8, ValueValidator<A9, R9> v9, ValueValidator<A10, R10> v10,
			ValueValidator<A11, R11> v11, ValueValidator<A12, R12> v12, ValueValidator<A13, R13> v13,
			ValueValidator<A14, R14> v14, ValueValidator<A15, R15> v15) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;
		this.v5 = v5;
		this.v6 = v6;
		this.v7 = v7;
		this.v8 = v8;
		this.v9 = v9;
		this.v10 = v10;
		this.v11 = v11;
		this.v12 = v12;
		this.v13 = v13;
		this.v14 = v14;
		this.v15 = v15;
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, BigDecimal, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _bigDecimal(
			ValueValidator<BigDecimal, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, BigDecimal, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, BigDecimal> _bigDecimal(
			String name,
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		return this._bigDecimal(BigDecimalValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, BigDecimal, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, BigDecimal> _bigDecimal(
			String name) {
		return this._bigDecimal(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, BigInteger, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _bigInteger(
			ValueValidator<BigInteger, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, BigInteger, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, BigInteger> _bigInteger(
			String name,
			Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
		return this._bigInteger(BigIntegerValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, BigInteger, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, BigInteger> _bigInteger(
			String name) {
		return this._bigInteger(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Boolean, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _boolean(
			ValueValidator<Boolean, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Boolean, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Boolean> _boolean(
			String name,
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		return this._boolean(BooleanValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Boolean, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Boolean> _boolean(
			String name) {
		return this._boolean(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Double, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _double(
			ValueValidator<Double, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Double, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Double> _double(
			String name,
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		return this._double(DoubleValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Double, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Double> _double(
			String name) {
		return this._double(name, Function.identity());
	}

	public <E extends Enum<E>, T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, E, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _enum(
			ValueValidator<E, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public <E extends Enum<E>> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, E, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, E> _enum(
			String name, Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return this._enum(EnumValidatorBuilder.of(name, constraints).build());
	}

	public <E extends Enum<E>> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, E, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, E> _enum(
			String name) {
		return this._enum(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Float, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _float(
			ValueValidator<Float, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Float, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Float> _float(
			String name, Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		return this._float(FloatValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Float, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Float> _float(
			String name) {
		return this._float(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Instant, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _instant(
			ValueValidator<Instant, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Instant, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Instant> _instant(
			String name,
			Function<InstantConstraint<Arguments1<Instant>>, InstantConstraint<Arguments1<Instant>>> constraints) {
		return this._instant(InstantValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Instant, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Instant> _instant(
			String name) {
		return this._instant(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Integer, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _integer(
			ValueValidator<Integer, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Integer, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Integer> _integer(
			String name,
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
		return this._integer(IntegerValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Integer, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Integer> _integer(
			String name) {
		return this._integer(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, LocalDateTime, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _localDateTime(
			ValueValidator<LocalDateTime, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, LocalDateTime, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, LocalDateTime> _localDateTime(
			String name,
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		return this._localDateTime(LocalDateTimeValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, LocalDateTime, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, LocalDateTime> _localDateTime(
			String name) {
		return this._localDateTime(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, LocalTime, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _localTime(
			ValueValidator<LocalTime, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, LocalTime, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, LocalTime> _localTime(
			String name,
			Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
		return this._localTime(LocalTimeValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, LocalTime, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, LocalTime> _localTime(
			String name) {
		return this._localTime(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Long, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _long(
			ValueValidator<Long, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Long, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Long> _long(
			String name, Function<LongConstraint<Arguments1<Long>>, LongConstraint<Arguments1<Long>>> constraints) {
		return this._long(LongValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Long, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Long> _long(
			String name) {
		return this._long(name, Function.identity());
	}

	public <T1, T2> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T1, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T2> _object(
			ValueValidator<T1, T2> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _object(
			String name, Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return this._object(ObjectValidatorBuilder.of(name, constraints).build());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _object(
			String name) {
		return this._object(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, OffsetDateTime, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _offsetDateTime(
			ValueValidator<OffsetDateTime, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, OffsetDateTime, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, OffsetDateTime> _offsetDateTime(
			String name,
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		return this._offsetDateTime(OffsetDateTimeValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, OffsetDateTime, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, OffsetDateTime> _offsetDateTime(
			String name) {
		return this._offsetDateTime(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Short, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _short(
			ValueValidator<Short, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Short, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Short> _short(
			String name, Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		return this._short(ShortValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Short, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Short> _short(
			String name) {
		return this._short(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, String, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _string(
			ValueValidator<String, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, String, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, String> _string(
			String name,
			Function<CharSequenceConstraint<Arguments1<String>, String>, CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return this._string(StringValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, String, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, String> _string(
			String name) {
		return this._string(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, YearMonth, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _yearMonth(
			ValueValidator<YearMonth, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, YearMonth, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, YearMonth> _yearMonth(
			String name,
			Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
		return this._yearMonth(YearMonthValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, YearMonth, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, YearMonth> _yearMonth(
			String name) {
		return this._yearMonth(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Year, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _year(
			ValueValidator<Year, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Year, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Year> _year(
			String name, Function<YearConstraint<Arguments1<Year>>, YearConstraint<Arguments1<Year>>> constraints) {
		return this._year(YearValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Year, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, Year> _year(
			String name) {
		return this._year(name, Function.identity());
	}

	public <T> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, ZonedDateTime, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, T> _zonedDateTime(
			ValueValidator<ZonedDateTime, T> validator) {
		return new Arguments16ValidatorBuilder<>(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8,
				this.v9, this.v10, this.v11, this.v12, this.v13, this.v14, this.v15, validator);
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, ZonedDateTime, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, ZonedDateTime> _zonedDateTime(
			String name,
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		return this._zonedDateTime(ZonedDateTimeValidatorBuilder.of(name, constraints).build());
	}

	public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, ZonedDateTime, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, ZonedDateTime> _zonedDateTime(
			String name) {
		return this._zonedDateTime(name, Function.identity());
	}

	public <X> Arguments15Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, X> apply(
			Function15<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? super R10, ? super R11, ? super R12, ? super R13, ? super R14, ? super R15, ? extends X> f) {
		return ArgumentsValidators
			.split(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11,
					this.v12, this.v13, this.v14, this.v15)
			.apply(f);
	}

}
