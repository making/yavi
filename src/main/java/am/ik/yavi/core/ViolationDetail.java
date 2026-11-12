/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * A {@link ConstraintViolation} in a form that is ready to be serialized, for example as
 * an element of the body of an API response.
 *
 * <p>
 * It tells which value failed, with the {@code name} for a form and the {@code pointer}
 * for a JSON document, and why, with the message id, the formatted message and the
 * message arguments. A JSON library that supports records (for example Jackson) writes it
 * as
 *
 * <pre>{@code
 * {"name": "items[1].name", "pointer": "/items/1/name", "messageKey": "charSequence.notBlank",
 *  "message": "\"items[1].name\" must not be blank", "args": ["items[1].name", ""]}
 * }</pre>
 *
 * <p>
 * The arguments are those of {@link ConstraintViolation#args()}: they start with the name
 * and, by convention, end with the value that violated the constraint. Map the detail to
 * a type of your own before it is serialized if that value must not be sent back.
 *
 * @param name the name of the value that failed, such as {@code items[1].name}; empty for
 * the validated value itself
 * @param pointer the JSON Pointer (RFC 6901) to the value that failed, such as
 * {@code /items/1/name}; empty for the validated value itself
 * @param messageKey the message id of the violated constraint
 * @param message the formatted message
 * @param args the message arguments, unmodifiable
 * @since 0.0.3
 * @see ConstraintViolations#details()
 */
public record ViolationDetail(String name, String pointer, String messageKey, String message,
		List<@Nullable Object> args) {
}
