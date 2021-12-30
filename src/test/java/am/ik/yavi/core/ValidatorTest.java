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

import am.ik.yavi.*;
import am.ik.yavi.ConstraintViolationsException;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.base.NumericConstraintBase;
import am.ik.yavi.constraint.charsequence.CodePoints;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsRanges;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsSet;
import am.ik.yavi.fn.Either;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static am.ik.yavi.constraint.charsequence.variant.IdeographicVariationSequence.IGNORE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class ValidatorTest {
	@Test
	void allInvalid() throws Exception {
		User user = new User("", "example.com", 300);
		user.setEnabled(false);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(4);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"name\" must be greater than or equal to 1. The given size is 0");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).message())
				.isEqualTo("\"email\" must be a valid email address");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.email");
		assertThat(violations.get(2).message())
				.isEqualTo("\"age\" must be less than or equal to 200");
		assertThat(violations.get(2).messageKey()).isEqualTo("numeric.lessThanOrEqual");
		assertThat(violations.get(3).message()).isEqualTo("\"enabled\" must be true");
		assertThat(violations.get(3).messageKey()).isEqualTo("boolean.isTrue");
	}

	@Test
	void codePointsAllIncludedRange() throws Exception {
		CodePointsRanges<String> whiteList = () -> Arrays.asList(
				CodePoints.Range.of(0x0041 /* A */, 0x005A /* Z */),
				CodePoints.Range.of(0x0061 /* a */, 0x007A /* z */));

		User user = new User("abc@b.c", null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.codePoints(whiteList).asWhiteList())
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"[@, .]\" is/are not allowed for \"name\"");
		assertThat(violations.get(0).messageKey()).isEqualTo("codePoints.asWhiteList");
	}

	@Test
	void codePointsAllIncludedRangeBeginToEnd() throws Exception {
		User user = new User("abc@b.c", null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.codePoints(0x0041 /* A */, 0x007A /* z */).asWhiteList())
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"[@, .]\" is/are not allowed for \"name\"");
		assertThat(violations.get(0).messageKey()).isEqualTo("codePoints.asWhiteList");
	}

	@Test
	void codePointsAllIncludedRangeRange() throws Exception {
		User user = new User("abc@b.c", null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name", c -> c
						.codePoints(CodePoints.Range.of(0x0041 /* A */, 0x005A /* Z */),
								CodePoints.Range.of(0x0061 /* a */, 0x007A /* z */))
						.asWhiteList())
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"[@, .]\" is/are not allowed for \"name\"");
		assertThat(violations.get(0).messageKey()).isEqualTo("codePoints.asWhiteList");
	}

	@Test
	void codePointsAllIncludedSet() throws Exception {
		CodePointsSet<String> whiteList = () -> new HashSet<>(
				Arrays.asList(0x0041 /* A */, 0x0042 /* B */, 0x0043 /* C */,
						0x0044 /* D */, 0x0045 /* E */, 0x0046 /* F */, 0x0047 /* G */,
						0x0048 /* H */, 0x0049 /* I */, 0x004A /* J */, 0x004B /* K */,
						0x004C /* L */, 0x004D /* M */, 0x004E /* N */, 0x004F /* O */,
						0x0050 /* P */, 0x0051 /* Q */, 0x0052 /* R */, 0x0053 /* S */,
						0x0054 /* T */, 0x0055 /* U */, 0x0056 /* V */, 0x0057 /* W */,
						0x0058 /* X */, 0x0059 /* Y */, 0x005A /* Z */, //
						0x0061 /* a */, 0x0062 /* b */, 0x0063 /* c */, 0x0064 /* d */,
						0x0065 /* e */, 0x0066 /* f */, 0x0067 /* g */, 0x0068 /* h */,
						0x0069 /* i */, 0x006A /* j */, 0x006B /* k */, 0x006C /* l */,
						0x006D /* m */, 0x006E /* n */, 0x006F /* o */, 0x0070 /* p */,
						0x0071 /* q */, 0x0072 /* r */, 0x0073 /* s */, 0x0074 /* t */,
						0x0075 /* u */, 0x0076 /* v */, 0x0077 /* w */, 0x0078 /* x */,
						0x0079 /* y */, 0x007A /* z */));

		User user = new User("abc@b.c", null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.codePoints(whiteList).asWhiteList())
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"[@, .]\" is/are not allowed for \"name\"");
		assertThat(violations.get(0).messageKey()).isEqualTo("codePoints.asWhiteList");
	}

	@Test
	void codePointsAllIncludedSetSet() throws Exception {
		Set<Integer> whiteList = new HashSet<>(
				Arrays.asList(0x0041 /* A */, 0x0042 /* B */, 0x0043 /* C */,
						0x0044 /* D */, 0x0045 /* E */, 0x0046 /* F */, 0x0047 /* G */,
						0x0048 /* H */, 0x0049 /* I */, 0x004A /* J */, 0x004B /* K */,
						0x004C /* L */, 0x004D /* M */, 0x004E /* N */, 0x004F /* O */,
						0x0050 /* P */, 0x0051 /* Q */, 0x0052 /* R */, 0x0053 /* S */,
						0x0054 /* T */, 0x0055 /* U */, 0x0056 /* V */, 0x0057 /* W */,
						0x0058 /* X */, 0x0059 /* Y */, 0x005A /* Z */, //
						0x0061 /* a */, 0x0062 /* b */, 0x0063 /* c */, 0x0064 /* d */,
						0x0065 /* e */, 0x0066 /* f */, 0x0067 /* g */, 0x0068 /* h */,
						0x0069 /* i */, 0x006A /* j */, 0x006B /* k */, 0x006C /* l */,
						0x006D /* m */, 0x006E /* n */, 0x006F /* o */, 0x0070 /* p */,
						0x0071 /* q */, 0x0072 /* r */, 0x0073 /* s */, 0x0074 /* t */,
						0x0075 /* u */, 0x0076 /* v */, 0x0077 /* w */, 0x0078 /* x */,
						0x0079 /* y */, 0x007A /* z */));

		User user = new User("abc@b.c", null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.codePoints(whiteList).asWhiteList())
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"[@, .]\" is/are not allowed for \"name\"");
		assertThat(violations.get(0).messageKey()).isEqualTo("codePoints.asWhiteList");
	}

	@Test
	void codePointsNotIncludedRange() throws Exception {
		CodePointsRanges<String> blackList = () -> Arrays.asList(
				CodePoints.Range.of(0x0041 /* A */, 0x0042 /* B */),
				CodePoints.Range.of(0x0061 /* a */, 0x0062 /* b */));

		User user = new User("abcA@Bb.c", null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.codePoints(blackList).asBlackList())
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"[a, b, A, B]\" is/are not allowed for \"name\"");
		assertThat(violations.get(0).messageKey()).isEqualTo("codePoints.asBlackList");
	}

	@Test
	void codePointsNotIncludedSet() throws Exception {
		CodePointsSet<String> blackList = () -> new HashSet<>(
				Arrays.asList(0x0061 /* a */, 0x0062 /* b */));

		User user = new User("abcA@Bb.c", null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.codePoints(blackList).asBlackList())
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"[a, b]\" is/are not allowed for \"name\"");
		assertThat(violations.get(0).messageKey()).isEqualTo("codePoints.asBlackList");
	}

	@Test
	void combiningCharacterByteSizeInValid() throws Exception {
		User user = new User("モジ" /* モシ\u3099 */, null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.lessThanOrEqual(2).asByteArray().lessThanOrEqual(6))
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo(
				"The byte size of \"name\" must be less than or equal to 6. The given size is 9");
	}

	@Test
	void combiningCharacterSizeAndByteSizeInValid() throws Exception {
		User user = new User("モジ" /* モシ\u3099 */, null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.lessThanOrEqual(1).asByteArray().lessThanOrEqual(3))
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(2);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"name\" must be less than or equal to 1. The given size is 2");
		assertThat(violations.get(1).message()).isEqualTo(
				"The byte size of \"name\" must be less than or equal to 3. The given size is 9");
	}

	@Test
	void combiningCharacterValid() throws Exception {
		User user = new User("モジ" /* モシ\u3099 */, null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.fixedSize(2).asByteArray().fixedSize(9))
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void condition() {
		Validator<User> validator = ValidatorBuilder.of(User.class) //
				.constraintOnCondition((u, cg) -> !u.getName().isEmpty(), //
						b -> b.constraint(User::getEmail, "email",
								c -> c.email().notEmpty()))
				.build();
		{
			User user = new User("", "", -1);
			ConstraintViolations violations = validator.validate(user);
			assertThat(violations.isValid()).isTrue();
		}
		{
			User user = new User("foobar", "", -1);
			ConstraintViolations violations = validator.validate(user, Group.UPDATE);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message())
					.isEqualTo("\"email\" must not be empty");
			assertThat(violations.get(0).messageKey()).isEqualTo("container.notEmpty");
		}
	}

	@Test
	void constraintOnTarget() {
		Validator<Range> validator = ValidatorBuilder.of(Range.class) //
				.constraintOnTarget(Range::isToGreaterThanFrom, "to",
						"to.isGreaterThanFrom", "\"to\" must be greater than \"from\".") //
				.build();
		{
			Range range = new Range(1, 10);
			ConstraintViolations violations = validator.validate(range);
			assertThat(violations.isValid()).isTrue();
		}
		{
			Range range = new Range(10, 1);
			ConstraintViolations violations = validator.validate(range);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message())
					.isEqualTo("\"to\" must be greater than \"from\".");
			assertThat(violations.get(0).messageKey()).isEqualTo("to.isGreaterThanFrom");
		}
	}

	@Test
	void customMessageFormatter() throws Exception {
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.messageFormatter((messageKey, defaultMessageFormat, args,
						locale) -> args[0].toString().toUpperCase() + "."
								+ messageKey.toUpperCase())
				.constraint(User::getAge, "age", c -> c.notNull() //
						.greaterThanOrEqual(0) //
						.lessThanOrEqual(20))
				.build();
		User user = new User("foo", "foo@example.com", 30);
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo("AGE.NUMERIC.LESSTHANOREQUAL");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.lessThanOrEqual");
	}

	@Test
	void details() throws Exception {
		User user = new User("", "example.com", 300);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		List<ViolationDetail> details = violations.details();
		assertThat(details.size()).isEqualTo(3);
		assertThat(details.get(0).getDefaultMessage()).isEqualTo(
				"The size of \"name\" must be greater than or equal to 1. The given size is 0");
		assertThat(details.get(0).getKey()).isEqualTo("container.greaterThanOrEqual");
		assertThat(details.get(0).getArgs()).containsExactly("name", 1, 0);
		assertThat(details.get(1).getDefaultMessage())
				.isEqualTo("\"email\" must be a valid email address");
		assertThat(details.get(1).getKey()).isEqualTo("charSequence.email");
		assertThat(details.get(1).getArgs()).containsExactly("email", "example.com");
		assertThat(details.get(2).getDefaultMessage())
				.isEqualTo("\"age\" must be less than or equal to 200");
		assertThat(details.get(2).getKey()).isEqualTo("numeric.lessThanOrEqual");
		assertThat(details.get(2).getArgs()).containsExactly("age", 200, 300);
	}

	@Test
	void emojiInValid() throws Exception {
		User user = new User("I❤️☕️", null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name", c -> c.emoji().greaterThan(3)).build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"name\" must be greater than 3. The given size is 3");
	}

	@Test
	void emojiValid() throws Exception {
		User user = new User("I❤️☕️", null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name", c -> c.emoji().lessThanOrEqual(3))
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void group() {
		User user = new User("foobar", "foo@example.com", -1);
		Validator<User> validator = ValidatorBuilder.of(User.class) //
				.constraintOnCondition(Group.UPDATE.toCondition(), //
						b -> b.constraint(User::getName, "name",
								c -> c.lessThanOrEqual(5)))
				.build();
		{
			ConstraintViolations violations = validator.validate(user);
			assertThat(violations.isValid()).isTrue();
		}
		{
			ConstraintViolations violations = validator.validate(user, Group.UPDATE);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo(
					"The size of \"name\" must be less than or equal to 5. The given size is 6");
			assertThat(violations.get(0).messageKey())
					.isEqualTo("container.lessThanOrEqual");
		}
	}

	@Test
	void groupConditionByGroup() {
		User user = new User("foobar", "foo@example.com", -1);
		Validator<User> validator = ValidatorBuilder.of(User.class) //
				.constraintOnCondition(
						(u, cg) -> cg == Group.UPDATE || cg == Group.DELETE, //
						b -> b.constraint(User::getName, "name",
								c -> c.lessThanOrEqual(5)))
				.build();
		{
			ConstraintViolations violations = validator.validate(user);
			assertThat(violations.isValid()).isTrue();
		}
		{
			ConstraintViolations violations = validator.validate(user, Group.UPDATE);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo(
					"The size of \"name\" must be less than or equal to 5. The given size is 6");
			assertThat(violations.get(0).messageKey())
					.isEqualTo("container.lessThanOrEqual");
		}
		{
			ConstraintViolations violations = validator.validate(user, Group.DELETE);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo(
					"The size of \"name\" must be less than or equal to 5. The given size is 6");
			assertThat(violations.get(0).messageKey())
					.isEqualTo("container.lessThanOrEqual");
		}
	}

	@Test
	void groupTwoCondition() {
		User user = new User("foobar", "foo@example.com", -1);
		Validator<User> validator = ValidatorBuilder.of(User.class) //
				.constraintOnGroup(Group.UPDATE, //
						b -> b.constraint(User::getName, "name",
								c -> c.lessThanOrEqual(5)))
				.constraintOnGroup(Group.DELETE, //
						b -> b.constraint(User::getName, "name",
								c -> c.greaterThanOrEqual(5)))
				.build();
		{
			ConstraintViolations violations = validator.validate(user);
			assertThat(violations.isValid()).isTrue();
		}
		{
			ConstraintViolations violations = validator.validate(user, Group.UPDATE);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo(
					"The size of \"name\" must be less than or equal to 5. The given size is 6");
			assertThat(violations.get(0).messageKey())
					.isEqualTo("container.lessThanOrEqual");
		}
		{
			ConstraintViolations violations = validator.validate(user, Group.DELETE);
			assertThat(violations.isValid()).isTrue();
		}
	}

	@Test
	void ivsByteSizeInValid() throws Exception {
		User user = new User("葛󠄁飾区" /* 葛\uDB40\uDD01飾区 */, null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.variant(opts -> opts.ivs(IGNORE)).lessThanOrEqual(3)
								.asByteArray().lessThanOrEqual(12))
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo(
				"The byte size of \"name\" must be less than or equal to 12. The given size is 13");
	}

	@Test
	void ivsInValid() throws Exception {
		User user = new User("葛󠄁飾区" /* 葛\uDB40\uDD01飾区 */, null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.fixedSize(3).asByteArray().fixedSize(13))
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("The size of \"name\" must be 3. The given size is 4");
	}

	@Test
	void ivsSizeAndByteSizeInValid() throws Exception {
		User user = new User("葛󠄁飾区" /* 葛\uDB40\uDD01飾区 */, null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.lessThanOrEqual(3).asByteArray().lessThanOrEqual(12))
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(2);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"name\" must be less than or equal to 3. The given size is 4");
		assertThat(violations.get(1).message()).isEqualTo(
				"The byte size of \"name\" must be less than or equal to 12. The given size is 13");
	}

	@Test
	void ivsValid() throws Exception {
		User user = new User("葛󠄁飾区" /* 葛\uDB40\uDD01飾区 */, null, null);
		Validator<User> validator = ValidatorBuilder.of(User.class)
				.constraint(User::getName, "name",
						c -> c.variant(opts -> opts.ivs(IGNORE)).fixedSize(3)
								.asByteArray().fixedSize(13))
				.build();
		ConstraintViolations violations = validator.validate(user);
		violations.forEach(x -> System.out.println(x.message()));
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void multipleViolationOnOneProperty() throws Exception {
		User user = new User("foo", "aa", 200);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(2);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"email\" must be greater than or equal to 5. The given size is 2");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).message())
				.isEqualTo("\"email\" must be a valid email address");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.email");
	}

	@Test
	void nullValues() throws Exception {
		User user = new User(null, null, null);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message()).isEqualTo("\"name\" must not be null");
		assertThat(violations.get(0).messageKey()).isEqualTo("object.notNull");
		assertThat(violations.get(1).message()).isEqualTo("\"email\" must not be null");
		assertThat(violations.get(1).messageKey()).isEqualTo("object.notNull");
		assertThat(violations.get(2).message()).isEqualTo("\"age\" must not be null");
		assertThat(violations.get(2).messageKey()).isEqualTo("object.notNull");
	}

	@Test
	void overrideMessage() {
		Validator<User> validator = ValidatorBuilder.<User> of() //
				.constraint(User::getName, "name",
						c -> c.notNull().message("name is required!") //
								.greaterThanOrEqual(1).message("name is too small!") //
								.lessThanOrEqual(20).message("name is too large!")) //
				.build();

		{
			User user = new User(null, "a@b.c", 10);
			ConstraintViolations violations = validator.validate(user);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("name is required!");
			assertThat(violations.get(0).messageKey()).isEqualTo("object.notNull");
		}
		{
			User user = new User("", "a@b.c", 10);
			ConstraintViolations violations = validator.validate(user);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("name is too small!");
			assertThat(violations.get(0).messageKey())
					.isEqualTo("container.greaterThanOrEqual");
		}
		{
			User user = new User("012345678901234567890", "a@b.c", 10);
			ConstraintViolations violations = validator.validate(user);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("name is too large!");
			assertThat(violations.get(0).messageKey())
					.isEqualTo("container.lessThanOrEqual");
		}
	}

	@Test
	void overrideViolationMessage() {
		Validator<User> validator = ValidatorBuilder.<User> of() //
				.constraint(User::getName, "name",
						c -> c.notNull()
								.message(ViolationMessage.of("a", "name is required!")) //
								.greaterThanOrEqual(1)
								.message(ViolationMessage.of("b", "name is too small!")) //
								.lessThanOrEqual(20)
								.message(ViolationMessage.of("c", "name is too large!"))) //
				.build();

		{
			User user = new User(null, "a@b.c", 10);
			ConstraintViolations violations = validator.validate(user);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("name is required!");
			assertThat(violations.get(0).messageKey()).isEqualTo("a");
		}
		{
			User user = new User("", "a@b.c", 10);
			ConstraintViolations violations = validator.validate(user);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("name is too small!");
			assertThat(violations.get(0).messageKey()).isEqualTo("b");
		}
		{
			User user = new User("012345678901234567890", "a@b.c", 10);
			ConstraintViolations violations = validator.validate(user);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("name is too large!");
			assertThat(violations.get(0).messageKey()).isEqualTo("c");
		}
	}

	@Test
	void throwIfInValidInValid() throws Exception {
		User user = new User("foo", "foo@example.com", -1);
		try {
			validator().validate(user).throwIfInvalid(ConstraintViolationsException::new);
			fail("fail");
		}
		catch (ConstraintViolationsException e) {
			ConstraintViolations violations = e.getViolations();
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message())
					.isEqualTo("\"age\" must be greater than or equal to 0");
			assertThat(violations.get(0).messageKey())
					.isEqualTo("numeric.greaterThanOrEqual");
		}
	}

	@Test
	void throwIfInValidValid() throws Exception {
		User user = new User("foo", "foo@example.com", 30);
		validator().validate(user).throwIfInvalid(ConstraintViolationsException::new);
	}

	@Test
	void valid() throws Exception {
		User user = new User("foo", "foo@example.com", 30);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void validateToEitherInValid() throws Exception {
		User user = new User("foo", "foo@example.com", -1);
		Either<ConstraintViolations, User> either = validator().either().validate(user);
		assertThat(either.isLeft()).isTrue();
		ConstraintViolations violations = either.left().get();
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"age\" must be greater than or equal to 0");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("numeric.greaterThanOrEqual");
	}

	@Test
	void validateToEitherValid() throws Exception {
		User user = new User("foo", "foo@example.com", 30);
		Either<ConstraintViolations, User> either = validator().either().validate(user);
		assertThat(either.isRight()).isTrue();
		assertThat(either.right().get()).isSameAs(user);
	}

	@Test
	void violateGroupAndDefault() {
		User user = new User("foobar", "foo@example.com", -1);
		Validator<User> validator = ValidatorBuilder.of(User.class) //
				.constraint(User::getEmail, "email", c -> c.email().lessThanOrEqual(10))
				.constraintOnGroup(Group.UPDATE, //
						b -> b.constraint(User::getName, "name",
								c -> c.lessThanOrEqual(5)))
				.build();
		{
			ConstraintViolations violations = validator.validate(user);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo(
					"The size of \"email\" must be less than or equal to 10. The given size is 15");
			assertThat(violations.get(0).messageKey())
					.isEqualTo("container.lessThanOrEqual");
		}
		{
			ConstraintViolations violations = validator.validate(user, Group.UPDATE);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(2);
			assertThat(violations.get(0).message()).isEqualTo(
					"The size of \"email\" must be less than or equal to 10. The given size is 15");
			assertThat(violations.get(0).messageKey())
					.isEqualTo("container.lessThanOrEqual");
			assertThat(violations.get(1).message()).isEqualTo(
					"The size of \"name\" must be less than or equal to 5. The given size is 6");
			assertThat(violations.get(1).messageKey())
					.isEqualTo("container.lessThanOrEqual");
		}
	}

	@Test
	void agePositiveValidatorUserValid() {
		User user = new User("Diego", "foo@bar.com", 10);

		Validator<User> validator = ValidatorBuilder.<User> of()
				.constraint(User::getAge, "age", NumericConstraintBase::positive).build();

		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void agePositiveValidatorUserInValid() {
		User user = new User("Diego", "foo@bar.com", -1);

		Validator<User> validator = ValidatorBuilder.<User> of()
				.constraint(User::getAge, "age", NumericConstraintBase::positive).build();

		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo("\"age\" must be positive");
	}

	@Test
	void ageNegativeValidatorUserValid() {
		User user = new User("Diego", "foo@bar.com", -1);

		Validator<User> validator = ValidatorBuilder.<User> of()
				.constraint(User::getAge, "age", NumericConstraintBase::negative).build();

		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void ageNegativeValidatorUserInValid() {
		User user = new User("Diego", "foo@bar.com", 10);

		Validator<User> validator = ValidatorBuilder.<User> of()
				.constraint(User::getAge, "age", NumericConstraintBase::negative).build();

		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo("\"age\" must be negative");
	}

	@Test
	void calendarDateIsBeforeNowValid() {
		LocalDateTime now = LocalDateTime.now();
		CalendarEntryLocalDateTime birthdayPartyEntry = new CalendarEntryLocalDateTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalDateTime> validator = ValidatorBuilder
				.<CalendarEntryLocalDateTime> of()
				.constraint(CalendarEntryLocalDateTime::getDateTime, "datetime",
						c -> c.before(now.plusHours(10)))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void nameStartWithValid() {
		User user = new User("Diego Krupitza", "foo@bar.com", 22);

		Validator<User> validator = ValidatorBuilder.<User> of()
				.constraint(User::getName, "name", c -> c.startsWith("Diego")).build();

		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void calendarDateIsBeforeNowInValid() {
		LocalDateTime now = LocalDateTime.now();
		CalendarEntryLocalDateTime birthdayPartyEntry = new CalendarEntryLocalDateTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalDateTime> validator = ValidatorBuilder
				.<CalendarEntryLocalDateTime> of()
				.constraint(CalendarEntryLocalDateTime::getDateTime, "datetime",
						c -> c.before(now.minusHours(10)))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.startsWith("\"datetime\" has to be before");
	}

	@Test
	void calendarDateIsAfterNowInValid() {
		LocalDateTime now = LocalDateTime.now();
		CalendarEntryLocalDateTime birthdayPartyEntry = new CalendarEntryLocalDateTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalDateTime> validator = ValidatorBuilder
				.<CalendarEntryLocalDateTime> of()
				.constraint(CalendarEntryLocalDateTime::getDateTime, "datetime",
						c -> c.after(now.plusHours(10)))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.startsWith("\"datetime\" has to be after");
	}

	@Test
	void calendarDateIsAfterNowValid() {
		LocalDateTime now = LocalDateTime.now();
		CalendarEntryLocalDateTime birthdayPartyEntry = new CalendarEntryLocalDateTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalDateTime> validator = ValidatorBuilder
				.<CalendarEntryLocalDateTime> of()
				.constraint(CalendarEntryLocalDateTime::getDateTime, "datetime",
						c -> c.after(now.minusHours(10)))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void calendarDateIsBetweenNowValid() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime before = now.minusHours(10);
		LocalDateTime after = now.plusHours(10);
		CalendarEntryLocalDateTime birthdayPartyEntry = new CalendarEntryLocalDateTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalDateTime> validator = ValidatorBuilder
				.<CalendarEntryLocalDateTime> of()
				.constraint(CalendarEntryLocalDateTime::getDateTime, "datetime",
						c -> c.between(before, after))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void nameStartWithInValid() {
		User user = new User("NotDiego", "foo@bar.com", 22);

		Validator<User> validator = ValidatorBuilder.<User> of()
				.constraint(User::getName, "name", c -> c.startsWith("Diego")).build();

		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"name\" must start with \"Diego\"");
	}

	@Test
	void nameEndsWithValid() {
		User user = new User("Diego Krupitza", "foo@bar.com", 22);

		Validator<User> validator = ValidatorBuilder.<User> of()
				.constraint(User::getName, "name", c -> c.endsWith("Krupitza")).build();

		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void calendarDateIsBetweenNowEqualInValid() {
		LocalDateTime now = LocalDateTime.now();

		CalendarEntryLocalDateTime birthdayPartyEntry = new CalendarEntryLocalDateTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalDateTime> validator = ValidatorBuilder
				.<CalendarEntryLocalDateTime> of()
				.constraint(CalendarEntryLocalDateTime::getDateTime, "datetime",
						c -> c.between(now, now))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.startsWith("\"datetime\" has to be between");
	}

	@Test
	void calendarDateIsBetweenNowInValid() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime before = now.plusDays(10);
		LocalDateTime after = now.plusDays(20);

		CalendarEntryLocalDateTime birthdayPartyEntry = new CalendarEntryLocalDateTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalDateTime> validator = ValidatorBuilder
				.<CalendarEntryLocalDateTime> of()
				.constraint(CalendarEntryLocalDateTime::getDateTime, "datetime",
						c -> c.between(before, after))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.startsWith("\"datetime\" has to be between");
	}

	@Test
	void nameEndsWithInValid() {
		User user = new User("Diego Not", "foo@bar.com", 22);

		Validator<User> validator = ValidatorBuilder.<User> of()
				.constraint(User::getName, "name", c -> c.endsWith("Diego")).build();

		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"name\" must end with \"Diego\"");
	}

	@Test
	void timeIsBeforeNowValid() {
		LocalTime now = LocalTime.of(12, 30);
		CalendarEntryLocalTime birthdayPartyEntry = new CalendarEntryLocalTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalTime> validator = ValidatorBuilder
				.<CalendarEntryLocalTime> of().constraint(CalendarEntryLocalTime::getTime,
						"time", c -> c.before(now.plusHours(10)))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void timeIsBeforeNowInValid() {
		LocalTime now = LocalTime.of(12, 30);
		CalendarEntryLocalTime birthdayPartyEntry = new CalendarEntryLocalTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalTime> validator = ValidatorBuilder
				.<CalendarEntryLocalTime> of().constraint(CalendarEntryLocalTime::getTime,
						"time", c -> c.before(now.minusHours(10)))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).startsWith("\"time\" has to be before");
	}

	@Test
	void timeIsAfterNowInValid() {
		LocalTime now = LocalTime.of(12, 30);
		CalendarEntryLocalTime birthdayPartyEntry = new CalendarEntryLocalTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalTime> validator = ValidatorBuilder
				.<CalendarEntryLocalTime> of().constraint(CalendarEntryLocalTime::getTime,
						"time", c -> c.after(now.plusHours(10)))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).startsWith("\"time\" has to be after");
	}

	@Test
	void timeIsAfterNowValid() {
		LocalTime now = LocalTime.of(12, 30);
		CalendarEntryLocalTime birthdayPartyEntry = new CalendarEntryLocalTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalTime> validator = ValidatorBuilder
				.<CalendarEntryLocalTime> of().constraint(CalendarEntryLocalTime::getTime,
						"time", c -> c.after(now.minusHours(10)))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void timeIsBetweenNowValid() {
		LocalTime now = LocalTime.of(12, 30);
		LocalTime before = now.minusHours(10);
		LocalTime after = now.plusHours(10);

		CalendarEntryLocalTime birthdayPartyEntry = new CalendarEntryLocalTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalTime> validator = ValidatorBuilder
				.<CalendarEntryLocalTime> of().constraint(CalendarEntryLocalTime::getTime,
						"time", c -> c.between(before, after))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void timeIsBetweenNowEqualInValid() {
		LocalTime now = LocalTime.of(12, 30);

		CalendarEntryLocalTime birthdayPartyEntry = new CalendarEntryLocalTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalTime> validator = ValidatorBuilder
				.<CalendarEntryLocalTime> of().constraint(CalendarEntryLocalTime::getTime,
						"time", c -> c.between(now, now))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).startsWith("\"time\" has to be between");
	}

	@Test
	void timeIsBetweenNowInValid() {
		LocalTime now = LocalTime.of(12, 30);
		LocalTime before = now.plusHours(10);
		LocalTime after = now.plusHours(10).plusMinutes(1);

		CalendarEntryLocalTime birthdayPartyEntry = new CalendarEntryLocalTime(
				"BirthdayParty", now);

		Validator<CalendarEntryLocalTime> validator = ValidatorBuilder
				.<CalendarEntryLocalTime> of().constraint(CalendarEntryLocalTime::getTime,
						"time", c -> c.between(before, after))
				.build();

		ConstraintViolations violations = validator.validate(birthdayPartyEntry);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).startsWith("\"time\" has to be between");
	}

	Validator<User> validator() {
		return ValidatorBuilder.<User> of() //
				.constraint(User::getName, "name", c -> c.notNull() //
						.greaterThanOrEqual(1) //
						.lessThanOrEqual(20)) //
				.constraint(User::getEmail, "email", c -> c.notNull() //
						.greaterThanOrEqual(5) //
						.lessThanOrEqual(50) //
						.email()) //
				.constraint(User::getAge, "age", c -> c.notNull() //
						.greaterThanOrEqual(0) //
						.lessThanOrEqual(200)) //
				.constraint(User::isEnabled, "enabled", c -> c.isTrue()) //
				.build();
	}
}
