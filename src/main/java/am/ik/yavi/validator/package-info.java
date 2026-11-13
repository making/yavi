/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Entry point for describing an input as a schema and for the validation of single
 * values.
 *
 * <p>
 * {@link am.ik.yavi.validator.Yavi#schema()} returns a
 * {@link am.ik.yavi.builder.YaviSchema} builder, the shortest way to build a validator
 * that creates an object only from valid arguments or from the valid properties of a map.
 * Methods such as {@link am.ik.yavi.validator.Yavi#_string(java.util.function.Function)}
 * return a validator of a single value.
 *
 * @since 0.14.0
 */
@NullMarked
package am.ik.yavi.validator;

import org.jspecify.annotations.NullMarked;
