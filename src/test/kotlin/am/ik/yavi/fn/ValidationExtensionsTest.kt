/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.fn

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidationExtensionsTest {
	@Test
	fun awaitMapSuccess() {
		val validation = Validation.success<String, Int>(1)
		val ret: Validation<String, String> = runBlocking {
			validation.awaitMap { int(it) }
		}
		assertThat(ret.isValid).isTrue
		assertThat(ret.value()).isEqualTo("Int: 1")
	}

	@Test
	fun awaitMapFailure() {
		val validation = Validation.failure<String, Int>(listOf("error"))
		val ret: Validation<String, String> = runBlocking {
			validation.awaitMap { int(it) }
		}
		assertThat(ret.isValid).isFalse
		assertThat(ret.errors()).containsExactly("error")
	}

	@Test
	fun awaitErrorMapSuccess() {
		val validation = Validation.success<String, Int>(1)
		val ret: Validation<String, Int> = runBlocking {
			validation.awaitMapError { str(it) }
		}
		assertThat(ret.isValid).isTrue
		assertThat(ret.value()).isEqualTo(1)
	}

	@Test
	fun awaitErrorMapFailure() {
		val validation = Validation.failure<String, Int>(listOf("error"))
		val ret: Validation<String, Int> = runBlocking {
			validation.awaitMapError { str(it) }
		}
		assertThat(ret.isValid).isFalse
		assertThat(ret.errors()).containsExactly("String: error")
	}

	@Test
	fun awaitErrorsMapSuccess() {
		val validation = Validation.success<String, Int>(1)
		val ret: Validation<String, Int> = runBlocking {
			validation.awaitMapErrors { str(it) }
		}
		assertThat(ret.isValid).isTrue
		assertThat(ret.value()).isEqualTo(1)
	}

	@Test
	fun awaitErrorsMapFailure() {
		val validation = Validation.failure<String, Int>(listOf("error"))
		val ret: Validation<String, Int> = runBlocking {
			validation.awaitMapErrors { str(it) }
		}
		assertThat(ret.isValid).isFalse
		assertThat(ret.errors()).containsExactly("String: error")
	}

	@Test
	fun awaitErrorsBimapSuccess() {
		val validation = Validation.success<String, Int>(1)
		val ret: Validation<String, String> = runBlocking {
			validation.awaitBimap({ str(it) }, { int(it) })
		}
		assertThat(ret.isValid).isTrue
		assertThat(ret.value()).isEqualTo("Int: 1")
	}

	@Test
	fun awaitErrorsBimapFailure() {
		val validation = Validation.failure<String, Int>(listOf("error"))
		val ret: Validation<String, String> = runBlocking {
			validation.awaitBimap({ str(it) }, { int(it) })
		}
		assertThat(ret.isValid).isFalse
		assertThat(ret.errors()).containsExactly("String: error")
	}

	@Test
	fun awaitErrorsFoldSuccess() {
		val validation = Validation.success<String, Int>(1)
		val ret: String = runBlocking {
			validation.awaitFold({ join(it) }, { int(it) })
		}
		assertThat(ret).isEqualTo("Int: 1")
	}

	@Test
	fun awaitErrorsFoldFailure() {
		val validation = Validation.failure<String, Int>(listOf("e1", "e2"))
		val ret: String = runBlocking {
			validation.awaitFold({ join(it) }, { int(it) })
		}
		assertThat(ret).isEqualTo("e1,e2")
	}

	suspend fun int(i: Int): String {
		delay(1)
		return "Int: $i"
	}

	suspend fun str(s: String): String {
		delay(1)
		return "String: $s"
	}

	suspend fun str(list: List<String>): List<String> {
		delay(1)
		return list.map { "String: $it" }
	}

	suspend fun join(list: List<String>): String {
		delay(1)
		return list.joinToString(",")
	}
}