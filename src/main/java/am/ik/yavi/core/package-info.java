/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Core validation engine of YAVI.
 *
 * <p>
 * {@link am.ik.yavi.core.Validatable} and its main implementation
 * {@link am.ik.yavi.core.Validator} validate an object against a set of constraints and
 * return {@link am.ik.yavi.core.ConstraintViolations}. Validators are usually created
 * with {@link am.ik.yavi.builder.ValidatorBuilder}; the built validator is immutable and
 * can be reused.
 *
 * <p>
 * Constraints can be applied conditionally. {@link am.ik.yavi.core.ConstraintCondition}
 * decides at validation time whether a validator is applied, while
 * {@link am.ik.yavi.core.ConstraintGroup} selects constraints by a named group such as
 * {@code CREATE} or {@code UPDATE}. {@link am.ik.yavi.core.ConstraintContext} carries
 * attributes for such conditions. Validators also support nested object and collection
 * validation, and a fail-fast mode that stops at the first violation
 * ({@link am.ik.yavi.core.Validatable#failFast(boolean)}).
 *
 * <p>
 * For applicative style validation, {@link am.ik.yavi.core.ValueValidator} validates an
 * input and produces a transformed value wrapped in {@link am.ik.yavi.core.Validated},
 * accumulating violations instead of stopping at the first one.
 * {@link am.ik.yavi.core.ApplicativeValidator} is the variant that returns the validated
 * object itself. These types compose with the generated
 * {@code am.ik.yavi.arguments.ArgumentsNValidator} types to validate constructor
 * arguments and create only valid objects.
 *
 * <p>
 * Constraints treat {@code null} as valid by default unless the constraint is built to
 * reject null (see {@link am.ik.yavi.core.NullAs}). Violation messages are identified by
 * the message ids in {@link am.ik.yavi.core.ViolationMessage} and formatted through
 * {@link am.ik.yavi.message.MessageFormatter}.
 */
@NullMarked
package am.ik.yavi.core;

import org.jspecify.annotations.NullMarked;
