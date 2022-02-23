/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
