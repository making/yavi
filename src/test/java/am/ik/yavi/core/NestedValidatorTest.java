/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CharSequenceConstraint;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static am.ik.yavi.core.Group.CREATE;
import static am.ik.yavi.core.Group.UPDATE;
import static org.assertj.core.api.Assertions.assertThat;

public class NestedValidatorTest extends AbstractNestedValidatorTest {
	@Override
	protected Validator<Address> validator() {
		return ValidatorBuilder.<Address> of()
				.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
				.nest(Address::country, "country", Country.validator())
				.nestIfPresent(Address::phoneNumber, "phoneNumber",
						PhoneNumber.validator())
				.build();
	}

	private ConstraintCondition<IntRange> whenAllOfNumbersNotNull = (r,
			g) -> r.small != null && r.big != null;

	private Validator<IntRange> thenCompareNumbers = ValidatorBuilder.of(IntRange.class)
			.constraintOnTarget(r -> r.big > r.small, "big", null,
					"[big] must be greater than [small]")
			.build();

	private Validator<IntRange> intRangeValidator = ValidatorBuilder.of(IntRange.class)
			.constraint((ValidatorBuilder.ToInteger<IntRange>) r -> r.small, "small",
					Constraint::notNull)
			.constraint((ValidatorBuilder.ToInteger<IntRange>) r -> r.big, "big",
					Constraint::notNull)
			.constraintOnCondition(whenAllOfNumbersNotNull, thenCompareNumbers).build();

	private Validator<NestedObject> nestedObjectValidator = ValidatorBuilder
			.<NestedObject> of()
			.constraintOnCondition(CREATE.toCondition(),
					b -> b.constraint(NestedObject::getId, "id", Constraint::isNull))
			.constraintOnCondition(UPDATE.toCondition(),
					b -> b.constraint(NestedObject::getId, "id", Constraint::notNull))
			.constraint(NestedObject::getText, "text", CharSequenceConstraint::notBlank)
			.build();

	private Validator<MainObject> mainObjectValidator = ValidatorBuilder.<MainObject> of()
			.constraintOnCondition(CREATE.toCondition(),
					b -> b.constraint(MainObject::getId, "id", Constraint::isNull))
			.constraintOnCondition(UPDATE.toCondition(),
					b -> b.constraint(MainObject::getId, "id", Constraint::notNull))
			.nest(MainObject::getNested, "nested", nestedObjectValidator).build();

	private Validator<MainObject> mainObjectIfPresentValidator = ValidatorBuilder
			.<MainObject> of()
			.constraintOnCondition(CREATE.toCondition(),
					b -> b.constraint(MainObject::getId, "id", Constraint::isNull))
			.constraintOnCondition(UPDATE.toCondition(),
					b -> b.constraint(MainObject::getId, "id", Constraint::notNull))
			.nestIfPresent(MainObject::getNested, "nested", nestedObjectValidator)
			.build();

	private Validator<NestedObject> nestedObjectValidator_GH29 = ValidatorBuilder
			.<NestedObject> of()
			.constraintOnCondition(CREATE.toCondition(),
					b -> b.constraint(NestedObject::getId, "id", Constraint::isNull))
			.constraintOnCondition(UPDATE.toCondition(),
					b -> b.constraint(NestedObject::getId, "id", Constraint::notNull))
			.constraint(NestedObject::getText, "text", CharSequenceConstraint::notBlank)
			.build();

	private Validator<MainObject> mainObjectValidator_GH29 = ValidatorBuilder
			.<MainObject> of()
			.constraintOnCondition(CREATE.toCondition(),
					b -> b.constraint(MainObject::getId, "id", Constraint::isNull))
			.constraintOnCondition(UPDATE.toCondition(),
					b -> b.constraint(MainObject::getId, "id", Constraint::notNull))
			.constraintOnCondition(CREATE.toCondition(),
					b -> b.nest(MainObject::getNested, "nested",
							nestedObjectValidator_GH29))
			.constraintOnCondition(UPDATE.toCondition(), b -> b
					.nest(MainObject::getNested, "nested", nestedObjectValidator_GH29))
			.build();

	private Validator<NestedObject> nestedObjectValidator_GH90 = ValidatorBuilder
			.<NestedObject> of()
			.constraintOnCondition(CREATE.toCondition(),
					b -> b.constraint(NestedObject::getId, "id", Constraint::isNull))
			.constraintOnCondition(UPDATE.toCondition(),
					b -> b.constraint(NestedObject::getId, "id", Constraint::notNull))
			.constraint(NestedObject::getText, "text", CharSequenceConstraint::notBlank)
			.forEach(NestedObject::getIntRanges, "intRanges", intRangeValidator).build();

	private Validator<MainObject> mainObjectValidator_GH90 = ValidatorBuilder
			.<MainObject> of()
			.constraintOnCondition(CREATE.toCondition(),
					b -> b.constraint(MainObject::getId, "id", Constraint::isNull))
			.constraintOnCondition(UPDATE.toCondition(),
					b -> b.constraint(MainObject::getId, "id", Constraint::notNull))
			.nest(MainObject::getNested, "nested", nestedObjectValidator_GH90).build();

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

		final ConstraintViolations result = mainObjectValidator_GH29.validate(target,
				CREATE);

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
				.nest(n -> n.intRange, "intRange", intRangeValidator).build();

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

		ConstraintViolations result = mainObjectIfPresentValidator.validate(target,
				CREATE);

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

		final ConstraintViolations result = mainObjectValidator_GH29.validate(target,
				UPDATE);

