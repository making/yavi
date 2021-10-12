/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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
import java.util.function.Supplier;

import am.ik.yavi.fn.Either;
import am.ik.yavi.fn.Pair;
import am.ik.yavi.message.MessageFormatter;

/**
 * Validates the target instances.
 *
 * A <code>Validator</code> instance is immutable and can be used as a singleton.
 *
 * @param <T> the type of the instance to validate
 * @author Toshiaki Maki
 */
public class Validator<T> implements ValidatorSubset<T> {
	private final List<CollectionValidator<T, ?, ?>> collectionValidators;

	private final List<Pair<ConstraintCondition<T>, ValidatorSubset<T>>> conditionalValidators;

	private final MessageFormatter messageFormatter;

	private final String messageKeySeparator;

	private final List<ConstraintPredicates<T, ?>> predicatesList;

	private final String prefix;

	private final boolean failFast;

	private final EitherValidator<T> eitherValidator = ValidatorSubset.super.either();

	private final ApplicativeValidator<T> applicativeValidator = ValidatorSubset.super.applicative();

	public Validator(String messageKeySeparator,
			List<ConstraintPredicates<T, ?>> predicatesList,
			List<CollectionValidator<T, ?, ?>> collectionValidators,
			List<Pair<ConstraintCondition<T>, ValidatorSubset<T>>> conditionalValidators,
			MessageFormatter messageFormatter) {
		this(messageKeySeparator, predicatesList, collectionValidators,
				conditionalValidators, messageFormatter, false);
	}

	/**
	 * @since 0.8.0
	 */
	public Validator(String messageKeySeparator,
			List<ConstraintPredicates<T, ?>> predicatesList,
			List<CollectionValidator<T, ?, ?>> collectionValidators,
			List<Pair<ConstraintCondition<T>, ValidatorSubset<T>>> conditionalValidators,
			MessageFormatter messageFormatter, boolean failFast) {
		this(messageKeySeparator, predicatesList, collectionValidators,
				conditionalValidators, messageFormatter, failFast, "");
	}

	private Validator(String messageKeySeparator,
			List<ConstraintPredicates<T, ?>> predicatesList,
			List<CollectionValidator<T, ?, ?>> collectionValidators,
			List<Pair<ConstraintCondition<T>, ValidatorSubset<T>>> conditionalValidators,
			MessageFormatter messageFormatter, boolean failFast, String prefix) {
		this.messageKeySeparator = messageKeySeparator;
		this.predicatesList = Collections.unmodifiableList(predicatesList);
		this.collectionValidators = Collections.unmodifiableList(collectionValidators);
		this.conditionalValidators = Collections.unmodifiableList(conditionalValidators);
		this.messageFormatter = messageFormatter;
		this.failFast = failFast;
		this.prefix = (prefix == null || prefix.isEmpty()
				|| prefix.endsWith(this.messageKeySeparator)) ? prefix
						: prefix + this.messageKeySeparator;
	}

	public Validator<T> prefixed(String prefix) {
		return new Validator<>(this.messageKeySeparator, this.predicatesList,
				this.collectionValidators, this.conditionalValidators,
				this.messageFormatter, this.failFast, prefix);
	}

	/**
	 * Set whether to enable fail fast mode. If enabled, Validator returns from the
	 * current validation as soon as the first constraint violation occurs.
	 *
	 * @param failFast whether to enable fail fast mode
	 * @since 0.8.0
	 */
	public Validator<T> failFast(boolean failFast) {
		return new Validator<>(this.messageKeySeparator, this.predicatesList,
				this.collectionValidators, this.conditionalValidators,
				this.messageFormatter, failFast, this.prefix);
	}

	/**
	 * This method is supposed to be used only internally.
	 *
	 * @param action callback per <code>ConstraintPredicates</code>.
	 */
	public void forEachPredicates(Consumer<ConstraintPredicates<T, ?>> action) {
		this.predicatesList.forEach(action);
	}

	/**
	 * This method is supposed to be used only internally.
	 *
	 * @param action callback per <code>CollectionValidator</code>.
	 */
	public void forEachCollectionValidator(
			Consumer<CollectionValidator<T, ?, ?>> action) {
		this.collectionValidators.forEach(action);
	}

	/**
	 * This method is supposed to be used only internally.
	 *
	 * @param action callback per <code>Pair<ConstraintCondition<T>, Validator<T>></code>.
	 */
	public void forEachConditionalValidator(
			Consumer<Pair<ConstraintCondition<T>, ValidatorSubset<T>>> action) {
		this.conditionalValidators.forEach(action);
	}

	@Override
	public ConstraintViolations validate(T target, Locale locale,
			ConstraintGroup constraintGroup) {
		return this.validate(target, "", -1, locale, constraintGroup);
	}

	@Override
	public EitherValidator<T> either() {
		return this.eitherValidator;
	}

