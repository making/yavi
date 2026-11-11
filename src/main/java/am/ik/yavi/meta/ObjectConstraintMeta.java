/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

/**
 * Meta object for a property of an arbitrary type {@code E}.
 *
 * <p>
 * The annotation processor generates a constant of this type for a member annotated with
 * {@link ConstraintTarget} or {@link ConstraintArguments} whose type has no dedicated
 * meta interface, for example a custom type or a collection. It is also accepted by the
 * {@code nest} methods of {@link am.ik.yavi.builder.ValidatorBuilder} to select a nested
 * property.
 *
 * @param <T> the type from which the value is extracted: a bean type or an
 * {@code am.ik.yavi.arguments.ArgumentsN} container
 * @param <E> the type of the property value
 * @since 0.4.0
 */
public interface ObjectConstraintMeta<T, E> extends ConstraintMeta<T, E> {

}
