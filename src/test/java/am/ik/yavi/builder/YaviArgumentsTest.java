/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import am.ik.yavi.arguments.Arguments;
import am.ik.yavi.arguments.Arguments7Validator;
import am.ik.yavi.builder.ArgumentDefinition.Kind;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.PathSegment;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import am.ik.yavi.validator.Yavi;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class YaviArgumentsTest {

	record Address(@Nullable String street, @Nullable String city) {
	}

	record User(@Nullable String contact, @Nullable Optional<String> nickname, @Nullable Integer age,
			@Nullable Address address, @Nullable Address billing, @Nullable List<String> roles,
			@Nullable Map<String, Integer> scores) {

		boolean isAdultIfAdmin() {
			return this.age == null || this.roles == null || this.age >= 18 || !this.roles.contains("admin");
		}
	}

	static final ValueValidator<Map<String, String>, Address> addressValidator = Yavi.arguments()
		._string("street", c -> c.notBlank())
		._string("city", c -> c.notBlank())
		.apply(Address::new)
		.compose(map -> {
			final Map<String, String> nonNull = Objects.requireNonNull(map);
			return Arguments.of(nonNull.get("street"), nonNull.get("city"));
		});

	static final Arguments7ValidatorBuilder<String, String, Integer, Map<String, String>, Map<String, String>, List<String>, Map<String, Integer>, String, Optional<String>, Integer, Address, Address, List<String>, Map<String, Integer>> builder = Yavi
		.arguments()
		._object("contact",
				ValueValidator.<String, String>anyOf(StringValidatorBuilder.of("", c -> c.email()).build(),
						StringValidatorBuilder.of("", c -> c.pattern("[0-9]{10,11}")).build()))
		._string("nickname", c -> c.notBlank())
		.optional()
		._integer("age", c -> c.notNull().greaterThanOrEqual(0))
		.defaultValue(20)
		._object("address", addressValidator)
		._object("billing", addressValidator)
		.nullable()
		._list("roles", StringValidatorBuilder.of("", c -> c.notBlank()).build())
		._map("scores", IntegerValidatorBuilder.of("", c -> c.greaterThanOrEqual(0)).build());

	static final Arguments7Validator<String, String, Integer, Map<String, String>, Map<String, String>, List<String>, Map<String, Integer>, User> validator = builder
		.apply(User::new)
		.constraintOnTarget(User::isAdultIfAdmin, "roles",
				ViolationMessage.of("user.adultAdmin", "\"{0}\" must not contain \"admin\" for a minor"));

	@Test
	void valid() {
		final Map<String, String> address = Map.of("street", "1-2-3", "city", "Tokyo");
		final Map<String, Integer> scores = new LinkedHashMap<>();
		scores.put("math", 80);
		assertThat(validator.validated("yavi@example.com", "yavi", 30, address, address, List.of("admin"), scores))
			.isEqualTo(new User("yavi@example.com", Optional.of("yavi"), 30, new Address("1-2-3", "Tokyo"),
					new Address("1-2-3", "Tokyo"), List.of("admin"), Map.of("math", 80)));
		// a missing value of an optional, defaulted or nullable argument is accepted
		assertThat(validator.validated("0312345678", null, null, address, null, List.of(), Map.of())).isEqualTo(
				new User("0312345678", Optional.empty(), 20, new Address("1-2-3", "Tokyo"), null, List.of(), Map.of()));
	}

	@Test
	void violationsAreLocatedUnderTheArgument() {
		final ConstraintViolations violations = validator
			.validate("yavi", " ", -1, Map.of("city", "Tokyo"), Map.of("street", "1-2-3"), List.of("admin", ""),
					Map.of("math", -1))
			.errors();
		assertThat(violations).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("contact", "object.anyOf"), tuple("contact", "charSequence.email"),
					tuple("contact", "charSequence.pattern"), tuple("nickname", "charSequence.notBlank"),
					tuple("age", "numeric.greaterThanOrEqual"), tuple("address.street", "charSequence.notBlank"),
					tuple("billing.city", "charSequence.notBlank"), tuple("roles[1]", "charSequence.notBlank"),
					tuple("scores.math", "numeric.greaterThanOrEqual"));
		assertThat(violations).extracting(ConstraintViolation::path)
			.contains(List.of(new PathSegment.Property("contact")),
					List.of(new PathSegment.Property("address"), new PathSegment.Property("street")),
					List.of(new PathSegment.Property("roles"), new PathSegment.Index(1)),
					List.of(new PathSegment.Property("scores"), new PathSegment.Property("math")));
		assertThat(violations.get(7).message()).isEqualTo("\"roles[1]\" must not be blank");
	}

	@Test
	void missingObjectListAndMapAreRejected() {
		final ConstraintViolations violations = validator.validate(null, null, null, null, null, null, null).errors();
		assertThat(violations).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("contact", "object.notNull"), tuple("address", "object.notNull"),
					tuple("roles", "object.notNull"), tuple("scores", "object.notNull"));
	}

	@Test
	void constraintOnTargetIsCheckedAfterTheArguments() {
		final Map<String, String> address = Map.of("street", "1-2-3", "city", "Tokyo");
		final ConstraintViolations violations = validator
			.validate("yavi@example.com", null, 17, address, null, List.of("admin"), Map.of())
			.errors();
		assertThat(violations)
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey, ConstraintViolation::message)
			.containsExactly(tuple("roles", "user.adultAdmin", "\"roles\" must not contain \"admin\" for a minor"));
	}

	@Test
	void definitionsDescribeTheArguments() {
		final Arguments8ValidatorBuilder<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> extended = builder
			._string(StringValidatorBuilder.of("unnamed", c -> c.notBlank()).build());
		assertThat(List.of(extended.d1, extended.d2, extended.d3, extended.d4, extended.d5, extended.d6, extended.d7,
				extended.d8))
			.extracting(ArgumentDefinition::name, ArgumentDefinition::kind, ArgumentDefinition::type,
					ArgumentDefinition::optional)
			.containsExactly(tuple("contact", Kind.OBJECT, Object.class, false),
					tuple("nickname", Kind.VALUE, String.class, true), tuple("age", Kind.VALUE, Integer.class, true),
					tuple("address", Kind.OBJECT, Object.class, false),
					tuple("billing", Kind.OBJECT, Object.class, true), tuple("roles", Kind.LIST, List.class, false),
					tuple("scores", Kind.MAP, Map.class, false), tuple(null, Kind.VALUE, String.class, false));
	}

}
