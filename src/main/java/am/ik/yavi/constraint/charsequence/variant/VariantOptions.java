/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence.variant;

import am.ik.yavi.jsr305.Nullable;

public class VariantOptions {

	private final MongolianFreeVariationSelector fvs;

	private final IdeographicVariationSequence ivs;

	private final StandardizedVariationSequence svs;

	public VariantOptions(StandardizedVariationSequence svs, IdeographicVariationSequence ivs,
			MongolianFreeVariationSelector fvs) {
		this.svs = svs;
		this.ivs = ivs;
		this.fvs = fvs;
	}

	public static Builder builder() {
		return new Builder();
	}

	private boolean isNotIgnoreAll() {
		return !this.svs.ignore() && !this.fvs.ignore() && !this.ivs.ignore();
	}

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

	public static class Builder {

		private MongolianFreeVariationSelector fvs;

		private IdeographicVariationSequence ivs;

		private StandardizedVariationSequence svs;

		Builder() {
			this.notIgnoreAll();
		}

		public VariantOptions build() {
			return new VariantOptions(this.svs, this.ivs, this.fvs);
		}

		public Builder fvs(MongolianFreeVariationSelector fvs) {
			this.fvs = fvs;
			return this;
		}

		public Builder ignoreAll() {
			this.svs = StandardizedVariationSequence.IGNORE;
			this.ivs = IdeographicVariationSequence.IGNORE;
			this.fvs = MongolianFreeVariationSelector.IGNORE;
			return this;
		}

		public Builder ivs(IdeographicVariationSequence ivs) {
			this.ivs = ivs;
			return this;
		}

		public Builder notIgnoreAll() {
			this.svs = StandardizedVariationSequence.NOT_IGNORE;
			this.ivs = IdeographicVariationSequence.NOT_IGNORE;
			this.fvs = MongolianFreeVariationSelector.NOT_IGNORE;
			return this;
		}

		public Builder svs(StandardizedVariationSequence svs) {
			this.svs = svs;
			return this;
		}

	}

}
