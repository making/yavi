/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Constraints on array values.
 *
 * <p>
 * The classes cover object arrays and every primitive array type. They provide
 * containment and the size constraints inherited from
 * {@link am.ik.yavi.constraint.base.ContainerConstraintBase}. A {@code null} array is
 * considered valid and the checks are skipped, except for
 * {@link am.ik.yavi.constraint.base.ContainerConstraintBase#notEmpty()}.
 * </p>
 * @since 0.0.17
 */
@NonNullApi
package am.ik.yavi.constraint.array;

import am.ik.yavi.jsr305.NonNullApi;
