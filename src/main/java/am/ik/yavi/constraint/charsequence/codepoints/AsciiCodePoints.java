/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import am.ik.yavi.constraint.charsequence.CodePoints;

public enum AsciiCodePoints implements CodePoints<String> {

	ASCII_PRINTABLE_CHARS((CodePointsRanges<String>) () -> {
		return Collections.singletonList(Range.of(0x0020 /*   */, 0x007E /* ~ */));
	}), //
	ASCII_CONTROL_CHARS((CodePointsRanges<String>) () -> {
		return Arrays.asList(Range.of(0x0000 /* NULL */, 0x001F /* UNIT SEPARATOR */),
				Range.single(0x007F /* DELETE */));
	}), //
	CRLF((CodePointsSet<String>) () -> {
		return new HashSet<>(
				Arrays.asList(0x000A /* LINE FEED */, 0x000D /* CARRIAGE RETURN */));
	});

	private final CodePoints<String> delegate;

	AsciiCodePoints(CodePoints<String> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Set<Integer> allExcludedCodePoints(String s) {
		return this.delegate.allExcludedCodePoints(s);
	}

}
