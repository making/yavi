/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.fn


/**
 * Folds this validation asynchronously, like [Validation.fold].
 *
 * <p>
 * If this validation is successful, [mapper] is invoked with the value; otherwise
 * [errorsMapper] is invoked with the error list. Only one of the functions is invoked.
 * Both functions may suspend, and an exception they throw is propagated to the caller.
 *
 * @param <E> type of the errors in the case of failure
 * @param <T> type of the value in the case of success
 * @param <U> type of the result
 * @param errorsMapper maps the errors of a failed validation to the result
 * @param mapper maps the value of a successful validation to the result
 * @return the result of [mapper] or [errorsMapper]
 * @since 0.9.1
 */
suspend fun <E, T, U> Validation<E, T>.awaitFold(
	errorsMapper: suspend (List<E>) -> U,
	mapper: suspend (T) -> U
): U =
	if (isValid) mapper.invoke(value()) else errorsMapper.invoke(errors())

/**
 * Maps this validation asynchronously, like [Validation.bimap].
 *
 * <p>
 * If this validation is successful, [mapper] is invoked with the value and the result is
 * returned as a successful validation. Otherwise [errorsMapper] is invoked with the
 * error list and the result is returned as a failed validation. Only one of the
 * functions is invoked. Both functions may suspend, and an exception they throw is
 * propagated to the caller.
 *
 * @param <E> type of the errors in the case of failure
 * @param <T> type of the value in the case of success
 * @param <X> type of the mapped errors
 * @param <Y> type of the mapped value
 * @param errorsMapper maps the errors of a failed validation
 * @param mapper maps the value of a successful validation
 * @return a successful validation holding the mapped value, or a failed validation
 * holding the mapped errors
 * @since 0.9.1
 */
suspend fun <E, T, X, Y> Validation<E, T>.awaitBimap(
	errorsMapper: suspend (List<E>) -> List<X>,
	mapper: suspend (T) -> Y
): Validation<X, Y> =
	if (isValid) Validation.success(mapper.invoke(value())) else Validation.failure(
		errorsMapper.invoke(errors())
	)

/**
 * Maps the errors of this validation asynchronously, like [Validation.mapErrors].
 *
 * <p>
 * A successful validation is returned unchanged. For a failed validation,
 * [errorsMapper] is invoked with the error list and the result is returned as a failed
 * validation. The mapper may suspend, and an exception it throws is propagated to the
 * caller.
 *
 * @param <E> type of the errors in the case of failure
 * @param <T> type of the value in the case of success
 * @param <X> type of the mapped errors
 * @param errorsMapper maps the errors of a failed validation
 * @return this validation if it is successful, or a failed validation holding the
 * mapped errors
 * @since 0.9.1
 */
@Suppress("UNCHECKED_CAST")
suspend fun <E, T, X> Validation<E, T>.awaitMapErrors(errorsMapper: suspend (List<E>) -> List<X>): Validation<X, T> =
	if (isValid) this as Validation<X, T> else Validation.failure(
		errorsMapper.invoke(
			errors()
		)
	)

/**
 * Maps each error of this validation asynchronously, like [Validation.mapError].
 *
 * <p>
 * A successful validation is returned unchanged. For a failed validation,
 * [errorsMapper] is invoked with each error and the results are returned as a failed
 * validation. The mapper may suspend, and an exception it throws is propagated to the
 * caller.
 *
 * @param <E> type of the errors in the case of failure
 * @param <T> type of the value in the case of success
 * @param <X> type of the mapped errors
 * @param errorsMapper maps each error of a failed validation
 * @return this validation if it is successful, or a failed validation holding the
 * mapped errors
 * @since 0.9.1
 */
@Suppress("UNCHECKED_CAST")
suspend fun <E, T, X> Validation<E, T>.awaitMapError(errorsMapper: suspend (E) -> X): Validation<X, T> =
	if (isValid) this as Validation<X, T> else Validation.failure(errors().map {
		errorsMapper.invoke(
			it
		)
	})

/**
 * Maps the value of this validation asynchronously, like [Validation.map].
 *
 * <p>
 * If this validation is successful, [mapper] is invoked with the value and the result is
 * returned as a successful validation. Otherwise this failed validation is returned
 * unchanged. The mapper may suspend, and an exception it throws is propagated to the
 * caller.
 *
 * @param <E> type of the errors in the case of failure
 * @param <T> type of the value in the case of success
 * @param <Y> type of the mapped value
 * @param mapper maps the value of a successful validation
 * @return a successful validation holding the mapped value, or this failed validation
 * @since 0.9.1
 */
@Suppress("UNCHECKED_CAST")
suspend fun <E, T, Y> Validation<E, T>.awaitMap(mapper: suspend (T) -> Y): Validation<E, Y> =
	if (isValid) Validation.success(mapper.invoke(value())) else this as Validation<E, Y>
