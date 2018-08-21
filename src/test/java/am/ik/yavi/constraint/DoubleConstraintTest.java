package am.ik.yavi.constraint;

import org.junit.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class DoubleConstraintTest {
    private DoubleConstraint<Double> constraint = new DoubleConstraint<>();

    @Test
    public void greaterThan() {
        Predicate<Double> predicate = constraint.greaterThan(100.0).holders().get(0)
                .predicate();
        assertThat(predicate.test(101.0)).isTrue();
        assertThat(predicate.test(100.0)).isFalse();
    }

    @Test
    public void greaterThanOrEquals() {
        Predicate<Double> predicate = constraint.greaterThanOrEquals(100.0).holders()
                .get(0).predicate();
        assertThat(predicate.test(101.0)).isTrue();
        assertThat(predicate.test(100.0)).isTrue();
        assertThat(predicate.test(99.0)).isFalse();
    }

    @Test
    public void lessThan() {
        Predicate<Double> predicate = constraint.lessThan(100.0).holders().get(0)
                .predicate();
        assertThat(predicate.test(99.0)).isTrue();
        assertThat(predicate.test(100.0)).isFalse();
    }

    @Test
    public void lessThanOrEquals() {
        Predicate<Double> predicate = constraint.lessThanOrEquals(100.0).holders().get(0)
                .predicate();
        assertThat(predicate.test(99.0)).isTrue();
        assertThat(predicate.test(100.0)).isTrue();
        assertThat(predicate.test(101.0)).isFalse();
    }
}