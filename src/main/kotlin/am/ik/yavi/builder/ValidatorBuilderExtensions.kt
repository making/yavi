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

import am.ik.yavi.constraint.*
import am.ik.yavi.constraint.array.*
import am.ik.yavi.core.ConstraintCondition
import am.ik.yavi.core.ConstraintGroup
import am.ik.yavi.core.Validator
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KProperty1

fun <T, N> ValidatorBuilder<T>.nest(prop: KProperty1<T, N?>, validator: Validator<N>): ValidatorBuilder<T> =
        this.nest(prop, prop.name, validator)

fun <T, N> ValidatorBuilder<T>.nestIfPresent(prop: KProperty1<T, N?>, validator: Validator<N>): ValidatorBuilder<T> =
        this.nestIfPresent(prop, prop.name, validator)

@Suppress("UNCHECKED_CAST")
fun <T, N> ValidatorBuilder<T>.nest(prop: KProperty1<T, N?>, block: ValidatorBuilder<N>.() -> Unit): ValidatorBuilder<T> =
        this.nest(prop, prop.name) { it.apply(block as ValidatorBuilder<N?>.() -> Unit) }

@Suppress("UNCHECKED_CAST")
fun <T, N> ValidatorBuilder<T>.nestIfPresent(prop: KProperty1<T, N?>, block: ValidatorBuilder<N>.() -> Unit): ValidatorBuilder<T> =
        this.nestIfPresent(prop, prop.name) { it.apply(block as ValidatorBuilder<N?>.() -> Unit) }

fun <T, L : Collection<E>?, E> ValidatorBuilder<T>.forEach(prop: KProperty1<T, L>, validator: Validator<E>): ValidatorBuilder<T> =
        this.forEach(prop, prop.name, validator)

fun <T, L : Collection<E>?, E> ValidatorBuilder<T>.forEachIfPresent(prop: KProperty1<T, L?>, validator: Validator<E>): ValidatorBuilder<T> =
        this.forEachIfPresent(prop, prop.name, validator)

fun <T, L : Collection<E>?, E> ValidatorBuilder<T>.forEach(prop: KProperty1<T, L?>, block: ValidatorBuilder<E>.() -> Unit): ValidatorBuilder<T> =
        this.forEach(prop, prop.name) { it.apply(block) }

fun <T, L : Collection<E>?, E> ValidatorBuilder<T>.forEachIfPresent(prop: KProperty1<T, L?>, block: ValidatorBuilder<E>.() -> Unit): ValidatorBuilder<T> =
        this.forEachIfPresent(prop, prop.name) { it.apply(block) }

@JvmName("forEachMapMap")
fun <T, K, V> ValidatorBuilder<T>.forEach(prop: KProperty1<T, Map<K, V>?>, validator: Validator<V>): ValidatorBuilder<T> =
        this.forEach(prop, prop.name, validator)

@JvmName("forEachIfPresentMap")
fun <T, K, V> ValidatorBuilder<T>.forEachIfPresent(prop: KProperty1<T, Map<K, V>?>, validator: Validator<V>): ValidatorBuilder<T> =
        this.forEachIfPresent(prop, prop.name, validator)

@JvmName("forEachMapMap")
fun <T, K, V> ValidatorBuilder<T>.forEach(prop: KProperty1<T, Map<K, V>?>, block: ValidatorBuilder<V>.() -> Unit): ValidatorBuilder<T> =
        this.forEach(prop, prop.name) { it.apply(block) }

@JvmName("forEachIfPresentMap")
fun <T, K, V> ValidatorBuilder<T>.forEachIfPresent(prop: KProperty1<T, Map<K, V>?>, block: ValidatorBuilder<V>.() -> Unit): ValidatorBuilder<T> =
        this.forEachIfPresent(prop, prop.name) { it.apply(block) }

@JvmName("forEachMapArray")
fun <T, E> ValidatorBuilder<T>.forEach(prop: KProperty1<T, Array<E>?>, validator: Validator<E>): ValidatorBuilder<T> =
        this.forEach(prop, prop.name, validator)

@JvmName("forEachIfPresentArray")
fun <T, E> ValidatorBuilder<T>.forEachIfPresent(prop: KProperty1<T, Array<E>?>, validator: Validator<E>): ValidatorBuilder<T> =
        this.forEachIfPresent(prop, prop.name, validator)

