/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * Defines a set of Unicode code points for a character sequence constraint.
 *
 * <p>
 * An implementation returns the code points of a value that are not allowed by its
 * definition. The result is used by
 * {@link am.ik.yavi.constraint.CharSequenceConstraint#codePoints(CodePoints)} to build
 * either a black list constraint, which rejects the values that contain one of those code
 * points, or a white list constraint, which rejects the values that contain a code point
 * outside of them.
 * </p>
 *
 * <p>
 * A {@code null} or empty value has no code point, so the returned set is empty.
 * </p>
 *
 * @param <E> the type of the character sequence
 * @since 0.0.10
 */
@FunctionalInterface
public interface CodePoints<E extends CharSequence> {

	/**
	 * Returns the code points in the given value that are excluded by this definition.
	 * @param s the value, possibly {@code null}
	 * @return the excluded code points, empty if {@code s} is {@code null} or empty
	 */
	Set<Integer> allExcludedCodePoints(E s);

	/**
	 * A {@link CodePoints} definition given by the ranges of the included code points.
	 *
	 * @param <E> the type of the character sequence
	 * @since 0.0.10
	 */
	@FunctionalInterface
	interface CodePointsRanges<E extends CharSequence> extends CodePoints<E> {

		/**
		 * Returns the code points of the given value that are not in any of
		 * {@link #asRanges()}.
		 * @param s the value, possibly {@code null}
		 * @return the code points outside of the ranges, empty if {@code s} is
		 * {@code null} or empty
		 */
		@Override
		default Set<Integer> allExcludedCodePoints(@Nullable E s) {
			if (s == null || s.length() == 0) {
				return Collections.emptySet();
			}
			String str = s.toString();
			Set<Integer> excludedCodePoints = new LinkedHashSet<>();
			int len = str.length();
			Integer codePoint;
			List<Range> ranges = this.asRanges();
			for (int i = 0; i < len; i += Character.charCount(codePoint)) {
				codePoint = str.codePointAt(i);
				boolean included = false;
				for (Range range : ranges) {
					if (range.begin() <= codePoint && codePoint <= range.end()) {
						included = true;
						break;
					}
				}
				if (!included) {
					excludedCodePoints.add(codePoint);
				}
			}
			return excludedCodePoints;
		}

		/**
		 * Returns the ranges of the included code points.
		 * @return the included ranges
		 */
		List<Range> asRanges();

	}

	/**
	 * A {@link CodePoints} definition given by the set of the included code points.
	 *
	 * @param <E> the type of the character sequence
	 * @since 0.0.10
	 */
	@FunctionalInterface
	interface CodePointsSet<E extends CharSequence> extends CodePoints<E> {

		/**
		 * Returns the code points of the given value that are not in {@link #asSet()}.
		 * @param s the value, possibly {@code null}
		 * @return the code points outside of the set, empty if {@code s} is {@code null}
		 * or empty
		 */
		@Override
		default Set<Integer> allExcludedCodePoints(@Nullable E s) {
			if (s == null || s.length() == 0) {
				return Collections.emptySet();
			}
			String str = s.toString();
			Set<Integer> excludedCodePoints = new LinkedHashSet<>();
			int len = str.length();
			Integer codePoint;
			Set<Integer> set = this.asSet();
			for (int i = 0; i < len; i += Character.charCount(codePoint)) {
				codePoint = str.codePointAt(i);
				if (!set.contains(codePoint)) {
					excludedCodePoints.add(codePoint);
				}
			}
			return excludedCodePoints;
		}

		/**
		 * Returns the set of the included code points.
		 * @return the included code points
		 */
		Set<Integer> asSet();

	}

	/**
	 * An inclusive range of Unicode code points.
	 *
	 * @since 0.0.10
	 */
	interface Range {

		/**
		 * Creates a range from the given strings. The lowest code point of each string
		 * becomes the corresponding bound, so {@code of("a", "z")} is the range from
		 * U+0061 to U+007A. An empty string is treated as U+0000.
		 * @param begin the string whose lowest code point is the inclusive lower bound
		 * @param end the string whose lowest code point is the inclusive upper bound
		 * @return the range
		 * @throws IllegalArgumentException if the lower bound is greater than the upper
		 * bound
		 */
		static Range of(String begin, String end) {
			return Range.of(begin.codePoints().sorted().findFirst().orElse(0),
					end.codePoints().sorted().findFirst().orElse(0));
		}

		/**
		 * Creates a range between the given code points.
		 * @param begin the inclusive lower bound
		 * @param end the inclusive upper bound
		 * @return the range
		 * @throws IllegalArgumentException if {@code begin} is greater than {@code end}
		 */
		static Range of(int begin, int end) {
			if (begin > end) {
				throw new IllegalArgumentException("begin must not be greater than end [" + begin + ", " + end + "]");
			}
			return new Range() {
				@Override
				public int begin() {
					return begin;
				}

				@Override
				public int end() {
					return end;
				}

				@Override
				public boolean equals(Object obj) {
					if (!(obj instanceof Range)) {
						return false;
					}
					Range range = (Range) obj;
					return Objects.equals(begin, range.begin()) && Objects.equals(end, range.end());
				}

				@Override
				public int hashCode() {
					return Objects.hash(begin, end);
				}
			};
		}

		/**
		 * Creates a range that contains only the given code point.
		 * @param value the code point
		 * @return the range
		 */
		static Range single(int value) {
			return of(value, value);
		}

		/**
		 * Returns the inclusive lower bound of the range.
		 * @return the lower bound
		 */
		int begin();

		/**
		 * Returns the inclusive upper bound of the range.
		 * @return the upper bound
		 */
		int end();

	}

}
