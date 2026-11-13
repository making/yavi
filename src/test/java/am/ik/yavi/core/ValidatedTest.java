/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.PathSegment.Index;
import am.ik.yavi.core.PathSegment.Property;
import am.ik.yavi.fn.Validation;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class ValidatedTest {

	record Address(String street, @Nullable String country) {
	}

	record Item(String name, List<String> tags) {
	}

	record Order(String id, Address address, List<@Nullable Item> items) {
	}

	// A separator other than "." and a property name containing it prove that the
	// path is not derived from the flat name.
	static final Validator<Address> addressValidator = new ValidatorBuilder<Address>("/")
		._string(Address::street, "street", c -> c.notBlank())
		.constraintOnCondition((address, group) -> address.street().isBlank(),
				b -> b._string(Address::country, "country", c -> c.notNull()))
		.build();

	static final Validator<Order> orderValidator = new ValidatorBuilder<Order>("/")
		._string(Order::id, "order/id", c -> c.notBlank())
		.nest(Order::address, "address", addressValidator)
		.forEach(Order::items, "items",
				b -> b._string(Item::name, "name", c -> c.notBlank())
					.forEach(Item::tags, "tags", t -> t._string(tag -> tag, "", c -> c.notBlank())))
		.build();

	static <T extends @Nullable Object> String describe(Validated<T> validated) {
		return switch (validated) {
			case Validated.Valid<T>(var value) -> "valid " + value;
			case Validated.Invalid<T>(var violations) -> "invalid " + violations.size();
		};
	}

	@Test
	void successWith() {
		final Validated<String> validated = Validated.successWith("OK");
		assertThat(validated.isValid()).isTrue();
		assertThat(validated.value()).isEqualTo("OK");
		assertThat(describe(validated)).isEqualTo("valid OK");
		assertThat(describe(Validated.<@Nullable String>successWith(null))).isEqualTo("valid null");
	}

	@Test
	void pathsOfNestedAndIndexedViolations() {
		Validated<Order> validated = orderValidator.applicative()
			.validate(new Order("", new Address("", null),
					Arrays.asList(new Item("item", List.of("tag", "")), null, new Item("", List.of()))));
		assertThat(describe(validated)).isEqualTo("invalid 6");
		assertThat(validated.errors()).extracting(ConstraintViolation::name)
			.containsExactly("order/id", "address/street", "items[0]/tags[1]", "items[1]", "items[2]/name",
					"address/country");
		assertThat(validated.errors()).extracting(ConstraintViolation::path)
			.containsExactly(List.of(new Property("order/id")),
					List.of(new Property("address"), new Property("street")),
					List.of(new Property("items"), new Index(0), new Property("tags"), new Index(1)),
					List.of(new Property("items"), new Index(1)),
					List.of(new Property("items"), new Index(2), new Property("name")),
					List.of(new Property("address"), new Property("country")));
		assertThat(validated.errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("charSequence.notBlank", "charSequence.notBlank", "charSequence.notBlank",
					"object.notNull", "charSequence.notBlank", "object.notNull");
		assertThat(orderValidator.prefixed("order/").validate(new Order("1", new Address("", "JP"), List.of())))
			.extracting(ConstraintViolation::path)
			.containsExactly(List.of(new Property("order"), new Property("address"), new Property("street")));
		// the violations of a lifted element are located under its index
		final Order valid = new Order("1", new Address("a", "JP"), List.of());
		final ConstraintViolations lifted = orderValidator.applicative()
			.liftList()
			.prefixed("orders")
			.validate(List.of(valid, new Order("1", new Address("", "JP"), List.of())))
			.errors();
		assertThat(lifted).extracting(ConstraintViolation::name).containsExactly("orders[1]/address/street");
		assertThat(lifted).extracting(violation -> violation.args()[0]).containsExactly("orders[1]/address/street");
		assertThat(lifted).extracting(ConstraintViolation::path)
			.containsExactly(
					List.of(new Property("orders"), new Index(1), new Property("address"), new Property("street")));
		final ValueValidator<Iterable<String>, Set<String>> emails = StringValidatorBuilder
			.of("email", c -> c.notBlank().email())
			.build()
			.liftSet()
			.prefixed("emails");
		assertThat(emails.validate(List.of("a@example.com", "b")).errors()).extracting(ConstraintViolation::name)
			.containsExactly("emails[1].email");
		final ConstraintViolations untyped = emails.readingWith(InputReader.ofJdkTypes())
			.validate(List.of("a@example.com", 1, "b"))
			.errors();
		assertThat(untyped).extracting(ConstraintViolation::name, ConstraintViolation::messageKey)
			.containsExactly(tuple("emails[1].email", "object.type"), tuple("emails[2].email", "charSequence.email"));
		assertThat(untyped.get(0).path()).containsExactly(new Property("emails"), new Index(1), new Property("email"));
	}

	@Test
	void failureWith() {
		final Validated<Object> validated = Validated
			.failureWith(ConstraintViolation.builder().name("name").message("\"{0}\" must not be blank."));
		assertThat(validated.isValid()).isFalse();
		assertThat(validated.errors()).hasSize(1);
		assertThat(validated.errors().get(0).message()).isEqualTo("\"name\" must not be blank.");
		assertThat(describe(validated)).isEqualTo("invalid 1");
		// the short form of a violation on the validated value itself
		final ViolationMessage outOfRange = ViolationMessage.of("port.range", "\"{0}\" must be in {1}-{2} but was {3}");
		final Validated<Integer> port = Validated.failureWith(outOfRange, 0, Locale.ENGLISH, 1, 999);
		final ConstraintViolation violation = port.errors().get(0);
		assertThat(violation.name()).isEmpty();
		assertThat(violation.path()).isEmpty();
		assertThat(violation.messageKey()).isEqualTo("port.range");
		assertThat(violation.violatedValue()).isEqualTo(0);
		assertThat(violation.locale()).isEqualTo(Locale.ENGLISH);
		assertThat(port.prefixed("port").errors().get(0).message()).isEqualTo("\"port\" must be in 1-999 but was 0");
		assertThat(ConstraintViolation.of(outOfRange, null, Locale.ENGLISH, 1, 999).violatedValue()).isNull();
	}

	@Test
	void mapFlatMapAndCombineReturnValidated() {
		// usable as an argument and with var, without a target type
		final Validated<String> validated = Validated.successWith("80");
		assertThat(describe(validated.map(Integer::valueOf))).isEqualTo("valid 80");
		assertThat(describe(validated.flatMap(s -> Validated.successWith(s.length())))).isEqualTo("valid 2");
		final var failed = validated.<Integer>flatMap(
				s -> Validated.failureWith(ViolationMessage.of("port.invalid", "invalid"), s, Locale.ENGLISH));
		assertThat(describe(failed)).isEqualTo("invalid 1");
		assertThat(failed.map(i -> i + 1)).isSameAs(failed);
		assertThat(failed.flatMap(i -> Validated.successWith(i + 1))).isSameAs(failed);
		// a validation that is not a Validated is converted
		assertThat(validated.flatMap(s -> Validation.<ConstraintViolation, Integer>success(s.length())))
			.isEqualTo(Validated.successWith(2));
		// and so is one that is combined with a Validated
		assertThat(describe(validated.combine(Validation.<ConstraintViolation, Integer>success(1))
			.combine(failed)
			.apply((s, i, j) -> s + i + j))).isEqualTo("invalid 1");
		assertThat(
				describe(validated.combine(Validation.<ConstraintViolation, Integer>success(1)).apply((s, i) -> s + i)))
			.isEqualTo("valid 801");
	}

	@Test
	void testFailureWith() {
		final Validated<Object> validated = Validated.failureWith(Collections
			.singletonList(ConstraintViolation.builder().name("name").message("\"{0}\" must not be blank.")));
		assertThat(validated.isValid()).isFalse();
		assertThat(validated.errors()).hasSize(1);
		assertThat(validated.errors().get(0).message()).isEqualTo("\"name\" must not be blank.");
	}

}