/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import am.ik.yavi.arguments.Arguments2;
import am.ik.yavi.arguments.Arguments1Validator;
import am.ik.yavi.builder.DynamicSchema;
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
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.jsonschema.JsonSchema;
import am.ik.yavi.validator.Yavi;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
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
			<X> Validated<X> validate(ValueValidator<Map<String, Object>, X> validator,
					@Nullable Map<String, Object> input) {
				assumeTrue(hasJdkJson(), "jdk.incubator.json is a module of JDK 28 or later");
				return validator.readingWith(jdkJsonReader())
					.validate(parseJdkJson(jsonMapper.writeValueAsString(input)));
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
				// a map that is null has no parameters to stand for it
				return validator.readingWith(ParameterTree.reader())
					.validate(ParameterTree.of(input == null ? Collections.singletonMap("", null)
							: ParameterTree.parametersOf((Map<String, ?>) Objects.requireNonNull(strings(input)))));
			}
		};

		abstract <X> Validated<X> validate(ValueValidator<Map<String, Object>, X> validator,
				@Nullable Map<String, Object> input);

	}

	static final JsonMapper jsonMapper = new JsonMapper();

	static boolean hasJdkJson() {
		return Runtime.version().feature() >= 28;
	}

	@SuppressWarnings("unchecked")
	static InputReader<Object> jdkJsonReader() {
		try {
			return (InputReader<Object>) Class.forName("am.ik.yavi.input.JdkJsonReader")
				.getDeclaredField("INSTANCE")
				.get(null);
		}
		catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	static Object parseJdkJson(String json) {
		try {
			return Class.forName("jdk.incubator.json.Json").getMethod("parse", String.class).invoke(null, json);
		}
		catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

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

	static final ObjectInputValidator<User> userValidator = Yavi.schema()
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
		// a rule on the created object keeps the validator of the properties
		.constraintOnTarget(User::hasTagIfAdmin, "tags",
				ViolationMessage.of("user.adminTags", "An admin must have a tag"))
		.rejectingUnknownProperties();

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
			// an empty string is a null node wherever it is, which a property requires
			// rather than rejects as null, and a string that does not stand for the type
			// is reported like any other mismatch
			final Validated<User> coerced = model.validate(userValidator,
					input("email", "", "age", "", "role", "", "birthday", "", "address", map("street", "1-2-3"), "tags",
							List.of("a", ""), "scores", map("math", "-1", "art", "1.5", "music", " 1"), "nickname", "",
							"rate", "high", "name", ""));
			assertThat(coerced.errors())
				.extracting(ConstraintViolation::name, ConstraintViolation::messageKey,
						violation -> violation.violatedValue())
				.containsExactly(tuple("email", "charSequence.notBlank", null), tuple("age", "object.required", null),
						tuple("role", "object.required", null), tuple("address.city", "object.required", null),
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
		// every form of a map of parameters is read by one validator, which any
		// combinator follows without a type witness
		final ValueValidator<Map<String, ?>, Shape> ofParameters = shapeValidator.readingParameters()
			.formattingWith((messageKey, defaultMessageFormat, args, locale) -> messageKey);
		assertThat(ofParameters.validated(Map.of("type", "circle", "radius", "2"))).isEqualTo(new Circle(2));
		assertThat(ofParameters.validated(Map.of("type", List.of("square"), "side", new String[] { "3" })))
			.isEqualTo(new Square("square", 3));
		assertThat(ofParameters.validate(Map.of("type", List.of("circle"))).errors())
			.extracting(ConstraintViolation::message)
			.containsExactly("object.required");
		// a rule on the created object keeps the union, whatever alternative is selected
		assertThat(shapeValidator
			.constraintOnTarget(shape -> shape instanceof Circle, "type",
					ViolationMessage.of("shape.round", "\"{0}\" must be round"))
			.readingParameters()
			.validate(Map.of("type", "square", "side", "3"))
			.errors()).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("type", "shape.round"));
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
			// null is given to a validator that accepts it
			._object("expr", exprValidator.nullable())
			._list("comments", commentValidator)
			.defaultValue(List.of())
			.fromMap(Formula::new);
		assertThat(model.validate(formulaValidator, input("name", "sum")).value())
			.isEqualTo(new Formula("sum", null, List.of()));
		assertThat(model.validate(formulaValidator, input("name", "sum", "expr", map("op", "num"))).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("expr.value", "object.required"));
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
		final ObjectInputValidator.IgnoringUnknown<List<Object>> sized = Yavi.schema()
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
		// a name is defined once, whatever the validator is created with
		assertThatThrownBy(() -> Yavi.schema()._string("a")._integer("a").apply((a, b) -> ""))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("The property 'a' is defined twice");
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
		final Map<String, Object> invalid = input(
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
				"extra.b[0]", "1");
		final Validated<Order> validated = validator.validate(invalid);
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
		final ValueValidator<Map<String, ?>, String> mixedValidator = Yavi.schema()
			._string("s")
			._list("l", StringValidatorBuilder.of("", c -> c.notBlank()).build())
			._object("o", addressValidator)
			._object("note", Object.class)
			.fromMap((s, l, o, note) -> "")
			.readingParameters();
		final Map<String, Object> mixed = input("s", "x", "s.b", "y", "l", "x", "l[0]", "y", "o", "x", "o.street", "y",
				"note", "x", "note.lang", "en");
		assertThat(mixedValidator.validate(mixed).errors())
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
		// the parameters are written back as the validator reads them: the described
		// properties in their order and in the described shape, with what was sent
		final Map<String, Object> written = ParameterTree.of(parameters).toMap(validator.description());
		assertThat(written).containsExactly(entry("customer", "a"),
				entry("items", List.of(Map.of("name", "ink", "quantity", "2"), Map.of("name", "pen", "quantity", ""))),
				entry("coupons", List.of("7")), entry("stock", Map.of("1", "3", "a.b", "0")),
				entry("note", List.of("x", "y")));
		// under the keys of the violations, which are parameters that stand for the same
		// tree; an empty list or map has no key
		assertThat(ParameterTree.of(parameters).toParameters(validator.description())).containsExactly(
				entry("customer", "a"), entry("items[0].name", "ink"), entry("items[0].quantity", "2"),
				entry("items[1].name", "pen"), entry("items[1].quantity", ""), entry("coupons", List.of("7")),
				entry("stock.1", "3"), entry("stock[a.b]", "0"), entry("note", List.of("x", "y")));
		assertThat(validator.validated(ParameterTree.parametersOf(written))).isEqualTo(validator.validated(parameters));
		assertThat(ParameterTree.parametersOf(map("a", List.of(), "b", Map.of(), "c", List.of(List.of("x")))))
			.isEqualTo(Map.of("c[0]", List.of("x")));
		// the tree is the caller's, for example to add what a page needs
		written.put("id", 1);
		((List<Object>) Objects.requireNonNull(written.get("note"))).add("z");
		assertThat(ParameterTree.of(parameters).toMap(validator.description())).doesNotContainKey("id")
			.containsEntry("note", List.of("x", "y"));
		// a value that was not sent or has not the described shape is what an empty
		// form sends, so the entries are the same for every request; an unknown key has
		// none, nothing is coerced, and a skipped index keeps the others at theirs
		assertThat(ParameterTree.of(invalid).toMap(validator.description()))
			.isEqualTo(Map.of("customer", "", "items", Arrays.asList(null, null, Map.of("name", "", "quantity", "")),
					"coupons", List.of(), "stock", Map.of("01", "x", "0", "1")));
		assertThat(ParameterTree.of(invalid).toParameters(validator.description())).isEqualTo(
				Map.of("customer", "", "items[2].name", "", "items[2].quantity", "", "stock.01", "x", "stock.0", "1"));
		// a value of type Object is what the tree holds
		assertThat(ParameterTree.of(mixed).toMap(mixedValidator.description())).isEqualTo(Map.of("s", "", "l",
				List.of(), "o", Map.of("street", "", "city", ""), "note", Map.of("", "x", "lang", "en")));
		assertThat(ParameterTree.of(mixed).toParameters(mixedValidator.description()))
			.isEqualTo(Map.of("s", "", "o.street", "", "o.city", "", "note", "x", "note.lang", "en"));
		// the properties of alternatives are those of all of them, and the rest is
		// written if it is read
		final ValueDescription shapes = Yavi.schema()
			._embedded(shapeValidator)
			._rest()
			.fromMap((shape, rest) -> "")
			.description();
		assertThat(ParameterTree.of(input("radius", "x", "side", List.of("1", "2"), "color", "red", "extra.a", "1"))
			.toMap(shapes))
			.isEqualTo(Map.of("type", "", "radius", "x", "side", "", "color", "red", "extra", Map.of("a", "1")));
		assertThat(ParameterTree.of(input("type", "square", "side", "1", "color", "red"))
			.toMap(shapeValidator.description())).isEqualTo(Map.of("type", "square", "radius", "", "side", "1"));
		// a checkbox that is not checked, a property that may be absent and a structure
		// that contains itself have no entry unless they are sent
		final ValueDescription unsent = Yavi.schema()
			._boolean("flag")
			._string("patch")
			.presence()
			._object("expr", exprValidator)
			.fromMap((flag, patch, expr) -> "")
			.description();
		assertThat(ParameterTree.of(Map.of()).toMap(unsent)).isEqualTo(Map.of("expr", Map.of("op", "", "value", "")));
		assertThat(ParameterTree.of(Map.of("flag", "on", "patch", "")).toMap(unsent))
			.isEqualTo(Map.of("flag", "on", "patch", "", "expr", Map.of("op", "", "value", "")));
		assertThat(ParameterTree.of(input("a", "1")).toMap(Yavi._string().description())).isEmpty();
		// a list is also read from one text whose elements are separated: they are text
		// that stands for a value and are located as the values of a repeated key
		final ObjectInputValidator<List<Object>> article = Yavi.schema()
			._list("tags", Yavi._string(c -> c.notBlank().lessThanOrEqual(5)), c -> c.unique())
			.splitBy(",")
			._list("ids", Yavi._integer(c -> c.positive()))
			.splitBy(Pattern.compile("\\s*;\\s*|\\R"))
			.defaultValue(List.of())
			.fromMap((tags, ids) -> List.of(tags, ids));
		final ValueValidator<Map<String, ?>, List<Object>> split = article.readingParameters();
		assertThat(split.validated(Map.of("tags", "java,yavi", "ids", "1; 2\n3\n")))
			.containsExactly(List.of("java", "yavi"), List.of(1, 2, 3));
		// every text is split, a separator at the end starts no element, and a tree
		// that is not text is split too
		assertThat(split.validated(Map.of("tags", List.of("a,b,", "c"), "ids[0]", "1;2", "ids[1]", "3")))
			.containsExactly(List.of("a", "b", "c"), List.of(1, 2, 3));
		assertThat(article.validated(input("tags", "a", "ids", List.of(1, "2;3")))).containsExactly(List.of("a"),
				List.of(1, 2, 3));
		// one text is written back as it was sent, and so are several values
		assertThat(ParameterTree.of(Map.of("tags", "java,yavi", "ids", "1; 2\n3\n")).toMap(split.description()))
			.isEqualTo(Map.of("tags", "java,yavi", "ids", "1; 2\n3\n"));
		assertThat(ParameterTree.of(Map.of("tags", List.of("a,b,", "c"), "ids[0]", "1;2", "ids[1]", "3"))
			.toMap(split.description())).isEqualTo(Map.of("tags", List.of("a,b,", "c"), "ids", List.of("1;2", "3")));
		assertThat(ParameterTree.of(Map.of()).toMap(split.description())).isEqualTo(Map.of("tags", "", "ids", ""));
		// nothing is dropped or trimmed: an element without a value is reported
		assertThat(split.validate(Map.of("tags", "java,,toolong, x", "ids", "1;x;-3;")).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("tags[1]", "charSequence.notBlank"), tuple("tags[2]", "container.lessThanOrEqual"),
					tuple("ids[1]", "object.type"), tuple("ids[2]", "numeric.positive"));
		assertThat(List.of(split.validate(Map.of("tags", "a,a")).errors(), split.validate(Map.of("tags", "")).errors(),
				article.validate(input("tags", 1)).errors()))
			.extracting(violations -> violations.get(0).name(), violations -> violations.get(0).messageKey())
			.containsExactly(tuple("tags", "collection.unique"), tuple("tags", "object.required"),
					tuple("tags", "object.type"));
		// a value that is neither text nor a list is not one element, and an element of
		// a list that is not text is validated as it is
		assertThat(Stream
			.of(input("tags", 1), input("tags", true), input("tags", Map.of("a", "b")),
					input("tags", "a", "ids", List.of(1, true)))
			.map(input -> article.validate(input).errors().get(0)))
			.extracting(ConstraintViolation::name, ConstraintViolation::message)
			.containsExactly(tuple("tags", "\"tags\" must be of type array or string"),
					tuple("tags", "\"tags\" must be of type array or string"),
					tuple("tags", "\"tags\" must be of type array or string"),
					tuple("ids[1]", "\"ids[1]\" must be of type integer"));
		assertThat(JsonSchema.toJson(article.description())).contains(
				"\"tags\":{\"type\":[\"array\",\"string\"],\"items\":{\"type\":\"string\",\"minLength\":1,\"maxLength\":5},\"uniqueItems\":true}",
				"\"ids\":{\"type\":[\"array\",\"string\",\"null\"],\"items\":{\"type\":\"integer\",\"exclusiveMinimum\":0},\"default\":[]}");
		assertThatThrownBy(() -> Yavi.schema()._list("tags", Yavi._string()).splitBy(""))
			.isInstanceOf(IllegalArgumentException.class);
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
				// a type with a single string form is read from it
				exact(UUID.class, "123E4567-e89b-12d3-a456-426614174000",
						UUID.fromString("123e4567-e89b-12d3-a456-426614174000")),
				exact(UUID.class, UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
						UUID.fromString("123e4567-e89b-12d3-a456-426614174000")),
				exact(UUID.class, "1-1-1-1-1", null), exact(UUID.class, 1, null),
				exact(URI.class, "https://example.com/a?b=c", URI.create("https://example.com/a?b=c")),
				exact(URI.class, "/a/b", URI.create("/a/b")), exact(URI.class, "https://example.com/a b", null),
				exact(Duration.class, "PT1H30M", Duration.ofMinutes(90)), exact(Duration.class, "1h", null),
				exact(Period.class, "P1Y2M", Period.of(1, 2, 0)), exact(Period.class, "PT1H", null),
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
				lenient(UUID.class, " 123e4567-e89b-12d3-a456-426614174000 ",
						UUID.fromString("123e4567-e89b-12d3-a456-426614174000")),
				// a string stays a string, without the white space around it for the
				// rules that trim
				Arguments.of(String.class, " 1 ", " 1 ", " 1 ", "1"),
				Arguments.of(Object.class, " 1 ", " 1 ", " 1 ", "1"));
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
		expectations.put(
				validator.readingWith(
						InputReader.ofJdkTypes().coercingStrings(StringCoercion.strict().trimming().booleanWords())),
				lenient);
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
		final Arguments1Validator<Role, Role> role = Yavi._enum("role", Role.class).ignoringCase();
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
		assertThat(role.description()).isEqualTo(Yavi._enum("role", Role.class).description());
		assertThat(JsonSchema.toJson(validator.description())).contains("\"enum\":[\"ADMIN\",\"USER\"]");
		// a name that differs from several constants only by its case is none of them
		final ValueValidator<Object, Unit> unit = Yavi._enum(Unit.class).ignoringCase().untyped();
		assertThat(unit.validated("Kg")).isEqualTo(Unit.Kg);
		assertThat(unit.validated("g")).isEqualTo(Unit.G);
		assertThat(unit.validate("kG").errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.oneOf");
		// a form: the fields are trimmed, a blank one is not given, and a checkbox is
		// "on" or absent
		final StringCoercion form = StringCoercion.strict().trimming().booleanWords();
		final ValueValidator<Map<String, ?>, Signup> ofForm = validator.readingParameters(form);
		assertThat(ofForm.validated(Map.of("name", " a ", "age", " 30 ", "subscribed", "on", "role", " admin ")))
			.isEqualTo(new Signup("a", 30, true, Role.ADMIN));
		assertThat(ofForm.validated(Map.of("name", " ", "age", " \t", "role", "USER")))
			.isEqualTo(new Signup(null, 20, false, Role.USER));
		// the constraints see the trimmed text, and so does the discriminator of a union
		assertThat(Yavi.schema()
			._string("name", c -> c.lessThanOrEqual(1))
			._list("tags", Yavi._string(c -> c.lessThanOrEqual(1)))
			.splitBy(",")
			.fromMap((name, tags) -> name + tags)
			.readingParameters(form)
			.validated(Map.of("name", " a ", "tags", "b , c"))).isEqualTo("a[b, c]");
		assertThat(shapeValidator.readingParameters(form).validated(Map.of("type", " circle ", "radius", "1")))
			.isEqualTo(new Circle(1));
		assertThat(shapeValidator.readingParameters(form).validate(Map.of("type", " oval ")).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::violatedValue)
			.containsExactly(tuple("type", " oval "));
		// a string whose white space is a part of it says so, and keeps saying so; a
		// blank one is still not given
		final Arguments1Validator<String, String> password = Yavi._string("password").untrimmed();
		final ValueValidator<Map<String, ?>, String> ofPassword = Yavi.schema()
			._string(Yavi._string("x", c -> c.greaterThanOrEqual(3))
				.map(String::length)
				.preprocess(String::toLowerCase)
				.named("length")
				.lazy()
				.untrimmed())
			._string(password)
			.fromMap((length, value) -> length.get() + value)
			.readingParameters(form);
		assertThat(ofPassword.validated(Map.of("length", " a ", "password", " p "))).isEqualTo("3 p ");
		assertThat(ofPassword.validate(Map.of("length", " a ", "password", " ")).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("password", "object.required"));
		assertThat(ofPassword.validate(Map.of("length", "a", "password", "p")).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("length", "container.greaterThanOrEqual"));
		assertThat(password.untyped().validated(" p ")).isEqualTo(" p ");
		assertThat(password.untyped().validate(1).errors()).extracting(ConstraintViolation::message)
			.containsExactly("\"password\" must be of type string");
		assertThat(password.description()).isEqualTo(Yavi._string("password").description());
		// a field left empty is required rather than null, unless a constraint has a
		// message of its own for it
		assertThat(ofForm.validate(Map.of("role", " ")).errors()).extracting(ConstraintViolation::message)
			.containsExactly("\"role\" is required");
		assertThat(Yavi.schema()
			._string("name", c -> c.notBlank())
			._integer("age", c -> c.notNull().message("Give \"{0}\""))
			._object("address", addressValidator)
			.fromMap((name, age, address) -> name)
			.readingParameters()
			.validate(Map.of("name", "", "age", "", "address", ""))
			.errors()).extracting(ConstraintViolation::message)
			.containsExactly("\"name\" must not be blank", "Give \"age\"", "\"address\" is required");
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

	record Account(@Nullable String deletedAt, Optional<String> bio, Integer page, @Nullable String note) {
	}

	// the arguments of a function are nullable, and a Presence never is
	@SuppressWarnings("NullAway")
	record Token(String name, Integer ttl, List<Integer> ports, List<String> roles, @Nullable String note,
			Address address) {
	}

	@ParameterizedTest
	@EnumSource(Model.class)
	void propertySaysHowItsViolationsRead(Model model) {
		final ViolationMessage ports = ViolationMessage.of("token.ports", "{0} must be port numbers");
		final var arguments = Yavi.schema()
			._string("name", c -> c.notBlank().lessThanOrEqual(3))
			.label("Token name")
			._integer("ttl", c -> c.positive())
			.label("TTL")
			.typeMessage("{0} must be a number of seconds, not {2}")
			._list("ports", Yavi._integer(c -> c.positive()))
			.typeMessage(ports)
			.label("Ports")
			._list("roles", Yavi._string(c -> c.notBlank()))
			.label("Roles")
			.required("Select at least one role")
			._string("note")
			.label("Note")
			.nullable()
			.required(ViolationMessage.of("token.note", "Send {0}, which may be empty"))
			._object("address", addressValidator)
			.label("Address");
		final ObjectInputValidator<Token> validator = arguments.fromMap(Token::new);
		final ConstraintViolations violations = model
			.validate(validator,
					input("name", "abcd", "ttl", "soon", "ports", List.of(80, "http"), "address",
							map("street", "1-2-3")))
			.errors();
		// the label is what a message calls the property and its elements; the name
		// still locates the value, and a nested property is left to its own messages
		assertThat(violations.messagesByName()).containsExactly(
				Map.entry("name",
						List.of("The size of \"Token name\" must be less than or equal to 3. The given size is 4")),
				Map.entry("ttl", List.of("TTL must be a number of seconds, not soon")),
				Map.entry("ports[1]", List.of("Ports must be port numbers")),
				Map.entry("roles", List.of("Select at least one role")),
				Map.entry("note", List.of("Send Note, which may be empty")),
				Map.entry("address.city", List.of("\"address.city\" is required")));
		// a message format keeps the key and a message replaces it; the arguments and
		// their names are kept
		assertThat(violations).extracting(ConstraintViolation::messageKey, ConstraintViolation::label)
			.containsExactly(tuple("container.lessThanOrEqual", "Token name"), tuple("object.type", "TTL"),
					tuple("token.ports", "Ports"), tuple("object.required", "Roles"), tuple("token.note", "Note"),
					tuple("object.required", "address.city"));
		assertThat(violations.details().get(2).params()).containsExactly(Map.entry("type", "integer"));
		// the first message argument is the label, as a BindingResult of Spring MVC
		// takes it, and a formatter given later formats every violation; one that has a
		// message for every key does not replace a text, which keeps its key
		assertThat(violations.get(0).args()[0]).isEqualTo("Token name");
		final MessageFormatter knowsEveryKey = (messageKey, defaultMessageFormat, args, locale) -> messageKey + ":"
				+ args[0];
		assertThat(violations.formattedWith(knowsEveryKey).messagesByName().values())
			.extracting(messages -> messages.get(0))
			.containsExactly("container.lessThanOrEqual:Token name", "TTL must be a number of seconds, not soon",
					"token.ports:Ports", "Select at least one role", "token.note:Note", "object.required:address.city");
		assertThat(violations).extracting(ConstraintViolation::localizable)
			.containsExactly(true, false, true, false, true, true);
		assertThat(model.validate(validator.formattingWith(knowsEveryKey), input("name", "abc")).errors())
			.extracting(ConstraintViolation::message)
			.containsExactly("object.required:TTL", "object.required:Ports", "Select at least one role",
					"token.note:Note", "object.required:Address");
		// the violation of a value that is of the type and the one of null are not
		// replaced, and a typed argument is labeled too
		assertThat(model
			.validate(validator,
					input("name", "abc", "ttl", 0, "ports", List.of(80), "roles", List.of("a"), "note", null, "address",
							null))
			.errors()).extracting(ConstraintViolation::message)
			.containsExactly("\"TTL\" must be positive", "\"Address\" must not be null");
		assertThat(arguments.apply(Token::new)
			.validate("", 1, List.of(0), List.of(), null, input("street", "", "city", "Tokyo"))
			.errors()).extracting(ConstraintViolation::name, ConstraintViolation::message)
			.containsExactly(tuple("name", "\"Token name\" must not be blank"),
					tuple("ports[0]", "\"Ports\" must be positive"),
					tuple("address.street", "\"address.street\" must not be blank"));
	}

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
		// properties that are known at run time are read into a map of their values: a
		// validator is used under the name of its property, and an absent property with
		// a presence has no entry
		DynamicSchema<@Nullable Object> questions = Yavi.dynamicSchema();
		for (String name : List.of("q1", "q2")) {
			questions = questions.property(name, Yavi._string("answer", c -> c.notBlank())).label(name.toUpperCase());
		}
		final DynamicSchema<@Nullable Object> survey = questions
			.property("q3", Yavi._integer(c -> c.positive()).defaultValue(0))
			.rejectingNull()
			.property("q4", Yavi._string(c -> c.notBlank()).nullable())
			.presence()
			.property("q5", Yavi._list(Yavi._integer(), c -> c.notEmpty()).splitBy(","))
			.required("Select {0}");
		final ObjectInputValidator<Map<String, @Nullable Object>> answers = survey.build().rejectingUnknownProperties();
		assertThat(model.validate(answers, input("q1", "a", "q2", "b", "q4", null, "q5", "1,2")).value())
			.containsExactly(Map.entry("q1", "a"), Map.entry("q2", "b"), Map.entry("q3", 0),
					new java.util.AbstractMap.SimpleEntry<>("q4", null), Map.entry("q5", List.of(1, 2)));
		assertThat(model.validate(answers, input("q1", "a", "q2", "b", "q3", 3, "q5", List.of(3))).value())
			.containsOnlyKeys("q1", "q2", "q3", "q5");
		assertThat(model.validate(answers, input("q1", " ", "q3", null, "q4", " ", "x", 1)).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::message)
			.containsExactly(tuple("q1", "\"Q1\" must not be blank"), tuple("q2", "\"Q2\" is required"),
					tuple("q3", "\"q3\" must not be null"), tuple("q4", "\"q4\" must not be blank"),
					tuple("q5", "Select q5"), tuple("x", "\"x\" is not a known property"));
		// the rest of the properties is validated and added to the map
		final ObjectInputValidator<Map<String, @Nullable Object>> open = survey.rest(Yavi._integer(c -> c.positive()))
			.build();
		assertThat(model.validate(open, input("q1", "a", "q2", "b", "q5", "1", "x", 1)).value()).containsEntry("x", 1)
			.containsEntry("q3", 0);
		assertThat(model.validate(open, input("q1", "a", "q2", "b", "q5", "1", "x", 0)).errors())
			.extracting(ConstraintViolation::name)
			.containsExactly("x");
		// the validator is one of a schema: a property of another, read from parameters
		assertThat(Yavi.schema()
			._object("answers", answers)
			.fromMap(values -> values)
			.readingParameters()
			.validate(Map.of("answers.q1", "a", "answers[q5]", "x,2"))
			.errors()).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("answers.q2", "object.required"), tuple("answers.q5[0]", "object.type"));
		assertThat(JsonSchema.toJson(answers.description())).endsWith("""
				"q3":{"type":"integer","exclusiveMinimum":0,"default":0},\
				"q4":{"type":["string","null"],"minLength":1},\
				"q5":{"type":["array","string"],"items":{"type":"integer"},"minItems":1}},\
				"required":["q1","q2","q5"],"additionalProperties":false}""");
		assertThatThrownBy(() -> survey.property("q1", Yavi._string())).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The property 'q1' is defined twice.");
		// without a Presence: the key is required although null is accepted, or null is
		// rejected although the key may be absent
		final var strict = Yavi.schema()
			._string("deletedAt")
			.nullable()
			.required()
			._string("bio", c -> c.notBlank())
			.optional()
			.rejectingNull()
			// a validator that accepts null is taken as it is
			._integer(Yavi._integer("page", c -> c.positive()).defaultValue(1))
			.rejectingNull()
			// null produces null, an invalid value the fallback
			._string("note", c -> c.notBlank())
			.recover("-")
			.nullable()
			.required();
		final ObjectInputValidator<Account> account = strict.fromMap(Account::new);
		assertThat(model.validate(account, input("deletedAt", null, "note", " ")).value())
			.isEqualTo(new Account(null, Optional.empty(), 1, "-"));
		assertThat(model.validate(account, input("bio", null, "page", null, "note", 1)).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("deletedAt", "object.required"), tuple("bio", "object.notNull"),
					tuple("page", "object.notNull"));
		assertThat(model.validate(account, input("deletedAt", "2026-11-13", "bio", " ", "page", 2)).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("bio", "charSequence.notBlank"), tuple("note", "object.required"));
		// a parameter with an empty value is present and null, which text cannot say: it
		// is rejected as a value that is required
		assertThat(account.readingParameters().validate(Map.of("deletedAt", "", "bio", "")).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("bio", "object.required"), tuple("note", "object.required"));
		assertThat(validator.readingParameters().validate(Map.of("nickname", "")).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("nickname", "object.required"));
		final String schema = """
				{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{\
				"deletedAt":{"type":["string","null"]},\
				"bio":{"type":"string","minLength":1},\
				"page":{"type":"integer","exclusiveMinimum":0,"default":1},\
				"note":{"type":["string","null"],"minLength":1}},\
				"required":["deletedAt","note"]}""";
		assertThat(JsonSchema.toJson(account.description())).isEqualTo(schema);
		// typed arguments have no absent state and are described alike
		final var typedAccount = strict.apply(Account::new);
		assertThat(typedAccount.validated(null, null, null, null))
			.isEqualTo(new Account(null, Optional.empty(), 1, null));
		assertThat(JsonSchema.toJson(typedAccount.description())).isEqualTo(schema);
	}

	@Test
	void inputTypeMustBeKnownToReadFromAMap() {
		// a value validator that takes an Object decides itself what it accepts
		final ValueValidator<Object, Integer> coerced = (value, locale, context) -> Yavi._integer()
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
			.containsExactly(tuple("count", "object.type"), tuple("id", "object.anyOf"));
		assertThatThrownBy(() -> Yavi.schema()._object("count", coerced).fromMap(Optional::ofNullable))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("The input type of the argument 'count' is unknown");
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
		// coerced, and its null node and an empty string are both null, of which only the
		// null node is reported as null
		final ValueValidator<JsonNode, Square> square = Yavi.schema()
			._string("type")
			.nullable()
			._integer("side")
			.from(JsonNodeReader.INSTANCE.coercingStrings(), Square::new);
		assertThat(square.validated(jsonMapper.readTree("""
				{"type":"","side":"3"}"""))).isEqualTo(new Square(null, 3));
		assertThat(square.validate(jsonMapper.readTree("""
				{"type":null,"side":""}""")).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.required");
		assertThat(square.validate(jsonMapper.readTree("""
				{"type":null,"side":null}""")).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.notNull");
		assertThatThrownBy(() -> lambda.readingWith(JsonNodeReader.INSTANCE))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void numbersAndKeysAreReadAsTheParserKeepsThem() {
		final ObjectInputValidator<BigDecimal> price = Yavi.schema()
			._bigDecimal("price", c -> c.fractionDigits(2))
			.fromMap(p -> p);
		final ValueValidator<JsonNode, BigDecimal> node = price.readingWith(JsonNodeReader.INSTANCE);
		final String scaled = """
				{"price": 0.10}""";
		final String large = """
				{"price": 12345678901234567.89}""";
		final String longFraction = """
				{"price": 19.990000000000000000001}""";
		final String huge = """
				{"price": 1e400}""";
		final String duplicated = """
				{"price": 1, "price": 2}""";
		// a default mapper holds a number with a fraction as a double, in a tree and in a
		// map: the scale and the digits beyond the 17th are lost before a validator reads
		// the number, 1e400 is infinite and the last of the same keys replaces the others
		assertThat(node.validated(jsonMapper.readTree(scaled))).isEqualTo(new BigDecimal("0.1"));
		assertThat(node.validated(jsonMapper.readTree(large))).isEqualTo(new BigDecimal("12345678901234568"));
		assertThat(node.validated(jsonMapper.readTree(longFraction))).isEqualTo(new BigDecimal("19.99"));
		assertThat(node.validate(jsonMapper.readTree(huge)).errors())
			.extracting(ConstraintViolation::messageKey, ConstraintViolation::violatedValue)
			.containsExactly(tuple("object.type", Double.POSITIVE_INFINITY));
		assertThat(Yavi.schema()
			._double("price")
			.from(JsonNodeReader.INSTANCE, p -> p)
			.validated(jsonMapper.readTree(huge))).isEqualTo(Double.POSITIVE_INFINITY);
		assertThat(node.validated(jsonMapper.readTree(duplicated))).isEqualTo(new BigDecimal("2"));
		assertThat(price.validated(jsonMapper.readValue(scaled, new TypeReference<>() {
		}))).isEqualTo(new BigDecimal("0.1"));
		// a mapper that is told to keeps the number as it is written and refuses the
		// document that has a key twice
		final JsonMapper exactMapper = JsonMapper.builder()
			.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
			.enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
			.build();
		assertThat(node.validated(exactMapper.readTree(scaled))).isEqualTo(new BigDecimal("0.10"));
		assertThat(node.validated(exactMapper.readTree(large))).isEqualTo(new BigDecimal("12345678901234567.89"));
		assertThat(node.validate(exactMapper.readTree(longFraction)).errors())
			.extracting(ConstraintViolation::messageKey)
			.containsExactly("numeric.fractionDigits");
		assertThat(Yavi._bigDecimal()
			.readingWith(JsonNodeReader.INSTANCE)
			.validated(exactMapper.readTree(huge).get("price"))).isEqualTo(new BigDecimal("1E+400"));
		assertThat(Yavi.schema()
			._double("price")
			.from(JsonNodeReader.INSTANCE, p -> p)
			.validate(exactMapper.readTree(huge))
			.errors()).extracting(ConstraintViolation::messageKey).containsExactly("object.type");
		assertThatThrownBy(() -> exactMapper.readTree(duplicated)).isInstanceOf(JacksonException.class);
		assertThat(price.validated(exactMapper.readValue(scaled, new TypeReference<>() {
		}))).isEqualTo(new BigDecimal("0.10"));
		if (!hasJdkJson()) {
			return;
		}
		// the tree model of the JDK keeps the text of a number and refuses a key written
		// twice; a number that no BigDecimal holds is not a number of any type
		final ValueValidator<Object, BigDecimal> jdk = price.readingWith(jdkJsonReader());
		assertThat(jdk.validated(parseJdkJson(scaled))).isEqualTo(new BigDecimal("0.10"));
		assertThat(jdk.validated(parseJdkJson(large))).isEqualTo(new BigDecimal("12345678901234567.89"));
		assertThat(jdk.validate(parseJdkJson(longFraction)).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("numeric.fractionDigits");
		assertThat(jdk.validate(parseJdkJson("""
				{"price": 1e3000000000}""")).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.type");
		assertThatThrownBy(() -> parseJdkJson(duplicated)).isInstanceOf(IllegalStateException.class)
			.hasStackTraceContaining("Duplicate member name");
	}

}
