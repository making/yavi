/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder

import am.ik.yavi.arguments.Arguments1
import am.ik.yavi.constraint.EnumConstraint
import am.ik.yavi.constraint.ObjectConstraint

/**
 * Adds the next argument as a constant of the reified enum type [E] with the given name
 * and configured constraints.
 *
 * <p>
 * The type argument replaces the `Class` argument of the Java method, so the enum type is
 * written once:
 *
 * ```kotlin
 * Yavi.schema()
 *     ._enum<Role>("role") { it.oneOf(Role.ADMIN) }
 * ```
 *
 * @param E the enum type of the argument
 * @param name the property name used in violation names and messages
 * @param constraints function that configures the constraints
 * @return a builder for the next argument
 * @since 0.18.0
 */
inline fun <reified E : Enum<E>> YaviSchema._enum(
    name: String,
    noinline constraints: (EnumConstraint<Arguments1<E>, E>) -> EnumConstraint<Arguments1<E>, E>
): Arguments1ValidatorBuilder.Added<E, E> = this._enum(name, E::class.java, constraints)

/**
 * Adds the next argument as a constant of the reified enum type [E] with the given name and
 * no constraints.
 *
 * <p>
 * The type argument replaces the `Class` argument of the Java method:
 *
 * ```kotlin
 * Yavi.schema()._enum<Role>("role")
 * ```
 *
 * @param E the enum type of the argument
 * @param name the property name recorded for the argument
 * @return a builder for the next argument
 * @since 0.18.0
 */
inline fun <reified E : Enum<E>> YaviSchema._enum(name: String): Arguments1ValidatorBuilder.Added<E, E> =
    this._enum(name, E::class.java)

/**
 * Adds the next argument as an object of the reified type [T] with the given name and
 * configured constraints.
 *
 * <p>
 * The type argument replaces the `Class` argument of the Java method, so the type is
 * written once:
 *
 * ```kotlin
 * Yavi.schema()
 *     ._object<UUID>("id") { it.notNull() }
 * ```
 *
 * @param T type of the argument
 * @param name the property name used in violation names and messages
 * @param constraints function that configures the constraints
 * @return a builder for the next argument
 * @since 0.18.0
 */
inline fun <reified T : Any> YaviSchema._object(
    name: String,
    noinline constraints: (ObjectConstraint<Arguments1<T>, T>) -> ObjectConstraint<Arguments1<T>, T>
): Arguments1ValidatorBuilder.Added<T, T> = this._object(name, T::class.java, constraints)

/**
 * Adds the next argument as an object of the reified type [T] with the given name and no
 * constraints.
 *
 * <p>
 * The type argument replaces the `Class` argument of the Java method:
 *
 * ```kotlin
 * Yavi.schema()._object<UUID>("id")
 * ```
 *
 * @param T type of the argument
 * @param name the property name recorded for the argument
 * @return a builder for the next argument
 * @since 0.18.0
 */
inline fun <reified T : Any> YaviSchema._object(name: String): Arguments1ValidatorBuilder.Added<T, T> =
    this._object(name, T::class.java)
