/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CollectionConstraint;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh432Test {

	@Test
	@SuppressWarnings("unchecked")
	void testDuplicatedElements() {
		Data u = new Data(Arrays.asList(1, 1, 1, 2, 3, 1, 1, 2, 3, 1, 4));
		Validator<Data> validator = ValidatorBuilder.of(Data.class)
			.constraint(Data::getList, "list", CollectionConstraint::unique)
			.build();
		ConstraintViolations violations = validator.validate(u);
		assertThat(violations.isValid()).isFalse();
		Object[] args = violations.get(0).args();
		assertThat(args).hasSize(2);
		assertThat(args[0]).isEqualTo("list");
		assertThat((Collection<Integer>) args[1]).containsExactly(1, 2, 3);
	}

	public static class Data {

		final List<Integer> list;

		public Data(List<Integer> list) {
			this.list = list;
		}

		public List<Integer> getList() {
			return list;
		}

	}

}
