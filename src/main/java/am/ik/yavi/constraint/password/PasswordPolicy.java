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
package am.ik.yavi.constraint.password;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @param <T> target class
 * @since 0.7.0
 */
public interface PasswordPolicy<T> extends Predicate<T> {

	default String name() {
		return this.getClass().getSimpleName()
				.replace(PasswordPolicy.class.getSimpleName(), "");
	}

	/**
	 * Rename the password policy
	 *
	 * @param name new name
	 * @return renamed policy
	 * @since 0.8.1
	 */
	default PasswordPolicy<T> name(String name) {
		return new PasswordPolicy<T>() {
			@Override
			public String name() {
				return name;
			}

			@Override
			public boolean test(T t) {
				return PasswordPolicy.this.test(t);
			}
		};
	}

	static <T> PasswordPolicy<T> of(String name, Predicate<T> predicate) {
		return new PasswordPolicy<T>() {
			@Override
			public boolean test(T t) {
				return predicate.test(t);
			}

			@Override
			public String name() {
				return name;
			}
		};
	}

	static <T extends CharSequence> PatternPasswordPolicy<T> pattern(String name,
			String regex) {
		return new PatternPasswordPolicy<>(name, regex);
	}

	/**
	 * @since 0.8.1
	 */
	class PatternPasswordPolicy<T extends CharSequence> implements PasswordPolicy<T> {
		private final String name;

		private final String regex;

		private final Pattern pattern;

		private final int count;

		public PatternPasswordPolicy(String name, String regex) {
			this(name, regex, 1);
		}

		public PatternPasswordPolicy(String name, String regex, int count) {
			if (count <= 0) {
				throw new IllegalArgumentException("'count' must be greater than 0");
			}
			this.name = name;
			this.regex = regex;
			this.pattern = Pattern.compile(regex);
			this.count = count;
		}

		@Override
		public String name() {
			return this.name;
		}

		@Override
		public boolean test(T input) {
			final Matcher matcher = this.pattern.matcher(input);
			int count = 0;
			while (matcher.find()) {
				if (++count >= this.count) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Change the count of the policy
		 *
		 * @param count new count
		 * @return new policy
		 */
		public PatternPasswordPolicy<T> count(int count) {
			if (this.count == count) {
				return this;
			}
			return new PatternPasswordPolicy<>(this.name, this.regex, count);
		}
	}

	PatternPasswordPolicy<String> UPPERCASE = PasswordPolicy.pattern("Uppercase",
			"[A-Z]");

	PatternPasswordPolicy<String> LOWERCASE = PasswordPolicy.pattern("Lowercase",
			"[a-z]");

	/**
	 * @since 0.8.1
	 */
	PatternPasswordPolicy<String> ALPHABETS = PasswordPolicy.pattern("Alphabets",
			"[a-zA-Z]");

	PatternPasswordPolicy<String> NUMBERS = PasswordPolicy.pattern("Numbers", "[0-9]");

	PatternPasswordPolicy<String> SYMBOLS = PasswordPolicy.pattern("Symbols",
			"[!-/:-@\\[-`{-\\~]");
}
