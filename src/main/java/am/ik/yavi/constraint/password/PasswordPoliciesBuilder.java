/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.password;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.NullAs;
import am.ik.yavi.core.ViolationMessage.Default;

/**
 * Collects the {@link PasswordPolicy} instances that a password value must meet and
 * builds the corresponding constraint predicates.
 *
 * <p>
 * A required policy produces one predicate that the value must satisfy; a missing policy
 * is reported with the message key {@code password.required}, where {@code {1}} is the
 * policy name and {@code {2}} is the value. Optional policies produce a single predicate
 * with the message key {@code password.optional} that requires at least the configured
 * number of them to match; {@code {1}} is that number and {@code {2}} is the list of the
 * optional policy names.
 * </p>
 *
 * <p>
 * A builder is used through
 * {@link am.ik.yavi.constraint.CharSequenceConstraint#password(java.util.function.Function)}
 * or
 * {@link am.ik.yavi.constraint.ObjectConstraint#password(java.util.function.Function)}. A
 * {@code null} value is valid and skips all policies. A builder is not thread-safe; use
 * it from a single thread while building the validator.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <E> the type of the password value
 * @param <B> the concrete builder type returned for method chaining
 * @since 0.7.0
 */
public abstract class PasswordPoliciesBuilder<T, E, B extends PasswordPoliciesBuilder<T, E, B>> {

	private final Set<PasswordPolicy<E>> requiredPolicies = new LinkedHashSet<>();

	private final Set<PasswordPolicy<E>> optionalPolicies = new LinkedHashSet<>();

	private int minimumOptionalPoliciesRequirement;

	/**
	 * Creates a builder without any policy.
	 */
	public PasswordPoliciesBuilder() {
	}

	/**
	 * Returns this builder as its concrete type.
	 * @return this builder
	 */
	protected abstract B cast();

	/**
	 * Adds a policy that the value must meet.
	 * @param policy the required policy
	 * @return this builder
	 */
	public B required(PasswordPolicy<E> policy) {
		this.requiredPolicies.add(policy);
		return this.cast();
	}

	/**
	 * Adds policies that the value must all meet.
	 * @param policies the required policies
	 * @return this builder
	 */
	@SafeVarargs
	public final B required(PasswordPolicy<E>... policies) {
		this.requiredPolicies.addAll(Arrays.asList(policies));
		return this.cast();
	}

	/**
	 * Adds optional policies and sets how many of the optional policies the value must
	 * meet at all.
	 * @param minimumRequirement the minimum number of the optional policies that must
	 * match; a value of zero makes any value valid
	 * @param policies the optional policies
	 * @return this builder
	 */
	@SafeVarargs
	public final B optional(int minimumRequirement, PasswordPolicy<E>... policies) {
		this.optionalPolicies.addAll(Arrays.asList(policies));
		this.minimumOptionalPoliciesRequirement = minimumRequirement;
		return this.cast();
	}

	/**
	 * Builds the constraint predicates for the configured policies. The required policies
	 * come first, one predicate each, followed by one predicate for the optional
	 * policies.
	 * @return the constraint predicates, empty if no policy was added
	 */
	public List<ConstraintPredicate<E>> build() {
		final List<ConstraintPredicate<E>> predicates = new ArrayList<>();
		if (!requiredPolicies.isEmpty()) {
			predicates.addAll(this.requiredConstraintPredicates());
		}
		if (!optionalPolicies.isEmpty()) {
			predicates.add(this.optionalConstraintPredicate());
		}
		return predicates;
	}

	private List<ConstraintPredicate<E>> requiredConstraintPredicates() {
		return this.requiredPolicies.stream()
			.map(policy -> ConstraintPredicate.of(policy, Default.PASSWORD_REQUIRED,
					() -> new Object[] { policy.name() }, NullAs.VALID))
			.collect(Collectors.toList());
	}

	private ConstraintPredicate<E> optionalConstraintPredicate() {
		final Predicate<E> predicate = input -> {
			int matched = 0;
			for (PasswordPolicy<E> policy : this.optionalPolicies) {
				if (policy.test(input)) {
					matched++;
				}
			}
			return matched >= this.minimumOptionalPoliciesRequirement;
		};
		return ConstraintPredicate.of(predicate, Default.PASSWORD_OPTIONAL,
				() -> new Object[] { this.minimumOptionalPoliciesRequirement,
						this.optionalPolicies.stream().map(PasswordPolicy::name).collect(Collectors.toList()) },
				NullAs.VALID);
	}

}
