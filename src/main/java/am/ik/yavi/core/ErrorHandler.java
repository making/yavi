/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

/**
 * Receives constraint violations when a validator is exposed as a
 * {@link java.util.function.BiConsumer}.
 *
 * <p>
 * See {@link Validatable#toBiConsumer(ErrorHandler)} and
 * {@link ValueValidator#toBiConsumer(ErrorHandler)}.
 *
 * @param <E> error type that the violations are recorded in
 * @since 0.13.0
 */
public interface ErrorHandler<E> {

	/**
	 * Handles a single constraint violation.
	 * @param errors the error object to record the violation in
	 * @param name the property name that failed validation
	 * @param messageKey the message id of the violated constraint
	 * @param args the message arguments; the first argument is the property name and the
	 * last argument is the value that violated the constraint
	 * @param defaultMessage the formatted violation message
	 */
	void handleError(E errors, String name, String messageKey, Object[] args, String defaultMessage);

}
