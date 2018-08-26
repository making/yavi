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