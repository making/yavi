/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.List;

/**
 * Defines the message id and the default message format of a constraint violation.
 *
 * <p>
 * A {@code ViolationMessage} is used to attach a message to a constraint, for example
 * with {@link Constraint#message(ViolationMessage)}. The message id identifies the
 * message in a resource bundle so that it can be localized by a
 * {@link am.ik.yavi.message.MessageFormatter}, while the default message format is used
 * when no localized message is found. A message {@linkplain #withText(String) written as
 * text} keeps the id only to name the violation: its format is not replaced.
 *
 * <p>
 * A message can also name its arguments with {@link #paramNames()}, so that a violation
 * exposes them by name ({@link ConstraintViolation#params()}) to a consumer that does not
 * know the positions of the message format.
 *
 * <p>
 * Implementations must be immutable. This interface is also used as the message type of
 * custom constraints; see {@link CustomConstraint}.
 *
 * @since 0.1.0
 */
public interface ViolationMessage {

	/**
	 * Creates a {@code ViolationMessage} with the given message id and default message
	 * format.
	 * @param messageKey the message id used for message lookup
	 * @param defaultMessageFormat the message format used when no localized message is
	 * found
	 * @return a new {@code ViolationMessage}
	 */
	static ViolationMessage of(String messageKey, String defaultMessageFormat) {
		return of(messageKey, defaultMessageFormat, new String[0]);
	}

	/**
	 * Creates a {@code ViolationMessage} with the given message id and default message
	 * format, whose arguments have the given names.
	 *
	 * <pre>{@code
	 * // "{1}" is "min" and "{2}" is "max"; the violated value, which follows them, has no name
	 * ViolationMessage.of("order.quantity", "\"{0}\" must be between {1} and {2}", "min", "max");
	 * }</pre>
	 * @param messageKey the message id used for message lookup
	 * @param defaultMessageFormat the message format used when no localized message is
	 * found
	 * @param paramNames the names of the message arguments from <code>{1}</code> on (see
	 * {@link #paramNames()})
	 * @return a new {@code ViolationMessage}
	 * @since 0.18.0
	 */
	static ViolationMessage of(String messageKey, String defaultMessageFormat, String... paramNames) {
		final List<String> names = List.of(paramNames);
		return new ViolationMessage() {
			@Override
			public String defaultMessageFormat() {
				return defaultMessageFormat;
			}

			@Override
			public String messageKey() {
				return messageKey;
			}

			@Override
			public List<String> paramNames() {
				return names;
			}
		};
	}

	/**
	 * Returns the default message format of this violation message.
	 * @return the message format used when no localized message is found
	 */
	String defaultMessageFormat();

	/**
	 * Returns the message id of this violation message.
	 * @return the message id used for message lookup
	 */
	String messageKey();

	/**
	 * Returns the names of the message arguments that follow the name of the value: the
	 * first name is that of <code>{1}</code>, the second that of <code>{2}</code> and so
	 * on. A violation exposes the arguments that have a name as
	 * {@link ConstraintViolation#params()}, which is what
	 * {@link ConstraintViolations#details()} returns instead of the positional arguments.
	 *
	 * <p>
	 * An argument without a name is not exposed. Give no name to the violated value,
	 * which is the last argument, unless it is meant to be returned to the client: it may
	 * be a password or a token. The message format is not affected by the names and keeps
	 * referring to the arguments by position.
	 * @return the unmodifiable names of the arguments; empty by default
	 * @since 0.18.0
	 */
	default List<String> paramNames() {
		return List.of();
	}

	/**
	 * Returns whether a {@link am.ik.yavi.message.MessageFormatter} may look up a message
	 * for the key of this message, so that a resource bundle localizes or replaces the
	 * format. A message made with {@link #withText(String)} is not: its format is the
	 * message.
	 * @return {@code true} by default
	 * @since 0.18.0
	 */
	default boolean localizable() {
		return true;
	}

