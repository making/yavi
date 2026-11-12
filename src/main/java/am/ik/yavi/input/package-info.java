/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Validation of an untyped input, such as a parsed JSON document, that creates a typed
 * object.
 *
 * <p>
 * {@link am.ik.yavi.input.MapInputValidator} reads the properties of an object from a
 * {@code Map<String, Object>} and {@link am.ik.yavi.input.DiscriminatedUnionValidator}
 * selects the validator of such a map by the value of one of its keys. Both are
 * {@link am.ik.yavi.core.ValueValidator}s, so they nest and combine like any other
 * validator, and both read the tree model of a JSON library instead of a map with an
 * {@link am.ik.yavi.core.InputReader}.
 *
 * @since 0.18.0
 */
@NullMarked
package am.ik.yavi.input;

import org.jspecify.annotations.NullMarked;
