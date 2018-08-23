/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import am.ik.yavi.fn.Either;
import am.ik.yavi.message.MessageFormatter;

public final class Validator<T> {
	final List<ConstraintPredicates<T, ?>> predicatesList;
	private final MessageFormatter messageFormatter;

	public Validator(List<ConstraintPredicates<T, ?>> predicatesList,
			MessageFormatter messageFormatter) {
		this.predicatesList = Collections.unmodifiableList(predicatesList);
		this.messageFormatter = messageFormatter;
	}

	public static <T> ValidatorBuilder<T> builder() {
		return new ValidatorBuilder<>();
	}

	public static <T> ValidatorBuilder<T> builder(Class<T> clazz) {
		return new ValidatorBuilder<>();
	}

	@SuppressWarnings("unchecked")
	public final ConstraintViolations validate(T target) {
		ConstraintViolations violations = new ConstraintViolations();
		for (ConstraintPredicates<T, ?> predicates : this.predicatesList) {
			if (predicates instanceof NestedConstraintPredicates) {
				NestedConstraintPredicates<T, ?, ?> nested = (NestedConstraintPredicates<T, ?, ?>) predicates;
				Object nestedValue = nested.nestedValue(target);
				if (nestedValue == null) {
					continue;
				}
			}
			for (ConstraintPredicate<?> constraintPredicate : predicates.predicates()) {
				Object v = predicates.toValue().apply(target);
				Predicate<Object> predicate = (Predicate<Object>) constraintPredicate
						.predicate();
				if (v == null && constraintPredicate.nullValidity().skipNull()) {
					continue;
				}
				if (!predicate.test(v)) {
					String name = predicates.name();
					Object[] args = constraintPredicate.args().get();
					violations.add(new ConstraintViolation(name,
							constraintPredicate.messageKey(),
							constraintPredicate.defaultMessageFormat(),
							pad(name, args, v), v, this.messageFormatter));
				}
			}
		}
		return violations;
	}

	public final Either<ConstraintViolations, T> validateToEither(T target) {
		ConstraintViolations violations = this.validate(target);
		if (violations.isValid()) {
			return Either.right(target);
		}
		else {
			return Either.left(violations);
		}
	}

	private Object[] pad(String name, Object[] args, Object value) {
		Object[] pad = new Object[args.length + 2];
		pad[0] = name;
		System.arraycopy(args, 0, pad, 1, args.length);
		pad[pad.length - 1] = value;
		return pad;
	}
}