		assertThat(result.isValid()).isTrue();
	}

	@Test
	void shouldBeValid_GH90() {
		MainObject target = new MainObject();

		NestedObject nested = new NestedObject();
		nested.setText("test");
		nested.setIntRanges(Collections.singletonList(new IntRange(1, 2)));

		target.setNested(nested);

		ConstraintViolations result = mainObjectValidator_GH90.validate(target, CREATE);

		assertThat(result.isValid()).isTrue();
	}

	@Test
	void shouldBeInvalid_GH90() {
		MainObject target = new MainObject();

		NestedObject nested = new NestedObject();
		nested.setText("test");
		nested.setIntRanges(Collections.singletonList(new IntRange(null, 2)));

		target.setNested(nested);

		ConstraintViolations result = mainObjectValidator_GH90.validate(target, CREATE);

		assertThat(result.isValid()).isFalse();
		assertThat(result).extracting(ConstraintViolation::name)
				.containsExactly("nested.intRanges[0].small");
	}

	@org.junit.jupiter.api.Nested
	class DeepNestingGH60Tests {

		@Test
		void shouldBeValidForObjectValidation() {
			Validator<NestedObject> NestedObjectValidator = ValidatorBuilder.<NestedObject>of()
					.constraint(NestedObject::getText, "text", Constraint::notNull)
					.build();
			Validator<MainObject> mainObjectValidator = ValidatorBuilder.<MainObject>of()
					.nestIfPresent(MainObject::getNested, "nested", NestedObjectValidator)
					.build();
			Validator<Root> rootValidator = ValidatorBuilder.<Root>of()
					.nest(Root::getMainObject, "mainObject", mainObjectValidator)
					.build();

			MainObject mainObject = new MainObject();
			mainObject.setNested(null);
			Root root = new Root(mainObject);

			ConstraintViolations rootViolations = rootValidator.validate(root);

			assertThat(rootViolations.isValid()).isTrue();
		}

		@Test
		void shouldBeInvalidForObjectValidation() {
			Validator<NestedObject> NestedObjectValidator = ValidatorBuilder.<NestedObject>of()
					.constraint(NestedObject::getText, "text", Constraint::notNull)
					.build();
			Validator<MainObject> mainObjectValidator = ValidatorBuilder.<MainObject>of()
					.nest(MainObject::getNested, "nested", NestedObjectValidator)
					.build();
			Validator<Root> rootValidator = ValidatorBuilder.<Root>of()
					.nest(Root::getMainObject, "mainObject", mainObjectValidator)
					.build();

			MainObject mainObject = new MainObject();
			mainObject.setNested(null);
			Root root = new Root(mainObject);

			ConstraintViolations rootViolations = rootValidator.validate(root);

			assertThat(rootViolations.isValid()).isFalse();
		}

		@Test
		void shouldBeValidForEachVersion() {
			Validator<NestedObject> NestedObjectValidator = ValidatorBuilder.<NestedObject>of()
					.constraint(NestedObject::getText, "text", Constraint::notNull)
					.build();
			Validator<MainObject> mainObjectValidator = ValidatorBuilder.<MainObject>of()
					.forEachIfPresent(MainObject::getNestedList, "nestedList", NestedObjectValidator)
					.build();
			Validator<Root> rootValidator = ValidatorBuilder.<Root>of()
					.nest(Root::getMainObject, "mainObject", mainObjectValidator)
					.build();

			MainObject mainObject = new MainObject();
			mainObject.setNestedList(null);
			Root root = new Root(mainObject);

			ConstraintViolations rootViolations = rootValidator.validate(root);

			assertThat(rootViolations.isValid()).isTrue();
		}

		@Test
		void shouldBeInvalidForEachVersion() {
			Validator<NestedObject> NestedObjectValidator = ValidatorBuilder.<NestedObject>of()
					.constraint(NestedObject::getText, "text", Constraint::notNull)
					.build();
			Validator<MainObject> mainObjectValidator = ValidatorBuilder.<MainObject>of()
					.forEach(MainObject::getNestedList, "nestedList", NestedObjectValidator)
					.build();
			Validator<Root> rootValidator = ValidatorBuilder.<Root>of()
					.nest(Root::getMainObject, "mainObject", mainObjectValidator)
					.build();

			MainObject mainObject = new MainObject();
			mainObject.setNestedList(null);
			Root root = new Root(mainObject);

			ConstraintViolations rootViolations = rootValidator.validate(root);

			assertThat(rootViolations.isValid()).isFalse();
		}

	}

	private static class Root {
		private MainObject mainObject;

		public Root(MainObject mainObject) {
			this.mainObject = mainObject;
		}

		public MainObject getMainObject() {
			return mainObject;
		}
	}

	public static class Nested {
		IntRange intRange;

		Nested(IntRange intRange) {
			this.intRange = intRange;
		}
	}

	public static class IntRange {
		public Integer small;
		public Integer big;

		IntRange(Integer small, Integer big) {
			this.small = small;
			this.big = big;
		}
	}

	public static class MainObject {
		Long id;
		NestedObject nested;
		List<NestedObject> nestedList;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public NestedObject getNested() {
			return nested;
		}

		public void setNested(NestedObject nested) {
			this.nested = nested;
		}

		public List<NestedObject> getNestedList() {
			return nestedList;
		}

		public void setNestedList(List<NestedObject> nestedList) {
			this.nestedList = nestedList;
		}
	}

	public static class NestedObject {
		Long id;
		String text;
		List<IntRange> intRanges;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public List<IntRange> getIntRanges() {
			return intRanges;
		}

		public void setIntRanges(List<IntRange> intRanges) {
			this.intRanges = intRanges;
		}
	}
}
