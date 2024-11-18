/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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

/**
 * @since 0.14.0
 */
public class InheritanceValidator<T, N extends T> implements Validatable<T> {
	private final Class<N> nClass;
	private final Validatable<N> validator;

	public InheritanceValidator(Class<N> nClass, Validatable<N> validator) {
		this.nClass = nClass;
		this.validator = validator;
	}

	@Override
	public ConstraintViolations validate(T target, Locale locale,
			ConstraintContext constraintContext) {
		final N n = nClass.cast(target);

		return this.validator.validate(n, locale, constraintContext);
	}

	@Override
	public Validatable<T> failFast(boolean failFast) {
		final Validatable<N> validatable = this.validator.failFast(failFast);
		return new InheritanceValidator<>(nClass, validatable);
	}

	@Override
	public boolean isFailFast() {
		return this.validator.isFailFast();
	}
}
