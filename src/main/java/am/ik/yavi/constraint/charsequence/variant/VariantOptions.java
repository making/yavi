/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence.variant;

import am.ik.yavi.jsr305.Nullable;

/**
 * Determines which Unicode variation selectors are removed from a character sequence
 * before its size is measured.
 *
 * <p>
 * Variation selectors are zero-width characters that change the glyph of the preceding
 * character. Removing them lets a value that only differs in its selector be counted like
 * the base character. By default no selector is removed. Use {@link #builder()},
 * optionally {@link Builder#ignoreAll()}, to create an instance, and pass it with
 * {@link am.ik.yavi.constraint.CharSequenceConstraint#variant(java.util.function.Function)}
 * or to the
 * {@link am.ik.yavi.constraint.CharSequenceConstraint#CharSequenceConstraint(java.text.Normalizer.Form, VariantOptions)}
 * constructor.
 * </p>
 *
 * <p>
 * An instance is immutable and can be reused. A {@code null} selector setting is not
 * allowed.
 * </p>
 *
 * @since 0.0.14
 */
public class VariantOptions {

	private final MongolianFreeVariationSelector fvs;

	private final IdeographicVariationSequence ivs;

	private final StandardizedVariationSequence svs;

	/**
	 * Creates options with the given selector settings.
	 * @param svs the setting for the standardized variation selectors
	 * @param ivs the setting for the ideographic variation selectors
	 * @param fvs the setting for the Mongolian free variation selectors
	 */
	public VariantOptions(StandardizedVariationSequence svs, IdeographicVariationSequence ivs,
			MongolianFreeVariationSelector fvs) {
		this.svs = svs;
		this.ivs = ivs;
		this.fvs = fvs;
	}

	/**
	 * Returns a builder that ignores no variation selector.
	 * @return a new builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	private boolean isNotIgnoreAll() {
		return !this.svs.ignore() && !this.fvs.ignore() && !this.ivs.ignore();
	}

	/**
	 * Returns the given value with the variation selectors selected for removal deleted.
	 * @param s the value, possibly {@code null}
	 * @return the value without the ignored selectors, or an empty string if {@code s} is
	 * {@code null} or empty
	 */
	public String ignored(@Nullable String s) {
		if (s == null || s.isEmpty()) {
			return "";
		}
		if (this.isNotIgnoreAll()) {
			return s;
		}
		StringBuilder regex = new StringBuilder("[");
		if (this.svs.ignore()) {
			regex.append(StandardizedVariationSequence.RANGE);
		}
		if (this.ivs.ignore()) {
			regex.append(IdeographicVariationSequence.RANGE);
		}
		if (this.fvs.ignore()) {
			regex.append(MongolianFreeVariationSelector.RANGE);
		}
		regex.append("]");
		return regex.length() == 2 ? s : s.replaceAll(regex.toString(), "");
	}

	/**
	 * Builder for {@link VariantOptions}. It starts with no selector ignored.
	 */
	public static class Builder {

		private MongolianFreeVariationSelector fvs;

		private IdeographicVariationSequence ivs;

		private StandardizedVariationSequence svs;

		Builder() {
			this.notIgnoreAll();
		}

		/**
		 * Creates the options with the settings made on this builder.
		 * @return the options
		 */
		public VariantOptions build() {
			return new VariantOptions(this.svs, this.ivs, this.fvs);
		}

		/**
		 * Sets the setting for the Mongolian free variation selectors.
		 * @param fvs the setting
		 * @return this builder
		 */
		public Builder fvs(MongolianFreeVariationSelector fvs) {
			this.fvs = fvs;
			return this;
		}

		/**
		 * Ignores all three kinds of variation selectors before the size is measured.
		 * @return this builder
		 */
		public Builder ignoreAll() {
			this.svs = StandardizedVariationSequence.IGNORE;
			this.ivs = IdeographicVariationSequence.IGNORE;
			this.fvs = MongolianFreeVariationSelector.IGNORE;
			return this;
		}

		/**
		 * Sets the setting for the ideographic variation selectors.
		 * @param ivs the setting
		 * @return this builder
		 */
		public Builder ivs(IdeographicVariationSequence ivs) {
			this.ivs = ivs;
			return this;
		}

		/**
		 * Ignores no variation selector before the size is measured. This is the initial
		 * state of the builder.
		 * @return this builder
		 */
		public Builder notIgnoreAll() {
			this.svs = StandardizedVariationSequence.NOT_IGNORE;
			this.ivs = IdeographicVariationSequence.NOT_IGNORE;
			this.fvs = MongolianFreeVariationSelector.NOT_IGNORE;
			return this;
		}

		/**
		 * Sets the setting for the standardized variation selectors.
		 * @param svs the setting
		 * @return this builder
		 */
		public Builder svs(StandardizedVariationSequence svs) {
			this.svs = svs;
			return this;
		}

	}

}
