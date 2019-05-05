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
package am.ik.yavi.builder

import am.ik.yavi.constraint.*
import am.ik.yavi.constraint.array.*
import am.ik.yavi.core.ConstraintCondition
import am.ik.yavi.core.Validator
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KProperty1

@JvmName("constraintOnCharSequence")
fun <T, E : CharSequence?> ValidatorBuilder<T>.constraint(prop: KProperty1<T, E?>, block: CharSequenceConstraint<T, E?>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnBoolean")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, Boolean?>, block: BooleanConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnChar")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, Char?>, block: CharacterConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnByte")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, Byte?>, block: ByteConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnShort")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, Short?>, block: ShortConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnInt")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, Int?>, block: IntegerConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnLong")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, Long?>, block: LongConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnFloat")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, Float?>, block: FloatConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnDouble")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, Double?>, block: DoubleConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnBigInteger")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, BigInteger?>, block: BigIntegerConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnBigDecimal")
fun <T, E : BigDecimal?> ValidatorBuilder<T>.constraint(prop: KProperty1<T, E?>, block: BigDecimalConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnCollection")
fun <T, L : Collection<E>?, E> ValidatorBuilder<T>.constraint(prop: KProperty1<T, L?>, block: CollectionConstraint<T, L?, E>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnMap")
fun <T, K, V> ValidatorBuilder<T>.constraint(prop: KProperty1<T, Map<K, V>?>, block: MapConstraint<T, K, V>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnObjectArray")
fun <T, E> ValidatorBuilder<T>.constraint(prop: KProperty1<T, Array<E>?>, block: ObjectArrayConstraint<T, E>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnBooleanArray")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, BooleanArray?>, block: BooleanArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnCharArray")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, CharArray?>, block: CharArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnByteArray")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, ByteArray?>, block: ByteArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnShortArray")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, ShortArray?>, block: ShortArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnIntArray")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, IntArray?>, block: IntArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnLongArray")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, LongArray?>, block: LongArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnFloatArray")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, FloatArray?>, block: FloatArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

@JvmName("constraintOnDoubleArray")
fun <T> ValidatorBuilder<T>.constraint(prop: KProperty1<T, DoubleArray?>, block: DoubleArrayConstraint<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraint(prop, prop.name) { it.apply(block) }

fun <T, E> ValidatorBuilder<T>.constraintOnObject(prop: KProperty1<T, E?>, block: ObjectConstraint<T, E?>.() -> Unit): ValidatorBuilder<T> =
        this.constraintOnObject(prop, prop.name) { it.apply(block) }

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

fun <T> ValidatorBuilder<T>.constraintOnCondition(condition: ConstraintCondition<T>, block: ValidatorBuilder<T>.() -> Unit): ValidatorBuilder<T> =
        this.constraintOnCondition(condition, ValidatorBuilder.ValidatorBuilderConverter { it.apply(block) })