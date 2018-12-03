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

import java.math.BigInteger;
import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BigIntegerConstraintTest {
	private BigIntegerConstraint<BigInteger> constraint = new BigIntegerConstraint<>();

	@Test
	public void greaterThan() {
		Predicate<BigInteger> predicate = constraint.greaterThan(new BigInteger("100"))
				.predicates().peekFirst().predicate();
		assertThat(predicate.test(new BigInteger("101"))).isTrue();
		assertThat(predicate.test(new BigInteger("100"))).isFalse();
	}

	@Test
	public void greaterThanOrEqual() {
		Predicate<BigInteger> predicate = constraint
				.greaterThanOrEqual(new BigInteger("100")).predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new BigInteger("101"))).isTrue();
		assertThat(predicate.test(new BigInteger("100"))).isTrue();
		assertThat(predicate.test(new BigInteger("99"))).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<BigInteger> predicate = constraint.lessThan(new BigInteger("100"))
				.predicates().peekFirst().predicate();
		assertThat(predicate.test(new BigInteger("99"))).isTrue();
		assertThat(predicate.test(new BigInteger("100"))).isFalse();
	}

	@Test
	public void lessThanOrEqual() {
		Predicate<BigInteger> predicate = constraint
				.lessThanOrEqual(new BigInteger("100")).predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new BigInteger("99"))).isTrue();
		assertThat(predicate.test(new BigInteger("100"))).isTrue();
		assertThat(predicate.test(new BigInteger("101"))).isFalse();
	}
}