/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.constraint.charsequence.variant.IdeographicVariationSequence;
import am.ik.yavi.constraint.charsequence.variant.MongolianFreeVariationSelector;

public class CharSequenceConstraintTest {
	private CharSequenceConstraint<String, String> constraint = new CharSequenceConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<String> predicate = constraint.notEmpty().predicates().get(0)
				.predicate();
		assertThat(predicate.test("foo")).isTrue();
		assertThat(predicate.test("")).isFalse();
	}

	@Test
	public void notBlank() {
		Predicate<String> predicate = constraint.notBlank().predicates().get(0)
				.predicate();
		assertThat(predicate.test("foo")).isTrue();
		assertThat(predicate.test("")).isFalse();
		assertThat(predicate.test("    ")).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<String> predicate = constraint.lessThan(3).predicates().get(0)
				.predicate();
		assertThat(predicate.test("ab")).isTrue();
		assertThat(predicate.test("abc")).isFalse();
		assertThat(predicate.test("\uD842\uDFB7田")).isTrue(); // surrogate pair
	}

	@Test
	public void lessThanOrEqual() {
		Predicate<String> predicate = constraint.lessThanOrEqual(3).predicates().get(0)
				.predicate();
		assertThat(predicate.test("ab")).isTrue();
		assertThat(predicate.test("abc")).isTrue();
		assertThat(predicate.test("abcd")).isFalse();
		assertThat(predicate.test("\uD842\uDFB7野屋")).isTrue(); // surrogate pair
	}

	@Test
	public void greaterThan() {
		Predicate<String> predicate = constraint.greaterThan(3).predicates().get(0)
				.predicate();
		assertThat(predicate.test("abcd")).isTrue();
		assertThat(predicate.test("abc")).isFalse();
		assertThat(predicate.test("\uD842\uDFB7野屋")).isFalse(); // surrogate pair
	}

	@Test
	public void greaterThanOrEqual() {
		Predicate<String> predicate = constraint.greaterThanOrEqual(3).predicates().get(0)
				.predicate();
		assertThat(predicate.test("abcd")).isTrue();
		assertThat(predicate.test("abc")).isTrue();
		assertThat(predicate.test("ab")).isFalse();
		assertThat(predicate.test("\uD842\uDFB7田")).isFalse(); // surrogate pair
	}

	@Test
	public void contains() {
		Predicate<String> predicate = constraint.contains("a").predicates().get(0)
				.predicate();
		assertThat(predicate.test("yavi")).isTrue();
		assertThat(predicate.test("yvi")).isFalse();
	}

	@Test
	public void email() {
		Predicate<String> predicate = constraint.email().predicates().get(0).predicate();
		assertThat(predicate.test("abc@example.com")).isTrue();
		assertThat(predicate.test("example.com")).isFalse();
		assertThat(predicate.test("")).isTrue();
	}

	@Test
	public void url() {
		Predicate<String> predicate = constraint.url().predicates().get(0).predicate();
		assertThat(predicate.test("http://example.com")).isTrue();
		assertThat(predicate.test("example.com")).isFalse();
		assertThat(predicate.test("")).isTrue();
	}

	@Test
	public void pattern() {
		Predicate<String> predicate = constraint.pattern("[0-9]{4}").predicates().get(0)
				.predicate();
		assertThat(predicate.test("1234")).isTrue();
		assertThat(predicate.test("134a")).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<String> predicate = constraint.fixedSize(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test("a")).isFalse();
		assertThat(predicate.test("ab")).isTrue();
		assertThat(predicate.test("abc")).isFalse();
	}

	@Test
	public void normalizeCombiningCharacter() {
		Predicate<String> predicate = new CharSequenceConstraint<String, String>()
				.fixedSize(2).predicates().get(0).predicate();
		assertThat(predicate.test("モジ" /* モシ\u3099 */)).isTrue();
	}

	@Test
	public void notNormalizeCombiningCharacter() {
		Predicate<String> predicate = new CharSequenceConstraint<String, String>()
				.normalizer(null).fixedSize(3).predicates().get(0).predicate();
		assertThat(predicate.test("モジ" /* モシ\u3099 */)).isTrue();
	}

	@Test
	public void ignoreIvsCharacter() {
		Predicate<String> predicate = new CharSequenceConstraint<String, String>()
				.fixedSize(1).predicates().get(0).predicate();
		assertThat(predicate.test("\uD842\uDF9F\uDB40\uDD00")).isTrue();
		assertThat(predicate.test("\u908A\uDB40\uDD07")).isTrue();
	}

	@Test
	public void notIgnoreIvsCharacter() {
		Predicate<String> predicate = new CharSequenceConstraint<String, String>()
				.variant(ops -> ops.ivs(IdeographicVariationSequence.NOT_IGNORE))
				.fixedSize(2).predicates().get(0).predicate();
		assertThat(predicate.test("\uD842\uDF9F\uDB40\uDD00")).isTrue();
		assertThat(predicate.test("\u908A\uDB40\uDD07")).isTrue();
	}

	@Test
	public void ignoreFvsCharacter() {
		Predicate<String> predicate = new CharSequenceConstraint<String, String>()
				.fixedSize(1).predicates().get(0).predicate();
		assertThat(predicate.test("ᠠ᠋")).isTrue();
		assertThat(predicate.test("ᠰ᠌")).isTrue();
	}

	@Test
	public void notIgnoreFvsCharacter() {
		Predicate<String> predicate = new CharSequenceConstraint<String, String>()
				.variant(ops -> ops.fvs(MongolianFreeVariationSelector.NOT_IGNORE))
				.fixedSize(2)
				.predicates().get(0).predicate();
		assertThat(predicate.test("ᠠ᠋")).isTrue();
		assertThat(predicate.test("ᠰ᠌")).isTrue();
	}
}