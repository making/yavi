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

/**
 * Combines several {@link CodePoints} definitions into one.
 *
 * <p>
 * The included code points of the result are the union of the included code points of the
 * given definitions. A white list constraint built from it therefore accepts a code point
 * that any of the definitions includes, and a black list constraint rejects a code point
 * that any of them excludes.
 * </p>
 *
 * @param <E> the type of the character sequence
 * @since 0.0.15
 */
public class CompositeCodePoints<E extends CharSequence> implements CodePoints<E> {

	private final List<CodePoints<E>> composite = new ArrayList<>();

	/**
	 * Creates the combined definition.
	 * @param codePoints the definitions to combine
	 * @throws IllegalArgumentException if the given definitions provide no code point,
	 * that is, if none is given or all of them are empty sets or ranges
	 */
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

	/**
	 * Returns the code points of the given value that none of the combined definitions
	 * includes.
	 * @param s the value, possibly {@code null}
	 * @return the code points outside of all definitions, empty if {@code s} is
	 * {@code null} or empty
	 */
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
