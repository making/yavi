/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import am.ik.yavi.ConstraintViolationsException;
import am.ik.yavi.User;
import am.ik.yavi.constraint.charsequence.CodePoints;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsRanges;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsSet;
import am.ik.yavi.constraint.charsequence.variant.IdeographicVariationSequence;
import am.ik.yavi.fn.Either;

public class ValidatorTest {
	Validator<User> validator() {
		return Validator.<User> builder() //
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

	@Test
	public void valid() throws Exception {
		User user = new User("foo", "foo@example.com", 30);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	public void allInvalid() throws Exception {
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
	public void details() throws Exception {
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
	public void multipleViolationOnOneProperty() throws Exception {
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
	public void nullValues() throws Exception {
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
	public void combiningCharacterValid() throws Exception {
		User user = new User("モジ" /* モシ\u3099 */, null, null);
		Validator<User> validator = Validator.builder(User.class)
				.constraint(User::getName, "name",
						c -> c.fixedSize(2).asByteArray().fixedSize(9))
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	public void combiningCharacterByteSizeInValid() throws Exception {
		User user = new User("モジ" /* モシ\u3099 */, null, null);
		Validator<User> validator = Validator.builder(User.class)
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
	public void combiningCharacterSizeAndByteSizeInValid() throws Exception {
		User user = new User("モジ" /* モシ\u3099 */, null, null);
		Validator<User> validator = Validator.builder(User.class)
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
	public void ivsValid() throws Exception {
		User user = new User("葛󠄁飾区" /* 葛\uDB40\uDD01飾区 */, null, null);
		Validator<User> validator = Validator.builder(User.class)
				.constraint(User::getName, "name",
						c -> c.fixedSize(3).asByteArray().fixedSize(13))
				.build();
		ConstraintViolations violations = validator.validate(user);
		violations.forEach(x -> System.out.println(x.message()));
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	public void ivsInValid() throws Exception {
		User user = new User("葛󠄁飾区" /* 葛\uDB40\uDD01飾区 */, null, null);
		Validator<User> validator = Validator.builder(User.class)
				.constraint(User::getName, "name",
						c -> c.variant(ops -> ops.notIgnoreAll()).fixedSize(3)
								.asByteArray().fixedSize(13))
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("The size of \"name\" must be 3. The given size is 4");
	}

	@Test
	public void ivsByteSizeInValid() throws Exception {
		User user = new User("葛󠄁飾区" /* 葛\uDB40\uDD01飾区 */, null, null);
		Validator<User> validator = Validator.builder(User.class)
				.constraint(User::getName, "name",
						c -> c.lessThanOrEqual(3).asByteArray().lessThanOrEqual(12))
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo(
				"The byte size of \"name\" must be less than or equal to 12. The given size is 13");
	}

	@Test
	public void ivsSizeAndByteSizeInValid() throws Exception {
		User user = new User("葛󠄁飾区" /* 葛\uDB40\uDD01飾区 */, null, null);
		Validator<User> validator = Validator.builder(User.class).constraint(
				User::getName, "name",
				c -> c.variant(opts -> opts.ivs(IdeographicVariationSequence.NOT_IGNORE))
						.lessThanOrEqual(3).asByteArray().lessThanOrEqual(12))
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
	public void codePointsAllIncludedSet() throws Exception {
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
		Validator<User> validator = Validator.builder(User.class)
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
	public void codePointsAllIncludedRange() throws Exception {
		CodePointsRanges<String> whiteList = () -> Arrays.asList(
				CodePoints.Range.of(0x0041/* A */, 0x005A /* Z */),
				CodePoints.Range.of(0x0061/* a */, 0x007A /* z */));

		User user = new User("abc@b.c", null, null);
		Validator<User> validator = Validator.builder(User.class)
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
	public void codePointsNotIncludedSet() throws Exception {
		CodePointsSet<String> blackList = () -> new HashSet<>(
				Arrays.asList(0x0061 /* a */, 0x0062 /* b */));

		User user = new User("abcA@Bb.c", null, null);
		Validator<User> validator = Validator.builder(User.class)
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
	public void codePointsNotIncludedRange() throws Exception {
		CodePointsRanges<String> blackList = () -> Arrays.asList(
				CodePoints.Range.of(0x0041/* A */, 0x0042 /* B */),
				CodePoints.Range.of(0x0061/* a */, 0x0062 /* b */));

		User user = new User("abcA@Bb.c", null, null);
		Validator<User> validator = Validator.builder(User.class)
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
	public void codePointsAllIncludedSetSet() throws Exception {
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
		Validator<User> validator = Validator.builder(User.class)
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
	public void codePointsAllIncludedRangeRange() throws Exception {
		User user = new User("abc@b.c", null, null);
		Validator<User> validator = Validator.builder(User.class).constraint(
				User::getName, "name",
				c -> c.codePoints(CodePoints.Range.of(0x0041/* A */, 0x005A /* Z */),
						CodePoints.Range.of(0x0061/* a */, 0x007A /* z */)).asWhiteList())
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"[@, .]\" is/are not allowed for \"name\"");
		assertThat(violations.get(0).messageKey()).isEqualTo("codePoints.asWhiteList");
	}

	@Test
	public void codePointsAllIncludedRangeBeginToEnd() throws Exception {
		User user = new User("abc@b.c", null, null);
		Validator<User> validator = Validator.builder(User.class)
				.constraint(User::getName, "name",
						c -> c.codePoints(0x0041/* A */, 0x007A /* z */).asWhiteList())
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"[@, .]\" is/are not allowed for \"name\"");
		assertThat(violations.get(0).messageKey()).isEqualTo("codePoints.asWhiteList");
	}

	@Test
	public void emojiValid() throws Exception {
		User user = new User("I❤️☕️", null, null);
		Validator<User> validator = Validator.builder(User.class)
				.constraint(User::getName, "name", c -> c.emoji().lessThanOrEqual(3))
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	public void emojiInValid() throws Exception {
		User user = new User("I❤️☕️", null, null);
		Validator<User> validator = Validator.builder(User.class)
				.constraint(User::getName, "name", c -> c.emoji().greaterThan(3)).build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"name\" must be greater than 3. The given size is 3");
	}

	@Test
	public void throwIfInValidValid() throws Exception {
		User user = new User("foo", "foo@example.com", 30);
		validator().validate(user).throwIfInvalid(ConstraintViolationsException::new);
	}

	@Test
	public void throwIfInValidInValid() throws Exception {
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
	public void validateToEitherValid() throws Exception {
		User user = new User("foo", "foo@example.com", 30);
		Either<ConstraintViolations, User> either = validator().validateToEither(user);
		assertThat(either.isRight()).isTrue();
		assertThat(either.right().get()).isSameAs(user);
	}

	@Test
	public void validateToEitherInValid() throws Exception {
		User user = new User("foo", "foo@example.com", -1);
		Either<ConstraintViolations, User> either = validator().validateToEither(user);
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
	public void customMessageFormatter() throws Exception {
		Validator<User> validator = Validator.builder(User.class)
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
	public void group() {
		User user = new User("foobar", "foo@example.com", -1);
		Validator<User> validator = Validator.builder(User.class) //
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
	public void groupTwoCondition() {
		User user = new User("foobar", "foo@example.com", -1);
		Validator<User> validator = Validator.builder(User.class) //
				.constraintOnCondition(Group.UPDATE.toCondition(), //
						b -> b.constraint(User::getName, "name",
								c -> c.lessThanOrEqual(5)))
				.constraintOnCondition(Group.DELETE.toCondition(), //
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
	public void groupConditionByGroup() {
		User user = new User("foobar", "foo@example.com", -1);
		Validator<User> validator = Validator.builder(User.class) //
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
	public void violateGroupAndDefault() {
		User user = new User("foobar", "foo@example.com", -1);
		Validator<User> validator = Validator.builder(User.class) //
				.constraint(User::getEmail, "email", c -> c.email().lessThanOrEqual(10))
				.constraintOnCondition(Group.UPDATE.toCondition(), //
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
	public void condition() {
		Validator<User> validator = Validator.builder(User.class) //
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

	enum Group implements ConstraintGroup {
		UPDATE, DELETE
	}
}