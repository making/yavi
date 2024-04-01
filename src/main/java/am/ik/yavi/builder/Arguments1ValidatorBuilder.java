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
import am.ik.yavi.fn.Function1;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.14.0
 */
public final class Arguments1ValidatorBuilder<A1> {

	final ValueValidator<A1, A1> v1;

	public Arguments1ValidatorBuilder(ValueValidator<A1, A1> v1) {
		this.v1 = v1;
	}

	public Arguments2ValidatorBuilder<A1, BigDecimal> _bigDecimal(String name,
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				BigDecimalValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, BigInteger> _bigInteger(String name,
			Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				BigIntegerValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, Boolean> _boolean(String name,
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				BooleanValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, Double> _double(String name,
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				DoubleValidatorBuilder.of(name, constraints).build());
	}

	public <E extends Enum<E>> Arguments2ValidatorBuilder<A1, E> _enum(String name,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				EnumValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, Float> _float(String name,
			Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				FloatValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, Instant> _instant(String name,
			Function<InstantConstraint<Arguments1<Instant>>, InstantConstraint<Arguments1<Instant>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				InstantValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, Integer> _integer(String name,
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				IntegerValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, LocalDateTime> _localDateTime(String name,
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				LocalDateTimeValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, LocalTime> _localTime(String name,
			Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				LocalTimeValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, Long> _long(String name,
			Function<LongConstraint<Arguments1<Long>>, LongConstraint<Arguments1<Long>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				LongValidatorBuilder.of(name, constraints).build());
	}

	public <T> Arguments2ValidatorBuilder<A1, T> _object(String name,
			Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				ObjectValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, OffsetDateTime> _offsetDateTime(String name,
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				OffsetDateTimeValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, Short> _short(String name,
			Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				ShortValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, String> _string(String name,
			Function<CharSequenceConstraint<Arguments1<String>, String>, CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				StringValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, YearMonth> _yearMonth(String name,
			Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				YearMonthValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, Year> _year(String name,
			Function<YearConstraint<Arguments1<Year>>, YearConstraint<Arguments1<Year>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				YearValidatorBuilder.of(name, constraints).build());
	}

	public Arguments2ValidatorBuilder<A1, ZonedDateTime> _zonedDateTime(String name,
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		return new Arguments2ValidatorBuilder<>(this.v1,
				ZonedDateTimeValidatorBuilder.of(name, constraints).build());
	}

	public <R> ValueValidator<A1, R> apply(Function1<A1, R> f) {
		return this.v1.andThen(f::apply);
	}

	public ValueValidator<A1, A1> get() {
		return this.v1;
	}
}