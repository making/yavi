/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.builder.chained;

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
import am.ik.yavi.arguments.Validator13;
import am.ik.yavi.builder.BigDecimalValidatorBuilder;
import am.ik.yavi.builder.BigIntegerValidatorBuilder;
import am.ik.yavi.builder.BooleanValidatorBuilder;
import am.ik.yavi.builder.DoubleValidatorBuilder;
import am.ik.yavi.builder.EnumValidatorBuilder;
import am.ik.yavi.builder.FloatValidatorBuilder;
import am.ik.yavi.builder.InstantValidatorBuilder;
import am.ik.yavi.builder.IntegerValidatorBuilder;
import am.ik.yavi.builder.LocalDateTimeValidatorBuilder;
import am.ik.yavi.builder.LocalTimeValidatorBuilder;
import am.ik.yavi.builder.LongValidatorBuilder;
import am.ik.yavi.builder.ObjectValidatorBuilder;
import am.ik.yavi.builder.OffsetDateTimeValidatorBuilder;
import am.ik.yavi.builder.ShortValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.builder.YearMonthValidatorBuilder;
import am.ik.yavi.builder.YearValidatorBuilder;
import am.ik.yavi.builder.ZonedDateTimeValidatorBuilder;
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
import am.ik.yavi.fn.Function13;

/**
 * Generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-chained-builder.sh
 *
 * @since 0.14.0
 */
public final class Validator13ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13> {

	final ValueValidator<A1, A1> v1;

	final ValueValidator<A2, A2> v2;

	final ValueValidator<A3, A3> v3;

	final ValueValidator<A4, A4> v4;

	final ValueValidator<A5, A5> v5;

	final ValueValidator<A6, A6> v6;

	final ValueValidator<A7, A7> v7;

	final ValueValidator<A8, A8> v8;

	final ValueValidator<A9, A9> v9;

	final ValueValidator<A10, A10> v10;

	final ValueValidator<A11, A11> v11;

	final ValueValidator<A12, A12> v12;

	final ValueValidator<A13, A13> v13;

	public Validator13ChainedBuilder(ValueValidator<A1, A1> v1, ValueValidator<A2, A2> v2,
			ValueValidator<A3, A3> v3, ValueValidator<A4, A4> v4,
			ValueValidator<A5, A5> v5, ValueValidator<A6, A6> v6,
			ValueValidator<A7, A7> v7, ValueValidator<A8, A8> v8,
			ValueValidator<A9, A9> v9, ValueValidator<A10, A10> v10,
			ValueValidator<A11, A11> v11, ValueValidator<A12, A12> v12,
			ValueValidator<A13, A13> v13) {
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
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, BigDecimal> _bigDecimal(
			String name,
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, BigDecimalValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, BigInteger> _bigInteger(
			String name,
			Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, BigIntegerValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, Boolean> _boolean(
			String name,
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, BooleanValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, Double> _double(
			String name,
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, DoubleValidatorBuilder.of(name, constraints).build());
	}

	public <E extends Enum<E>> Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, E> _enum(
			String name,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, EnumValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, Float> _float(
			String name,
			Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, FloatValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, Instant> _instant(
			String name,
			Function<InstantConstraint<Arguments1<Instant>>, InstantConstraint<Arguments1<Instant>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, InstantValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, Integer> _integer(
			String name,
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, IntegerValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, LocalDateTime> _localDateTime(
			String name,
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, LocalDateTimeValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, LocalTime> _localTime(
			String name,
			Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, LocalTimeValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, Long> _long(
			String name,
			Function<LongConstraint<Arguments1<Long>>, LongConstraint<Arguments1<Long>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, LongValidatorBuilder.of(name, constraints).build());
	}

	public <T> Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, T> _object(
			String name,
			Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, ObjectValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, OffsetDateTime> _offsetDateTime(
			String name,
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, OffsetDateTimeValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, Short> _short(
			String name,
			Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, ShortValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, String> _string(
			String name,
			Function<CharSequenceConstraint<Arguments1<String>, String>, CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, StringValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, YearMonth> _yearMonth(
			String name,
			Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, YearMonthValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, Year> _year(
			String name,
			Function<YearConstraint<Arguments1<Year>>, YearConstraint<Arguments1<Year>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, YearValidatorBuilder.of(name, constraints).build());
	}

	public Validator14ChainedBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, ZonedDateTime> _zonedDateTime(
			String name,
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		return new Validator14ChainedBuilder<>(this.v1, this.v2, this.v3, this.v4,
				this.v5, this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, ZonedDateTimeValidatorBuilder.of(name, constraints).build());
	}

	public <R> Validator13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R> apply(
			Function13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R> f) {
		return ArgumentsValidators
				.split(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7,
						this.v8, this.v9, this.v10, this.v11, this.v12, this.v13)
				.apply(f);
	}

}
