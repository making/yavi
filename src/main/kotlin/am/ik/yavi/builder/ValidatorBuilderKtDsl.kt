/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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
 * Creates a [Validator] for the target type [T] using a Kotlin DSL.
 *
 * <p>
 * The [init] block receives a [ValidatorBuilderKt] and configures property constraints,
 * nested validators and conditional validators. The returned validator is immutable and
 * thread-safe.
 *
 * Example:
 *
 * ```kotlin
 * val validator = validator<Person> {
 *     Person::name {
 *         notBlank()
 *         lessThanOrEqual(20)
 *     }
 *     Person::age { greaterThanOrEqual(0) }
 * }
 * ```
 *
 * @param <T> the type of the target object to validate
 * @param init configures the validator
 * @return a new validator
 * @since 0.9.0
 */
inline fun <T : Any> validator(init: ValidatorBuilderKt<T>.() -> Unit): Validator<T> {
	val builder = ValidatorBuilderKt<T>(ValidatorBuilder())
	return builder.apply(init).build()
}

/**
 * Receiver of the Kotlin DSL that builds a [Validator] for the target type [T].
 *
 * <p>
 * An instance is used as the receiver of the [validator] block. A property reference of
 * [T] adds constraints for that property, and nested objects and collection elements are
 * validated with [nest] and [forEach]. The property name is obtained with Kotlin
 * reflection, so `kotlin-reflect` must be on the classpath at runtime.
 *
 * Example:
 *
 * ```kotlin
 * val validator = validator<Person> {
 *     Person::name {
 *         notBlank()
 *         lessThanOrEqual(20)
 *     }
 *     Person::address nest {
 *         Address::street { notBlank() }
 *     }
 * }
 * ```
 *
 * A builder is mutable and not thread-safe. The [validator] function builds an immutable
 * [Validator] from it.
 *
 * @param <T> the type of the target object to validate
 * @since 0.9.0
 */
class ValidatorBuilderKt<T : Any>(private val validatorBuilder: ValidatorBuilder<T>) {

