/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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

/**
 * Constraint on the Unicode code points of a character sequence.
 *
 * <p>
 * Instances are created with {@link CharSequenceConstraint#codePoints(CodePoints)} and
 * one of the methods of its {@link Builder}: {@link #asBlackList()} rejects the values
 * that contain a code point of the {@link CodePoints} definition, and
 * {@link #asWhiteList()} rejects the values that contain a code point outside of it. The
 * value is inspected code point by code point, so a surrogate pair is treated as one code
 * point.
 * </p>
 *
 * <p>
 * A {@code null} value is valid. A violation is reported with the message key
 * {@code codePoints.asBlackList} or {@code codePoints.asWhiteList}, whose default message
 * is {@code "{1}" is/are not allowed for "{0}"}, where {@code {0}} is the property name
 * and {@code {1}} is the list of the offending characters.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <E> the type of the character sequence the constraints apply to
 * @since 0.0.10
 */
public class CodePointsConstraint<T, E extends CharSequence> extends CharSequenceConstraint<T, E> {

	private final CodePoints<E> codePoints;

	/**
	 * Creates the constraint. The predicates added to {@code delegate} are copied to the
	 * new constraint.
	 * @param delegate the constraint whose predicates are copied
	 * @param codePoints the code points that the constraint allows or rejects
	 */
	public CodePointsConstraint(CharSequenceConstraint<T, E> delegate, CodePoints<E> codePoints) {
		super();
		this.codePoints = codePoints;
		this.predicates().addAll(delegate.predicates());
	}

	/**
	 * Adds a constraint that rejects the values containing a code point of the configured
	 * definition.
	 * @return this constraint
	 */
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

	/**
	 * Adds a constraint that rejects the values containing a code point outside of the
	 * configured definition.
	 * @return this constraint
	 */
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

	/**
	 * Returns this constraint.
	 * @return this constraint
	 */
	@Override
	public CodePointsConstraint<T, E> cast() {
		return this;
	}

	/**
	 * Builder for a {@link CodePointsConstraint}, returned by
	 * {@link CharSequenceConstraint#codePoints(CodePoints)}.
	 *
	 * @param <T> the type of the object that owns the property
	 * @param <E> the type of the character sequence the constraints apply to
	 * @since 0.0.17
	 */
	public static class Builder<T, E extends CharSequence> {

		private final CodePoints<E> codePoints;

		private final CharSequenceConstraint<T, E> delegate;

		/**
		 * Creates the builder.
		 * @param delegate the constraint whose predicates are copied to the built
		 * constraint
		 * @param codePoints the code points that the built constraint allows or rejects
		 */
		public Builder(CharSequenceConstraint<T, E> delegate, CodePoints<E> codePoints) {
			this.delegate = delegate;
			this.codePoints = codePoints;
		}

		/**
		 * Builds a constraint that rejects the values containing a code point of the
		 * configured definition.
		 * @return the constraint
		 */
		public CodePointsConstraint<T, E> asBlackList() {
			return new CodePointsConstraint<>(this.delegate, this.codePoints).asBlackList();
		}

		/**
		 * Builds a constraint that rejects the values containing a code point outside of
		 * the configured definition.
		 * @return the constraint
		 */
		public CodePointsConstraint<T, E> asWhiteList() {
			return new CodePointsConstraint<>(this.delegate, this.codePoints).asWhiteList();
		}

	}

}
