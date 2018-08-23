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

import static am.ik.yavi.core.NullValidity.NULL_IS_INVALID;
import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
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
				.add(new ConstraintPredicate<>(
						x -> x != null && x.toString().trim().length() != 0,
						"charSequence.notBlank", "\"{0}\" must not be blank",
						() -> new Object[] {}, NULL_IS_INVALID));
		return this;
	}

	public CharSequenceConstraint<T, E> contains(CharSequence s) {
		this.predicates()
				.add(new ConstraintPredicate<>(x -> x.toString().contains(s),
						"charSequence.contains", "\"{0}\" must contain {1}",
						() -> new Object[] { s }, NULL_IS_VALID));
		return this;
	}

	public CharSequenceConstraint<T, E> email() {
		this.predicates()
				.add(new ConstraintPredicate<>(
						x -> VALID_EMAIL_ADDRESS_REGEX.matcher(x).matches(),
						"charSequence.email", "\"{0}\" must be a valid email address",
						() -> new Object[] {}, NULL_IS_VALID));
		return this;
	}

	public CharSequenceConstraint<T, E> url() {
		this.predicates().add(new ConstraintPredicate<>(x -> {
			try {
				new URL(x.toString());
				return true;
			}
			catch (MalformedURLException e) {
				return false;
			}
		}, "charSequence.url", "\"{0}\" must be a valid URL", () -> new Object[] {},
				NULL_IS_VALID));
		return this;
	}

	public CharSequenceConstraint<T, E> pattern(String regex) {
		this.predicates()
				.add(new ConstraintPredicate<>(x -> Pattern.matches(regex, x),
						"charSequence.pattern", "\"{0}\" must match {1}",
						() -> new Object[] { regex }, NULL_IS_VALID));
		return this;
	}

	public ByteSizeConstraint<T, E> asByteArray(Charset charset) {
		return new ByteSizeConstraint<>(this, charset);
	}

	public ByteSizeConstraint<T, E> asByteArray() {
		return this.asByteArray(StandardCharsets.UTF_8);
	}

	public static class ByteSizeConstraint<T, E extends CharSequence>
			extends CharSequenceConstraint<T, E> {
		private final Charset charset;

		private ByteSizeConstraint(CharSequenceConstraint<T, E> delete, Charset charset) {
			super();
			this.charset = charset;
			this.predicates().addAll(delete.predicates());
		}

		@Override
		public ByteSizeConstraint<T, E> cast() {
			return this;
		}

		private int size(E x) {
			return x.toString().getBytes(charset).length;
		}

		public ByteSizeConstraint<T, E> lessThan(int max) {
			this.predicates().add(new ConstraintPredicate<>(x -> size(x) < max,
					"byteSize.lessThan", "The size of \"{0}\" must be less than {1}",
					() -> new Object[] { max }, NULL_IS_VALID));
			return this;
		}

		public ByteSizeConstraint<T, E> lessThanOrEqual(int max) {
			this.predicates()
					.add(new ConstraintPredicate<>(x -> size(x) <= max,
							"byteSize.lessThanOrEqual",
							"The byte size of \"{0}\" must be less than or equal to {1}",
							() -> new Object[] { max }, NULL_IS_VALID));
			return this;
		}

		public ByteSizeConstraint<T, E> greaterThan(int min) {
			this.predicates()
					.add(new ConstraintPredicate<>(x -> size(x) > min,
							"byteSize.greaterThan",
							"The byte size of \"{0}\" must be greater than {1}",
							() -> new Object[] { min }, NULL_IS_VALID));
			return this;
		}

		public ByteSizeConstraint<T, E> greaterThanOrEqual(int min) {
			this.predicates().add(new ConstraintPredicate<>(x -> size(x) >= min,
					"byteSize.greaterThanOrEqual",
					"The byte size of \"{0}\" must be greater than or equal to {1}",
					() -> new Object[] { min }, NULL_IS_VALID));
			return this;
		}

		public ByteSizeConstraint<T, E> fixedSize(int size) {
			this.predicates()
					.add(new ConstraintPredicate<>(x -> size(x) == size,
							"byteSize.fixedSize", "The byte size of \"{0}\" must be {1}",
							() -> new Object[] { size }, NULL_IS_VALID));
			return this;
		}
	}
}
