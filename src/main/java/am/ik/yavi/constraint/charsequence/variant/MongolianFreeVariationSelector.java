/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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

public enum MongolianFreeVariationSelector {
	IGNORE(true), NOT_IGNORE(false);
	public static final String RANGE = "\u180B-\u180D";
	private final boolean ignore;

	MongolianFreeVariationSelector(boolean ignore) {
		this.ignore = ignore;
	}

	public boolean ignore() {
		return this.ignore;
	}
}
