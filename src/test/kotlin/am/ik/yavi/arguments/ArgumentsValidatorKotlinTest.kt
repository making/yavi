/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments

import am.ik.yavi.builder.ArgumentsValidatorBuilder
import am.ik.yavi.builder._enum
import am.ik.yavi.builder._object
import am.ik.yavi.core.ConstraintViolationsException
import am.ik.yavi.validator.Yavi
import am.ik.yavi.validator._enum as yaviEnum
import am.ik.yavi.validator._object as yaviObject
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ArgumentsValidatorKotlinTest {
    data class Person(val firstName: String, val lastName: String, val age: Int)

    val validator: Arguments3Validator<String, String, Int, Person> = ArgumentsValidatorBuilder.of<String, String, Int, Person>(::Person)
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

    data class Member(val name: String, val nickname: String?, val age: Int)

    // an argument is required, so its value is not nullable unless nullable() says so
    val memberValidator: Arguments3Validator<String, String, Int, Member> = Yavi.schema()
            ._string("name") { it.notBlank() }
            ._string("nickname") { it.lessThanOrEqual(10) }.nullable()
            ._integer("age") { it.greaterThanOrEqual(0) }
            .apply { name, nickname, age -> Member(name.trim(), nickname?.trim(), age + 0) }

    @Test
    fun valid() {
        val person = validator.validated("John", "Doe", 20)
        assertThat(person.firstName).isEqualTo("John")
        assertThat(person.lastName).isEqualTo("Doe")
        assertThat(person.age).isEqualTo(20)
        assertThat(memberValidator.validated(" John ", null, 20)).isEqualTo(Member("John", null, 20))
        assertThat(memberValidator.validate("John", "jd", null).errors().map { it.message() })
                .containsExactly("\"age\" must not be null")
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

    enum class Role {
        ADMIN, MEMBER
    }

    data class Account(val role: Role, val id: UUID)

    data class ReversedAccount(val id: UUID, val role: Role)

    @Test
    fun reifiedSchemaValid() {
        val id = UUID.randomUUID()
        val withConstraints = Yavi.schema()
                ._enum<Role>("role", { it.oneOf(Role.ADMIN) })
                ._object<UUID>("id")
                .apply(::Account)
        assertThat(withConstraints.validated(Role.ADMIN, id)).isEqualTo(Account(Role.ADMIN, id))

        val withoutConstraints = Yavi.schema()
                ._enum<Role>("role")
                ._object<UUID>("id", { it.notNull() })
                .apply(::Account)
        assertThat(withoutConstraints.validated(Role.MEMBER, id)).isEqualTo(Account(Role.MEMBER, id))

        val reversed = Yavi.schema()
                ._object<UUID>("id")
                ._enum<Role>("role")
                .apply(::ReversedAccount)
        assertThat(reversed.validated(id, Role.MEMBER)).isEqualTo(ReversedAccount(id, Role.MEMBER))

        val reversedWithConstraints = Yavi.schema()
                ._object<UUID>("id")
                ._enum<Role>("role", { it.oneOf(Role.ADMIN) })
                .apply(::ReversedAccount)
        assertThat(reversedWithConstraints.validated(id, Role.ADMIN)).isEqualTo(ReversedAccount(id, Role.ADMIN))

        assertThat(yaviEnum<Role>("role") { it.oneOf(Role.ADMIN) }.validate(Role.ADMIN).value())
                .isEqualTo(Role.ADMIN)
        assertThat(yaviEnum<Role>("role").validate(Role.MEMBER).value()).isEqualTo(Role.MEMBER)
        assertThat(yaviEnum<Role> { it.oneOf(Role.ADMIN) }.validate(Role.ADMIN).value()).isEqualTo(Role.ADMIN)
        assertThat(yaviEnum<Role>().validate(Role.MEMBER).value()).isEqualTo(Role.MEMBER)
        assertThat(yaviObject<UUID>("id").validate(id).value()).isEqualTo(id)
        assertThat(yaviObject<UUID>("id", { it.notNull() }).validate(id).value()).isEqualTo(id)
        assertThat(yaviObject<UUID> { it.notNull() }.validate(id).value()).isEqualTo(id)
        assertThat(yaviObject<UUID>().validate(id).value()).isEqualTo(id)

        // the Java-style calls are still available
        assertThat(Yavi.schema()._enum("role", Role::class.java).apply { it }.validate(Role.ADMIN).isValid()).isTrue()
        assertThat(Yavi.schema()._object("id", UUID::class.java).apply { it }.validate(id).isValid()).isTrue()
    }

    @Test
    fun reifiedSchemaInvalid() {
        val id = UUID.randomUUID()
        val validator = Yavi.schema()
                ._enum<Role>("role", { it.oneOf(Role.ADMIN) })
                ._object<UUID>("id")
                .apply(::Account)
        assertThat(validator.validate(Role.MEMBER, id).errors().map { it.message() })
                .containsExactly("\"role\" must be one of the following values: [ADMIN]")

        assertThat(
                yaviEnum<Role>("role", { it.oneOf(Role.ADMIN) }).validate(Role.MEMBER).errors().map { it.message() })
                .containsExactly("\"role\" must be one of the following values: [ADMIN]")
        assertThat(
                yaviObject<UUID>("id", { it.equalTo(id) }).validate(UUID.randomUUID()).errors().map { it.message() })
                .containsExactly("\"id\" must be equal to $id")
    }
}
