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

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test

class EitherExtensionsTest {
    @Test
    fun leftOrNullExists() {
        val either = Either.left<Int, String>(1)
        Assertions.assertThat(either.leftOrNull()).isEqualTo(1)
    }

    @Test
    fun leftOrNullNotExists() {
        val either = Either.right<Int, String>("foo")
        Assertions.assertThat(either.leftOrNull()).isNull()
    }

    @Test
    fun rightOrNullExists() {
        val either = Either.right<Int, String>("foo")
        Assertions.assertThat(either.rightOrNull()).isEqualTo("foo")
    }

    @Test
    fun rightOrNullNotExists() {
        val either = Either.left<Int, String>(1)
        Assertions.assertThat(either.rightOrNull()).isNull()
    }

    @Test
    fun awaitBimapRight() {
        val either = Either.right<Int, String>("foo")
        val ret = runBlocking {
            either.awaitBimap({ int(it) }, { str(it) })
        }
        Assertions.assertThat(ret.right).isEqualTo("String: foo")
        Assertions.assertThat(ret.left).isNull()
    }

    @Test
    fun awaitBimapLeft() {
        val either = Either.left<Int, String>(1)
        val ret = runBlocking {
            either.awaitBimap({ int(it) }, { str(it) })
        }
        Assertions.assertThat(ret.right).isNull()
        Assertions.assertThat(ret.left).isEqualTo("Int: 1")
    }

    @Test
    fun awaitFoldRight() {
        val either = Either.right<Int, String>("foo")
        val ret = runBlocking {
            either.awaitFold({ int(it) }, { str(it) })
        }
        Assertions.assertThat(ret).isEqualTo("String: foo")
    }

    @Test
    fun awaitFoldLeft() {
        val either = Either.left<Int, String>(1)
        val ret = runBlocking {
            either.awaitFold({ int(it) }, { str(it) })
        }
        Assertions.assertThat(ret).isEqualTo("Int: 1")
    }

    @Test
    fun awaitRightMap() {
        val either = Either.right<Int, String>("foo")
        val ret = runBlocking {
            either.awaitRightMap { str(it) }
        }
        Assertions.assertThat(ret.right).isEqualTo("String: foo")
        Assertions.assertThat(ret.isLeft).isFalse()
    }

    @Test
    fun awaitLeftMap() {
        val either = Either.left<Int, String>(1)
        val ret = runBlocking {
            either.awaitLeftMap { int(it) }
        }
        Assertions.assertThat(ret.isRight).isFalse()
        Assertions.assertThat(ret.left).isEqualTo("Int: 1")
    }

    suspend fun int(i: Int): String {
        delay(1)
        return "Int: $i"
    }

    suspend fun str(s: String): String {
        delay(1)
        return "String: $s"
    }
}