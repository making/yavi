/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

import java.util.function.Function;

/**
 * @since 0.4.0
 */
public interface ConstraintMeta<T, V> {

	String name();

	Function<T, V> toValue();

}
