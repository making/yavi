/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Base classes for the constraint types in {@code am.ik.yavi.constraint}.
 *
 * <p>
 * Each base class implements the fluent methods of the corresponding value category and
 * leaves the concrete comparison to its subclasses:
 * </p>
 * <ul>
 * <li>{@link am.ik.yavi.constraint.base.NumericConstraintBase} for numeric values</li>
 * <li>{@link am.ik.yavi.constraint.base.ContainerConstraintBase} for collections, maps
 * and arrays</li>
 * <li>{@link am.ik.yavi.constraint.base.TemporalConstraintBase} and its subclasses for
 * temporal values</li>
 * <li>{@link am.ik.yavi.constraint.base.ConstraintBase} for all other values</li>
 * </ul>
 *
 * <p>
 * The classes are public so that the constraint types of this library can extend them;
 * they are not meant to be used directly.
 * </p>
 * @since 0.0.1
 */
@NonNullApi
package am.ik.yavi.constraint.base;

import am.ik.yavi.jsr305.NonNullApi;
