/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ViolationMessage;
import org.jspecify.annotations.Nullable;

/**
 * A rule on the object that a validator of this package creates, added with
 * {@code constraintOnTarget}.
 *
 * @param predicate the rule that the created object must satisfy
 * @param name the name of the reported violation
 * @param violationMessage the message of the reported violation
 * @param <X> the type of the created object
 */
record TargetRule<X extends @Nullable Object> (Predicate<? super X> predicate, String name,
		ViolationMessage violationMessage) {

	/**
	 * Returns the given rules followed by the given one.
	 */
	static <X extends @Nullable Object> List<TargetRule<X>> append(List<TargetRule<X>> rules, TargetRule<X> rule) {
		final List<TargetRule<X>> appended = new ArrayList<>(rules);
		appended.add(rule);
		return List.copyOf(appended);
	}

	/**
	 * Checks the rules in their order on a valid result and stops at the first one that
	 * is violated, as chained calls of {@code ValueValidator#constraintOnTarget} do.
	 */
	static <X extends @Nullable Object> Validated<X> check(List<TargetRule<X>> rules, Validated<X> validated,
			Locale locale) {
		if (!validated.isValid()) {
			return validated;
		}
		final X value = validated.value();
		for (TargetRule<X> rule : rules) {
			if (!rule.predicate.test(value)) {
				return Validated
					.failureWith(ConstraintViolation.of(rule.violationMessage, value, locale).prefixed(rule.name));
			}
		}
		return validated;
	}

}
