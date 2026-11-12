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
import java.util.function.Function;

/**
 * Represents a single constraint violation that occurs during validation.
 *
 * <p>
 * A violation carries the property name, the message id, the default message format, the
 * message arguments, the message formatter and the locale. The message arguments start
 * with the property name and end with the value that violated the constraint.
 *
 * <p>
 * Instances are immutable in these properties, except that {@link #args()} returns the
 * internal array, which should not be modified. Use {@link #builder()} to create
 * instances in a type-safe manner with required and optional properties clearly
 * distinguished.
 *
 * @since 0.1.0
 */
public class ConstraintViolation {

	/**
	 * Arguments to be used when formatting the constraint violation message. The first
	 * element is the property name and the last element is the value that violated the
	 * constraint.
	 */
	private final Object[] args;

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
	 * Creates a new constraint violation.
	 *
	 * <p>
	 * The {@link #path()} of the violation is a single property segment holding the name.
	 * @param name the field or property name that this constraint violation applies to
	 * @param messageKey the id used to look up localized messages
	 * @param defaultMessageFormat the default message format to use when no localized
	 * message is found
	 * @param args the arguments to be used when formatting the message
	 * @param messageFormatter the message formatter to be used; {@code null} selects
	 * {@link SimpleMessageFormatter#getInstance()}
	 * @param locale the locale to be used for message localization; {@code null} selects
	 * the default locale
	 * @deprecated Use {@link #builder()} instead for a more fluent and type-safe API
	 */
	@Deprecated
	public ConstraintViolation(String name, String messageKey, String defaultMessageFormat, Object @Nullable [] args,
			@Nullable MessageFormatter messageFormatter, @Nullable Locale locale) {
		this(name, null, messageKey, defaultMessageFormat, args, messageFormatter, locale);
	}

	private ConstraintViolation(String name, @Nullable List<PathSegment> path, String messageKey,
			String defaultMessageFormat, Object @Nullable [] args, @Nullable MessageFormatter messageFormatter,
			@Nullable Locale locale) {
		this.name = name;
		this.path = path == null ? PathSegment.of(name) : path;
		this.messageKey = messageKey;
		this.defaultMessageFormat = defaultMessageFormat;
		if (args != null) {
			this.args = args;
		}
		else {
			this.args = new Object[0];
		}
		this.messageFormatter = messageFormatter == null ? SimpleMessageFormatter.getInstance() : messageFormatter;
		this.locale = locale == null ? Locale.getDefault() : locale;
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
	 * Returns structured information about this constraint violation.
	 *
	 * <p>
	 * The returned detail contains the message id and the arguments of this violation
	 * together with its formatted message.
	 * @return a new {@link ViolationDetail} representing this constraint violation
	 */
	public ViolationDetail detail() {
		return new ViolationDetail(this.messageKey, this.args, this.message());
	}

	/**
	 * Returns structured information about this constraint violation, using the provided
	 * message formatter to format the message.
	 *
	 * <p>
	 * All other properties (message id and arguments) remain the same as in this
	 * violation.
	 * @param messageFormatter the message formatter to use for formatting the message
	 * @return a new {@link ViolationDetail} representing this constraint violation
	 * @since 0.16.0
	 */
	public ViolationDetail detail(MessageFormatter messageFormatter) {
		return new ViolationDetail(this.messageKey, this.args, this.message(messageFormatter));
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
	 * @return the formatted message describing the constraint violation
	 */
	public String message() {
		return this.messageFormatter.format(this.messageKey, this.defaultMessageFormat, this.args, this.locale);
	}

	/**
	 * Returns the formatted, localized message for this constraint violation using the
	 * provided message formatter instead of the one configured at creation time.
	 *
	 * <p>
	 * All other properties (message id, default message format, arguments and locale)
	 * remain the same as in this violation.
	 *
	 * <pre>{@code
	 * ConstraintViolation violation = ...;
	 * String message = violation.message(new SimpleMessageFormatter());
	 * }</pre>
	 * @param messageFormatter the message formatter to use for formatting the message
	 * @return the formatted message using the provided formatter
	 * @since 0.16.0
	 */
	public String message(MessageFormatter messageFormatter) {
		return messageFormatter.format(this.messageKey, this.defaultMessageFormat, this.args, this.locale);
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
	 * kept if the name does not change.
	 * @param rename the function to transform the current name
	 * @return a new ConstraintViolation with the renamed name
	 * @since 0.7.0
	 */
	public ConstraintViolation rename(Function<? super String, String> rename) {
		final String newName = rename.apply(name);
		return this.relocate(newName, newName.equals(this.name) ? this.path : PathSegment.of(newName));
	}

	/**
	 * Returns a new ConstraintViolation located under the property with the given name. A
	 * property segment is prepended to the {@link #path()} and the name is prefixed
	 * accordingly: {@code street} becomes {@code address.street}, {@code [0]} becomes
	 * {@code address[0]} and an empty name becomes {@code address}.
	 *
	 * <p>
	 * The flat name is joined with {@code .}, the default message key separator. An empty
	 * property name leaves the violation unchanged.
	 * @param name the name of the property that contains the violated value
	 * @return a new ConstraintViolation under the given property
	 * @since 0.18.0
	 */
	public ConstraintViolation prefixed(String name) {
		if (name.isEmpty()) {
			return this;
		}
		final boolean joined = this.name.isEmpty()
				|| (!this.path.isEmpty() && this.path.get(0) instanceof PathSegment.Index);
		return this.relocate(joined ? name + this.name : name + "." + this.name,
				PathSegment.concat(PathSegment.of(name), this.path));
	}

	/**
	 * Returns a new ConstraintViolation located under the element at the given index. An
	 * index segment is prepended to the {@link #path()} and the name is prefixed
	 * accordingly: {@code street} becomes {@code [0].street} and an empty name becomes
	 * {@code [0]}.
	 * @param index the index of the element that contains the violated value
	 * @return a new ConstraintViolation under the given element
	 * @since 0.18.0
	 */
	public ConstraintViolation prefixed(int index) {
		final boolean joined = this.name.isEmpty()
				|| (!this.path.isEmpty() && this.path.get(0) instanceof PathSegment.Index);
		final String prefix = "[" + index + "]";
		return this.relocate(joined ? prefix + this.name : prefix + "." + this.name,
				PathSegment.concat(List.of(new PathSegment.Index(index)), this.path));
	}

	/**
	 * Returns a new ConstraintViolation with the given name and path. If the arguments
	 * array is not empty, the first element will be updated with the new name.
	 * @param newName the new flat name
	 * @param newPath the new structured path
	 * @return a new ConstraintViolation with the given name and path
	 */
	ConstraintViolation relocate(String newName, List<PathSegment> newPath) {
		final Object[] newArgs = this.args().clone();
		if (newArgs.length > 0) {
			newArgs[0] = newName;
		}
		return new ConstraintViolation(newName, newPath, this.messageKey, this.defaultMessageFormat, newArgs,
				this.messageFormatter, this.locale);
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

		/** The message id for internationalization */
		private @Nullable String messageKey;

		/** The default message format if no localized message is found */
		private @Nullable String defaultMessageFormat;

		/** The arguments to be used when formatting the message */
		private Object @Nullable [] args;

		/** The message formatter to be used for formatting the message */
		private @Nullable MessageFormatter messageFormatter;

		/** The locale to be used for message localization */
		private @Nullable Locale locale;

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
			return new ConstraintViolation(Objects.requireNonNull(this.name), this.path,
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
