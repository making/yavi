/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import org.jspecify.annotations.Nullable;
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents a single constraint violation that occurs during validation.
 *
 * <p>
 * A violation carries the property name, the message id, the default message format, the
 * message arguments, the message formatter and the locale. The message arguments start
 * with the property name and end with the value that violated the constraint. A violation
 * that reports that no alternative matched is a {@link NoAlternativeMatched}, which also
 * carries the violations of each alternative.
 *
 * <p>
 * Instances are immutable in these properties, except that {@link #args()} returns the
 * internal array, which should not be modified. Use {@link #builder()} to create
 * instances in a type-safe manner with required and optional properties clearly
 * distinguished.
 *
 * @since 0.1.0
 */
public sealed class ConstraintViolation permits ConstraintViolation.NoAlternativeMatched {

	/**
	 * Arguments to be used when formatting the constraint violation message. The first
	 * element is the property name and the last element is the value that violated the
	 * constraint.
	 */
	private final Object[] args;

	private static final String DEFAULT_SEPARATOR = ".";

	private static final String QUOTED_NAME = "\"{0}\"";

	/**
	 * The default message format to use when no localized message is found for the
	 * message id. This format string may contain placeholders that will be replaced with
	 * values from the args array.
	 */
	private final String defaultMessageFormat;

	/**
	 * The locale to be used for message localization. If not explicitly specified, the
	 * system default locale is used.
	 */
	private final Locale locale;

	/**
	 * The message formatter responsible for formatting the constraint violation message.
	 * This formatter handles the localization and argument substitution in the message
	 * template.
	 */
	private final MessageFormatter messageFormatter;

	/**
	 * The id used to look up localized messages in resource bundles. This is the primary
	 * identifier for retrieving the proper message format.
	 */
	private final String messageKey;

	/**
	 * The field or property name that this constraint violation applies to. This
	 * identifies which part of the validated object failed validation.
	 */
	private final String name;

	/**
	 * The structured path to the value that this constraint violation applies to. Unlike
	 * the name, it does not depend on the message key separator.
	 */
	private final List<PathSegment> path;

	/**
	 * The separator that joins the name with the name of a containing property or element
	 * when this violation is located under it.
	 */
	private final String separator;

	private ConstraintViolation(String name, @Nullable List<PathSegment> path, String separator, String messageKey,
			String defaultMessageFormat, Object[] args, MessageFormatter messageFormatter, Locale locale) {
		this.name = name;
		this.path = path == null ? PathSegment.of(name) : path;
		this.separator = separator;
		this.messageKey = messageKey;
		this.defaultMessageFormat = defaultMessageFormat;
		this.args = args;
		this.messageFormatter = messageFormatter;
		this.locale = locale;
	}

	/**
	 * Returns this violation as one that reports that no alternative matched, with the
	 * violations of each alternative that was tried and failed. This is for a
	 * hand-written validator that chooses between alternatives as
	 * {@link ValueValidator#anyOf(ValueValidator[])} does.
	 *
	 * <p>
	 * The violations of the alternatives must be located relative to the same value as
	 * this violation.
	 * @param alternatives the violations of each alternative, in the order of the
	 * alternatives
	 * @return a new violation with the given alternatives
	 * @since 0.18.0
	 */
	public NoAlternativeMatched withAlternatives(List<? extends List<ConstraintViolation>> alternatives) {
		return new NoAlternativeMatched(this.name, this.path, this.args,
				alternatives.stream().map(ConstraintViolation::unmodifiable).toList(), this);
	}

	private static ConstraintViolations unmodifiable(List<ConstraintViolation> violations) {
		return new ConstraintViolations(List.copyOf(violations));
	}

	/**
	 * Returns the arguments used for message formatting.
	 *
	 * <p>
	 * The first element is the property name and the last element is the value that
	 * violated the constraint.
	 * @return the array of arguments; this is the internal array and should not be
	 * modified by the caller
	 */
	public Object[] args() {
		return this.args;
	}

	/**
	 * Returns the default message format string.
	 * @return the default message format string used when no localized message is found
	 */
	public String defaultMessageFormat() {
		return this.defaultMessageFormat;
	}

	/**
	 * Returns this constraint violation in a form that is ready to be serialized, for
	 * example as an element of the body of an API response.
	 *
	 * <p>
	 * The returned detail contains the {@link #name()}, the {@link #pointer()}, the
	 * message id, the formatted {@link #message()} and the arguments of this violation.
	 * The detail of a {@link NoAlternativeMatched} is a
	 * {@link ViolationDetail.NoAlternativeMatched}, which also holds the details of the
	 * alternatives; any other detail is a {@link ViolationDetail.Simple}.
	 * @return a new {@link ViolationDetail} representing this constraint violation
	 */
	public ViolationDetail detail() {
		return this.detail(this.messageFormatter);
	}

	/**
	 * Returns this constraint violation in a form that is ready to be serialized, using
	 * the provided message formatter to format the message.
	 *
	 * <p>
	 * All other properties remain the same as in {@link #detail()}. The messages of the
	 * alternatives of a {@link NoAlternativeMatched} are formatted with the provided
	 * formatter too.
	 * @param messageFormatter the message formatter to use for formatting the message
	 * @return a new {@link ViolationDetail} representing this constraint violation
	 * @since 0.16.0
	 */
	public ViolationDetail detail(MessageFormatter messageFormatter) {
		return new ViolationDetail.Simple(this.name, this.pointer(), this.messageKey, this.message(messageFormatter),
				this.detailArgs());
	}

	private List<@Nullable Object> detailArgs() {
		return Collections.unmodifiableList(Arrays.asList(this.args.clone()));
	}

	/**
	 * Returns the locale used for message localization.
	 * @return the locale used for message formatting and localization
	 */
	public Locale locale() {
		return this.locale;
	}

	/**
	 * Returns the formatted, localized message for this constraint violation.
	 *
	 * <p>
	 * The message is formatted using the configured message formatter, applying the
	 * message id, default message format, arguments and locale.
	 *
	 * <p>
	 * A violation of a value without a name, such as the root of a validator of a single
	 * value, has no name to show. The formatter is then given the default message format
	 * with the quoted name ({@code "{0}"}) replaced by "the value", so that {@code "{0}"
	 * must not be blank} reads "The value must not be blank". The message id and
	 * {@link #args()} are the same as those of a named violation, and
	 * {@link #defaultMessageFormat()} still returns the format as it was given.
	 * @return the formatted message describing the constraint violation
	 */
	public String message() {
		return this.message(this.messageFormatter);
	}

	/**
	 * Returns the formatted, localized message for this constraint violation using the
	 * provided message formatter instead of the one configured at creation time.
	 *
	 * <p>
	 * All other properties (message id, default message format, arguments and locale)
	 * remain the same as in this violation, including the default message format given
	 * for a value without a name (see {@link #message()}).
	 *
	 * <pre>{@code
	 * ConstraintViolation violation = ...;
	 * String message = violation.message(SimpleMessageFormatter.getInstance());
	 * }</pre>
	 * @param messageFormatter the message formatter to use for formatting the message
	 * @return the formatted message using the provided formatter
	 * @since 0.16.0
	 */
	public String message(MessageFormatter messageFormatter) {
		return messageFormatter.format(this.messageKey, this.messageFormat(), this.args, this.locale);
	}

	private String messageFormat() {
		final boolean unnamed = this.name.isEmpty() && this.args.length > 0 && "".equals(this.args[0]);
		if (!unnamed) {
			return this.defaultMessageFormat;
		}
		final String format = this.defaultMessageFormat.startsWith(QUOTED_NAME)
				? "The value" + this.defaultMessageFormat.substring(QUOTED_NAME.length()) : this.defaultMessageFormat;
		return format.replace(QUOTED_NAME, "the value");
	}

	/**
	 * Returns the message id used for looking up localized messages.
	 * @return the message id for internationalization
	 */
	public String messageKey() {
		return this.messageKey;
	}

	/**
	 * Returns the field or property name that this constraint violation applies to.
	 * @return the name of the field or property that failed validation
	 */
	public String name() {
		return this.name;
	}

	/**
	 * Returns the structured path to the value that this constraint violation applies to.
	 *
	 * <p>
	 * The path carries the same information as {@link #name()} as a list of segments. For
	 * example the name {@code forms[0].street} corresponds to the path
	 * {@code [Property("forms"), Index(0), Property("street")]}. It is built where the
	 * violation is created, not by parsing the name, so it is independent of the message
	 * key separator. A violation on the validated value itself, which has an empty name,
	 * has an empty path.
	 * @return the unmodifiable path
	 * @since 0.18.0
	 */
	public List<PathSegment> path() {
		return this.path;
	}

	/**
	 * Returns the JSON Pointer (RFC 6901) to the value that this constraint violation
	 * applies to, which is the {@link #path()} rendered by
	 * {@link PathSegment#toJsonPointer(List)}.
	 *
	 * <p>
	 * The violation named {@code items[1].name} has the pointer {@code /items/1/name}. A
	 * violation on the validated value itself has the empty pointer. Unlike the name, the
	 * pointer does not depend on the message key separator.
	 * @return the JSON Pointer of the path
	 * @since 0.18.0
	 */
	public String pointer() {
		return PathSegment.toJsonPointer(this.path);
	}

	/**
	 * Returns a string representation of this constraint violation.
	 *
	 * <p>
	 * The string representation includes the name, message id, default message format and
	 * arguments.
	 * @return a string representation of this constraint violation
	 */
	@Override
	public String toString() {
		return "ConstraintViolation{" + "name='" + name + '\'' + ", messageKey='" + messageKey + '\''
				+ ", defaultMessageFormat='" + defaultMessageFormat + '\'' + ", args=" + Arrays.toString(args) + '}';
	}

	/**
	 * Returns the value that violated the constraint.
	 *
	 * <p>
	 * By convention, the last element in the args array is the actual value that violated
	 * the constraint. This method provides convenient access to that value.
	 * @return the value that violated the constraint
	 * @throws ArrayIndexOutOfBoundsException if the args array is empty
	 */
	public Object violatedValue() {
		return this.args[this.args.length - 1];
	}

	/**
	 * Returns a new ConstraintViolation with the name transformed using the provided
	 * function. If the arguments array is not empty, the first element will be updated
	 * with the new name.
	 *
	 * <p>
	 * The structure of an arbitrary new name is unknown, so the {@link #path()} of the
	 * returned violation is a single property segment holding the new name. The path is
	 * kept if the name does not change. The violations of the alternatives of a
	 * {@link NoAlternativeMatched} are renamed with the same function.
	 * @param rename the function to transform the current name
	 * @return a new ConstraintViolation with the renamed name
	 * @since 0.7.0
	 */
	public ConstraintViolation rename(Function<? super String, String> rename) {
		return this.relocate(violation -> rename.apply(violation.name),
				(violation, newName) -> newName.equals(violation.name) ? violation.path : PathSegment.of(newName));
	}

	/**
	 * Returns a new ConstraintViolation located under the property with the given name. A
	 * property segment is prepended to the {@link #path()} and the name is prefixed
	 * accordingly: {@code street} becomes {@code address.street}, {@code [0]} becomes
	 * {@code address[0]} and an empty name becomes {@code address}.
	 *
	 * <p>
	 * The flat name is joined with the message key separator of the validator that
	 * reported the violation, which is {@code .} by default, so a name stays uniform:
	 * {@code address/street} becomes {@code order/address/street}. An empty property name
	 * leaves the violation unchanged. The violations of the alternatives of a
	 * {@link NoAlternativeMatched} are prefixed too.
	 * @param name the name of the property that contains the violated value
	 * @return a new ConstraintViolation under the given property
	 * @since 0.18.0
	 */
	public ConstraintViolation prefixed(String name) {
		if (name.isEmpty()) {
			return this;
		}
		final List<PathSegment> prefix = PathSegment.of(name);
		return this.relocate(violation -> violation.join(name),
				(violation, newName) -> PathSegment.concat(prefix, violation.path));
	}

	/**
	 * Returns a new ConstraintViolation located under the element at the given index. An
	 * index segment is prepended to the {@link #path()} and the name is prefixed
	 * accordingly: {@code street} becomes {@code [0].street} and an empty name becomes
	 * {@code [0]}. The flat name is joined like {@link #prefixed(String)} does, and the
	 * violations of the alternatives of a {@link NoAlternativeMatched} are prefixed too.
	 * @param index the index of the element that contains the violated value
	 * @return a new ConstraintViolation under the given element
	 * @since 0.18.0
	 */
	public ConstraintViolation prefixed(int index) {
		final List<PathSegment> prefix = List.of(new PathSegment.Index(index));
		return this.relocate(violation -> violation.join("[" + index + "]"),
				(violation, newName) -> PathSegment.concat(prefix, violation.path));
	}

	private String join(String prefix) {
		final boolean adjacent = this.name.isEmpty()
				|| (!this.path.isEmpty() && this.path.get(0) instanceof PathSegment.Index);
		return adjacent ? prefix + this.name : prefix + this.separator + this.name;
	}

	/**
	 * Returns a new ConstraintViolation with the name and the path that the given
	 * functions derive from this violation. If the arguments array is not empty, the
	 * first element will be updated with the new name. The violations of the alternatives
	 * of a {@link NoAlternativeMatched} are relocated with the same functions, so that
	 * they stay relative to the same value as the violation.
	 * @param newName the function that derives the new flat name from a violation
	 * @param newPath the function that derives the new structured path from a violation
	 * and its new name
	 * @return a new ConstraintViolation with the derived name and path
	 */
	final ConstraintViolation relocate(Function<? super ConstraintViolation, String> newName,
			BiFunction<? super ConstraintViolation, String, List<PathSegment>> newPath) {
		final String name = newName.apply(this);
		final Object[] newArgs = this.args().clone();
		if (newArgs.length > 0) {
			newArgs[0] = name;
		}
		return this.relocated(name, newPath.apply(this, name), newArgs, newName, newPath);
	}

	/**
	 * Returns a violation of the same type as this one at the given location.
	 */
	ConstraintViolation relocated(String name, List<PathSegment> path, Object[] args,
			Function<? super ConstraintViolation, String> newName,
			BiFunction<? super ConstraintViolation, String, List<PathSegment>> newPath) {
		return new ConstraintViolation(name, path, this.separator, this.messageKey, this.defaultMessageFormat, args,
				this.messageFormatter, this.locale);
	}

	/**
	 * A violation that reports that no alternative matched, such as the one with the
	 * message key {@code object.anyOf} that
	 * {@link ValueValidator#anyOf(ValueValidator[])} reports. It is the only violation of
	 * that result; why each alternative failed is in {@link #alternatives()}.
	 *
	 * <pre>{@code
	 * if (violation instanceof ConstraintViolation.NoAlternativeMatched unmatched) {
	 *     List<ConstraintViolations> alternatives = unmatched.alternatives();
	 * }
	 * }</pre>
	 *
	 * @since 0.18.0
	 * @see ConstraintViolation#withAlternatives(List)
	 */
	public static final class NoAlternativeMatched extends ConstraintViolation {

		private final List<ConstraintViolations> alternatives;

		private NoAlternativeMatched(String name, List<PathSegment> path, Object[] args,
				List<ConstraintViolations> alternatives, ConstraintViolation source) {
			super(name, path, source.separator, source.messageKey, source.defaultMessageFormat, args,
					source.messageFormatter, source.locale);
			this.alternatives = alternatives;
		}

		/**
		 * Returns the violations of each alternative that was tried and failed: one
		 * {@link ConstraintViolations} per alternative, in the order of the alternatives,
		 * so that a client can show the alternative it considers the closest instead of
		 * contradictory messages of all of them. An alternative that is itself a choice
		 * has a {@code NoAlternativeMatched} of its own.
		 *
		 * <p>
		 * The violations of the alternatives are located like this violation: they are
		 * renamed and prefixed with it.
		 * @return the unmodifiable violations of each alternative
		 */
		public List<ConstraintViolations> alternatives() {
			return this.alternatives;
		}

		@Override
		public ViolationDetail.NoAlternativeMatched detail() {
			return this.detail(super.messageFormatter);
		}

		@Override
		public ViolationDetail.NoAlternativeMatched detail(MessageFormatter messageFormatter) {
			return new ViolationDetail.NoAlternativeMatched(this.name(), this.pointer(), this.messageKey(),
					this.message(messageFormatter), super.detailArgs(),
					this.alternatives.stream()
						.map(violations -> violations.stream()
							.map(violation -> violation.detail(messageFormatter))
							.toList())
						.toList());
		}

		@Override
		public NoAlternativeMatched rename(Function<? super String, String> rename) {
			return (NoAlternativeMatched) super.rename(rename);
		}

		@Override
		public NoAlternativeMatched prefixed(String name) {
			return (NoAlternativeMatched) super.prefixed(name);
		}

		@Override
		public NoAlternativeMatched prefixed(int index) {
			return (NoAlternativeMatched) super.prefixed(index);
		}

		@Override
		NoAlternativeMatched relocated(String name, List<PathSegment> path, Object[] args,
				Function<? super ConstraintViolation, String> newName,
				BiFunction<? super ConstraintViolation, String, List<PathSegment>> newPath) {
			return new NoAlternativeMatched(name, path, args,
					this.alternatives.stream()
						.map(violations -> unmodifiable(
								violations.stream().map(violation -> violation.relocate(newName, newPath)).toList()))
						.toList(),
					this);
		}

		@Override
		public String toString() {
			final String violation = super.toString();
			return violation.substring(0, violation.length() - 1) + ", alternatives=" + this.alternatives + '}';
		}

	}

	/**
	 * Creates a constraint violation of the given message on the validated value itself.
	 * This is the short form of {@link #builder()} for a hand-written validator.
	 *
	 * <p>
	 * The violation has an empty name and an empty path, so it takes the name of the
	 * property that the validator is applied to; give it another location with
	 * {@link #prefixed(String)}. The message arguments are the name as <code>{0}</code>,
	 * then the given arguments, then the violated value, as the messages of
	 * {@link ViolationMessage.Default} have them.
	 * @param message the message key and the default message format
	 * @param violatedValue the value that violated the rule, possibly {@code null}
	 * @param locale the locale of the message
	 * @param args the message arguments from <code>{1}</code> on, each possibly
	 * {@code null}
	 * @return a new constraint violation with an empty name
	 * @since 0.18.0
	 * @see Validated#failureWith(ViolationMessage, Object, Locale, Object...)
	 */
	// Message arguments may hold a null violated value; the array type does not
	// express element nullness.
	@SuppressWarnings("NullAway")
	public static ConstraintViolation of(ViolationMessage message, @Nullable Object violatedValue, Locale locale,
			@Nullable Object... args) {
		final Object[] completeArgs = new Object[args.length + 2];
		completeArgs[0] = "";
		System.arraycopy(args, 0, completeArgs, 1, args.length);
		completeArgs[args.length + 1] = violatedValue;
		return new ConstraintViolation("", null, DEFAULT_SEPARATOR, message.messageKey(),
				message.defaultMessageFormat(), completeArgs, SimpleMessageFormatter.getInstance(), locale);
	}

	/**
	 * Creates a new builder for constructing ConstraintViolation instances using a
	 * fluent, type-safe API. This builder implements a staged builder pattern to enforce
	 * required properties while making optional properties truly optional.
	 * @return the first stage of the builder which requires setting the name property
	 * @since 0.15.0
	 */
	public static StagedBuilders.Name builder() {
		return new Builder();
	}

	/**
	 * Implementation of the staged builder pattern for creating ConstraintViolation
	 * instances. This builder class implements all the staged interfaces to provide a
	 * fluent and type-safe way to construct ConstraintViolation objects.
	 *
	 * @since 0.15.0
	 */
	public static class Builder implements StagedBuilders.Name, StagedBuilders.MessageKey,
			StagedBuilders.DefaultMessageFormat, StagedBuilders.Optionals {

		/** The field name that the constraint violation applies to */
		private @Nullable String name;

		/** The structured path to the value that the constraint violation applies to */
		private @Nullable List<PathSegment> path;

		/** The separator that joins the name with a containing property or element */
		private String separator = DEFAULT_SEPARATOR;

		/** The message id for internationalization */
		private @Nullable String messageKey;

		/** The default message format if no localized message is found */
		private @Nullable String defaultMessageFormat;

		/** The arguments to be used when formatting the message */
		private Object[] args = new Object[0];

		/** The message formatter to be used for formatting the message */
		private MessageFormatter messageFormatter = SimpleMessageFormatter.getInstance();

		/** The locale to be used for message localization */
		private Locale locale = Locale.getDefault();

		/**
		 * Private constructor to prevent direct instantiation. Use
		 * {@link ConstraintViolation#builder()} instead.
		 */
		private Builder() {
		}

		/**
		 * Sets the name of the field or property that this constraint violation applies
		 * to.
		 * @param name the field or property name
		 * @return the next stage of the builder for method chaining
		 */
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets the message id for internationalization lookup.
		 * @param messageKey the id used to look up localized messages
		 * @return the next stage of the builder for method chaining
		 */
		public Builder messageKey(String messageKey) {
			this.messageKey = messageKey;
			return this;
		}

		/**
		 * Sets the default message format to use when no localized message is found.
		 * @param defaultMessageFormat the default message format string
		 * @return the next stage of the builder for method chaining
		 */
		public Builder defaultMessageFormat(String defaultMessageFormat) {
			this.defaultMessageFormat = defaultMessageFormat;
			return this;
		}

		/**
		 * Sets the arguments to be used when formatting the message.
		 * @param args the arguments to be used in message formatting
		 * @return this builder for method chaining
		 */
		public Builder args(Object... args) {
			this.args = args;
			return this;
		}

		/**
		 * Sets the arguments to be used when formatting the message, automatically
		 * including the name as the first argument.
		 *
		 * This method is more convenient than {@link #args(Object...)} when the name
		 * needs to be the first argument in the message, which is a common pattern for
		 * constraint violations.
		 * @param args the arguments to be used in message formatting (excluding the name)
		 * @return this builder for method chaining
		 */
		public Builder argsWithPrependedName(Object... args) {
			Object[] argsWithName = new Object[args.length + 1];
			argsWithName[0] = Objects.requireNonNull(this.name);
			System.arraycopy(args, 0, argsWithName, 1, args.length);
			this.args = argsWithName;
			return this;
		}

		/**
		 * Sets the arguments to be used when formatting the message, automatically
		 * prepending the name as the first argument and appending the violatedValue as
		 * the last argument.
		 *
		 * This method provides a complete solution for the most common constraint
		 * violation formatting pattern by automatically handling both the name and
		 * violated value positioning.
		 * @param args optional arguments to be used in message formatting (excluding both
		 * name and violated value)
		 * @param violatedValue the value object that violated the constraint, actual
		 * value is retrieved by calling value() method
		 * @return this builder for method chaining
		 */
		// Message arguments may hold a null violated value; the array type does not
		// express element nullness.
		@SuppressWarnings("NullAway")
		public Builder argsWithPrependedNameAndAppendedViolatedValue(Object[] args, ViolatedValue violatedValue) {
			Object[] completeArgs = new Object[args.length + 2];
			completeArgs[0] = this.name;
			System.arraycopy(args, 0, completeArgs, 1, args.length);
			completeArgs[args.length + 1] = violatedValue.value();
			this.args = completeArgs;
			return this;
		}

		/**
		 * Sets the structured path to the value that the constraint violation applies to.
		 * If not specified, the path is a single property segment holding the name.
		 * @param path the structured path
		 * @return this builder for method chaining
		 * @since 0.18.0
		 */
		public Builder path(List<PathSegment> path) {
			this.path = Collections.unmodifiableList(new ArrayList<>(path));
			return this;
		}

		/**
		 * Sets the separator that joins the name with the name of a containing property
		 * or element when the violation is
		 * {@linkplain ConstraintViolation#prefixed(String) prefixed}. If not specified,
		 * {@code .} is used.
		 * @param separator the separator of the names
		 * @return this builder for method chaining
		 * @since 0.18.0
		 */
		public Builder separator(String separator) {
			this.separator = separator;
			return this;
		}

		/**
		 * Sets the message formatter to be used for message formatting. If not specified,
		 * a default {@link SimpleMessageFormatter} will be used.
		 * @param messageFormatter the message formatter to use
		 * @return this builder for method chaining
		 */
		public Builder messageFormatter(MessageFormatter messageFormatter) {
			this.messageFormatter = messageFormatter;
			return this;
		}

		/**
		 * Sets the locale to be used for message localization. If not specified, the
		 * system default locale will be used.
		 * @param locale the locale to use for message localization
		 * @return this builder for method chaining
		 */
		public Builder locale(Locale locale) {
			this.locale = locale;
			return this;
		}

		/**
		 * Builds a new {@link ConstraintViolation} instance with the configured
		 * properties.
		 * @return a new {@link ConstraintViolation} instance
		 */
		public ConstraintViolation build() {
			return new ConstraintViolation(Objects.requireNonNull(this.name), this.path, this.separator,
					Objects.requireNonNull(this.messageKey), Objects.requireNonNull(this.defaultMessageFormat),
					this.args, this.messageFormatter, this.locale);
		}

	}

	/**
	 * Container interface for the staged builder interfaces used in the
	 * ConstraintViolation's builder pattern.
	 *
	 * <p>
	 * This interface hierarchy enables a type-safe builder pattern that enforces required
	 * properties to be set in a specific order before optional properties. The pattern
	 * ensures that a ConstraintViolation cannot be built without first setting all
	 * required properties.
	 *
	 * <p>
	 * The staged building process follows this sequence:
	 * <ol>
	 * <li>First, set the name property ({@link Name})</li>
	 * <li>Then, set the message id property ({@link MessageKey})</li>
	 * <li>Next, set the default message format property
	 * ({@link DefaultMessageFormat})</li>
	 * <li>Finally, optionally set any additional properties and build
	 * ({@link Optionals})</li>
	 * </ol>
	 *
	 * <p>
	 * This design prevents common errors by making it impossible to build an invalid
	 * ConstraintViolation instance.
	 *
	 * @since 0.15.0
	 * @see Builder
	 */
	public interface StagedBuilders {

		/**
		 * First stage of the builder which requires setting the name property.
		 *
		 * <p>
		 * This is the entry point for the staged builder pattern. The name property
		 * specifies which field or property the constraint violation applies to.
		 *
		 * @since 0.15.0
		 */
		interface Name {

			/**
			 * Sets the name of the field or property that this constraint violation
			 * applies to.
			 * @param name the field or property name
			 * @return the next stage of the builder which requires setting the message id
			 */
			MessageKey name(String name);

		}

		/**
		 * Second stage of the builder which requires setting the message id property.
		 *
		 * <p>
		 * This interface represents the second stage in the staged builder pattern after
		 * the name has been set. The message id is used for internationalization lookup
		 * to find the appropriate localized message template.
		 *
		 * @since 0.15.0
		 */
		interface MessageKey {

			/**
			 * The default message id to use when no specific id is needed. This constant
			 * is used as a fallback when internationalization is not required.
			 */
			String DEFAULT_MESSAGE_KEY = "_";

			/**
			 * Sets the message id for internationalization lookup.
			 *
			 * <p>
			 * The message id is used to look up localized messages in resource bundles.
			 * @param messageKey the id used to look up localized messages
			 * @return the next stage of the builder which requires setting the default
			 * message format
			 */
			DefaultMessageFormat messageKey(String messageKey);

			/**
			 * Convenient shortcut builder method that creates a complete constraint
			 * violation in a single call. This method automatically:
			 * <ul>
			 * <li>Sets the message id to the default value
			 * ({@link #DEFAULT_MESSAGE_KEY})</li>
			 * <li>Uses the provided message as the default message format</li>
			 * <li>Prepends the field name as the first argument in the argument list</li>
			 * <li>Builds and returns the final ConstraintViolation object</li>
			 * </ul>
			 *
			 * <p>
			 * This method is particularly useful for simple constraint violations where
			 * complex message formatting or internationalization is not required.
			 * @param message the message text to be used as the default message format
			 * @return a fully constructed {@link ConstraintViolation} instance with the
			 * specified message
			 * @since 0.15.0
			 */
			default ConstraintViolation message(String message) {
				return this.messageKey(DEFAULT_MESSAGE_KEY)
					.defaultMessageFormat(message)
					.argsWithPrependedName()
					.build();
			}

		}

		/**
		 * Third stage of the builder which requires setting the default message format
		 * property.
		 *
		 * <p>
		 * This interface represents the third stage in the staged builder pattern after
		 * the message id has been set. The default message format is the fallback message
		 * template to use when no localized message is found for the message id.
		 *
		 * @since 0.15.0
		 */
		interface DefaultMessageFormat {

			/**
			 * Sets the default message format to use when no localized message is found.
			 *
			 * <p>
			 * This format string serves as a fallback when no localized message can be
			 * found for the specified message id. It may contain placeholders that will
			 * be replaced with values from the args array during message formatting.
			 * @param defaultMessageFormat the default message format string
			 * @return the final stage of the builder where all remaining properties are
			 * optional
			 */
			Optionals defaultMessageFormat(String defaultMessageFormat);

		}

		/**
		 * Final stage of the builder where all remaining properties are optional. The
		 * build() method can be called at any point from this stage.
		 *
		 * <p>
		 * This interface represents the final stage in the staged builder pattern after
		 * all required properties have been set. At this point, the builder can be used
		 * to set optional properties like arguments, message formatter, and locale, or it
		 * can directly build the ConstraintViolation instance.
		 *
		 * @since 0.15.0
		 */
		interface Optionals {

			/**
			 * Sets the arguments to be used when formatting the message.
			 *
			 * <p>
			 * These arguments will be used to replace placeholders in the message format
			 * string. The exact replacement behavior depends on the message formatter
			 * being used.
			 * @param args the arguments to be used in message formatting
			 * @return this builder for method chaining
			 */
			Optionals args(Object... args);

			/**
			 * Sets the arguments to be used when formatting the message, automatically
			 * including the name as the first argument.
			 *
			 * <p>
			 * This method is more convenient than {@link #args(Object...)} when the name
			 * needs to be the first argument in the message, which is a common pattern
			 * for constraint violations. It automatically prepends the field or property
			 * name to the beginning of the arguments array.
			 *
			 * <p>
			 * For example, if the name is "username" and the args are ["must be", 5,
			 * "characters"], the resulting arguments will be ["username", "must be", 5,
			 * "characters"].
			 * @param args the arguments to be used in message formatting (excluding the
			 * name)
			 * @return this builder for method chaining
			 */
			Optionals argsWithPrependedName(Object... args);

			/**
			 * Sets the arguments to be used when formatting the message, automatically
			 * prepending the name as the first argument and appending the violatedValue
			 * as the last argument.
			 *
			 * <p>
			 * This method provides a complete solution for the most common constraint
			 * violation formatting pattern by automatically handling both the name and
			 * violated value positioning in the arguments array.
			 *
			 * <p>
			 * For example, if the name is "username", the args are ["must be between", 3,
			 * "and", 20], and the violated value is "a", the resulting arguments will be
			 * ["username", "must be between", 3, "and", 20, "a"].
			 *
			 * <p>
			 * This is particularly useful for formatting validation messages that need to
			 * reference both the field name and the invalid value that was provided.
			 * @param args optional arguments to be used in message formatting (excluding
			 * both name and violated value)
			 * @param violatedValue the value object that violated the constraint, actual
			 * value is retrieved by calling value() method
			 * @return this builder for method chaining
			 */
			Optionals argsWithPrependedNameAndAppendedViolatedValue(Object[] args, ViolatedValue violatedValue);

			/**
			 * Sets the structured path to the value that the constraint violation applies
			 * to.
			 *
			 * <p>
			 * The name stays the flat representation used in messages; the path is the
			 * machine-readable counterpart exposed by {@link ConstraintViolation#path()}.
			 * If not specified, the path is a single property segment holding the name,
			 * or an empty path if the name is empty.
			 * @param path the structured path
			 * @return this builder for method chaining
			 * @since 0.18.0
			 */
			Optionals path(List<PathSegment> path);

			/**
			 * Sets the separator that joins the name with the name of a containing
			 * property or element when the violation is
			 * {@linkplain ConstraintViolation#prefixed(String) prefixed}. A validator
			 * sets its message key separator, so that a prefixed name is joined
			 * uniformly. If not specified, {@code .} is used.
			 * @param separator the separator of the names
			 * @return this builder for method chaining
			 * @since 0.18.0
			 */
			Optionals separator(String separator);

			/**
			 * Sets the message formatter to be used for message formatting.
			 *
			 * <p>
			 * The message formatter is responsible for formatting the constraint
			 * violation message by using the message id to look up localized messages and
			 * applying the arguments to the message template.
			 *
			 * <p>
			 * If not specified, a default {@link SimpleMessageFormatter} will be used.
			 * @param messageFormatter the message formatter to use for formatting the
			 * message
			 * @return this builder for method chaining
			 */
			Optionals messageFormatter(MessageFormatter messageFormatter);

			/**
			 * Sets the locale to be used for message localization.
			 *
			 * <p>
			 * The locale affects how messages are looked up and formatted, especially for
			 * internationalized applications that support multiple languages.
			 *
			 * <p>
			 * If not specified, the system default locale will be used.
			 * @param locale the locale to use for message localization
			 * @return this builder for method chaining
			 */
			Optionals locale(Locale locale);

			/**
			 * Builds a new {@link ConstraintViolation} instance with the configured
			 * properties.
			 *
			 * <p>
			 * This method finalizes the building process and creates a new immutable
			 * ConstraintViolation instance with all the properties that have been set on
			 * the builder. Any properties that weren't explicitly set will use
			 * appropriate default values.
			 * @return a new immutable {@link ConstraintViolation} instance
			 */
			ConstraintViolation build();

		}

	}

}
