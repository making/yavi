package am.ik.yavi.constraint.array;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class BooleanArrayConstraintTest {

    private BooleanArrayConstraint<boolean[]> constraint = new BooleanArrayConstraint<>();

    @Test
    void containsTest(){
        Predicate<boolean[]> predicate = constraint.contains(true).predicates().peekFirst().predicate();
        assertThat(predicate.test(new boolean[]{true,false})).isTrue();
        assertThat(predicate.test(new boolean[]{false,false})).isFalse();
    }

    @Test
    void onlyContainsTest(){
        Predicate<boolean[]> predicate = constraint.onlyContains(true).predicates().peekFirst().predicate();
        assertThat(predicate.test(new boolean[]{true,true,true})).isTrue();
        assertThat(predicate.test(new boolean[]{true,true,false})).isFalse();
    }

    @Test
    void sizeTestFailed(){
        Predicate<boolean[]> predicate = constraint.fixedSize(3).predicates().peekFirst().predicate();
        assertThat(predicate.test(new boolean[]{true,true})).isFalse();
    }

    @Test
    void sizeTestPass(){
        Predicate<boolean[]> predicate = constraint.fixedSize(3).predicates().peekFirst().predicate();
        assertThat(predicate.test(new boolean[]{true,true,true})).isTrue();
    }

}
