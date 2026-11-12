/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.PathSegment.Index;
import am.ik.yavi.core.PathSegment.Property;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

	static <T> String describe(Validated<T> validated) {
		return switch (validated) {
			case Validated.Valid < T > valid -> "valid " + valid.valueNullable();
			case Validated.Invalid < T > invalid -> "invalid " + invalid.errors().size();
		};
	}

	@Test
	void successWith() {
		final Validated<String> validated = Validated.successWith("OK");
		assertThat(validated.isValid()).isTrue();
		assertThat(validated.value()).isEqualTo("OK");
		assertThat(describe(validated)).isEqualTo("valid OK");
		assertThat(describe(Validated.<String>successWith(null))).isEqualTo("valid null");
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
		assertThat(StringValidatorBuilder.of("email", c -> c.notBlank().email())
			.build()
			.liftList()
			.validate(List.of("a@example.com", "b"))
			.errors()).extracting(ConstraintViolation::path)
			.containsExactly(List.of(new Property("email"), new Index(1)));
	}

	@Test
	void failureWith() {
		final Validated<Object> validated = Validated
			.failureWith(ConstraintViolation.builder().name("name").message("\"{0}\" must not be blank."));
		assertThat(validated.isValid()).isFalse();
		assertThat(validated.errors()).hasSize(1);
		assertThat(validated.errors().get(0).message()).isEqualTo("\"name\" must not be blank.");
		assertThat(describe(validated)).isEqualTo("invalid 1");
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