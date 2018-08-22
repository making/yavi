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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionConstraintTest {
	private CollectionConstraint<List<String>, List<String>, String> constraint = new CollectionConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<List<String>> predicate = constraint.notEmpty().predicates().get(0)
				.predicate();
		assertThat(predicate.test(Collections.singletonList("foo"))).isTrue();
		assertThat(predicate.test(Collections.emptyList())).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<List<String>> predicate = constraint.lessThan(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(Collections.singletonList("foo"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isFalse();
	}

	@Test
	public void lessThanOrEqual() {
		Predicate<List<String>> predicate = constraint.lessThanOrEqual(2).predicates()
				.get(0).predicate();
		assertThat(predicate.test(Collections.singletonList("foo"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar", "baz"))).isFalse();
	}

	@Test
	public void greaterThan() {
		Predicate<List<String>> predicate = constraint.greaterThan(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isFalse();
		assertThat(predicate.test(Arrays.asList("foo", "bar", "baz"))).isTrue();
	}

	@Test
	public void greaterThanOrEqual() {
		Predicate<List<String>> predicate = constraint.greaterThanOrEqual(2).predicates()
				.get(0).predicate();
		assertThat(predicate.test(Collections.singletonList("foo"))).isFalse();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar", "baz"))).isTrue();
	}

	@Test
	public void contains() {
		Predicate<List<String>> predicate = constraint.contains("foo").predicates().get(0)
				.predicate();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isTrue();
		assertThat(predicate.test(Arrays.asList("bar", "baz"))).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<List<String>> predicate = constraint.fixedSize(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(Collections.singletonList("foo"))).isFalse();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar", "baz"))).isFalse();
	}
}