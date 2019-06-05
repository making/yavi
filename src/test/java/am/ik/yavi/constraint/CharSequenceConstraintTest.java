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
package am.ik.yavi.constraint;

import am.ik.yavi.constraint.charsequence.variant.IdeographicVariationSequence;
import am.ik.yavi.constraint.charsequence.variant.MongolianFreeVariationSelector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class CharSequenceConstraintTest {

	@ParameterizedTest
	@ValueSource(strings = {"yavi", "aは小文字"})
	void validContains(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.contains("a"));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"yvi", "Aは大文字"})
	void invalidContains(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.contains("a"));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"abc@example.com", "abc@localhost", "abc@192.168.1.10", "東京@example.com", ""})
	void validEmail(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.email());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"example.com", "abc@@example.com"})
	void invalidEmail(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.email());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"ab", "漢字"})
	void validFixedSize(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.fixedSize(2));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"a", "abc"})
	void invalidFixedSize(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.fixedSize(2));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"abcd", "朝ごはん"})
	void validGreaterThan(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.greaterThan(3));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"abc", "\uD842\uDFB7野屋"})
	void invalidGreaterThan(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.greaterThan(3));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"abcd", "abc"})
	void validGreaterThanOrEqual(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.greaterThanOrEqual(3));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"ab", "\uD842\uDFB7田"})
	void invalidGreaterThanOrEqual(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.greaterThanOrEqual(3));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"ᠠ᠋", "ᠰ᠌"})
	void ignoreFvsCharacter(String value) {
		Predicate<String> predicate = retrievePredicate(c ->
				c.variant(opts -> opts.fvs(MongolianFreeVariationSelector.IGNORE)).fixedSize(1));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"\uD842\uDF9F\uDB40\uDD00", "\u908A\uDB40\uDD07"})
	void ignoreIvsCharacter(String value) {
		Predicate<String> predicate = retrievePredicate(c ->
				c.variant(opts -> opts.ivs(IdeographicVariationSequence.IGNORE)).fixedSize(1));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"ab", "\uD842\uDFB7田"})
	void validLessThan(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.lessThan(3));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"abc", "\uD842\uDFB7野屋"})
	void invalidLessThan(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.lessThan(3));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"ab", "abc", "\uD842\uDFB7野屋"})
	void validLessThanOrEqual(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.lessThanOrEqual(3));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"abcd"})
	void invalidLessThanOrEqual(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.lessThanOrEqual(3));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"モジ" /* モシ\u3099 */})
	void validNormalizeCombiningCharacter(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.fixedSize(2));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"foo", "漢字"})
	void validNotBlank(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.notBlank());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "    ", "　　　"})
	void invalidNotBlank(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.notBlank());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"foo", " "})
	void validNotEmpty(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.notEmpty());
		assertThat(predicate.test(value)).isTrue();
	}

	@Test
	void invalidNotEmpty() {
		Predicate<String> predicate = retrievePredicate(c -> c.notEmpty());
		assertThat(predicate.test("")).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"ᠠ᠋", "ᠰ᠌"})
	void notIgnoreFvsCharacter(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.fixedSize(2));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"\uD842\uDF9F\uDB40\uDD00", "\u908A\uDB40\uDD07"})
	void notIgnoreIvsCharacter(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.fixedSize(2));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"モジ" /* モシ\u3099 */})
	void notNormalizeCombiningCharacter(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.normalizer(null).fixedSize(3));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"1234", "0000"})
	void validPattern(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.pattern("[0-9]{4}"));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"134a", "abcd"})
	void invalidPattern(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.pattern("[0-9]{4}"));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"http://example.com", "https://example.com", ""})
	void validUrl(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.url());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"example.com", "htt://example.com"})
	void invalidUrl(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.url());
		assertThat(predicate.test(value)).isFalse();
	}

	private static Predicate<String> retrievePredicate(Function<CharSequenceConstraint<String, String>, CharSequenceConstraint<String, String>> constraint) {
		return constraint.apply(new CharSequenceConstraint<>()).predicates().peekFirst().predicate();
	}
}
