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

import java.util.Locale;

@FunctionalInterface
public interface ValidatorSubset<T> {
	/**
	 * Validates all constraints on {@code target}.
	 *
	 * @param target target to validate
	 * @param locale the locale targeted for the violation messages.
	 * @param constraintGroup constraint group to validate
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	ConstraintViolations validate(T target, Locale locale,
			ConstraintGroup constraintGroup);

	/**
	 * Validates all constraints on {@code target}. <br>
	 * {@code Locale.getDefault()} is used to locate the violation messages.
	 * {@code ConstraintGroup.DEFAULT} is used as a constraint group.
	 *
	 * @param target target to validate
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	default ConstraintViolations validate(T target) {
		return this.validate(target, Locale.getDefault(), ConstraintGroup.DEFAULT);
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
	default ConstraintViolations validate(T target, Locale locale) {
		return this.validate(target, locale, ConstraintGroup.DEFAULT);
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
	default ConstraintViolations validate(T target, ConstraintGroup constraintGroup) {
		return this.validate(target, Locale.getDefault(), constraintGroup);
	}


	/**
	 * Returns the corresponding either validator
	 * @return either validator
	 * @since 0.6.0
	 */
	default EitherValidator<T> either() {
		return EitherValidator.of(this);
	}

	/**
	 * Returns the corresponding applicative validator
	 * @return applicative validator
	 * @since 0.6.0
	 */
	default ApplicativeValidator<T> applicative() {
		return ApplicativeValidator.of(this);
	}
}
