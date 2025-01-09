/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.constraint;

import am.ik.yavi.Color;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Predicate;

class EnumConstraintTest {

	private final EnumConstraint<Color, Color> enumConstraint = new EnumConstraint<>();

	@Test
	public void testIllegalOneOfArgument() {
		assertThrows(IllegalArgumentException.class, enumConstraint::oneOf);
	}

	@ParameterizedTest(name = "test oneOf(BLUE,RED) for {0} should return {1}")
	@CsvSource({ "BLUE,true", "GREEN,false" })
	void testIsOneOf(Color color, boolean expectedResult) {
		Predicate<Color> predicate = enumConstraint.oneOf(Color.BLUE, Color.RED).predicates().peekFirst().predicate();

		boolean actualResult = predicate.test(color);

		Truth.assertThat(actualResult).isEqualTo(expectedResult);
	}

}
