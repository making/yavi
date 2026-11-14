/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import am.ik.yavi.core.Presence;
import am.ik.yavi.core.ReversibleValueValidator;
import am.ik.yavi.core.StringCoercion;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import am.ik.yavi.validator.Yavi;
import org.h2.jdbcx.JdbcDataSource;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.core.simple.JdbcClient;

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

	static final ObjectInputValidator.Reversible<UserName> userNameValidator = Yavi.schema()
		._string("first_name", c -> c.notBlank())
		._string("last_name", c -> c.notBlank())
		.fromMapReversible(UserName::new, UserName::first, UserName::last);

	static final ObjectInputValidator.Reversible<Department> departmentValidator = Yavi.schema()
		._string("dept_name", c -> c.notBlank())
		._string("dept_code", c -> c.notBlank())
		.flatFromMapReversible((name, code) -> Validated.successWith(new Department(name, code)), Department::name,
				Department::code)
		.rejectingUnknownProperties();

	static final ObjectInputValidator.Reversible<Address> addressValidator = Yavi.schema()
		._string("city", c -> c.notBlank())
		._string("street")
		.optional()
		.fromMapReversible(Address::new, Address::city, Address::street)
		.constraintOnTarget(address -> !address.city().equals("nowhere"), "city", ViolationMessage.of("city", "!"));

	static final ObjectInputValidator.Reversible<Employee> employeeValidator = Yavi.schema()
		._integer("id")
		._embedded(userNameValidator)
		._embedded(departmentValidator)
		.nullable()
		._string(emailValidator)
		._string("nickname")
		.optional()
		._enum("role", Role.class)
		.defaultValue(Role.USER)
		._localDate("joined")
		._list("tags", Yavi._string(c -> c.notBlank()))
		.splitBy(",")
		.defaultValue(List.of())
		._list("scores", Yavi._integer())
		.splitBy(Pattern.compile("\\s+"))
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
		final Map<String, Object> map = employeeValidator.toMap(employee);
		// the properties in their order, embedded ones and the rest in the same map
		assertThat(map).containsExactly(entry("id", 1), entry("first_name", "Taro"), entry("last_name", "Yamada"),
				entry("dept_name", "Sales"), entry("dept_code", "S1"), entry("email", "taro@example.com"),
				entry("nickname", "taro"), entry("role", "ADMIN"), entry("joined", LocalDate.of(2024, 4, 1)),
				entry("tags", "java,yavi"), entry("scores", List.of(1, 2)),
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
				entry("tags", null), entry("scores", List.of()), entry("addresses", List.of()),
				entry("rates", Map.of()), entry("active", false));
		assertThat(employeeValidator.validate(written)).isEqualTo(Validated.successWith(empty));
		final Employee cleared = new Employee(2, empty.name(), null, empty.email(), Optional.empty(), Role.USER,
				empty.joined(), List.of(), List.of(), List.of(), Map.of(), Presence.presentNull(), null, false,
				Map.of());
		assertThat(employeeValidator.toMap(cleared)).containsEntry("memo", null);
		assertThat(employeeValidator.validate(employeeValidator.toMap(cleared)))
			.isEqualTo(Validated.successWith(cleared));

		// the map is the caller's, and an object that the validator does not create is
		// rejected
		written.put("updated_by", "admin");
		assertThatThrownBy(() -> employeeValidator.toMap(
				new Employee(1, employee.name(), null, employee.email(), Optional.empty(), Role.USER, employee.joined(),
						List.of(), List.of(), List.of(), Map.of(), Presence.absent(), null, true, Map.of("id", 9))))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("The property 'id' is written twice.");

		// a validator that is derived without changing the value still goes back
		assertThat(emailValidator.reverse(emailValidator.validated(" taro@example.com ")))
			.isEqualTo("taro@example.com");
		assertThat(emailValidator.named("contact").reverse(new Email("a@example.com"))).isEqualTo("a@example.com");
		assertThat(emailValidator.optional().reversible().orElseThrow().reverse(Optional.empty())).isNull();
		assertThat(Yavi._integer("page").defaultValue(1).named("p").reversible().orElseThrow().reverse(2)).isEqualTo(2);
		assertThat(Yavi._string("name").map(String::length).reversible()).isEmpty();
		final ValueValidator<String, String> lambda = (value, locale, context) -> Validated
			.successWith(String.valueOf(value));
		assertThat(lambda.reversible()).isEmpty();
		assertThat(ReversibleValueValidator.of(lambda, value -> value).reverse("a")).isEqualTo("a");
	}

	static Stream<Arguments> irreversible() {
		final ValueValidator<Map<String, Object>, UserName> plain = Yavi.schema()
			._string("first_name")
			._string("last_name")
			.fromMap(UserName::new);
		return Stream.of(
				Arguments.of((Supplier<Object>) () -> Yavi.schema()
					._string(Yavi._string("email").map(Email::new))
					.fromMapReversible(email -> email, email -> email), "The property 'email'"),
				Arguments.of(
						(Supplier<Object>) () -> Yavi.schema()
							._string("id")
							._embedded(plain)
							.fromMapReversible((id, name) -> name, name -> "", name -> name),
						"The embedded validator at position 2"),
				Arguments.of((Supplier<Object>) () -> Yavi.schema()
					._list("names", plain)
					.fromMapReversible(names -> names, names -> names), "The property 'names'"),
				Arguments.of((Supplier<Object>) () -> Yavi.schema()
					._object("name", plain)
					.optional()
					.fromMapReversible(name -> name, name -> name), "The property 'name'"),
				Arguments.of((Supplier<Object>) () -> Yavi.schema()
					._rest(Yavi._string().map(Email::new))
					.fromMapReversible(rest -> rest, rest -> rest), "The rest of the properties"));
	}

	@ParameterizedTest
	@MethodSource("irreversible")
	void aPropertyThatDoesNotKnowItsReverseIsRejectedWhenTheValidatorIsBuilt(Supplier<Object> schema, String part) {
		assertThatThrownBy(schema::get).isInstanceOf(IllegalStateException.class)
			.hasMessageStartingWith(part + " cannot be written back");
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
		final ObjectInputValidator.Reversible<Circle> circleValidator = Yavi.schema()
			._double("radius")
			.fromMapReversible(Circle::new, Circle::radius);
		final ObjectInputValidator.Reversible<Square> squareValidator = Yavi.schema()
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
		final ObjectInputValidator.Reversible<Drawing> drawingValidator = Yavi.schema()
			._string("title")
			._embedded(shapeValidator)
			._list("others", shapeValidator)
			.fromMapReversible(Drawing::new, Drawing::title, Drawing::shape, Drawing::others);
		final Drawing drawing = new Drawing("a", new Circle(1.0), List.of(new Square(2.0, "square")));
		assertThat(drawingValidator.toMap(drawing)).containsExactly(entry("title", "a"), entry("type", "circle"),
				entry("radius", 1.0), entry("others", List.of(mapOf("side", 2.0, "type", "square"))));
		assertThat(drawingValidator.validate(drawingValidator.toMap(drawing)))
			.isEqualTo(Validated.successWith(drawing));
		// an alternative without its type gives a union that does not write
		final DiscriminatedUnionValidator<Shape> reading = shapeValidator.when("other", circleValidator);
		assertThat(reading.reversible()).isEmpty();

		final ObjectInputValidator.Reversible<Tree> treeValidator = ValueValidator.recursive(self -> Yavi.schema()
			._string("name")
			._list("children", self)
			.defaultValue(List.of())
			.fromMapReversible(Tree::new, Tree::name, Tree::children));
		final Tree tree = new Tree("root", List.of(new Tree("leaf", List.of())));
		assertThat(treeValidator.toMap(tree))
			.isEqualTo(mapOf("name", "root", "children", List.of(mapOf("name", "leaf", "children", List.of()))));
		assertThat(treeValidator.validate(treeValidator.toMap(tree))).isEqualTo(Validated.successWith(tree));

		// a schema whose properties are known at run time writes the map it produces
		final ObjectInputValidator.Reversible<Map<String, Object>> dynamicValidator = Yavi.<Object>dynamicSchema()
			.property("email", emailValidator)
			.property("shape", shapeValidator)
			.property("memo", Yavi._string().nullable())
			.presence()
			.rest(Yavi._integer())
			.buildReversible();
		final Map<String, Object> values = mapOf("email", new Email("a@example.com"), "shape", new Circle(1.0), "n", 1);
		assertThat(dynamicValidator.toMap(values)).containsExactly(entry("email", "a@example.com"),
				entry("shape", mapOf("type", "circle", "radius", 1.0)), entry("n", 1));
		assertThat(dynamicValidator.validate(dynamicValidator.toMap(values))).isEqualTo(Validated.successWith(values));
	}

	record Signup(String name, Integer age, Role role, Boolean agreed, LocalDate birthday, List<String> tags,
			Address address, List<Address> others) {
	}

	@Test
	void aStoredObjectIsShownInAFormAsTheParametersThatAreSentForIt() {
		final ObjectInputValidator.Reversible<Signup> signupSchema = Yavi.schema()
			._string("name", c -> c.notBlank())
			._integer("age")
			._enum("role", Role.class)
			._boolean("agreed")
			.defaultValue(false)
			._localDate("birthday")
			._list("tags", Yavi._string())
			.splitBy(",")
			.defaultValue(List.of())
			._object("address", addressValidator)
			._list("others", addressValidator)
			.defaultValue(List.of())
			.fromMapReversible(Signup::new, Signup::name, Signup::age, Signup::role, Signup::agreed, Signup::birthday,
					Signup::tags, Signup::address, Signup::others);
		final ValueValidator<Map<String, ?>, Signup> signupValidator = signupSchema
			.readingParameters(StringCoercion.strict().trimming());
		final Signup stored = new Signup("Taro", 30, Role.ADMIN, false, LocalDate.of(2000, 1, 2), List.of("a", "b"),
				new Address("Tokyo", Optional.empty()), List.of(new Address("Osaka", Optional.of("2-2"))));

		final ParameterTree tree = ParameterTree.ofValues(signupSchema.toMap(stored));
		// the values as text in the shape of the description, like the parameters that
		// were sent: no entry for a checkbox that is not checked, an empty string for a
		// field without a value
		assertThat(tree.toMap(signupSchema.description())).containsExactly(entry("name", "Taro"), entry("age", "30"),
				entry("role", "ADMIN"), entry("birthday", "2000-01-02"), entry("tags", "a,b"),
				entry("address", mapOf("city", "Tokyo", "street", "")),
				entry("others", List.of(mapOf("city", "Osaka", "street", "2-2"))));
		final Map<String, Object> parameters = tree.toParameters(signupSchema.description());
		assertThat(parameters).containsExactly(entry("name", "Taro"), entry("age", "30"), entry("role", "ADMIN"),
				entry("birthday", "2000-01-02"), entry("tags", "a,b"), entry("address.city", "Tokyo"),
				entry("address.street", ""), entry("others[0].city", "Osaka"), entry("others[0].street", "2-2"));
		assertThat(signupValidator.validate(parameters)).isEqualTo(Validated.successWith(stored));
		final Signup agreed = new Signup("Taro", 30, Role.ADMIN, true, stored.birthday(), List.of(), stored.address(),
				List.of());
		assertThat(ParameterTree.ofValues(signupSchema.toMap(agreed)).toMap(signupSchema.description()))
			.containsEntry("agreed", "true")
			.containsEntry("tags", "")
			.containsEntry("others", List.of());
		assertThat(StringCoercion.textOf(new BigDecimal("1E+3"))).isEqualTo("1000");
	}

	record Event(Integer id, UserName owner, Optional<String> note, Role role, LocalDate day, LocalTime start,
			LocalDateTime createdAt, BigDecimal price, Boolean open) {
	}

	@Test
	void anObjectIsWrittenAsTheParametersOfAStatementAndReadFromItsRow() {
		final ObjectInputValidator.Reversible<Event> eventValidator = Yavi.schema()
			._integer("id")
			._embedded(userNameValidator)
			._string("note")
			.optional()
			._enum("role", Role.class)
			._localDate("event_day")
			._localTime("start_time")
			._localDateTime("created_at")
			._bigDecimal("price")
			._boolean("open")
			.fromMapReversible(Event::new, Event::id, Event::owner, Event::note, Event::role, Event::day, Event::start,
					Event::createdAt, Event::price, Event::open);
		final JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL("jdbc:h2:mem:reversible;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
		final JdbcClient jdbcClient = JdbcClient.create(dataSource);
		jdbcClient
			.sql("CREATE TABLE event (id INT PRIMARY KEY, first_name VARCHAR(32) NOT NULL, last_name VARCHAR(32) NOT NULL, "
					+ "note VARCHAR(32), role VARCHAR(8) NOT NULL, event_day DATE NOT NULL, start_time TIME NOT NULL, "
					+ "created_at TIMESTAMP NOT NULL, price DECIMAL(10, 2) NOT NULL, open BOOLEAN NOT NULL)")
			.update();
		final List<Event> events = List.of(
				new Event(1, new UserName("Taro", "Yamada"), Optional.of("memo"), Role.ADMIN,
						LocalDate.of(2026, 11, 14), LocalTime.of(9, 30), LocalDateTime.of(2026, 11, 1, 12, 0, 5),
						new BigDecimal("1200.50"), true),
				new Event(2, new UserName("Hanako", "Sato"), Optional.empty(), Role.USER, LocalDate.of(2026, 12, 1),
						LocalTime.of(18, 0), LocalDateTime.of(2026, 11, 2, 8, 0), new BigDecimal("0.00"), false));
		for (Event event : events) {
			assertThat(jdbcClient
				.sql("INSERT INTO event (id, first_name, last_name, note, role, event_day, start_time, created_at, price, open) "
						+ "VALUES (:id, :first_name, :last_name, :note, :role, :event_day, :start_time, :created_at, :price, :open)")
				.params(eventValidator.toMap(event))
				.update()).isEqualTo(1);
		}
		// a nested object is bound under the names of its violations
		record Venue(Integer id, Address address) {
		}
		final ObjectInputValidator.Reversible<Venue> venueValidator = Yavi.schema()
			._integer("id")
			._object("address", addressValidator)
			.fromMapReversible(Venue::new, Venue::id, Venue::address);
		jdbcClient.sql("CREATE TABLE venue (id INT PRIMARY KEY, city VARCHAR(32) NOT NULL, street VARCHAR(32))")
			.update();
		jdbcClient.sql("INSERT INTO venue (id, city, street) VALUES (:id, :address.city, :address.street)")
			.params(ParameterTree
				.parametersOf(venueValidator.toMap(new Venue(1, new Address("Tokyo", Optional.empty())))))
			.update();
		assertThat(jdbcClient.sql("SELECT city FROM venue WHERE street IS NULL").query(String.class).single())
			.isEqualTo("Tokyo");

		// a row holds null for a column without a value, although the type of the map
		// that a validator reads does not say so
		@SuppressWarnings({ "unchecked", "NullAway" })
		final List<Map<String, Object>> rows = (List<Map<String, Object>>) (List<?>) jdbcClient
			.sql("SELECT * FROM event ORDER BY id")
			.query()
			.listOfRows();
		assertThat(rows.stream().map(eventValidator::validate))
			.containsExactlyElementsOf(events.stream().map(Validated::successWith).toList());
	}

}
