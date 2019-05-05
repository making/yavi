/*
 * Copyright (C) 2018-2019 Toshiaki Maki <makingx@gmail.com>
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

import java.util.*;

import am.ik.yavi.jsr305.Nullable;

@FunctionalInterface
public interface CodePoints<E extends CharSequence> {

	Set<Integer> allExcludedCodePoints(E s);

	@FunctionalInterface
	interface CodePointsSet<E extends CharSequence> extends CodePoints<E> {
		Set<Integer> asSet();

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
	}

	@FunctionalInterface
	interface CodePointsRanges<E extends CharSequence> extends CodePoints<E> {
		List<Range> asRanges();

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

	}

	interface Range {
		static Range of(String begin, String end) {
			return Range.of(begin.codePoints().sorted().findFirst().orElse(0),
					end.codePoints().sorted().findFirst().orElse(0));
		}

		static Range of(int begin, int end) {
			if (begin > end) {
				throw new IllegalArgumentException("begin must not be greater than end ["
						+ begin + ", " + end + "]");
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
				public int hashCode() {
					return Objects.hash(begin, end);
				}

				@Override
				public boolean equals(Object obj) {
					if (!(obj instanceof Range)) {
						return false;
					}
					Range range = (Range) obj;
					return Objects.equals(begin, range.begin())
							&& Objects.equals(end, range.end());
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
