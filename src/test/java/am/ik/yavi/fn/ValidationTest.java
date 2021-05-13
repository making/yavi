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
		final Validation<String, String> validation = Validation
				.failure(Arrays.asList("errors1", "errors2"));
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
		assertThat(validation)
				.isEqualTo(Validation.failure(Arrays.asList("errors1", "errors2")));
		assertThat(validation.hashCode()).isEqualTo(
				Validation.failure(Arrays.asList("errors1", "errors2")).hashCode());
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
		final Validation<String, String> validation = Validation
				.failure(Arrays.asList("errors1", "errors2"));
		final String fold = validation.fold(e -> String.join(",", e),
				String::toUpperCase);
		assertThat(fold).isEqualTo("errors1,errors2");
	}

	@Test
	void successToEither() {
		final Validation<String, String> validation = Validation.success("test");
		final Either<List<String>, String> either = validation.toEither();
		assertThat(either.isRight()).isTrue();
		assertThat(either.right().get()).isEqualTo("test");
	}

	@Test
	void failureToEither() {
		final Validation<String, String> validation = Validation
				.failure(Arrays.asList("errors1", "errors2"));
		final Either<List<String>, String> either = validation.toEither();
		assertThat(either.isLeft()).isTrue();
		assertThat(either.left().get()).isEqualTo(Arrays.asList("errors1", "errors2"));
	}

	@Test
	void successFromEither() {
		final Either<List<String>, String> either = Either.right("test");
		final Validation<String, String> validation = Validation.fromEither(either);
		assertThat(validation.isValid()).isTrue();
		assertThat(validation.value()).isEqualTo("test");
	}

	@Test
	void failureFromEither() {
		final Either<List<String>, String> either = Either
				.left(Arrays.asList("errors1", "errors2"));
		final Validation<String, String> validation = Validation.fromEither(either);
		assertThat(validation.isValid()).isFalse();
		assertThat(validation.errors()).isEqualTo(Arrays.asList("errors1", "errors2"));
	}

	@Test
	void compose_all_valid() {
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
		final Validation<String, String> v11 = Validation.success("s11");
		final Validation<String, String> v12 = Validation.success("s12");
		final Validation<String, String> v13 = Validation.success("s13");
		final Validation<String, String> v14 = Validation.success("s14");
		final Validation<String, String> v15 = Validation.success("s15");
		final Validation<String, String> v16 = Validation.success("s16");

		final Validation<String, String> validation = v1.compose(v2).compose(v3)
				.compose(v4).compose(v5).compose(v6).compose(v7).compose(v8).compose(v9)
				.compose(v10).compose(v11).compose(v12).compose(v13).compose(v14)
				.compose(v15).compose(v16)
				.apply((s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15,
						s16) -> String.join(", ", s1, s2, s3, s4, s5, s6, s7, s8, s9, s10,
						s11, s12, s13, s14, s15, s16));
		assertThat(validation.isValid()).isTrue();
		assertThat(validation.value()).isEqualTo(
				"s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16");
	}

	@Test
	void compose_all_invalid() {
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
		final Validation<String, String> v11 = Validation.failure("f11");
		final Validation<String, String> v12 = Validation.failure("f12");
		final Validation<String, String> v13 = Validation.failure("f13");
		final Validation<String, String> v14 = Validation.failure("f14");
		final Validation<String, String> v15 = Validation.failure("f15");
		final Validation<String, String> v16 = Validation.failure("f16");

		final Validation<String, String> validation = v1.compose(v2).compose(v3)
				.compose(v4).compose(v5).compose(v6).compose(v7).compose(v8).compose(v9)
				.compose(v10).compose(v11).compose(v12).compose(v13).compose(v14)
				.compose(v15).compose(v16)
				.apply((s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15,
						s16) -> String.join(", ", s1, s2, s3, s4, s5, s6, s7, s8, s9, s10,
						s11, s12, s13, s14, s15, s16));

		assertThat(validation.isValid()).isFalse();
		assertThat(validation.errors()).containsExactly("f1", "f2", "f3", "f4", "f5", "f6",
				"f7", "f8", "f9", "f10", "f11", "f12", "f13", "f14", "f15", "f16");
	}

	@Test
	void compose_first_invalid() {
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
		final Validation<String, String> v11 = Validation.success("s11");
		final Validation<String, String> v12 = Validation.success("s12");
		final Validation<String, String> v13 = Validation.success("s13");
		final Validation<String, String> v14 = Validation.success("s14");
		final Validation<String, String> v15 = Validation.success("s15");
		final Validation<String, String> v16 = Validation.success("s16");

		final Validation<String, String> validation = v1.compose(v2).compose(v3)
				.compose(v4).compose(v5).compose(v6).compose(v7).compose(v8).compose(v9)
				.compose(v10).compose(v11).compose(v12).compose(v13).compose(v14)
				.compose(v15).compose(v16)
				.apply((s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15,
						s16) -> String.join(", ", s1, s2, s3, s4, s5, s6, s7, s8, s9, s10,
						s11, s12, s13, s14, s15, s16));

		assertThat(validation.isValid()).isFalse();
		assertThat(validation.errors()).containsExactly("f1");
	}

	@Test
	void compose_last_invalid() {
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
		final Validation<String, String> v11 = Validation.success("s11");
		final Validation<String, String> v12 = Validation.success("s12");
		final Validation<String, String> v13 = Validation.success("s13");
		final Validation<String, String> v14 = Validation.success("s14");
		final Validation<String, String> v15 = Validation.success("s15");
		final Validation<String, String> v16 = Validation.failure("f16");

		final Validation<String, String> validation = v1.compose(v2).compose(v3)
				.compose(v4).compose(v5).compose(v6).compose(v7).compose(v8).compose(v9)
				.compose(v10).compose(v11).compose(v12).compose(v13).compose(v14)
				.compose(v15).compose(v16)
				.apply((s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15,
						s16) -> String.join(", ", s1, s2, s3, s4, s5, s6, s7, s8, s9, s10,
						s11, s12, s13, s14, s15, s16));

		assertThat(validation.isValid()).isFalse();
		assertThat(validation.errors()).containsExactly("f16");
	}

	@Test
	void orElseThrowSuccess() {
		final String s = Validation.<List<String>, String>success("test")
				.orElseThrow(errors -> new IllegalArgumentException("errors=" + errors));
		assertThat(s).isEqualTo("test");
	}

	@Test
	void orElseThrowFailure() {
		assertThatThrownBy(() -> Validation
				.<String, String>failure(Arrays.asList("e1", "e2"))
				.orElseThrow(errors -> new IllegalArgumentException("errors=" + errors)))
				.hasMessage("errors=[e1, e2]")
				.isInstanceOf(IllegalArgumentException.class);
	}
}