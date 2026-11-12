/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.jsonschema;

import java.math.BigDecimal;
import java.util.List;

import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.builder.BigDecimalValidatorBuilder;
import am.ik.yavi.builder.EnumValidatorBuilder;
import am.ik.yavi.builder.IntegerValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSchemaTest {

	enum Color {

		RED, BLUE

	}

	@Test
	void constraintsAreTranslatedToKeywords() {
		final ValueValidator<String, String> code = StringValidatorBuilder
			.of("code", c -> c.notNull().greaterThanOrEqual(2).lessThan(10).pattern("[A-Z]+"))
			.build();
		final ValueValidator<Integer, Integer> seats = IntegerValidatorBuilder
			.of("seats", c -> c.greaterThan(0).lessThanOrEqual(8))
			.build()
			.recover(4);
		final ValueValidator<BigDecimal, BigDecimal> price = BigDecimalValidatorBuilder
			.of("price", c -> c.notNull().positive().fractionDigits(2).integerDigits(6))
			.build();
		final ValueValidator<Color, Color> color = EnumValidatorBuilder
			.<Color>of("color", c -> c.notNull().oneOf(List.of(Color.RED)))
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
				"code":{"type":"string","minLength":2,"maxLength":9,"pattern":"^(?:[A-Z]+)$"},\
				"seats":{"type":["integer","null"],"exclusiveMinimum":0,"maximum":8,"default":4},\
				"price":{"type":"number","exclusiveMinimum":0,"multipleOf":0.01,"exclusiveMaximum":1000000},\
				"color":{"type":["string","null"],"enum":["RED",null],"default":"BLUE"},\
				"arg5":{},\
				"flag":{"type":["boolean","null"]}},\
				"required":["code","price"]}""");
	}

}
