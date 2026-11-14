/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.Presence;
import am.ik.yavi.core.ReversibleValueValidator;
import am.ik.yavi.core.StringCoercion;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import am.ik.yavi.jsonschema.JsonSchema;
import am.ik.yavi.validator.Yavi;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class ReversibleValidatorTest {

	enum Role {

		ADMIN, USER

	}

	record Email(String value) {
	}

	record UserName(String first, String last) {
	}

	record Department(String name, String code) {
	}

	record Address(String city, Optional<String> street) {
	}

	record Employee(Integer id, UserName name, @Nullable Department department, Email email, Optional<String> nickname,
			Role role, LocalDate joined, List<String> tags, List<Integer> scores, List<Address> addresses,
			Map<String, BigDecimal> rates, Presence<String> memo, @Nullable Integer level, Boolean active,
			Map<String, @Nullable Object> rest) {
	}

	static final ReversibleValueValidator<String, Email> emailValidator = Yavi
		._string("email", c -> c.trim().notBlank().email())
		.map(Email::new, Email::value);

	static final ObjectInputValidator.Reversible<UserName> userNameValidator = Yavi.reversibleSchema()
		._string("first_name", c -> c.notBlank())
		._string("last_name", c -> c.notBlank())
		.fromMapReversible(UserName::new, UserName::first, UserName::last);

	static final ObjectInputValidator.Reversible<Department> departmentValidator = Yavi.reversibleSchema()
		._string("dept_name", c -> c.notBlank())
		._string("dept_code", c -> c.notBlank())
		.flatFromMapReversible((name, code) -> Validated.successWith(new Department(name, code)), Department::name,
				Department::code)
		.rejectingUnknownProperties();

	static final ObjectInputValidator.Reversible<Address> addressValidator = Yavi.reversibleSchema()
		._string("city", c -> c.notBlank())
		._string("street")
		.optional()
		.fromMapReversible(Address::new, Address::city, Address::street)
		.constraintOnTarget(address -> !address.city().equals("nowhere"), "city", ViolationMessage.of("city", "!"));

	static final ObjectInputValidator.Reversible<Employee> employeeValidator = Yavi.reversibleSchema()
		._integer("id")
		._embedded(userNameValidator)
		._embedded(departmentValidator)
		.nullable()
		._string("email", c -> c.trim().notBlank().email())
		.map(Email::new, Email::value)
		._string("nickname")
		.optional()
		._enum("role", Role.class)
		.defaultValue(Role.USER)
		._localDate("joined")
		._list("tags", Yavi._string(c -> c.notBlank()))
		.splitBy(",")
		.defaultValue(List.of())
		._list("scores", Yavi._integer())
		.label("Scores")
		.splitBy(Pattern.compile("\\s+"), " ")
		.defaultValue(List.of())
		._list("addresses", addressValidator)
		._map("rates", Yavi._bigDecimal())
		._string("memo")
		.nullable()
		.presence()
		._integer("level")
		.nullable()
		.rejectingNull()
		._boolean(Yavi._boolean("active").recover(false))
		._rest()
		.fromMapReversible(Employee::new, Employee::id, Employee::name, Employee::department, Employee::email,
				Employee::nickname, Employee::role, Employee::joined, Employee::tags, Employee::scores,
				Employee::addresses, Employee::rates, Employee::memo, Employee::level, Employee::active,
				Employee::rest);

	static Employee employee() {
		return new Employee(1, new UserName("Taro", "Yamada"), new Department("Sales", "S1"),
				new Email("taro@example.com"), Optional.of("taro"), Role.ADMIN, LocalDate.of(2024, 4, 1),
				List.of("java", "yavi"), List.of(1, 2),
				List.of(new Address("Tokyo", Optional.of("1-1")), new Address("Osaka", Optional.empty())),
				Map.of("jpy", new BigDecimal("1.5")), Presence.of("hello"), 3, true, Map.of("extra", "x"));
	}

	static <K, V> Map<K, V> mapOf(@Nullable Object... keysAndValues) {
		final Map<@Nullable Object, @Nullable Object> map = new LinkedHashMap<>();
		for (int i = 0; i < keysAndValues.length; i += 2) {
			map.put(keysAndValues[i], keysAndValues[i + 1]);
		}
		@SuppressWarnings("unchecked")
		final Map<K, V> typed = (Map<K, V>) map;
		return typed;
	}

	@Test
	void anObjectIsWrittenBackAsTheMapItIsReadFrom() {
		final Employee employee = employee();
		// the rules on the created object are all checked and keep the reverse
		final ObjectInputValidator.Reversible<Address> withStreet = addressValidator
			.constraintOnTarget(address -> address.street().isPresent(), "street", ViolationMessage.of("street", "!"));
		assertThat(withStreet.validate(withStreet.toMap(new Address("nowhere", Optional.empty()))).errors())
			.extracting(ConstraintViolation::name)
			.containsExactly("city", "street");
		final ReversibleValueValidator<String, Email> ofExample = emailValidator
			.constraintOnTarget(email -> email.value().endsWith("@example.com"), "domain",
					ViolationMessage.of("domain", "!"))
			.constraintOnTarget(email -> email.value().length() > 20, "length", ViolationMessage.of("length", "!"));
		assertThat(ofExample.validate(ofExample.reverse(new Email("a@example.org"))).errors())
			.extracting(ConstraintViolation::name)
			.containsExactly("domain", "length");
		final Map<String, Object> map = employeeValidator.toMap(employee);
		// the properties in their order, embedded ones and the rest in the same map
		assertThat(map).containsExactly(entry("id", 1), entry("first_name", "Taro"), entry("last_name", "Yamada"),
				entry("dept_name", "Sales"), entry("dept_code", "S1"), entry("email", "taro@example.com"),
				entry("nickname", "taro"), entry("role", "ADMIN"), entry("joined", LocalDate.of(2024, 4, 1)),
				entry("tags", "java,yavi"), entry("scores", "1 2"),
				entry("addresses",
						List.of(mapOf("city", "Tokyo", "street", "1-1"), mapOf("city", "Osaka", "street", null))),
				entry("rates", Map.of("jpy", new BigDecimal("1.5"))), entry("memo", "hello"), entry("level", 3),
				entry("active", true), entry("extra", "x"));
		assertThat(employeeValidator.reverse(employee)).isEqualTo(map);
		assertThat(employeeValidator.validate(map)).isEqualTo(Validated.successWith(employee));

		// what has no value: null for a key that a named parameter needs, and no entry
		// for a property that is absent or rejects null
		final Employee empty = new Employee(2, new UserName("Hanako", "Sato"), null, new Email("hanako@example.com"),
				Optional.empty(), Role.USER, LocalDate.of(2025, 1, 1), List.of(), List.of(), List.of(), Map.of(),
				Presence.absent(), null, false, Map.of());
		final Map<String, Object> written = employeeValidator.toMap(empty);
		assertThat(written).containsExactly(entry("id", 2), entry("first_name", "Hanako"), entry("last_name", "Sato"),
				entry("dept_name", null), entry("dept_code", null), entry("email", "hanako@example.com"),
				entry("nickname", null), entry("role", "USER"), entry("joined", LocalDate.of(2025, 1, 1)),
				entry("tags", null), entry("scores", null), entry("addresses", List.of()), entry("rates", Map.of()),
				entry("active", false));
		assertThat(employeeValidator.validate(written)).isEqualTo(Validated.successWith(empty));
		final Employee cleared = new Employee(2, empty.name(), null, empty.email(), Optional.empty(), Role.USER,
				empty.joined(), List.of(), List.of(), List.of(), Map.of(), Presence.presentNull(), null, false,
				Map.of());
		assertThat(employeeValidator.toMap(cleared)).containsEntry("memo", null);
		assertThat(employeeValidator.validate(employeeValidator.toMap(cleared)))
			.isEqualTo(Validated.successWith(cleared));

		// the map is the caller's, and an object that the validator does not create is
		// rejected. A label that is written before splitBy is kept
		written.put("updated_by", "admin");
		written.put("scores", "1 x");
		assertThat(employeeValidator.validate(written).errors()).extracting(violation -> violation.message())
			.containsExactly("\"Scores\" must be of type integer");
		assertThatThrownBy(() -> employeeValidator.toMap(
				new Employee(1, employee.name(), null, employee.email(), Optional.empty(), Role.USER, employee.joined(),
						List.of(), List.of(), List.of(), Map.of(), Presence.absent(), null, true, Map.of("id", 9))))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The property 'id' is written twice.");

		// a view writes some of the properties: those of the elements of a list by the
		// path to them and those of an embedded validator by their names. What is not in
		// the view has no entry, and the rest is written only by a view that excludes
		final PropertyView<Employee> summary = employeeValidator.including("id", "first_name", "addresses.city",
				"rates", "memo");
		assertThat(summary.toMap(employee)).containsExactly(entry("id", 1), entry("first_name", "Taro"),
				entry("addresses", List.of(mapOf("city", "Tokyo"), mapOf("city", "Osaka"))),
				entry("rates", Map.of("jpy", new BigDecimal("1.5"))), entry("memo", "hello"));
		assertThat(summary.toMap(empty)).containsExactly(entry("id", 2), entry("first_name", "Hanako"),
				entry("addresses", List.of()), entry("rates", Map.of()));
		final PropertyView<Employee> withoutContact = employeeValidator.excluding("email", "dept_code",
				"addresses.street", "level");
		assertThat(withoutContact.toMap(employee)).containsExactly(entry("id", 1), entry("first_name", "Taro"),
				entry("last_name", "Yamada"), entry("dept_name", "Sales"), entry("nickname", "taro"),
				entry("role", "ADMIN"), entry("joined", LocalDate.of(2024, 4, 1)), entry("tags", "java,yavi"),
				entry("scores", "1 2"), entry("addresses", List.of(mapOf("city", "Tokyo"), mapOf("city", "Osaka"))),
				entry("rates", Map.of("jpy", new BigDecimal("1.5"))), entry("memo", "hello"), entry("active", true),
				entry("extra", "x"));
		// a view is made narrower, and keeps what it left out of a nested object
		assertThat(withoutContact.including("id", "addresses").excluding("id").toMap(employee))
			.containsExactly(entry("addresses", List.of(mapOf("city", "Tokyo"), mapOf("city", "Osaka"))));
		// the accessor of a property that is not written is not called
		final ObjectInputValidator.Reversible<UserName> firstNameOnly = Yavi.reversibleSchema()
			._string("first_name")
			._string("last_name")
			.fromMapReversible(UserName::new, UserName::first, name -> {
				throw new IllegalStateException("not loaded");
			});
		assertThat(firstNameOnly.excluding("last_name").toMap(employee.name()))
			.containsExactly(entry("first_name", "Taro"));
		// a name is checked when the view is built
		assertThatThrownBy(() -> employeeValidator.excluding("emial")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageStartingWith(
					"The property 'emial' is not defined by the validator. The properties are [id, first_name, ");
		assertThatThrownBy(() -> employeeValidator.including("id", "addresses.town"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The property 'addresses.town' is not defined by the validator. "
					+ "The properties of 'addresses' are [city, street].");
		assertThatThrownBy(() -> employeeValidator.excluding("extra")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageStartingWith("The property 'extra' is not defined by the validator.");
		assertThatThrownBy(() -> employeeValidator.including("id.value")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The property 'id' does not describe an object, so no property of it can be chosen. "
					+ "Name it as a whole.");
		assertThatThrownBy(() -> employeeValidator.including("id", "id")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The property 'id' is given twice.");
		assertThatThrownBy(() -> employeeValidator.excluding("addresses", "addresses.city"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The property 'addresses' is given as a whole and with the paths to its properties [city].");
		assertThatThrownBy(() -> withoutContact.excluding("addresses.street"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The property 'addresses.street' is not written by the view that is made narrower.");
		assertThatThrownBy(() -> summary.including("email")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The property 'email' is not written by the view that is made narrower.");

		// a validator that is derived without changing the value still goes back, which
		// its type tells
		assertThat(emailValidator.reverse(emailValidator.validated(" taro@example.com ")))
			.isEqualTo("taro@example.com");
		final Email email = new Email("a@example.com");
		assertThat(emailValidator.named("contact").reverse(email)).isEqualTo("a@example.com");
		assertThat(emailValidator.optional().named("contact").reverse(Optional.empty())).isNull();
		assertThat(Yavi._integer("page").defaultValue(1).named("p").reverse(2)).isEqualTo(2);
		final ValueValidator<String, String> lambda = (value, locale, context) -> Validated
			.successWith(String.valueOf(value));
		assertThat(ReversibleValueValidator.of(lambda, value -> value).prefixed("p").reverse("a")).isEqualTo("a");
		// a container of values goes back to the container of their inputs
		assertThat(emailValidator.liftSet().reverse(Set.of(email))).isEqualTo(List.of("a@example.com"));
		assertThat(Yavi._integer().liftList().reverse(List.of(1, 2))).isEqualTo(List.of(1, 2));
		assertThat(emailValidator.liftMap().reverse(Map.of("k", email))).isEqualTo(Map.of("k", "a@example.com"));
		assertThat(emailValidator.liftOptional().reverse(Optional.of(email))).isEqualTo(Optional.of("a@example.com"));
		assertThat(emailValidator.liftPresence().reverse(Presence.of(email))).isEqualTo(Presence.of("a@example.com"));
		assertThat(emailValidator.liftPresence().reverse(Presence.absent())).isEqualTo(Presence.absent());
	}

	sealed interface Shape {

	}

	record Circle(Double radius) implements Shape {
	}

	record Square(Double side, String type) implements Shape {
	}

	record Tree(String name, List<Tree> children) {
	}

	@Test
	void aUnionAndARecursiveValidatorAreWrittenBack() {
		final ObjectInputValidator.Reversible<Circle> circleValidator = Yavi.reversibleSchema()
			._double("radius")
			.fromMapReversible(Circle::new, Circle::radius);
		final ObjectInputValidator.Reversible<Square> squareValidator = Yavi.reversibleSchema()
			._double("side")
			._string("type")
			.fromMapReversible(Square::new, Square::side, Square::type);
		final DiscriminatedUnionValidator.Reversible<Shape> shapeValidator = DiscriminatedUnionValidator
			.<Shape>of("type")
			.when("circle", Circle.class, circleValidator)
			.when("square", Square.class, squareValidator)
			.constraintOnTarget(shape -> true, "shape", ViolationMessage.of("shape", "!"));
		// the discriminator is added first unless the alternative writes it
		assertThat(shapeValidator.toMap(new Circle(1.5))).containsExactly(entry("type", "circle"),
				entry("radius", 1.5));
		assertThat(shapeValidator.toMap(new Square(2.0, "square"))).containsExactly(entry("side", 2.0),
				entry("type", "square"));
		assertThat(shapeValidator.validate(shapeValidator.toMap(new Circle(1.5))))
			.isEqualTo(Validated.successWith(new Circle(1.5)));
		// a union is embedded and nested like any reversible validator
		record Drawing(String title, Shape shape, List<Shape> others) {
		}
		final ObjectInputValidator.Reversible<Drawing> drawingValidator = Yavi.reversibleSchema()
			._string("title")
			._embedded(shapeValidator)
			._list("others", shapeValidator)
			.fromMapReversible(Drawing::new, Drawing::title, Drawing::shape, Drawing::others);
		final Drawing drawing = new Drawing("a", new Circle(1.0), List.of(new Square(2.0, "square")));
		assertThat(drawingValidator.toMap(drawing)).containsExactly(entry("title", "a"), entry("type", "circle"),
				entry("radius", 1.0), entry("others", List.of(mapOf("side", 2.0, "type", "square"))));
		assertThat(drawingValidator.validate(drawingValidator.toMap(drawing)))
			.isEqualTo(Validated.successWith(drawing));
		// a view of a union writes the properties that an alternative defines, and
		// always the discriminator, also of a union that is nested
		final PropertyView<Shape> radius = shapeValidator.including("radius");
		assertThat(radius.toMap(new Circle(1.5))).containsExactly(entry("type", "circle"), entry("radius", 1.5));
		assertThat(radius.toMap(new Square(2.0, "square"))).containsExactly(entry("type", "square"));
		assertThatThrownBy(() -> shapeValidator.excluding("type")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The discriminator 'type' is always written: it is neither included nor excluded.");
		assertThat(drawingValidator.including("title", "others.side").toMap(drawing))
			.containsExactly(entry("title", "a"), entry("others", List.of(mapOf("side", 2.0, "type", "square"))));
		assertThat(drawingValidator.excluding("radius", "others").toMap(drawing)).containsExactly(entry("title", "a"),
				entry("type", "circle"));
		// an alternative without its type gives a union that does not write
		final DiscriminatedUnionValidator<Shape> reading = shapeValidator.when("other", circleValidator);
		assertThat(reading).isNotInstanceOf(ReversibleValueValidator.class);

		final ObjectInputValidator.Reversible<Tree> treeValidator = ReversibleValueValidator
			.recursive(self -> Yavi.reversibleSchema()
				._string("name")
				._list("children", self)
				.defaultValue(List.of())
				.fromMapReversible(Tree::new, Tree::name, Tree::children));
		final Tree tree = new Tree("root", List.of(new Tree("leaf", List.of())));
		assertThat(treeValidator.toMap(tree))
			.isEqualTo(mapOf("name", "root", "children", List.of(mapOf("name", "leaf", "children", List.of()))));
		assertThat(treeValidator.validate(treeValidator.toMap(tree))).isEqualTo(Validated.successWith(tree));
		// a path names the properties at its depth
		assertThat(treeValidator.excluding("children.name").toMap(tree))
			.isEqualTo(mapOf("name", "root", "children", List.of(mapOf("children", List.of()))));

		// a schema whose properties are known at run time writes the map it produces
		final ObjectInputValidator.Reversible<Map<String, Object>> dynamicValidator = Yavi
			.<Object>reversibleDynamicSchema()
			.property("email", emailValidator)
			.property("shape", shapeValidator)
			.property("memo", Yavi._string().nullable())
			.presence()
			.rest(Yavi._integer())
			.build();
		final Map<String, Object> values = mapOf("email", new Email("a@example.com"), "shape", new Circle(1.0), "n", 1);
		assertThat(dynamicValidator.toMap(values)).containsExactly(entry("email", "a@example.com"),
				entry("shape", mapOf("type", "circle", "radius", 1.0)), entry("n", 1));
		assertThat(dynamicValidator.validate(dynamicValidator.toMap(values))).isEqualTo(Validated.successWith(values));
		assertThat(dynamicValidator.including("email", "shape.radius").toMap(values))
			.containsExactly(entry("email", "a@example.com"), entry("shape", mapOf("type", "circle", "radius", 1.0)));
	}

	record Signup(String name, Integer age, Role role, Boolean agreed, LocalDate birthday, List<String> tags,
			List<String> links, Address address, List<Address> others) {
	}

	@Test
	void aStoredObjectIsShownInAFormAsTheParametersThatAreSentForIt() {
		final ObjectInputValidator.Reversible<Signup> signupSchema = Yavi.reversibleSchema()
			._string("name", c -> c.notBlank())
			._integer("age")
			._enum("role", Role.class)
			._boolean("agreed")
			.defaultValue(false)
			._localDate("birthday")
			._list("tags", Yavi._string())
			.splitBy(",")
			.defaultValue(List.of())
			// one value per line of a textarea
			._list("links", Yavi._string(c -> c.url()))
			.splitBy(Pattern.compile("\\s*\\R\\s*"), "\n")
			.defaultValue(List.of())
			._object("address", addressValidator)
			._list("others", addressValidator)
			.defaultValue(List.of())
			.fromMapReversible(Signup::new, Signup::name, Signup::age, Signup::role, Signup::agreed, Signup::birthday,
					Signup::tags, Signup::links, Signup::address, Signup::others);
		final ValueValidator<Map<String, ?>, Signup> signupValidator = signupSchema
			.readingParameters(StringCoercion.strict().trimming());
		final Signup stored = new Signup("Taro", 30, Role.ADMIN, false, LocalDate.of(2000, 1, 2), List.of("a", "b"),
				List.of("https://a.example", "https://b.example"), new Address("Tokyo", Optional.empty()),
				List.of(new Address("Osaka", Optional.of("2-2"))));

		final ParameterTree tree = ParameterTree.ofValues(signupSchema.toMap(stored));
		// the values as text in the shape of the description, like the parameters that
		// were sent: no entry for a checkbox that is not checked, an empty string for a
		// field without a value
		assertThat(tree.toMap(signupSchema.description())).containsExactly(entry("name", "Taro"), entry("age", "30"),
				entry("role", "ADMIN"), entry("birthday", "2000-01-02"), entry("tags", "a,b"),
				entry("links", "https://a.example\nhttps://b.example"),
				entry("address", mapOf("city", "Tokyo", "street", "")),
				entry("others", List.of(mapOf("city", "Osaka", "street", "2-2"))));
		// which is what the parameters that were sent are shown as
		assertThat(ParameterTree.of(Map.of("links", "https://a.example\nhttps://b.example"))
			.toMap(signupSchema.description()))
			.containsEntry("links", tree.toMap(signupSchema.description()).get("links"));
		final Map<String, Object> parameters = tree.toParameters(signupSchema.description());
		assertThat(parameters).containsExactly(entry("name", "Taro"), entry("age", "30"), entry("role", "ADMIN"),
				entry("birthday", "2000-01-02"), entry("tags", "a,b"),
				entry("links", "https://a.example\nhttps://b.example"), entry("address.city", "Tokyo"),
				entry("address.street", ""), entry("others[0].city", "Osaka"), entry("others[0].street", "2-2"));
		assertThat(signupValidator.validate(parameters)).isEqualTo(Validated.successWith(stored));
		final Signup agreed = new Signup("Taro", 30, Role.ADMIN, true, stored.birthday(), List.of(), List.of(),
				stored.address(), List.of());
		assertThat(ParameterTree.ofValues(signupSchema.toMap(agreed)).toMap(signupSchema.description()))
			.containsEntry("agreed", "true")
			.containsEntry("tags", "")
			.containsEntry("links", "")
			.containsEntry("others", List.of());
		// a new form is given its initial values as a map of some of the properties
		assertThat(ParameterTree.ofValues(Map.of("age", 20, "agreed", true)).toParameters(signupSchema.description()))
			.containsExactly(entry("name", ""), entry("age", "20"), entry("role", ""), entry("agreed", "true"),
					entry("birthday", ""), entry("tags", ""), entry("links", ""), entry("address.city", ""),
					entry("address.street", ""));
		// the delimiter is what the pattern separates: the text is split where it is
		// joined
		assertThatThrownBy(
				() -> Yavi.reversibleSchema()._list("links", Yavi._string()).splitBy(Pattern.compile("\\R"), ", "))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The separator '\\R' does not match the delimiter ', ' that joins the elements.");
		// a view feeds the tree as the whole map does: a field that is not written is
		// shown without a value
		assertThat(ParameterTree.ofValues(signupSchema.excluding("age", "address.street", "others").toMap(stored))
			.toParameters(signupSchema.description())).containsExactly(entry("name", "Taro"), entry("age", ""),
					entry("role", "ADMIN"), entry("birthday", "2000-01-02"), entry("tags", "a,b"),
					entry("links", "https://a.example\nhttps://b.example"), entry("address.city", "Tokyo"),
					entry("address.street", ""));
		assertThat(StringCoercion.textOf(new BigDecimal("1E+3"))).isEqualTo("1000");
	}

	record Event(Integer id, UserName owner, Optional<String> note, Role role, LocalDate day, LocalTime start,
			LocalDateTime createdAt, BigDecimal price, Boolean open) {
	}

	@Test
	void anObjectIsWrittenAsTheParametersOfAStatementAndReadFromItsRow() {
		final ObjectInputValidator.Reversible<Event> eventValidator = Yavi.reversibleSchema()
			._integer("id")
			._embedded(userNameValidator)
			._string("note")
			.optional()
			._enum("role", Role.class)
			._localDate("event_day")
			._localTime("start_time")
			._localDateTime("created_at")
			._bigDecimal("price")
			// a database without a boolean holds 0 or 1
			._integer(Yavi._integer("open").map(open -> open == 1, open -> open ? 1 : 0))
			.fromMapReversible(Event::new, Event::id, Event::owner, Event::note, Event::role, Event::day, Event::start,
					Event::createdAt, Event::price, Event::open);
		final SingleConnectionDataSource dataSource = new SingleConnectionDataSource("jdbc:sqlite::memory:", true);
		final JdbcClient jdbcClient = JdbcClient.create(dataSource);
		jdbcClient
			.sql("CREATE TABLE event (id INT PRIMARY KEY, first_name VARCHAR(32) NOT NULL, last_name VARCHAR(32) NOT NULL, "
					+ "note VARCHAR(32), role VARCHAR(8) NOT NULL, event_day DATE NOT NULL, start_time TIME NOT NULL, "
					+ "created_at TIMESTAMP NOT NULL, price DECIMAL(10, 2) NOT NULL, open BOOLEAN NOT NULL)")
			.update();
		final List<Event> events = List.of(
				new Event(1, new UserName("Taro", "Yamada"), Optional.of("memo"), Role.ADMIN,
						LocalDate.of(2026, 11, 14), LocalTime.of(9, 30), LocalDateTime.of(2026, 11, 1, 12, 0, 5),
						new BigDecimal("1200.5"), true),
				new Event(2, new UserName("Hanako", "Sato"), Optional.empty(), Role.USER, LocalDate.of(2026, 12, 1),
						LocalTime.of(18, 0), LocalDateTime.of(2026, 11, 2, 8, 0), new BigDecimal("0"), false));
		for (Event event : events) {
			assertThat(jdbcClient
				.sql("INSERT INTO event (id, first_name, last_name, note, role, event_day, start_time, created_at, price, open) "
						+ "VALUES (:id, :first_name, :last_name, :note, :role, :event_day, :start_time, :created_at, :price, :open)")
				.params(eventValidator.toMap(event))
				.update()).isEqualTo(1);
		}
		// a driver that holds a date or a time as a type of java.sql
		final Map<String, Object> row = eventValidator.toMap(events.get(0));
		row.put("event_day", java.sql.Date.valueOf(events.get(0).day()));
		row.put("start_time", java.sql.Time.valueOf(events.get(0).start()));
		row.put("created_at", java.sql.Timestamp.valueOf(events.get(0).createdAt()));
		assertThat(eventValidator.validate(row)).isEqualTo(Validated.successWith(events.get(0)));

		// a nested object is bound under the names of its violations
		record Venue(Integer id, Address address, Instant openedAt) {
		}
		final ObjectInputValidator.Reversible<Venue> venueValidator = Yavi.reversibleSchema()
			._integer("id")
			._object("address", addressValidator)
			// a driver that holds a timestamp as the milliseconds of the epoch
			._long(Yavi._long("opened_at").map(Instant::ofEpochMilli, Instant::toEpochMilli))
			.fromMapReversible(Venue::new, Venue::id, Venue::address, Venue::openedAt);
		jdbcClient
			.sql("CREATE TABLE venue (id INT PRIMARY KEY, city VARCHAR(32) NOT NULL, street VARCHAR(32), "
					+ "opened_at TIMESTAMP NOT NULL)")
			.update();
		final Venue venue = new Venue(1, new Address("Tokyo", Optional.empty()),
				Instant.ofEpochMilli(1_700_000_000_000L));
		jdbcClient.sql(
				"INSERT INTO venue (id, city, street, opened_at) VALUES (:id, :address.city, :address.street, :opened_at)")
			.params(ParameterTree.parametersOf(venueValidator.toMap(venue)))
			.update();
		// a row that was stored as a java.sql.Timestamp before is read with the others
		jdbcClient.sql("INSERT INTO venue (id, city, opened_at) VALUES (2, 'Osaka', ?)")
			.param(java.sql.Timestamp.from(venue.openedAt()))
			.update();
		assertThat(jdbcClient.sql("SELECT city FROM venue WHERE street IS NULL ORDER BY id").query(String.class).list())
			.containsExactly("Tokyo", "Osaka");

		// a row holds null for a column without a value, although the type of the map
		// that a validator reads does not say so
		@SuppressWarnings({ "unchecked", "NullAway" })
		final Function<String, List<Map<String, Object>>> rows = sql -> (List<Map<String, Object>>) (List<?>) jdbcClient
			.sql(sql)
			.query()
			.listOfRows();
		assertThat(rows.apply("SELECT * FROM event ORDER BY id").stream().map(eventValidator::validate))
			.containsExactlyElementsOf(events.stream().map(Validated::successWith).toList());
		assertThat(rows.apply("SELECT id, opened_at FROM venue ORDER BY id")
			.stream()
			.map(Yavi.reversibleSchema()
				._long(Yavi._long("opened_at").map(Instant::ofEpochMilli, Instant::toEpochMilli))
				.fromMapReversible()::validated))
			.containsExactly(venue.openedAt(), venue.openedAt());
	}

	record Account(Integer id, String name, String passwordHash) {
	}

	record Order(Integer id, String memo, Account user, List<Account> watchers) {
	}

	record Member(@Nullable Account account, String team) {
	}

	record Node(String name, String secret, List<Node> children) {
	}

	sealed interface Credential {

	}

	record Password(String value) implements Credential {
	}

	record ApiKey(String value, String label) implements Credential {
	}

	@Test
	void aWithheldPropertyIsWrittenOnlyWhereItIsNamed() {
		final ObjectInputValidator.Reversible<Account> accountValidator = Yavi.reversibleSchema()
			._integer("id")
			._string("name")
			._string("password_hash")
			.withheld()
			.label("Password hash")
			.fromMapReversible(Account::new, Account::id, Account::name, Account::passwordHash);
		final Account account = new Account(1, "Taro", "{bcrypt}x");
		// the reverse writes every property and is read back; the map that is shown is
		// without it, and so is a view that excludes. A view that includes writes it if
		// it is named
		assertThat(accountValidator.reverse(account)).containsExactly(entry("id", 1), entry("name", "Taro"),
				entry("password_hash", "{bcrypt}x"));
		assertThat(accountValidator.validate(accountValidator.reverse(account)))
			.isEqualTo(Validated.successWith(account));
		assertThat(accountValidator.toMap(account)).containsExactly(entry("id", 1), entry("name", "Taro"));
		assertThat(accountValidator.excluding("name").toMap(account)).containsExactly(entry("id", 1));
		final PropertyView<Account> credentials = accountValidator.including("id", "password_hash");
		assertThat(credentials.toMap(account)).containsExactly(entry("id", 1), entry("password_hash", "{bcrypt}x"));
		assertThat(credentials.excluding("password_hash").toMap(account)).containsExactly(entry("id", 1));
		// its accessor is not called where it is not written
		assertThat(Yavi.reversibleSchema()
			._integer("id")
			._string("name")
			._string("password_hash")
			.withheld()
			.fromMapReversible(Account::new, Account::id, Account::name, a -> {
				throw new IllegalStateException("not loaded");
			})
			.toMap(account)).containsExactly(entry("id", 1), entry("name", "Taro"));
		assertThatThrownBy(() -> accountValidator.excluding("password_hash"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The property 'password_hash' is withheld: a view writes it only if it includes it by its "
					+ "name, so excluding it has no effect.");
		assertThatThrownBy(() -> accountValidator.excluding("name").including("password_hash"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The property 'password_hash' is not written by the view that is made narrower: it is "
					+ "withheld, and written only by a view that includes it by its name.");

		// at any depth: of a nested object and of the elements of a list, whose name
		// stands for the properties that are not withheld
		final ObjectInputValidator.Reversible<Order> orderValidator = Yavi.reversibleSchema()
			._integer("id")
			._string("memo")
			._object("user", accountValidator)
			._list("watchers", accountValidator)
			.fromMapReversible(Order::new, Order::id, Order::memo, Order::user, Order::watchers);
		final Order order = new Order(7, "gift", account, List.of(new Account(2, "Hanako", "{bcrypt}y")));
		assertThat(orderValidator.excluding("memo").toMap(order)).containsExactly(entry("id", 7),
				entry("user", mapOf("id", 1, "name", "Taro")),
				entry("watchers", List.of(mapOf("id", 2, "name", "Hanako"))));
		assertThat(orderValidator.including("id", "user").toMap(order)).containsExactly(entry("id", 7),
				entry("user", mapOf("id", 1, "name", "Taro")));
		assertThat(orderValidator.toMap(order)).containsExactly(entry("id", 7), entry("memo", "gift"),
				entry("user", mapOf("id", 1, "name", "Taro")),
				entry("watchers", List.of(mapOf("id", 2, "name", "Hanako"))));
		assertThat(orderValidator.validate(orderValidator.reverse(order))).isEqualTo(Validated.successWith(order));
		// the path to it is given next to the object that has it, or alone
		assertThat(orderValidator.including("user", "user.password_hash", "watchers.password_hash").toMap(order))
			.containsExactly(entry("user", mapOf("id", 1, "name", "Taro", "password_hash", "{bcrypt}x")),
					entry("watchers", List.of(mapOf("password_hash", "{bcrypt}y"))));
		assertThatThrownBy(() -> orderValidator.including("user", "user.name"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The property 'user.name' is written as a part of 'user', which is given as a whole.");
		assertThatThrownBy(() -> orderValidator.excluding("watchers.password_hash"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageStartingWith("The property 'watchers.password_hash' is withheld");
		assertThatThrownBy(() -> orderValidator.excluding("memo").including("user", "user.password_hash"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageStartingWith("The property 'user.password_hash' is not written by the view");
		// of an embedded validator, also of one that may have no value
		final ObjectInputValidator.Reversible<Member> memberValidator = Yavi.reversibleSchema()
			._embedded(accountValidator)
			.nullable()
			._string("team")
			.fromMapReversible(Member::new, Member::account, Member::team);
		assertThat(memberValidator.excluding("id").toMap(new Member(account, "a")))
			.containsExactly(entry("name", "Taro"), entry("team", "a"));
		assertThat(memberValidator.including("team", "password_hash").toMap(new Member(null, "a")))
			.containsExactly(entry("password_hash", null), entry("team", "a"));
		// one property that is defined once, with its mark, is its own result
		final ObjectInputValidator.Reversible<String> secretValidator = Yavi.reversibleSchema()
			._string("secret", c -> c.notBlank())
			.withheld()
			.fromMapReversible();
		assertThat(secretValidator.reverse("s")).containsExactly(entry("secret", "s"));
		assertThat(secretValidator.toMap("s")).isEmpty();
		final ObjectInputValidator<List<Object>> noteValidator = Yavi.schema()
			._string("note")
			._embedded(secretValidator)
			.optional()
			.fromMap((note, secret) -> List.<Object>of(note, secret));
		assertThat(noteValidator.validated(mapOf("note", "n", "secret", "s"))).containsExactly("n", Optional.of("s"));
		assertThat(noteValidator.validated(mapOf("note", "n"))).containsExactly("n", Optional.empty());
		assertThat(ParameterTree.of(Map.of("note", "n", "secret", "s")).toMap(noteValidator.description()))
			.containsExactly(entry("note", "n"), entry("secret", ""));
		// and so is the rest alone: the map of its validated values
		final ObjectInputValidator.Reversible<Map<String, Integer>> scoresValidator = Yavi.reversibleSchema()
			._rest(Yavi._integer())
			.fromMapReversible();
		assertThat(scoresValidator.validated(mapOf("a", 1, "b", 2))).containsExactly(entry("a", 1), entry("b", 2));
		assertThat(scoresValidator.toMap(Map.of("a", 1))).containsExactly(entry("a", 1));
		assertThat(scoresValidator.validate(mapOf("a", "x")).errors()).extracting(violation -> violation.name())
			.containsExactly("a");
		// of a structure that contains itself, as deep as the written object goes
		final ObjectInputValidator.Reversible<Node> nodeValidator = ReversibleValueValidator
			.recursive(self -> Yavi.reversibleSchema()
				._string("name")
				._string("secret")
				.withheld()
				._list("children", self)
				.fromMapReversible(Node::new, Node::name, Node::secret, Node::children));
		final Node node = new Node("root", "r", List.of(new Node("leaf", "l", List.of())));
		assertThat(nodeValidator.excluding("name").toMap(node))
			.isEqualTo(mapOf("children", List.of(mapOf("name", "leaf", "children", List.of()))));
		assertThat(nodeValidator.including("name", "children.secret").toMap(node))
			.isEqualTo(mapOf("name", "root", "children", List.of(mapOf("secret", "l"))));
		// of a union, if an alternative that defines it withholds it; the discriminator
		// is always written, so it is not withheld
		final ObjectInputValidator.Reversible<Password> passwordValidator = Yavi.reversibleSchema()
			._string("value")
			.withheld()
			.fromMapReversible(Password::new, Password::value);
		final DiscriminatedUnionValidator.Reversible<Credential> credentialValidator = DiscriminatedUnionValidator
			.<Credential>of("kind")
			.when("password", Password.class, passwordValidator)
			.when("api_key", ApiKey.class,
					Yavi.reversibleSchema()
						._string("value")
						._string("label")
						.fromMapReversible(ApiKey::new, ApiKey::value, ApiKey::label));
		assertThat(credentialValidator.excluding("label").toMap(new ApiKey("k", "ci")))
			.containsExactly(entry("kind", "api_key"));
		assertThat(credentialValidator.toMap(new Password("p"))).containsExactly(entry("kind", "password"));
		assertThat(credentialValidator.reverse(new ApiKey("k", "ci"))).containsExactly(entry("kind", "api_key"),
				entry("value", "k"), entry("label", "ci"));
		assertThat(credentialValidator.including("value").toMap(new Password("p")))
			.containsExactly(entry("kind", "password"), entry("value", "p"));
		assertThatThrownBy(
				() -> DiscriminatedUnionValidator.<Credential>of("value").when("password", passwordValidator))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The alternative 'password' withholds the discriminator 'value', which tells the "
					+ "alternative of a written object and is always written.");
		// and of a schema whose properties are known at run time
		final ObjectInputValidator.Reversible<Map<String, Object>> dynamicValidator = Yavi
			.<Object>reversibleDynamicSchema()
			.property("token", Yavi._string())
			.withheld()
			.property("credential", credentialValidator)
			.build();
		assertThat(dynamicValidator.excluding("credential.label")
			.toMap(mapOf("token", "t", "credential", new Password("p"))))
			.containsExactly(entry("credential", mapOf("kind", "password")));

		// a form that is shown again does not hold what was sent for it, although the
		// validator, which does not have to be reversible, reads it
		final ValueValidator<Map<String, ?>, List<Object>> signupValidator = Yavi.schema()
			._string("name")
			._string("password", c -> c.greaterThanOrEqual(8))
			.withheld()
			._object("account", accountValidator)
			.fromMap((name, password, owner) -> List.<Object>of(name, password, owner))
			.readingParameters();
		final Map<String, String> parameters = Map.of("name", "Taro", "password", "short", "account.id", "1",
				"account.name", "Taro", "account.password_hash", "x");
		assertThat(signupValidator.validate(parameters).errors()).extracting(violation -> violation.name())
			.containsExactly("password");
		assertThat(ParameterTree.of(parameters).toParameters(signupValidator.description())).containsExactly(
				entry("name", "Taro"), entry("password", ""), entry("account.id", "1"), entry("account.name", "Taro"),
				entry("account.password_hash", ""));
		assertThat(ParameterTree.ofValues(accountValidator.reverse(account)).toMap(accountValidator.description()))
			.containsExactly(entry("id", "1"), entry("name", "Taro"), entry("password_hash", ""));
		// its JSON Schema is writeOnly, and the arguments of a typed validator say it too
		assertThat(JsonSchema.of(accountValidator.description())).extractingByKey("properties")
			.isEqualTo(mapOf("id", mapOf("type", "integer"), "name", mapOf("type", "string"), "password_hash",
					mapOf("type", "string", "writeOnly", true)));
		assertThat(Yavi.schema()
			._string("name")
			._string("password")
			.withheld()
			.apply((name, password) -> name)
			.description())
			.isEqualTo(new ValueDescription.Properties(
					List.of(new ValueDescription.Property("name", Yavi._string("name").description()),
							new ValueDescription.Property("password", Yavi._string("password").description(), true))));
	}

}
