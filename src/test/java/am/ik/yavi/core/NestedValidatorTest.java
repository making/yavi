/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Objects;
import org.jspecify.annotations.Nullable;
import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CharSequenceConstraint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static am.ik.yavi.core.Group.CREATE;
import static am.ik.yavi.core.Group.UPDATE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class NestedValidatorTest extends AbstractNestedValidatorTest {

	@Override
	protected Validator<Address> validator() {
		return ValidatorBuilder.<Address>of()
			.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
			.nest(Address::country, "country", Country.validator())
			.nestIfPresent(Address::phoneNumber, "phoneNumber", PhoneNumber.validator())
			.build();
	}

	private ConstraintCondition<IntRange> whenAllOfNumbersNotNull = (r, g) -> r.small != null && r.big != null;

	private Validator<IntRange> thenCompareNumbers = ValidatorBuilder.of(IntRange.class)
		.constraintOnTarget(r -> Objects.requireNonNull(r.big) > Objects.requireNonNull(r.small), "big", "",
				"[big] must be greater than [small]")
		.build();

	private Validator<IntRange> intRangeValidator = ValidatorBuilder.of(IntRange.class)
		.constraint((ValidatorBuilder.ToInteger<IntRange>) r -> r.small, "small", Constraint::notNull)
		.constraint((ValidatorBuilder.ToInteger<IntRange>) r -> r.big, "big", Constraint::notNull)
		.constraintOnCondition(whenAllOfNumbersNotNull, thenCompareNumbers)
		.build();

	private Validator<NestedObject> nestedObjectValidator = ValidatorBuilder.<NestedObject>of()
		.constraintOnCondition(CREATE.toCondition(), b -> b.constraint(NestedObject::getId, "id", Constraint::isNull))
		.constraintOnCondition(UPDATE.toCondition(), b -> b.constraint(NestedObject::getId, "id", Constraint::notNull))
		.constraint(NestedObject::getText, "text", CharSequenceConstraint::notBlank)
		.build();

	private Validator<MainObject> mainObjectValidator = ValidatorBuilder.<MainObject>of()
		.constraintOnCondition(CREATE.toCondition(), b -> b.constraint(MainObject::getId, "id", Constraint::isNull))
		.constraintOnCondition(UPDATE.toCondition(), b -> b.constraint(MainObject::getId, "id", Constraint::notNull))
		.nest(MainObject::getNested, "nested", nestedObjectValidator)
		.build();

	private Validator<MainObject> mainObjectIfPresentValidator = ValidatorBuilder.<MainObject>of()
		.constraintOnCondition(CREATE.toCondition(), b -> b.constraint(MainObject::getId, "id", Constraint::isNull))
		.constraintOnCondition(UPDATE.toCondition(), b -> b.constraint(MainObject::getId, "id", Constraint::notNull))
		.nestIfPresent(MainObject::getNested, "nested", nestedObjectValidator)
		.build();

	private Validator<NestedObject> nestedObjectValidator_GH29 = ValidatorBuilder.<NestedObject>of()
		.constraintOnCondition(CREATE.toCondition(), b -> b.constraint(NestedObject::getId, "id", Constraint::isNull))
		.constraintOnCondition(UPDATE.toCondition(), b -> b.constraint(NestedObject::getId, "id", Constraint::notNull))
		.constraint(NestedObject::getText, "text", CharSequenceConstraint::notBlank)
		.build();

	private Validator<MainObject> mainObjectValidator_GH29 = ValidatorBuilder.<MainObject>of()
		.constraintOnCondition(CREATE.toCondition(), b -> b.constraint(MainObject::getId, "id", Constraint::isNull))
		.constraintOnCondition(UPDATE.toCondition(), b -> b.constraint(MainObject::getId, "id", Constraint::notNull))
		.constraintOnCondition(CREATE.toCondition(),
				b -> b.nest(MainObject::getNested, "nested", nestedObjectValidator_GH29))
		.constraintOnCondition(UPDATE.toCondition(),
				b -> b.nest(MainObject::getNested, "nested", nestedObjectValidator_GH29))
		.build();

	private Validator<NestedObject> nestedObjectValidator_GH90 = ValidatorBuilder.<NestedObject>of()
		.constraintOnCondition(CREATE.toCondition(), b -> b.constraint(NestedObject::getId, "id", Constraint::isNull))
		.constraintOnCondition(UPDATE.toCondition(), b -> b.constraint(NestedObject::getId, "id", Constraint::notNull))
		.constraint(NestedObject::getText, "text", CharSequenceConstraint::notBlank)
		.forEach(NestedObject::getIntRanges, "intRanges", intRangeValidator)
		.build();

	private Validator<MainObject> mainObjectValidator_GH90 = ValidatorBuilder.<MainObject>of()
		.constraintOnCondition(CREATE.toCondition(), b -> b.constraint(MainObject::getId, "id", Constraint::isNull))
		.constraintOnCondition(UPDATE.toCondition(), b -> b.constraint(MainObject::getId, "id", Constraint::notNull))
		.nest(MainObject::getNested, "nested", nestedObjectValidator_GH90)
		.build();

	@Test
	void shouldBeInValid_GH28() {
		MainObject target = new MainObject();

		ConstraintViolations result = mainObjectValidator.validate(target, CREATE);

		assertThat(result.isValid()).isFalse();
		assertThat(result.get(0).name()).isEqualTo("nested");
		assertThat(result.get(0).messageKey()).isEqualTo("object.notNull");
	}

	@Test
	void shouldBeInvalid_GH24() {
		MainObject target = new MainObject();
		target.setId(1L);

		NestedObject nested = new NestedObject();
		nested.setId(10L);
		target.setNested(nested);

		ConstraintViolations result = mainObjectValidator.validate(target, CREATE);

		assertThat(result.isValid()).isFalse();
		assertThat(result).hasSize(3);
		assertThat(result.get(0).name()).isEqualTo("nested.text");
		assertThat(result.get(1).name()).isEqualTo("id");
		assertThat(result.get(2).name()).isEqualTo("nested.id");
	}

	@Test
	void shouldBeInvalid_GH29() {
		final MainObject target = new MainObject();
		target.setId(1L);

		final NestedObject nested = new NestedObject();
		nested.setId(1L);
		target.setNested(nested);

		final ConstraintViolations result = mainObjectValidator_GH29.validate(target, CREATE);

		assertThat(result.isValid()).isFalse();
		assertThat(result).hasSize(3);
	}

	@Test
	void shouldBeValid_GH24() {
		MainObject target = new MainObject();
		target.setId(1L);

		NestedObject nested = new NestedObject();
		nested.setId(1L);
		nested.setText("test");
		target.setNested(nested);

		ConstraintViolations result = mainObjectValidator.validate(target, UPDATE);

		assertThat(result.isValid()).isTrue();
	}

	@Test
	void testNested_GH20() {
		Validator<Nested> validator = ValidatorBuilder.of(Nested.class)
			.nest(n -> n.intRange, "intRange", intRangeValidator)
			.build();

		Nested nested = new Nested(new IntRange(1, 0));

		final ConstraintViolations result = validator.validate(nested);
		assertThat(result.isValid()).isFalse();
		assertThat(result.get(0).name()).isEqualTo("intRange.big");
	}

	@Test
	void testStandAlone_GH20() {
		IntRange intRange = new IntRange(1, 0);

		final ConstraintViolations result = intRangeValidator.validate(intRange);
		assertThat(result.isValid()).isFalse();
		assertThat(result.get(0).name()).isEqualTo("big");
	}

	@Test
	void shouldBeValid_GH28() {
		MainObject target = new MainObject();

		ConstraintViolations result = mainObjectIfPresentValidator.validate(target, CREATE);

		assertThat(result.isValid()).isTrue();
	}

	@Test
	void shouldBeValid_GH29() {
		final MainObject target = new MainObject();
		target.setId(1L);

		final NestedObject nested = new NestedObject();
		nested.setId(1L);
		nested.setText("test");
		target.setNested(nested);

		final ConstraintViolations result = mainObjectValidator_GH29.validate(target, UPDATE);

		assertThat(result.isValid()).isTrue();
	}

	@Test
	void shouldBeValid_GH90() {
		MainObject target = new MainObject();

		NestedObject nested = new NestedObject();
		nested.setText("test");
		nested.setIntRanges(singletonList(new IntRange(1, 2)));

		target.setNested(nested);

		ConstraintViolations result = mainObjectValidator_GH90.validate(target, CREATE);

		assertThat(result.isValid()).isTrue();
	}

	@Test
	void shouldBeInvalid_GH90() {
		MainObject target = new MainObject();

		NestedObject nested = new NestedObject();
		nested.setText("test");
		nested.setIntRanges(singletonList(new IntRange(null, 2)));

		target.setNested(nested);

		ConstraintViolations result = mainObjectValidator_GH90.validate(target, CREATE);

		assertThat(result.isValid()).isFalse();
		assertThat(result).extracting(ConstraintViolation::name).containsExactly("nested.intRanges[0].small");
	}

	@org.junit.jupiter.api.Nested
	class DeepNestingGH60Tests {

		@Test
		void shouldValidateObjectWithManyLayersNestedObjectWhenInnerNestedObjectIsRequired() {
			final Validator<A> validator = ValidatorBuilder.of(A.class)
				.nest(A::getB, "b",
						b -> b.nest(B::getC, "c", c -> c.nest(C::getD, "d", d -> d.nest(D::getE, "e",
								e -> e._integer(E::getValue, "value", value -> value.greaterThanOrEqual(100))))))
				.build();

			assertSoftly(softly -> {
				softly.assertThat(validator.validate(new A(null)).isValid())
					.as("1-layer nested object is null")
					.isFalse();
				softly.assertThat(validator.validate(new A(new B(null))).isValid())
					.as("2-layer nested object is null")
					.isFalse();
				softly.assertThat(validator.validate(new A(new B(new C(null)))).isValid())
					.as("3-layer nested object is null")
					.isFalse();
				softly.assertThat(validator.validate(new A(new B(new C(new D(null, null))))).isValid())
					.as("4-layer nested object is null (when it is required)")
					.isFalse();
				softly.assertThat(validator.validate(new A(new B(new C(new D(new E(100), null))))).isValid())
					.as("5-layer constraint is valid")
					.isTrue();
			});
		}

		@Test
		void shouldValidateObjectWithManyLayersNestedObjectWhenInnerNestedObjectIsOptional() {
			final Validator<A> validator = ValidatorBuilder.of(A.class)
				.nest(A::getB, "b",
						b -> b.nestIfPresent(B::getC, "c", c -> c.nest(C::getD, "d", d -> d.nestIfPresent(D::getE, "e",
								e -> e._integer(E::getValue, "value", value -> value.greaterThanOrEqual(100))))))
				.build();

			assertSoftly(softly -> {
				softly.assertThat(validator.validate(new A(null)).isValid())
					.as("1-layer nested object is null")
					.isFalse();
				softly.assertThat(validator.validate(new A(new B(null))).isValid())
					.as("2-layer nested object is null (when it is optional)")
					.isTrue();
				softly.assertThat(validator.validate(new A(new B(new C(null)))).isValid())
					.as("3-layer nested object is null")
					.isFalse();
				softly.assertThat(validator.validate(new A(new B(new C(new D(null, null))))).isValid())
					.as("4-layer nested object is null (when it is optional)")
					.isTrue();
				softly.assertThat(validator.validate(new A(new B(new C(new D(new E(100), null))))).isValid())
					.as("5-layer constraint is valid")
					.isTrue();
			});
		}

		@Test
		void shouldValidateObjectWithManyLayersNestedObjectWhenForEachMethodIsUsed() {
			final Validator<A> validator = ValidatorBuilder.of(A.class)
				.nest(A::getB, "b",
						b -> b.nest(B::getC, "c", c -> c.nest(C::getD, "d", d -> d.forEach(D::getList, "list",
								e -> e._integer(E::getValue, "value", value -> value.greaterThanOrEqual(100))))))
				.build();

			assertSoftly(softly -> {
				softly.assertThat(validator.validate(new A(null)).isValid())
					.as("1-layer nested object is null")
					.isFalse();
				softly.assertThat(validator.validate(new A(new B(null))).isValid())
					.as("2-layer nested object is null")
					.isFalse();
				softly.assertThat(validator.validate(new A(new B(new C(null)))).isValid())
					.as("3-layer nested object is null")
					.isFalse();
				softly.assertThat(validator.validate(new A(new B(new C(new D(null, null))))).isValid())
					.as("4-layer nested object is null (when it is required)")
					.isFalse();
				softly
					.assertThat(
							validator.validate(new A(new B(new C(new D(null, singletonList(new E(100))))))).isValid())
					.as("5-layer constraint is valid")
					.isTrue();
			});
		}

		@Test
		void shouldValidateObjectWithManyLayersNestedObjectWhenForEachIfPresentMethodIsUsed() {
			final Validator<A> validator = ValidatorBuilder.of(A.class)
				.nest(A::getB, "b", b -> b.nestIfPresent(B::getC, "c",
						c -> c.nest(C::getD, "d", d -> d.forEachIfPresent(D::getList, "list",
								e -> e._integer(E::getValue, "value", value -> value.greaterThanOrEqual(100))))))
				.build();

			assertSoftly(softly -> {
				softly.assertThat(validator.validate(new A(null)).isValid())
					.as("1-layer nested object is null")
					.isFalse();
				softly.assertThat(validator.validate(new A(new B(null))).isValid())
					.as("2-layer nested object is null (when it is optional)")
					.isTrue();
				softly.assertThat(validator.validate(new A(new B(new C(null)))).isValid())
					.as("3-layer nested object is null")
					.isFalse();
				softly.assertThat(validator.validate(new A(new B(new C(new D(null, null))))).isValid())
					.as("4-layer nested object is null (when it is optional)")
					.isTrue();
				softly
					.assertThat(
							validator.validate(new A(new B(new C(new D(null, singletonList(new E(100))))))).isValid())
					.as("5-layer constraint is valid")
					.isTrue();
			});
		}

	}

	public static class Nested {

		@Nullable IntRange intRange;

		Nested(@Nullable IntRange intRange) {
			this.intRange = intRange;
		}

	}

	public static class IntRange {

		public @Nullable Integer small;

		public @Nullable Integer big;

		IntRange(@Nullable Integer small, @Nullable Integer big) {
			this.small = small;
			this.big = big;
		}

	}

	public static class MainObject {

		@Nullable Long id;

		@Nullable NestedObject nested;

		public @Nullable Long getId() {
			return id;
		}

		public void setId(@Nullable Long id) {
			this.id = id;
		}

		public @Nullable NestedObject getNested() {
			return nested;
		}

		public void setNested(@Nullable NestedObject nested) {
			this.nested = nested;
		}

	}

	public static class NestedObject {

		@Nullable Long id;

		@Nullable String text;

		@Nullable List<IntRange> intRanges;

		public @Nullable Long getId() {
			return id;
		}

		public void setId(@Nullable Long id) {
			this.id = id;
		}

		public @Nullable String getText() {
			return text;
		}

		public void setText(@Nullable String text) {
			this.text = text;
		}

		public @Nullable List<IntRange> getIntRanges() {
			return intRanges;
		}

		public void setIntRanges(@Nullable List<IntRange> intRanges) {
			this.intRanges = intRanges;
		}

	}

	private static class A {

		private final @Nullable B b;

		public A(@Nullable B b) {
			this.b = b;
		}

		public @Nullable B getB() {
			return b;
		}

	}

	private static class B {

		private final @Nullable C c;

		public B(@Nullable C c) {
			this.c = c;
		}

		public @Nullable C getC() {
			return c;
		}

	}

	private static class C {

		private final @Nullable D d;

		public C(@Nullable D d) {
			this.d = d;
		}

		public @Nullable D getD() {
			return d;
		}

	}

	private static class D {

		private final @Nullable E e;

		private final @Nullable List<E> list;

		private D(@Nullable E e, @Nullable List<E> list) {
			this.e = e;
			this.list = list;
		}

		public @Nullable E getE() {
			return e;
		}

		public @Nullable List<E> getList() {
			return list;
		}

	}

	private static class E {

		private final int value;

		public E(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

	}

}
