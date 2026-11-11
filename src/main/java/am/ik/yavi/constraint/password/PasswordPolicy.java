/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.password;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A named requirement that a password value has to meet.
 *
 * <p>
 * A policy is a {@link Predicate} that returns {@code true} when the value meets the
 * requirement, together with the name that is reported in the violation message. Policies
 * are collected by {@link PasswordPoliciesBuilder} and turned into constraints by its
 * {@link PasswordPoliciesBuilder#build()} method. A policy can be created with
 * {@link #of(String, Predicate)}, {@link #pattern(String, String)} or one of the
 * constants of this interface.
 * </p>
 *
 * <p>
 * Implementations are expected to be stateless and reusable.
 * </p>
 *
 * @param <T> the type of the value the policy tests
 * @since 0.7.0
 */
public interface PasswordPolicy<T> extends Predicate<T> {

	/**
	 * Returns the name of the policy reported in violation messages. The default is the
	 * simple class name with the suffix {@code PasswordPolicy} removed, which is an empty
	 * string for an anonymous implementation.
	 * @return the name of the policy
	 */
	default String name() {
		return this.getClass().getSimpleName().replace(PasswordPolicy.class.getSimpleName(), "");
	}

	/**
	 * Returns a copy of this policy that reports the given name.
	 * @param name the new name
	 * @return a policy that tests the same values and reports {@code name}
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

	/**
	 * Creates a policy with the given name and predicate.
	 * @param name the name reported in violation messages
	 * @param predicate the requirement, which returns {@code true} when the value meets
	 * it
	 * @param <T> the type of the value the policy tests
	 * @return the policy
	 */
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

	/**
	 * Creates a policy that requires at least one match of the given regular expression.
	 * @param name the name reported in violation messages
	 * @param regex the regular expression
	 * @param <T> the type of the character sequence the policy tests
	 * @return the policy
	 */
	static <T extends CharSequence> PatternPasswordPolicy<T> pattern(String name, String regex) {
		return new PatternPasswordPolicy<>(name, regex);
	}

	/**
	 * A policy that requires a minimum number of matches of a regular expression in the
	 * value.
	 *
	 * @param <T> the type of the character sequence the policy tests
	 * @since 0.8.1
	 */
	class PatternPasswordPolicy<T extends CharSequence> implements PasswordPolicy<T> {

		private final String name;

		private final String regex;

		private final Pattern pattern;

		private final int count;

		/**
		 * Creates a policy that requires at least one match of the given regular
		 * expression.
		 * @param name the name reported in violation messages
		 * @param regex the regular expression
		 * @throws IllegalArgumentException if {@code regex} is not a valid regular
		 * expression
		 */
		public PatternPasswordPolicy(String name, String regex) {
			this(name, regex, 1);
		}

		/**
		 * Creates a policy that requires at least {@code count} matches of the given
		 * regular expression.
		 * @param name the name reported in violation messages
		 * @param regex the regular expression
		 * @param count the minimum number of matches
		 * @throws IllegalArgumentException if {@code count} is zero or negative, or if
		 * {@code regex} is not a valid regular expression
		 */
		public PatternPasswordPolicy(String name, String regex, int count) {
			if (count <= 0) {
				throw new IllegalArgumentException("'count' must be greater than 0");
			}
			this.name = name;
			this.regex = regex;
			this.pattern = Pattern.compile(regex);
			this.count = count;
		}

		/**
		 * Returns the name of the policy.
		 * @return the name reported in violation messages
		 */
		@Override
		public String name() {
			return this.name;
		}

		/**
		 * Returns whether the value contains at least the required number of matches of
		 * the regular expression.
		 * @param input the value to test
		 * @return {@code true} if enough matches are found
		 */
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
		 * Returns a policy that is the same except for the required number of matches.
		 * @param count the new minimum number of matches
		 * @return this policy if {@code count} is the current count, otherwise a new
		 * policy
		 * @throws IllegalArgumentException if {@code count} is zero or negative
		 */
		public PatternPasswordPolicy<T> count(int count) {
			if (this.count == count) {
				return this;
			}
			return new PatternPasswordPolicy<>(this.name, this.regex, count);
		}

	}

	/**
	 * Policy that requires at least one uppercase ASCII letter.
	 */
	PatternPasswordPolicy<String> UPPERCASE = PasswordPolicy.pattern("Uppercase", "[A-Z]");

	/**
	 * Policy that requires at least one lowercase ASCII letter.
	 */
	PatternPasswordPolicy<String> LOWERCASE = PasswordPolicy.pattern("Lowercase", "[a-z]");

	/**
	 * Policy that requires at least one ASCII letter.
	 *
	 * @since 0.8.1
	 */
	PatternPasswordPolicy<String> ALPHABETS = PasswordPolicy.pattern("Alphabets", "[a-zA-Z]");

	/**
	 * Policy that requires at least one ASCII digit.
	 */
	PatternPasswordPolicy<String> NUMBERS = PasswordPolicy.pattern("Numbers", "[0-9]");

	/**
	 * Policy that requires at least one printable ASCII symbol other than a letter, digit
	 * or space.
	 */
	PatternPasswordPolicy<String> SYMBOLS = PasswordPolicy.pattern("Symbols", "[!-/:-@\\[-`{-\\~]");

}
