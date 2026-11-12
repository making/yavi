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
		},
		// the same strings as flat parameters, whose keys are the paths of the scalars
		PARAMETERS {
			@Override
			<X> Validated<X> validate(ValueValidator<Map<String, Object>, X> validator,
					@Nullable Map<String, Object> input) {
				final Map<String, @Nullable Object> parameters = new LinkedHashMap<>();
				flatten("", strings(input), parameters);
				return validator.readingWith(ParameterTree.reader()).validate(ParameterTree.of(parameters));
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

	static final DiscriminatedUnionValidator<Shape> shapeValidator = DiscriminatedUnionValidator.<Shape>of("type")
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

	/**
	 * Puts the scalars of a tree of strings under their path. A list of scalars is the
	 * values of its key, and the elements of any other list are indexed. An empty object
	 * or list has no parameter.
	 */
	static void flatten(String path, @Nullable Object node, Map<String, @Nullable Object> parameters) {
		if (node instanceof Map<?, ?>object) {
			object.forEach(
					(name, value) -> flatten(path.isEmpty() ? (String) name : path + "." + name, value, parameters));
		}
		else if (node instanceof List<?>array && array.stream().anyMatch(Map.class::isInstance)) {
			for (int i = 0; i < array.size(); i++) {
				flatten(path + "[" + i + "]", array.get(i), parameters);
			}
		}
		else if (!(node instanceof List<?>array) || !array.isEmpty()) {
			parameters.put(path, node);
		}
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
		final Validated<User> defaulted = model.validate(userValidator,
				input("email", "a@example.com", "age", 0, "role", "USER", "address",
						map("street", "1-2-3", "city", "Tokyo"), "tags", List.of(), "scores", map(), "nickname", null));
		if (model == Model.PARAMETERS) {
			// parameters cannot hold an empty list or object: its key is absent
			assertThat(defaulted.errors()).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
				.containsExactly(tuple("tags", "object.required"), tuple("scores", "object.required"));
		}
		else {
			assertThat(defaulted.value().rate()).isEqualTo(1.5);
		}
	}

	@ParameterizedTest
	@EnumSource(Model.class)
	void violationsOfTheMapAreAccumulated(Model model) {
		if (model == Model.STRINGS || model == Model.PARAMETERS) {
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
		// a map of parameters is read with the type it has
		final ValueValidator<Map<String, String>, Shape> ofStrings = shapeValidator.readingParameters();
		assertThat(ofStrings.validated(Map.of("type", "circle", "radius", "2"))).isEqualTo(new Circle(2));
		final ValueValidator<Map<String, ?>, Address> address = addressValidator.readingParameters();
		assertThat(address.validate(Map.of("street", "1-2-3", "city", "")).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("city", "charSequence.notBlank"));
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

	record Item(@Nullable String name, @Nullable Integer quantity) {
	}

	record Order(@Nullable String customer, @Nullable List<Item> items, @Nullable List<Integer> coupons,
			@Nullable Map<String, Integer> stock, @Nullable Object note) {
	}

	@Test
	void parametersAreReadAsTheTreeTheyStandFor() {
		final ValueValidator<Map<String, ?>, Order> validator = Yavi.arguments()
			._string("customer", c -> c.notBlank())
			._list("items",
					Yavi.arguments()
						._string("name", c -> c.notBlank())
						._integer("quantity", c -> c.positive())
						.defaultValue(1)
						.fromMap(Item::new)
						.rejectingUnknownProperties())
			// a list that may be empty has a default value, because its key is then
			// absent
			.defaultValue(List.of())
			._list("coupons", IntegerValidatorBuilder.of("", c -> c.positive()).build())
			._map("stock", IntegerValidatorBuilder.of("", c -> c.greaterThanOrEqual(0)).build())
			.defaultValue(Map.of())
			._object("note", Object.class)
			.fromMap(Order::new)
			.rejectingUnknownProperties()
			.readingParameters();
		// the names are written with dots or brackets, the indexes in any order; one
		// value
		// is a scalar or a list of one element, and a value may be a list or an array
		final Map<String, Object> parameters = new LinkedHashMap<>();
		parameters.put("items[1][name]", new String[] { "pen" });
		parameters.put("items[0].name", List.of("ink"));
		parameters.put("items.0.quantity", "2");
		parameters.put("customer", List.of("a"));
		parameters.put("coupons[]", "7");
		parameters.put("stock[1]", "3");
		parameters.put("stock[a.b]", "0");
		parameters.put("note", new String[] { "x", "y" });
		assertThat(validator.validated(parameters))
			.isEqualTo(new Order("a", List.of(new Item("ink", 2), new Item("pen", 1)), List.of(7),
					Map.of("1", 3, "a.b", 0), List.of("x", "y")));
		// keys of the same node are merged, a list of one empty string is null like an
		// absent one, and a list of indexes is also an object
		assertThat(validator.validated(input("customer", "a", "coupons", List.of("1", "2"), "coupons[]", "3", "items",
				"", "stock[0]", "5", "note", "x")))
			.isEqualTo(new Order("a", List.of(), List.of(1, 2, 3), Map.of("0", 5), "x"));
		final Validated<Order> validated = validator.validate(input(
				// several values are not a scalar
				"customer", List.of("a", "b"),
				// a skipped index is null, and the others keep theirs
				"items[2].name", "", "items[2].price", "1",
				// an index above the limit, with a leading zero or after a name is a name
				"coupons[256]", "1", "stock[01]", "x", "stock[0]", "1",
				// keys that are not paths
				"items[0", "1", "[0]", "1", "a..b", "1", "a[]b", "1", "a[][b]", "1", ".", "1", "", "1",
				"deep" + ".x".repeat(ParameterTree.MAX_DEPTH), "1",
				// an unknown key is reported with its tree
				"extra.b[0]", "1"));
		assertThat(validated.errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey, ConstraintViolation::violatedValue)
			.containsExactly(tuple("customer", "object.type", List.of("a", "b")),
					tuple("items[0]", "object.notNull", null), tuple("items[1]", "object.notNull", null),
					tuple("items[2].name", "charSequence.notBlank", null),
					tuple("items[2].price", "object.unknownProperty", "1"),
					tuple("coupons", "object.type", Map.of("256", "1")), tuple("stock.01", "object.type", "x"),
					tuple("items[0", "object.unknownProperty", "1"), tuple("[0]", "object.unknownProperty", "1"),
					tuple("a..b", "object.unknownProperty", "1"), tuple("a[]b", "object.unknownProperty", "1"),
					tuple("a[][b]", "object.unknownProperty", "1"), tuple(".", "object.unknownProperty", "1"),
					tuple("", "object.unknownProperty", "1"),
					tuple("deep" + ".x".repeat(ParameterTree.MAX_DEPTH), "object.unknownProperty", "1"),
					tuple("extra", "object.unknownProperty", Map.of("b", List.of("1"))));
		// a key with values and nested keys is neither a scalar, a list nor an object
		assertThat(Yavi.arguments()
			._string("s")
			._list("l", StringValidatorBuilder.of("", c -> c.notBlank()).build())
			._object("o", addressValidator)
			._object("note", Object.class)
			.fromMap((s, l, o, note) -> "")
			.readingParameters()
			.validate(input("s", "x", "s.b", "y", "l", "x", "l[0]", "y", "o", "x", "o.street", "y", "note", "x",
					"note.lang", "en"))
			.errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey, ConstraintViolation::violatedValue)
			.containsExactly(tuple("s", "object.type", Map.of("", "x", "b", "y")),
					tuple("l", "object.type", Map.of("", "x", "0", "y")),
					tuple("o", "object.type", Map.of("", "x", "street", "y")));
		// the largest index is given with the tree, which is what any validator reads
		final ValueValidator<ParameterTree, Optional<List<Integer>>> coupons = Yavi.arguments()
			._list("coupons", IntegerValidatorBuilder.of("", c -> c.positive()).build())
			.from(ParameterTree.reader(), Optional::ofNullable);
		assertThat(coupons.validated(ParameterTree.of(Map.of("coupons[1]", "5"), 1))).contains(Arrays.asList(null, 5));
		assertThat(coupons.validate(ParameterTree.of(Map.of("coupons[2]", "5"), 1)).errors())
			.extracting(ConstraintViolation::message)
			.containsExactly("\"coupons\" must be of type array");
		assertThat(validator.validate(null).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.notNull");
		assertThatThrownBy(() -> ParameterTree.of(Map.of(), -1)).isInstanceOf(IllegalArgumentException.class);
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
