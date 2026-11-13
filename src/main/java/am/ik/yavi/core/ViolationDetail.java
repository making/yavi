/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * A {@link ConstraintViolation} in a form that is ready to be serialized, for example as
 * an element of the body of an API response.
 *
 * <p>
 * It tells which value failed, with the {@code name} for a form and the {@code pointer}
 * for a JSON document, and why, with the message id, the formatted message and the
 * parameters of the violation by name. A JSON library that supports records (for example
 * Jackson) writes a {@link Simple} as
 *
 * <pre>{@code
 * {"name": "items[1].quantity", "pointer": "/items/1/quantity", "messageKey": "numeric.lessThanOrEqual",
 *  "message": "\"items[1].quantity\" must be less than or equal to 99", "params": {"max": 99}}
 * }</pre>
 *
 * <p>
 * The parameters are those of {@link ConstraintViolation#params()}: the message arguments
 * that the violation message names, so that a client can render a message of its own from
 * the message id without knowing the positions of the message format. The value that
 * violated the constraint has no name in the built-in messages and is not in the detail,
 * which is therefore safe to return as it is; a caller that needs that value or the
 * positional arguments reads {@link ConstraintViolation#violatedValue()} and
 * {@link ConstraintViolation#args()}.
 *
 * <p>
 * The detail of the violation that {@link ValueValidator#anyOf(ValueValidator[])} reports
 * ({@code object.anyOf}) is a {@link NoAlternativeMatched}, which also holds the details
 * of each alternative that was tried, in the order of the alternatives:
 *
 * <pre>{@code
 * {"name": "contact", "pointer": "/contact", "messageKey": "object.anyOf",
 *  "message": "\"contact\" must satisfy at least one of the 2 alternatives", "params": {"count": 2},
 *  "alternatives": [[{"name": "contact", "messageKey": "charSequence.email", ...}],
 *                   [{"name": "contact", "messageKey": "charSequence.pattern", ...}]]}
 * }</pre>
 *
 * @since 0.0.3
 * @see ConstraintViolations#details()
 */
public sealed interface ViolationDetail {

	/**
	 * Returns the name of the value that failed, such as {@code items[1].name}; empty for
	 * the validated value itself.
	 * @return the name of the value
	 */
	String name();

	/**
	 * Returns the JSON Pointer (RFC 6901) to the value that failed, such as
	 * {@code /items/1/name}; empty for the validated value itself.
	 * @return the JSON Pointer to the value
	 */
	String pointer();

	/**
	 * Returns the message id of the violated constraint.
	 * @return the message id
	 */
	String messageKey();

	/**
	 * Returns the formatted message.
	 * @return the formatted message
	 */
	String message();

	/**
	 * Returns the parameters of the violation by name, such as <code>{"max": 99}</code>;
	 * empty if the violation message names no argument.
	 * @return the unmodifiable parameters
	 * @since 0.18.0
	 * @see ConstraintViolation#params()
	 */
	Map<String, @Nullable Object> params();

	/**
	 * The detail of a violation of a constraint.
	 *
	 * @param name the name of the value that failed
	 * @param pointer the JSON Pointer to the value that failed
	 * @param messageKey the message id of the violated constraint
	 * @param message the formatted message
	 * @param params the parameters of the violation by name, unmodifiable
	 * @since 0.18.0
	 */
	record Simple(String name, String pointer, String messageKey, String message,
			Map<String, @Nullable Object> params) implements ViolationDetail {
	}

	/**
	 * The detail of a violation that reports that no alternative matched.
	 *
	 * @param name the name of the value that failed
	 * @param pointer the JSON Pointer to the value that failed
	 * @param messageKey the message id of the violation, such as {@code object.anyOf}
	 * @param message the formatted message
	 * @param params the parameters of the violation by name, unmodifiable
	 * @param alternatives the details of the violations of each alternative that was
	 * tried and failed, in the order of the alternatives, unmodifiable
	 * @since 0.18.0
	 * @see ConstraintViolation.NoAlternativeMatched
	 */
	record NoAlternativeMatched(String name, String pointer, String messageKey, String message,
			Map<String, @Nullable Object> params, List<List<ViolationDetail>> alternatives) implements ViolationDetail {
	}

}
