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
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

import static am.ik.yavi.constraint.ViolationMessage.Default.*;
import static am.ik.yavi.core.NullValidity.NULL_IS_INVALID;
import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.constraint.charsequence.ByteSizeConstraint;
import am.ik.yavi.core.ConstraintPredicate;

public class CharSequenceConstraint<T, E extends CharSequence>
		extends ContainerConstraintBase<T, E, CharSequenceConstraint<T, E>> {
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile(
			"^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	private final Normalizer.Form normalizerForm;

	public CharSequenceConstraint() {
		this(null);
	}

	public CharSequenceConstraint(Normalizer.Form normalizerForm) {
		this.normalizerForm = normalizerForm;
	}

	@Override
	public CharSequenceConstraint<T, E> cast() {
		return this;
	}

	@Override
	protected ToIntFunction<E> size() {
		return cs -> {
			String s = this.normalizerForm == null ? cs.toString()
					: Normalizer.normalize(cs, this.normalizerForm);
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
}