	/**
	 * Returns this message with the given text as its format, which no
	 * {@link am.ik.yavi.message.MessageFormatter} replaces with a message found for the
	 * key: the text is shown even when a resource bundle has a message for the key. The
	 * key and the names of the arguments are kept, so that the violation is still known
	 * by its key, and the arguments are still substituted.
	 *
	 * <pre>{@code
	 * ViolationMessage.Default.OBJECT_REQUIRED.withText("Select at least one role");
	 * }</pre>
	 *
	 * <p>
	 * This is the message that a text given in code for one constraint or one property
	 * becomes, such as {@code message("...")} of a constraint and {@code required("...")}
	 * of a property. To let a resource bundle localize the text, give a message with a
	 * key of its own with {@link #of(String, String, String...)} instead.
	 * @param messageFormat the message format, which may refer to the arguments
	 * @return a message that is not {@link #localizable()}
	 * @since 0.18.0
	 */
	default ViolationMessage withText(String messageFormat) {
		final String messageKey = this.messageKey();
		final List<String> names = this.paramNames();
		return new ViolationMessage() {
			@Override
			public String defaultMessageFormat() {
				return messageFormat;
			}

			@Override
			public String messageKey() {
				return messageKey;
			}

			@Override
			public List<String> paramNames() {
				return names;
			}

			@Override
			public boolean localizable() {
				return false;
			}
		};
	}

	/**
	 * Default messages for the constraints provided by this library.
	 *
	 * <p>
	 * In every default message format, {@code {0}} is the property name. The last
	 * argument is reserved for the value that violated the constraint, so it is
	 * referenced by the highest placeholder of a message that reports the actual value
	 * (for example the given size). The remaining arguments depend on the constraint and
	 * are described for each constant.
	 *
	 * <p>
	 * The arguments that the constraint is configured with are named after the parameters
	 * of the constraint method ({@code max} of {@code lessThanOrEqual(max)}), and so is
	 * what a constraint computes from the value (the {@code actualSize}, the
	 * {@code duplicates}). The value that violated the constraint has no name; see
	 * {@link #paramNames()}.
	 *
	 * @since 0.1.0
	 */
	enum Default implements ViolationMessage {

