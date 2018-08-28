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
package am.ik.yavi.constraint;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

import static am.ik.yavi.constraint.ViolationMessage.Default.*;
import static am.ik.yavi.core.NullValidity.NULL_IS_INVALID;
import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.constraint.charsequence.*;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsRanges;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsSet;
import am.ik.yavi.constraint.charsequence.CodePoints.Range;
import am.ik.yavi.core.ConstraintPredicate;

public class CharSequenceConstraint<T, E extends CharSequence>
		extends ContainerConstraintBase<T, E, CharSequenceConstraint<T, E>> {
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile(
			"^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	protected final Normalizer.Form normalizerForm;
	protected final IdeographicVariationSequence ivs;

	public CharSequenceConstraint() {
		this(Normalizer.Form.NFC, IdeographicVariationSequence.IGNORE);
	}

	public CharSequenceConstraint(Normalizer.Form normalizerForm,
			IdeographicVariationSequence ivs) {
		this.normalizerForm = normalizerForm;
		this.ivs = ivs;
	}

	public CharSequenceConstraint<T, E> ivs(IdeographicVariationSequence IVS) {
		CharSequenceConstraint<T, E> constraint = new CharSequenceConstraint<>(
				this.normalizerForm, IVS);
		constraint.predicates().addAll(this.predicates());
		return constraint;
	}

	public CharSequenceConstraint<T, E> normalizer(Normalizer.Form normalizerForm) {
		CharSequenceConstraint<T, E> constraint = new CharSequenceConstraint<>(
				normalizerForm, this.ivs);
		constraint.predicates().addAll(this.predicates());
		return constraint;
	}

	protected String normalize(String s) {
		String cutIsv;
		if (this.ivs.ignore()) {
			cutIsv = s.replaceAll("[" + IdeographicVariationSequence.RANGE + "]", "");
		}
		else {
			cutIsv = s;
		}
		return this.normalizerForm == null ? cutIsv
				: Normalizer.normalize(cutIsv, this.normalizerForm);
	}

	@Override
	public CharSequenceConstraint<T, E> cast() {
		return this;
	}

	@Override
	protected ToIntFunction<E> size() {
		return cs -> {
			String s = this.normalize(cs.toString());
			return s.codePointCount(0, s.length());
		};
	}

	public CharSequenceConstraint<T, E> notBlank() {
		this.predicates()
				.add(ConstraintPredicate.of(
						x -> x != null && x.toString().trim().length() != 0,
						CHAR_SEQUENCE_NOT_BLANK, () -> new Object[] {}, NULL_IS_INVALID));
		return this;
	}

	public CharSequenceConstraint<T, E> contains(CharSequence s) {
		this.predicates().add(ConstraintPredicate.of(x -> x.toString().contains(s),
				CHAR_SEQUENCE_CONTAINS, () -> new Object[] { s }, NULL_IS_VALID));
		return this;
	}

	public CharSequenceConstraint<T, E> email() {
		this.predicates().add(ConstraintPredicate.of(x -> {
			if (size().applyAsInt(x) == 0) {
				return true;
			}
			return VALID_EMAIL_ADDRESS_REGEX.matcher(x).matches();
		}, CHAR_SEQUENCE_EMAIL, () -> new Object[] {}, NULL_IS_VALID));
		return this;
	}

	public CharSequenceConstraint<T, E> url() {
		this.predicates().add(ConstraintPredicate.of(x -> {
			if (size().applyAsInt(x) == 0) {
				return true;
			}
			try {
				new URL(x.toString());
				return true;
			}
			catch (MalformedURLException e) {
				return false;
			}
		}, CHAR_SEQUENCE_URL, () -> new Object[] {}, NULL_IS_VALID));
		return this;
	}

	public CharSequenceConstraint<T, E> pattern(String regex) {
		this.predicates().add(ConstraintPredicate.of(x -> Pattern.matches(regex, x),
				CHAR_SEQUENCE_PATTERN, () -> new Object[] { regex }, NULL_IS_VALID));
		return this;
	}

	public ByteSizeConstraint<T, E> asByteArray(Charset charset) {
		return new ByteSizeConstraint<>(this, charset);
	}

	public ByteSizeConstraint<T, E> asByteArray() {
		return this.asByteArray(StandardCharsets.UTF_8);
	}

	public CodePointsConstraint<T, E> codePoints(CodePoints<E> codePoints) {
		return new CodePointsConstraint<>(this, codePoints);
	}

	public CodePointsConstraint<T, E> codePoints(Set<Integer> allowedCodePoints) {
		return this.codePoints((CodePointsSet<E>) () -> allowedCodePoints);
	}

	public CodePointsConstraint<T, E> codePoints(int begin, int end) {
		return this.codePoints(Range.of(begin, end));
	}

	public CodePointsConstraint<T, E> codePoints(Range range, Range... ranges) {
		return this.codePoints((CodePointsRanges<E>) () -> {
			List<Range> list = new ArrayList<>();
			list.add(range);
			list.addAll(Arrays.asList(ranges));
			return list;
		});
	}

	public EmojiConstraint<T, E> emoji() {
		return new EmojiConstraint<>(this, this.normalizerForm, this.ivs);
	}
}
