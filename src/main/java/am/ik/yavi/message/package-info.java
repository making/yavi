/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Message formatting support for constraint violations.
 *
 * <p>
 * {@link am.ik.yavi.message.MessageFormatter} turns the message id and default message
 * format carried by a {@link am.ik.yavi.core.ConstraintViolation} into the message text.
 * {@link am.ik.yavi.message.SimpleMessageFormatter} formats the default message without
 * localization, while {@link am.ik.yavi.message.MessageSourceMessageFormatter} resolves
 * localized messages through a message source such as Spring Framework's
 * {@code MessageSource}.
 *
 * @since 0.0.17
 */
@NullMarked
package am.ik.yavi.message;

import org.jspecify.annotations.NullMarked;
