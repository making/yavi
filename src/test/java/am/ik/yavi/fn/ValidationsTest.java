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

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationsTest {
	@Test
	void compose() {
		final Validation<List<String>, String> v1 = Validation.success("s1");
		final Validation<List<String>, String> v2 = Validation.success("s2");
		final Validation<List<String>, String> v3 = Validation.success("s3");

		final String s = Validations.compose(v1, v2, v3)
				.apply((s1, s2, s3) -> String.join("-", s1, s2, s3)).value();
		assertThat(s).isEqualTo("s1-s2-s3");
	}

	@Test
	void apply() {
		final Validation<List<String>, String> v1 = Validation.success("s1");
		final Validation<List<String>, String> v2 = Validation.success("s2");
		final Validation<List<String>, String> v3 = Validation.success("s3");

		final String s = Validations
				.apply(v1, v2, v3, (s1, s2, s3) -> String.join("-", s1, s2, s3)).value();
		assertThat(s).isEqualTo("s1-s2-s3");
	}
}