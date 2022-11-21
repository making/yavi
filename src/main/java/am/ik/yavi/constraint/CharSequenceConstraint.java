/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.constraint.charsequence.ByteSizeConstraint;
import am.ik.yavi.constraint.charsequence.CodePoints;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsRanges;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsSet;
import am.ik.yavi.constraint.charsequence.CodePoints.Range;
import am.ik.yavi.constraint.charsequence.CodePointsConstraint;
import am.ik.yavi.constraint.charsequence.EmojiConstraint;
import am.ik.yavi.constraint.charsequence.variant.VariantOptions;
import am.ik.yavi.constraint.inetaddress.InetAddressUtils;
import am.ik.yavi.constraint.password.CharSequencePasswordPoliciesBuilder;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.ViolationMessage;

import static am.ik.yavi.core.NullAs.INVALID;
import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.*;

public class CharSequenceConstraint<T, E extends CharSequence>
		extends ContainerConstraintBase<T, E, CharSequenceConstraint<T, E>> {
	private static final String EMAIL_PART = "[^\\x00-\\x1F()<>@,;:\\\\\".\\[\\]\\s]";

	private static final String DOMAIN_PATTERN = EMAIL_PART + "+(\\." + EMAIL_PART
			+ "+)*";

	private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern
			.compile(
					"^" + EMAIL_PART + "+(\\." + EMAIL_PART + "+)*@(" + DOMAIN_PATTERN
							+ "|" + InetAddressUtils.IPV4_REGEX + ")$",
					Pattern.CASE_INSENSITIVE);

	private static final Pattern VALID_UUID_REGEX = Pattern
			.compile("\\p{XDigit}{8}(-\\p{XDigit}{4}){4}\\p{XDigit}{8}");

	protected final Normalizer.Form normalizerForm;

	protected final VariantOptions variantOptions;

	public CharSequenceConstraint() {
		this(Normalizer.Form.NFC, VariantOptions.builder().build());
	}

	public CharSequenceConstraint(Normalizer.Form normalizerForm,
			VariantOptions variantOptions) {
		this.normalizerForm = normalizerForm;
		this.variantOptions = variantOptions;
	}

	public ByteSizeConstraint<T, E> asByteArray(Charset charset) {
		return new ByteSizeConstraint<>(this, charset);
	}

	public ByteSizeConstraint<T, E> asByteArray() {
		return this.asByteArray(StandardCharsets.UTF_8);
	}

	@Override
	public CharSequenceConstraint<T, E> cast() {
		return this;
	}

	public CodePointsConstraint.Builder<T, E> codePoints(CodePoints<E> codePoints) {
		return new CodePointsConstraint.Builder<>(this, codePoints);
	}

	public CodePointsConstraint.Builder<T, E> codePoints(Set<Integer> allowedCodePoints) {
		return this.codePoints((CodePointsSet<E>) () -> allowedCodePoints);
	}

	public CodePointsConstraint.Builder<T, E> codePoints(int begin, int end) {
		return this.codePoints(Range.of(begin, end));
	}

	public CodePointsConstraint.Builder<T, E> codePoints(Range range, Range... ranges) {
		return this.codePoints((CodePointsRanges<E>) () -> {
			List<Range> list = new ArrayList<>();
			list.add(range);
			list.addAll(Arrays.asList(ranges));
			return list;
		});
	}

	public CharSequenceConstraint<T, E> contains(CharSequence s) {
		this.predicates().add(ConstraintPredicate.of(x -> x.toString().contains(s),
				CHAR_SEQUENCE_CONTAINS, () -> new Object[] { s }, VALID));
		return this;
	}

	/**
	 * Does the given value start with the {@code prefix}
	 * @param prefix the prefix the value has to start with
	 * @since 0.10.0
	 */
	public CharSequenceConstraint<T, E> startsWith(CharSequence prefix) {
		this.predicates()
				.add(ConstraintPredicate.of(
						x -> x.toString().startsWith(prefix.toString()),
						CHAR_SEQUENCE_STARTSWITH, () -> new Object[] { prefix }, VALID));
		return this;
	}

	/**
	 * Does the given value end with the {@code suffix}
	 * @param suffix the suffix the value has to end with
	 * @since 0.10.0
	 */
	public CharSequenceConstraint<T, E> endsWith(CharSequence suffix) {
		this.predicates()
				.add(ConstraintPredicate.of(x -> x.toString().endsWith(suffix.toString()),
						CHAR_SEQUENCE_ENDSWITH, () -> new Object[] { suffix }, VALID));
		return this;
	}

	public CharSequenceConstraint<T, E> email() {
		this.predicates().add(ConstraintPredicate.of(x -> {
			if (size().applyAsInt(x) == 0) {
				return true;
			}
			return VALID_EMAIL_ADDRESS_REGEX.matcher(x).matches();
		}, CHAR_SEQUENCE_EMAIL, () -> new Object[] {}, VALID));
		return this;
	}

	/**
	 * @since 0.7.0
	 */
	public CharSequenceConstraint<T, E> password(
			Function<CharSequencePasswordPoliciesBuilder<T, E>, List<ConstraintPredicate<E>>> builder) {
		final List<ConstraintPredicate<E>> predicates = builder
				.apply(new CharSequencePasswordPoliciesBuilder<>());
		this.predicates().addAll(predicates);
		return this;
	}

	private <U> CharSequenceConstraint<T, E> isValidRepresentationOf(
			Function<String, U> converter, ViolationMessage message) {
		this.predicates().add(ConstraintPredicate.of(x -> {
			if (size().applyAsInt(x) == 0) {
				return true;
			}
			try {
				converter.apply(x.toString());
				return true;
			}
			catch (NumberFormatException ignored) {
				return false;
			}
		}, message, () -> new Object[] {}, VALID));
		return this;
	}

	/**
	 * @since 0.12.0
	 */
	private CharSequenceConstraint<T, E> isLocalDate(String pattern, Locale locale) {
		this.predicates().add(ConstraintPredicate.of(x -> {
			try {
				DateTimeFormatter.ofPattern(pattern, locale).withResolverStyle(ResolverStyle.STRICT).parse(x);
				return true;
			}
			catch (DateTimeParseException ignored) {
				return false;
			}
		}, CHAR_SEQUENCE_LOCAL_DATE, () -> new Object[] {pattern}, VALID));
		return this;
	}

	/**
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isByte() {
		return this.isValidRepresentationOf(Byte::parseByte, CHAR_SEQUENCE_BYTE);
	}

	/**
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isShort() {
		return this.isValidRepresentationOf(Short::parseShort, CHAR_SEQUENCE_SHORT);
	}

	/**
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isInteger() {
		return this.isValidRepresentationOf(Integer::parseInt, CHAR_SEQUENCE_INTEGER);
	}

	/**
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isLong() {
		return this.isValidRepresentationOf(Long::parseLong, CHAR_SEQUENCE_LONG);
	}

	/**
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isFloat() {
		return this.isValidRepresentationOf(Float::parseFloat, CHAR_SEQUENCE_FLOAT);
	}

	/**
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isDouble() {
		return this.isValidRepresentationOf(Double::parseDouble, CHAR_SEQUENCE_DOUBLE);
	}

	/**
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isBigInteger() {
		return this.isValidRepresentationOf(BigInteger::new, CHAR_SEQUENCE_BIGINTEGER);
	}

	/**
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isBigDecimal() {
		return this.isValidRepresentationOf(BigDecimal::new, CHAR_SEQUENCE_BIGDECIMAL);
	}

	/**
	 * @since 0.12.0
	 */
	public CharSequenceConstraint<T, E> isIsoLocalDate() {
		return this.isLocalDate("uuuu-MM-dd");
	}

	/**
	 * @since 0.12.0
	 */
	public CharSequenceConstraint<T, E> isLocalDate(String pattern) {
		return this.isLocalDate(pattern, Locale.getDefault());
	}

	public EmojiConstraint<T, E> emoji() {
		return new EmojiConstraint<>(this, this.normalizerForm, this.variantOptions);
	}

	public CharSequenceConstraint<T, E> normalizer(Normalizer.Form normalizerForm) {
		CharSequenceConstraint<T, E> constraint = new CharSequenceConstraint<>(
				normalizerForm, this.variantOptions);
		constraint.predicates().addAll(this.predicates());
		return constraint;
	}

	public CharSequenceConstraint<T, E> notBlank() {
		this.predicates()
				.add(ConstraintPredicate.of(
						x -> x != null && trim(x.toString()).length() != 0,
						CHAR_SEQUENCE_NOT_BLANK, () -> new Object[] {}, INVALID));
		return this;
	}

	public CharSequenceConstraint<T, E> pattern(String regex) {
		this.predicates().add(ConstraintPredicate.of(x -> Pattern.matches(regex, x),
				CHAR_SEQUENCE_PATTERN, () -> new Object[] { regex }, VALID));
		return this;
	}

	/**
	 * @since 0.11.1
	 */
	public CharSequenceConstraint<T, E> pattern(Pattern regex) {
		this.predicates().add(ConstraintPredicate.of(x -> regex.matcher(x).matches(),
				CHAR_SEQUENCE_PATTERN, () -> new Object[] { regex.pattern() }, VALID));
		return this;
	}

	/**
	 * @since 0.11.1
	 */
	public CharSequenceConstraint<T, E> pattern(Supplier<Pattern> regexSupplier) {
		this.predicates()
				.add(ConstraintPredicate.of(x -> regexSupplier.get().matcher(x).matches(),
						CHAR_SEQUENCE_PATTERN,
						() -> new Object[] { regexSupplier.get().pattern() }, VALID));
		return this;
	}

	/**
	 * @since 0.7.0
	 */
	public CharSequenceConstraint<T, E> ipv4() {
		this.predicates()
				.add(ConstraintPredicate.of(x -> InetAddressUtils.isIpv4(x.toString()),
						CHAR_SEQUENCE_IPV4, () -> new Object[] {}, VALID));
		return this;
	}

	/**
	 * @since 0.7.0
	 */
	public CharSequenceConstraint<T, E> ipv6() {
		this.predicates()
				.add(ConstraintPredicate.of(x -> InetAddressUtils.isIpv6(x.toString()),
						CHAR_SEQUENCE_IPV6, () -> new Object[] {}, VALID));
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
		}, CHAR_SEQUENCE_URL, () -> new Object[] {}, VALID));
		return this;
	}

	/**
	 * @since 0.10.0
	 */
	public CharSequenceConstraint<T, E> uuid() {
		this.predicates().add(ConstraintPredicate.of(x -> {
			if (size().applyAsInt(x) == 0) {
				return true;
			}
			return VALID_UUID_REGEX.matcher(x).matches();
		}, CHAR_SEQUENCE_UUID, () -> new Object[] {}, VALID));
		return this;
	}

	/**
	 * @since 0.7.0
	 */
	public CharSequenceConstraint<T, E> luhn() {
		this.predicates().add(ConstraintPredicate.of(CharSequenceConstraint::luhnCheck,
				CHAR_SEQUENCE_LUHN, () -> new Object[] {}, VALID));
		return this;
	}

	// https://github.com/apache/commons-validator/blob/master/src/main/java/org/apache/commons/validator/CreditCardValidator.java
	static boolean luhnCheck(CharSequence cardNumber) {
		// number must be validated as 0..9 numeric first!!
		final int digits = cardNumber.length();
		final int oddOrEven = digits & 1;
		long sum = 0;
		for (int count = 0; count < digits; count++) {
			int digit;
			try {
				digit = Integer.parseInt(cardNumber.charAt(count) + "");
			}
			catch (NumberFormatException e) {
				return false;
			}
			if (((count & 1) ^ oddOrEven) == 0) { // not
				digit *= 2;
				if (digit > 9) {
					digit -= 9;
				}
			}
			sum += digit;
		}
		return sum != 0 && (sum % 10 == 0);
	}

	public CharSequenceConstraint<T, E> variant(
			Function<VariantOptions.Builder, VariantOptions.Builder> opts) {
		VariantOptions.Builder builder = VariantOptions.builder();
		CharSequenceConstraint<T, E> constraint = new CharSequenceConstraint<>(
				this.normalizerForm, opts.apply(builder).build());
		constraint.predicates().addAll(this.predicates());
		return constraint;
	}

	protected String normalize(String s) {
		String str = this.variantOptions.ignored(s);
		return this.normalizerForm == null ? str
				: Normalizer.normalize(str, this.normalizerForm);
	}

	@Override
	protected ToIntFunction<E> size() {
		return cs -> {
			String s = this.normalize(cs.toString());
			return s.codePointCount(0, s.length());
		};
	}

	private static String trim(String s) {
		if (s.length() == 0) {
			return s;
		}
		StringBuilder sb = new StringBuilder(s);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
}
