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
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationTest {

	@Test
	void success() {
		final Validation<String, String> validation = Validation.success("test");
		assertThat(validation.isValid()).isTrue();
		assertThat(validation.value()).isEqualTo("test");
		assertThatThrownBy(validation::errors).isInstanceOf(NoSuchElementException.class);
		assertThat(validation.map(String::toUpperCase).value()).isEqualTo("TEST");
		assertThat(validation
				.mapErrors(x -> x.stream().map(String::toUpperCase).collect(toList()))
				.value()).isEqualTo("test");
		assertThat(validation.flatMap(s -> Validation.success("hello " + s)).value())
				.isEqualTo("hello test");
		assertThat(validation).isEqualTo(Validation.success("test"));
		assertThat(validation.hashCode())
				.isEqualTo(Validation.success("test").hashCode());
	}

	@Test
	void failure() {
		final Validation<String, String> validation = Validation.failure("errors1",
				"errors2");
		assertThat(validation.isValid()).isFalse();
		assertThatThrownBy(validation::value).isInstanceOf(NoSuchElementException.class);
		assertThat(validation.errors()).isEqualTo(Arrays.asList("errors1", "errors2"));
		assertThat(validation.map(String::toUpperCase).errors())
				.isEqualTo(Arrays.asList("errors1", "errors2"));
		assertThat(validation
				.mapErrors(x -> x.stream().map(String::toUpperCase).collect(toList()))
				.errors()).isEqualTo(Arrays.asList("ERRORS1", "ERRORS2"));
		assertThat(validation.flatMap(s -> Validation.success("hello " + s)).errors())
				.isEqualTo(Arrays.asList("errors1", "errors2"));
		assertThat(validation).isEqualTo(Validation.failure("errors1", "errors2"));
		assertThat(validation.hashCode())
				.isEqualTo(Validation.failure("errors1", "errors2").hashCode());
	}

	@Test
	void foldSuccess() {
		final Validation<String, String> validation = Validation.success("test");
		final String fold = validation.fold(e -> String.join(",", e),
				String::toUpperCase);
		assertThat(fold).isEqualTo("TEST");
	}

	@Test
	void foldFailure() {
		final Validation<String, String> validation = Validation.failure("errors1",
				"errors2");
		final String fold = validation.fold(e -> String.join(",", e),
				String::toUpperCase);
		assertThat(fold).isEqualTo("errors1,errors2");
	}

	@Test
	void bimapSuccess() {
		final Validation<String, String> validation = Validation.success("test");
		final Validation<String, String> bimap = validation.bimap(
				errors -> errors.stream().map(String::toUpperCase).collect(toList()),
				String::toUpperCase);
		assertThat(bimap.isValid()).isTrue();
		assertThat(bimap.value()).isEqualTo("TEST");
	}

	@Test
	void bimapFailure() {
		final Validation<String, String> validation = Validation.failure("errors1",
				"errors2");
		final Validation<String, String> bimap = validation.bimap(
				errors -> errors.stream().map(String::toUpperCase).collect(toList()),
				String::toUpperCase);
		assertThat(bimap.isValid()).isFalse();
		assertThat(bimap.errors()).isEqualTo(Arrays.asList("ERRORS1", "ERRORS2"));
	}

	@Test
	void combine_all_valid() {
		final Validation<String, String> v1 = Validation.success("s1");
		final Validation<String, String> v2 = Validation.success("s2");
		final Validation<String, String> v3 = Validation.success("s3");
		final Validation<String, String> v4 = Validation.success("s4");
		final Validation<String, String> v5 = Validation.success("s5");
		final Validation<String, String> v6 = Validation.success("s6");
		final Validation<String, String> v7 = Validation.success("s7");
		final Validation<String, String> v8 = Validation.success("s8");
		final Validation<String, String> v9 = Validation.success("s9");
		final Validation<String, String> v10 = Validation.success("s10");

		final Validation<String, String> validation = v1.combine(v2).combine(v3)
				.combine(v4).combine(v5).combine(v6).combine(v7).combine(v8).combine(v9)
				.combine(v10).apply((s1, s2, s3, s4, s5, s6, s7, s8, s9, s10) -> String
						.join(", ", s1, s2, s3, s4, s5, s6, s7, s8, s9, s10));
		assertThat(validation.isValid()).isTrue();
		assertThat(validation.value())
				.isEqualTo("s1, s2, s3, s4, s5, s6, s7, s8, s9, s10");
	}

	@Test
	void combine_all_invalid() {
		final Validation<String, String> v1 = Validation.failure("f1");
		final Validation<String, String> v2 = Validation.failure("f2");
		final Validation<String, String> v3 = Validation.failure("f3");
		final Validation<String, String> v4 = Validation.failure("f4");
		final Validation<String, String> v5 = Validation.failure("f5");
		final Validation<String, String> v6 = Validation.failure("f6");
		final Validation<String, String> v7 = Validation.failure("f7");
		final Validation<String, String> v8 = Validation.failure("f8");
		final Validation<String, String> v9 = Validation.failure("f9");
		final Validation<String, String> v10 = Validation.failure("f10");

		final Validation<String, String> validation = v1.combine(v2).combine(v3)
				.combine(v4).combine(v5).combine(v6).combine(v7).combine(v8).combine(v9)
				.combine(v10).apply((s1, s2, s3, s4, s5, s6, s7, s8, s9, s10) -> String
						.join(", ", s1, s2, s3, s4, s5, s6, s7, s8, s9, s10));

		assertThat(validation.isValid()).isFalse();
		assertThat(validation.errors()).containsExactly("f1", "f2", "f3", "f4", "f5",
				"f6", "f7", "f8", "f9", "f10");
	}

	@Test
	void combine_first_invalid() {
		final Validation<String, String> v1 = Validation.failure("f1");
		final Validation<String, String> v2 = Validation.success("s2");
		final Validation<String, String> v3 = Validation.success("s3");
		final Validation<String, String> v4 = Validation.success("s4");
		final Validation<String, String> v5 = Validation.success("s5");
		final Validation<String, String> v6 = Validation.success("s6");
		final Validation<String, String> v7 = Validation.success("s7");
		final Validation<String, String> v8 = Validation.success("s8");
		final Validation<String, String> v9 = Validation.success("s9");
		final Validation<String, String> v10 = Validation.success("s10");

		final Validation<String, String> validation = v1.combine(v2).combine(v3)
				.combine(v4).combine(v5).combine(v6).combine(v7).combine(v8).combine(v9)
				.combine(v10).apply((s1, s2, s3, s4, s5, s6, s7, s8, s9, s10) -> String
						.join(", ", s1, s2, s3, s4, s5, s6, s7, s8, s9, s10));

		assertThat(validation.isValid()).isFalse();
		assertThat(validation.errors()).containsExactly("f1");
	}

	@Test
	void combine_last_invalid() {
		final Validation<String, String> v1 = Validation.success("s1");
		final Validation<String, String> v2 = Validation.success("s2");
		final Validation<String, String> v3 = Validation.success("s3");
		final Validation<String, String> v4 = Validation.success("s4");
		final Validation<String, String> v5 = Validation.success("s5");
		final Validation<String, String> v6 = Validation.success("s6");
		final Validation<String, String> v7 = Validation.success("s7");
		final Validation<String, String> v8 = Validation.success("s8");
		final Validation<String, String> v9 = Validation.success("s9");
		final Validation<String, String> v10 = Validation.failure("f10");

		final Validation<String, String> validation = v1.combine(v2).combine(v3)
				.combine(v4).combine(v5).combine(v6).combine(v7).combine(v8).combine(v9)
				.combine(v10).apply((s1, s2, s3, s4, s5, s6, s7, s8, s9, s10) -> String
						.join(", ", s1, s2, s3, s4, s5, s6, s7, s8, s9, s10));

		assertThat(validation.isValid()).isFalse();
		assertThat(validation.errors()).containsExactly("f10");
	}

	@Test
	void orElseThrowSuccess() {
		final String s = Validation.<String, String> success("test")
				.orElseThrow(errors -> new IllegalArgumentException("errors=" + errors));
		assertThat(s).isEqualTo("test");
	}

	@Test
	void orElseThrowFailure() {
		assertThatThrownBy(() -> Validation
				.<String, String> failure(Arrays.asList("e1", "e2"))
				.orElseThrow(errors -> new IllegalArgumentException("errors=" + errors)))
				.hasMessage("errors=[e1, e2]")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void orElseGetSuccess() {
		final String s = Validation.<String, String> success("test")
				.orElseGet(errors -> String.join(",", errors));
		assertThat(s).isEqualTo("test");
	}

	@Test
	void orElseGetFailure() {
		final String s = Validation.<String, String> failure("e1", "e2")
				.orElseGet(errors -> String.join(",", errors));
		assertThat(s).isEqualTo("e1,e2");
	}
}