		/**
		 * Message id {@code object.notNull}. Default message: {@code "{0}" must not be
		 * null}. {@code {0}} is the property name.
		 */
		OBJECT_NOT_NULL("object.notNull", "\"{0}\" must not be null"), //
		/**
		 * Message id {@code object.isNull}. Default message: {@code "{0}" must be null}.
		 * {@code {0}} is the property name.
		 */
		OBJECT_IS_NULL("object.isNull", "\"{0}\" must be null"), //
		/**
		 * Message id {@code object.equalTo}. Default message: {@code "{0}" must be equal
		 * to {1}}. {@code {0}} is the property name and {@code {1}} is the expected
		 * value.
		 */
		OBJECT_EQUAL_TO("object.equalTo", "\"{0}\" must be equal to {1}", "value"), //
		/**
		 * Message id {@code object.oneOf}. Default message: {@code "{0}" must be one of
		 * the following values: {1}}. {@code {0}} is the property name and {@code {1}} is
		 * the collection of allowed values.
		 */
		OBJECT_ONE_OF("object.oneOf", "\"{0}\" must be one of the following values: {1}", "values"), //
		/**
		 * Message id {@code object.notOneOf}. Default message: {@code "{0}" must not be
		 * one of the following values: {1}}. {@code {0}} is the property name and
		 * {@code {1}} is the collection of forbidden values.
		 */
		OBJECT_NOT_ONE_OF("object.notOneOf", "\"{0}\" must not be one of the following values: {1}", "values"), //
		/**
		 * Message id {@code object.anyOf}. Default message: {@code "{0}" must satisfy at
		 * least one of the {1} alternatives}. {@code {0}} is the property name and
		 * {@code {1}} is the number of alternatives.
		 * @since 0.18.0
		 */
		OBJECT_ANY_OF("object.anyOf", "\"{0}\" must satisfy at least one of the {1} alternatives", "count"), //
		/**
		 * Message id {@code object.type}. Default message: {@code "{0}" must be of type
		 * {1}}. {@code {0}} is the property name and {@code {1}} is the name of the
		 * expected type, such as {@code string}, {@code integer} or {@code object}.
		 * Reported when an untyped input does not represent the expected type.
		 * @since 0.18.0
		 */
		OBJECT_TYPE("object.type", "\"{0}\" must be of type {1}", "type"), //
		/**
		 * Message id {@code object.required}. Default message: {@code "{0}" is required}.
		 * {@code {0}} is the property name. Reported when an input map has no key for a
		 * property that does not accept {@code null}.
		 * @since 0.18.0
		 */
		OBJECT_REQUIRED("object.required", "\"{0}\" is required"), //
		/**
		 * Message id {@code object.unknownProperty}. Default message: {@code "{0}" is not
		 * a known property}. {@code {0}} is the key of the input map that no property is
		 * defined for.
		 * @since 0.18.0
		 */
		OBJECT_UNKNOWN_PROPERTY("object.unknownProperty", "\"{0}\" is not a known property"), //
		/**
		 * Message id {@code container.notEmpty}. Default message: {@code "{0}" must not
		 * be empty}. {@code {0}} is the property name.
		 */
		CONTAINER_NOT_EMPTY("container.notEmpty", "\"{0}\" must not be empty"), //
		/**
		 * Message id {@code container.lessThan}. Default message: {@code The size of
		 * "{0}" must be less than {1}. The given size is {2}}. {@code {0}} is the
		 * property name, {@code {1}} is the maximum size and {@code {2}} is the actual
		 * size.
		 */
		CONTAINER_LESS_THAN("container.lessThan", "The size of \"{0}\" must be less than {1}. The given size is {2}",
				"max", "actualSize"), //
		/**
		 * Message id {@code container.lessThanOrEqual}. Default message: {@code The size
		 * of "{0}" must be less than or equal to {1}. The given size is {2}}. {@code {0}}
		 * is the property name, {@code {1}} is the maximum size and {@code {2}} is the
		 * actual size.
		 */
		CONTAINER_LESS_THAN_OR_EQUAL("container.lessThanOrEqual",
				"The size of \"{0}\" must be less than or equal to {1}. The given size is {2}", "max", "actualSize"), //
		/**
		 * Message id {@code container.greaterThan}. Default message: {@code The size of
		 * "{0}" must be greater than {1}. The given size is {2}}. {@code {0}} is the
		 * property name, {@code {1}} is the minimum size and {@code {2}} is the actual
		 * size.
		 */
		CONTAINER_GREATER_THAN("container.greaterThan",
				"The size of \"{0}\" must be greater than {1}. The given size is {2}", "min", "actualSize"), //
		/**
		 * Message id {@code container.greaterThanOrEqual}. Default message: {@code The
		 * size of "{0}" must be greater than or equal to {1}. The given size is {2}}.
		 * {@code {0}} is the property name, {@code {1}} is the minimum size and
		 * {@code {2}} is the actual size.
		 */
		CONTAINER_GREATER_THAN_OR_EQUAL("container.greaterThanOrEqual",
				"The size of \"{0}\" must be greater than or equal to {1}. The given size is {2}", "min", "actualSize"), //
		/**
		 * Message id {@code container.fixedSize}. Default message: {@code The size of
		 * "{0}" must be {1}. The given size is {2}}. {@code {0}} is the property name,
		 * {@code {1}} is the expected size and {@code {2}} is the actual size.
		 */
		CONTAINER_FIXED_SIZE("container.fixedSize", "The size of \"{0}\" must be {1}. The given size is {2}", "size",
				"actualSize"), //
		/**
		 * Message id {@code numeric.greaterThan}. Default message: {@code "{0}" must be
		 * greater than {1}}. {@code {0}} is the property name and {@code {1}} is the
		 * exclusive lower bound.
		 */
		NUMERIC_GREATER_THAN("numeric.greaterThan", "\"{0}\" must be greater than {1}", "min"), //
		/**
		 * Message id {@code numeric.greaterThanOrEqual}. Default message: {@code "{0}"
		 * must be greater than or equal to {1}}. {@code {0}} is the property name and
		 * {@code {1}} is the inclusive lower bound.
		 */
		NUMERIC_GREATER_THAN_OR_EQUAL("numeric.greaterThanOrEqual", "\"{0}\" must be greater than or equal to {1}",
				"min"), //
		/**
		 * Message id {@code numeric.lessThan}. Default message: {@code "{0}" must be less
		 * than {1}}. {@code {0}} is the property name and {@code {1}} is the exclusive
		 * upper bound.
		 */
		NUMERIC_LESS_THAN("numeric.lessThan", "\"{0}\" must be less than {1}", "max"), //
		/**
		 * Message id {@code numeric.lessThanOrEqual}. Default message: {@code "{0}" must
		 * be less than or equal to {1}}. {@code {0}} is the property name and {@code {1}}
		 * is the inclusive upper bound.
		 */
		NUMERIC_LESS_THAN_OR_EQUAL("numeric.lessThanOrEqual", "\"{0}\" must be less than or equal to {1}", "max"), //
		/**
		 * Message id {@code numeric.positive}. Default message: {@code "{0}" must be
		 * positive}. {@code {0}} is the property name.
		 */
		NUMERIC_POSITIVE("numeric.positive", "\"{0}\" must be positive"), //
		/**
		 * Message id {@code numeric.positiveOrZero}. Default message: {@code "{0}" must
		 * be positive or zero}. {@code {0}} is the property name.
		 */
		NUMERIC_POSITIVE_OR_ZERO("numeric.positiveOrZero", "\"{0}\" must be positive or zero"), //
		/**
		 * Message id {@code numeric.negative}. Default message: {@code "{0}" must be
		 * negative}. {@code {0}} is the property name.
		 */
		NUMERIC_NEGATIVE("numeric.negative", "\"{0}\" must be negative"), //
		/**
		 * Message id {@code numeric.negativeOrZero}. Default message: {@code "{0}" must
		 * be negative or zero}. {@code {0}} is the property name.
		 */
		NUMERIC_NEGATIVE_OR_ZERO("numeric.negativeOrZero", "\"{0}\" must be negative or zero"), //
		/**
		 * Message id {@code numeric.multipleOf}. Default message: {@code "{0}" must be a
		 * multiple of {1}}. {@code {0}} is the property name and {@code {1}} is the
		 * divisor.
		 * @since 0.18.0
		 */
		NUMERIC_MULTIPLE_OF("numeric.multipleOf", "\"{0}\" must be a multiple of {1}", "divisor"), //
		/**
		 * Message id {@code numeric.fractionDigits}. Default message: {@code "{0}" must
		 * have at most {1} fraction digits}. {@code {0}} is the property name and
		 * {@code {1}} is the largest number of digits after the decimal point.
		 *
		 * @since 0.18.0
		 */
		NUMERIC_FRACTION_DIGITS("numeric.fractionDigits", "\"{0}\" must have at most {1} fraction digits", "max"), //
		/**
		 * Message id {@code numeric.integerDigits}. Default message: {@code "{0}" must
		 * have at most {1} integer digits}. {@code {0}} is the property name and
		 * {@code {1}} is the largest number of digits before the decimal point.
		 *
		 * @since 0.18.0
		 */
		NUMERIC_INTEGER_DIGITS("numeric.integerDigits", "\"{0}\" must have at most {1} integer digits", "max"), //
		/**
		 * Message id {@code boolean.isTrue}. Default message: {@code "{0}" must be true}.
		 * {@code {0}} is the property name.
		 */
		BOOLEAN_IS_TRUE("boolean.isTrue", "\"{0}\" must be true"), //
		/**
		 * Message id {@code boolean.isFalse}. Default message: {@code "{0}" must be
		 * false}. {@code {0}} is the property name.
		 */
		BOOLEAN_IS_FALSE("boolean.isFalse", "\"{0}\" must be false"), //
		/**
		 * Message id {@code charSequence.notBlank}. Default message: {@code "{0}" must
		 * not be blank}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_NOT_BLANK("charSequence.notBlank", "\"{0}\" must not be blank"), //
		/**
		 * Message id {@code charSequence.contains}. Default message: {@code "{0}" must
		 * contain {1}}. {@code {0}} is the property name and {@code {1}} is the required
		 * substring.
		 */
		CHAR_SEQUENCE_CONTAINS("charSequence.contains", "\"{0}\" must contain {1}", "substring"), //
		/**
		 * Message id {@code charSequence.startsWith}. Default message: {@code "{0}" must
		 * start with "{1}"}. {@code {0}} is the property name and {@code {1}} is the
		 * required prefix.
		 */
		CHAR_SEQUENCE_STARTSWITH("charSequence.startsWith", "\"{0}\" must start with \"{1}\"", "prefix"), //
		/**
		 * Message id {@code charSequence.endsWith}. Default message: {@code "{0}" must
		 * end with "{1}"}. {@code {0}} is the property name and {@code {1}} is the
		 * required suffix.
		 */
		CHAR_SEQUENCE_ENDSWITH("charSequence.endsWith", "\"{0}\" must end with \"{1}\"", "suffix"), //
		/**
		 * Message id {@code charSequence.email}. Default message: {@code "{0}" must be a
		 * valid email address}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_EMAIL("charSequence.email", "\"{0}\" must be a valid email address"), //
		/**
		 * Message id {@code charSequence.ipv4}. Default message: {@code "{0}" must be a
		 * valid IPv4}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_IPV4("charSequence.ipv4", "\"{0}\" must be a valid IPv4"), //
		/**
		 * Message id {@code charSequence.ipv6}. Default message: {@code "{0}" must be a
		 * valid IPv6}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_IPV6("charSequence.ipv6", "\"{0}\" must be a valid IPv6"), //
		/**
		 * Message id {@code charSequence.ip}. Default message: {@code "{0}" must be a
		 * valid IP address}. {@code {0}} is the property name.
		 * @since 0.18.0
		 */
		CHAR_SEQUENCE_IP("charSequence.ip", "\"{0}\" must be a valid IP address"), //
		/**
		 * Message id {@code charSequence.uri}. Default message: {@code "{0}" must be a
		 * valid URI}. {@code {0}} is the property name.
		 * @since 0.18.0
		 */
		CHAR_SEQUENCE_URI("charSequence.uri", "\"{0}\" must be a valid URI"), //
		/**
		 * Message id {@code charSequence.url}. Default message: {@code "{0}" must be a
		 * valid URL}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_URL("charSequence.url", "\"{0}\" must be a valid URL"), //
		/**
		 * Message id {@code charSequence.url.protocol}. Default message: {@code The
		 * protocol of "{0}" must be one of the following values: {1}}. {@code {0}} is the
		 * property name and {@code {1}} is the collection of allowed protocols.
		 */
		CHAR_SEQUENCE_URL_PROTOCOL("charSequence.url.protocol",
				"The protocol of \"{0}\" must be one of the following values: {1}", "values"), //
		/**
		 * Message id {@code charSequence.url.host}. Default message: {@code The host of
		 * "{0}" must be one of the following values: {1}}. {@code {0}} is the property
		 * name and {@code {1}} is the collection of allowed hosts.
		 */
		CHAR_SEQUENCE_URL_HOST("charSequence.url.host", "The host of \"{0}\" must be one of the following values: {1}",
				"values"), //
		/**
		 * Message id {@code charSequence.url.port}. Default message: {@code The port of
		 * "{0}" must be one of the following values: {1}}. {@code {0}} is the property
		 * name and {@code {1}} is the collection of allowed ports.
		 */
		CHAR_SEQUENCE_URL_PORT("charSequence.url.port", "The port of \"{0}\" must be one of the following values: {1}",
				"values"), //
		/**
		 * Message id {@code charSequence.uuid}. Default message: {@code "{0}" must be a
		 * valid UUID}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_UUID("charSequence.uuid", "\"{0}\" must be a valid UUID"), //
		/**
		 * Message id {@code charSequence.ulid}. Default message: {@code "{0}" must be a
		 * valid ULID}. {@code {0}} is the property name.
		 * @since 0.18.0
		 */
		CHAR_SEQUENCE_ULID("charSequence.ulid", "\"{0}\" must be a valid ULID"), //
		/**
		 * Message id {@code charSequence.pattern}. Default message: {@code "{0}" must
		 * match {1}}. {@code {0}} is the property name and {@code {1}} is the regular
		 * expression.
		 */
		CHAR_SEQUENCE_PATTERN("charSequence.pattern", "\"{0}\" must match {1}", "pattern"), //
		/**
		 * Message id {@code charSequence.luhn}. Default message: {@code the check digit
		 * for "{0}" is invalid, Luhn checksum failed}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_LUHN("charSequence.luhn", "the check digit for \"{0}\" is invalid, Luhn checksum failed"), //
		/**
		 * Message id {@code charSequence.boolean}. Default message: {@code "{0}" must be
		 * a valid representation of a boolean}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_BOOLEAN("charSequence.boolean", "\"{0}\" must be a valid representation of a boolean"), //
		/**
		 * Message id {@code charSequence.byte}. Default message: {@code "{0}" must be a
		 * valid representation of a byte}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_BYTE("charSequence.byte", "\"{0}\" must be a valid representation of a byte"), //
		/**
		 * Message id {@code charSequence.short}. Default message: {@code "{0}" must be a
		 * valid representation of a short}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_SHORT("charSequence.short", "\"{0}\" must be a valid representation of a short"), //
		/**
		 * Message id {@code charSequence.integer}. Default message: {@code "{0}" must be
		 * a valid representation of an integer}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_INTEGER("charSequence.integer", "\"{0}\" must be a valid representation of an integer"), //
		/**
		 * Message id {@code charSequence.long}. Default message: {@code "{0}" must be a
		 * valid representation of a long}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_LONG("charSequence.long", "\"{0}\" must be a valid representation of a long"), //
		/**
		 * Message id {@code charSequence.float}. Default message: {@code "{0}" must be a
		 * valid representation of a float}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_FLOAT("charSequence.float", "\"{0}\" must be a valid representation of a float"), //
		/**
		 * Message id {@code charSequence.double}. Default message: {@code "{0}" must be a
		 * valid representation of a double}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_DOUBLE("charSequence.double", "\"{0}\" must be a valid representation of a double"), //
		/**
		 * Message id {@code charSequence.biginteger}. Default message: {@code "{0}" must
		 * be a valid representation of a big integer}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_BIGINTEGER("charSequence.biginteger", "\"{0}\" must be a valid representation of a big integer"), //
		/**
		 * Message id {@code charSequence.bigdecimal}. Default message: {@code "{0}" must
		 * be a valid representation of a big decimal}. {@code {0}} is the property name.
		 */
		CHAR_SEQUENCE_BIGDECIMAL("charSequence.bigdecimal", "\"{0}\" must be a valid representation of a big decimal"), //
		/**
		 * Message id {@code charSequence.localdate}. Default message: {@code "{0}" must
		 * be a valid representation of a local date using the pattern: {1}. The give
		 * value is: {2}} (the wording "give" is kept as-is). {@code {0}} is the property
		 * name, {@code {1}} is the date pattern and {@code {2}} is the given value.
		 */
		CHAR_SEQUENCE_LOCAL_DATE("charSequence.localdate",
				"\"{0}\" must be a valid representation of a local date using the pattern: {1}. The give value is: {2}",
				"pattern"),
		/**
		 * Message id {@code byteSize.lessThan}. Default message: {@code The byte size of
		 * "{0}" must be less than {1}. The given size is {2}}. {@code {0}} is the
		 * property name, {@code {1}} is the maximum byte size and {@code {2}} is the
		 * actual byte size.
		 */
		BYTE_SIZE_LESS_THAN("byteSize.lessThan",
				"The byte size of \"{0}\" must be less than {1}. The given size is {2}", "max", "actualSize"), //
		/**
		 * Message id {@code byteSize.lessThanOrEqual}. Default message: {@code The byte
		 * size of "{0}" must be less than or equal to {1}. The given size is {2}}.
		 * {@code {0}} is the property name, {@code {1}} is the maximum byte size and
		 * {@code {2}} is the actual byte size.
		 */
		BYTE_SIZE_LESS_THAN_OR_EQUAL("byteSize.lessThanOrEqual",
				"The byte size of \"{0}\" must be less than or equal to {1}. The given size is {2}", "max",
				"actualSize"), //
		/**
		 * Message id {@code byteSize.greaterThan}. Default message: {@code The byte size
		 * of "{0}" must be greater than {1}. The given size is {2}}. {@code {0}} is the
		 * property name, {@code {1}} is the minimum byte size and {@code {2}} is the
		 * actual byte size.
		 */
		BYTE_SIZE_GREATER_THAN("byteSize.greaterThan",
				"The byte size of \"{0}\" must be greater than {1}. The given size is {2}", "min", "actualSize"), //
		/**
		 * Message id {@code byteSize.greaterThanOrEqual}. Default message: {@code The
		 * byte size of "{0}" must be greater than or equal to {1}. The given size is
		 * {2}}. {@code {0}} is the property name, {@code {1}} is the minimum byte size
		 * and {@code {2}} is the actual byte size.
		 */
		BYTE_SIZE_GREATER_THAN_OR_EQUAL("byteSize.greaterThanOrEqual",
				"The byte size of \"{0}\" must be greater than or equal to {1}. The given size is {2}", "min",
				"actualSize"), //
		/**
		 * Message id {@code byteSize.fixedSize}. Default message: {@code The byte size of
		 * "{0}" must be {1}. The given size is {2}}. {@code {0}} is the property name,
		 * {@code {1}} is the expected byte size and {@code {2}} is the actual byte size.
		 */
		BYTE_SIZE_FIXED_SIZE("byteSize.fixedSize", "The byte size of \"{0}\" must be {1}. The given size is {2}",
				"size", "actualSize"), //
		/**
		 * Message id {@code collection.contains}. Default message: {@code "{0}" must
		 * contain {1}}. {@code {0}} is the property name and {@code {1}} is the required
		 * element.
		 */
		COLLECTION_CONTAINS("collection.contains", "\"{0}\" must contain {1}", "element"), //
		/**
		 * Message id {@code collection.unique}. Default message: {@code "{0}" must be
		 * unique. {1} is/are duplicated.}. {@code {0}} is the property name and
		 * {@code {1}} is the collection of duplicated elements.
		 */
		COLLECTION_UNIQUE("collection.unique", "\"{0}\" must be unique. {1} is/are duplicated.", "duplicates"), //
		/**
		 * Message id {@code collection.containsAll}. Default message: {@code "{0}" must
		 * be have of all of the following values: {1}, missing values: {2}}. {@code {0}}
		 * is the property name, {@code {1}} is the required values and {@code {2}} is the
		 * collection of missing values.
		 */
		COLLECTION_CONTAINS_ALL("collection.containsAll",
				"\"{0}\" must be have of all of the following values: {1}, missing values: {2}", "values", "missing"), //
		/**
		 * Message id {@code map.containsValue}. Default message: {@code "{0}" must
		 * contain value {1}}. {@code {0}} is the property name and {@code {1}} is the
		 * required value.
		 */
		MAP_CONTAINS_VALUE("map.containsValue", "\"{0}\" must contain value {1}", "value"), //
		/**
		 * Message id {@code map.containsKey}. Default message: {@code "{0}" must contain
		 * key {1}}. {@code {0}} is the property name and {@code {1}} is the required key.
		 */
		MAP_CONTAINS_KEY("map.containsKey", "\"{0}\" must contain key {1}", "key"), //
		/**
		 * Message id {@code array.contains}. Default message: {@code "{0}" must contain
		 * {1}}. {@code {0}} is the property name and {@code {1}} is the required element.
		 */
		ARRAY_CONTAINS("array.contains", "\"{0}\" must contain {1}", "element"), //
		/**
		 * Message id {@code codePoints.asWhiteList}. Default message: {@code "{1}" is/are
		 * not allowed for "{0}"}. {@code {0}} is the property name and {@code {1}} is the
		 * collection of characters that are not allowed.
		 */
		CODE_POINTS_ALL_INCLUDED("codePoints.asWhiteList", "\"{1}\" is/are not allowed for \"{0}\"", "notAllowed"), //
		/**
		 * Message id {@code codePoints.asBlackList}. Default message: {@code "{1}" is/are
		 * not allowed for "{0}"}. {@code {0}} is the property name and {@code {1}} is the
		 * collection of characters that are not allowed.
		 */
		CODE_POINTS_NOT_INCLUDED("codePoints.asBlackList", "\"{1}\" is/are not allowed for \"{0}\"", "notAllowed"), //
		/**
		 * Message id {@code password.required}. Default message: {@code "{0}" must meet
		 * {1} policy}. {@code {0}} is the property name and {@code {1}} is the name of
		 * the required password policy.
		 */
		PASSWORD_REQUIRED("password.required", "\"{0}\" must meet {1} policy", "policy"), //
		/**
		 * Message id {@code password.optional}. Default message: {@code "{0}" must meet
		 * at least {1} policies from {2}}. {@code {0}} is the property name, {@code {1}}
		 * is the minimum number of policies and {@code {2}} is the collection of optional
		 * policy names.
		 */
		PASSWORD_OPTIONAL("password.optional", "\"{0}\" must meet at least {1} policies from {2}", "min", "policies"), //
		/**
		 * Message id {@code temporal.past}. Default message: {@code "{0}" must be a past
		 * date}. {@code {0}} is the property name.
		 */
		TEMPORAL_PAST("temporal.past", "\"{0}\" must be a past date"), //
		/**
		 * Message id {@code temporal.pastOrPresent}. Default message: {@code "{0}" must
		 * be a date in the past or in the present}. {@code {0}} is the property name.
		 */
		TEMPORAL_PAST_OR_PRESENT("temporal.pastOrPresent", "\"{0}\" must be a date in the past or in the present"), //
		/**
		 * Message id {@code temporal.future}. Default message: {@code "{0}" must be a
		 * future date}. {@code {0}} is the property name.
		 */
		TEMPORAL_FUTURE("temporal.future", "\"{0}\" must be a future date"), //
		/**
		 * Message id {@code temporal.futureOrPresent}. Default message: {@code "{0}" must
		 * be a date in the present or in the future}. {@code {0}} is the property name.
		 */
		TEMPORAL_FUTURE_OR_PRESENT("temporal.futureOrPresent",
				"\"{0}\" must be a date in the present or in the future"), //
		/**
		 * Message id {@code temporal.before}. Default message: {@code "{0}" has to be
		 * before {1}}. {@code {0}} is the property name and {@code {1}} is the boundary
		 * value.
		 */
		TEMPORAL_BEFORE("temporal.before", "\"{0}\" has to be before {1}", "other"), //
		/**
		 * Message id {@code temporal.beforeOrEqual}. Default message: {@code "{0}" has to
		 * be before or equals to {1}}. {@code {0}} is the property name and {@code {1}}
		 * is the boundary value.
		 */
		TEMPORAL_BEFORE_OR_EQUAL("temporal.beforeOrEqual", "\"{0}\" has to be before or equals to {1}", "other"), //
		/**
		 * Message id {@code temporal.after}. Default message: {@code "{0}" has to be
		 * after {1}}. {@code {0}} is the property name and {@code {1}} is the boundary
		 * value.
		 */
		TEMPORAL_AFTER("temporal.after", "\"{0}\" has to be after {1}", "other"), //
		/**
		 * Message id {@code temporal.afterOrEqual}. Default message: {@code "{0}" has to
		 * be after or equals to {1}}. {@code {0}} is the property name and {@code {1}} is
		 * the boundary value.
		 */
		TEMPORAL_AFTER_OR_EQUAL("temporal.afterOrEqual", "\"{0}\" has to be after or equals to {1}", "other"), //
		/**
		 * Message id {@code temporal.between}. Default message: {@code "{0}" has to be
		 * between {1} and {2}}. {@code {0}} is the property name, {@code {1}} is the
		 * start of the range and {@code {2}} is the end of the range.
		 */
		TEMPORAL_BETWEEN("temporal.between", "\"{0}\" has to be between {1} and {2}", "rangeFrom", "rangeTo"), //
		/**
		 * Message id {@code temporal.field}. Default message: {@code The {1} of "{0}" is
		 * invalid}. {@code {0}} is the property name and {@code {1}} is the temporal
		 * field.
		 */
		TEMPORAL_FIELD("temporal.field", "The {1} of \"{0}\" is invalid", "field") //
		;

		private final String defaultMessageFormat;

		private final String messageKey;

		private final List<String> paramNames;

		Default(String messageKey, String defaultMessageFormat, String... paramNames) {
			this.messageKey = messageKey;
			this.defaultMessageFormat = defaultMessageFormat;
			this.paramNames = List.of(paramNames);
		}

		/**
		 * Returns the default message format of this message.
		 * @return the default message format
		 */
		@Override
		public String defaultMessageFormat() {
			return this.defaultMessageFormat;
		}

		/**
		 * Returns the message id of this message.
		 * @return the message id
		 */
		@Override
		public String messageKey() {
			return this.messageKey;
		}

		/**
		 * Returns the names of the arguments of this message.
		 * @return the unmodifiable names of the arguments
		 * @since 0.18.0
		 */
		@Override
		public List<String> paramNames() {
			return this.paramNames;
		}

	}

}
