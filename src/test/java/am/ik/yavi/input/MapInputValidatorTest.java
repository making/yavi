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
import am.ik.yavi.core.InputReader;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

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

	/**
	 * The form in which an input is given to a validator of maps.
	 */
	enum Model {

		MAP {
			@Override
			<X> Validated<X> validate(ValueValidator<Map<String, Object>, X> validator,
					@Nullable Map<String, Object> input) {
				return validator.validate(input);
			}
		},
		// the same input as the tree model of Jackson, which is read without a copy
		JSON_NODE {
			@Override
			<X> Validated<X> validate(ValueValidator<Map<String, Object>, X> validator,
					@Nullable Map<String, Object> input) {
				return validator.readingWith(JsonNodeReader.INSTANCE).validate(jsonMapper.valueToTree(input));
			}
		},
		// a tree whose scalars are all strings, such as request parameters; a string is
		// coerced only where another type is expected
		STRINGS {
			@Override
			<X> Validated<X> validate(ValueValidator<Map<String, Object>, X> validator,
					@Nullable Map<String, Object> input) {
				return validator.readingWith(InputReader.ofJdkTypes().coercingStrings()).validate(strings(input));
			}
		};

		abstract <X> Validated<X> validate(ValueValidator<Map<String, Object>, X> validator,
				@Nullable Map<String, Object> input);

	}

	static final JsonMapper jsonMapper = new JsonMapper();

	sealed interface Shape {

	}

	record Circle(@Nullable Integer radius) implements Shape {
	}

	record Square(@Nullable String type, @Nullable Integer side) implements Shape {
	}

	static final MapInputValidator<Address> addressValidator = Yavi.arguments()
		._string("street", c -> c.notBlank())
		._string("city", c -> c.notBlank())
		.fromMap(Address::new)
		.rejectingUnknownProperties();

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
		.rejectingUnknownProperties()
		.constraintOnTarget(User::hasTagIfAdmin, "tags",
				ViolationMessage.of("user.adminTags", "An admin must have a tag"));

	static final ValueValidator<Map<String, Object>, Shape> shapeValidator = DiscriminatedUnionValidator
		.<Shape>of("type")
		// the discriminator is not an unknown property of an alternative that rejects
		// them
		.when("circle",
				Yavi.arguments()
					._integer("radius", c -> c.notNull().positive())
					.fromMap(Circle::new)
					.rejectingUnknownProperties())
		// an alternative may define the discriminator itself, and ignores unknown
		// properties by default
		.when("square", Yavi.arguments()._string("type")._integer("side", c -> c.notNull()).fromMap(Square::new));

	static Map<String, @Nullable Object> map(@Nullable Object... keysAndValues) {
		final Map<String, @Nullable Object> map = new LinkedHashMap<>();
		for (int i = 0; i < keysAndValues.length; i += 2) {
			map.put((String) keysAndValues[i], keysAndValues[i + 1]);
		}
		return map;
	}

	/**
	 * Returns the same tree with every scalar as a string.
	 */
	static @Nullable Object strings(@Nullable Object node) {
		if (node instanceof Map<?, ?>object) {
			final Map<Object, @Nullable Object> strings = new LinkedHashMap<>();
			object.forEach((name, value) -> strings.put(name, strings(value)));
			return strings;
		}
		if (node instanceof List<?>array) {
			return array.stream().map(MapInputValidatorTest::strings).toList();
		}
		return node == null ? null : node.toString();
	}

	@SuppressWarnings({ "unchecked", "NullAway" })
	static Map<String, Object> input(@Nullable Object... keysAndValues) {
		return (Map<String, Object>) (Map<String, ?>) map(keysAndValues);
	}

	@ParameterizedTest
	@EnumSource(Model.class)
	void validMapCreatesTheObject(Model model) {
		// numbers are taken whatever their type is, as long as the value is kept
		final Validated<User> validated = model.validate(userValidator,
				input("email", "a@example.com", "age", 30L, "role", "ADMIN", "birthday", "2000-02-29", "address",
						map("street", "1-2-3", "city", "Tokyo"), "tags", List.of("a", "b"), "scores",
						map("math", new BigDecimal("90.0")), "rate", 2));
		assertThat(validated.value()).isEqualTo(new User("a@example.com", 30, Role.ADMIN, LocalDate.of(2000, 2, 29),
				new Address("1-2-3", "Tokyo"), List.of("a", "b"), Map.of("math", 90), Optional.empty(), 2.0));
		// an absent key is validated like null
		assertThat(model
			.validate(userValidator, input("email", "a@example.com", "age", 0, "role", "USER", "address",
					map("street", "1-2-3", "city", "Tokyo"), "tags", List.of(), "scores", map(), "nickname", null))
			.value()
			.rate()).isEqualTo(1.5);
	}

	@ParameterizedTest
	@EnumSource(Model.class)
	void violationsOfTheMapAreAccumulated(Model model) {
		if (model == Model.STRINGS) {
			// an empty string is a null node wherever it is, and a string that does not
			// stand for the type is reported like any other mismatch
			final Validated<User> coerced = model.validate(userValidator,
					input("email", "", "age", "", "role", "", "birthday", "", "address", map("street", "1-2-3"), "tags",
							List.of("a", ""), "scores", map("math", "-1", "art", "1.5", "music", " 1"), "nickname", "",
							"rate", "high", "name", ""));
			assertThat(coerced.errors())
				.extracting(ConstraintViolation::name, ConstraintViolation::messageKey,
						violation -> violation.violatedValue())
				.containsExactly(tuple("email", "charSequence.notBlank", null), tuple("age", "object.notNull", null),
						tuple("role", "object.notNull", null), tuple("address.city", "object.required", null),
						tuple("tags[1]", "charSequence.notBlank", null),
						tuple("scores.math", "numeric.greaterThanOrEqual", -1),
						tuple("scores.art", "object.type", "1.5"), tuple("scores.music", "object.type", " 1"),
						tuple("rate", "object.type", "high"), tuple("name", "object.unknownProperty", null));
			return;
		}
		final Validated<User> validated = model.validate(userValidator,
				input("email", null, "age", "30", "role", "GUEST", "birthday", "2001-02-29", "address",
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
		assertThat(model
			.validate(userValidator,
					input("email", "a@example.com", "age", 1, "role", "ADMIN", "address", "Tokyo", "tags", "a",
							"scores", List.of()))
			.errors()).extracting(ConstraintViolation::message)
			.containsExactly("\"address\" must be of type object", "\"tags\" must be of type array",
					"\"scores\" must be of type object");
		assertThat(model
			.validate(userValidator,
					input("email", "a@example.com", "age", 1, "role", "ADMIN", "address",
							map("street", "1-2-3", "city", "Tokyo"), "tags", List.of(), "scores", map()))
			.errors()).extracting(ConstraintViolation::messageKey).containsExactly("user.adminTags");
		assertThat(model.validate(userValidator, null).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.notNull");
		// any input is taken once untyped
		assertThat(addressValidator.untyped().validate(List.of()).errors()).extracting(ConstraintViolation::message)
			.containsExactly("\"\" must be of type object");
	}

	@ParameterizedTest
	@EnumSource(Model.class)
	void discriminatorSelectsTheAlternative(Model model) {
		assertThat(model.validate(shapeValidator, input("type", "circle", "radius", 2)).value())
			.isEqualTo(new Circle(2));
		assertThat(model.validate(shapeValidator, input("type", "square", "side", 3, "color", "red")).value())
			.isEqualTo(new Square("square", 3));
		// only the selected alternative is validated
		assertThat(model.validate(shapeValidator, input("type", "circle", "radius", 0, "side", 3)).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("radius", "numeric.positive"), tuple("side", "object.unknownProperty"));
		assertThat(model.validate(shapeValidator, input("type", "triangle")).errors())
			.extracting(ConstraintViolation::message)
			.containsExactly("\"type\" must be one of the following values: [circle, square]");
		assertThat(model.validate(shapeValidator, input("type", 1)).errors())
			.extracting(ConstraintViolation::messageKey)
			.containsExactly("object.oneOf");
		assertThat(model.validate(shapeValidator, input("radius", 2)).errors()).extracting(ConstraintViolation::message)
			.containsExactly("\"type\" is required");
		// a union is nested like any other validator
		final MapInputValidator<Optional<List<Shape>>> drawing = Yavi.arguments()
			._list("shapes", shapeValidator)
			.fromMap(Optional::ofNullable);
		assertThat(model.validate(drawing, input("shapes", List.of(map("type", "circle"), "square"))).errors())
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
		return Stream.of(exact(Integer.class, 1L, 1), exact(Integer.class, 1.0, 1), exact(Integer.class, 1.5, null),
				exact(Integer.class, 1L << 40, null), exact(Long.class, new BigInteger("5"), 5L),
				exact(BigInteger.class, 5, new BigInteger("5")),
				// the digits of a huge exponent are not written out
				exact(BigInteger.class, new BigDecimal("1e1000000000"), null), exact(Double.class, 1, 1.0),
				exact(Double.class, new BigDecimal("0.1"), 0.1), exact(Double.class, new BigDecimal("1e400"), null),
				exact(Double.class, Double.NaN, Double.NaN), exact(Float.class, 0.1, 0.1f),
				exact(BigDecimal.class, 0.1, new BigDecimal("0.1")), exact(String.class, 1, null),
				exact(Role.class, Role.USER, Role.USER), exact(Role.class, "user", null),
				exact(Year.class, 2026, Year.of(2026)), exact(Year.class, "2026", Year.of(2026)),
				exact(Map.class, Map.of(1, 2), null), exact(Object.class, List.of(1), List.of(1)),
				// a string is a number or a boolean only when strings are coerced
				coerced(Integer.class, "1", 1), coerced(Integer.class, "+1.0e1", 10),
				coerced(Integer.class, "1.5", null), coerced(Integer.class, " 1", null),
				coerced(Integer.class, "0x1", null), coerced(Integer.class, "2147483648", null),
				coerced(Long.class, "2147483648", 2147483648L), coerced(Short.class, "-1", (short) -1),
				coerced(Byte.class, "128", null), coerced(BigInteger.class, "1e3", new BigInteger("1000")),
				coerced(BigInteger.class, "1e1000000000", null), coerced(BigDecimal.class, "1,000", null),
				coerced(BigDecimal.class, "0.10", new BigDecimal("0.10")), coerced(Double.class, "0.1", 0.1),
				coerced(Double.class, "NaN", null), coerced(Double.class, "1e400", null),
				coerced(Float.class, "0.1", 0.1f), coerced(Boolean.class, "true", true),
				coerced(Boolean.class, "false", false), coerced(Boolean.class, "TRUE", null),
				coerced(Boolean.class, "on", null),
				// a string stays a string
				Arguments.of(String.class, "1", "1", "1"), Arguments.of(Object.class, "1", "1", "1"));
	}

	/**
	 * An input that is read in the same way whether strings are coerced or not.
	 */
	static Arguments exact(Class<?> type, Object input, @Nullable Object expected) {
		return Arguments.of(type, input, expected, expected);
	}

	/**
	 * A string that represents the type only when strings are coerced.
	 */
	static Arguments coerced(Class<?> type, String input, @Nullable Object expected) {
		return Arguments.of(type, input, null, expected);
	}

	@ParameterizedTest
	@MethodSource("untypedInputs")
	<T> void untypedInputMustRepresentTheType(Class<T> type, Object input, @Nullable Object expected,
			@Nullable Object coerced) {
		final ValueValidator<Object, T> validator = ValueValidator.<T>passThrough().untyped(type);
		final Map<ValueValidator<Object, T>, @Nullable Object> expectations = new LinkedHashMap<>();
		expectations.put(validator, expected);
		expectations.put(validator.readingWith(InputReader.ofJdkTypes().coercingStrings()), coerced);
		expectations.forEach((untyped, value) -> {
			final Validated<T> validated = untyped.validate(input);
			if (value == null) {
				assertThat(validated.errors())
					.extracting(ConstraintViolation::messageKey, ConstraintViolation::violatedValue)
					.containsExactly(tuple(type.isEnum() ? "object.oneOf" : "object.type", input));
			}
			else {
				assertThat(validated.value()).isEqualTo(value);
			}
		});
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
		final var arguments = Yavi.arguments()
			._object("count", coerced.untyped(Object.class))
			._object("id", id)
			._object("payload", Object.class);
		final MapInputValidator<List<Object>> validator = arguments
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
		// the same arguments are read from a node: a validator that takes an Object is
		// given the value of the node, a null node is null wherever it is, and the result
		// can be given yet another reader
		final ValueValidator<JsonNode, List<Object>> nodeValidator = arguments.from(JsonNodeReader.INSTANCE,
				(count, i, payload) -> Arrays.asList(count, i, payload));
		final JsonNode node = jsonMapper.readTree("""
				{"count":"10","id":7,"payload":[1]}""");
		assertThat(nodeValidator.validated(node)).containsExactly(10, 7, node.get("payload"));
		assertThat(nodeValidator.readingWith(InputReader.ofJdkTypes())
			.validated(input("count", "10", "id", 7, "payload", List.of(1)))).containsExactly(10, 7, List.of(1));
		assertThat(StringValidatorBuilder.of("name", c -> c.notBlank())
			.build()
			.optional()
			.readingWith(JsonNodeReader.INSTANCE)
			.validated(jsonMapper.nullNode())).isEmpty();
		// a property or an alternative that is a lambda is read only from JDK types
		final MapInputValidator<Object> lambda = MapInputValidator
			.of(List.of(new MapInputValidator.Property<>("count", coerced)), values -> values.get(0));
		assertThat(lambda.untyped().validated(map("count", "10"))).isEqualTo(10);
		// the node is read as it is under another reader, coerced or not: its strings are
		// coerced, and its null node and an empty string are both null
		final ValueValidator<JsonNode, Square> square = Yavi.arguments()
			._string("type")
			._integer("side", c -> c.notNull())
			.from(JsonNodeReader.INSTANCE.coercingStrings(), Square::new);
		assertThat(square.validated(jsonMapper.readTree("""
				{"type":"","side":"3"}"""))).isEqualTo(new Square(null, 3));
		assertThat(square.validate(jsonMapper.readTree("""
				{"type":null,"side":""}""")).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.notNull");
		assertThatThrownBy(() -> lambda.readingWith(JsonNodeReader.INSTANCE))
			.isInstanceOf(UnsupportedOperationException.class);
	}

}
