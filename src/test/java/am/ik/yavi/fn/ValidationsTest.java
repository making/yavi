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
package am.ik.yavi.fn;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationsTest {
	@Test
	void combine() {
		final Validation<List<String>, String> v1 = Validation.success("s1");
		final Validation<List<String>, String> v2 = Validation.success("s2");
		final Validation<List<String>, String> v3 = Validation.success("s3");

		final String s = Validations.combine(v1, v2, v3)
				.apply((s1, s2, s3) -> String.join("-", s1, s2, s3)).value();
		assertThat(s).isEqualTo("s1-s2-s3");
	}

	@Test
	void apply() {
		final Validation<List<String>, String> v1 = Validation.success("s1");
		final Validation<List<String>, String> v2 = Validation.success("s2");
		final Validation<List<String>, String> v3 = Validation.success("s3");

		final String s = Validations
				.apply((s1, s2, s3) -> String.join("-", s1, s2, s3), v1, v2, v3).value();
		assertThat(s).isEqualTo("s1-s2-s3");
	}

	@Test
	void sequenceValid() {
		final Validation<List<String>, List<Integer>> validation = Validations
				.sequence(Arrays.asList(Validation.success(1), Validation.success(2)));
		assertThat(validation.value()).containsExactly(1, 2);
	}

	@Test
	void sequenceInvalid() {
		final Validation<String, List<Integer>> validation = Validations
				.sequence(Arrays.asList(Validation.success(1),
						Validation.failure(Arrays.asList("e1", "e2")),
						Validation.success(2),
						Validation.failure(Arrays.asList("e3", "e4"))));
		assertThat(validation.errors()).containsExactly("e1", "e2", "e3", "e4");
	}

	@Test
	void traverseValid() {
		final Validation<String, List<Integer>> validation = Validations
				.traverse(Arrays.asList(1, 2), i -> Validation.success(i));
		assertThat(validation.value()).containsExactly(1, 2);
	}

	@Test
	void traverseInvalid() {
		final Validation<String, List<Integer>> validation = Validations
				.traverse(Arrays.asList(1, -1, 2, -2), i -> {
					if (i > 0) {
						return Validation.success(i);
					}
					else {
						return Validation.failure(
								Arrays.asList("e" + (-2 * i - 1), "e" + (-2 * i)));
					}
				});
		assertThat(validation.errors()).containsExactly("e1", "e2", "e3", "e4");
	}

	@Test
	void traverseIndexedInvalid() {
		final Validation<String, List<Integer>> validation = Validations
				.traverseIndexed(Arrays.asList(1, -1, 2, -2), (i, index) -> {
					if (i > 0) {
						return Validation.success(i);
					}
					else {
						return Validation.failure("e[" + index + "]");
					}
				});
		assertThat(validation.errors()).containsExactly("e[1]", "e[3]");
	}
}