/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.constraint.charsequence.ByteSizeConstraint;
import am.ik.yavi.constraint.charsequence.CodePoints;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsRanges;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsSet;
import am.ik.yavi.constraint.charsequence.CodePoints.Range;
import am.ik.yavi.constraint.charsequence.CodePointsConstraint;
import am.ik.yavi.constraint.charsequence.EmojiConstraint;
import am.ik.yavi.constraint.charsequence.UrlConstraintBuilder;
import am.ik.yavi.constraint.charsequence.variant.VariantOptions;
import am.ik.yavi.constraint.inetaddress.InetAddressUtils;
import am.ik.yavi.constraint.password.CharSequencePasswordPoliciesBuilder;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.ViolationMessage;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

import org.jspecify.annotations.Nullable;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

import static am.ik.yavi.core.NullAs.INVALID;
import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_BIGDECIMAL;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_BIGINTEGER;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_BYTE;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_CONTAINS;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_DOUBLE;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_EMAIL;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_ENDSWITH;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_FLOAT;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_INTEGER;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_IP;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_IPV4;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_IPV6;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_LOCAL_DATE;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_LONG;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_LUHN;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_NOT_BLANK;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_PATTERN;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_SHORT;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_STARTSWITH;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_ULID;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_URI;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_UUID;

/**
 * Constraints on a character sequence value.
 *
 * <p>
 * The size constraints inherited from
 * {@link am.ik.yavi.constraint.base.ContainerConstraintBase} ({@code fixedSize},
 * {@code greaterThan}, {@code greaterThanOrEqual}, {@code lessThan} and
 * {@code lessThanOrEqual}) count the Unicode code points of the value after the ignored
 * variation selectors are removed and the value is normalized to the configured form. The
 * default form is {@link java.text.Normalizer.Form#NFC}, so a character written as a
 * combining sequence is counted as one code point. An emoji that consists of several code
 * points, such as a ZWJ sequence or a character with a skin tone modifier, is counted per
 * code point; use {@link #emoji()} to count it as one.
 * </p>
 *
 * <p>
 * The other checks inspect the value as given. No normalization or variation selector
 * removal is applied to {@link #contains(CharSequence)},
 * {@link #startsWith(CharSequence)}, {@link #endsWith(CharSequence)}, {@link #email()},
 * {@link #pattern(String)}, {@link #url()}, {@link #uri()}, {@link #ip()},
 * {@link #ipv4()}, {@link #ipv6()}, {@link #uuid()}, {@link #ulid()}, {@link #luhn()},
 * {@link #asByteArray()} or {@link #codePoints(CodePoints)}.
 * </p>
 *
 * <p>
 * Unless stated otherwise, a {@code null} value is valid and the check is skipped. Only
 * {@link #notBlank()} reports a {@code null} value as a violation.
 * </p>
 *
 * <pre>{@code
 * Validator<Car> validator = ValidatorBuilder.<Car>of()
 *     ._string(Car::licensePlate, "licensePlate", c -> c.notBlank().lessThanOrEqual(14))
 *     ._string(Car::email, "email", c -> c.email())
 *     .build();
 * }</pre>
 *
 * @param <T> the type of the object that owns the property
 * @param <E> the type of the character sequence the constraints apply to
 * @since 0.0.1
 */
