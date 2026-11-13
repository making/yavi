/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.jsonschema;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Period;
import java.util.List;
import java.util.UUID;

import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.builder.BigDecimalValidatorBuilder;
import am.ik.yavi.builder.EnumValidatorBuilder;
import am.ik.yavi.builder.IntegerValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.validator.Yavi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSchemaTest {

	enum Color {

		RED, BLUE

	}

	@Test
	void constraintsAreTranslatedToKeywords() {
		final ValueValidator<String, String> code = StringValidatorBuilder
			.of("code", c -> c.notNull().greaterThanOrEqual(2).lessThan(10).pattern("[A-Z]+").uri())
			.build();
		final ValueValidator<Integer, Integer> seats = IntegerValidatorBuilder
			.of("seats", c -> c.greaterThan(0).lessThanOrEqual(8).multipleOf(2))
			.build()
			.recover(4);
		final ValueValidator<BigDecimal, BigDecimal> price = BigDecimalValidatorBuilder
			.of("price",
					c -> c.notNull()
						.positive()
						.multipleOf(new BigDecimal("0.05"))
						.fractionDigits(2)
						.multipleOf(new BigDecimal("0.25"))
						.integerDigits(6))
			.build();
		final ValueValidator<Color, Color> color = EnumValidatorBuilder
			.of("color", Color.class, c -> c.notNull().oneOf(List.of(Color.RED)))
			.build()
			.defaultValue(Color.BLUE);
		// compose changes the input, so the description is unknown unless it is supplied
		final ValueValidator<Object, String> composed = code.compose(String::valueOf);
		final ValueValidator<Object, String> described = composed
			.describedAs(new ValueDescription.Value("flag", Boolean.class, List.of()));
		assertThat(composed.description()).isEqualTo(new ValueDescription.Unknown());
		// the arguments are named after the values of their validators
		final ValueDescription description = ArgumentsValidators.split(code, seats, price, color, composed, described)
			.apply((a1, a2, a3, a4, a5, a6) -> "")
			.description();
		assertThat(JsonSchema.toJson(description)).isEqualTo("""
				{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{\
				"code":{"type":"string","minLength":2,"maxLength":9,"pattern":"^(?:[A-Z]+)$","format":"uri"},\
				"seats":{"type":["integer","null"],"exclusiveMinimum":0,"maximum":8,"multipleOf":2,"default":4},\
				"price":{"type":"number","exclusiveMinimum":0,"multipleOf":0.25,"exclusiveMaximum":1000000},\
				"color":{"type":["string","null"],"enum":["RED",null],"default":"BLUE"},\
				"arg5":{},\
				"flag":{"type":["boolean","null"]}},\
				"required":["code","price"]}""");
		// a type that is read from its string form is a string with a format
		assertThat(JsonSchema.toJson(Yavi.schema()
			._object("id", UUID.class)
			._object("site", URI.class)
			._object("ttl", Duration.class)
			._object("term", Period.class)
			.defaultValue(Period.ofMonths(1))
			.fromMap((id, site, ttl, term) -> id)
			.description())).isEqualTo("""
					{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{\
					"id":{"type":"string","format":"uuid"},\
					"site":{"type":"string","format":"uri-reference"},\
					"ttl":{"type":"string","format":"duration"},\
					"term":{"type":["string","null"],"format":"duration","default":"P1M"}},\
					"required":["id","site","ttl"]}""");
	}

}
