/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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
