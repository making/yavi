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
package am.ik.yavi.constraint;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import am.ik.yavi.constraint.charsequence.variant.IdeographicVariationSequence;
import am.ik.yavi.constraint.charsequence.variant.MongolianFreeVariationSelector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CharSequenceConstraintTest {

	@ParameterizedTest
	@ValueSource(strings = { "yavi", "aは小文字" })
	void validContains(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.contains("a"));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "yvi", "Aは大文字" })
	void invalidContains(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.contains("a"));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "abc@example.com", "abc@localhost", "abc@192.168.1.10",
			"東京@example.com", "" })
	void validEmail(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.email());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "example.com", "abc@@example.com" })
	void invalidEmail(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.email());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "ab", "漢字" })
	void validFixedSize(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.fixedSize(2));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "a", "abc" })
	void invalidFixedSize(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.fixedSize(2));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "abcd", "朝ごはん" })
	void validGreaterThan(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.greaterThan(3));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "abc", "\uD842\uDFB7野屋" })
	void invalidGreaterThan(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.greaterThan(3));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "abcd", "abc" })
	void validGreaterThanOrEqual(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.greaterThanOrEqual(3));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "ab", "\uD842\uDFB7田" })
	void invalidGreaterThanOrEqual(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.greaterThanOrEqual(3));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "ᠠ᠋", "ᠰ᠌" })
	void ignoreFvsCharacter(String value) {
		Predicate<String> predicate = retrievePredicate(
				c -> c.variant(opts -> opts.fvs(MongolianFreeVariationSelector.IGNORE))
						.fixedSize(1));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "\uD842\uDF9F\uDB40\uDD00", "\u908A\uDB40\uDD07" })
	void ignoreIvsCharacter(String value) {
		Predicate<String> predicate = retrievePredicate(
				c -> c.variant(opts -> opts.ivs(IdeographicVariationSequence.IGNORE))
						.fixedSize(1));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "ab", "\uD842\uDFB7田" })
	void validLessThan(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.lessThan(3));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "abc", "\uD842\uDFB7野屋" })
	void invalidLessThan(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.lessThan(3));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "ab", "abc", "\uD842\uDFB7野屋" })
	void validLessThanOrEqual(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.lessThanOrEqual(3));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "abcd" })
	void invalidLessThanOrEqual(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.lessThanOrEqual(3));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "モジ" /* モシ\u3099 */ })
	void validNormalizeCombiningCharacter(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.fixedSize(2));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "foo", "漢字" })
	void validNotBlank(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.notBlank());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "    ", "　　　" })
	void invalidNotBlank(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.notBlank());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "foo", " " })
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
	@ValueSource(strings = { "ᠠ᠋", "ᠰ᠌" })
	void notIgnoreFvsCharacter(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.fixedSize(2));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "\uD842\uDF9F\uDB40\uDD00", "\u908A\uDB40\uDD07" })
	void notIgnoreIvsCharacter(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.fixedSize(2));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "モジ" /* モシ\u3099 */ })
	void notNormalizeCombiningCharacter(String value) {
		Predicate<String> predicate = retrievePredicate(
				c -> c.normalizer(null).fixedSize(3));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "1234", "0000" })
	void validPattern(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.pattern("[0-9]{4}"));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "134a", "abcd" })
	void invalidPattern(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.pattern("[0-9]{4}"));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "http://example.com", "https://example.com", "" })
	void validUrl(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.url());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "example.com", "htt://example.com" })
	void invalidUrl(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.url());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@MethodSource("randomUUIDs")
	void validUUID(String value) {
		Predicate<String> predicate = retrievePredicate(CharSequenceConstraint::uuid);
		assertThat(predicate.test(value)).isTrue();
	}

	private static Stream<String> randomUUIDs() {
		return Stream.generate(UUID::randomUUID).map(UUID::toString).limit(10);
	}

	@ParameterizedTest
	@ValueSource(strings = { "nonsense-nonsense-nonsense",
			"12345678-1234-1234-1234-1234-12345678" })
	void invalidUUID(String value) {
		Predicate<String> predicate = retrievePredicate(CharSequenceConstraint::uuid);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-128", "0", "127" })
	void validIsByte(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isByte());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-129", "128", "a" })
	void invalidIsByte(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isByte());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-32768", "0", "32767" })
	void validIsShort(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isShort());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-32769", "32768", "a" })
	void invalidIsShort(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isShort());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-2147483648", "0", "2147483647" })
	void validIsInteger(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isInteger());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-2147483649", "2147483648", "a" })
	void invalidIsInteger(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isInteger());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-9223372036854775808", "0", "9223372036854775807" })
	void validIsLong(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isLong());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-9223372036854775809", "9223372036854775808", "a" })
	void invalidIsLong(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isLong());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-100", "-1.0", "0", "1.0", "100" })
	void validIsFloat(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isFloat());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "a" })
	void invalidIsFloat(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isFloat());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-100", "-1.0", "0", "1.0", "100" })
	void validIsDouble(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isDouble());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "a" })
	void invalidIsDouble(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isDouble());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-9223372036854775809", "0", "9223372036854775808" })
	void validIsBigInteger(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isBigInteger());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "a", "0.1" })
	void invalidIsBigInteger(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isBigInteger());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-100", "-1.0", "0", "1.0", "100" })
	void validIsBigDecimal(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isBigDecimal());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "a" })
	void invalidIsBigDecimal(String value) {
		Predicate<String> predicate = retrievePredicate(c -> c.isBigDecimal());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "4111111111111111", "4242424242424242", "4012888888881881",
			"5555555555554444", "5105105105105100", "378282246310005", "371449635398431",
			"30569309025904", "38520000023237", "3530111333300000", "3566002020360505" })
	void validLuhn(String value) {
		final Predicate<String> predicate = retrievePredicate(c -> c.luhn());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "4111111111111112", "4242424242424243", "401288888888188a" })
	void invalidLuhn(String value) {
		final Predicate<String> predicate = retrievePredicate(c -> c.luhn());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "1.1.1.1", "127.0.0.1", "255.255.255.255", "0.0.0.0" })
	void validIpv4(String value) {
		final Predicate<String> predicate = retrievePredicate(c -> c.ipv4());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "1.1.1.1.1", "255.255.255.256", "a.a.a.a" })
	void invalidIpv4(String value) {
		final Predicate<String> predicate = retrievePredicate(c -> c.ipv4());
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "1762:0:0:0:0:B03:1:AF18", "0:0:0:0:0:0:0:0",
			"0:0:0:0:0:0:0:1", "::1", "2001:0db8:bd05:01d2:288a:1fc0:0001:10ee",
			"2001:db8:20:3:1000:100:20:3", "2001:db8::1234:0:0:9abc", "2001:db8::9abc",
			"::ffff:192.0.2.1", "fe80::0123:4567:89ab:cdef%4",
			"fe80::0123:4567:89ab:cdef%fxp0" })
	void validIpv6(String value) {
		final Predicate<String> predicate = retrievePredicate(c -> c.ipv6());
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "1.1.1.1.1", "0:0:0:0:0:0:0:Z" })
	void invalidIpv6(String value) {
		final Predicate<String> predicate = retrievePredicate(c -> c.ipv6());
		assertThat(predicate.test(value)).isFalse();
	}

	private static Predicate<String> retrievePredicate(
			Function<CharSequenceConstraint<String, String>, CharSequenceConstraint<String, String>> constraint) {
		return constraint.apply(new CharSequenceConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
