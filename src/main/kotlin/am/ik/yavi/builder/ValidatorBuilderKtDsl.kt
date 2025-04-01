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
package am.ik.yavi.builder

import am.ik.yavi.constraint.*
import am.ik.yavi.constraint.array.*
import am.ik.yavi.core.ConstraintCondition
import am.ik.yavi.core.ConstraintGroup
import am.ik.yavi.core.Validator
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import kotlin.reflect.KProperty1

/**
 * Validator DSL
 * @since 0.9.0
 */
inline fun <T> validator(init: ValidatorBuilderKt<T>.() -> Unit): Validator<T> {
	val builder = ValidatorBuilderKt<T>(ValidatorBuilder())
	return builder.apply(init).build()
}

class ValidatorBuilderKt<T>(private val validatorBuilder: ValidatorBuilder<T>) {

	@PublishedApi
	internal fun build(): Validator<T> {
		return validatorBuilder.build()
	}

	@JvmName("invokeTString")
	operator fun KProperty1<T, String?>.invoke(block: CharSequenceConstraint<T, String?>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTString")
	operator fun KProperty1<T, String?>.invoke(
		name: String,
		block: CharSequenceConstraint<T, String?>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTBoolean")
	operator fun KProperty1<T, Boolean?>.invoke(block: BooleanConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTBoolean")
	operator fun KProperty1<T, Boolean?>.invoke(
		name: String,
		block: BooleanConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTChar")
	operator fun KProperty1<T, Char?>.invoke(block: CharacterConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTChar")
	operator fun KProperty1<T, Char?>.invoke(
		name: String,
		block: CharacterConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTByte")
	operator fun KProperty1<T, Byte?>.invoke(block: ByteConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTByte")
	operator fun KProperty1<T, Byte?>.invoke(
		name: String,
		block: ByteConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTShort")
	operator fun KProperty1<T, Short?>.invoke(block: ShortConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTShort")
	operator fun KProperty1<T, Short?>.invoke(
		name: String,
		block: ShortConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTInt")
	operator fun KProperty1<T, Int?>.invoke(block: IntegerConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTInt")
	operator fun KProperty1<T, Int?>.invoke(
		name: String,
		block: IntegerConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTLong")
	operator fun KProperty1<T, Long?>.invoke(block: LongConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTLong")
	operator fun KProperty1<T, Long?>.invoke(
		name: String,
		block: LongConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTFloat")
	operator fun KProperty1<T, Float?>.invoke(block: FloatConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTFloat")
	operator fun KProperty1<T, Float?>.invoke(
		name: String,
		block: FloatConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTDouble")
	operator fun KProperty1<T, Double?>.invoke(block: DoubleConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTDouble")
	operator fun KProperty1<T, Double?>.invoke(
		name: String,
		block: DoubleConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTBigInteger")
	operator fun KProperty1<T, BigInteger?>.invoke(block: BigIntegerConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTBigInteger")
	operator fun KProperty1<T, BigInteger?>.invoke(
		name: String,
		block: BigIntegerConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTBigDecimal")
	operator fun KProperty1<T, BigDecimal?>.invoke(block: BigDecimalConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTBigDecimal")
	operator fun KProperty1<T, BigDecimal?>.invoke(
		name: String,
		block: BigDecimalConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * @since 0.10.0
	 */
	@JvmName("invokeTLocalTime")
	operator fun KProperty1<T, LocalTime?>.invoke(block: LocalTimeConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * @since 0.10.0
	 */
	@JvmName("invokeTLocalTime")
	operator fun KProperty1<T, LocalTime?>.invoke(
		name: String,
		block: LocalTimeConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * @since 0.10.0
	 */
	@JvmName("invokeTLocalDate")
	operator fun KProperty1<T, LocalDate?>.invoke(block: LocalDateConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * @since 0.10.0
	 */
	@JvmName("invokeTLocalDate")
	operator fun KProperty1<T, LocalDate?>.invoke(
		name: String,
		block: LocalDateConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * @since 0.10.0
	 */
	@JvmName("invokeTLocalDateTime")
	operator fun KProperty1<T, LocalDateTime?>.invoke(block: LocalDateTimeConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * @since 0.10.0
	 */
	@JvmName("invokeTLocalDateTime")
	operator fun KProperty1<T, LocalDateTime?>.invoke(
		name: String,
		block: LocalDateTimeConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * @since 0.10.0
	 */
	@JvmName("invokeTZonedDateTime")
	operator fun KProperty1<T, ZonedDateTime?>.invoke(block: ZonedDateTimeConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * @since 0.10.0
	 */
	@JvmName("invokeTZonedDateTime")
	operator fun KProperty1<T, ZonedDateTime?>.invoke(
		name: String,
		block: ZonedDateTimeConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * @since 0.10.0
	 */
	@JvmName("invokeTOffsetDataTime")
	operator fun KProperty1<T, OffsetDateTime?>.invoke(block: OffsetDateTimeConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * @since 0.10.0
	 */
	@JvmName("invokeTOffsetDataTime")
	operator fun KProperty1<T, OffsetDateTime?>.invoke(
		name: String,
		block: OffsetDateTimeConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * @since 0.10.0
	 */
	@JvmName("invokeTInstant")
	operator fun KProperty1<T, Instant?>.invoke(block: InstantConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * @since 0.10.0
	 */
	@JvmName("invokeTInstant")
	operator fun KProperty1<T, Instant?>.invoke(
		name: String,
		block: InstantConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * @since 0.11.0
	 */
	@JvmName("invokeTYearMonth")
	operator fun KProperty1<T, YearMonth?>.invoke(block: YearMonthConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * @since 0.11.0
	 */
	@JvmName("invokeTYearMonth")
	operator fun KProperty1<T, YearMonth?>.invoke(
		name: String,
		block: YearMonthConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * @since 0.11.0
	 */
	@JvmName("invokeTYear")
	operator fun KProperty1<T, Year?>.invoke(block: YearConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * @since 0.11.0
	 */
	@JvmName("invokeTYear")
	operator fun KProperty1<T, Year?>.invoke(
		name: String,
		block: YearConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	infix fun <N> KProperty1<T, N?>.nest(validator: Validator<N>) =
		validatorBuilder.nest(this, this.name, validator)

	infix fun <N> KProperty1<T, N?>.nestIfPresent(validator: Validator<N>) =
		validatorBuilder.nestIfPresent(this, this.name, validator)

	@Suppress("UNCHECKED_CAST")
	infix fun <N> KProperty1<T, N?>.nest(block: ValidatorBuilderKt<N>.() -> Unit) =
		validatorBuilder.nest(this, this.name) {
			ValidatorBuilderKt(it).apply(block as ValidatorBuilderKt<N?>.() -> Unit).validatorBuilder
		}

	@Suppress("UNCHECKED_CAST")
	infix fun <N> KProperty1<T, N?>.nestIfPresent(block: ValidatorBuilderKt<N>.() -> Unit) =
		validatorBuilder.nestIfPresent(this, this.name) {
			ValidatorBuilderKt(it).apply(block as ValidatorBuilderKt<N?>.() -> Unit).validatorBuilder
		}

	infix fun <L : Collection<E>?, E> KProperty1<T, L?>.forEach(validator: Validator<E>) =
		validatorBuilder.forEach(this, this.name, validator)

	infix fun <L : Collection<E>?, E> KProperty1<T, L?>.forEach(block: ValidatorBuilderKt<E>.() -> Unit) =
		validatorBuilder.forEach(
			this,
			this.name
		) { ValidatorBuilderKt(it).apply(block).validatorBuilder }

	infix fun <L : Collection<E>?, E> KProperty1<T, L?>.forEachIfPresent(validator: Validator<E>) =
		validatorBuilder.forEachIfPresent(this, this.name, validator)

	infix fun <L : Collection<E>?, E> KProperty1<T, L?>.forEachIfPresent(block: ValidatorBuilderKt<E>.() -> Unit) =
		validatorBuilder.forEachIfPresent(this, this.name) {
			ValidatorBuilderKt(it).apply(
				block
			).validatorBuilder
		}

	@JvmName("forEachMapMap")
	infix fun <K, V> KProperty1<T, Map<K, V>?>.forEach(validator: Validator<V>) =
		validatorBuilder.forEach(this, this.name, validator)

	@JvmName("forEachMapMap")
	infix fun <K, V> KProperty1<T, Map<K, V>?>.forEach(block: ValidatorBuilderKt<V>.() -> Unit) =
		validatorBuilder.forEach(
			this,
			this.name
		) { ValidatorBuilderKt(it).apply(block).validatorBuilder }

	@JvmName("forEachIfPresentMap")
	infix fun <K, V> KProperty1<T, Map<K, V>?>.forEachIfPresent(validator: Validator<V>) =
		validatorBuilder.forEachIfPresent(this, this.name, validator)

	@JvmName("forEachIfPresentMap")
	infix fun <K, V> KProperty1<T, Map<K, V>?>.forEachIfPresent(block: ValidatorBuilderKt<V>.() -> Unit) =
		validatorBuilder.forEachIfPresent(this, this.name) {
			ValidatorBuilderKt(it).apply(
				block
			).validatorBuilder
		}

	@JvmName("forEachMapArray")
	infix fun <E> KProperty1<T, Array<E>?>.forEach(validator: Validator<E>) =
		validatorBuilder.forEach(this, this.name, validator)

	@JvmName("forEachMapArray")
	infix fun <E> KProperty1<T, Array<E>?>.forEach(block: ValidatorBuilderKt<E>.() -> Unit) =
		validatorBuilder.forEach(
			this,
			this.name
		) { ValidatorBuilderKt(it).apply(block).validatorBuilder }

	@JvmName("forEachIfPresentArray")
	infix fun <E> KProperty1<T, Array<E>?>.forEachIfPresent(validator: Validator<E>) =
		validatorBuilder.forEachIfPresent(this, this.name, validator)

	@JvmName("forEachIfPresentArray")
	infix fun <E> KProperty1<T, Array<E>?>.forEachIfPresent(block: ValidatorBuilderKt<E>.() -> Unit) =
		validatorBuilder.forEachIfPresent(this, this.name) {
			ValidatorBuilderKt(it).apply(
				block
			).validatorBuilder
		}

	@JvmName("invokeTCollection")
	operator fun <L : Collection<E>?, E> KProperty1<T, L?>.invoke(block: CollectionConstraint<T, L?, E>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTCollection")
	operator fun <L : Collection<E>?, E> KProperty1<T, L?>.invoke(
		name: String,
		block: CollectionConstraint<T, L?, E>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTMap")
	operator fun <K, V> KProperty1<T, Map<K, V>?>.invoke(block: MapConstraint<T, K, V>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTMap")
	operator fun <K, V> KProperty1<T, Map<K, V>?>.invoke(
		name: String,
		block: MapConstraint<T, K, V>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTArray")
	operator fun <E> KProperty1<T, Array<E>?>.invoke(block: ObjectArrayConstraint<T, E>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTArray")
	operator fun <E> KProperty1<T, Array<E>?>.invoke(
		name: String,
		block: ObjectArrayConstraint<T, E>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	operator fun <E : Enum<E>?> KProperty1<T, E?>.invoke(block: EnumConstraint<T, E>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }


	infix fun <E> KProperty1<T, E?>.onObject(block: ObjectConstraint<T, E?>.() -> Unit) =
		validatorBuilder.constraintOnObject(this, this.name) { it.apply(block) }

	@JvmName("invokeTBooleanArray")
	operator fun KProperty1<T, BooleanArray?>.invoke(block: BooleanArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTBooleanArray")
	operator fun KProperty1<T, BooleanArray?>.invoke(
		name: String,
		block: BooleanArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTCharArray")
	operator fun KProperty1<T, CharArray?>.invoke(block: CharArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTCharArray")
	operator fun KProperty1<T, CharArray?>.invoke(
		name: String,
		block: CharArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTByteArray")
	operator fun KProperty1<T, ByteArray?>.invoke(block: ByteArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTByteArray")
	operator fun KProperty1<T, ByteArray?>.invoke(
		name: String,
		block: ByteArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTShortArray")
	operator fun KProperty1<T, ShortArray?>.invoke(block: ShortArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTShortArray")
	operator fun KProperty1<T, ShortArray?>.invoke(
		name: String,
		block: ShortArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTIntArray")
	operator fun KProperty1<T, IntArray?>.invoke(block: IntArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTIntArray")
	operator fun KProperty1<T, IntArray?>.invoke(
		name: String,
		block: IntArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTLongArray")
	operator fun KProperty1<T, LongArray?>.invoke(block: LongArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTLongArray")
	operator fun KProperty1<T, LongArray?>.invoke(
		name: String,
		block: LongArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTFloatArray")
	operator fun KProperty1<T, FloatArray?>.invoke(block: FloatArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTFloatArray")
	operator fun KProperty1<T, FloatArray?>.invoke(
		name: String,
		block: FloatArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	@JvmName("invokeTDoubleArray")
	operator fun KProperty1<T, DoubleArray?>.invoke(block: DoubleArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	@JvmName("invokeTDoubleArray")
	operator fun KProperty1<T, DoubleArray?>.invoke(
		name: String,
		block: DoubleArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	fun onCondition(
		condition: ConstraintCondition<T>,
		block: ValidatorBuilderKt<T>.() -> Unit
	) =
		validatorBuilder.constraintOnCondition(condition) {
			ValidatorBuilderKt(it).apply(block).validatorBuilder
		}

	fun onGroup(group: ConstraintGroup, block: ValidatorBuilderKt<T>.() -> Unit) =
		validatorBuilder.constraintOnCondition(group.toCondition()) {
			ValidatorBuilderKt(it).apply(block).validatorBuilder
		}

	/**
	 * @since 0.12.0
	 */
	fun unwrap() = validatorBuilder
}
