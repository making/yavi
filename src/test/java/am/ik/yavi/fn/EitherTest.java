/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.fn;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EitherTest {

	@Test
	public void left() {
		Either<String, Integer> either = Either.left("Hello");
		assertThat(either.isLeft()).isTrue();
		assertThat(either.isRight()).isFalse();
		assertThat(either.left().get()).isEqualTo("Hello");
	}

	@Test
	public void right() {
		Either<String, Integer> either = Either.right(100);
		assertThat(either.isLeft()).isFalse();
		assertThat(either.isRight()).isTrue();
		assertThat(either.right().get()).isEqualTo(100);
	}

	@Test
	public void leftOrElseGet() {
		Either<String, Integer> either = Either.right(100);
		assertThat(either.leftOrElseGet(String::valueOf)).isEqualTo("100");
	}

	@Test
	public void rightOrElseGet() {
		Either<String, Integer> either = Either.left("100");
		assertThat(either.rightOrElseGet(Integer::valueOf)).isEqualTo(100);
	}

	@Test
	public void swap() {
		Either<String, Integer> either = Either.left("Hello");
		assertThat(either.swap()).isEqualTo(Either.right("Hello"));
	}

	@Test
	public void foldForLeft() {
		Either<String, Integer> either = Either.left("Hello");
		String ret = either.fold(s -> s + "!!", String::valueOf);
		assertThat(ret).isEqualTo("Hello!!");
	}

	@Test
	public void foldForRight() {
		Either<String, Integer> either = Either.right(100);
		String ret = either.fold(s -> s + "!!", String::valueOf);
		assertThat(ret).isEqualTo("100");
	}

	@Test
	public void bimapForLeft() {
		Either<String, Integer> either = Either.left("Hello");
		Either<String, Integer> bimap = either.bimap(s -> s + s, i -> i * 2);
		assertThat(bimap).isEqualTo(Either.left("HelloHello"));
	}

	@Test
	public void bimapForRight() {
		Either<String, Integer> either = Either.right(100);
		Either<String, Integer> bimap = either.bimap(s -> s + s, i -> i * 2);
		assertThat(bimap).isEqualTo(Either.right(200));
	}

	@Test
	public void mapLeft() {
		Either<Integer, String> either = Either.left(100);
		Either<Integer, String> map = either.mapLeft(i -> i * 2);
		assertThat(map).isEqualTo(Either.left(200));
	}

	@Test
	public void mapRight() {
		Either<String, Integer> either = Either.right(100);
		Either<String, Integer> map = either.mapRight(i -> i * 2);
		assertThat(map).isEqualTo(Either.right(200));
	}
}