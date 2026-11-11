/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Annotation processor that generates constraint metadata classes.
 *
 * <p>
 * {@link am.ik.yavi.processor.ConstraintMetaProcessor} is registered through
 * {@code META-INF/services/javax.annotation.processing.Processor} and runs as part of
 * compilation when this artifact is on the annotation processor path. It creates a
 * {@code _XxxMeta} class for members annotated with
 * {@link am.ik.yavi.meta.ConstraintTarget} and a {@code _XxxArgumentsMeta} class for a
 * constructor or method annotated with {@link am.ik.yavi.meta.ConstraintArguments}, in
 * the same package as the annotated type. The generated sources are written to the
 * annotation processor output directory, for example
 * {@code target/generated-sources/annotations}.
 */
@NonNullApi
package am.ik.yavi.processor;

import am.ik.yavi.jsr305.NonNullApi;
