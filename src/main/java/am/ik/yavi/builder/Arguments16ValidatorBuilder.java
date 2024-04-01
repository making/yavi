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
import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.arguments.Arguments16Validator;
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
import am.ik.yavi.fn.Function16;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.14.0
 */
public final class Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16> {

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

	final ValueValidator<A14, A14> v14;

	final ValueValidator<A15, A15> v15;

	final ValueValidator<A16, A16> v16;

	public Arguments16ValidatorBuilder(ValueValidator<A1, A1> v1,
			ValueValidator<A2, A2> v2, ValueValidator<A3, A3> v3,
			ValueValidator<A4, A4> v4, ValueValidator<A5, A5> v5,
			ValueValidator<A6, A6> v6, ValueValidator<A7, A7> v7,
			ValueValidator<A8, A8> v8, ValueValidator<A9, A9> v9,
			ValueValidator<A10, A10> v10, ValueValidator<A11, A11> v11,
			ValueValidator<A12, A12> v12, ValueValidator<A13, A13> v13,
			ValueValidator<A14, A14> v14, ValueValidator<A15, A15> v15,
			ValueValidator<A16, A16> v16) {
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
		this.v16 = v16;
	}

	public <R> Arguments16Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, R> apply(
			Function16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, R> f) {
		return ArgumentsValidators.split(this.v1, this.v2, this.v3, this.v4, this.v5,
				this.v6, this.v7, this.v8, this.v9, this.v10, this.v11, this.v12,
				this.v13, this.v14, this.v15, this.v16).apply(f);
	}

}