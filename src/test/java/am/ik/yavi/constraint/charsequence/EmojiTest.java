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
package am.ik.yavi.constraint.charsequence;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmojiTest {

	@Test
	public void emoji() {
		assertThat(Emoji.tryCount("I am 👱🏿")).isEqualTo(6);
	}

	@Test
	public void heart() {
		assertThat(Emoji.tryCount("❤️💙💚💛🧡💜🖤")).isEqualTo(7);
	}

	@Test
	public void family() {
		assertThat(Emoji.tryCount("👩‍❤️‍💋‍👩👪👩‍👩‍👧‍👦👨‍👦‍👦👨‍👧👩‍👧"))
				.isEqualTo(6);
	}

	@Test
	public void elf() {
		assertThat(Emoji.tryCount("🧝🧝🏻🧝🏼🧝🏽🧝🏾🧝🏿")).isEqualTo(6);
	}

	@Test
	public void emoji11All() throws Exception {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(this
				.getClass().getClassLoader().getResourceAsStream("emoji-test.txt")))) {
			String line;
			do {
				line = reader.readLine();
				if (line == null || line.startsWith("#") || line.isEmpty()) {
					continue;
				}
				int[] codePoints = Arrays.stream(line.split(";")[0].trim().split(" "))
						.mapToInt(x -> Integer.parseInt(x, 16)).toArray();
				String emoji = new String(codePoints, 0, codePoints.length);
				int len = Emoji.tryCount("This is " + emoji + ".");
				assertThat(len).describedAs(emoji).isEqualTo(10);
			}
			while (line != null);
		}
	}
}