	@Override
	public ApplicativeValidator<T> applicative() {
		return this.applicativeValidator;
	}

	/**
	 * Deprecated in favor of {@code either().validate(target)}
	 *
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
	@Deprecated
	public Either<ConstraintViolations, T> validateToEither(T target) {
		return this.eitherValidator.validate(target);
	}

	/**
	 * Deprecated in favor of {@code either().validate(target, constraintGroup)}
	 *
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
	@Deprecated
	public Either<ConstraintViolations, T> validateToEither(T target,
			ConstraintGroup constraintGroup) {
		return this.eitherValidator.validate(target, constraintGroup);
	}

	/**
	 * Deprecated in favor of {@code either().validate(target, locale)}
	 *
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
	@Deprecated
	public Either<ConstraintViolations, T> validateToEither(T target, Locale locale) {
		return this.either().validate(target, locale);
	}

	/**
	 * Deprecated in favor of {@code either().validate(target, locale, constraintGroup)}
	 *
	 * Validates all constraints on {@code target} and returns {@code Either} object that
	 * has constraint violations on the left or validated object on the right. <br>
	 *
	 * @param target target to validate
	 * @return either object that has constraint violations on the left or validated
	 * object on the right
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	@Deprecated
	public Either<ConstraintViolations, T> validateToEither(T target, Locale locale,
			ConstraintGroup constraintGroup) {
		return this.either().validate(target, locale, constraintGroup);
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

	private Object[] pad(String name, Object[] args, ViolatedValue violatedValue) {
		Object[] pad = new Object[args.length + 2];
		pad[0] = name;
		System.arraycopy(args, 0, pad, 1, args.length);
		pad[pad.length - 1] = violatedValue.value();
		return pad;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ConstraintViolations validate(T target, String collectionName, int index,
			Locale locale, ConstraintGroup constraintGroup) {
		if (target == null) {
			throw new IllegalArgumentException("target must not be null");
		}
		final ConstraintViolations violations = new ConstraintViolations();
		for (ConstraintPredicates<T, ?> predicates : this.predicatesList) {
			if (predicates instanceof NestedConstraintPredicates) {
				final NestedConstraintPredicates<T, ?, ?> nested = (NestedConstraintPredicates<T, ?, ?>) predicates;
				final Object nestedValue = nested.nestedValue(target);
				if (nestedValue == null) {
					continue;
				}
			}
			for (ConstraintPredicate<?> constraintPredicate : predicates.predicates()) {
				final Object v = predicates.toValue().apply(target);
				if (v == null && constraintPredicate.nullValidity().skipNull()) {
					continue;
				}
				final Optional<ViolatedValue> violated = ((ConstraintPredicate) constraintPredicate)
						.violatedValue(v);
				if (violated.isPresent()) {
					final ViolatedValue violatedValue = violated.get();
					final String name = this.prefix
							+ this.indexedName(predicates.name(), collectionName, index);
					final Supplier<Object[]> argsSupplier = constraintPredicate.args();
					final Object[] args = (argsSupplier instanceof ViolatedArguments)
							? ((ViolatedArguments) argsSupplier)
									.arguments(violatedValue.value())
							: argsSupplier.get();
					violations.add(new ConstraintViolation(name,
							constraintPredicate.messageKey(),
							constraintPredicate.defaultMessageFormat(),
							pad(name, args, violatedValue), this.messageFormatter,
							locale));
					if (this.failFast) {
						return violations;
					}
				}
			}
		}
		for (CollectionValidator<T, ?, ?> collectionValidator : this.collectionValidators) {
			final Collection collection = collectionValidator.toCollection()
					.apply(target);
			if (collection != null) {
				final Validator validator = collectionValidator.validator()
						.failFast(this.failFast);
				int i = 0;
				for (Object element : collection) {
					if (element != null) {
						final String nestedName = this.indexedName(
								collectionValidator.name(), collectionName, index);
						final ConstraintViolations v = validator.validate(element,
								nestedName, i++, locale, constraintGroup);
						violations.addAll(v);
						if (this.failFast) {
							return violations;
						}
					}
				}
			}
		}
		for (Pair<ConstraintCondition<T>, ValidatorSubset<T>> pair : this.conditionalValidators) {
			final ConstraintCondition<T> condition = pair.first();
			if (condition.test(target, constraintGroup)) {
				final ValidatorSubset<T> validator = pair.second();
				final ConstraintViolations constraintViolations = validator
						.validate(target, locale, constraintGroup);
				for (ConstraintViolation violation : constraintViolations) {
					final ConstraintViolation renamed = violation
							.rename(name -> this.prefix
									+ this.indexedName(name, collectionName, index));
					violations.add(renamed);
					if (this.failFast) {
						return violations;
					}
				}
			}
		}
		return violations;
	}
}
