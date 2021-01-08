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
package am.ik.yavi.constraint.charsequence.variant;

import am.ik.yavi.jsr305.Nullable;

public class VariantOptions {
	private final MongolianFreeVariationSelector fvs;

	private final IdeographicVariationSequence ivs;

	private final StandardizedVariationSequence svs;

	public VariantOptions(StandardizedVariationSequence svs,
			IdeographicVariationSequence ivs, MongolianFreeVariationSelector fvs) {
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
