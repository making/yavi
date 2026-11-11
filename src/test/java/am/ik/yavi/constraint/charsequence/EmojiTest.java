/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmojiTest {

	@Test
	void elf() {
		String emoji = "🧝🧝🏻🧝🏼🧝🏽🧝🏾🧝🏿";
		assertThat(emoji.length()).isEqualTo(22);
		assertThat(Emoji.bestEffortCount(emoji)).isEqualTo(6);
	}

	@Test
	void emoji() {
		String emoji = "I am 👱🏿";
		assertThat(emoji.length()).isEqualTo(9);
		assertThat(Emoji.bestEffortCount(emoji)).isEqualTo(6);
	}

	@Test
	void emoji11All() throws Exception {
		verifyEmojiAll("emoji-test-11.txt");
	}

	@Test
	void emoji12All() throws Exception {
		verifyEmojiAll("emoji-test-12.txt");
	}

	@Test
	void emoji14All() throws Exception {
		verifyEmojiAll("emoji-test-14.txt");
	}

	@Test
	void family() {
		String emoji = "👩‍❤️‍💋‍👩👪👩‍👩‍👧‍👦👨‍👦‍👦👨‍👧👩‍👧";
		assertThat(emoji.length()).isEqualTo(42);
		assertThat(Emoji.bestEffortCount(emoji)).isEqualTo(6);
	}

	@Test
	void heart() {
		String emoji = "❤️💙💚💛🧡💜🖤";
		assertThat(emoji.length()).isEqualTo(14);
		assertThat(Emoji.bestEffortCount(emoji)).isEqualTo(7);
	}

	@Test
	void subdivisionFlags() {
		String emoji = "🏴󠁧󠁢󠁥󠁮󠁧󠁿🏴󠁧󠁢󠁳󠁣󠁴󠁿🏴󠁧󠁢󠁷󠁬󠁳󠁿";
		assertThat(emoji.length()).isEqualTo(42);
		assertThat(Emoji.bestEffortCount(emoji)).isEqualTo(3);
	}

	void verifyEmojiAll(String file) throws Exception {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(file)))) {
			String line;
			int n = 0;
			do {
				n++;
				line = reader.readLine();
				if (line == null || line.startsWith("#") || line.isEmpty()) {
					continue;
				}
				int[] codePoints = Arrays.stream(line.split(";")[0].trim().split(" "))
					.mapToInt(x -> Integer.parseInt(x, 16))
					.toArray();
				String emoji = new String(codePoints, 0, codePoints.length);
				int len = Emoji.bestEffortCount("This is " + emoji + ".");
				assertThat(len).describedAs(emoji + " L" + n).isEqualTo(10);

			}
			while (line != null);
		}
	}

}
