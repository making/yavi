/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MapConstraintTest {

	@Test
	void containsKey() {
		Predicate<Map<String, String>> predicate = retrievePredicate(c -> c.containsKey("foo"));
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(Collections.singletonMap("bar", "baz"))).isFalse();
	}

	@Test
	void containsValue() {
		Predicate<Map<String, String>> predicate = retrievePredicate(c -> c.containsValue("bar"));
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(Collections.singletonMap("foo", "baz"))).isFalse();
	}

	@Test
	void fixedSize() {
		Predicate<Map<String, String>> predicate = retrievePredicate(c -> c.fixedSize(2));
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isFalse();
		assertThat(predicate.test(new HashMap<String, String>() {
			{
				put("a", "b");
				put("b", "c");
			}
		})).isTrue();
		assertThat(predicate.test(new HashMap<String, String>() {
			{
				put("a", "b");
				put("b", "c");
				put("c", "d");
			}
		})).isFalse();
	}

	@Test
	void greaterThan() {
		Predicate<Map<String, String>> predicate = retrievePredicate(c -> c.greaterThan(2));
		assertThat(predicate.test(new HashMap<String, String>() {
			{
				put("a", "b");
				put("b", "c");
			}
		})).isFalse();
		assertThat(predicate.test(new HashMap<String, String>() {
			{
				put("a", "b");
				put("b", "c");
				put("c", "d");
			}
		})).isTrue();
	}

	@Test
	void greaterThanOrEqual() {
		Predicate<Map<String, String>> predicate = retrievePredicate(c -> c.greaterThanOrEqual(2));
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isFalse();
		assertThat(predicate.test(new HashMap<String, String>() {
			{
				put("a", "b");
				put("b", "c");
			}
		})).isTrue();
		assertThat(predicate.test(new HashMap<String, String>() {
			{
				put("a", "b");
				put("b", "c");
				put("c", "d");
			}
		})).isTrue();
	}

	@Test
	void lessThan() {
		Predicate<Map<String, String>> predicate = retrievePredicate(c -> c.lessThan(2));
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(new HashMap<String, String>() {
			{
				put("a", "b");
				put("b", "c");
			}
		})).isFalse();
	}

	@Test
	void lessThanOrEqual() {
		Predicate<Map<String, String>> predicate = retrievePredicate(c -> c.lessThanOrEqual(2));
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(new HashMap<String, String>() {
			{
				put("a", "b");
				put("b", "c");
			}
		})).isTrue();
		assertThat(predicate.test(new HashMap<String, String>() {
			{
				put("a", "b");
				put("b", "c");
				put("c", "d");
			}
		})).isFalse();
	}

	@Test
	void notEmpty() {
		Predicate<Map<String, String>> predicate = retrievePredicate(c -> c.notEmpty());
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(Collections.emptyMap())).isFalse();
	}

	 private static Predicate<Map<String, String>> retrievePredicate(Function<MapConstraint<Map<String, String>, String, String>, MapConstraint<Map<String, String>, String, String>> constraint) {
		 return constraint.apply(new MapConstraint<>()).predicates().peekFirst().predicate();
	 }
}
