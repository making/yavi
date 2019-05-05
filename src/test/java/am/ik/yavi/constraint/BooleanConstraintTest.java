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

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BooleanConstraintTest {
	private BooleanConstraint<Boolean> constraint = new BooleanConstraint<>();

	@Test
	public void isTrue() {
		Predicate<Boolean> predicate = constraint.isTrue().predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(true)).isTrue();
		assertThat(predicate.test(false)).isFalse();
	}

	@Test
	public void isFalse() {
		Predicate<Boolean> predicate = constraint.isFalse().predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(true)).isFalse();
		assertThat(predicate.test(false)).isTrue();
	}
}
