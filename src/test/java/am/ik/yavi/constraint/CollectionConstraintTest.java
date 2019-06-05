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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionConstraintTest {

	@Test
	 void contains() {
		Predicate<List<String>> predicate = retrievePredicate(c -> c.contains("foo"));
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isTrue();
		assertThat(predicate.test(Arrays.asList("bar", "baz"))).isFalse();
	}

	@Test
	 void fixedSize() {
		Predicate<List<String>> predicate = retrievePredicate(c -> c.fixedSize(2));
		assertThat(predicate.test(Collections.singletonList("foo"))).isFalse();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar", "baz"))).isFalse();
	}

	@Test
	 void greaterThan() {
		Predicate<List<String>> predicate = retrievePredicate(c -> c.greaterThan(2));
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isFalse();
		assertThat(predicate.test(Arrays.asList("foo", "bar", "baz"))).isTrue();
	}

	@Test
	 void greaterThanOrEqual() {
		Predicate<List<String>> predicate = retrievePredicate(c -> c.greaterThanOrEqual(2));
		assertThat(predicate.test(Collections.singletonList("foo"))).isFalse();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar", "baz"))).isTrue();
	}

	@Test
	 void lessThan() {
		Predicate<List<String>> predicate = retrievePredicate(c -> c.lessThan(2));
		assertThat(predicate.test(Collections.singletonList("foo"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isFalse();
	}

	@Test
	 void lessThanOrEqual() {
		Predicate<List<String>> predicate = retrievePredicate(c -> c.lessThanOrEqual(2));
		assertThat(predicate.test(Collections.singletonList("foo"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar", "baz"))).isFalse();
	}

	@Test
	 void notEmpty() {
		Predicate<List<String>> predicate = retrievePredicate(c -> c.notEmpty());
		assertThat(predicate.test(Collections.singletonList("foo"))).isTrue();
		assertThat(predicate.test(Collections.emptyList())).isFalse();
	}

	private static Predicate<List<String>> retrievePredicate(Function<CollectionConstraint<List<String>, List<String>, String>, CollectionConstraint<List<String>, List<String>, String>> constraint) {
		return constraint.apply(new CollectionConstraint<>()).predicates().peekFirst().predicate();
	}
}
