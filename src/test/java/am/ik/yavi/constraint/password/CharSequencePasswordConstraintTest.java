/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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

import java.util.List;
import java.util.function.Function;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static am.ik.yavi.constraint.password.PasswordPolicy.LOWERCASE;
import static am.ik.yavi.constraint.password.PasswordPolicy.NUMBERS;
import static am.ik.yavi.constraint.password.PasswordPolicy.SYMBOLS;
import static am.ik.yavi.constraint.password.PasswordPolicy.UPPERCASE;
import static org.assertj.core.api.Assertions.assertThat;

public class CharSequencePasswordConstraintTest {

	@ParameterizedTest
	@ValueSource(strings = { "ABCD", "A12b", "Ab12#!" })
	void validPassword_uppercase(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.uppercase().build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "abc", "ab1", "ab1!" })
	void invalidPassword_uppercase(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.uppercase().build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Uppercase");
	}

	@ParameterizedTest
	@ValueSource(strings = { "abc", "ab1", "ab1!" })
	void invalidPassword_uppercase_rename(String input) {
		final Validator<String> validator = buildValidator(
				c -> c.password(policies -> policies.required(UPPERCASE.name("大文字")).build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("大文字");
	}

	@ParameterizedTest
	@ValueSource(strings = { "abcd", "A12b", "Ab12#!" })
	void validPassword_lowercase(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.lowercase().build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "ABC", "AB1", "AB1!" })
	void invalidPassword_lowercase(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.lowercase().build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Lowercase");
	}

	@ParameterizedTest
	@ValueSource(strings = { "abcd", "A12b", "Ab12#!", "ABC", "AB1", "AB1!" })
	void validPassword_alphabet(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.alphabets().build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "123!", "123", "@!" })
	void invalidPassword_alphabet(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.alphabets().build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Alphabets");
	}

	@ParameterizedTest
	@ValueSource(strings = { "1234", "a12b", "Az12#!" })
	void validPassword_numbers(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.numbers().build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "abc", "abC", "abC#!" })
	void invalidPassword_numbers(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.numbers().build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Numbers");
	}

	@ParameterizedTest
	@ValueSource(strings = { "1234!", "a12b#", "Az12#!", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",",
			"-", ".", "/", ":", ";", "<", "=", ">", "?", "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~" })
	void validPassword_symbols(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.symbols().build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "abc", "abC", "abC1" })
	void invalidPassword_symbols(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.symbols().build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Symbols");
	}

	@ParameterizedTest
	@ValueSource(strings = { "VMware1!", "Az12#!" })
	void validPassword_strong(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.strong()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void invalidPassword_strong_symbols_numbers_uppercase_are_missing() {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.strong()));
		final ConstraintViolations violations = validator.validate("abc");
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(3);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Uppercase");
		assertThat(violations.get(1).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(1).args()).hasSize(3);
		assertThat(violations.get(1).args()[1]).isEqualTo("Numbers");
		assertThat(violations.get(2).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(2).args()).hasSize(3);
		assertThat(violations.get(2).args()[1]).isEqualTo("Symbols");
	}

	@Test
	void invalidPassword_strong_symbols_numbers_are_missing() {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.strong()));
		final ConstraintViolations violations = validator.validate("abC");
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(2);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Numbers");
		assertThat(violations.get(1).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(1).args()).hasSize(3);
		assertThat(violations.get(1).args()[1]).isEqualTo("Symbols");
	}

	@Test
	void invalidPassword_numbers_are_missing() {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.strong()));
		final ConstraintViolations violations = validator.validate("abC#!");
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Numbers");
	}

	@Test
	void invalidPassword_symbols_are_missing() {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.strong()));
		final ConstraintViolations violations = validator.validate("123abC");
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Symbols");
	}

	@ParameterizedTest
	@ValueSource(strings = { "abC1", "A1!", "1#a", "VMware1!" })
	void validPassword_optional(String input) {
		final Validator<String> validator = buildValidator(
				c -> c.password(policies -> policies.optional(3, LOWERCASE, UPPERCASE, NUMBERS, SYMBOLS).build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "abC", "1!", "1a" })
	@SuppressWarnings({ "unchecked" })
	void invalidPassword_optional(String input) {
		final Validator<String> validator = buildValidator(
				c -> c.password(policies -> policies.optional(3, LOWERCASE, UPPERCASE, NUMBERS, SYMBOLS).build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.optional");
		assertThat(violations.get(0).args()).hasSize(4);
		assertThat(violations.get(0).args()[1]).isEqualTo(3);
		assertThat((List<String>) violations.get(0).args()[2]).containsExactly("Lowercase", "Uppercase", "Numbers",
				"Symbols");
	}

	@ParameterizedTest
	@ValueSource(strings = { "abC1", "abC!", "VMware1!" })
	void validPassword_required_and_optional(String input) {
		final Validator<String> validator = buildValidator(c -> c
			.password(policies -> policies.required(LOWERCASE, UPPERCASE).optional(1, NUMBERS, SYMBOLS).build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	@SuppressWarnings({ "unchecked" })
	void invalidPassword_required_and_optional_missing_lowercase_and_optional() {
		final Validator<String> validator = buildValidator(c -> c
			.password(policies -> policies.required(LOWERCASE, UPPERCASE).optional(1, NUMBERS, SYMBOLS).build()));
		final ConstraintViolations violations = validator.validate("ABC");
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(2);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Lowercase");
		assertThat(violations.get(1).messageKey()).isEqualTo("password.optional");
		assertThat(violations.get(1).args()).hasSize(4);
		assertThat(violations.get(1).args()[1]).isEqualTo(1);
		assertThat((List<String>) violations.get(1).args()[2]).containsExactly("Numbers", "Symbols");
	}

	@Test
	void invalidPassword_required_and_optional_missing_lowercase() {
		final Validator<String> validator = buildValidator(c -> c
			.password(policies -> policies.required(LOWERCASE, UPPERCASE).optional(1, NUMBERS, SYMBOLS).build()));
		final ConstraintViolations violations = validator.validate("ABC1!");
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Lowercase");
	}

	@Test
	@SuppressWarnings({ "unchecked" })
	void invalidPassword_required_and_optional_missing_optional() {
		final Validator<String> validator = buildValidator(c -> c
			.password(policies -> policies.required(LOWERCASE, UPPERCASE).optional(1, NUMBERS, SYMBOLS).build()));
		final ConstraintViolations violations = validator.validate("abC");
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.optional");
		assertThat(violations.get(0).args()).hasSize(4);
		assertThat(violations.get(0).args()[1]).isEqualTo(1);
		assertThat((List<String>) violations.get(0).args()[2]).containsExactly("Numbers", "Symbols");
	}

	@ParameterizedTest
	@ValueSource(strings = { "VMware1!", "Az12#!" })
	void validPassword_required_and_optional_and_custom(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.uppercase()
			.lowercase()
			.numbers()
			.symbols()
			.required(new PredictableWordsPasswordPolicy())
			.build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "PassWord1!" })
	void invalidPassword_required_and_optional_and_custom(String input) {
		final Validator<String> validator = buildValidator(c -> c.password(policies -> policies.uppercase()
			.lowercase()
			.numbers()
			.symbols()
			.required(new PredictableWordsPasswordPolicy())
			.build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("PredictableWords");
	}

	@ParameterizedTest
	@ValueSource(strings = { "12abc", "12ABC", "12aBc" })
	void valid_count(String input) {
		final Validator<String> validator = buildValidator(
				c -> c.password(policies -> policies.numbers(2).alphabets(3).build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "12ab", "123aB" })
	void invalid_alphabets_count(String input) {
		final Validator<String> validator = buildValidator(
				c -> c.password(policies -> policies.numbers(2).alphabets(3).build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Alphabets");
	}

	@ParameterizedTest
	@ValueSource(strings = { "1aBc", "abC!" })
	void invalid_numbers_count(String input) {
		final Validator<String> validator = buildValidator(
				c -> c.password(policies -> policies.numbers(2).alphabets(3).build()));
		final ConstraintViolations violations = validator.validate(input);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("Numbers");
	}

	static class PredictableWordsPasswordPolicy implements PasswordPolicy<String> {

		@Override
		public boolean test(String s) {
			return !s.toUpperCase().contains("PASSWORD");
		}

	}

	private static Validator<String> buildValidator(
			Function<CharSequenceConstraint<String, String>, CharSequenceConstraint<String, String>> constraint) {
		return ValidatorBuilder.of(String.class)._string(c -> c, "password", constraint).build();
	}

}
