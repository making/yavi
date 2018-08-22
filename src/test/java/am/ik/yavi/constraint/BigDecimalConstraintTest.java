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

import java.math.BigDecimal;
import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BigDecimalConstraintTest {
	private BigDecimalConstraint<BigDecimal> constraint = new BigDecimalConstraint<>();

	@Test
	public void greaterThan() {
		Predicate<BigDecimal> predicate = constraint.greaterThan(new BigDecimal("100"))
				.predicates().get(0).predicate();
		assertThat(predicate.test(new BigDecimal("101"))).isTrue();
		assertThat(predicate.test(new BigDecimal("100"))).isFalse();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<BigDecimal> predicate = constraint
				.greaterThanOrEquals(new BigDecimal("100")).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new BigDecimal("101"))).isTrue();
		assertThat(predicate.test(new BigDecimal("100"))).isTrue();
		assertThat(predicate.test(new BigDecimal("99"))).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<BigDecimal> predicate = constraint.lessThan(new BigDecimal("100"))
				.predicates().get(0).predicate();
		assertThat(predicate.test(new BigDecimal("99"))).isTrue();
		assertThat(predicate.test(new BigDecimal("100"))).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<BigDecimal> predicate = constraint
				.lessThanOrEquals(new BigDecimal("100")).predicates().get(0).predicate();
		assertThat(predicate.test(new BigDecimal("99"))).isTrue();
		assertThat(predicate.test(new BigDecimal("100"))).isTrue();
		assertThat(predicate.test(new BigDecimal("101"))).isFalse();
	}
}