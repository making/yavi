/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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

import org.jspecify.annotations.Nullable;

import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_NOT_NULL;

/**
 * Standard {@link Validatable} implementation that validates a target object against a
 * set of constraint predicates, collection validators and conditional validators.
 *
 * <p>
 * A validator is normally created with {@link am.ik.yavi.builder.ValidatorBuilder}.
 * Instances are immutable and thread-safe, so they can be shared and reused. The
 * collections passed to the constructor are not copied, only wrapped as unmodifiable
 * views, so they must not be modified after construction.
 *
 * <p>
 * Supported features include:
 * <ul>
 * <li>collection element validation ({@link CollectionValidator})</li>
 * <li>nested object validation ({@link NestedValidator})</li>
 * <li>conditional validation ({@link ConstraintCondition}) and constraint groups
 * ({@link ConstraintGroup})</li>
 * <li>fail-fast mode, which returns after the first violation</li>
 * <li>localized messages through {@link MessageFormatter}</li>
 * <li>name prefixes for violations in nested object graphs</li>
 * </ul>
 *
 * <p>
 * Example: <pre>{@code
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
 * @since 0.0.1
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
	 * Creates a validator with fail-fast mode disabled.
	 *
	 * <p>
	 * Application code normally uses {@link am.ik.yavi.builder.ValidatorBuilder} instead
	 * of calling this constructor directly.
	 * @param messageKeySeparator the separator used to join message keys (e.g., ".")
	 * @param predicatesList list of constraint predicates to apply to the target object
	 * @param collectionValidators list of validators for collections within the target
	 * object
	 * @param conditionalValidators list of validators that are applied conditionally
	 * @param messageFormatter the formatter used to create violation messages
	 * @throws NullPointerException if any of the lists is {@code null}
	 */
	public Validator(String messageKeySeparator, List<ConstraintPredicates<T, ?>> predicatesList,
			List<CollectionValidator<T, ?, ?>> collectionValidators,
			List<Pair<ConstraintCondition<T>, Validatable<T>>> conditionalValidators,
			MessageFormatter messageFormatter) {
		this(messageKeySeparator, predicatesList, collectionValidators, conditionalValidators, messageFormatter, false);
	}

	/**
	 * Creates a validator with the specified fail-fast mode.
	 *
	 * <p>
	 * Application code normally uses {@link am.ik.yavi.builder.ValidatorBuilder} and
	 * {@link #failFast(boolean)} instead of calling this constructor directly.
	 * @param messageKeySeparator the separator used to join message keys (e.g., ".")
	 * @param predicatesList list of constraint predicates to apply to the target object
	 * @param collectionValidators list of validators for collections within the target
	 * object
	 * @param conditionalValidators list of validators that are applied conditionally
	 * @param messageFormatter the formatter used to create violation messages
	 * @param failFast whether to enable fail-fast mode
	 * @throws NullPointerException if any of the lists is {@code null}
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
	 * Returns a new validator that prefixes every violation name with the given prefix.
	 *
	 * <p>
	 * This is useful when the validator is applied to an object nested in another object,
	 * so that violations identify the path to the invalid property. The prefix is joined
	 * to the names with the configured message key separator. An empty prefix leaves the
	 * names unchanged.
	 *
	 * <p>
	 * Example: <pre>{@code
	 * Validator<Address> validator = ValidatorBuilder.<Address>of()
	 *     .constraint(Address::street, "street", c -> c.notBlank())
	 *     .build()
	 *     .prefixed("address");
	 * // a violation is reported with the name "address.street"
	 * }</pre>
	 * @param prefix the prefix to apply to all constraint names
	 * @return a new validator instance with the specified prefix
	 */
	public Validator<T> prefixed(String prefix) {
		return new Validator<>(this.messageKeySeparator, this.predicatesList, this.collectionValidators,
				this.conditionalValidators, this.messageFormatter, this.failFast, prefix);
	}

	/**
	 * Returns a new validator with the specified fail-fast mode. The constraints and all
	 * other settings are shared with this validator.
	 *
	 * <p>
	 * In fail-fast mode, validation returns immediately after the first constraint
	 * violation. When fail-fast mode is disabled (the default), all violations are
	 * collected.
	 *
	 * <p>
	 * Example: <pre>{@code
	 * Validator<Person> failFastValidator = validator.failFast(true);
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
	 * @return {@code true} if this validator is configured to operate in fail-fast mode,
	 * {@code false} otherwise
	 * @see #failFast(boolean)
	 */
	@Override
	public boolean isFailFast() {
		return failFast;
	}

	/**
	 * Iterates over the constraint predicates of this validator.
	 *
	 * <p>
	 * This method is intended for internal use, for example by
	 * {@link am.ik.yavi.builder.ValidatorBuilder} when nesting this validator.
	 * @param action callback per <code>ConstraintPredicates</code>.
	 */
	public void forEachPredicates(Consumer<ConstraintPredicates<T, ?>> action) {
		this.predicatesList.forEach(action);
	}

	/**
	 * Iterates over the collection validators of this validator.
	 *
	 * <p>
	 * This method is intended for internal use, for example by
	 * {@link am.ik.yavi.builder.ValidatorBuilder} when nesting this validator.
	 * @param action callback per <code>CollectionValidator</code>.
	 */
	public void forEachCollectionValidator(Consumer<CollectionValidator<T, ?, ?>> action) {
		this.collectionValidators.forEach(action);
	}

	/**
	 * Iterates over the conditional validators of this validator.
	 *
	 * <p>
	 * This method is intended for internal use, for example by
	 * {@link am.ik.yavi.builder.ValidatorBuilder} when nesting this validator.
	 * @param action callback per
	 * {@code Pair&lt;ConstraintCondition&lt;T&gt;, Validatable&lt;T&gt;&gt;}.
	 */
	public void forEachConditionalValidator(Consumer<Pair<ConstraintCondition<T>, Validatable<T>>> action) {
		this.conditionalValidators.forEach(action);
	}

	/**
	 * Validates the target object and returns the constraint violations.
	 *
	 * <p>
	 * Validation applies the constraint predicates, then the collection validators, then
	 * the conditional validators whose condition matches. Nested objects are validated
	 * only when they are not {@code null}; a {@code null} entry in a validated collection
	 * is reported as an {@code object.notNull} violation. Violation names are built from
	 * the prefix, the property name and, for collection elements, the element index.
	 *
	 * <p>
	 * When fail-fast mode is enabled, validation returns as soon as the first violation
	 * is found. Otherwise all violations are collected. The given locale is used to
	 * format the violation messages, and the constraint context is passed to conditional
	 * validators.
	 * @param target the object to validate, must not be null
	 * @param locale the locale for message formatting
	 * @param constraintContext context used by conditional validators
	 * @return constraint violations detected during validation, or an empty container if
	 * no violations
	 * @throws IllegalArgumentException if target is null
	 */
	@Override
	public ConstraintViolations validate(T target, Locale locale, ConstraintContext constraintContext) {
		return this.validate(target, "", -1, locale, constraintContext);
	}

	/**
	 * Returns an {@link ApplicativeValidator} based on this validator.
	 *
	 * <p>
	 * The returned validator applies the same constraints and wraps the result in a
	 * {@link Validated}, which can be combined with other validations to build a
	 * composite value in applicative style.
	 *
	 * <p>
	 * Example: <pre>{@code
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
				final @Nullable Object v = predicates.toValue().apply(target);
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
