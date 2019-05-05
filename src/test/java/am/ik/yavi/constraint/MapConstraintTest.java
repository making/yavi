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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MapConstraintTest {
	private MapConstraint<Map<String, String>, String, String> constraint = new MapConstraint<>();

	@Test
	public void containsKey() {
		Predicate<Map<String, String>> predicate = constraint.containsKey("foo")
				.predicates().peekFirst().predicate();
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(Collections.singletonMap("bar", "baz"))).isFalse();
	}

	@Test
	public void containsValue() {
		Predicate<Map<String, String>> predicate = constraint.containsValue("bar")
				.predicates().peekFirst().predicate();
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(Collections.singletonMap("foo", "baz"))).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<Map<String, String>> predicate = constraint.fixedSize(2).predicates()
				.peekFirst().predicate();
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
	public void greaterThan() {
		Predicate<Map<String, String>> predicate = constraint.greaterThan(2).predicates()
				.peekFirst().predicate();
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
	public void greaterThanOrEqual() {
		Predicate<Map<String, String>> predicate = constraint.greaterThanOrEqual(2)
				.predicates().peekFirst().predicate();
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
	public void lessThan() {
		Predicate<Map<String, String>> predicate = constraint.lessThan(2).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(new HashMap<String, String>() {
			{
				put("a", "b");
				put("b", "c");
			}
		})).isFalse();
	}

	@Test
	public void lessThanOrEqual() {
		Predicate<Map<String, String>> predicate = constraint.lessThanOrEqual(2)
				.predicates().peekFirst().predicate();
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
	public void notEmpty() {
		Predicate<Map<String, String>> predicate = constraint.notEmpty().predicates()
				.peekFirst().predicate();
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(Collections.emptyMap())).isFalse();
	}
}
