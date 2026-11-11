/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Factories that create validators with a shared configuration.
 *
 * <p>
 * {@link am.ik.yavi.factory.ValidatorFactory} returns a {@link am.ik.yavi.core.Validator}
 * and {@link am.ik.yavi.factory.BiConsumerFactory} returns a
 * {@link java.util.function.BiConsumer} that reports violations to an
 * {@link am.ik.yavi.core.ErrorHandler}. Both let an application define the message key
 * separator, the {@link am.ik.yavi.message.MessageFormatter} and, for the
 * {@code BiConsumer} variant, the error handler once, for example as beans in an IoC
 * container, and create validators from that configuration afterwards.
 *
 * <p>
 * The returned validators are immutable and thread-safe. A factory itself holds only the
 * shared configuration and can be reused.
 *
 * @since 0.5.0
 */
@NonNullApi
package am.ik.yavi.factory;

import am.ik.yavi.jsr305.NonNullApi;
