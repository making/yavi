/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import am.ik.yavi.builder.IntegerValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.PathSegment;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import am.ik.yavi.jsonschema.JsonSchema;
import am.ik.yavi.validator.Yavi;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class MapInputValidatorTest {

	enum Role {

		ADMIN, USER

	}

	record Address(@Nullable String street, @Nullable String city) {
	}

	record User(@Nullable String email, @Nullable Integer age, @Nullable Role role, @Nullable LocalDate birthday,
			@Nullable Address address, @Nullable List<String> tags, @Nullable Map<String, Integer> scores,
			@Nullable Optional<String> nickname, @Nullable Double rate) {

		boolean hasTagIfAdmin() {
			return this.role != Role.ADMIN || (this.tags != null && !this.tags.isEmpty());
		}
	}

	sealed interface Shape {

	}

	record Circle(@Nullable Integer radius) implements Shape {
	}

	record Square(@Nullable String type, @Nullable Integer side) implements Shape {
	}

	static final MapInputValidator<Address> addressValidator = Yavi.arguments()
		._string("street", c -> c.notBlank())
		._string("city", c -> c.notBlank())
		.fromMap(Address::new);

	static final ValueValidator<Map<String, Object>, User> userValidator = Yavi.arguments()
		._string("email", c -> c.notBlank().email())
		._integer("age", c -> c.notNull().greaterThanOrEqual(0))
		._enum("role", Role.class, c -> c.notNull())
		._localDate("birthday")
		._object("address", addressValidator)
		._list("tags", StringValidatorBuilder.of("", c -> c.notBlank()).build())
		._map("scores", IntegerValidatorBuilder.of("", c -> c.greaterThanOrEqual(0)).build())
		._string("nickname", c -> c.notBlank())
		.optional()
		._double("rate")
		.defaultValue(1.5)
		.fromMap(User::new)
		.constraintOnTarget(User::hasTagIfAdmin, "tags",
				ViolationMessage.of("user.adminTags", "An admin must have a tag"));

	static final ValueValidator<Map<String, Object>, Shape> shapeValidator = DiscriminatedUnionValidator
		.<Shape>of("type")
		.when("circle", Yavi.arguments()._integer("radius", c -> c.notNull().positive()).fromMap(Circle::new))
		// an alternative may define the discriminator itself
		.when("square", Yavi.arguments()._string("type")._integer("side", c -> c.notNull()).fromMap(Square::new));

	static Map<String, @Nullable Object> map(@Nullable Object... keysAndValues) {
		final Map<String, @Nullable Object> map = new LinkedHashMap<>();
		for (int i = 0; i < keysAndValues.length; i += 2) {
			map.put((String) keysAndValues[i], keysAndValues[i + 1]);
		}
		return map;
	}

	@SuppressWarnings({ "unchecked", "NullAway" })
	static Map<String, Object> input(@Nullable Object... keysAndValues) {
		return (Map<String, Object>) (Map<String, ?>) map(keysAndValues);
	}

	@Test
	void validMapCreatesTheObject() {
		// numbers are taken whatever their type is, as long as the value is kept
		final Validated<User> validated = userValidator.validate(input("email", "a@example.com", "age", 30L, "role",
				"ADMIN", "birthday", "2000-02-29", "address", map("street", "1-2-3", "city", "Tokyo"), "tags",
				List.of("a", "b"), "scores", map("math", new BigDecimal("90.0")), "rate", 2));
		assertThat(validated.value()).isEqualTo(new User("a@example.com", 30, Role.ADMIN, LocalDate.of(2000, 2, 29),
				new Address("1-2-3", "Tokyo"), List.of("a", "b"), Map.of("math", 90), Optional.empty(), 2.0));
		// an absent key is validated like null
		assertThat(userValidator
			.validated(input("email", "a@example.com", "age", 0, "role", "USER", "address",
					map("street", "1-2-3", "city", "Tokyo"), "tags", List.of(), "scores", map(), "nickname", null))
			.rate()).isEqualTo(1.5);
	}

	@Test
	void violationsOfTheMapAreAccumulated() {
		final Validated<User> validated = userValidator
			.validate(input("email", null, "age", "30", "role", "GUEST", "birthday", "2001-02-29", "address",
					map("street", "", "zip", "100"), "tags", Arrays.asList("a", 1, " "), "scores",
					map("math", -1, "art", 1.5), "nickname", 1, "rate", "high", "name", "x"));
		assertThat(validated.errors()).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(
					// an explicit null is reported by the constraints of the property
					tuple("email", "charSequence.notBlank"),
					// nothing is coerced
					tuple("age", "object.type"), tuple("role", "object.oneOf"), tuple("birthday", "object.type"),
					tuple("address.street", "charSequence.notBlank"),
					// an absent key of a property that rejects null
					tuple("address.city", "object.required"), tuple("address.zip", "object.unknownProperty"),
					tuple("tags[1]", "object.type"), tuple("tags[2]", "charSequence.notBlank"),
					tuple("scores.math", "numeric.greaterThanOrEqual"), tuple("scores.art", "object.type"),
					tuple("nickname", "object.type"), tuple("rate", "object.type"),
					tuple("name", "object.unknownProperty"));
		assertThat(validated.errors()).extracting(ConstraintViolation::message)
			.contains("\"age\" must be of type integer", "\"role\" must be one of the following values: [ADMIN, USER]",
					"\"birthday\" must be of type LocalDate", "\"address.city\" is required",
					"\"address.zip\" is not a known property", "\"tags[1]\" must be of type string",
					"\"scores.art\" must be of type integer");
		assertThat(validated.errors().get(7).path()).containsExactly(new PathSegment.Property("tags"),
				new PathSegment.Index(1));
		// a nested object, list or map must be one, and the rule on the target is checked
		// only when the map is valid
		assertThat(userValidator
			.validate(input("email", "a@example.com", "age", 1, "role", "ADMIN", "address", "Tokyo", "tags", "a",
					"scores", List.of()))
			.errors()).extracting(ConstraintViolation::message)
			.containsExactly("\"address\" must be of type object", "\"tags\" must be of type array",
					"\"scores\" must be of type object");
		assertThat(userValidator
			.validate(input("email", "a@example.com", "age", 1, "role", "ADMIN", "address",
					map("street", "1-2-3", "city", "Tokyo"), "tags", List.of(), "scores", map()))
			.errors()).extracting(ConstraintViolation::messageKey).containsExactly("user.adminTags");
		assertThat(userValidator.validate(null).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.notNull");
		// unknown properties are ignored on request, and any input is taken once untyped
		final ValueValidator<Object, Address> lenient = addressValidator.ignoringUnknownProperties().untyped();
		assertThat(lenient.validated(map("street", "1-2-3", "city", "Tokyo", "zip", "100")))
			.isEqualTo(new Address("1-2-3", "Tokyo"));
		assertThat(lenient.validate(List.of()).errors()).extracting(ConstraintViolation::message)
			.containsExactly("\"\" must be of type object");
	}

	@Test
	void discriminatorSelectsTheAlternative() {
		assertThat(shapeValidator.validated(input("type", "circle", "radius", 2))).isEqualTo(new Circle(2));
		assertThat(shapeValidator.validated(input("type", "square", "side", 3))).isEqualTo(new Square("square", 3));
		// only the selected alternative is validated
		assertThat(shapeValidator.validate(input("type", "circle", "radius", 0, "side", 3)).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("radius", "numeric.positive"), tuple("side", "object.unknownProperty"));
		assertThat(shapeValidator.validate(input("type", "triangle")).errors()).extracting(ConstraintViolation::message)
			.containsExactly("\"type\" must be one of the following values: [circle, square]");
		assertThat(shapeValidator.validate(input("type", 1)).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.oneOf");
		assertThat(shapeValidator.validate(input("radius", 2)).errors()).extracting(ConstraintViolation::message)
			.containsExactly("\"type\" is required");
		// a union is nested like any other validator
		final MapInputValidator<Optional<List<Shape>>> drawing = Yavi.arguments()
			._list("shapes", shapeValidator)
			.fromMap(Optional::ofNullable);
		assertThat(drawing.validate(input("shapes", List.of(map("type", "circle"), "square"))).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("shapes[0].radius", "object.required"), tuple("shapes[1]", "object.type"));
		assertThat(JsonSchema.toJson(drawing.description())).isEqualTo("""
				{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{\
				"shapes":{"type":"array","items":{"anyOf":[\
				{"type":"object","properties":{"type":{"type":"string","const":"circle"},\
				"radius":{"type":"integer","exclusiveMinimum":0}},"required":["type","radius"]},\
				{"type":"object","properties":{"type":{"type":["string","null"]},\
				"side":{"type":"integer"}},"required":["side"]}]}}},"required":["shapes"]}""");
	}

	static Stream<Arguments> untypedInputs() {
		return Stream.of(Arguments.of(Integer.class, 1L, 1), Arguments.of(Integer.class, 1.0, 1),
				Arguments.of(Integer.class, 1.5, null), Arguments.of(Integer.class, 1L << 40, null),
				Arguments.of(Integer.class, "1", null), Arguments.of(Long.class, new BigInteger("5"), 5L),
				Arguments.of(BigInteger.class, 5, new BigInteger("5")), Arguments.of(Double.class, 1, 1.0),
				Arguments.of(Double.class, new BigDecimal("0.1"), 0.1),
				Arguments.of(Double.class, new BigDecimal("1e400"), null),
				Arguments.of(Double.class, Double.NaN, Double.NaN), Arguments.of(Float.class, 0.1, 0.1f),
				Arguments.of(BigDecimal.class, 0.1, new BigDecimal("0.1")), Arguments.of(String.class, 1, null),
				Arguments.of(Boolean.class, "true", null), Arguments.of(Role.class, Role.USER, Role.USER),
				Arguments.of(Role.class, "user", null), Arguments.of(Year.class, 2026, Year.of(2026)),
				Arguments.of(Year.class, "2026", Year.of(2026)), Arguments.of(Map.class, Map.of(1, 2), null),
				Arguments.of(Object.class, List.of(1), List.of(1)));
	}

	@ParameterizedTest
	@MethodSource("untypedInputs")
	<T> void untypedInputMustRepresentTheType(Class<T> type, Object input, @Nullable Object expected) {
		final Validated<T> validated = ValueValidator.<T>passThrough().untyped(type).validate(input);
		if (expected == null) {
			assertThat(validated.isValid()).isFalse();
		}
		else {
			assertThat(validated.value()).isEqualTo(expected);
		}
	}

	@Test
	void inputTypeMustBeKnownToReadFromAMap() {
		// a value validator that takes an Object decides itself what it accepts
		final ValueValidator<Object, Integer> coerced = (value, locale, context) -> ValueValidator
			.<Integer>passThrough()
			.untyped(Integer.class)
			.validate(value instanceof String string ? Integer.valueOf(string) : value, locale, context);
		// alternatives are untyped one by one
		final ValueValidator<Object, Object> id = ValueValidator.anyOf(
				StringValidatorBuilder.of("", c -> c.notBlank()).build().untyped(),
				IntegerValidatorBuilder.of("", c -> c.positive()).build().untyped());
		final MapInputValidator<List<Object>> validator = Yavi.arguments()
			._object("count", coerced.untyped(Object.class))
			._object("id", id)
			._object("payload", Object.class)
			.fromMap((count, i, payload) -> Arrays.asList(count, i, payload));
		assertThat(validator.validated(input("count", "10", "id", 7, "payload", List.of(1)))).containsExactly(10, 7,
				List.of(1));
		assertThat(validator.validate(input("count", true, "id", true)).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("count", "object.type"), tuple("id", "object.anyOf"), tuple("id", "object.type"),
					tuple("id", "object.type"));
		assertThatThrownBy(() -> Yavi.arguments().<Role>_enum("role").fromMap(Optional::ofNullable))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("The input type of the argument 'role' is unknown");
		assertThatThrownBy(() -> Yavi.arguments()._object("count", coerced).fromMap(Optional::ofNullable))
			.isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(
				() -> Yavi.arguments()._integer(coerced.untyped(Object.class)::validate).fromMap(Optional::ofNullable))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("The argument at position 1 has no name");
		assertThatThrownBy(() -> Yavi.arguments()._string("a")._integer("a").fromMap((a, b) -> ""))
			.isInstanceOf(IllegalArgumentException.class);
	}

}
