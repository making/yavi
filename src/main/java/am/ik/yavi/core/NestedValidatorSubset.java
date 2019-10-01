/*
 * Copyright (C) 2018-2019 Toshiaki Maki <makingx@gmail.com>
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
import java.util.function.Function;

public class NestedValidatorSubset<T, N> implements ValidatorSubset<T> {
	private final Function<T, N> nested;
	private final ValidatorSubset<N> validator;

	public NestedValidatorSubset(Function<T, N> nested, ValidatorSubset<N> validator,
			String prefix) {
		this.nested = nested;
		this.validator = (validator instanceof Validator)
				? ((Validator<N>) validator).prefixed(prefix)
				: validator;
	}

	@Override
	public ConstraintViolations validate(T target, Locale locale) {
		final N n = this.nested.apply(target);
		return this.validator.validate(n, locale);
	}
}
