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
package am.ik.yavi.core;

import am.ik.yavi.fn.Pair;
import am.ik.yavi.message.MessageFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_NOT_NULL;

/**
 * A core component of the YAVI validation framework that validates target instances
 * against a set of constraints. This class is the primary entry point for validation
 * operations in YAVI.
 * 
 * <p>
 * A {@code Validator} instance is immutable and thread-safe, making it suitable for use
 * as a singleton. Once constructed with a specific set of validation rules, it can be
 * reused across multiple validation operations.
 * 
 * <p>
 * The validator supports several advanced features:
 * <ul>
 * <li>Collection validation - validates elements within collections</li>
 * <li>Nested validation - validates properties of complex objects</li>
 * <li>Conditional validation - applies validators conditionally based on predicates</li>
 * <li>Fail-fast mode - stops validation upon first constraint violation</li>
 * <li>Customizable error messages - supports i18n through {@link MessageFormatter}</li>
 * <li>Prefix support - allows namespace prefixing for validation in complex object
 * graphs</li>
 * </ul>
 * 
 * <p>
 * A validator instance is typically created using the
 * {@link am.ik.yavi.builder.ValidatorBuilder}, which provides a fluent API for defining
 * validation constraints.
 * 
 * <p>
 * Example usage: <pre>{@code
 * Validator<Person> validator = ValidatorBuilder.<Person>of()
 *     .constraint(Person::getName, "name", c -> c.notBlank().lessThan(20))
 *     .constraint(Person::getAge, "age", c -> c.greaterThan(0).lessThan(120))
 *     .build();
 *     
 * ConstraintViolations violations = validator.validate(person);
 * }</pre>
 *
 * @param <T> the type of the instance to validate
 * @author Toshiaki Maki
 * @see am.ik.yavi.builder.ValidatorBuilder
 * @see ConstraintViolations
 * @see Validatable
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

	/**
	 * Creates a new validator with the specified constraints, validators, and formatter
	 * settings.
	 * 
	 * <p>
	 * This constructor creates a validator with fail-fast mode disabled by default. This
	 * means all constraints will be validated even after violations are found.
	 * 
	 * <p>
	 * Note: This constructor is typically not called directly by application code.
	 * Instead, use {@link am.ik.yavi.builder.ValidatorBuilder} to create validator
	 * instances with a more convenient fluent API.
	 * @param messageKeySeparator the separator used to join message keys (e.g., ".")
	 * @param predicatesList list of constraint predicates to apply to the target object
	 * @param collectionValidators list of validators for collections within the target
	 * object
	 * @param conditionalValidators list of validators that are applied conditionally
	 * @param messageFormatter the formatter used to create violation messages
	 */
	public Validator(String messageKeySeparator, List<ConstraintPredicates<T, ?>> predicatesList,
			List<CollectionValidator<T, ?, ?>> collectionValidators,
			List<Pair<ConstraintCondition<T>, Validatable<T>>> conditionalValidators,
			MessageFormatter messageFormatter) {
		this(messageKeySeparator, predicatesList, collectionValidators, conditionalValidators, messageFormatter, false);
	}

	/**
	 * Creates a new validator with the specified constraints, validators, formatter
	 * settings, and fail-fast mode configuration.
	 * 
	 * <p>
	 * This constructor allows specifying whether the validator should operate in
	 * fail-fast mode. When fail-fast is enabled, validation stops as soon as the first
	 * constraint violation is detected. This can improve performance in cases where any
	 * validation failure is sufficient to reject the input.
	 * 
	 * <p>
	 * Note: This constructor is typically not called directly by application code.
	 * Instead, use {@link am.ik.yavi.builder.ValidatorBuilder} to create validator
	 * instances with a more convenient fluent API, and then apply
	 * {@link #failFast(boolean)} if needed.
	 * @param messageKeySeparator the separator used to join message keys (e.g., ".")
	 * @param predicatesList list of constraint predicates to apply to the target object
	 * @param collectionValidators list of validators for collections within the target
	 * object
	 * @param conditionalValidators list of validators that are applied conditionally
	 * @param messageFormatter the formatter used to create violation messages
	 * @param failFast whether to enable fail-fast mode
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

	/**
	 * Creates a new validator instance with the specified prefix applied to all
	 * constraint names.
	 * 
	 * <p>
	 * This is particularly useful when validating nested objects to provide a
	 * hierarchical naming structure for constraint violations. The prefix helps identify
	 * which part of a complex object structure contains validation errors.
	 * 
	 * <p>
	 * For example, when validating an {@code Address} object within a {@code Person}
	 * object, you might use {@code validator.prefixed("address")} to prefix all
	 * constraint violations with "address".
	 * 
	 * <p>
	 * The prefix is combined with the existing constraint names using the configured
	 * {@code messageKeySeparator}.
	 * @param prefix the prefix to apply to all constraint names
	 * @return a new validator instance with the specified prefix
	 */
	public Validator<T> prefixed(String prefix) {
		return new Validator<>(this.messageKeySeparator, this.predicatesList, this.collectionValidators,
				this.conditionalValidators, this.messageFormatter, this.failFast, prefix);
	}

	/**
	 * Creates a new validator instance with the specified fail-fast mode setting.
	 * 
	 * <p>
	 * In fail-fast mode, the validator stops validation immediately after finding the
	 * first constraint violation. This can be useful in scenarios where any validation
	 * failure is sufficient to reject the input, improving performance by avoiding
	 * unnecessary validation checks.
	 * 
	 * <p>
	 * When fail-fast mode is disabled (the default), the validator will collect all
	 * constraint violations, which is useful for providing comprehensive feedback to
	 * users.
	 * 
	 * <p>
	 * Since validator instances are immutable, this method returns a new validator
	 * instance with the same constraints and settings, but with the specified fail-fast
	 * mode.
	 *
	 * <p>
	 * Example usage: <pre>{@code
	 * // Create a validator with fail-fast enabled
	 * Validator<Person> failFastValidator = validator.failFast(true);
	 * 
	 * // Create a validator with fail-fast disabled
	 * Validator<Person> completeValidator = validator.failFast(false);
	 * }</pre>
	 * @param failFast whether to enable fail-fast mode
	 * @return a new validator instance with the specified fail-fast mode
	 * @since 0.8.0
	 * @see #isFailFast()
	 */
	@Override
	public Validator<T> failFast(boolean failFast) {
		return new Validator<>(this.messageKeySeparator, this.predicatesList, this.collectionValidators,
				this.conditionalValidators, this.messageFormatter, failFast, this.prefix);
	}

	/**
	 * Returns whether this validator operates in fail-fast mode.
	 * 
	 * <p>
	 * When a validator is in fail-fast mode, it will stop validation as soon as it
	 * encounters the first constraint violation, rather than collecting all violations.
	 * This can improve performance in scenarios where any validation failure is
	 * sufficient to reject the input.
	 * @return {@code true} if this validator is configured to operate in fail-fast mode,
	 * {@code false} otherwise
	 * @see #failFast(boolean)
	 */
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

	/**
	 * Validates the target object and returns any constraint violations detected.
	 * 
	 * <p>
	 * This method performs validation against all constraints defined in this validator
	 * for the given target object. The validation process includes:
	 * <ul>
	 * <li>Basic property validation using the defined constraint predicates</li>
	 * <li>Collection element validation (if any collection validators are defined)</li>
	 * <li>Conditional validation (if any conditional validators are defined)</li>
	 * </ul>
	 * 
	 * <p>
	 * If fail-fast mode is enabled, the validation will stop upon the first violated
	 * constraint. Otherwise, it will collect all constraint violations.
	 * 
	 * <p>
	 * The locale parameter is used for message formatting of constraint violation
	 * messages. The constraint context provides additional information that can be used
	 * by conditional validators.
	 * @param target the object to validate, must not be null
	 * @param locale the locale for message formatting
	 * @param constraintContext additional context for validation
	 * @return constraint violations detected during validation, or an empty container if
	 * no violations
	 * @throws IllegalArgumentException if target is null
	 */
	@Override
	public ConstraintViolations validate(T target, Locale locale, ConstraintContext constraintContext) {
		return this.validate(target, "", -1, locale, constraintContext);
	}

	/**
	 * Returns an applicative validator based on this validator.
	 * 
	 * <p>
	 * An applicative validator provides a functional programming style approach to
	 * validation, allowing the composition of validation results using applicative
	 * functors. This enables validating an object and transforming the result in a fluent
	 * style.
	 * 
	 * <p>
	 * The returned applicative validator shares the same validation rules as this
	 * validator but provides additional methods for composing validation results with
	 * functions.
	 *
	 * <p>
	 * Example usage: <pre>{@code
	 * // Create an applicative validator
	 * ApplicativeValidator<Person> applicative = validator.applicative();
	 * 
	 * // Validate and get a Validated result
	 * Validated<Person> result = applicative.validate(person);
	 * 
	 * // Check if validation passed and get the value
	 * if (result.isValid()) {
	 *     Person validPerson = result.value();
	 *     // Do something with the valid person
	 * } else {
	 *     ConstraintViolations violations = result.errors();
	 *     // Handle validation errors
	 * }
	 * 
	 * // Combining multiple validations
	 * Validated<Country> countryResult = Country.validator().applicative().validate(country);
	 * Validated<String> streetResult = streetValidator.validate(street);
	 * Validated<PhoneNumber> phoneResult = PhoneNumber.validator().applicative().validate(phoneNumber);
	 * 
	 * // Combine results with a constructor to create a composite object
	 * Validated<Address> addressResult = countryResult
	 *     .combine(streetResult)
	 *     .combine(phoneResult)
	 *     .apply(Address::new);
	 * }</pre>
	 * @return an applicative validator that shares the same validation rules as this
	 * validator
	 * @see ApplicativeValidator
	 * @see Validated
	 */
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
					final ConstraintViolation violation = ConstraintViolation.builder()
						.name(name)
						.messageKey(constraintPredicate.messageKey())
						.defaultMessageFormat(constraintPredicate.defaultMessageFormat())
						.argsWithPrependedNameAndAppendedViolatedValue(args, violatedValue)
						.messageFormatter(this.messageFormatter)
						.locale(locale)
						.build();
					violations.add(violation);
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
		return ConstraintViolation.builder()
			.name(name)
			.messageKey(OBJECT_NOT_NULL.messageKey())
			.defaultMessageFormat(OBJECT_NOT_NULL.defaultMessageFormat())
			.argsWithPrependedName()
			.messageFormatter(this.messageFormatter)
			.locale(locale)
			.build();
	}

}
