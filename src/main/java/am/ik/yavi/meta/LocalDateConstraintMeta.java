/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

import java.time.LocalDate;

/**
 * Meta object for a property that holds a {@link LocalDate} value.
 *
 * <p>
 * The annotation processor generates a constant of this type for a member annotated with
 * {@link ConstraintTarget} or {@link ConstraintArguments}. The constant returns the
 * property name and a function that extracts the value, and is passed to a
 * {@code constraint(...)} method of {@link am.ik.yavi.builder.ValidatorBuilder}.
 *
 * @param <T> the type from which the value is extracted: a bean type or an
 * {@code am.ik.yavi.arguments.ArgumentsN} container
 * @since 0.10.0
 */
public interface LocalDateConstraintMeta<T> extends ConstraintMeta<T, LocalDate> {

}
