/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence;

import java.text.Normalizer;

import org.jspecify.annotations.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_FIXED_SIZE;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_GREATER_THAN;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_GREATER_THAN_OR_EQUAL;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_LESS_THAN;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_LESS_THAN_OR_EQUAL;

import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.constraint.charsequence.variant.VariantOptions;
import am.ik.yavi.core.ConstraintPredicate;

/**
 * Constraint that measures a character sequence in extended grapheme clusters instead of
 * Unicode code points.
 *
 * <p>
 * The size is the number of extended grapheme clusters after the value is normalized with
 * the configured normalizer and the ignored variation selectors are removed. The clusters
 * are recognized with the {@code \b{g}} grapheme cluster boundary of
 * {@link java.util.regex.Pattern}. An emoji written with skin tone modifiers, keycaps,
 * regional indicator pairs, subdivision flags or ZWJ sequences is a single cluster and
 * therefore counts as one.
 * </p>
 *
 * <p>
 * A {@code null} value is valid. A violation is reported with the message key
 * {@code container.fixedSize}, {@code container.greaterThan},
 * {@code container.greaterThanOrEqual}, {@code container.lessThan} or
 * {@code container.lessThanOrEqual}, where {@code {1}} is the required size and
 * {@code {2}} is the actual emoji count.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <E> the type of the character sequence the constraints apply to
 * @since 0.0.12
 */
public class EmojiConstraint<T, E extends CharSequence> extends CharSequenceConstraint<T, E> {

	/**
	 * Matches the boundaries of extended grapheme clusters. A value with {@code n}
	 * clusters has {@code n + 1} boundaries.
	 * @see <a href="https://unicode.org/reports/tr29/">Unicode Text Segmentation</a>
	 */
	private static final Pattern GRAPHEME_CLUSTER_BOUNDARY = Pattern.compile("\\b{g}");

	/**
	 * Creates the constraint. The predicates added to {@code delegate} are copied to the
	 * new constraint.
	 * @param delegate the constraint whose predicates are copied
	 * @param normalizerForm the normalization form applied before counting, or
	 * {@code null} to skip normalization
	 * @param variantOptions the variation selectors to ignore before counting
	 */
	public EmojiConstraint(CharSequenceConstraint<T, E> delegate, Normalizer.@Nullable Form normalizerForm,
			VariantOptions variantOptions) {
		super(normalizerForm, variantOptions);
		this.predicates().addAll(delegate.predicates());
	}

	/**
	 * Requires the emoji count of the value to be exactly {@code size}.
	 * @param size the required number of emoji
	 * @return this constraint
	 */
	@Override
	public EmojiConstraint<T, E> fixedSize(int size) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) == size, this::size),
					CONTAINER_FIXED_SIZE, () -> new Object[] { size }, VALID));
		return this;
	}

	/**
	 * Requires the emoji count of the value to be greater than {@code min}.
	 * @param min the exclusive lower bound of the emoji count
	 * @return this constraint
	 */
	@Override
	public EmojiConstraint<T, E> greaterThan(int min) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) > min, this::size),
					CONTAINER_GREATER_THAN, () -> new Object[] { min }, VALID));
		return this;
	}

	/**
	 * Requires the emoji count of the value to be greater than or equal to {@code min}.
	 * @param min the inclusive lower bound of the emoji count
	 * @return this constraint
	 */
	@Override
	public EmojiConstraint<T, E> greaterThanOrEqual(int min) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) >= min, this::size),
					CONTAINER_GREATER_THAN_OR_EQUAL, () -> new Object[] { min }, VALID));
		return this;
	}

	/**
	 * Requires the emoji count of the value to be less than {@code max}.
	 * @param max the exclusive upper bound of the emoji count
	 * @return this constraint
	 */
	@Override
	public EmojiConstraint<T, E> lessThan(int max) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) < max, this::size),
					CONTAINER_LESS_THAN, () -> new Object[] { max }, VALID));
		return this;
	}

	/**
	 * Requires the emoji count of the value to be less than or equal to {@code max}.
	 * @param max the inclusive upper bound of the emoji count
	 * @return this constraint
	 */
	@Override
	public EmojiConstraint<T, E> lessThanOrEqual(int max) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(this.checkSizePredicate(x -> size(x) <= max, this::size),
					CONTAINER_LESS_THAN_OR_EQUAL, () -> new Object[] { max }, VALID));
		return this;
	}

	private int size(E x) {
		return graphemeClusterCount(this.normalize(x.toString()));
	}

	/**
	 * Returns the number of extended grapheme clusters in the given string.
	 * @param s the string to measure
	 * @return the number of grapheme clusters
	 */
	static int graphemeClusterCount(String s) {
		Matcher matcher = GRAPHEME_CLUSTER_BOUNDARY.matcher(s);
		int boundaries = 0;
		while (matcher.find()) {
			boundaries++;
		}
		// An empty string has one boundary and no cluster.
		return boundaries - 1;
	}

}
