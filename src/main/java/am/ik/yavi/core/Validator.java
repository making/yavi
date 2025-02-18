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
package am.ik.yavi.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import am.ik.yavi.fn.Pair;
import am.ik.yavi.message.MessageFormatter;

import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_NOT_NULL;

/**
 * Validates the target instances. A <code>Validator</code> instance is immutable and can
 * be used as a singleton.
 *
 * @param <T> the type of the instance to validate
 * @author Toshiaki Maki
 */
public class Validator<T> implements Validatable<T> {

	private final List<CollectionValidator<T, ?, ?>> collectionValidators;

	private final List<Pair<ConstraintCondition<T>, Validatable<T>>> conditionalValidators;

	private final MessageFormatter messageFormatter;

	private final String messageKeySeparator;

	private final List<ConstraintPredicates<T, ?>> predicatesList;

	private final String prefix;

	private final boolean failFast;

	private final ApplicativeValidator<T> applicativeValidator = Validatable.super.applicative();

	public Validator(String messageKeySeparator, List<ConstraintPredicates<T, ?>> predicatesList,
			List<CollectionValidator<T, ?, ?>> collectionValidators,
			List<Pair<ConstraintCondition<T>, Validatable<T>>> conditionalValidators,
			MessageFormatter messageFormatter) {
		this(messageKeySeparator, predicatesList, collectionValidators, conditionalValidators, messageFormatter, false);
	}

	/**
	 * @since 0.8.0
	 */
	public Validator(String messageKeySeparator, List<ConstraintPredicates<T, ?>> predicatesList,
			List<CollectionValidator<T, ?, ?>> collectionValidators,
			List<Pair<ConstraintCondition<T>, Validatable<T>>> conditionalValidators, MessageFormatter messageFormatter,
			boolean failFast) {
		this(messageKeySeparator, predicatesList, collectionValidators, conditionalValidators, messageFormatter,
				failFast, "");
	}

	private Validator(String messageKeySeparator, List<ConstraintPredicates<T, ?>> predicatesList,
			List<CollectionValidator<T, ?, ?>> collectionValidators,
			List<Pair<ConstraintCondition<T>, Validatable<T>>> conditionalValidators, MessageFormatter messageFormatter,
			boolean failFast, String prefix) {
		this.messageKeySeparator = messageKeySeparator;
		this.predicatesList = Collections.unmodifiableList(predicatesList);
		this.collectionValidators = Collections.unmodifiableList(collectionValidators);
		this.conditionalValidators = Collections.unmodifiableList(conditionalValidators);
		this.messageFormatter = messageFormatter;
		this.failFast = failFast;
		this.prefix = (prefix == null || prefix.isEmpty() || prefix.endsWith(this.messageKeySeparator)) ? prefix
				: prefix + this.messageKeySeparator;
	}

	public Validator<T> prefixed(String prefix) {
		return new Validator<>(this.messageKeySeparator, this.predicatesList, this.collectionValidators,
				this.conditionalValidators, this.messageFormatter, this.failFast, prefix);
	}

	/**
	 * Set whether to enable fail fast mode. If enabled, Validator returns from the
	 * current validation as soon as the first constraint violation occurs.
	 * @param failFast whether to enable fail fast mode
	 * @since 0.8.0
	 */
	@Override
	public Validator<T> failFast(boolean failFast) {
		return new Validator<>(this.messageKeySeparator, this.predicatesList, this.collectionValidators,
				this.conditionalValidators, this.messageFormatter, failFast, this.prefix);
	}

	@Override
	public boolean isFailFast() {
		return failFast;
	}

	/**
	 * This method is supposed to be used only internally.
	 * @param action callback per <code>ConstraintPredicates</code>.
	 */
	public void forEachPredicates(Consumer<ConstraintPredicates<T, ?>> action) {
		this.predicatesList.forEach(action);
	}

