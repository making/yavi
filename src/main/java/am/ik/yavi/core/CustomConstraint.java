/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Objects;
import java.util.function.Predicate;

import am.ik.yavi.jsr305.Nullable;

public interface CustomConstraint<V> extends Predicate<V>, ViolationMessage, ViolatedArguments<V> {

	Objects[] EMPTY_ARRAY = new Objects[0];

	@Override
	default Object[] arguments(@Nullable V violatedValue) {
		return EMPTY_ARRAY;
	}

}
