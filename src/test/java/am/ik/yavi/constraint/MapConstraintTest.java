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
	public void notEmpty() {
		Predicate<Map<String, String>> predicate = constraint.notEmpty().holders().get(0)
				.predicate();
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(Collections.emptyMap())).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<Map<String, String>> predicate = constraint.lessThan(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(new HashMap<String, String>() {
			{
				put("a", "b");
				put("b", "c");
			}
		})).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<Map<String, String>> predicate = constraint.lessThanOrEquals(2)
				.holders().get(0).predicate();
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
	public void greaterThan() {
		Predicate<Map<String, String>> predicate = constraint.greaterThan(2).holders()
				.get(0).predicate();
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
	public void greaterThanOrEquals() {
		Predicate<Map<String, String>> predicate = constraint.greaterThanOrEquals(2)
				.holders().get(0).predicate();
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
	public void containsValue() {
		Predicate<Map<String, String>> predicate = constraint.containsValue("bar")
				.holders().get(0).predicate();
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(Collections.singletonMap("foo", "baz"))).isFalse();
	}

	@Test
	public void containsKey() {
		Predicate<Map<String, String>> predicate = constraint.containsKey("foo").holders()
				.get(0).predicate();
		assertThat(predicate.test(Collections.singletonMap("foo", "bar"))).isTrue();
		assertThat(predicate.test(Collections.singletonMap("bar", "baz"))).isFalse();
	}
}