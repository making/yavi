/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.validator

import am.ik.yavi.arguments.Arguments1
import am.ik.yavi.arguments.EnumValidator
import am.ik.yavi.arguments.ObjectValidator
import am.ik.yavi.constraint.EnumConstraint
import am.ik.yavi.constraint.ObjectConstraint

/*
 * A static method of Yavi cannot take a reified type argument, so these top-level
 * functions replace the Class argument of Yavi._enum and Yavi._object for Kotlin callers.
 * They are imported by name:
 *
 *     import am.ik.yavi.validator._enum
 *     import am.ik.yavi.validator._object
 *
 * The Java calls, such as Yavi._enum("role", Role.class), are unaffected.
 */

/**
 * Returns a validator of a constant of the reified enum type [E] with the given name and
 * configured constraints.
 *
 * <p>
 * Import the function by name to use it:
 *
 * ```kotlin
 * import am.ik.yavi.validator._enum
 *
 * val role: Arguments1Validator<Role, Role> = _enum<Role>("role") { it.oneOf(Role.ADMIN) }
 * ```
 *
 * @param E the enum type of the value
 * @param name the name used in violation names and messages
 * @param constraints function that configures the constraints
 * @return a validator of the value
 * @since 0.18.0
 */
inline fun <reified E : Enum<E>> _enum(
    name: String,
    noinline constraints: (EnumConstraint<Arguments1<E>, E>) -> EnumConstraint<Arguments1<E>, E>
): EnumValidator.Reversible<E> = Yavi._enum(name, E::class.java, constraints)

/**
 * Returns a validator of a constant of the reified enum type [E] with the given name and
 * no constraints.
 *
 * <p>
 * Import the function by name to use it:
 *
 * ```kotlin
 * import am.ik.yavi.validator._enum
 *
 * val role: Arguments1Validator<Role, Role> = _enum<Role>("role")
 * ```
 *
 * @param E the enum type of the value
 * @param name the name used in violation names and messages
 * @return a validator of the value
 * @since 0.18.0
 */
inline fun <reified E : Enum<E>> _enum(name: String): EnumValidator.Reversible<E> =
    Yavi._enum(name, E::class.java)

/**
 * Returns a validator of a constant of the reified enum type [E] without a name, with the
 * given constraints.
 *
 * <p>
 * Import the function by name to use it:
 *
 * ```kotlin
 * import am.ik.yavi.validator._enum
 *
 * val role = _enum<Role> { it.oneOf(Role.ADMIN) }
 * ```
 *
 * @param E the enum type of the value
 * @param constraints function that configures the constraints
 * @return a validator of the value
 * @since 0.18.0
 */
inline fun <reified E : Enum<E>> _enum(
    noinline constraints: (EnumConstraint<Arguments1<E>, E>) -> EnumConstraint<Arguments1<E>, E>
): EnumValidator.Reversible<E> = Yavi._enum(E::class.java, constraints)

/**
 * Returns a validator of a constant of the reified enum type [E] without a name and
 * without constraints.
 *
 * <p>
 * Import the function by name to use it:
 *
 * ```kotlin
 * import am.ik.yavi.validator._enum
 *
 * val role: Arguments1Validator<Role, Role> = _enum<Role>()
 * ```
 *
 * @param E the enum type of the value
 * @return a validator of the value
 * @since 0.18.0
 */
inline fun <reified E : Enum<E>> _enum(): EnumValidator.Reversible<E> = Yavi._enum(E::class.java)

/**
 * Returns a validator of a value of the reified type [T] with the given name and
 * configured constraints.
 *
 * <p>
 * Import the function by name to use it:
 *
 * ```kotlin
 * import am.ik.yavi.validator._object
 *
 * val id: Arguments1Validator<UUID, UUID> = _object<UUID>("id") { it.notNull() }
 * ```
 *
 * @param T the type of the value
 * @param name the name used in violation names and messages
 * @param constraints function that configures the constraints
 * @return a validator of the value
 * @since 0.18.0
 */
inline fun <reified T : Any> _object(
    name: String,
    noinline constraints: (ObjectConstraint<Arguments1<T>, T>) -> ObjectConstraint<Arguments1<T>, T>
): ObjectValidator.Reversible<T> = Yavi._object(name, T::class.java, constraints)

/**
 * Returns a validator of a value of the reified type [T] with the given name and no
 * constraints.
 *
 * <p>
 * Import the function by name to use it:
 *
 * ```kotlin
 * import am.ik.yavi.validator._object
 *
 * val id: Arguments1Validator<UUID, UUID> = _object<UUID>("id")
 * ```
 *
 * @param T the type of the value
 * @param name the name used in violation names and messages
 * @return a validator of the value
 * @since 0.18.0
 */
inline fun <reified T : Any> _object(name: String): ObjectValidator.Reversible<T> =
    Yavi._object(name, T::class.java)

/**
 * Returns a validator of a value of the reified type [T] without a name, with the given
 * constraints.
 *
 * <p>
 * Import the function by name to use it:
 *
 * ```kotlin
 * import am.ik.yavi.validator._object
 *
 * val id = _object<UUID> { it.notNull() }
 * ```
 *
 * @param T the type of the value
 * @param constraints function that configures the constraints
 * @return a validator of the value
 * @since 0.18.0
 */
inline fun <reified T : Any> _object(
    noinline constraints: (ObjectConstraint<Arguments1<T>, T>) -> ObjectConstraint<Arguments1<T>, T>
): ObjectValidator.Reversible<T> = Yavi._object(T::class.java, constraints)

/**
 * Returns a validator of a value of the reified type [T] without a name and without
 * constraints.
 *
 * <p>
 * Import the function by name to use it:
 *
 * ```kotlin
 * import am.ik.yavi.validator._object
 *
 * val id: Arguments1Validator<UUID, UUID> = _object<UUID>()
 * ```
 *
 * @param T the type of the value
 * @return a validator of the value
 * @since 0.18.0
 */
inline fun <reified T : Any> _object(): ObjectValidator.Reversible<T> = Yavi._object(T::class.java)
