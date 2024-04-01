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
package am.ik.yavi.validator;

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
import am.ik.yavi.builder.Arguments1ValidatorBuilder;
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

/**
 * YAVI Validator Builder
 *
 * @since 0.14.0
 */
public final class Yavi {

	/**
	 * Buider for ArgumentsBuilder<br>
	 * <br>
	 * <strong>Example:</strong>
	 *
	 * <pre>
	 * <code>
	 * Arguments3Validator<String, String, Integer, Car> validator = Yavi.arguments()
	 * 	._string("manufacturer", c -> c.notNull())
	 * 	._string("licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
	 * 	._integer("seatCount", c -> c.greaterThanOrEqual(2))
	 * 	.apply(Car::new);
	 * </code>
	 * </pre>
	 *
	 * @return {@code YaviArguments} builder
	 */
	public static YaviArguments arguments() {
		return new YaviArguments();
	}

	private Yavi() {

	}

	public static class YaviArguments {

		private YaviArguments() {

		}

		public Arguments1ValidatorBuilder<BigDecimal> _bigDecimal(String name,
				Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					BigDecimalValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<BigDecimal> _bigDecimal(String name) {
			return this._bigDecimal(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<BigInteger> _bigInteger(String name,
				Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					BigIntegerValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<BigInteger> _bigInteger(String name) {
			return this._bigInteger(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<Boolean> _boolean(String name,
				Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					BooleanValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<Boolean> _boolean(String name) {
			return this._boolean(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<Double> _double(String name,
				Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					DoubleValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<Double> _double(String name) {
			return this._double(name, Function.identity());
		}

		public <E extends Enum<E>> Arguments1ValidatorBuilder<E> _enum(String name,
				Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					EnumValidatorBuilder.of(name, constraints).build());
		}

		public <E extends Enum<E>> Arguments1ValidatorBuilder<E> _enum(String name) {
			return this._enum(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<Float> _float(String name,
				Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					FloatValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<Float> _float(String name) {
			return this._float(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<Instant> _instant(String name,
				Function<InstantConstraint<Arguments1<Instant>>, InstantConstraint<Arguments1<Instant>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					InstantValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<Instant> _instant(String name) {
			return this._instant(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<Integer> _integer(String name,
				Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					IntegerValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<Integer> _integer(String name) {
			return this._integer(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<LocalDateTime> _localDateTime(String name,
				Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					LocalDateTimeValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<LocalDateTime> _localDateTime(String name) {
			return this._localDateTime(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<LocalTime> _localTime(String name,
				Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					LocalTimeValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<LocalTime> _localTime(String name) {
			return this._localTime(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<Long> _long(String name,
				Function<LongConstraint<Arguments1<Long>>, LongConstraint<Arguments1<Long>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					LongValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<Long> _long(String name) {
			return this._long(name, Function.identity());
		}

		public <T> Arguments1ValidatorBuilder<T> _object(String name,
				Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					ObjectValidatorBuilder.of(name, constraints).build());
		}

		public <T> Arguments1ValidatorBuilder<T> _object(String name) {
			return this._object(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<OffsetDateTime> _offsetDateTime(String name,
				Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					OffsetDateTimeValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<OffsetDateTime> _offsetDateTime(String name) {
			return this._offsetDateTime(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<Short> _short(String name,
				Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					ShortValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<Short> _short(String name) {
			return this._short(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<String> _string(String name,
				Function<CharSequenceConstraint<Arguments1<String>, String>, CharSequenceConstraint<Arguments1<String>, String>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					StringValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<String> _string(String name) {
			return this._string(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<YearMonth> _yearMonth(String name,
				Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					YearMonthValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<YearMonth> _yearMonth(String name) {
			return this._yearMonth(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<Year> _year(String name,
				Function<YearConstraint<Arguments1<Year>>, YearConstraint<Arguments1<Year>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					YearValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<Year> _year(String name) {
			return this._year(name, Function.identity());
		}

		public Arguments1ValidatorBuilder<ZonedDateTime> _zonedDateTime(String name,
				Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
			return new Arguments1ValidatorBuilder<>(
					ZonedDateTimeValidatorBuilder.of(name, constraints).build());
		}

		public Arguments1ValidatorBuilder<ZonedDateTime> _zonedDateTime(String name) {
			return this._zonedDateTime(name, Function.identity());
		}
	}
}
