/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import am.ik.yavi.arguments.Arguments;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.arguments.IntegerValidator;
import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.Arguments7Validator;
import am.ik.yavi.core.ConstraintDescription;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.PathSegment;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ViolationMessage;
import am.ik.yavi.jsonschema.JsonSchema;
import am.ik.yavi.validator.Yavi;
import org.jspecify.annotations.Nullable;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class YaviSchemaTest {

	record Address(String street, String city) {
	}

	record User(@Nullable String contact, Optional<String> nickname, Integer age, Address address,
			@Nullable Address billing, @Nullable List<String> roles, @Nullable Map<String, Integer> scores) {

		boolean isAdultIfAdmin() {
			return this.roles == null || this.age >= 18 || !this.roles.contains("admin");
		}
	}

	static final ValueValidator<Map<String, String>, Address> addressValidator = Yavi.schema()
		._string("street", c -> c.notBlank())
		._string("city", c -> c.notBlank())
		.apply(Address::new)
		.compose(map -> {
			final Map<String, String> nonNull = Objects.requireNonNull(map);
			return Arguments.of(nonNull.get("street"), nonNull.get("city"));
		});

	static final Arguments7ValidatorBuilder<String, String, Integer, Map<String, String>, Map<String, String>, List<String>, Map<String, Integer>, String, Optional<String>, Integer, Address, @Nullable Address, List<String>, Map<String, Integer>> builder = Yavi
		.arguments()
		._object("contact",
				ValueValidator.<String, String>anyOf(Yavi._string(c -> c.email()),
						Yavi._string(c -> c.pattern("[0-9]{10,11}"))))
		._string("nickname", c -> c.notBlank())
		.optional()
		._integer("age", c -> c.greaterThanOrEqual(0))
		.defaultValue(20)
		._object("address", addressValidator)
		._object("billing", addressValidator.nullable())
		._list("roles", Yavi._string(c -> c.trim().notBlank()), c -> c.lessThanOrEqual(2).unique())
		._map("scores", Yavi._integer(c -> c.greaterThanOrEqual(0)), c -> c.lessThanOrEqual(1));

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
			.validate("yavi", " ", -1, Map.of("city", "Tokyo"), Map.of("street", "1-2-3"), List.of("admin", "", "user"),
					Map.of("math", -1, "art", 1))
			.errors();
		// the constraints of a list or map are not checked if an element is invalid
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
		// they are checked on the validated elements and reported on the argument
		final Map<String, String> address = Map.of("street", "1-2-3", "city", "Tokyo");
		assertThat(validator
			.validate("yavi@example.com", null, 30, address, null, List.of("user", " user"),
					Map.of("math", 1, "art", 1))
			.errors()).extracting(ConstraintViolation::name, ConstraintViolation::message, ConstraintViolation::path)
			.containsExactly(
					tuple("roles", "\"roles\" must be unique. [user] is/are duplicated.",
							List.of(new PathSegment.Property("roles"))),
					tuple("scores", "The size of \"scores\" must be less than or equal to 1. The given size is 2",
							List.of(new PathSegment.Property("scores"))));
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
	void descriptionDescribesTheArguments() {
		final ConstraintDescription notBlank = new ConstraintDescription("charSequence.notBlank", List.of(), true);
		// a value whose constraints accept null is described as required
		final ConstraintDescription notNull = new ConstraintDescription("object.notNull", List.of(), true);
		final ConstraintDescription notNegative = new ConstraintDescription("numeric.greaterThanOrEqual", List.of(0),
				false);
		final ValueDescription properties = new ValueDescription.Properties(List.of(
				new ValueDescription.Property("street",
						new ValueDescription.Value("street", String.class, List.of(notBlank))),
				new ValueDescription.Property("city",
						new ValueDescription.Value("city", String.class, List.of(notBlank)))));
		final ValueDescription address = new ValueDescription.NotNull(properties);
		// constraintOnTarget keeps the description and is not part of it
		assertThat(validator.description()).isEqualTo(
				new ValueDescription.Properties(List.of(
						new ValueDescription.Property(
								"contact",
								new ValueDescription.NotNull(new ValueDescription.AnyOf(List.of(
										new ValueDescription.Value("", String.class,
												List.of(notNull,
														new ConstraintDescription("charSequence.email", List.of(),
																false))),
										new ValueDescription.Value("", String.class,
												List.of(notNull,
														new ConstraintDescription("charSequence.pattern",
																List.of("[0-9]{10,11}"), false))))))),
						new ValueDescription.Property("nickname",
								new ValueDescription.Optional(
										new ValueDescription.Value("nickname", String.class, List.of(notBlank)), null)),
						new ValueDescription.Property(
								"age",
								new ValueDescription.Optional(
										new ValueDescription.Value("age", Integer.class, List.of(notNull, notNegative)),
										20)),
						new ValueDescription.Property("address", address),
						// a validator that accepts null describes itself
						new ValueDescription.Property("billing", new ValueDescription.Optional(properties, null)),
						new ValueDescription.Property("roles",
								new ValueDescription.NotNull(new ValueDescription.Elements(
										new ValueDescription.Value("", String.class, List.of(notBlank)),
										List.of(new ConstraintDescription("container.lessThanOrEqual", List.of(2),
												false),
												new ConstraintDescription("collection.unique", List.of(), false))))),
						new ValueDescription.Property("scores",
								new ValueDescription.NotNull(new ValueDescription.Entries(
										new ValueDescription.Value("", Integer.class, List.of(notNull, notNegative)),
										List.of(new ConstraintDescription("container.lessThanOrEqual", List.of(1),
												false))))))));
		final String addressSchema = """
				"properties":{"street":{"type":"string","minLength":1},"city":{"type":"string","minLength":1}},\
				"required":["street","city"]}""";
		assertThat(JsonSchema.toJson(validator.description())).isEqualTo("""
				{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{\
				"contact":{"anyOf":[{"type":"string","format":"email"},\
				{"type":"string","pattern":"^(?:[0-9]{10,11})$"}]},\
				"nickname":{"type":["string","null"],"minLength":1},\
				"age":{"type":["integer","null"],"minimum":0,"default":20},\
				"address":{"type":"object",%s,\
				"billing":{"type":["object","null"],%s,\
				"roles":{"type":"array","items":{"type":"string","minLength":1},"maxItems":2,"uniqueItems":true},\
				"scores":{"type":"object","additionalProperties":{"type":"integer","minimum":0},\
				"maxProperties":1}},\
				"required":["contact","address","roles","scores"]}""".formatted(addressSchema, addressSchema));
	}

	@Test
	void validatorOfASingleValue() {
		enum
		Role{

		ADMIN,USER

		}
		assertThat(Yavi._string(c -> c.notBlank().email()).map(String::length).validated("a@b.c")).isEqualTo(5);
		assertThat(Yavi._enum(Role.class).untyped().validated("ADMIN")).isEqualTo(Role.ADMIN);
		assertThat(Yavi._enum(Role.class, c -> c.oneOf(Role.USER)).validate(Role.ADMIN).errors())
			.extracting(ConstraintViolation::messageKey)
			.containsExactly("object.oneOf");
		assertThat(Yavi.<List<String>>_object(c -> c.notNull()).liftList().validated(List.of(List.of("a"))))
			.containsExactly(List.of("a"));
		// a list or map that is not an argument is null as an empty one, and is not
		// optional if a constraint rejects an empty one
		final ValueValidator<Iterable<String>, List<String>> tags = Yavi._list(Yavi._string(c -> c.notBlank()),
				c -> c.notEmpty().lessThanOrEqual(2));
		final ValueValidator<Map<String, Integer>, Map<String, Integer>> stock = Yavi
			._map(Yavi._integer(c -> c.notNull()), c -> c.greaterThanOrEqual(1).containsKey("a"));
		assertThat(tags.untyped().validated(List.of("a", "b"))).containsExactly("a", "b");
		assertThat(stock.untyped().validated(Map.of("a", 1))).containsExactly(Map.entry("a", 1));
		assertThat(List.of(tags.validate(null).errors(), tags.untyped().validate(List.of()).errors(),
				stock.validate(null).errors(), stock.untyped().validate(Map.of("b", 1)).errors()))
			.extracting(violations -> violations.get(0).name(), violations -> violations.get(0).message())
			.containsExactly(tuple("", "The value must not be empty"), tuple("", "The value must not be empty"),
					tuple("", "The size of the value must be greater than or equal to 1. The given size is 0"),
					tuple("", "The value must contain key a"));
		assertThat(tags.untyped().validate(List.of("a", " ", "c")).errors()).extracting(ConstraintViolation::name)
			.containsExactly("[1]");
		assertThat(tags.description().acceptsNull()).isFalse();
		assertThat(JsonSchema.toJson(tags.description())).endsWith(
				"\"type\":\"array\",\"items\":{\"type\":\"string\",\"minLength\":1},\"minItems\":1,\"maxItems\":2}");
		assertThat(JsonSchema.toJson(stock.description()))
			.endsWith("\"type\":\"object\",\"additionalProperties\":{\"type\":\"integer\"},\"minProperties\":1}");
		// a value without a name is not named in the message
		assertThat(Yavi._integer(c -> c.notNull().positive()).untyped().validate("1").errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::message)
			.containsExactly(tuple("", "The value must be of type integer"));
		assertThat(Yavi._object(byte[].class, c -> c.notNull()).untyped().validate(null).errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::message)
			.containsExactly(tuple("", "The value must not be null"));
		assertThat(Yavi._string("email", c -> c.notBlank()).validate(" ").errors())
			.extracting(ConstraintViolation::name, ConstraintViolation::message)
			.containsExactly(tuple("email", "\"email\" must not be blank"));
		// a value is required: null is reported by a constraint that rejects it, or else
		// with object.notNull, whether the constraints are conditional or not
		final IntegerValidator<Integer> age = Yavi._integer("age", c -> c.greaterThanOrEqual(0));
		final ConstraintGroup strict = ConstraintGroup.of("strict");
		final IntegerValidator<Integer> conditional = IntegerValidatorBuilder.wrap(b -> b.constraintOnGroup(strict,
				v -> v._integer(Arguments1::arg1, "years", c -> c.notNull().greaterThanOrEqual(0))))
			.build();
		assertThat(List.of(age.validate(null).errors(), Yavi._string("name", c -> c.notBlank()).validate(null).errors(),
				Yavi._integer(c -> c).untyped().validate(null).errors(), conditional.validate(null).errors(),
				conditional.validate(null, strict).errors()))
			.extracting(ConstraintViolations::size, violations -> violations.get(0).name(),
					violations -> violations.get(0).message())
			.containsExactly(tuple(1, "age", "\"age\" must not be null"),
					tuple(1, "name", "\"name\" must not be blank"), tuple(1, "", "The value must not be null"),
					tuple(1, "", "The value must not be null"), tuple(1, "years", "\"years\" must not be null"));
		assertThat(age.description().acceptsNull()).isFalse();
		// the modifiers accept null, and the type of the value says what it is then
		final ValueValidator<Integer, @Nullable Integer> nullableAge = age.nullable();
		final ValueValidator<Integer, Optional<Integer>> optionalAge = age.optional();
		assertThat(nullableAge.validated(null)).isNull();
		assertThat(nullableAge.description().acceptsNull()).isTrue();
		assertThat(optionalAge.validated(null)).isEmpty();
		assertThat(age.defaultValue(20).validated(null)).isEqualTo(20);
		assertThat(nullableAge.validate(-1).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("numeric.greaterThanOrEqual");
		// isNull() alone is never satisfied by a required value; with nullable() the
		// value must be null
		final IntegerValidator<Integer> mustBeNull = Yavi._integer("id", c -> c.isNull());
		assertThat(List.of(mustBeNull.validate(null).errors(), mustBeNull.validate(1).errors(),
				mustBeNull.nullable().validate(1).errors()))
			.extracting(violations -> violations.get(0).messageKey())
			.containsExactly("object.notNull", "object.isNull", "object.isNull");
		assertThat(mustBeNull.nullable().validate(null).isValid()).isTrue();
	}

	@Test
	// a nullness checker reports these chains, the compiler alone does not
	@SuppressWarnings("NullAway")
	void nullIsNotAFallbackOfAValueThatIsNeverNull() {
		// the function that creates the result would be given a null that the type of
		// its parameter excludes, an Optional included
		final YaviSchema schema = Yavi.schema();
		final List<ThrowingCallable> rejected = List.of(() -> schema._string("x").defaultValue(null),
				() -> schema._string("x").recover(null), () -> schema._string("x").optional().recover(null),
				() -> schema._string("x").recover("a").defaultValue(null),
				() -> schema._list("x", Yavi._string(c -> c.notBlank())).defaultValue(null),
				() -> schema._rest().recover(null),
				() -> schema._string("x").recoverWith(violations -> null).apply(x -> x).validate(null),
				() -> schema._string("x")
					.optional()
					.recoverWith(violations -> null)
					.fromMap(x -> x)
					.validate(Map.of()));
		assertThat(rejected).allSatisfy(chain -> assertThatThrownBy(chain).isInstanceOf(NullPointerException.class)
			.hasMessageContaining("of the argument")
			.hasMessageContaining("is null, but its validated value is never null"));
		// nullable() says null, and a value validator may produce null on its own
		assertThat(schema._string("x", c -> c.notBlank())
			.nullable()
			.recover(null)
			.apply(x -> Optional.ofNullable(x))
			.validated(" ")).isEmpty();
		assertThat(schema._string(ValueValidator.<String>passThrough())
			.defaultValue(null)
			.apply(x -> Optional.ofNullable(x))
			.validated((String) null)).isEmpty();
	}

	@Test
	void chainWithoutEffectDoesNotCompile() throws Exception {
		// every chain is one line of a source file: the lines that do not compile are
		// reported by the compiler
		final List<String> accepted = List.of(
				// an invalid value is recovered as an absent one
				"s._string(\"x\").optional().recover(Optional.empty()).required()",
				"s._string(\"x\").nullable().recoverWith(violations -> null).rejectingNull()",
				// null produces something else than the fallback
				"s._integer(\"x\").recover(0).nullable().required()",
				"s._integer(\"x\").recover(0).defaultValue(1).presence()",
				"s._integer(\"x\").recoverWith(violations -> 0).optional().rejectingNull()",
				// a validator that accepts null
				"s._integer(Yavi._integer(\"x\", c -> c.positive()).defaultValue(1)).rejectingNull()",
				"s._integer(Yavi._integer(\"x\", c -> c.positive()).nullable()).presence()",
				// a lambda is not taken for a validator that accepts null
				"s._string((value, locale, context) -> Validated.successWith(value)).nullable().required()",
				"s._object(\"o\", s._string(\"x\").fromMap(x -> x).nullable()).required()",
				"InputReader.ofJdkTypes().coercingStrings(StringCoercion.strict().trimming())",
				// the rest is never null
				"s._rest().recover(Map.of())", "s._embedded(s._string(\"x\").fromMap(x -> x)).optional()",
				"DiscriminatedUnionValidator.<String>of(\"type\").when(\"a\", s._string(\"x\").fromMap(x -> x))"
						+ ".validate(Map.of())");
		final List<String> rejected = List.of(
				// only the last one would decide what null produces
				"s._string(\"x\").nullable().optional()", "s._string(\"x\").optional().nullable()",
				"s._string(\"x\").optional().optional()", "s._string(\"x\").nullable().nullable()",
				"s._string(\"x\").optional().defaultValue(Optional.of(\"a\"))",
				"s._string(\"x\").nullable().defaultValue(\"a\")", "s._integer(\"x\").defaultValue(1).nullable()",
				"s._integer(\"x\").defaultValue(1).optional()", "s._integer(\"x\").defaultValue(1).defaultValue(2)",
				"s._integer(\"x\").defaultValue(1).recover(2)",
				"s._integer(\"x\").defaultValue(1).recoverWith(violations -> 2)",
				// the second one would never recover
				"s._integer(\"x\").recover(1).recover(2)", "s._integer(\"x\").recover(1).recoverWith(violations -> 2)",
				"s._integer(\"x\").recover(0).nullable().optional()",
				"s._string(\"x\").optional().recover(Optional.empty()).nullable()",
				"s._string(\"x\").optional().recover(Optional.empty()).recover(Optional.empty())",
				// a property that rejects null rejects an absent key too
				"s._string(\"x\").required()", "s._string(\"x\").rejectingNull()",
				"s._string(Yavi._string(\"x\", c -> c.notBlank())).required()",
				// a validator that accepts null
				"s._integer(Yavi._integer(\"x\", c -> c.positive()).defaultValue(1)).nullable()",
				"s._integer(Yavi._integer(\"x\", c -> c.positive()).nullable()).recover(2)",
				"s._object(\"o\", s._string(\"x\").fromMap(x -> x).nullable()).nullable()",
				// the rest is never null, and is not read from a key
				"s._rest().nullable()", "s._rest().optional()", "s._rest().defaultValue(Map.of())",
				"s._rest().recover(Map.of()).recover(Map.of())", "s._rest().presence()",
				"s._embedded(s._string(\"x\").fromMap(x -> x)).optional().required()",
				// a reader that coerces strings is given its rules once
				"InputReader.ofJdkTypes().coercingStrings(StringCoercion.strict().trimming()).coercingStrings()",
				"ParameterTree.reader().coercingStrings(StringCoercion.strict().trimming())",
				// a union without alternatives would reject every input
				"DiscriminatedUnionValidator.<String>of(\"type\").validate(Map.of())");
		final List<String> chains = new ArrayList<>(accepted);
		chains.addAll(rejected);
		final String source = """
				import java.util.Map; import java.util.Optional; import am.ik.yavi.builder.YaviSchema; \
				import am.ik.yavi.core.InputReader; import am.ik.yavi.core.StringCoercion; \
				import am.ik.yavi.core.Validated; import am.ik.yavi.input.DiscriminatedUnionValidator; \
				import am.ik.yavi.input.ParameterTree; \
				import am.ik.yavi.validator.Yavi; \
				class Chains { void chains(YaviSchema s) { Object[] chains = {
				%s
				}; } }""".formatted(String.join(",\n", chains));
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		final JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///Chains.java"),
				JavaFileObject.Kind.SOURCE) {
			@Override
			public CharSequence getCharContent(boolean ignoreEncodingErrors) {
				return source;
			}
		};
		compiler.getTask(null, null, diagnostics, List.of("-proc:none", "-d",
				Files.createTempDirectory("chains").toString(), "-classpath", System.getProperty("java.class.path")),
				null, List.of(file))
			.call();
		// the first chain is on the second line
		assertThat(diagnostics.getDiagnostics()).filteredOn(d -> d.getKind() == Diagnostic.Kind.ERROR)
			.extracting(d -> chains.get((int) d.getLineNumber() - 2))
			.containsExactlyElementsOf(rejected);
	}

}
