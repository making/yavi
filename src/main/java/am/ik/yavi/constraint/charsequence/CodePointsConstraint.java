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
package am.ik.yavi.constraint.charsequence;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.CODE_POINTS_ALL_INCLUDED;
import static am.ik.yavi.core.ViolationMessage.Default.CODE_POINTS_NOT_INCLUDED;

import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.ViolatedValue;

public class CodePointsConstraint<T, E extends CharSequence>
		extends CharSequenceConstraint<T, E> {
	private final CodePoints<E> codePoints;

	public CodePointsConstraint(CharSequenceConstraint<T, E> delegate,
			CodePoints<E> codePoints) {
		super();
		this.codePoints = codePoints;
		this.predicates().addAll(delegate.predicates());
	}

	public CodePointsConstraint<T, E> asBlackList() {
		this.predicates().add(ConstraintPredicate.withViolatedValue(x -> {
			Set<Integer> excludedFromBlackList = this.codePoints.allExcludedCodePoints(x);
			Integer codePoint;
			String str = x.toString();
			int len = str.length();
			Set<Integer> included = new LinkedHashSet<>();
			for (int i = 0; i < len; i += Character.charCount(codePoint)) {
				codePoint = str.codePointAt(i);
				if (!excludedFromBlackList.contains(codePoint)) {
					included.add(codePoint);
				}
			}
			if (included.isEmpty()) {
				return Optional.empty();
			}
			List<String> includedList = included.stream() //
					.map(i -> new String(new int[] { i }, 0, 1)) //
					.collect(Collectors.toList());
			return Optional.of(new ViolatedValue(includedList));
		}, CODE_POINTS_NOT_INCLUDED, () -> new Object[] {}, VALID));
		return this;
	}

	public CodePointsConstraint<T, E> asWhiteList() {
		this.predicates().add(ConstraintPredicate.withViolatedValue(x -> {
			Set<Integer> excludedFromWhiteList = this.codePoints.allExcludedCodePoints(x);
			if (excludedFromWhiteList.isEmpty()) {
				return Optional.empty();
			}
			List<String> excludedList = excludedFromWhiteList.stream() //
					.map(i -> new String(new int[] { i }, 0, 1)) //
					.collect(Collectors.toList());
			return Optional.of(new ViolatedValue(excludedList));
		}, CODE_POINTS_ALL_INCLUDED, () -> new Object[] {}, VALID));
		return this;
	}

	@Override
	public CodePointsConstraint<T, E> cast() {
		return this;
	}

	public static class Builder<T, E extends CharSequence> {
		private final CodePoints<E> codePoints;

		private final CharSequenceConstraint<T, E> delegate;

		public Builder(CharSequenceConstraint<T, E> delegate, CodePoints<E> codePoints) {
			this.delegate = delegate;
			this.codePoints = codePoints;
		}

		public CodePointsConstraint<T, E> asBlackList() {
			return new CodePointsConstraint<>(this.delegate, this.codePoints)
					.asBlackList();
		}

		public CodePointsConstraint<T, E> asWhiteList() {
			return new CodePointsConstraint<>(this.delegate, this.codePoints)
					.asWhiteList();
		}
	}

}
