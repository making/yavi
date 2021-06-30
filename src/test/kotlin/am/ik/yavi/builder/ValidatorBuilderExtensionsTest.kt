/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.builder

import am.ik.yavi.core.ConstraintGroup
import am.ik.yavi.core.Group
import am.ik.yavi.core.Validator
import org.assertj.core.api.Assertions
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger


data class DemoString(var x: String?)
data class DemoStringNotNullable(var x: String)
data class DemoBoolean(var x: Boolean?)
data class DemoInt(var x: Int?)
data class DemoChar(val x: Char?)
data class DemoByte(val x: Byte?)
data class DemoShort(val x: Short?)
data class DemoLong(val x: Long?)
data class DemoFloat(val x: Float?)
data class DemoBigInteger(val x: BigInteger?)
data class DemoBigDecimal(val x: BigDecimal?)
data class DemoCollection(val x: Collection<String>?)
data class DemoMap(val x: Map<String, String>?)
data class DemoArray(val x: Array<String>?)
data class DemoBooleanArray(val x: BooleanArray?)
data class DemoCharArray(val x: CharArray?)
data class DemoByteArray(val x: ByteArray?)
data class DemoShortArray(val x: ShortArray?)
data class DemoIntArray(val x: IntArray?)
data class DemoLongArray(val x: LongArray?)
data class DemoFloatArray(val x: FloatArray?)
data class DemoDoubleArray(val x: DoubleArray?)
data class DemoNested(val x: DemoString)
data class DemoNestedIfPresent(val x: DemoString?)
data class DemoForEachCollection(val x: Collection<DemoString>)
data class DemoForEachIfPresentCollection(val x: Collection<DemoString>?)
data class DemoForEachMap(val x: Map<String, DemoString>)
data class DemoForEachIfPresentMap(val x: Map<String, DemoString>?)
data class DemoForEachArray(val x: Array<DemoString>)
data class DemoForEachIfPresentArray(val x: Array<DemoString>?)

class ValidatorBuilderExtensionsTest {
    private val demoStringValidator = validator<DemoString> {
        DemoString::x {
            greaterThan(1)
            lessThan(5)
        }
    }

