package am.ik.yavi.builder

import am.ik.yavi.constraint.*
import am.ik.yavi.constraint.array.*
import am.ik.yavi.core.ConstraintCondition
import am.ik.yavi.core.ConstraintGroup
import am.ik.yavi.core.Validator
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KProperty1

inline fun <T> validator(init: ValidatorBuilderKt<T>.() -> Unit): Validator<T> {
    val builder = ValidatorBuilderKt<T>(ValidatorBuilder())
    return builder.apply(init).build()
}

class ValidatorBuilderKt<T>(private val validatorBuilder: ValidatorBuilder<T>) {

    fun build(): Validator<T> {
        return validatorBuilder.build()
    }

    @JvmName("invokeTString")
    operator fun KProperty1<T, String?>.invoke(block: CharSequenceConstraint<T, String?>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTBoolean")
    operator fun KProperty1<T, Boolean?>.invoke(block: BooleanConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTChar")
    operator fun KProperty1<T, Char?>.invoke(block: CharacterConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTByte")
    operator fun KProperty1<T, Byte?>.invoke(block: ByteConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTShort")
    operator fun KProperty1<T, Short?>.invoke(block: ShortConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTInt")
    operator fun KProperty1<T, Int?>.invoke(block: IntegerConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTLong")
    operator fun KProperty1<T, Long?>.invoke(block: LongConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTFloat")
    operator fun KProperty1<T, Float?>.invoke(block: FloatConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTDouble")
    operator fun KProperty1<T, Double?>.invoke(block: DoubleConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTBigInteger")
    operator fun KProperty1<T, BigInteger?>.invoke(block: BigIntegerConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTBigDecimal")
    operator fun KProperty1<T, BigDecimal?>.invoke(block: BigDecimalConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    /** Q: this will be required for each type, right? */
    infix fun KProperty1<T, String?>.required(block: CharSequenceConstraint<T, String?>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.notNull().apply(block) }

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

    infix fun <L: Collection<E>?, E> KProperty1<T, L?>.forEach(validator: Validator<E>) =
        validatorBuilder.forEach(this, this.name, validator)

    infix fun <L: Collection<E>?, E> KProperty1<T, L?>.forEach(block: ValidatorBuilderKt<E>.() -> Unit) =
        validatorBuilder.forEach(this, this.name) { ValidatorBuilderKt(it).apply(block).validatorBuilder }

    infix fun <L: Collection<E>?, E> KProperty1<T, L?>.forEachIfPresent(validator: Validator<E>) =
        validatorBuilder.forEachIfPresent(this, this.name, validator)

    infix fun <L: Collection<E>?, E> KProperty1<T, L?>.forEachIfPresent(block: ValidatorBuilderKt<E>.() -> Unit) =
        validatorBuilder.forEachIfPresent(this, this.name) { ValidatorBuilderKt(it).apply(block).validatorBuilder }

    @JvmName("forEachMapMap")
    infix fun <K, V> KProperty1<T, Map<K, V>?>.forEach(validator: Validator<V>) =
        validatorBuilder.forEach(this, this.name, validator)

    @JvmName("forEachMapMap")
    infix fun <K, V> KProperty1<T, Map<K, V>?>.forEach(block: ValidatorBuilderKt<V>.() -> Unit) =
        validatorBuilder.forEach(this, this.name) { ValidatorBuilderKt(it).apply(block).validatorBuilder }

    @JvmName("forEachIfPresentMap")
    infix fun <K, V> KProperty1<T, Map<K, V>?>.forEachIfPresent(validator: Validator<V>) =
        validatorBuilder.forEachIfPresent(this, this.name, validator)

    @JvmName("forEachIfPresentMap")
    infix fun <K, V> KProperty1<T, Map<K, V>?>.forEachIfPresent(block: ValidatorBuilderKt<V>.() -> Unit) =
        validatorBuilder.forEachIfPresent(this, this.name) { ValidatorBuilderKt(it).apply(block).validatorBuilder }

    @JvmName("forEachMapArray")
    infix fun <E> KProperty1<T, Array<E>?>.forEach(validator: Validator<E>) =
        validatorBuilder.forEach(this, this.name, validator)

    @JvmName("forEachMapArray")
    infix fun <E> KProperty1<T, Array<E>?>.forEach(block: ValidatorBuilderKt<E>.() -> Unit) =
        validatorBuilder.forEach(this, this.name) { ValidatorBuilderKt(it).apply(block).validatorBuilder }

    @JvmName("forEachIfPresentArray")
    infix fun <E> KProperty1<T, Array<E>?>.forEachIfPresent(validator: Validator<E>) =
        validatorBuilder.forEachIfPresent(this, this.name, validator)

    @JvmName("forEachIfPresentArray")
    infix fun <E> KProperty1<T, Array<E>?>.forEachIfPresent(block: ValidatorBuilderKt<E>.() -> Unit) =
        validatorBuilder.forEachIfPresent(this, this.name) { ValidatorBuilderKt(it).apply(block).validatorBuilder }

    @JvmName("invokeTCollection")
    operator fun <L : Collection<E>?, E> KProperty1<T, L?>.invoke(block: CollectionConstraint<T, L?, E>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTMap")
    operator fun <K, V> KProperty1<T, Map<K, V>?>.invoke(block: MapConstraint<T, K, V>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTArray")
    operator fun <E> KProperty1<T, Array<E>?>.invoke(block: ObjectArrayConstraint<T, E>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    infix fun <E> KProperty1<T, E?>.onObject(block: ObjectConstraint<T, E?>.() -> Unit) =
        validatorBuilder.constraintOnObject(this, this.name) { it.apply(block) }

    @JvmName("invokeTBooleanArray")
    operator fun KProperty1<T, BooleanArray?>.invoke(block: BooleanArrayConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTCharArray")
    operator fun KProperty1<T, CharArray?>.invoke(block: CharArrayConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTByteArray")
    operator fun KProperty1<T, ByteArray?>.invoke(block: ByteArrayConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTShortArray")
    operator fun KProperty1<T, ShortArray?>.invoke(block: ShortArrayConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTIntArray")
    operator fun KProperty1<T, IntArray?>.invoke(block: IntArrayConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTLongArray")
    operator fun KProperty1<T, LongArray?>.invoke(block: LongArrayConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTFloatArray")
    operator fun KProperty1<T, FloatArray?>.invoke(block: FloatArrayConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

    @JvmName("invokeTDoubleArray")
    operator fun KProperty1<T, DoubleArray?>.invoke(block: DoubleArrayConstraint<T>.() -> Unit) =
        validatorBuilder.constraint(this, this.name) { it.apply(block) }

     fun onCondition(condition: ConstraintCondition<T>, block: ValidatorBuilderKt<T>.() -> Unit) =
         validatorBuilder.constraintOnCondition(condition) {
             ValidatorBuilderKt(it).apply(block).validatorBuilder
         }

     fun onGroup(group: ConstraintGroup, block: ValidatorBuilderKt<T>.() -> Unit) =
         validatorBuilder.constraintOnCondition(group.toCondition()) {
             ValidatorBuilderKt(it).apply(block).validatorBuilder
         }

}