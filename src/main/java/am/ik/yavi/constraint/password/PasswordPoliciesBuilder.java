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
 * @since 0.7.0
 */
public abstract class PasswordPoliciesBuilder<T, E, B extends PasswordPoliciesBuilder<T, E, B>> {
	private final Set<PasswordPolicy<E>> requiredPolicies = new LinkedHashSet<>();

	private final Set<PasswordPolicy<E>> optionalPolicies = new LinkedHashSet<>();

	private int minimumOptionalPoliciesRequirement;

	protected abstract B cast();

	public B required(PasswordPolicy<E> policy) {
		this.requiredPolicies.add(policy);
		return this.cast();
	}

	@SafeVarargs
	public final B required(PasswordPolicy<E>... policies) {
		this.requiredPolicies.addAll(Arrays.asList(policies));
		return this.cast();
	}

	@SafeVarargs
	public final B optional(int minimumRequirement, PasswordPolicy<E>... policies) {
		this.optionalPolicies.addAll(Arrays.asList(policies));
		this.minimumOptionalPoliciesRequirement = minimumRequirement;
		return this.cast();
	}

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
						this.optionalPolicies.stream().map(PasswordPolicy::name)
								.collect(Collectors.toList()) },
				NullAs.VALID);
	}
}