public class CharSequenceConstraint<T, E extends CharSequence>
		extends ContainerConstraintBase<T, E, CharSequenceConstraint<T, E>> {

	private static final String EMAIL_PART = "[^\\x00-\\x1F()<>@,;:\\\\\".\\[\\]\\s]";

	private static final String DOMAIN_PATTERN = EMAIL_PART + "+(\\." + EMAIL_PART + "+)*";

	private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^" + EMAIL_PART + "+(\\." + EMAIL_PART
			+ "+)*@(" + DOMAIN_PATTERN + "|" + InetAddressUtils.IPV4_REGEX + ")$", Pattern.CASE_INSENSITIVE);

	private static final Pattern VALID_UUID_REGEX = Pattern.compile("\\p{XDigit}{8}(-\\p{XDigit}{4}){4}\\p{XDigit}{8}");

	// 26 characters of Crockford's Base32; the first one holds only 3 of the 128 bits
	private static final Pattern VALID_ULID_REGEX = Pattern.compile("[0-7][0-9A-HJKMNP-TV-Z]{25}",
			Pattern.CASE_INSENSITIVE);

	/**
	 * The normalization form applied before the size is measured, or {@code null} if
	 * normalization is disabled.
	 */
	protected final Normalizer.@Nullable Form normalizerForm;

	/**
	 * The variation selectors removed before the size is measured.
	 */
	protected final VariantOptions variantOptions;

	/**
	 * Creates a constraint that normalizes with {@link java.text.Normalizer.Form#NFC} and
	 * ignores no variation selector.
	 */
	public CharSequenceConstraint() {
		this(Normalizer.Form.NFC, VariantOptions.builder().build());
	}

	/**
	 * Creates a constraint with the given normalization and variation selector settings.
	 * @param normalizerForm the normalization form applied before the size is measured,
	 * or {@code null} to skip normalization
	 * @param variantOptions the variation selectors to ignore before the size is measured
	 */
	public CharSequenceConstraint(Normalizer.@Nullable Form normalizerForm, VariantOptions variantOptions) {
		this.normalizerForm = normalizerForm;
		this.variantOptions = variantOptions;
	}

	/**
	 * Returns a constraint that measures the value in bytes of the given charset instead
	 * of code points.
	 * @param charset the charset used to encode the value
	 * @return the byte size constraint
	 */
	public ByteSizeConstraint<T, E> asByteArray(Charset charset) {
		return new ByteSizeConstraint<>(this, charset);
	}

	/**
	 * Returns a constraint that measures the value in UTF-8 bytes instead of code points.
	 * @return the byte size constraint
	 */
	public ByteSizeConstraint<T, E> asByteArray() {
		return this.asByteArray(StandardCharsets.UTF_8);
	}

	/**
	 * Returns this constraint.
	 * @return this constraint
	 */
	@Override
	public CharSequenceConstraint<T, E> cast() {
		return this;
	}

	/**
	 * Starts building a constraint on the code points of the value.
	 *
	 * <p>
	 * The result of the given {@link CodePoints} decides which code points the constraint
	 * reports: {@link CodePointsConstraint.Builder#asWhiteList()} rejects the values that
	 * contain a code point outside of it, and
	 * {@link CodePointsConstraint.Builder#asBlackList()} rejects the values that contain
	 * a code point of it.
	 * </p>
	 * @param codePoints the code point definition
	 * @return the builder for the code point constraint
	 */
	public CodePointsConstraint.Builder<T, E> codePoints(CodePoints<E> codePoints) {
		return new CodePointsConstraint.Builder<>(this, codePoints);
	}

	/**
	 * Starts building a constraint on the code points of the value, using the given set
	 * as the code point definition. With
	 * {@link CodePointsConstraint.Builder#asWhiteList()} the set contains the allowed
	 * code points, with {@link CodePointsConstraint.Builder#asBlackList()} the rejected
	 * ones.
	 * @param allowedCodePoints the code points of the definition
	 * @return the builder for the code point constraint
	 */
	public CodePointsConstraint.Builder<T, E> codePoints(Set<Integer> allowedCodePoints) {
		return this.codePoints((CodePointsSet<E>) () -> allowedCodePoints);
	}

	/**
	 * Starts building a constraint on the code points of the value, using the inclusive
	 * range between {@code begin} and {@code end} as the code point definition.
	 * @param begin the inclusive lower bound of the definition
	 * @param end the inclusive upper bound of the definition
	 * @return the builder for the code point constraint
	 * @throws IllegalArgumentException if {@code begin} is greater than {@code end}
	 */
	public CodePointsConstraint.Builder<T, E> codePoints(int begin, int end) {
		return this.codePoints(Range.of(begin, end));
	}

	/**
	 * Starts building a constraint on the code points of the value, using the union of
	 * the given inclusive ranges as the code point definition.
	 * @param range the first range of the definition
	 * @param ranges the remaining ranges of the definition
	 * @return the builder for the code point constraint
	 */
	public CodePointsConstraint.Builder<T, E> codePoints(Range range, Range... ranges) {
		return this.codePoints((CodePointsRanges<E>) () -> {
			List<Range> list = new ArrayList<>();
			list.add(range);
			list.addAll(Arrays.asList(ranges));
			return list;
		});
	}

	/**
	 * Requires the value to contain {@code s}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.contains}, where
	 * {@code {1}} is {@code s}.
	 * </p>
	 * @param s the required substring
	 * @return this constraint
	 */
	public CharSequenceConstraint<T, E> contains(CharSequence s) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> x.toString().contains(s), CHAR_SEQUENCE_CONTAINS, () -> new Object[] { s },
					VALID));
		return this;
	}

	/**
	 * Requires the value to start with {@code prefix}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.startsWith}, where
	 * {@code {1}} is {@code prefix}.
	 * </p>
	 * @param prefix the prefix the value has to start with
	 * @return this constraint
	 * @since 0.10.0
	 */
	public CharSequenceConstraint<T, E> startsWith(CharSequence prefix) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> x.toString().startsWith(prefix.toString()), CHAR_SEQUENCE_STARTSWITH,
					() -> new Object[] { prefix }, VALID));
		return this;
	}

	/**
	 * Requires the value to end with {@code suffix}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.endsWith}, where
	 * {@code {1}} is {@code suffix}.
	 * </p>
	 * @param suffix the suffix the value has to end with
	 * @return this constraint
	 * @since 0.10.0
	 */
	public CharSequenceConstraint<T, E> endsWith(CharSequence suffix) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> x.toString().endsWith(suffix.toString()), CHAR_SEQUENCE_ENDSWITH,
					() -> new Object[] { suffix }, VALID));
		return this;
	}

	/**
	 * Requires the value to be a valid email address. An empty value is valid.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.email}.
	 * </p>
	 * @return this constraint
	 */
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
	 * Adds the password policy constraints built by the given builder.
	 *
	 * <p>
	 * A missing policy is reported with the message key {@code password.required} or
	 * {@code password.optional}. An empty value does not skip the policies; only a
	 * {@code null} value is skipped.
	 * </p>
	 *
	 * <pre>{@code
	 * c.password(policies -> policies.strong())
	 * }</pre>
	 * @param builder the function that configures the password policies
	 * @return this constraint
	 * @since 0.7.0
	 */
	public CharSequenceConstraint<T, E> password(
			Function<CharSequencePasswordPoliciesBuilder<T, E>, List<ConstraintPredicate<E>>> builder) {
		final List<ConstraintPredicate<E>> predicates = builder.apply(new CharSequencePasswordPoliciesBuilder<>());
		this.predicates().addAll(predicates);
		return this;
	}

	private <U> CharSequenceConstraint<T, E> isValidRepresentationOf(Function<String, U> converter,
			ViolationMessage message) {
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
	private CharSequenceConstraint<T, E> isLocalDatePattern(String pattern) {
		this.predicates().add(ConstraintPredicate.of(x -> {
			try {
				DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT).parse(x);
				return true;
			}
			catch (DateTimeParseException ignored) {
				return false;
			}
		}, CHAR_SEQUENCE_LOCAL_DATE, () -> new Object[] { pattern }, VALID));
		return this;
	}

	/**
	 * Requires the value to be a valid representation of a byte. An empty value is valid.
	 * The value is passed to {@link Byte#parseByte(String)} as given, without trimming.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.byte}.
	 * </p>
	 * @return this constraint
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isByte() {
		return this.isValidRepresentationOf(Byte::parseByte, CHAR_SEQUENCE_BYTE);
	}

	/**
	 * Requires the value to be a valid representation of a short. An empty value is
	 * valid. The value is passed to {@link Short#parseShort(String)} as given, without
	 * trimming.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.short}.
	 * </p>
	 * @return this constraint
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isShort() {
		return this.isValidRepresentationOf(Short::parseShort, CHAR_SEQUENCE_SHORT);
	}

	/**
	 * Requires the value to be a valid representation of an integer. An empty value is
	 * valid. The value is passed to {@link Integer#parseInt(String)} as given, without
	 * trimming.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.integer}.
	 * </p>
	 * @return this constraint
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isInteger() {
		return this.isValidRepresentationOf(Integer::parseInt, CHAR_SEQUENCE_INTEGER);
	}

	/**
	 * Requires the value to be a valid representation of a long. An empty value is valid.
	 * The value is passed to {@link Long#parseLong(String)} as given, without trimming.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.long}.
	 * </p>
	 * @return this constraint
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isLong() {
		return this.isValidRepresentationOf(Long::parseLong, CHAR_SEQUENCE_LONG);
	}

	/**
	 * Requires the value to be a valid representation of a float. An empty value is
	 * valid. The value is passed to {@link Float#parseFloat(String)} as given, without
	 * trimming.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.float}.
	 * </p>
	 * @return this constraint
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isFloat() {
		return this.isValidRepresentationOf(Float::parseFloat, CHAR_SEQUENCE_FLOAT);
	}

	/**
	 * Requires the value to be a valid representation of a double. An empty value is
	 * valid. The value is passed to {@link Double#parseDouble(String)} as given, without
	 * trimming.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.double}.
	 * </p>
	 * @return this constraint
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isDouble() {
		return this.isValidRepresentationOf(Double::parseDouble, CHAR_SEQUENCE_DOUBLE);
	}

	/**
	 * Requires the value to be a valid representation of a big integer. An empty value is
	 * valid. The value is passed to the {@link BigInteger#BigInteger(String)} constructor
	 * as given, without trimming.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.biginteger}.
	 * </p>
	 * @return this constraint
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isBigInteger() {
		return this.isValidRepresentationOf(BigInteger::new, CHAR_SEQUENCE_BIGINTEGER);
	}

	/**
	 * Requires the value to be a valid representation of a big decimal. An empty value is
	 * valid. The value is passed to the {@link BigDecimal#BigDecimal(String)} constructor
	 * as given, without trimming.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.bigdecimal}.
	 * </p>
	 * @return this constraint
	 * @since 0.6.0
	 */
	public CharSequenceConstraint<T, E> isBigDecimal() {
		return this.isValidRepresentationOf(BigDecimal::new, CHAR_SEQUENCE_BIGDECIMAL);
	}

	/**
	 * Requires the value to be a local date in the format {@code uuuu-MM-dd}, resolved
	 * with {@link ResolverStyle#STRICT}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.localdate}, where
	 * {@code {1}} is the pattern and {@code {2}} is the value.
	 * </p>
	 * @return this constraint
	 * @since 0.12.1
	 */
	public CharSequenceConstraint<T, E> isoLocalDate() {
		return this.isLocalDatePattern("uuuu-MM-dd");
	}

	/**
	 * Requires the value to be a valid local date for the given {@link DateTimeFormatter}
	 * pattern, resolved with {@link ResolverStyle#STRICT}.
	 *
	 * <p>
	 * The formatter is created each time a value is validated. A violation is reported
	 * with the message key {@code charSequence.localdate}, where {@code {1}} is the
	 * pattern and {@code {2}} is the value.
	 * </p>
	 * @param pattern the date pattern
	 * @return this constraint
	 * @throws IllegalArgumentException if the pattern is invalid, at validation time
	 * @since 0.12.1
	 */
	public CharSequenceConstraint<T, E> localDate(String pattern) {
		return this.isLocalDatePattern(pattern);
	}

	/**
	 * Returns a constraint that measures the value in extended grapheme clusters instead
	 * of code points. An emoji sequence such as a ZWJ sequence counts as one.
	 * @return the emoji constraint, configured with the current normalizer and variation
	 * selector settings
	 */
	public EmojiConstraint<T, E> emoji() {
		return new EmojiConstraint<>(this, this.normalizerForm, this.variantOptions);
	}

	/**
	 * Returns a new constraint that normalizes with the given form and keeps the
	 * predicates added so far. This constraint is not modified.
	 * @param normalizerForm the normalization form applied before the size is measured,
	 * or {@code null} to skip normalization
	 * @return the new constraint
	 */
	public CharSequenceConstraint<T, E> normalizer(Normalizer.@Nullable Form normalizerForm) {
		CharSequenceConstraint<T, E> constraint = new CharSequenceConstraint<>(normalizerForm, this.variantOptions);
		constraint.predicates().addAll(this.predicates());
		return constraint;
	}

	/**
	 * Requires the value to contain at least one non-whitespace character. A {@code null}
	 * value is reported as a violation. A character is whitespace according to
	 * {@link Character#isWhitespace(char)}, which includes the ideographic space.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.notBlank}.
	 * </p>
	 * @return this constraint
	 */
	public CharSequenceConstraint<T, E> notBlank() {
		this.predicates()
			.add(ConstraintPredicate.of(x -> x != null && trim(x.toString()).length() != 0, CHAR_SEQUENCE_NOT_BLANK,
					() -> new Object[] {}, INVALID));
		return this;
	}

	/**
	 * Requires the value to match the given regular expression in its entirety, as
	 * {@link java.util.regex.Pattern#matches(String, CharSequence)} does.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.pattern}, where
	 * {@code {1}} is {@code regex}.
	 * </p>
	 * @param regex the regular expression
	 * @return this constraint
	 * @throws java.util.regex.PatternSyntaxException if {@code regex} is invalid, at
	 * validation time
	 */
	public CharSequenceConstraint<T, E> pattern(String regex) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> Pattern.matches(regex, x), CHAR_SEQUENCE_PATTERN,
					() -> new Object[] { regex }, VALID));
		return this;
	}

	/**
	 * Requires the value to match the given regular expression in its entirety.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.pattern}, where
	 * {@code {1}} is the pattern of {@code regex}.
	 * </p>
	 * @param regex the regular expression
	 * @return this constraint
	 * @since 0.11.1
	 */
	public CharSequenceConstraint<T, E> pattern(Pattern regex) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> regex.matcher(x).matches(), CHAR_SEQUENCE_PATTERN,
					() -> new Object[] { regex.pattern() }, VALID));
		return this;
	}

	/**
	 * Requires the value to match, in its entirety, the regular expression supplied by
	 * {@code regexSupplier}. The supplier is called each time a value is validated, so
	 * the expression can be created lazily.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.pattern}, where
	 * {@code {1}} is the pattern supplied for the validated value.
	 * </p>
	 * @param regexSupplier the supplier of the regular expression
	 * @return this constraint
	 * @since 0.11.1
	 */
	public CharSequenceConstraint<T, E> pattern(Supplier<Pattern> regexSupplier) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> regexSupplier.get().matcher(x).matches(), CHAR_SEQUENCE_PATTERN,
					() -> new Object[] { regexSupplier.get().pattern() }, VALID));
		return this;
	}

	/**
	 * Requires the value to be an IPv4 address in dotted decimal notation. Unlike
	 * {@link #email()}, an empty value is not valid. See
	 * {@link InetAddressUtils#isIpv4(String)} for the accepted form.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.ipv4}.
	 * </p>
	 * @return this constraint
	 * @since 0.7.0
	 */
	public CharSequenceConstraint<T, E> ipv4() {
		this.predicates()
			.add(ConstraintPredicate.of(x -> InetAddressUtils.isIpv4(x.toString()), CHAR_SEQUENCE_IPV4,
					() -> new Object[] {}, VALID));
		return this;
	}

	/**
	 * Requires the value to be an IP address of either version, that is to satisfy
	 * {@link #ipv4()} or {@link #ipv6()}. An empty value is not valid.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.ip}.
	 * </p>
	 * @return this constraint
	 * @since 0.18.0
	 */
	public CharSequenceConstraint<T, E> ip() {
		this.predicates().add(ConstraintPredicate.of(x -> {
			final String address = x.toString();
			return InetAddressUtils.isIpv4(address) || InetAddressUtils.isIpv6(address);
		}, CHAR_SEQUENCE_IP, () -> new Object[] {}, VALID));
		return this;
	}

	/**
	 * Requires the value to be an IPv6 address. An empty value is not valid. See
	 * {@link InetAddressUtils#isIpv6(String)} for the accepted forms, including the zone
	 * id and the prefix length.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.ipv6}.
	 * </p>
	 * @return this constraint
	 * @since 0.7.0
	 */
	public CharSequenceConstraint<T, E> ipv6() {
		this.predicates()
			.add(ConstraintPredicate.of(x -> InetAddressUtils.isIpv6(x.toString()), CHAR_SEQUENCE_IPV6,
					() -> new Object[] {}, VALID));
		return this;
	}

	/**
	 * Requires the value to be a URL. An empty value is considered valid.
	 * <p>
	 * Since 0.17.0 the value is parsed with {@link java.net.URI} instead of the
	 * deprecated {@code URL(String)} constructor, which silently strips leading and
	 * trailing characters up to {@code ' '} before parsing. Values that only passed
	 * because of that trimming, that contain unencoded characters, or that have an empty
	 * authority are now rejected.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.url}.
	 * </p>
	 * @return this constraint
	 */
	public CharSequenceConstraint<T, E> url() {
		return this.url(UrlConstraintBuilder::build);
	}

	/**
	 * Requires the value to be a URL and restricts its protocol, host and/or port with
	 * the given builder. An empty value is considered valid.
	 *
	 * <pre>{@code
	 * c.url(url -> url.protocol("http", "https").host("example.com").build())
	 * }</pre>
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.url}, and with
	 * {@code charSequence.url.protocol}, {@code charSequence.url.host} or
	 * {@code charSequence.url.port} for an unexpected attribute.
	 * </p>
	 * @param builder the function that configures the URL restrictions
	 * @return this constraint
	 * @since 0.17.1
	 */
	public CharSequenceConstraint<T, E> url(Function<UrlConstraintBuilder<E>, List<ConstraintPredicate<E>>> builder) {
		this.predicates().addAll(builder.apply(new UrlConstraintBuilder<>(this.size())));
		return this;
	}

	/**
	 * Requires the value to be a URI with a scheme of any kind, such as
	 * {@code https://example.com/a?b=c}, {@code mailto:a@example.com} or
	 * {@code urn:isbn:9784774183619}. An empty value is considered valid.
	 *
	 * <p>
	 * Unlike {@link #url()}, the scheme does not have to be one that {@link java.net.URL}
	 * has a handler for. The value is parsed with {@link java.net.URI}; a reference
	 * without a scheme ({@code /a/b}) and a value with a character outside of US-ASCII,
	 * which RFC 3986 requires to be percent-encoded, are not valid.
	 * </p>
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.uri}.
	 * </p>
	 * @return this constraint
	 * @since 0.18.0
	 */
	public CharSequenceConstraint<T, E> uri() {
		this.predicates().add(ConstraintPredicate.of(x -> {
			if (size().applyAsInt(x) == 0) {
				return true;
			}
			final String value = x.toString();
			if (!value.chars().allMatch(c -> c < 0x80)) {
				return false;
			}
			try {
				return new URI(value).isAbsolute();
			}
			catch (URISyntaxException e) {
				return false;
			}
		}, CHAR_SEQUENCE_URI, () -> new Object[] {}, VALID));
		return this;
	}

	/**
	 * Requires the value to be a UUID. An empty value is valid. Only the 8-4-4-4-12
	 * hexadecimal format is checked; the version and the variant are not validated.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.uuid}.
	 * </p>
	 * @return this constraint
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
	 * Requires the value to be a ULID: 26 characters of Crockford's Base32, of which the
	 * first is {@code 0} to {@code 7} because a ULID has 128 bits. Lower case letters are
	 * accepted. An empty value is valid.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.ulid}.
	 * </p>
	 * @return this constraint
	 * @since 0.18.0
	 */
	public CharSequenceConstraint<T, E> ulid() {
		this.predicates().add(ConstraintPredicate.of(x -> {
			if (size().applyAsInt(x) == 0) {
				return true;
			}
			return VALID_ULID_REGEX.matcher(x).matches();
		}, CHAR_SEQUENCE_ULID, () -> new Object[] {}, VALID));
		return this;
	}

	/**
	 * Requires the value to pass the Luhn check, which is used for credit card numbers.
	 * The value must consist of decimal digits; an empty value or a value with any other
	 * character is not valid.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.luhn}.
	 * </p>
	 * @return this constraint
	 * @since 0.7.0
	 */
	public CharSequenceConstraint<T, E> luhn() {
		this.predicates()
			.add(ConstraintPredicate.of(CharSequenceConstraint::luhnCheck, CHAR_SEQUENCE_LUHN, () -> new Object[] {},
					VALID));
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

	/**
	 * Returns a new constraint that removes the variation selectors selected by the given
	 * options before the size is measured and keeps the predicates added so far. This
	 * constraint is not modified.
	 *
	 * <pre>{@code
	 * c.variant(opts -> opts.fvs(MongolianFreeVariationSelector.IGNORE)).fixedSize(1)
	 * }</pre>
	 * @param opts the function that configures the variation options
	 * @return the new constraint
	 */
	public CharSequenceConstraint<T, E> variant(Function<VariantOptions.Builder, VariantOptions.Builder> opts) {
		VariantOptions.Builder builder = VariantOptions.builder();
		CharSequenceConstraint<T, E> constraint = new CharSequenceConstraint<>(this.normalizerForm,
				opts.apply(builder).build());
		constraint.predicates().addAll(this.predicates());
		return constraint;
	}

	/**
	 * Returns the given value with the ignored variation selectors removed and normalized
	 * to the configured form.
	 * @param s the value to normalize
	 * @return the normalized value
	 */
	protected String normalize(String s) {
		String str = this.variantOptions.ignored(s);
		return this.normalizerForm == null ? str : Normalizer.normalize(str, this.normalizerForm);
	}

	/**
	 * Returns a function that counts the Unicode code points of the normalized value.
	 * @return the function that computes the size of a value
	 */
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
