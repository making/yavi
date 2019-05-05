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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.fn.Either;
import am.ik.yavi.fn.Pair;
import am.ik.yavi.message.MessageFormatter;

/**
 * Validates the target instances. must be thread safe.
 * 
 * @param <T> the type of the instance to validate
 * @author Toshiaki Maki
 */
public final class Validator<T> {
	private final List<ConstraintPredicates<T, ?>> predicatesList;
	private final String messageKeySeparator;
	private final List<CollectionValidator<T, ?, ?>> collectionValidators;
	private final List<Pair<ConstraintCondition<T>, Validator<T>>> conditionalValidators;
	private final MessageFormatter messageFormatter;

	public Validator(String messageKeySeparator,
			List<ConstraintPredicates<T, ?>> predicatesList,
			List<CollectionValidator<T, ?, ?>> collectionValidators,
			List<Pair<ConstraintCondition<T>, Validator<T>>> conditionalValidators,
			MessageFormatter messageFormatter) {
		this.messageKeySeparator = messageKeySeparator;
		this.predicatesList = Collections.unmodifiableList(predicatesList);
		this.collectionValidators = Collections.unmodifiableList(collectionValidators);
		this.conditionalValidators = conditionalValidators;
		this.messageFormatter = messageFormatter;
	}

	/**
	 * Deprecated in favor of {@link ValidatorBuilder#of()}
	 *
	 * Builder method to build {@code Validator} instance.
	 * @param <T> the type of the instance to validate
	 * @return builder instance
	 */
	@Deprecated
	public static <T> ValidatorBuilder<T> builder() {
		return ValidatorBuilder.of();
	}

	/**
	 * Deprecated in favor of {@link ValidatorBuilder#of(Class)}
	 * 
	 * Builder method to build {@code Validator} instance.
	 * @param clazz the class of the instance to validate
	 * @param <T> the type of the instance to validate
	 * @return builder instance
	 */
	@Deprecated
	public static <T> ValidatorBuilder<T> builder(
			@SuppressWarnings("unused") Class<T> clazz) {
		return ValidatorBuilder.of(clazz);
	}