    @Test
    fun konstraintOnCharSequence() {
        val validator = validator<DemoString> {
            DemoString::x {
                notEmpty()
                lessThan(5)
            }
        }

        var demo = DemoString("foo")
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoString(null)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        var violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must not be empty""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.notEmpty")

        demo = DemoString("")
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must not be empty""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.notEmpty")

        demo = DemoString("foofoo")
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnCharSequenceIfPresent() {
        val validator = validator<DemoString> {
            DemoString::x {
                lessThan(5)
            }
        }

        var demo = DemoString("foo")
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoString(null)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoString("")
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoString("foofoo")
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        var violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnCharSequenceNotNullable() {
        val validator = validator<DemoStringNotNullable> {
            DemoStringNotNullable::x {
                notEmpty()
                lessThan(5)
            }
        }

        var demo = DemoStringNotNullable("foo")
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoStringNotNullable("")
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        var violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must not be empty""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.notEmpty")

        demo = DemoStringNotNullable("foofoo")
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnCharSequenceRequired() {
        val validator = validator<DemoString> {
            DemoString::x required {
                lessThan(5)
            }
        }

        var demo = DemoString("foo")
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoString(null)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        var violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must not be null""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("object.notNull")

        demo = DemoString("foofoo")
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnBoolean() {
        val validator = ValidatorBuilder.of<DemoBoolean>()
                .konstraint(DemoBoolean::x) { isTrue }
                .build()

        var demo = DemoBoolean(true)
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoBoolean(false)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must be true""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("boolean.isTrue")
    }

    @Test
    fun konstraintOnInteger() {
        val validator = ValidatorBuilder.of<DemoInt>()
                .konstraint(DemoInt::x) { greaterThan(1).lessThan(10) }
                .build()

        var demo = DemoInt(5)
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoInt(100)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must be less than 10""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("numeric.lessThan")
    }

    @Test
    fun konstraintOnChar() {
        val validator = ValidatorBuilder.of<DemoChar>()
                .konstraint(DemoChar::x) { greaterThan('a').lessThan('z') }
                .build()

        var demo = DemoChar('c')
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoChar('A')
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must be greater than a""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("numeric.greaterThan")
    }

    @Test
    fun konstraintOnByte() {
        val validator = ValidatorBuilder.of<DemoByte>()
                .konstraint(DemoByte::x) { greaterThan(0x0).lessThan(0xf) }
                .build()

        var demo = DemoByte(0x5)
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoByte(0x10)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must be less than 15""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("numeric.lessThan")
    }

    @Test
    fun konstraintOnShort() {
        val validator = ValidatorBuilder.of<DemoShort>()
                .konstraint(DemoShort::x) { greaterThan(1).lessThan(10) }
                .build()

        var demo = DemoShort(5)
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoShort(100)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must be less than 10""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("numeric.lessThan")
    }

    @Test
    fun konstraintOnLong() {
        val validator = ValidatorBuilder.of<DemoLong>()
                .konstraint(DemoLong::x) { greaterThan(1).lessThan(10) }
                .build()

        var demo = DemoLong(5)
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoLong(100)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must be less than 10""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("numeric.lessThan")
    }

    @Test
    fun konstraintOnFloat() {
        val validator = ValidatorBuilder.of<DemoFloat>()
                .konstraint(DemoFloat::x) { greaterThan(0.0f).lessThan(10.0f) }
                .build()

        var demo = DemoFloat(5.0f)
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoFloat(100.0f)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must be less than 10""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("numeric.lessThan")
    }

    @Test
    fun konstraintOnBigInteger() {
        val validator = ValidatorBuilder.of<DemoBigInteger>()
                .konstraint(DemoBigInteger::x) { greaterThan(BigInteger.ZERO).lessThan(BigInteger.TEN) }
                .build()

        var demo = DemoBigInteger(BigInteger.valueOf(5))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoBigInteger(BigInteger.valueOf(100))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must be less than 10""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("numeric.lessThan")
    }

    @Test
    fun konstraintOnBigDecimal() {
        val validator = ValidatorBuilder.of<DemoBigDecimal>()
                .konstraint(DemoBigDecimal::x) { greaterThan(BigDecimal.ZERO).lessThan(BigDecimal.TEN) }
                .build()

        var demo = DemoBigDecimal(BigDecimal.valueOf(5))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoBigDecimal(BigDecimal.valueOf(100))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must be less than 10""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("numeric.lessThan")
    }

    @Test
    fun konstraintOnCollection() {
        val validator = ValidatorBuilder.of<DemoCollection>()
                .konstraint(DemoCollection::x) { greaterThan(1).lessThan(3) }
                .build()

        var demo = DemoCollection(listOf("a", "b"))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoCollection(listOf("a", "b", "c"))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnMap() {
        val validator = ValidatorBuilder.of<DemoMap>()
                .konstraint(DemoMap::x) { greaterThan(1).lessThan(3) }
                .build()

        var demo = DemoMap(mapOf("1" to "a", "2" to "a"))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoMap(mapOf("1" to "a", "2" to "b", "3" to "c"))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnArray() {
        val validator = ValidatorBuilder.of<DemoArray>()
                .konstraint(DemoArray::x) { greaterThan(1).lessThan(3) }
                .build()

        var demo = DemoArray(arrayOf("a", "b"))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoArray(arrayOf("a", "b", "c"))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnBooleanArray() {
        val validator = ValidatorBuilder.of<DemoBooleanArray>()
                .konstraint(DemoBooleanArray::x) { greaterThan(1).lessThan(3) }
                .build()

        var demo = DemoBooleanArray(booleanArrayOf(true, true))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoBooleanArray(booleanArrayOf(true, true, true))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnCharArray() {
        val validator = ValidatorBuilder.of<DemoCharArray>()
                .konstraint(DemoCharArray::x) { greaterThan(1).lessThan(3) }
                .build()

        var demo = DemoCharArray(charArrayOf('1', '2'))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoCharArray(charArrayOf('1', '2', '3'))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnByteArray() {
        val validator = ValidatorBuilder.of<DemoByteArray>()
                .konstraint(DemoByteArray::x) { greaterThan(1).lessThan(3) }
                .build()

        var demo = DemoByteArray(byteArrayOf(1, 2))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoByteArray(byteArrayOf(1, 2, 3))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnShortArray() {
        val validator = ValidatorBuilder.of<DemoShortArray>()
                .konstraint(DemoShortArray::x) { greaterThan(1).lessThan(3) }
                .build()

        var demo = DemoShortArray(shortArrayOf(1, 2))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoShortArray(shortArrayOf(1, 2, 3))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnIntArray() {
        val validator = ValidatorBuilder.of<DemoIntArray>()
                .konstraint(DemoIntArray::x) { greaterThan(1).lessThan(3) }
                .build()

        var demo = DemoIntArray(intArrayOf(1, 2))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoIntArray(intArrayOf(1, 2, 3))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnLongArray() {
        val validator = ValidatorBuilder.of<DemoLongArray>()
                .konstraint(DemoLongArray::x) { greaterThan(1).lessThan(3) }
                .build()

        var demo = DemoLongArray(longArrayOf(1, 2))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoLongArray(longArrayOf(1, 2, 3))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnFloatArray() {
        val validator = ValidatorBuilder.of<DemoFloatArray>()
                .konstraint(DemoFloatArray::x) { greaterThan(1).lessThan(3) }
                .build()

        var demo = DemoFloatArray(floatArrayOf(1f, 2f))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoFloatArray(floatArrayOf(1f, 2f, 3f))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintOnDoubleArray() {
        val validator = ValidatorBuilder.of<DemoDoubleArray>()
                .konstraint(DemoDoubleArray::x) { greaterThan(1).lessThan(3) }
                .build()

        var demo = DemoDoubleArray(doubleArrayOf(1.0, 2.0))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoDoubleArray(doubleArrayOf(1.0, 2.0, 3.0))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintNested() {
        val validator = validator<DemoNested> {
            DemoNested::x nest {
                DemoString::x { greaterThan(1).lessThan(5) }
            }
        }

        var demo = DemoNested(DemoString("foo"))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoNested(DemoString("foofoo"))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x.x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintNestedValidator() {
        val validator = ValidatorBuilder.of<DemoNested>()
                .nest(DemoNested::x, demoStringValidator)
                .build()

        var demo = DemoNested(DemoString("foo"))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoNested(DemoString("foofoo"))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x.x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintNestedIfPresent() {
        val validator = ValidatorBuilder.of<DemoNestedIfPresent>()
                .nestIfPresent(DemoNestedIfPresent::x) {
                    konstraint(DemoString::x) { greaterThan(1).lessThan(5) }
                }
                .build()

        var demo = DemoNestedIfPresent(DemoString("foo"))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoNestedIfPresent(null)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoNestedIfPresent(DemoString("foofoo"))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x.x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintNestedIfPresentValidator() {
        val validator = ValidatorBuilder.of<DemoNestedIfPresent>()
                .nestIfPresent(DemoNestedIfPresent::x, demoStringValidator)
                .build()

        var demo = DemoNestedIfPresent(DemoString("foo"))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoNestedIfPresent(null)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoNestedIfPresent(DemoString("foofoo"))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x.x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintForEachCollection() {
        val validator = validator<DemoForEachCollection> {
            DemoForEachCollection::x forEach {
                DemoString::x {
                    greaterThan(1)
                    lessThan(5)
                }
            }
        }

        var demo = DemoForEachCollection(listOf(DemoString("foo")))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachCollection(listOf(DemoString("foofoo")))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x[0].x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintForEachCollectionValidator() {
        val validator = ValidatorBuilder.of<DemoForEachCollection>()
                .forEach(DemoForEachCollection::x, demoStringValidator)
                .build()

        var demo = DemoForEachCollection(listOf(DemoString("foo")))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachCollection(listOf(DemoString("foofoo")))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x[0].x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintForEachIfPresentCollection() {
        val validator = ValidatorBuilder.of<DemoForEachIfPresentCollection>()
                .forEachIfPresent(DemoForEachIfPresentCollection::x) {
                    konstraint(DemoString::x) { greaterThan(1).lessThan(5) }
                }
                .build()

        var demo = DemoForEachIfPresentCollection(listOf(DemoString("foo")))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachIfPresentCollection(null)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachIfPresentCollection(listOf(DemoString("foofoo")))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x[0].x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintForEachIfPresentCollectionValidator() {
        val validator = ValidatorBuilder.of<DemoForEachIfPresentCollection>()
                .forEachIfPresent(DemoForEachIfPresentCollection::x, demoStringValidator)
                .build()

        var demo = DemoForEachIfPresentCollection(listOf(DemoString("foo")))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachIfPresentCollection(null)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachIfPresentCollection(listOf(DemoString("foofoo")))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x[0].x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintForEachMap() {
        val validator = ValidatorBuilder.of<DemoForEachMap>()
                .forEach(DemoForEachMap::x) {
                    konstraint(DemoString::x) { greaterThan(1).lessThan(5) }
                }
                .build()

        var demo = DemoForEachMap(mapOf("a" to DemoString("foo")))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachMap(mapOf("a" to DemoString("foofoo")))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x[0].x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintForEachIfPresentMap() {
        val validator = ValidatorBuilder.of<DemoForEachIfPresentMap>()
                .forEachIfPresent(DemoForEachIfPresentMap::x) {
                    konstraint(DemoString::x) { greaterThan(1).lessThan(5) }
                }
                .build()

        var demo = DemoForEachIfPresentMap(mapOf("a" to DemoString("foo")))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachIfPresentMap(null)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachIfPresentMap(mapOf("a" to DemoString("foofoo")))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x[0].x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintForEachMapValidator() {
        val validator = ValidatorBuilder.of<DemoForEachMap>()
                .forEach(DemoForEachMap::x, demoStringValidator)
                .build()

        var demo = DemoForEachMap(mapOf("a" to DemoString("foo")))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachMap(mapOf("a" to DemoString("foofoo")))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x[0].x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintForEachIfPresentMapValidator() {
        val validator = ValidatorBuilder.of<DemoForEachIfPresentMap>()
                .forEachIfPresent(DemoForEachIfPresentMap::x, demoStringValidator)
                .build()

        var demo = DemoForEachIfPresentMap(mapOf("a" to DemoString("foo")))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachIfPresentMap(null)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachIfPresentMap(mapOf("a" to DemoString("foofoo")))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x[0].x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintForEachArray() {
        val validator = ValidatorBuilder.of<DemoForEachArray>()
                .forEach(DemoForEachArray::x) {
                    konstraint(DemoString::x) { greaterThan(1).lessThan(5) }
                }
                .build()

        var demo = DemoForEachArray(arrayOf(DemoString("foo")))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachArray(arrayOf(DemoString("foofoo")))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x[0].x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintForEachIfPresentArray() {
        val validator = ValidatorBuilder.of<DemoForEachIfPresentArray>()
                .forEachIfPresent(DemoForEachIfPresentArray::x) {
                    konstraint(DemoString::x) { greaterThan(1).lessThan(5) }
                }
                .build()

        var demo = DemoForEachIfPresentArray(arrayOf(DemoString("foo")))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachIfPresentArray(null)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachIfPresentArray(arrayOf(DemoString("foofoo")))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x[0].x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintForEachArrayValidator() {
        val validator = ValidatorBuilder.of<DemoForEachArray>()
                .forEach(DemoForEachArray::x, demoStringValidator)
                .build()

        var demo = DemoForEachArray(arrayOf(DemoString("foo")))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachArray(arrayOf(DemoString("foofoo")))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x[0].x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun konstraintForEachIfPresentArrayValidator() {
        val validator = ValidatorBuilder.of<DemoForEachIfPresentArray>()
                .forEachIfPresent(DemoForEachIfPresentArray::x, demoStringValidator)
                .build()

        var demo = DemoForEachIfPresentArray(arrayOf(DemoString("foo")))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachIfPresentArray(null)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoForEachIfPresentArray(arrayOf(DemoString("foofoo")))
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x[0].x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }

    @Test
    fun onCondition() {
        val groupA = ConstraintGroup.of("A")
        val groupB = ConstraintGroup.of("B")

        val validator: Validator<DemoString> = ValidatorBuilder.of<DemoString>()
                .konstraintOnCondition(groupA.toCondition()) {
                    konstraint(DemoString::x) {
                        greaterThan(1)
                    }
                }
                .konstraintOnCondition(groupB.toCondition()) {
                    konstraint(DemoString::x) {
                        lessThan(3)
                    }
                }
                .build()

        // valid for A and B
        var demo = DemoString("aa")
        var violations = validator.validate(demo, groupA)
        Assertions.assertThat(violations.isValid).isTrue()

        violations = validator.validate(demo, groupB)
        Assertions.assertThat(violations.isValid).isTrue()

        // valid for only A
        demo = DemoString("aaa")
        violations = validator.validate(demo, groupA)
        Assertions.assertThat(violations.isValid).isTrue()

        violations = validator.validate(demo, groupB)
        Assertions.assertThat(violations.isValid).isFalse()
        var violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")

        // valid for only B
        demo = DemoString("a")
        violations = validator.validate(demo, groupA)
        Assertions.assertThat(violations.isValid).isFalse()
        violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be greater than 1. The given size is 1""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.greaterThan")

        violations = validator.validate(demo, groupB)
        Assertions.assertThat(violations.isValid).isTrue()
    }

    @Test
    fun onGroup() {
        val validator: Validator<DemoString> = ValidatorBuilder.of<DemoString>()
                .konstraintOnGroup(Group.UPDATE) {
                    konstraint(DemoString::x) {
                        greaterThan(1)
                    }
                }
                .konstraintOnGroup(Group.DELETE) {
                    konstraint(DemoString::x) {
                        lessThan(3)
                    }
                }
                .build()

        // valid for UPDATE and DELETe
        var demo = DemoString("aa")
        var violations = validator.validate(demo, Group.UPDATE)
        Assertions.assertThat(violations.isValid).isTrue()

        violations = validator.validate(demo, Group.DELETE)
        Assertions.assertThat(violations.isValid).isTrue()

        // valid for only UPDATE
        demo = DemoString("aaa")
        violations = validator.validate(demo, Group.UPDATE)
        Assertions.assertThat(violations.isValid).isTrue()

        violations = validator.validate(demo, Group.DELETE)
        Assertions.assertThat(violations.isValid).isFalse()
        var violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 3. The given size is 3""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")

        // valid for only DELETE
        demo = DemoString("a")
        violations = validator.validate(demo, Group.UPDATE)
        Assertions.assertThat(violations.isValid).isFalse()
        violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be greater than 1. The given size is 1""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.greaterThan")

        violations = validator.validate(demo, Group.DELETE)
        Assertions.assertThat(violations.isValid).isTrue()
    }

    @Test
    fun onObject() {
        val validator = ValidatorBuilder.of<DemoNestedIfPresent>()
                .konstraintOnObject(DemoNestedIfPresent::x) { notNull() }
                .build()

        var demo = DemoNestedIfPresent(DemoString("foo"))
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoNestedIfPresent(null)
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo(""""x" must not be null""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("object.notNull")
    }
}