	/**
	 * Builds the validator from the current state of the underlying builder.
	 *
	 * @return a new validator
	 * @since 0.9.0
	 */
	@PublishedApi
	internal fun build(): Validator<T> {
		return validatorBuilder.build()
	}

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [CharSequenceConstraint], for example `charSequence.notBlank`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTString")
	operator fun KProperty1<T, String?>.invoke(block: CharSequenceConstraint<T, String>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [CharSequenceConstraint], for example
	 * `charSequence.notBlank`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTString")
	operator fun KProperty1<T, String?>.invoke(
		name: String,
		block: CharSequenceConstraint<T, String>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [BooleanConstraint], for example `boolean.isTrue`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTBoolean")
	operator fun KProperty1<T, Boolean?>.invoke(block: BooleanConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [BooleanConstraint], for example `boolean.isTrue`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTBoolean")
	operator fun KProperty1<T, Boolean?>.invoke(
		name: String,
		block: BooleanConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [CharacterConstraint], for example `numeric.greaterThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTChar")
	operator fun KProperty1<T, Char?>.invoke(block: CharacterConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [CharacterConstraint], for example
	 * `numeric.greaterThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTChar")
	operator fun KProperty1<T, Char?>.invoke(
		name: String,
		block: CharacterConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [ByteConstraint], for example `numeric.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTByte")
	operator fun KProperty1<T, Byte?>.invoke(block: ByteConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [ByteConstraint], for example `numeric.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTByte")
	operator fun KProperty1<T, Byte?>.invoke(
		name: String,
		block: ByteConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [ShortConstraint], for example `numeric.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTShort")
	operator fun KProperty1<T, Short?>.invoke(block: ShortConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [ShortConstraint], for example `numeric.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTShort")
	operator fun KProperty1<T, Short?>.invoke(
		name: String,
		block: ShortConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [IntegerConstraint], for example `numeric.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTInt")
	operator fun KProperty1<T, Int?>.invoke(block: IntegerConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [IntegerConstraint], for example `numeric.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTInt")
	operator fun KProperty1<T, Int?>.invoke(
		name: String,
		block: IntegerConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [LongConstraint], for example `numeric.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTLong")
	operator fun KProperty1<T, Long?>.invoke(block: LongConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [LongConstraint], for example `numeric.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTLong")
	operator fun KProperty1<T, Long?>.invoke(
		name: String,
		block: LongConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [FloatConstraint], for example `numeric.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTFloat")
	operator fun KProperty1<T, Float?>.invoke(block: FloatConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [FloatConstraint], for example `numeric.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTFloat")
	operator fun KProperty1<T, Float?>.invoke(
		name: String,
		block: FloatConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [DoubleConstraint], for example `numeric.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTDouble")
	operator fun KProperty1<T, Double?>.invoke(block: DoubleConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [DoubleConstraint], for example `numeric.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTDouble")
	operator fun KProperty1<T, Double?>.invoke(
		name: String,
		block: DoubleConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [BigIntegerConstraint], for example `numeric.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTBigInteger")
	operator fun KProperty1<T, BigInteger?>.invoke(block: BigIntegerConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [BigIntegerConstraint], for example
	 * `numeric.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTBigInteger")
	operator fun KProperty1<T, BigInteger?>.invoke(
		name: String,
		block: BigIntegerConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [BigDecimalConstraint], for example `numeric.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTBigDecimal")
	operator fun KProperty1<T, BigDecimal?>.invoke(block: BigDecimalConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [BigDecimalConstraint], for example
	 * `numeric.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTBigDecimal")
	operator fun KProperty1<T, BigDecimal?>.invoke(
		name: String,
		block: BigDecimalConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [LocalTimeConstraint], for example `temporal.future`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.10.0
	 */
	@JvmName("invokeTLocalTime")
	operator fun KProperty1<T, LocalTime?>.invoke(block: LocalTimeConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [LocalTimeConstraint], for example
	 * `temporal.future`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.10.0
	 */
	@JvmName("invokeTLocalTime")
	operator fun KProperty1<T, LocalTime?>.invoke(
		name: String,
		block: LocalTimeConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [LocalDateConstraint], for example `temporal.future`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.10.0
	 */
	@JvmName("invokeTLocalDate")
	operator fun KProperty1<T, LocalDate?>.invoke(block: LocalDateConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [LocalDateConstraint], for example
	 * `temporal.future`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.10.0
	 */
	@JvmName("invokeTLocalDate")
	operator fun KProperty1<T, LocalDate?>.invoke(
		name: String,
		block: LocalDateConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [LocalDateTimeConstraint], for example `temporal.future`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.10.0
	 */
	@JvmName("invokeTLocalDateTime")
	operator fun KProperty1<T, LocalDateTime?>.invoke(block: LocalDateTimeConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [LocalDateTimeConstraint], for example
	 * `temporal.future`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.10.0
	 */
	@JvmName("invokeTLocalDateTime")
	operator fun KProperty1<T, LocalDateTime?>.invoke(
		name: String,
		block: LocalDateTimeConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [ZonedDateTimeConstraint], for example `temporal.future`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.10.0
	 */
	@JvmName("invokeTZonedDateTime")
	operator fun KProperty1<T, ZonedDateTime?>.invoke(block: ZonedDateTimeConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [ZonedDateTimeConstraint], for example
	 * `temporal.future`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.10.0
	 */
	@JvmName("invokeTZonedDateTime")
	operator fun KProperty1<T, ZonedDateTime?>.invoke(
		name: String,
		block: ZonedDateTimeConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [OffsetDateTimeConstraint], for example `temporal.future`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.10.0
	 */
	@JvmName("invokeTOffsetDataTime")
	operator fun KProperty1<T, OffsetDateTime?>.invoke(block: OffsetDateTimeConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [OffsetDateTimeConstraint], for example
	 * `temporal.future`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.10.0
	 */
	@JvmName("invokeTOffsetDataTime")
	operator fun KProperty1<T, OffsetDateTime?>.invoke(
		name: String,
		block: OffsetDateTimeConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [InstantConstraint], for example `temporal.future`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.10.0
	 */
	@JvmName("invokeTInstant")
	operator fun KProperty1<T, Instant?>.invoke(block: InstantConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [InstantConstraint], for example `temporal.future`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.10.0
	 */
	@JvmName("invokeTInstant")
	operator fun KProperty1<T, Instant?>.invoke(
		name: String,
		block: InstantConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [YearMonthConstraint], for example `temporal.future`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.11.0
	 */
	@JvmName("invokeTYearMonth")
	operator fun KProperty1<T, YearMonth?>.invoke(block: YearMonthConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [YearMonthConstraint], for example
	 * `temporal.future`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.11.0
	 */
	@JvmName("invokeTYearMonth")
	operator fun KProperty1<T, YearMonth?>.invoke(
		name: String,
		block: YearMonthConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [YearConstraint], for example `temporal.future`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.11.0
	 */
	@JvmName("invokeTYear")
	operator fun KProperty1<T, Year?>.invoke(block: YearConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [YearConstraint], for example `temporal.future`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.11.0
	 */
	@JvmName("invokeTYear")
	operator fun KProperty1<T, Year?>.invoke(
		name: String,
		block: YearConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a nested validator for the value of the selected property.
	 *
	 * <p>
	 * The constraints of the nested [validator] are applied to the property value and their
	 * violation names are prefixed with the property name and the message key separator, for
	 * example `address.street`. A `null` property is reported as an `object.notNull`
	 * violation under the property name and the nested constraints are skipped. Use
	 * [nestIfPresent] to accept a `null` property.
	 *
	 * @param <N> the type of the nested property
	 * @param validator the validator applied to the property value
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	infix fun <N : Any> KProperty1<T, N?>.nest(validator: Validator<N>) =
		validatorBuilder.nest(this, this.name, validator)

	/**
	 * Adds a nested validator that is applied only when the selected property is not `null`.
	 *
	 * <p>
	 * This works like [nest] except that a `null` property produces no violation and the
	 * nested constraints are skipped.
	 *
	 * @param <N> the type of the nested property
	 * @param validator the validator applied to the property value
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	infix fun <N : Any> KProperty1<T, N?>.nestIfPresent(validator: Validator<N>) =
		validatorBuilder.nestIfPresent(this, this.name, validator)

	/**
	 * Adds a nested validator built by the given DSL block for the selected property.
	 *
	 * <p>
	 * Violation names are prefixed with the property name and the message key separator, for
	 * example `address.street`. A `null` property is reported as an `object.notNull`
	 * violation under the property name and the nested constraints are skipped. Use
	 * [nestIfPresent] to accept a `null` property.
	 *
	 * @param <N> the type of the nested property
	 * @param block configures the nested validator
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@Suppress("UNCHECKED_CAST")
	infix fun <N : Any> KProperty1<T, N?>.nest(block: ValidatorBuilderKt<N>.() -> Unit) =
		validatorBuilder.nest<N>(this, this.name) {
			ValidatorBuilderKt(it as ValidatorBuilder<N>).apply(block).validatorBuilder
		}

	/**
	 * Adds a nested validator built by the given DSL block that is applied only when the
	 * selected property is not `null`.
	 *
	 * <p>
	 * This works like [nest] except that a `null` property produces no violation and the
	 * nested constraints are skipped.
	 *
	 * @param <N> the type of the nested property
	 * @param block configures the nested validator
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@Suppress("UNCHECKED_CAST")
	infix fun <N : Any> KProperty1<T, N?>.nestIfPresent(block: ValidatorBuilderKt<N>.() -> Unit) =
		validatorBuilder.nestIfPresent<N>(this, this.name) {
			ValidatorBuilderKt(it as ValidatorBuilder<N>).apply(block).validatorBuilder
		}

	/**
	 * Adds a validator that is applied to each element of the selected collection property.
	 *
	 * <p>
	 * Violation names are indexed under the property name, for example
	 * `addresses[0].street`. A `null` collection property is reported as an `object.notNull`
	 * violation under the property name, and a `null` element is reported as an
	 * `object.notNull` violation under its indexed name.
	 *
	 * @param <L> the collection type of the property
	 * @param <E> the element type
	 * @param validator the validator applied to each element
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	infix fun <L : Collection<E>, E : Any> KProperty1<T, L?>.forEach(validator: Validator<E>) =
		validatorBuilder.forEach<L, E>(this, this.name, validator)

	/**
	 * Adds a validator that is applied to each element of the selected collection property.
	 *
	 * <p>
	 * Violation names are indexed under the property name, for example
	 * `addresses[0].street`. A `null` collection property is reported as an `object.notNull`
	 * violation under the property name, and a `null` element is reported as an
	 * `object.notNull` violation under its indexed name.
	 *
	 * @param <L> the collection type of the property
	 * @param <E> the element type
	 * @param block configures the validator applied to each element
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	infix fun <L : Collection<E>, E : Any> KProperty1<T, L?>.forEach(block: ValidatorBuilderKt<E>.() -> Unit) =
		validatorBuilder.forEach<L, E>(
			this,
			this.name
		) { ValidatorBuilderKt(it as ValidatorBuilder<E>).apply(block).validatorBuilder }

	/**
	 * Adds a validator that is applied to each element of the selected collection property
	 * when the property is not `null`.
	 *
	 * <p>
	 * Violation names are indexed under the property name, for example
	 * `addresses[0].street`. A `null` collection property produces no violation. A `null`
	 * element inside a non-null collection is still reported as an `object.notNull`
	 * violation under its indexed name.
	 *
	 * @param <L> the collection type of the property
	 * @param <E> the element type
	 * @param validator the validator applied to each element
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	infix fun <L : Collection<E>, E : Any> KProperty1<T, L?>.forEachIfPresent(validator: Validator<E>) =
		validatorBuilder.forEachIfPresent<L, E>(this, this.name, validator)

	/**
	 * Adds a validator that is applied to each element of the selected collection property
	 * when the property is not `null`.
	 *
	 * <p>
	 * Violation names are indexed under the property name, for example
	 * `addresses[0].street`. A `null` collection property produces no violation. A `null`
	 * element inside a non-null collection is still reported as an `object.notNull`
	 * violation under its indexed name.
	 *
	 * @param <L> the collection type of the property
	 * @param <E> the element type
	 * @param block configures the validator applied to each element
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	infix fun <L : Collection<E>, E : Any> KProperty1<T, L?>.forEachIfPresent(block: ValidatorBuilderKt<E>.() -> Unit) =
		validatorBuilder.forEachIfPresent<L, E>(this, this.name) {
			ValidatorBuilderKt(it as ValidatorBuilder<E>).apply(
				block
			).validatorBuilder
		}

	/**
	 * Adds a validator that is applied to each value of the selected map property.
	 *
	 * <p>
	 * Violation names are indexed under the property name, for example
	 * `addresses[0].street`. A `null` map property is reported as an `object.notNull`
	 * violation under the property name, and a `null` value is reported as an
	 * `object.notNull` violation under its indexed name.
	 *
	 * @param <K> the key type of the map
	 * @param <V> the value type
	 * @param validator the validator applied to each value
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("forEachMapMap")
	infix fun <K : Any, V : Any> KProperty1<T, Map<K, V>?>.forEach(validator: Validator<V>) =
		validatorBuilder.forEach<K, V>(this, this.name, validator)

	/**
	 * Adds a validator that is applied to each value of the selected map property.
	 *
	 * <p>
	 * Violation names are indexed under the property name, for example
	 * `addresses[0].street`. A `null` map property is reported as an `object.notNull`
	 * violation under the property name, and a `null` value is reported as an
	 * `object.notNull` violation under its indexed name.
	 *
	 * @param <K> the key type of the map
	 * @param <V> the value type
	 * @param block configures the validator applied to each value
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("forEachMapMap")
	infix fun <K : Any, V : Any> KProperty1<T, Map<K, V>?>.forEach(block: ValidatorBuilderKt<V>.() -> Unit) =
		validatorBuilder.forEach<K, V>(
			this,
			this.name
		) { ValidatorBuilderKt(it as ValidatorBuilder<V>).apply(block).validatorBuilder }

	/**
	 * Adds a validator that is applied to each value of the selected map property when the
	 * property is not `null`.
	 *
	 * <p>
	 * Violation names are indexed under the property name, for example
	 * `addresses[0].street`. A `null` map property produces no violation. A `null` value
	 * inside a non-null map is still reported as an `object.notNull` violation under its
	 * indexed name.
	 *
	 * @param <K> the key type of the map
	 * @param <V> the value type
	 * @param validator the validator applied to each value
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("forEachIfPresentMap")
	infix fun <K : Any, V : Any> KProperty1<T, Map<K, V>?>.forEachIfPresent(validator: Validator<V>) =
		validatorBuilder.forEachIfPresent<K, V>(this, this.name, validator)

	/**
	 * Adds a validator that is applied to each value of the selected map property when the
	 * property is not `null`.
	 *
	 * <p>
	 * Violation names are indexed under the property name, for example
	 * `addresses[0].street`. A `null` map property produces no violation. A `null` value
	 * inside a non-null map is still reported as an `object.notNull` violation under its
	 * indexed name.
	 *
	 * @param <K> the key type of the map
	 * @param <V> the value type
	 * @param block configures the validator applied to each value
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("forEachIfPresentMap")
	infix fun <K : Any, V : Any> KProperty1<T, Map<K, V>?>.forEachIfPresent(block: ValidatorBuilderKt<V>.() -> Unit) =
		validatorBuilder.forEachIfPresent<K, V>(this, this.name) {
			ValidatorBuilderKt(it as ValidatorBuilder<V>).apply(
				block
			).validatorBuilder
		}

	/**
	 * Adds a validator that is applied to each element of the selected array property.
	 *
	 * <p>
	 * Violation names are indexed under the property name, for example
	 * `addresses[0].street`. A `null` array property is reported as an `object.notNull`
	 * violation under the property name, and a `null` element is reported as an
	 * `object.notNull` violation under its indexed name.
	 *
	 * @param <E> the element type
	 * @param validator the validator applied to each element
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("forEachMapArray")
	infix fun <E : Any> KProperty1<T, Array<E>?>.forEach(validator: Validator<E>) =
		validatorBuilder.forEach<E>(this, this.name, validator)

	/**
	 * Adds a validator that is applied to each element of the selected array property.
	 *
	 * <p>
	 * Violation names are indexed under the property name, for example
	 * `addresses[0].street`. A `null` array property is reported as an `object.notNull`
	 * violation under the property name, and a `null` element is reported as an
	 * `object.notNull` violation under its indexed name.
	 *
	 * @param <E> the element type
	 * @param block configures the validator applied to each element
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("forEachMapArray")
	infix fun <E : Any> KProperty1<T, Array<E>?>.forEach(block: ValidatorBuilderKt<E>.() -> Unit) =
		validatorBuilder.forEach<E>(
			this,
			this.name
		) { ValidatorBuilderKt(it as ValidatorBuilder<E>).apply(block).validatorBuilder }

	/**
	 * Adds a validator that is applied to each element of the selected array property when
	 * the property is not `null`.
	 *
	 * <p>
	 * Violation names are indexed under the property name, for example
	 * `addresses[0].street`. A `null` array property produces no violation. A `null` element
	 * inside a non-null array is still reported as an `object.notNull` violation under its
	 * indexed name.
	 *
	 * @param <E> the element type
	 * @param validator the validator applied to each element
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("forEachIfPresentArray")
	infix fun <E : Any> KProperty1<T, Array<E>?>.forEachIfPresent(validator: Validator<E>) =
		validatorBuilder.forEachIfPresent<E>(this, this.name, validator)

	/**
	 * Adds a validator that is applied to each element of the selected array property when
	 * the property is not `null`.
	 *
	 * <p>
	 * Violation names are indexed under the property name, for example
	 * `addresses[0].street`. A `null` array property produces no violation. A `null` element
	 * inside a non-null array is still reported as an `object.notNull` violation under its
	 * indexed name.
	 *
	 * @param <E> the element type
	 * @param block configures the validator applied to each element
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("forEachIfPresentArray")
	infix fun <E : Any> KProperty1<T, Array<E>?>.forEachIfPresent(block: ValidatorBuilderKt<E>.() -> Unit) =
		validatorBuilder.forEachIfPresent<E>(this, this.name) {
			ValidatorBuilderKt(it as ValidatorBuilder<E>).apply(
				block
			).validatorBuilder
		}

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [CollectionConstraint], for example `container.lessThan`.
	 *
	 * @param <L> the collection type of the property
	 * @param <E> the element type
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTCollection")
	operator fun <L : Collection<E>, E : Any> KProperty1<T, L?>.invoke(block: CollectionConstraint<T, L, E>.() -> Unit) =
		validatorBuilder.constraint<L, E>(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [CollectionConstraint], for example
	 * `container.lessThan`.
	 *
	 * @param <L> the collection type of the property
	 * @param <E> the element type
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTCollection")
	operator fun <L : Collection<E>, E : Any> KProperty1<T, L?>.invoke(
		name: String,
		block: CollectionConstraint<T, L, E>.() -> Unit
	) =
		validatorBuilder.constraint<L, E>(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [MapConstraint], for example `container.lessThan`.
	 *
	 * @param <K> the key type of the map
	 * @param <V> the value type
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTMap")
	operator fun <K : Any, V : Any> KProperty1<T, Map<K, V>?>.invoke(block: MapConstraint<T, K, V>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [MapConstraint], for example `container.lessThan`.
	 *
	 * @param <K> the key type of the map
	 * @param <V> the value type
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTMap")
	operator fun <K : Any, V : Any> KProperty1<T, Map<K, V>?>.invoke(
		name: String,
		block: MapConstraint<T, K, V>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [ObjectArrayConstraint], for example `container.lessThan`.
	 *
	 * @param <E> the element type
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTArray")
	operator fun <E : Any> KProperty1<T, Array<E>?>.invoke(block: ObjectArrayConstraint<T, E>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [ObjectArrayConstraint], for example
	 * `container.lessThan`.
	 *
	 * @param <E> the element type
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTArray")
	operator fun <E : Any> KProperty1<T, Array<E>?>.invoke(
		name: String,
		block: ObjectArrayConstraint<T, E>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [EnumConstraint], for example `object.oneOf`.
	 *
	 * @param <E> the enum type of the property
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.14.0
	 */
	operator fun <E : Enum<E>> KProperty1<T, E?>.invoke(block: EnumConstraint<T, E>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }


	/**
	 * Adds an object constraint for the selected property.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [ObjectConstraint], for example `object.notNull`.
	 *
	 * @param <E> the type of the property
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	infix fun <E : Any> KProperty1<T, E?>.onObject(block: ObjectConstraint<T, E>.() -> Unit) =
		validatorBuilder.constraintOnObject<E>(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [BooleanArrayConstraint], for example `container.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTBooleanArray")
	operator fun KProperty1<T, BooleanArray?>.invoke(block: BooleanArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [BooleanArrayConstraint], for example
	 * `container.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTBooleanArray")
	operator fun KProperty1<T, BooleanArray?>.invoke(
		name: String,
		block: BooleanArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [CharArrayConstraint], for example `container.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTCharArray")
	operator fun KProperty1<T, CharArray?>.invoke(block: CharArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [CharArrayConstraint], for example
	 * `container.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTCharArray")
	operator fun KProperty1<T, CharArray?>.invoke(
		name: String,
		block: CharArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [ByteArrayConstraint], for example `container.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTByteArray")
	operator fun KProperty1<T, ByteArray?>.invoke(block: ByteArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [ByteArrayConstraint], for example
	 * `container.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTByteArray")
	operator fun KProperty1<T, ByteArray?>.invoke(
		name: String,
		block: ByteArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [ShortArrayConstraint], for example `container.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTShortArray")
	operator fun KProperty1<T, ShortArray?>.invoke(block: ShortArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [ShortArrayConstraint], for example
	 * `container.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTShortArray")
	operator fun KProperty1<T, ShortArray?>.invoke(
		name: String,
		block: ShortArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [IntArrayConstraint], for example `container.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTIntArray")
	operator fun KProperty1<T, IntArray?>.invoke(block: IntArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [IntArrayConstraint], for example
	 * `container.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTIntArray")
	operator fun KProperty1<T, IntArray?>.invoke(
		name: String,
		block: IntArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [LongArrayConstraint], for example `container.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTLongArray")
	operator fun KProperty1<T, LongArray?>.invoke(block: LongArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [LongArrayConstraint], for example
	 * `container.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTLongArray")
	operator fun KProperty1<T, LongArray?>.invoke(
		name: String,
		block: LongArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [FloatArrayConstraint], for example `container.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTFloatArray")
	operator fun KProperty1<T, FloatArray?>.invoke(block: FloatArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [FloatArrayConstraint], for example
	 * `container.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTFloatArray")
	operator fun KProperty1<T, FloatArray?>.invoke(
		name: String,
		block: FloatArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference.
	 *
	 * <p>
	 * The property name is used as the violation name and as message argument `{0}`. The
	 * checks configured by [block] are evaluated against the property value. A `null` value
	 * is valid unless [block] adds a check that rejects it, such as `notNull`. Violations
	 * use the message ids of [DoubleArrayConstraint], for example `container.lessThan`.
	 *
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTDoubleArray")
	operator fun KProperty1<T, DoubleArray?>.invoke(block: DoubleArrayConstraint<T>.() -> Unit) =
		validatorBuilder.constraint(this, this.name) { it.apply(block) }

	/**
	 * Adds a constraint for the property selected by this property reference, using the
	 * given name in violation names and messages.
	 *
	 * <p>
	 * The checks configured by [block] are evaluated against the property value and
	 * violations are reported under [name] with [name] as message argument `{0}`. A `null`
	 * value is valid unless [block] adds a check that rejects it, such as `notNull`.
	 * Violations use the message ids of [DoubleArrayConstraint], for example
	 * `container.lessThan`.
	 *
	 * @param name the property name used in violation names and messages
	 * @param block configures the constraint
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	@JvmName("invokeTDoubleArray")
	operator fun KProperty1<T, DoubleArray?>.invoke(
		name: String,
		block: DoubleArrayConstraint<T>.() -> Unit
	) =
		validatorBuilder.constraint(this, name) { it.apply(block) }

	/**
	 * Adds a validator that is applied only when the given condition is met.
	 *
	 * <p>
	 * The condition is evaluated with the target object and the constraint context supplied
	 * to the validation call. When it returns `true`, the constraints configured by [block]
	 * are applied to the target and their violations are reported. Otherwise they are
	 * skipped.
	 *
	 * @param condition the condition that decides whether the constraints are applied
	 * @param block configures the conditional validator
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	fun onCondition(
		condition: ConstraintCondition<T>,
		block: ValidatorBuilderKt<T>.() -> Unit
	) =
		validatorBuilder.constraintOnCondition(condition) {
			ValidatorBuilderKt(it).apply(block).validatorBuilder
		}

	/**
	 * Adds a validator that is applied only when the given constraint group is active.
	 *
	 * <p>
	 * This is equivalent to `onCondition(group.toCondition(), block)`: the constraints
	 * configured by [block] are applied when the group passed to the validation call has the
	 * same name as [group], for example `validator.validate(target,
	 * ConstraintGroup.of("UPDATE"))`.
	 *
	 * @param group the constraint group that activates the validator
	 * @param block configures the group-specific validator
	 * @return the underlying [ValidatorBuilder]
	 * @since 0.9.0
	 */
	fun onGroup(group: ConstraintGroup, block: ValidatorBuilderKt<T>.() -> Unit) =
		validatorBuilder.constraintOnCondition(group.toCondition()) {
			ValidatorBuilderKt(it).apply(block).validatorBuilder
		}

	/**
	 * Returns the underlying [ValidatorBuilder].
	 *
	 * <p>
	 * This gives access to the Java API of the builder for constraints and settings that the
	 * DSL does not cover, so that they can be combined with the constraints of the DSL.
	 *
	 * @return the underlying builder
	 * @since 0.12.0
	 */
	fun unwrap() = validatorBuilder
}
