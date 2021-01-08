/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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
 * Returns the left value if exists, `null` otherwise
 *
 * @return the left value if exists, `null` otherwise
 */
fun <L, R> Either<L, R>.leftOrNull(): L? = this.left

/**
 * Returns the right value if exists, `null` otherwise
 *
 * @return the right value if exists, `null` otherwise
 */
fun <L, R> Either<L, R>.rightOrNull(): R? = this.right

/**
 * Coroutines variant of [am.ik.yavi.fn.Either.bimap].
 */
suspend fun <L, R, X, Y> Either<L, R>.awaitBimap(leftMapper: suspend (L) -> X, rightMapper: suspend (R) -> Y) =
        if (isLeft) Either<X, Y>(leftMapper.invoke(left), null) else Either<X, Y>(null, rightMapper.invoke(right))

/**
 * Coroutines variant of [am.ik.yavi.fn.Either.fold].
 */
suspend fun <L, R, U> Either<L, R>.awaitFold(leftMapper: suspend (L) -> U, rightMapper: suspend (R) -> U) =
        if (isLeft) leftMapper.invoke(left) else rightMapper.invoke(right)

/**
 * Coroutines variant of [am.ik.yavi.fn.Either.leftMap].
 */
suspend fun <L, R, X> Either<L, R>.awaitLeftMap(leftMapper: suspend (L) -> X) = Either<X, R>(leftMapper.invoke(left), null)

/**
 * Coroutines variant of [am.ik.yavi.fn.Either.rightMap].
 */
suspend fun <L, R, Y> Either<L, R>.awaitRightMap(rightMapper: suspend (R) -> Y) = Either<L, Y>(null, rightMapper.invoke(right))