	/**
	 * Validates all constraints on {@code target}. <br>
	 * {@code Locale.getDefault()} is used to locate the violation messages.
	 * {@code ConstraintGroup.DEFAULT} is used as a constraint group.
	 * 
	 * @param target target to validate
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	public ConstraintViolations validate(T target) {
		return this.validate(target, Locale.getDefault(), ConstraintGroup.DEFAULT);
	}

	/**
	 * Validates all constraints on {@code target}. <br>
	 * {@code Locale.getDefault()} is used to locate the violation messages.
	 *
	 * @param target target to validate
	 * @param constraintGroup constraint group to validate
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	public ConstraintViolations validate(T target, ConstraintGroup constraintGroup) {
		return this.validate(target, Locale.getDefault(), constraintGroup);
	}

	/**
	 * Validates all constraints on {@code target}.<br>
	 * {@code ConstraintGroup.DEFAULT} is used as a constraint group.
	 * 
	 * @param target target to validate
	 * @param locale the locale targeted for the violation messages.
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	public ConstraintViolations validate(T target, Locale locale) {
		return this.validate(target, locale, ConstraintGroup.DEFAULT);
	}

	/**
	 * Validates all constraints on {@code target}.
	 *
	 * @param target target to validate
	 * @param locale the locale targeted for the violation messages.
	 * @param constraintGroup constraint group to validate
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	public ConstraintViolations validate(T target, Locale locale,
			ConstraintGroup constraintGroup) {
		return this.validate(target, "", -1, locale, constraintGroup);
	}

	public void forEachPredicates(Consumer<ConstraintPredicates<T, ?>> action) {
		this.predicatesList.forEach(action);
	}

	@SuppressWarnings("unchecked")
	private ConstraintViolations validate(T target, String collectionName, int index,
			Locale locale, ConstraintGroup constraintGroup) {
		if (target == null) {
			throw new IllegalArgumentException("target must not be null");
		}
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
				if (v == null && constraintPredicate.nullValidity().skipNull()) {
					continue;
				}
				Optional<ViolatedValue> violated = ((ConstraintPredicate) constraintPredicate)
						.violatedValue(v);
				violated.ifPresent(violatedValue -> {
					String name = this.indexedName(predicates.name(), collectionName,
							index);
					Object[] args = constraintPredicate.args().get();
					violations.add(new ConstraintViolation(name,
							constraintPredicate.messageKey(),
							constraintPredicate.defaultMessageFormat(),
							pad(name, args, violatedValue), this.messageFormatter,
							locale));
				});
			}
		}
		this.collectionValidators.forEach(collectionValidator -> {
			Collection collection = collectionValidator.toCollection().apply(target);
			if (collection != null) {
				Validator validator = collectionValidator.validator();
				int i = 0;
				for (Object element : collection) {
					if (element != null) {
						String nestedName = this.indexedName(collectionValidator.name(),
								collectionName, index);
						ConstraintViolations v = validator.validate(element, nestedName,
								i++, locale, constraintGroup);
						violations.addAll(v);
					}
				}
			}
		});
		this.conditionalValidators.forEach(pair -> {
			ConstraintCondition<T> condition = pair.first();
			if (condition.test(target, constraintGroup)) {
				Validator<T> validator = pair.second();
				ConstraintViolations v = validator.validate(target, locale);
				violations.addAll(v);
			}
		});
		return violations;
	}

	private String indexedName(String name, String collectionName, int index) {
		if (index < 0) {
			return name;
		}
		if (name.isEmpty()) {
			return collectionName + "[" + index + "]";
		}
		return collectionName + "[" + index + "]" + this.messageKeySeparator + name;
	}

	/**
	 * Validates all constraints on {@code target} and returns {@code Either} object that
	 * has constraint violations on the left or validated object on the right. <br>
	 * {@code Locale.getDefault()} is used to locate the violation messages.<br>
	 * {@code ConstraintGroup.DEFAULT} is used as a constraint group.
	 *
	 * @param target target to validate
	 * @return either object that has constraint violations on the left or validated
	 * object on the right
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	public Either<ConstraintViolations, T> validateToEither(T target) {
		return this.validateToEither(target, Locale.getDefault(),
				ConstraintGroup.DEFAULT);
	}

	/**
	 * Validates all constraints on {@code target} and returns {@code Either} object that
	 * has constraint violations on the left or validated object on the right. <br>
	 * {@code Locale.getDefault()} is used to locate the violation messages.
	 *
	 * @param target target to validate
	 * @param constraintGroup constraint group to validate
	 * @return either object that has constraint violations on the left or validated
	 * object on the right
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	public Either<ConstraintViolations, T> validateToEither(T target,
			ConstraintGroup constraintGroup) {
		return this.validateToEither(target, Locale.getDefault(), constraintGroup);
	}

	/**
	 * Validates all constraints on {@code target} and returns {@code Either} object that
	 * has constraint violations on the left or validated object on the right. <br>
	 * {@code ConstraintGroup.DEFAULT} is used as a constraint group.
	 *
	 * @param target target to validate
	 * @param locale the locale targeted for the violation messages.
	 * @return either object that has constraint violations on the left or validated
	 * object on the right
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	public Either<ConstraintViolations, T> validateToEither(T target, Locale locale) {
		return this.validateToEither(target, locale, ConstraintGroup.DEFAULT);
	}

	/**
	 * Validates all constraints on {@code target} and returns {@code Either} object that
	 * has constraint violations on the left or validated object on the right. <br>
	 * 
	 * @param target target to validate
	 * @return either object that has constraint violations on the left or validated
	 * object on the right
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	public Either<ConstraintViolations, T> validateToEither(T target, Locale locale,
			ConstraintGroup constraintGroup) {
		ConstraintViolations violations = this.validate(target, locale, constraintGroup);
		if (violations.isValid()) {
			return Either.right(target);
		}
		else {
			return Either.left(violations);
		}
	}

	private Object[] pad(String name, Object[] args, ViolatedValue violatedValue) {
		Object[] pad = new Object[args.length + 2];
		pad[0] = name;
		System.arraycopy(args, 0, pad, 1, args.length);
		pad[pad.length - 1] = violatedValue.value();
		return pad;
	}
}
