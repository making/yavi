/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Compile-time annotations and generated metadata types for type-safe constraints.
 *
 * <p>
 * {@link am.ik.yavi.meta.ConstraintTarget} and
 * {@link am.ik.yavi.meta.ConstraintArguments} mark members whose values the annotation
 * processor turns into {@link am.ik.yavi.meta.ConstraintMeta} constants. A generated
 * class such as {@code _UserFormMeta} holds one constant per property, and the constants
 * are passed to {@code constraint(...)} methods of
 * {@link am.ik.yavi.builder.ValidatorBuilder} instead of string literals.
 *
 * <p>
 * The annotations have {@link java.lang.annotation.RetentionPolicy#SOURCE} retention, so
 * they are not present at runtime.
 *
 * <p>
 * Example: <pre>{@code
 * Validator<UserForm> validator = ValidatorBuilder.<UserForm>of()
 *     .constraint(_UserFormMeta.NAME, c -> c.notBlank())
 *     .constraint(_UserFormMeta.EMAIL, c -> c.notBlank().email())
 *     .build();
 * }</pre>
 */
@NullMarked
package am.ik.yavi.meta;

import org.jspecify.annotations.NullMarked;
