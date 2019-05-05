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
package am.ik.yavi.constraint.charsequence.codepoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import am.ik.yavi.constraint.charsequence.CodePoints;

public class CompositeCodePoints<E extends CharSequence> implements CodePoints<E> {
	private final List<CodePoints<E>> composite = new ArrayList<>();

	@SafeVarargs
	public CompositeCodePoints(CodePoints<E>... codePoints) {
		final Set<Integer> codePointsSet = new LinkedHashSet<>();
		final List<Range> ranges = new ArrayList<>();
		for (CodePoints<E> points : codePoints) {
			if (points instanceof CodePointsSet) {
				codePointsSet.addAll(((CodePointsSet<E>) points).asSet());
			}
			else if (points instanceof CodePointsRanges) {
				ranges.addAll(((CodePointsRanges<E>) points).asRanges());
			}
			else {
				composite.add(points);
			}
		}
		if (!codePointsSet.isEmpty()) {
			composite.add((CodePointsSet<E>) () -> codePointsSet);
		}
		if (!ranges.isEmpty()) {
			composite.add((CodePointsRanges<E>) () -> ranges);
		}
		if (composite.isEmpty()) {
			throw new IllegalArgumentException("No code point is included");
		}
	}

	@Override
	public Set<Integer> allExcludedCodePoints(E s) {
		Set<Integer> excluded = null;
		for (CodePoints<E> codePoints : this.composite) {
			Set<Integer> e = codePoints.allExcludedCodePoints(s);
			if (e.isEmpty()) {
				return e;
			}
			if (excluded == null) {
				excluded = e;
			}
			else {
				excluded.retainAll(e);
			}
		}
		return excluded == null ? Collections.emptySet() : excluded;
	}
}