@JvmName("forEachMapArray")
fun <T, E> ValidatorBuilder<T>.forEach(prop: KProperty1<T, Array<E>?>, block: ValidatorBuilder<E>.() -> Unit): ValidatorBuilder<T> =
        this.forEach(prop, prop.name) { it.apply(block) }

@JvmName("forEachIfPresentArray")
fun <T, E> ValidatorBuilder<T>.forEachIfPresent(prop: KProperty1<T, Array<E>?>, block: ValidatorBuilder<E>.() -> Unit): ValidatorBuilder<T> =
        this.forEachIfPresent(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnCharSequence")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, String?>, block: CharSequenceConstraint<T, String?>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnBoolean")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, Boolean?>, block: BooleanConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnChar")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, Char?>, block: CharacterConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnByte")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, Byte?>, block: ByteConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnShort")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, Short?>, block: ShortConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnInt")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, Int?>, block: IntegerConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnLong")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, Long?>, block: LongConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnFloat")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, Float?>, block: FloatConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnDouble")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, Double?>, block: DoubleConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnBigInteger")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, BigInteger?>, block: BigIntegerConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnBigDecimal")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, BigDecimal?>, block: BigDecimalConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnCollection")
fun <T, L : Collection<E>?, E> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, L?>, block: CollectionConstraint<T, L?, E>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnMap")
fun <T, K, V> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, Map<K, V>?>, block: MapConstraint<T, K, V>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnObjectArray")
fun <T, E> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, Array<E>?>, block: ObjectArrayConstraint<T, E>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnBooleanArray")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, BooleanArray?>, block: BooleanArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnCharArray")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, CharArray?>, block: CharArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnByteArray")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, ByteArray?>, block: ByteArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnShortArray")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, ShortArray?>, block: ShortArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnIntArray")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, IntArray?>, block: IntArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnLongArray")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, LongArray?>, block: LongArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnFloatArray")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, FloatArray?>, block: FloatArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("konstraintOnDoubleArray")
fun <T> ValidatorBuilder<T>.konstraint(prop: KProperty1<T, DoubleArray?>, block: DoubleArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

fun <T, E> ValidatorBuilder<T>.konstraintOnObject(prop: KProperty1<T, E?>, block: ObjectConstraint<T, E?>.() -> Unit): ValidatorBuilder<T> =
        this.constraintOnObject(prop, prop.name) { it.apply(block) }

fun <T> ValidatorBuilder<T>.konstraintOnCondition(condition: ConstraintCondition<T>, block: ValidatorBuilder<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraintOnCondition(condition, ValidatorBuilder.ValidatorBuilderConverter { it.apply(block) })

fun <T> ValidatorBuilder<T>.konstraintOnGroup(group: ConstraintGroup, block: ValidatorBuilder<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraintOnCondition(group.toCondition(), ValidatorBuilder.ValidatorBuilderConverter { it.apply(block) })

inline fun <T> validator(init: ValidatorBuilderKt<T>.() -> Unit): Validator<T> {
        val builder = ValidatorBuilderKt<T>()
        return builder.apply(init).build()
}

class ValidatorBuilderKt<T> : ValidatorBuilder<T>() {

        /** Q: this will be required for each type, right? */
        operator fun KProperty1<T, String?>.invoke(block: CharSequenceConstraint<T, String?>.() -> Unit) =
                constraint(this, this.name) { it.apply(block) }

        /** Q: this will be required for each type, right? */
        infix fun KProperty1<T, String?>.required(block: CharSequenceConstraint<T, String?>.() -> Unit) =
                constraint(this, this.name) { it.notNull().apply(block) }

        infix fun <L: Collection<E>?, E> KProperty1<T, L?>.forEach(block: ValidatorBuilderKt<E>.() -> Unit) {
                TODO()
                /** can't get following to work due to cumbersome inheritance setup of ValidatorBuilderKt :-( */
                // forEach(this, this.name) { it.apply(block) }
        }

        infix fun <N> KProperty1<T, N?>.nest(block: ValidatorBuilderKt<N>.() -> Unit) {
                // TODO()
                /** also not possible to cast... :-(( */
                // nest(this, this.name) { it.apply(block as ValidatorBuilder<N?>.() -> Unit) }
        }
}