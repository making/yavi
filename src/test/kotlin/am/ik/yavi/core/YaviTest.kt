/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core

import org.assertj.core.api.Assertions
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger


data class DemoString(var x: String?)
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

class YaviTest {
    private val demoStringValidator: Validator<DemoString> = Validator.builder<DemoString>()
            .constraint(DemoString::x) { greaterThan(1).lessThan(5) }
            .build()

    @Test
    fun constraintOnCharSequence() {
        val validator = Validator.builder<DemoString>()
                .constraint(DemoString::x) { notEmpty().lessThan(5) }
                .build()

        var demo = DemoString("foo")
        var violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isTrue()

        demo = DemoString("foofoo")
        violations = validator.validate(demo)
        Assertions.assertThat(violations.isValid).isFalse()
        Assertions.assertThat(violations.size).isEqualTo(1)
        val violation = violations[0]
        Assertions.assertThat(violation.message()).isEqualTo("""The size of "x" must be less than 5. The given size is 6""")
        Assertions.assertThat(violation.messageKey()).isEqualTo("container.lessThan")
    }


    @Test
    fun constraintOnBoolean() {
        val validator = Validator.builder<DemoBoolean>()
                .constraint(DemoBoolean::x) { isTrue }
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
    fun constraintOnInteger() {
        val validator = Validator.builder<DemoInt>()
                .constraint(DemoInt::x) { greaterThan(1).lessThan(10) }
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
    fun constraintOnChar() {
        val validator = Validator.builder<DemoChar>()
                .constraint(DemoChar::x) { greaterThan('a').lessThan('z') }
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
    fun constraintOnByte() {
        val validator = Validator.builder<DemoByte>()
                .constraint(DemoByte::x) { greaterThan(0x0).lessThan(0xf) }
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
    fun constraintOnShort() {
        val validator = Validator.builder<DemoShort>()
                .constraint(DemoShort::x) { greaterThan(1).lessThan(10) }
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
    fun constraintOnLong() {
        val validator = Validator.builder<DemoLong>()
                .constraint(DemoLong::x) { greaterThan(1).lessThan(10) }
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
    fun constraintOnFloat() {
        val validator = Validator.builder<DemoFloat>()
                .constraint(DemoFloat::x) { greaterThan(0.0f).lessThan(10.0f) }
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
    fun constraintOnBigInteger() {
        val validator = Validator.builder<DemoBigInteger>()
                .constraint(DemoBigInteger::x) { greaterThan(BigInteger.ZERO).lessThan(BigInteger.TEN) }
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
    fun constraintOnBigDecimal() {
        val validator = Validator.builder<DemoBigDecimal>()
                .constraint(DemoBigDecimal::x) { greaterThan(BigDecimal.ZERO).lessThan(BigDecimal.TEN) }
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
    fun constraintOnCollection() {
        val validator = Validator.builder<DemoCollection>()
                .constraint(DemoCollection::x) { greaterThan(1).lessThan(3) }
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
    fun constraintOnMap() {
        val validator = Validator.builder<DemoMap>()
                .constraint(DemoMap::x) { greaterThan(1).lessThan(3) }
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
    fun constraintOnArray() {
        val validator = Validator.builder<DemoArray>()
                .constraint(DemoArray::x) { greaterThan(1).lessThan(3) }
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
    fun constraintOnBooleanArray() {
        val validator = Validator.builder<DemoBooleanArray>()
                .constraint(DemoBooleanArray::x) { greaterThan(1).lessThan(3) }
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
    fun constraintOnCharArray() {
        val validator = Validator.builder<DemoCharArray>()
                .constraint(DemoCharArray::x) { greaterThan(1).lessThan(3) }
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
    fun constraintOnByteArray() {
        val validator = Validator.builder<DemoByteArray>()
                .constraint(DemoByteArray::x) { greaterThan(1).lessThan(3) }
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
    fun constraintOnShortArray() {
        val validator = Validator.builder<DemoShortArray>()
                .constraint(DemoShortArray::x) { greaterThan(1).lessThan(3) }
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
    fun constraintOnIntArray() {
        val validator = Validator.builder<DemoIntArray>()
                .constraint(DemoIntArray::x) { greaterThan(1).lessThan(3) }
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
    fun constraintOnLongArray() {
        val validator = Validator.builder<DemoLongArray>()
                .constraint(DemoLongArray::x) { greaterThan(1).lessThan(3) }
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
    fun constraintOnFloatArray() {
        val validator = Validator.builder<DemoFloatArray>()
                .constraint(DemoFloatArray::x) { greaterThan(1).lessThan(3) }
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
    fun constraintOnDoubleArray() {
        val validator = Validator.builder<DemoDoubleArray>()
                .constraint(DemoDoubleArray::x) { greaterThan(1).lessThan(3) }
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
    fun constraintNested() {
        val validator = Validator.builder<DemoNested>()
                .constraintForNested(DemoNested::x) {
                    constraint(DemoString::x) { greaterThan(1).lessThan(5) }
                }
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
    fun constraintNestedValidator() {
        val validator = Validator.builder<DemoNested>()
                .constraintForNested(DemoNested::x, demoStringValidator)
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
    fun constraintNestedIfPresent() {
        val validator = Validator.builder<DemoNestedIfPresent>()
                .constraintIfPresentForNested(DemoNestedIfPresent::x) {
                    constraint(DemoString::x) { greaterThan(1).lessThan(5) }
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
    fun constraintNestedIfPresentValidator() {
        val validator = Validator.builder<DemoNestedIfPresent>()
                .constraintIfPresentForNested(DemoNestedIfPresent::x, demoStringValidator)
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
    fun constraintForEachCollection() {
        val validator = Validator.builder<DemoForEachCollection>()
                .constraintForEach(DemoForEachCollection::x) {
                    constraint(DemoString::x) { greaterThan(1).lessThan(5) }
                }
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
    fun constraintForEachCollectionValidator() {
        val validator = Validator.builder<DemoForEachCollection>()
                .constraintForEach(DemoForEachCollection::x, demoStringValidator)
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
    fun constraintForEachIfPresentCollection() {
        val validator = Validator.builder<DemoForEachIfPresentCollection>()
                .constraintIfPresentForEach(DemoForEachIfPresentCollection::x) {
                    constraint(DemoString::x) { greaterThan(1).lessThan(5) }
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
    fun constraintForEachIfPresentCollectionValidator() {
        val validator = Validator.builder<DemoForEachIfPresentCollection>()
                .constraintIfPresentForEach(DemoForEachIfPresentCollection::x, demoStringValidator)
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
    fun constraintForEachMap() {
        val validator = Validator.builder<DemoForEachMap>()
                .constraintForEach(DemoForEachMap::x) {
                    constraint(DemoString::x) { greaterThan(1).lessThan(5) }
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
    fun constraintForEachIfPresentMap() {
        val validator = Validator.builder<DemoForEachIfPresentMap>()
                .constraintIfPresentForEach(DemoForEachIfPresentMap::x) {
                    constraint(DemoString::x) { greaterThan(1).lessThan(5) }
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
    fun constraintForEachMapValidator() {
        val validator = Validator.builder<DemoForEachMap>()
                .constraintForEach(DemoForEachMap::x, demoStringValidator)
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
    fun constraintForEachIfPresentMapValidator() {
        val validator = Validator.builder<DemoForEachIfPresentMap>()
                .constraintIfPresentForEach(DemoForEachIfPresentMap::x, demoStringValidator)
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
    fun constraintForEachArray() {
        val validator = Validator.builder<DemoForEachArray>()
                .constraintForEach(DemoForEachArray::x) {
                    constraint(DemoString::x) { greaterThan(1).lessThan(5) }
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
    fun constraintForEachIfPresentArray() {
        val validator = Validator.builder<DemoForEachIfPresentArray>()
                .constraintIfPresentForEach(DemoForEachIfPresentArray::x) {
                    constraint(DemoString::x) { greaterThan(1).lessThan(5) }
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
    fun constraintForEachArrayValidator() {
        val validator = Validator.builder<DemoForEachArray>()
                .constraintForEach(DemoForEachArray::x, demoStringValidator)
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
    fun constraintForEachIfPresentArrayValidator() {
        val validator = Validator.builder<DemoForEachIfPresentArray>()
                .constraintIfPresentForEach(DemoForEachIfPresentArray::x, demoStringValidator)
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

        val validator: Validator<DemoString> = Validator.builder<DemoString>()
                .constraintOnCondition(groupA.toCondition()) {
                    constraint(DemoString::x) {
                        greaterThan(1)
                    }
                }
                .constraintOnCondition(groupB.toCondition()) {
                    constraint(DemoString::x) {
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
    fun onObject() {
        val validator = Validator.builder<DemoNestedIfPresent>()
                .constraintOnObject(DemoNestedIfPresent::x) { notNull() }
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