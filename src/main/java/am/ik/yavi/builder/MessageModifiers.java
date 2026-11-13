/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import am.ik.yavi.core.ViolationMessage;

/**
 * The modifiers of an argument read from a key that say how its violations read to a
 * person: what the argument is called, and what is said about a value of another type.
 * The builders returned by {@code Yavi.schema()} have them after such an argument is
 * added, next to {@code required(...)}, which says what a missing value is told.
 *
 * <pre>{@code
 * Yavi.schema()
 *     ._string("email", c -> c.notBlank().email()).label("Email address")
 *     ._integer("ttl", c -> c.positive()).typeMessage("\"{0}\" must be a number of seconds").optional()
 *     ._list("roles", Yavi._string()).label("Roles").required("Select at least one role")
 * }</pre>
 *
 * <p>
 * They are part of the definition of the argument: write them right after it is added,
 * before the modifiers that decide what {@code null} produces. They apply to the
 * violations on the argument itself and on its elements ({@code roles} and
 * {@code roles[1]}), not to those of a nested property ({@code address.street}).
 *
 * @param <B> the type of the builder, which the modifiers return
 * @since 0.18.0
 * @see am.ik.yavi.input.PropertyMessages
 */
public interface MessageModifiers<B> {

	/**
	 * Calls the last added argument by the given label in the messages of its violations
	 * instead of its name: <code>{0}</code> of a message is the label.
	 *
	 * <pre>{@code
	 * ._string("email", c -> c.notBlank()).label("Email address")
	 * // "Email address" must not be blank
	 * }</pre>
	 *
	 * <p>
	 * The name, the path and the pointer of a violation still locate the value by the
	 * name of the argument, so {@code messagesByName()} has {@code email} as the key and
	 * a {@code ViolationDetail} has it as the name. A violation that already has a label
	 * keeps it.
	 * @param label what the argument is called in a message
	 * @return a builder whose last argument has the label
	 * @see am.ik.yavi.core.ConstraintViolation#label()
	 */
	B label(String label);

	/**
	 * Reports a value that does not represent the type of the last added argument with
	 * the given message format instead of the one of {@code object.type}. The message key
	 * {@code object.type} and the arguments are kept: <code>{0}</code> is the name or the
	 * label, <code>{1}</code> the name of the type and <code>{2}</code> the value.
	 *
	 * <pre>{@code
	 * ._integer("ttl", c -> c.positive()).typeMessage("\"{0}\" must be a number of seconds")
	 * }</pre>
	 *
	 * <p>
	 * It has an effect only with {@code fromMap}: a typed argument is of its type. An
	 * enum constant that does not exist is reported with {@code object.oneOf}, which this
	 * message does not replace.
	 * @param messageFormat the message format of a value of another type
	 * @return a builder whose last argument has the message
	 */
	default B typeMessage(String messageFormat) {
		return this.typeMessage(ViolationMessage.of(ViolationMessage.Default.OBJECT_TYPE.messageKey(), messageFormat,
				ViolationMessage.Default.OBJECT_TYPE.paramNames().toArray(String[]::new)));
	}

	/**
	 * Reports a value that does not represent the type of the last added argument with
	 * the given message instead of the one of {@code object.type}. Both the message key
	 * and the message format are replaced, so that a resource bundle can hold the message
	 * of this argument; the arguments are kept.
	 * @param message the message of a value of another type
	 * @return a builder whose last argument has the message
	 * @see #typeMessage(String)
	 */
	B typeMessage(ViolationMessage message);

}
