/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import am.ik.yavi.arguments.Arguments2;
import am.ik.yavi.arguments.EnumValidator;
import am.ik.yavi.builder.EnumValidatorBuilder;
import am.ik.yavi.builder.IntegerValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.ConstraintDescription;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.PathSegment;
import am.ik.yavi.core.Presence;
import am.ik.yavi.core.StringCoercion;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ObjectInputValidatorTest {

	enum Role {

		ADMIN, USER

	}

	record Address(String street, String city) {
	}

	record User(String email, Integer age, Role role, @Nullable LocalDate birthday, Address address,
			@Nullable List<String> tags, @Nullable Map<String, Integer> scores, Optional<String> nickname,
			Double rate) {

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
		// the same input as the tree model of the JDK (JEP 540), parsed from its JSON
		// text;
		// the reader is compiled only by a JDK that has the model
		JDK_JSON {
			@Override
			@SuppressWarnings("unchecked")
			<X> Validated<X> validate(ValueValidator<Map<String, Object>, X> validator,
					@Nullable Map<String, Object> input) {
				assumeTrue(Runtime.version().feature() >= 28, "jdk.incubator.json is a module of JDK 28 or later");
				try {
					final InputReader<Object> reader = (InputReader<Object>) Class
						.forName("am.ik.yavi.input.JdkJsonReader")
						.getDeclaredField("INSTANCE")
						.get(null);
					final Object tree = Class.forName("jdk.incubator.json.Json")
						.getMethod("parse", String.class)
						.invoke(null, jsonMapper.writeValueAsString(input));
					return validator.readingWith(reader).validate(tree);
				}
				catch (ReflectiveOperationException e) {
					throw new IllegalStateException(e);
				}
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

	record Circle(Integer radius) implements Shape {
	}

	record Square(@Nullable String type, Integer side) implements Shape {
	}

	static final ObjectInputValidator<Address> addressValidator = Yavi.schema()
		._string("street", c -> c.notBlank())
		._string("city", c -> c.notBlank())
		.fromMap(Address::new)
		.rejectingUnknownProperties();

	static final ValueValidator<Map<String, Object>, User> userValidator = Yavi.schema()
		._string("email", c -> c.trim().toLowerCase().notBlank().email())
		._integer("age", c -> c.notNull().greaterThanOrEqual(0))
		._enum("role", Role.class, c -> c.notNull())
		._localDate("birthday")
		.nullable()
		._object("address", addressValidator)
		._list("tags", StringValidatorBuilder.of("", c -> c.notBlank()).build(), c -> c.unique())
		._map("scores", IntegerValidatorBuilder.of("", c -> c.greaterThanOrEqual(0)).build(), c -> c.lessThanOrEqual(2))
		._string("nickname", c -> c.trim().notBlank())
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
				Yavi.schema()
					._integer("radius", c -> c.notNull().positive())
					.fromMap(Circle::new)
					.rejectingUnknownProperties())
		// an alternative may define the discriminator itself, and ignores unknown
		// properties by default
		.when("square", Yavi.schema()._string("type").nullable()._integer("side").fromMap(Square::new));

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
			return array.stream().map(ObjectInputValidatorTest::strings).toList();
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
		// numbers are taken whatever their type is, as long as the value is kept; a
		// string is transformed before it is constrained
		final Validated<User> validated = model.validate(userValidator,
				input("email", " A@example.com ", "age", 30L, "role", "ADMIN", "birthday", "2000-02-29", "address",
						map("street", "1-2-3", "city", "Tokyo"), "tags", List.of("a", "b"), "scores",
						map("math", new BigDecimal("90.0")), "nickname", " yavi", "rate", 2));
		assertThat(validated.value()).isEqualTo(new User("a@example.com", 30, Role.ADMIN, LocalDate.of(2000, 2, 29),
				new Address("1-2-3", "Tokyo"), List.of("a", "b"), Map.of("math", 90), Optional.of("yavi"), 2.0));
		// a number that no primitive type holds is read without a loss
		final BigInteger integer = new BigInteger("123456789012345678901234567890");
		final BigDecimal decimal = new BigDecimal("1e400");
		assertThat(model
			.validate(Yavi.schema()._bigInteger("integer")._bigDecimal("decimal").fromMap(List::of),
					input("integer", integer, "decimal", decimal))
			.value()).isEqualTo(List.of(integer, decimal));
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
		// a recovering argument never fails: a value that the reader takes for the type
		// is validated, and an absent key, a violated constraint, a value that does not
		// represent the type and an invalid embedded object give the fallback
		final Address nowhere = new Address("", "");
		final ObjectInputValidator<Page> page = Yavi.schema()
			._integer("size", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(100))
			.recover(20)
			._enum("role", Role.class, c -> c.notNull())
			.recoverWith(violations -> violations.isEmpty() ? Role.ADMIN : Role.USER)
			._embedded(addressValidator)
			.recover(nowhere)
			.fromMap(Page::new);
		assertThat(model.validate(page, input("size", 50, "role", "ADMIN", "street", "1-2-3", "city", "Tokyo")).value())
			.isEqualTo(new Page(50, Role.ADMIN, new Address("1-2-3", "Tokyo")));
		assertThat(model.validate(page, input("size", 999, "role", "admin", "street", "1-2-3")).value())
			.isEqualTo(new Page(20, Role.USER, nowhere));
		assertThat(model.validate(page, input("size", "many")).value()).isEqualTo(new Page(20, Role.ADMIN, nowhere));
	}

	@ParameterizedTest
	@EnumSource(Model.class)
	void violationsOfTheMapAreAccumulated(Model model) {
		// the constraints of a list or map are checked on the validated elements
		assertThat(model
			.validate(userValidator,
					input("email", "a@example.com", "age", 1, "role", "USER", "address",
							map("street", "1-2-3", "city", "Tokyo"), "tags", List.of("a", "b", "a"), "scores",
							map("math", 1, "art", 2, "music", 3)))
			.errors()).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("tags", "collection.unique"), tuple("scores", "container.lessThanOrEqual"));
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
			.containsExactly("The value must be of type object");
	}

	record Booking(DateRange period) {
	}

	record DateRange(LocalDate start, LocalDate end) {

		static Validated<DateRange> parse(LocalDate start, LocalDate end) {
			return start.isBefore(end) ? Validated.successWith(new DateRange(start, end))
					: Validated.failureWith(ViolationMessage.of("dateRange.invalid", "\"{0}\" must start before {1}"),
							start, Locale.ENGLISH, end);
		}

	}

	@ParameterizedTest
	@EnumSource(Model.class)
	void functionOfTheValidatedArgumentsCanFail(Model model) {
		final ObjectInputValidator<DateRange> rangeValidator = Yavi.schema()
			._localDate("start", c -> c.notNull())
			._localDate("end", c -> c.notNull())
			.flatFromMap(DateRange::parse)
			.rejectingUnknownProperties();
		final ValueValidator<Map<String, Object>, Booking> validator = Yavi.schema()
			._object("period", rangeValidator)
			.fromMap(Booking::new);
		assertThat(model.validate(validator, input("period", map("start", "2026-01-01", "end", "2026-01-31"))).value())
			.isEqualTo(new Booking(new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))));
		// the violation of the function is reported on the object it creates
		final ConstraintViolations violations = model
			.validate(validator, input("period", map("start", "2026-02-01", "end", "2026-01-31")))
			.errors();
		assertThat(violations).extracting(ConstraintViolation::name, ConstraintViolation::messageKey,
				ConstraintViolation::message, ConstraintViolation::violatedValue)
			.containsExactly(tuple("period", "dateRange.invalid", "\"period\" must start before 2026-01-31",
					LocalDate.of(2026, 2, 1)));
		assertThat(violations.get(0).path()).containsExactly(new PathSegment.Property("period"));
		// the function is not applied to invalid arguments
		assertThat(model.validate(validator, input("period", map("start", "2026-02-01", "zone", "UTC"))).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("period.end", "object.required"), tuple("period.zone", "object.unknownProperty"));
		if (model != Model.MAP) {
			return;
		}
		// the same function over arguments, and the arguments themselves as a record
		assertThat(Yavi.schema()
			._localDate("start", c -> c.notNull())
			._localDate("end", c -> c.notNull())
			.flatApply(DateRange::parse)
			.validate(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 31))
			.errors()).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("", "dateRange.invalid"));
		assertThat(Yavi.schema()._string("name", c -> c.notBlank()).flatApply(name -> Validated.successWith(name.length())).validate("abc").value())
			.isEqualTo(3);
		final String described = switch (Yavi.schema()
			._string("name", c -> c.notBlank())
			._integer("age", c -> c.notNull())
			.fromMap(am.ik.yavi.arguments.Arguments::of)
			.validate(input("name", "Taro", "age", 20))) {
			case Validated.Valid(Arguments2(var name, var age)) -> name + " is " + age;
			case Validated.Invalid(var errors) -> errors.toString();
		};
		assertThat(described).isEqualTo("Taro is 20");
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
		final ObjectInputValidator<Optional<List<Shape>>> drawing = Yavi.schema()
			._list("shapes", shapeValidator)
			.fromMap(Optional::ofNullable);
		assertThat(model.validate(drawing, input("shapes", List.of(map("type", "circle"), "square"))).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("shapes[0].radius", "object.required"), tuple("shapes[1]", "object.type"));
		assertThat(JsonSchema.toJson(drawing.description())).isEqualTo("""
				{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{\
				"shapes":{"type":"array","items":{"anyOf":[\
				{"type":"object","properties":{"type":{"type":"string","const":"circle"},\
				"radius":{"type":"integer","exclusiveMinimum":0}},"required":["type","radius"],\
				"additionalProperties":false},\
				{"type":"object","properties":{"type":{"type":["string","null"]},\
				"side":{"type":"integer"}},"required":["side"]}]}}},"required":["shapes"]}""");
	}

	record Comment(String body, List<Comment> replies) {
	}

	sealed interface Expr {

	}

	record Num(Integer value) implements Expr {
	}

	record Add(Expr left, Expr right) implements Expr {
	}

	record Formula(String name, @Nullable Expr expr, List<Comment> comments) {
	}

	static final ObjectInputValidator<Comment> commentValidator = ValueValidator.recursive("Comment",
			self -> Yavi.schema()
				._string("body", c -> c.notBlank())
				._list("replies", self)
				.defaultValue(List.of())
				.fromMap(Comment::new));

	static final AtomicInteger addSupplied = new AtomicInteger();

	// an alternative that is built later, and that rejects unknown properties
	static final DiscriminatedUnionValidator<Expr> exprValidator = DiscriminatedUnionValidator.<Expr>of("op")
		.when("num", Yavi.schema()._integer("value", c -> c.notNull()).fromMap(Num::new))
		.when("add", ValueValidator.lazy("Add", () -> {
			addSupplied.incrementAndGet();
			return ObjectInputValidatorTest.addValidator;
		}));

	static final ObjectInputValidator<Add> addValidator = Yavi.schema()
		._object("left", exprValidator)
		._object("right", exprValidator)
		.fromMap(Add::new)
		.rejectingUnknownProperties();

	@ParameterizedTest
	@EnumSource(Model.class)
	void recursiveStructureRefersToItsValidator(Model model) {
		assertThat(model
			.validate(commentValidator,
					input("body", "a", "replies",
							List.of(map("body", "b", "replies", List.of(map("body", "c"))), map("body", "d"))))
			.value())
			.isEqualTo(new Comment("a",
					List.of(new Comment("b", List.of(new Comment("c", List.of()))), new Comment("d", List.of()))));
		assertThat(
				model
					.validate(commentValidator,
							input("body", "a", "replies",
									List.of(map("body", "b", "replies", List.of(map("body", ""), "c")),
											map("body", " "))))
					.errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("replies[0].replies[0].body", "charSequence.notBlank"),
					tuple("replies[0].replies[1]", "object.type"), tuple("replies[1].body", "charSequence.notBlank"));
		// the validator itself is referred to as the root of the schema
		assertThat(JsonSchema.toJson(commentValidator.description())).isEqualTo("""
				{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{\
				"body":{"type":"string","minLength":1},\
				"replies":{"type":["array","null"],"items":{"$ref":"#"},"default":[]}},"required":["body"]}""");

		// validators that refer to each other, one of them before it is built
		final Map<String, Object> sum = input("op", "add", "left", map("op", "num", "value", 1), "right",
				map("op", "add", "left", map("op", "num", "value", 2), "right", map("op", "num", "value", 3)));
		assertThat(model.validate(exprValidator, sum).value())
			.isEqualTo(new Add(new Num(1), new Add(new Num(2), new Num(3))));
		assertThat(model
			.validate(exprValidator, input("op", "add", "left", map("op", "add", "left", map("op", "num"), "extra", 1)))
			.errors()).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("left.left.value", "object.required"), tuple("left.right", "object.required"),
					tuple("left.extra", "object.unknownProperty"), tuple("right", "object.required"));
		// the supplier is asked once, whatever reader the validator is given
		assertThat(addSupplied.get()).isOne();

		// a structure that is referred to from elsewhere is defined once, under the name
		// of its reference
		final ObjectInputValidator<Formula> formulaValidator = Yavi.schema()
			._string("name", c -> c.notBlank())
			._object("expr", exprValidator)
			.nullable()
			._list("comments", commentValidator)
			.defaultValue(List.of())
			.fromMap(Formula::new);
		assertThat(model
			.validate(formulaValidator, input("name", "sum", "expr", sum, "comments", List.of(map("body", "ok"))))
			.value())
			.isEqualTo(new Formula("sum", new Add(new Num(1), new Add(new Num(2), new Num(3))),
					List.of(new Comment("ok", List.of()))));
		assertThat(JsonSchema.toJson(formulaValidator.description())).isEqualTo("""
				{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{\
				"name":{"type":"string","minLength":1},\
				"expr":{"anyOf":[{"type":"object","properties":{"op":{"type":"string","const":"num"},\
				"value":{"type":"integer"}},"required":["op","value"]},{"$ref":"#/$defs/Add"},{"type":"null"}]},\
				"comments":{"type":["array","null"],"items":{"$ref":"#/$defs/Comment"},"default":[]}},\
				"required":["name"],"$defs":{\
				"Add":{"type":"object","properties":{"op":{"type":"string","const":"add"},\
				"left":{"anyOf":[{"type":"object","properties":{"op":{"type":"string","const":"num"},\
				"value":{"type":"integer"}},"required":["op","value"]},{"$ref":"#/$defs/Add"}]},\
				"right":{"anyOf":[{"type":"object","properties":{"op":{"type":"string","const":"num"},\
				"value":{"type":"integer"}},"required":["op","value"]},{"$ref":"#/$defs/Add"}]}},\
				"required":["op","left","right"],"additionalProperties":false},\
				"Comment":{"type":"object","properties":{"body":{"type":"string","minLength":1},\
				"replies":{"type":["array","null"],"items":{"$ref":"#/$defs/Comment"},"default":[]}},\
				"required":["body"]}}}""");
	}

	@Test
	void lazyValidatorIsResolvedOnFirstUse() {
		final List<ValueValidator<Map<String, Object>, Comment>> built = new ArrayList<>();
		final ValueValidator<Map<String, Object>, Comment> lazy = ValueValidator
			.lazy(() -> built.isEmpty() ? null : built.get(0));
		// it is built into other validators and given a reader before it can be resolved
		final ValueValidator<JsonNode, Optional<Comment>> reading = lazy.optional()
			.readingWith(JsonNodeReader.INSTANCE);
		assertThat(lazy.description()).isInstanceOf(ValueDescription.Reference.class)
			.isSameAs(lazy.readingWith(JsonNodeReader.INSTANCE).description())
			.hasToString("Reference[name=]");
		assertThatThrownBy(() -> reading.validate(jsonMapper.valueToTree(Map.of("body", "a"))))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("is not built yet");
		assertThatThrownBy(() -> JsonSchema.of(lazy.description())).isInstanceOf(IllegalStateException.class);
		built.add(commentValidator);
		assertThat(reading.validated(jsonMapper.valueToTree(Map.of("body", "a"))))
			.contains(new Comment("a", List.of()));
		// the reference is the schema of what it refers to
		assertThat(JsonSchema.of(lazy.description())).isEqualTo(JsonSchema.of(commentValidator.description()));
		assertThat(lazy.description().acceptsNull()).isFalse();
		// a validator that reads with a reader refers to itself with the same reader
		final ValueValidator<JsonNode, Comment> self = lazy.readingWith(JsonNodeReader.INSTANCE);
		assertThat(lazy.readingWith(JsonNodeReader.INSTANCE)).isSameAs(self);
		// a reference that leads only to itself describes nothing
		final List<ValueValidator<String, String>> loop = new ArrayList<>();
		loop.add(ValueValidator.lazy(() -> loop.get(0)));
		// a transform of the input is one of the validator referred to, which keeps its
		// input type
		final ValueValidator<String, String> name = ValueValidator
			.lazy(() -> StringValidatorBuilder.of("name", c -> c.notBlank()).build());
		assertThat(name.preprocess(String::trim).untyped().validated(" a ")).isEqualTo("a");
		assertThat(JsonSchema.toJson(loop.get(0).description()))
			.isEqualTo("{\"$schema\":\"https://json-schema.org/draft/2020-12/schema\"}");
	}

	record UserName(String first, String last) {
	}

	record Department(String name, String code) {
	}

	record Employee(Integer id, UserName name, @Nullable Department department, Map<String, Integer> scores) {
	}

	@ParameterizedTest
	@EnumSource(Model.class)
	void embeddedValidatorsAndTheRestReadTheSameMap(Model model) {
		// an embedded validator that rejects unknown properties sees only its own
		final ObjectInputValidator<UserName> userNameValidator = Yavi.schema()
			._string("first_name", c -> c.notBlank())
			._string("last_name", c -> c.notBlank())
			.fromMap(UserName::new)
			.rejectingUnknownProperties();
		final ObjectInputValidator<Department> departmentValidator = Yavi.schema()
			._string("dept_name", c -> c.notBlank())
			._string("dept_code", c -> c.notBlank())
			.fromMap(Department::new);
		final ObjectInputValidator<Employee> employeeValidator = Yavi.schema()
			._integer("id", c -> c.notNull())
			._embedded(userNameValidator)
			._embedded(departmentValidator)
			.nullable()
			._rest(IntegerValidatorBuilder.of("", c -> c.notNull().positive()).build())
			.fromMap(Employee::new);
		assertThat(model
			.validate(employeeValidator,
					input("id", 1, "first_name", "Taro", "last_name", "Yamada", "dept_name", "Sales", "dept_code", "S",
							"math", 90))
			.value()).isEqualTo(
					new Employee(1, new UserName("Taro", "Yamada"), new Department("Sales", "S"), Map.of("math", 90)));
		// a nullable embedded validator is given null when none of its properties has a
		// value
		assertThat(model
			.validate(employeeValidator, input("id", 1, "first_name", "Taro", "last_name", "Yamada", "dept_name", null))
			.value()).isEqualTo(new Employee(1, new UserName("Taro", "Yamada"), null, Map.of()));
		// the violations of an embedded validator are reported as they are, and those of
		// the rest under the key
		assertThat(model
			.validate(employeeValidator, input("id", 1, "first_name", "", "dept_name", "Sales", "math", 0, "art", "A"))
			.errors()).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("first_name", "charSequence.notBlank"), tuple("last_name", "object.required"),
					tuple("dept_code", "object.required"), tuple("math", "numeric.positive"),
					tuple("art", "object.type"));
		assertThat(JsonSchema.toJson(employeeValidator.description())).isEqualTo("""
				{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{\
				"id":{"type":"integer"},\
				"first_name":{"type":"string","minLength":1},"last_name":{"type":"string","minLength":1},\
				"dept_name":{"type":["string","null"],"minLength":1},\
				"dept_code":{"type":["string","null"],"minLength":1}},\
				"required":["id","first_name","last_name"],\
				"additionalProperties":{"type":"integer","exclusiveMinimum":0}}""");
		// the properties of an embedded union are those of its alternatives: the selected
		// one reports those of the others if it rejects unknown properties
		final ObjectInputValidator<List<Object>> eventValidator = Yavi.schema()
			._string("id", c -> c.notBlank())
			._embedded(shapeValidator)
			.fromMap((id, shape) -> List.of(id, shape))
			.rejectingUnknownProperties();
		assertThat(model.validate(eventValidator, input("id", "e1", "type", "circle", "radius", 2)).value())
			.containsExactly("e1", new Circle(2));
		assertThat(model
			.validate(eventValidator, input("id", "e1", "type", "circle", "radius", 2, "side", 3, "color", "red"))
			.errors()).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("side", "object.unknownProperty"), tuple("color", "object.unknownProperty"));
		assertThat(JsonSchema.toJson(eventValidator.description())).isEqualTo("""
				{"$schema":"https://json-schema.org/draft/2020-12/schema","anyOf":[\
				{"type":"object","properties":{"id":{"type":"string","minLength":1},\
				"type":{"type":"string","const":"circle"},"radius":{"type":"integer","exclusiveMinimum":0}},\
				"required":["id","type","radius"],"additionalProperties":false},\
				{"type":"object","properties":{"id":{"type":"string","minLength":1},\
				"type":{"type":["string","null"]},"side":{"type":"integer"}},\
				"required":["id","side"],"additionalProperties":false}]}""");
		// the rest is taken as it is without a validator, and arguments that are not read
		// from a key are still arguments of apply
		assertThat(Yavi.schema()
			._string("id")
			._rest()
			.fromMap((id, rest) -> Optional.ofNullable(rest))
			.validated(input("id", "e1", "tags", List.of("a")))).contains(Map.of("tags", List.of("a")));
		assertThat(Yavi.schema()
			._embedded(departmentValidator)
			._rest()
			.apply((department, rest) -> Optional.ofNullable(rest))
			.validated(input("dept_name", "Sales", "dept_code", "S"), input("math", 90))).contains(Map.of("math", 90));
		// any other validator is embedded as it is and reads the whole map, so the
		// properties that are read are unknown
		final ValueValidator<Map<String, Object>, Integer> size = (map, locale, context) -> Validated
			.successWith(Objects.requireNonNull(map).size());
		final ObjectInputValidator<List<Object>> sized = Yavi.schema()
			._string("id")
			._embedded(size)
			.fromMap((id, count) -> List.of(id, count));
		assertThat(sized.validated(input("id", "e1", "color", "red"))).containsExactly("e1", 2);
		assertThat(
				sized.description())
			.isEqualTo(new ValueDescription.Properties(
					List.of(new ValueDescription.Property("id",
							new ValueDescription.Value("id", String.class,
									List.of(new ConstraintDescription("object.notNull", List.of(), true))))),
					new ValueDescription.Unknown()));
		assertThatThrownBy(sized::rejectingUnknownProperties).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("unknown properties cannot be rejected");
		assertThatThrownBy(() -> Yavi.schema()._embedded(sized)._rest().fromMap((a, b) -> ""))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("rest of the properties cannot be told");
		assertThatThrownBy(() -> Yavi.schema()._rest().fromMap(Optional::ofNullable).rejectingUnknownProperties())
			.isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> Yavi.schema()._rest()._rest().fromMap((a, b) -> ""))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(
				() -> Yavi.schema()._string("dept_code")._embedded(departmentValidator).fromMap((a, b) -> ""))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("The property 'dept_code' is defined twice");
	}

	record Page(Integer size, Role role, Address address) {
	}

	record Item(String name, Integer quantity) {
	}

	record Order(String customer, List<Item> items, @Nullable List<Integer> coupons, Map<String, Integer> stock,
			@Nullable Object note) {
	}

	@Test
	void parametersAreReadAsTheTreeTheyStandFor() {
		final ValueValidator<Map<String, ?>, Order> validator = Yavi.schema()
			._string("customer", c -> c.notBlank())
			._list("items",
					Yavi.schema()
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
			.nullable()
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
		assertThat(Yavi.schema()
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
		final ValueValidator<ParameterTree, Optional<List<@Nullable Integer>>> coupons = Yavi.schema()
			._list("coupons", IntegerValidatorBuilder.of("", c -> c.positive()).build().nullable())
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
				coerced(Integer.class, "1.5", null), coerced(Integer.class, "0x1", null),
				coerced(Integer.class, "2147483648", null), coerced(Long.class, "2147483648", 2147483648L),
				coerced(Short.class, "-1", (short) -1), coerced(Byte.class, "128", null),
				coerced(BigInteger.class, "1e3", new BigInteger("1000")),
				coerced(BigInteger.class, "1e1000000000", null), coerced(BigDecimal.class, "1,000", null),
				coerced(BigDecimal.class, "0.10", new BigDecimal("0.10")), coerced(Double.class, "0.1", 0.1),
				coerced(Double.class, "NaN", null), coerced(Double.class, "1e400", null),
				coerced(Float.class, "0.1", 0.1f), coerced(Boolean.class, "true", true),
				coerced(Boolean.class, "false", false), coerced(Boolean.class, "maybe", null),
				// surrounding white space and the words of a boolean are taken only by
				// the rules that say so
				lenient(Integer.class, " 1", 1), lenient(Integer.class, "\t1 0\n", null),
				lenient(BigDecimal.class, "\u30001.5 ", new BigDecimal("1.5")), lenient(Boolean.class, "TRUE", true),
				lenient(Boolean.class, "on", true), lenient(Boolean.class, " Yes ", true),
				lenient(Boolean.class, "1", true), lenient(Boolean.class, "OFF", false),
				lenient(Boolean.class, "no", false), lenient(Boolean.class, "0", false),
				lenient(Boolean.class, "2", null), lenient(Role.class, " USER ", Role.USER),
				lenient(Year.class, " 2026 ", Year.of(2026)),
				// a string stays a string
				Arguments.of(String.class, " 1 ", " 1 ", " 1 ", " 1 "),
				Arguments.of(Object.class, " 1 ", " 1 ", " 1 ", " 1 "));
	}

	/**
	 * An input that is read in the same way whether strings are coerced or not.
	 */
	static Arguments exact(Class<?> type, Object input, @Nullable Object expected) {
		return Arguments.of(type, input, expected, expected, expected);
	}

	/**
	 * A string that represents the type only when strings are coerced.
	 */
	static Arguments coerced(Class<?> type, String input, @Nullable Object expected) {
		return Arguments.of(type, input, null, expected, expected);
	}

	/**
	 * A string that represents the type only when strings are trimmed and the words of a
	 * boolean are taken.
	 */
	static Arguments lenient(Class<?> type, String input, @Nullable Object expected) {
		return Arguments.of(type, input, null, null, expected);
	}

	@ParameterizedTest
	@MethodSource("untypedInputs")
	<T> void untypedInputMustRepresentTheType(Class<T> type, Object input, @Nullable Object expected,
			@Nullable Object coerced, @Nullable Object lenient) {
		final ValueValidator<Object, @Nullable T> validator = ValueValidator.<T>passThrough().untyped(type);
		final Map<ValueValidator<Object, @Nullable T>, @Nullable Object> expectations = new LinkedHashMap<>();
		expectations.put(validator, expected);
		expectations.put(validator.readingWith(InputReader.ofJdkTypes().coercingStrings()), coerced);
		// the rules of a reader that coerces are replaced
		expectations.put(validator.readingWith(InputReader.ofJdkTypes()
			.coercingStrings()
			.coercingStrings(StringCoercion.strict().trimming().booleanWords())), lenient);
		expectations.forEach((untyped, value) -> {
			final var validated = untyped.validate(input);
			if (value == null) {
				// the violated value is the input as it was given
				assertThat(validated.errors())
					.extracting(ConstraintViolation::messageKey, ConstraintViolation::violatedValue)
					.containsExactly(tuple(type.isEnum() ? "object.oneOf" : "object.type", input));
			}
			else {
				assertThat(validated.value()).isEqualTo(value);
			}
		});
	}

	record Signup(@Nullable String name, Integer age, Boolean subscribed, Role role) {
	}

	enum Unit {

		KG, Kg, G

	}

	@ParameterizedTest
	@EnumSource(Model.class)
	void lenientReadingsAreOptions(Model model) {
		// the name of a constant is read in any case by an argument that says so,
		// whatever the reader is
		final EnumValidator<Role, Role> role = Yavi._enum("role", Role.class, c -> c.notNull()).ignoringCase();
		final ObjectInputValidator<Signup> validator = Yavi.schema()
			._string("name")
			.nullable()
			._integer("age")
			.defaultValue(20)
			._boolean("subscribed")
			.defaultValue(false)
			._enum(role)
			.fromMap(Signup::new);
		assertThat(
				model.validate(validator, input("name", "a", "age", 30, "subscribed", true, "role", "Admin")).value())
			.isEqualTo(new Signup("a", 30, true, Role.ADMIN));
		assertThat(model.validate(validator, input("role", "root")).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey, ConstraintViolation::violatedValue)
			.containsExactly(tuple("role", "object.oneOf", "root"));
		assertThat(model.validate(validator, input()).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("role", "object.required"));
		// the option is kept by the validators derived from the validator, and the
		// description holds the names as they are declared
		assertThat(role.map(Role::ordinal).preprocess(r -> Role.USER).lazy().untyped().validated("admin").get())
			.isEqualTo(1);
		assertThat(role.optional().untyped().validated("user")).contains(Role.USER);
		assertThat(role.description()).isEqualTo(Yavi._enum("role", Role.class, c -> c.notNull()).description());
		assertThat(JsonSchema.toJson(validator.description())).contains("\"enum\":[\"ADMIN\",\"USER\"]");
		// a name that differs from several constants only by its case is none of them
		final ValueValidator<Object, Unit> unit = Yavi._enum(Unit.class).ignoringCase().untyped();
		assertThat(unit.validated("Kg")).isEqualTo(Unit.Kg);
		assertThat(unit.validated("g")).isEqualTo(Unit.G);
		assertThat(unit.validate("kG").errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.oneOf");
		assertThatThrownBy(() -> EnumValidatorBuilder.<Role>of("role", c -> c).build().ignoringCase())
			.isInstanceOf(IllegalStateException.class);
		// a form: the fields are trimmed, a blank one is not given, and a checkbox is
		// "on" or absent
		final StringCoercion form = StringCoercion.strict().trimming().booleanWords();
		final ValueValidator<Map<String, String>, Signup> ofForm = validator.readingParameters(form);
		assertThat(ofForm.validated(Map.of("name", " a ", "age", " 30 ", "subscribed", "on", "role", " admin ")))
			.isEqualTo(new Signup(" a ", 30, true, Role.ADMIN));
		assertThat(ofForm.validated(Map.of("name", " ", "age", " \t", "role", "USER")))
			.isEqualTo(new Signup(null, 20, false, Role.USER));
		assertThat(validator.readingParameters()
			.validate(Map.of("age", " 30 ", "subscribed", "on", "role", "USER"))
			.errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey, ConstraintViolation::violatedValue)
			.containsExactly(tuple("age", "object.type", " 30 "), tuple("subscribed", "object.type", "on"));
		// the words of a boolean are replaced by those of the application
		final InputReader<ParameterTree> words = ParameterTree
			.reader(StringCoercion.strict().booleanWords(List.of("Y", "はい"), List.of("N")));
		final ValueValidator<ParameterTree, Signup> ofWords = validator.readingWith(words);
		assertThat(ofWords.validated(ParameterTree.of(Map.of("subscribed", "y", "role", "USER"))).subscribed())
			.isTrue();
		assertThat(
				ofWords.validate(ParameterTree.of(Map.of("subscribed", "true", "age", " 1", "role", "USER"))).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("age", "object.type"), tuple("subscribed", "object.type"));
		assertThatThrownBy(() -> StringCoercion.strict().booleanWords(List.of("on"), List.of("ON")))
			.isInstanceOf(IllegalArgumentException.class);
	}

	record ProfilePatch(Presence<String> nickname, Presence<String> bio, Presence<Address> address,
			Presence<Integer> age) {
	}

	// the arguments of a function are nullable, and a Presence never is
	@SuppressWarnings("NullAway")
	@ParameterizedTest
	@EnumSource(Model.class)
	void absentKeyIsToldFromNull(Model model) {
		final var arguments = Yavi.schema()
			._string("nickname", c -> c.lessThanOrEqual(5))
			.presence()
			._string("bio")
			.nullable()
			.presence()
			._object("address", addressValidator)
			.nullable()
			.presence()
			._integer("age", c -> c.greaterThanOrEqual(0))
			.defaultValue(20)
			.presence();
		final ObjectInputValidator<ProfilePatch> validator = arguments.fromMap(ProfilePatch::new)
			.rejectingUnknownProperties();
		// an absent key is not validated, so it is not required
		assertThat(model.validate(validator, input()).value())
			.isEqualTo(new ProfilePatch(Presence.absent(), Presence.absent(), Presence.absent(), Presence.absent()));
		// a key that is present is validated as it was before presence(), null included
		assertThat(
				model.validate(validator, input("nickname", "yavi", "bio", null, "address", null, "age", null)).value())
			.isEqualTo(new ProfilePatch(Presence.of("yavi"), Presence.presentNull(), Presence.presentNull(),
					Presence.of(20)));
		assertThat(model
			.validate(validator, input("nickname", null, "bio", "", "address", map("street", "1-2-3"), "age", -1))
			.errors()).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("nickname", "object.notNull"), tuple("address.city", "object.required"),
					tuple("age", "numeric.greaterThanOrEqual"));
		// a partial update is merged into the current values
		final ProfilePatch patch = model.validate(validator, input("bio", null, "age", 30)).value();
		assertThat(patch.nickname().orElse("current")).isEqualTo("current");
		assertThat(patch.bio().orElse("current")).isNull();
		assertThat(patch.age().map(age -> age + 1)).isEqualTo(Presence.of(31));
		final List<@Nullable Object> given = new ArrayList<>();
		patch.nickname().ifPresent(given::add);
		patch.bio().ifPresent(given::add);
		patch.age().ifPresent(given::add);
		assertThat(given).containsExactly(null, 30);
		// a parameter with an empty value is present and null
		assertThat(validator.readingParameters().validated(Map.of("bio", "")).bio()).isEqualTo(Presence.presentNull());
		// the property is not required, and nullable only if it accepts null
		assertThat(JsonSchema.toJson(validator.description())).isEqualTo("""
				{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{\
				"nickname":{"type":"string","maxLength":5},\
				"bio":{"type":["string","null"]},\
				"address":{"type":["object","null"],"properties":{"street":{"type":"string","minLength":1},\
				"city":{"type":"string","minLength":1}},"required":["street","city"],"additionalProperties":false},\
				"age":{"type":["integer","null"],"minimum":0,"default":20}},\
				"additionalProperties":false}""");
		// typed arguments are given the three states
		final var typed = arguments.apply(ProfilePatch::new);
		assertThat(typed.validated(Presence.of("yavi"), Presence.presentNull(), null, Presence.absent())).isEqualTo(
				new ProfilePatch(Presence.of("yavi"), Presence.presentNull(), Presence.absent(), Presence.absent()));
		assertThat(
				typed.validate(Presence.presentNull(), Presence.absent(), Presence.absent(), Presence.of(-1)).errors())
			.extracting(ConstraintViolation::name)
			.containsExactly("nickname", "age");
		// a part defined without the builders
		final ObjectInputValidator<Object> bio = ObjectInputValidator.of(
				List.of(new ObjectInputValidator.PresenceProperty<>("bio",
						StringValidatorBuilder.of("bio", c -> c.notBlank()).build().liftPresence().untyped())),
				values -> values.get(0));
		assertThat(model.validate(bio, input()).value()).isEqualTo(Presence.absent());
		assertThat(model.validate(bio, input("bio", null)).errors()).extracting(ConstraintViolation::name)
			.containsExactly("bio");
	}

	@Test
	void inputTypeMustBeKnownToReadFromAMap() {
		// a value validator that takes an Object decides itself what it accepts
		final ValueValidator<Object, Integer> coerced = (value, locale, context) -> Yavi._integer(c -> c)
			.untyped()
			.validate(value instanceof String string ? Integer.valueOf(string) : value, locale, context);
		// alternatives are untyped one by one
		final ValueValidator<Object, Object> id = ValueValidator.anyOf(
				StringValidatorBuilder.of("", c -> c.notBlank()).build().untyped(),
				IntegerValidatorBuilder.of("", c -> c.positive()).build().untyped());
		final var arguments = Yavi.schema()
			._object("count", coerced.untyped(Object.class))
			._object("id", id)
			._object("payload", Object.class)
			.nullable();
		final ObjectInputValidator<List<Object>> validator = arguments
			.fromMap((count, i, payload) -> Arrays.asList(count, i, payload));
		assertThat(validator.validated(input("count", "10", "id", 7, "payload", List.of(1)))).containsExactly(10, 7,
				List.of(1));
		assertThat(validator.validate(input("count", true, "id", true)).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("count", "object.type"), tuple("id", "object.anyOf"), tuple("id", "object.type"),
					tuple("id", "object.type"));
		assertThatThrownBy(() -> Yavi.schema().<Role>_enum("role").fromMap(Optional::ofNullable))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("The input type of the argument 'role' is unknown");
		assertThatThrownBy(() -> Yavi.schema()._object("count", coerced).fromMap(Optional::ofNullable))
			.isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(
				() -> Yavi.schema()._integer(coerced.untyped(Object.class)::validate).fromMap(Optional::ofNullable))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("The argument at position 1 has no name");
		assertThatThrownBy(() -> Yavi.schema()._string("a")._integer("a").fromMap((a, b) -> ""))
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
		final ObjectInputValidator<Object> lambda = ObjectInputValidator
			.of(List.of(new ObjectInputValidator.Property<>("count", coerced)), values -> values.get(0));
		assertThat(lambda.untyped().validated(map("count", "10"))).isEqualTo(10);
		// the node is read as it is under another reader, coerced or not: its strings are
		// coerced, and its null node and an empty string are both null
		final ValueValidator<JsonNode, Square> square = Yavi.schema()
			._string("type")
			.nullable()
			._integer("side")
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
