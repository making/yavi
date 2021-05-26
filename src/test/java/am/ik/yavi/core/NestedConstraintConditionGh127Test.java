package am.ik.yavi.core;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CharSequenceConstraint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NestedConstraintConditionGh127Test {

    @Test
    void shouldReturnProperViolationForOneLevelNestedObjectWithConditionalValidator() {
        ConstraintViolations violations = D.CONDITIONAL_VALIDATOR.validate(new D(new E(null)));

        assertThat(violations.isValid()).isFalse();
        assertThat(violations.violations())
                .extracting(ConstraintViolation::name)
                .containsOnly(
                        "e.value"
                );
        assertThat(violations.violations())
                .extracting(ConstraintViolation::message)
                .containsOnly(
                        "\"e.value\" must not be blank"
                );
    }

    @Test
    void shouldReturnProperViolationForOneLevelNestedObjectWithoutConditionalValidator() {
        ConstraintViolations violations = D.VALIDATOR.validate(new D(new E(null)));

        assertThat(violations.isValid()).isFalse();
        assertThat(violations.violations())
                .extracting(ConstraintViolation::name)
                .containsOnly(
                        "e.value"
                );
        assertThat(violations.violations())
                .extracting(ConstraintViolation::message)
                .containsOnly(
                        "\"e.value\" must not be blank"
                );
    }

    @Test
    void shouldReturnProperViolationWhenTwiceConditionalValidatorIsUsed() {
        Validator<D> validator = ValidatorBuilder.of(D.class)
                .constraintOnCondition((c, g) -> true, D.CONDITIONAL_VALIDATOR)
                .build();

        ConstraintViolations violations = validator.validate(new D(new E(null)));

        assertThat(violations.isValid()).isFalse();
        assertThat(violations.violations())
                .extracting(ConstraintViolation::name)
                .containsOnly(
                        "e.value"
                );
        assertThat(violations.violations())
                .extracting(ConstraintViolation::message)
                .containsOnly(
                        "\"e.value\" must not be blank"
                );
    }

    @Test
    void shouldReturnProperViolationForFourLevelNestedObjectWithConditionalValidator() {
        Validator<A> validator = ValidatorBuilder.of(A.class)
                .nest(A::getB, "b",
                        b -> b.nest(B::getC, "c",
                                c -> c.nest(C::getD, "d", D.CONDITIONAL_VALIDATOR)))
                .build();

        ConstraintViolations violations = validator.validate(new A(new B(new C(new D(new E(null))))));

        assertThat(violations.isValid()).isFalse();
        assertThat(violations.violations())
                .extracting(ConstraintViolation::name)
                .containsOnly(
                        "b.c.d.e.value"
                );
        assertThat(violations.violations())
                .extracting(ConstraintViolation::message)
                .containsOnly(
                        "\"b.c.d.e.value\" must not be blank"
                );
    }

    @Test
    void shouldReturnProperViolationForFourLevelNestedObjectWithoutConditionalValidator() {
        Validator<A> validator = ValidatorBuilder.of(A.class)
                .nest(A::getB, "b",
                        b -> b.nest(B::getC, "c",
                                c -> c.nest(C::getD, "d", D.VALIDATOR)))
                .build();

        ConstraintViolations violations = validator.validate(new A(new B(new C(new D(new E(null))))));

        assertThat(violations.isValid()).isFalse();
        assertThat(violations.violations())
                .extracting(ConstraintViolation::name)
                .containsOnly(
                        "b.c.d.e.value"
                );
        assertThat(violations.violations())
                .extracting(ConstraintViolation::message)
                .containsOnly(
                        "\"b.c.d.e.value\" must not be blank"
                );
    }

    @Test
    void shouldReturnProperViolationWhenValidatorHasManyNestedConditionalValidators() {
        Validator<A> validator = ValidatorBuilder.of(A.class)
                .constraintOnCondition((c, g) -> true, ValidatorBuilder.of(A.class)
                        .nest(A::getB, "b", ValidatorBuilder.of(B.class)
                                .constraintOnCondition((c1, g1) -> true, ValidatorBuilder.of(B.class)
                                        .nest(B::getC, "c", ValidatorBuilder.of(C.class)
                                                .constraintOnCondition((c2, g2) -> true, ValidatorBuilder.of(C.class)
                                                        .nest(C::getD, "d", D.CONDITIONAL_VALIDATOR).build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();


        ConstraintViolations violations = validator.validate(new A(new B(new C(new D(new E(null))))));

        assertThat(violations.isValid()).isFalse();
        assertThat(violations.violations())
                .extracting(ConstraintViolation::name)
                .containsOnly(
                        "b.c.d.e.value"
                );
        assertThat(violations.violations())
                .extracting(ConstraintViolation::message)
                .containsOnly(
                        "\"b.c.d.e.value\" must not be blank"
                );
    }

    private static class A {

        private final B b;

        public A(B b) {
            this.b = b;
        }

        public B getB() {
            return b;
        }
    }

    private static class B {

        private final C c;

        public B(C c) {
            this.c = c;
        }

        public C getC() {
            return c;
        }
    }

    private static class C {

        private final D d;

        public C(D d) {
            this.d = d;
        }

        public D getD() {
            return d;
        }
    }

    private static class D {
        private static final Validator<D> VALIDATOR = ValidatorBuilder.of(D.class)
                .nest(D::getE, "e", E.VALIDATOR)
                .build();
        private static final Validator<D> CONDITIONAL_VALIDATOR = ValidatorBuilder.of(D.class)
                .constraintOnCondition((c,g) -> true, VALIDATOR)
                .build();

        private final E e;

        public D(E e) {
            this.e = e;
        }

        public E getE() {
            return e;
        }
    }

    private static class E {
        private static final Validator<E> VALIDATOR = ValidatorBuilder.of(E.class)
                .constraint(E::getValue, "value", CharSequenceConstraint::notBlank)
                .build();

        private final String value;

        public E(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }
}
