/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * JSR 305 based nullability annotations used in the YAVI API.
 *
 * <p>
 * {@link am.ik.yavi.jsr305.NonNull} and {@link am.ik.yavi.jsr305.Nullable} declare the
 * nullability of parameters, return values and fields, while
 * {@link am.ik.yavi.jsr305.NonNullApi} declares the default for a whole package. The
 * annotations leverage JSR 305 meta-annotations so that nullability is visible to tools
 * with JSR 305 support and to Kotlin's Java interop, which uses them to infer the
 * nullability of the YAVI API.
 *
 * @since 0.0.17
 */
package am.ik.yavi.jsr305;
