/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Arrays;
import java.util.List;

import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.PathSegment.Index;
import am.ik.yavi.core.PathSegment.Property;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParseResultTest {

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

	static String describe(ParseResult<Order> result) {
		return switch (result) {
			case ParseResult.Success<Order>(var order) -> "success " + order.id();
			case ParseResult.Failure<Order>(var issues) -> "failure " + issues.size();
		};
	}

	@Test
	void success() {
		ParseResult<Order> result = orderValidator.applicative()
			.validate(new Order("1", new Address("street", null), List.of(new Item("item", List.of("tag")))))
			.toParseResult();
		assertThat(describe(result)).isEqualTo("success 1");
		assertThat(Validated.<String>successWith(null).toParseResult()).isEqualTo(new ParseResult.Success<>(null));
	}

	@Test
	void failureWithNestedAndIndexedPaths() {
		Validated<Order> validated = orderValidator.applicative()
			.validate(new Order("", new Address("", null),
					Arrays.asList(new Item("item", List.of("tag", "")), null, new Item("", List.of()))));
		ParseResult<Order> result = validated.toParseResult();
		assertThat(describe(result)).isEqualTo("failure 6");
		assertThat(validated.errors()).extracting(ConstraintViolation::name)
			.containsExactly("order/id", "address/street", "items[0]/tags[1]", "items[1]", "items[2]/name",
					"address/country");
		if (!(result instanceof ParseResult.Failure<Order>(var issues))) {
			throw new AssertionError(result);
		}
		assertThat(issues).extracting(Issue::path)
			.containsExactly(List.of(new Property("order/id")),
					List.of(new Property("address"), new Property("street")),
					List.of(new Property("items"), new Index(0), new Property("tags"), new Index(1)),
					List.of(new Property("items"), new Index(1)),
					List.of(new Property("items"), new Index(2), new Property("name")),
					List.of(new Property("address"), new Property("country")));
		assertThat(issues).extracting(Issue::code)
			.containsExactly("charSequence.notBlank", "charSequence.notBlank", "charSequence.notBlank",
					"object.notNull", "charSequence.notBlank", "object.notNull");
		switch (issues.get(1)) {
			case Issue.Violation(var path, var code, var message, var args) -> {
				assertThat(message).isEqualTo("\"address/street\" must not be blank");
				assertThat(args).containsExactly("address/street", "");
			}
		}
		assertThat(orderValidator.prefixed("order/")
			.validate(new Order("1", new Address("", "JP"), List.of()))
			.issues()).extracting(Issue::path)
			.containsExactly(List.of(new Property("order"), new Property("address"), new Property("street")));
	}

	@Test
	void failureOfLiftedValueValidator() {
		ParseResult<List<String>> result = StringValidatorBuilder.of("email", c -> c.notBlank().email())
			.build()
			.liftList()
			.validate(List.of("a@example.com", "b"))
			.toParseResult();
		assertThat(result).isInstanceOfSatisfying(ParseResult.Failure.class,
				failure -> assertThat(((ParseResult.Failure<?>) failure).issues()).extracting(Issue::path, Issue::code)
					.containsExactly(org.assertj.core.groups.Tuple.tuple(List.of(new Property("email"), new Index(1)),
							"charSequence.email")));
	}

}
