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
				.predicates().get(0).predicate();
		assertThat(predicate.test(new BigInteger("101"))).isTrue();
		assertThat(predicate.test(new BigInteger("100"))).isFalse();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<BigInteger> predicate = constraint
				.greaterThanOrEquals(new BigInteger("100")).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new BigInteger("101"))).isTrue();
		assertThat(predicate.test(new BigInteger("100"))).isTrue();
		assertThat(predicate.test(new BigInteger("99"))).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<BigInteger> predicate = constraint.lessThan(new BigInteger("100"))
				.predicates().get(0).predicate();
		assertThat(predicate.test(new BigInteger("99"))).isTrue();
		assertThat(predicate.test(new BigInteger("100"))).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<BigInteger> predicate = constraint
				.lessThanOrEquals(new BigInteger("100")).predicates().get(0).predicate();
		assertThat(predicate.test(new BigInteger("99"))).isTrue();
		assertThat(predicate.test(new BigInteger("100"))).isTrue();
		assertThat(predicate.test(new BigInteger("101"))).isFalse();
	}
}