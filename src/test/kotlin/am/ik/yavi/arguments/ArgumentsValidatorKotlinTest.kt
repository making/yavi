/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.arguments

import am.ik.yavi.builder.ArgumentsValidatorBuilder
import am.ik.yavi.core.ConstraintViolationsException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class ArgumentsValidatorKotlinTest {
    data class Person(val firstName: String?, val lastName: String?, val age: Int?)

    val validator: Arguments3Validator<String?, String?, Int?, Person> = ArgumentsValidatorBuilder.of(::Person)
            .builder { b ->
                b._string({ it.arg1() }, "firstName") {
                    it.notEmpty().lessThanOrEqual(10)
                }._string({ it.arg2() }, "lastName") {
                    it.notEmpty().lessThanOrEqual(10)
                }._integer({ it.arg3() }, "age") {
                    it.notNull().greaterThanOrEqual(1).lessThanOrEqual(99)
                }
            }
            .build()

    @Test
    fun valid() {
        val person = validator.validated("John", "Doe", 20)
        assertThat(person.firstName).isEqualTo("John")
        assertThat(person.lastName).isEqualTo("Doe")
        assertThat(person.age).isEqualTo(20)
    }

    @Test
    fun invalid() {
        assertThatThrownBy { validator.validated("", "", 0) }
                .isInstanceOfSatisfying(ConstraintViolationsException::class.java
                ) {
                    assertThat(it.message).isEqualTo("Constraint violations found!" + System.lineSeparator()
                            + "* \"firstName\" must not be empty" + System.lineSeparator()
                            + "* \"lastName\" must not be empty" + System.lineSeparator()
                            + "* \"age\" must be greater than or equal to 1")
                }
    }
}