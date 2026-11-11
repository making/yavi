/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

/**
 * Applicative validator class
 *
 * @param <T> Target class
 * @since 0.6.0
 */
@FunctionalInterface
public interface ApplicativeValidator<T> extends ValueValidator<T, T> {

}