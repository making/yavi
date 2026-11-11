/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence.codepoints;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import am.ik.yavi.constraint.charsequence.CodePoints;

/**
 * Common groups of ASCII code points for the code point constraints.
 *
 * <p>
 * Use a constant as the {@link CodePoints} argument of
 * {@link am.ik.yavi.constraint.CharSequenceConstraint#codePoints(CodePoints)}, for
 * example {@code c.codePoints(AsciiCodePoints.ASCII_PRINTABLE_CHARS).asWhiteList()} or
 * {@code c.codePoints(AsciiCodePoints.CRLF).asBlackList()}.
 * </p>
 *
 * @since 0.0.10
 */
public enum AsciiCodePoints implements CodePoints<String> {

	/**
	 * The printable ASCII characters, from U+0020 (space) to U+007E (tilde).
	 */
	ASCII_PRINTABLE_CHARS((CodePointsRanges<String>) () -> {
		return Collections.singletonList(Range.of(0x0020 /*   */, 0x007E /* ~ */));
	}), //
	/**
	 * The ASCII control characters, from U+0000 (null) to U+001F (unit separator) and
	 * U+007F (delete).
	 */
	ASCII_CONTROL_CHARS((CodePointsRanges<String>) () -> {
		return Arrays.asList(Range.of(0x0000 /* NULL */, 0x001F /* UNIT SEPARATOR */),
				Range.single(0x007F /* DELETE */));
	}), //
	/**
	 * Carriage return (U+000D) and line feed (U+000A).
	 */
	CRLF((CodePointsSet<String>) () -> {
		return new HashSet<>(
				Arrays.asList(0x000A /* LINE FEED */, 0x000D /* CARRIAGE RETURN */));
	});

	private final CodePoints<String> delegate;

	AsciiCodePoints(CodePoints<String> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Returns the code points of the given value that are not in this group.
	 * @param s the value, possibly {@code null}
	 * @return the code points outside of the group, empty if {@code s} is {@code null} or
	 * empty
	 */
	@Override
	public Set<Integer> allExcludedCodePoints(String s) {
		return this.delegate.allExcludedCodePoints(s);
	}

}
