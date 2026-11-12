/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Entry point for the validation of arguments and of single values.
 *
 * <p>
 * {@link am.ik.yavi.validator.Yavi#arguments()} returns a
 * {@link am.ik.yavi.builder.YaviArguments} builder, the shortest way to build an
 * arguments validator that creates an object only from valid arguments. Methods such as
 * {@link am.ik.yavi.validator.Yavi#_string(java.util.function.Function)} return a
 * validator of a single value.
 *
 * @since 0.14.0
 */
@NullMarked
package am.ik.yavi.validator;

import org.jspecify.annotations.NullMarked;