	/**
	 * This method is supposed to be used only internally.
	 * @param action callback per <code>CollectionValidator</code>.
	 */
	public void forEachCollectionValidator(Consumer<CollectionValidator<T, ?, ?>> action) {
		this.collectionValidators.forEach(action);
	}

	/**
	 * This method is supposed to be used only internally.
	 * @param action callback per
	 * {@code Pair&lt;ConstraintCondition&lt;T&gt;, Validator&lt;T&gt;&gt;}.
	 */
	public void forEachConditionalValidator(Consumer<Pair<ConstraintCondition<T>, Validatable<T>>> action) {
		this.conditionalValidators.forEach(action);
	}

	@Override
	public ConstraintViolations validate(T target, Locale locale, ConstraintContext constraintContext) {
		return this.validate(target, "", -1, locale, constraintContext);
	}

	@Override
	public ApplicativeValidator<T> applicative() {
		return this.applicativeValidator;
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
	private ConstraintViolations validate(T target, String collectionName, int index, Locale locale,
			ConstraintContext constraintContext) {
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
				final Optional<ViolatedValue> violated = ((ConstraintPredicate) constraintPredicate).violatedValue(v);
				if (violated.isPresent()) {
					final ViolatedValue violatedValue = violated.get();
					final String name = this.prefix + this.indexedName(predicates.name(), collectionName, index);
					final Supplier<Object[]> argsSupplier = constraintPredicate.args();
					final Object[] args = (argsSupplier instanceof ViolatedArguments)
							? ((ViolatedArguments) argsSupplier).arguments(violatedValue.value()) : argsSupplier.get();
					violations.add(new ConstraintViolation(name, constraintPredicate.messageKey(),
							constraintPredicate.defaultMessageFormat(), pad(name, args, violatedValue),
							this.messageFormatter, locale));
					if (this.failFast || ((predicates instanceof NestedConstraintPredicates)
							&& ((NestedConstraintPredicates) predicates).isFailFast())) {
						return violations;
					}
				}
			}
		}
		for (CollectionValidator<T, ?, ?> collectionValidator : this.collectionValidators) {
			final Collection collection = collectionValidator.toCollection().apply(target);
			if (collection != null) {
				final Validator validator = this.failFast ? collectionValidator.validator().failFast(true)
						: collectionValidator.validator();
				int i = 0;
				for (Object element : collection) {
					final String nestedName = this.indexedName(collectionValidator.name(), collectionName, index);
					if (element != null) {
						final ConstraintViolations v = validator.validate(element, nestedName, i++, locale,
								constraintContext);
						violations.addAll(v);
					}
					else {
						final String name = this.indexedName("", nestedName, i++);
						final ConstraintViolation v = notNullViolation(name, locale);
						violations.add(v);
					}
					if (!violations.isEmpty() && (this.failFast || validator.isFailFast())) {
						return violations;
					}
				}
			}
		}
		for (Pair<ConstraintCondition<T>, Validatable<T>> pair : this.conditionalValidators) {
			final ConstraintCondition<T> condition = pair.first();
			if (condition.test(target, constraintContext)) {
				final Validatable<T> validator = this.failFast ? pair.second().failFast(true) : pair.second();
				final ConstraintViolations constraintViolations = validator.validate(target, locale, constraintContext);
				for (ConstraintViolation violation : constraintViolations) {
					final ConstraintViolation renamed = violation
						.rename(name -> this.prefix + this.indexedName(name, collectionName, index));
					violations.add(renamed);
					if (!violations.isEmpty() && (this.failFast || validator.isFailFast())) {
						return violations;
					}
				}
			}
		}
		return violations;
	}

	private ConstraintViolation notNullViolation(String name, Locale locale) {
		return new ConstraintViolation(name, OBJECT_NOT_NULL.messageKey(), OBJECT_NOT_NULL.defaultMessageFormat(),
				pad(name, new Object[] {}, new ViolatedValue(null)), this.messageFormatter, locale);
	}

}
