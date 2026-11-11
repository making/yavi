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

import am.ik.yavi.jsr305.Nullable;

@FunctionalInterface
public interface CodePoints<E extends CharSequence> {

	Set<Integer> allExcludedCodePoints(E s);

	@FunctionalInterface
	interface CodePointsRanges<E extends CharSequence> extends CodePoints<E> {

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

		List<Range> asRanges();

	}

	@FunctionalInterface
	interface CodePointsSet<E extends CharSequence> extends CodePoints<E> {

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

		Set<Integer> asSet();

	}

	interface Range {

		static Range of(String begin, String end) {
			return Range.of(begin.codePoints().sorted().findFirst().orElse(0),
					end.codePoints().sorted().findFirst().orElse(0));
		}

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

		static Range single(int value) {
			return of(value, value);
		}

		int begin();

		int end();

	}

}
