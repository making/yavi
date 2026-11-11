/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.fn


/**
 * Coroutines variant of [am.ik.yavi.fn.Validation.fold].
 * @since 0.9.1
 */
suspend fun <E, T, U> Validation<E, T>.awaitFold(
	errorsMapper: suspend (List<E>) -> U,
	mapper: suspend (T) -> U
): U =
	if (isValid) mapper.invoke(value()) else errorsMapper.invoke(errors())

/**
 * Coroutines variant of [am.ik.yavi.fn.Validation.bimap].
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
 * Coroutines variant of [am.ik.yavi.fn.Validation.mapErrors].
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
 * Coroutines variant of [am.ik.yavi.fn.Validation.mapError].
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
 * Coroutines variant of [am.ik.yavi.fn.Validation.map].
 * @since 0.9.1
 */
@Suppress("UNCHECKED_CAST")
suspend fun <E, T, Y> Validation<E, T>.awaitMap(mapper: suspend (T) -> Y): Validation<E, Y> =
	if (isValid) Validation.success(mapper.invoke(value())) else this as Validation<E, Y>
