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