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
package am.ik.yavi.arguments;

import java.util.List;
import java.util.Locale;

import am.ik.yavi.core.CollectionValidator;
import am.ik.yavi.core.ConstraintCondition;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.ConstraintPredicates;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import am.ik.yavi.core.ValidatorSubset;
import am.ik.yavi.fn.Either;
import am.ik.yavi.fn.Pair;
import am.ik.yavi.message.MessageFormatter;

/**
 * @since 0.3.0
 */
public final class Arguments14Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, X>
		extends
		Validator<Arguments14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14>> {
	private final Arguments14.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, X> mapper;

	public Arguments14Validator(String messageKeySeparator,
			List<ConstraintPredicates<Arguments14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14>, ?>> constraintPredicates,
			List<CollectionValidator<Arguments14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14>, ?, ?>> collectionValidators,
			List<Pair<ConstraintCondition<Arguments14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14>>, ValidatorSubset<Arguments14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14>>>> conditionalValidators,
			MessageFormatter messageFormatter,
			Arguments14.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, X> mapper) {
		super(messageKeySeparator, constraintPredicates, collectionValidators,
				conditionalValidators, messageFormatter);
		this.mapper = mapper;
	}

	public ConstraintViolations validateArgs(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6,
			A7 a7, A8 a8, A9 a9, A10 a10, A11 a11, A12 a12, A13 a13, A14 a14,
			ConstraintGroup constraintGroup) {
		return this.validate(
				Arguments.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14),
				Locale.getDefault(), constraintGroup);
	}

	public ConstraintViolations validateArgs(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6,
			A7 a7, A8 a8, A9 a9, A10 a10, A11 a11, A12 a12, A13 a13, A14 a14,
			Locale locale) {
		return this.validate(
				Arguments.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14),
				locale, ConstraintGroup.DEFAULT);
	}

	public ConstraintViolations validateArgs(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6,
			A7 a7, A8 a8, A9 a9, A10 a10, A11 a11, A12 a12, A13 a13, A14 a14,
			Locale locale, ConstraintGroup constraintGroup) {
		return this.validate(
				Arguments.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14),
				locale, constraintGroup);
	}

	public ConstraintViolations validateArgs(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6,
			A7 a7, A8 a8, A9 a9, A10 a10, A11 a11, A12 a12, A13 a13, A14 a14) {
		return this.validate(
				Arguments.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14),
				Locale.getDefault(), ConstraintGroup.DEFAULT);
	}

	public Either<ConstraintViolations, X> validateArgsToEither(A1 a1, A2 a2, A3 a3,
			A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9, A10 a10, A11 a11, A12 a12, A13 a13,
			A14 a14) {
		return this
				.validateToEither(Arguments.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10,
						a11, a12, a13, a14), Locale.getDefault(), ConstraintGroup.DEFAULT)
				.rightMap(values -> values.map(this.mapper));
	}

	public Either<ConstraintViolations, X> validateArgsToEither(A1 a1, A2 a2, A3 a3,
			A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9, A10 a10, A11 a11, A12 a12, A13 a13,
			A14 a14, ConstraintGroup constraintGroup) {
		return this
				.validateToEither(Arguments.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10,
						a11, a12, a13, a14), Locale.getDefault(), constraintGroup)
				.rightMap(values -> values.map(this.mapper));
	}

	public Either<ConstraintViolations, X> validateArgsToEither(A1 a1, A2 a2, A3 a3,
			A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9, A10 a10, A11 a11, A12 a12, A13 a13,
			A14 a14, Locale locale) {
		return this
				.validateToEither(Arguments.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10,
						a11, a12, a13, a14), locale, ConstraintGroup.DEFAULT)
				.rightMap(values -> values.map(this.mapper));
	}

	public Either<ConstraintViolations, X> validateArgsToEither(A1 a1, A2 a2, A3 a3,
			A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9, A10 a10, A11 a11, A12 a12, A13 a13,
			A14 a14, Locale locale, ConstraintGroup constraintGroup) {
		ConstraintViolations violations = this.validate(
				Arguments.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14),
				locale, constraintGroup);
		if (violations.isValid()) {
			return Either.right(Arguments
					.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14)
					.map(this.mapper));
		}
		else {
			return Either.left(violations);
		}
	}
}
