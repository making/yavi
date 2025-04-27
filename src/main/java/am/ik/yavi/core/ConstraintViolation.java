/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import am.ik.yavi.jsr305.Nullable;
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

/**
 * Represents a single constraint violation that occurs during validation.
 *
 * <p>
 * This class encapsulates details about a validation failure, including:
 * <ul>
 * <li>The field or property name that failed validation</li>
 * <li>The message key used for internationalization</li>
 * <li>The default message format to use when no localized message is found</li>
 * <li>Any arguments needed for message formatting</li>
 * <li>The message formatter and locale to use for message formatting</li>
 * </ul>
 *
 * <p>
 * ConstraintViolation objects are immutable after creation. The class provides a builder
 * API for creating instances in a type-safe manner with required and optional properties
 * clearly distinguished.
 *
 * @since 0.1.0
 */
public class ConstraintViolation {

	/**
	 * Arguments to be used when formatting the constraint violation message. The last
	 * element in this array typically contains the value that violated the constraint.
	 */
	private final Object[] args;

	/**
	 * The default message format to use when no localized message is found for the
	 * message key. This format string may contain placeholders that will be replaced with
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
	 * The key used to look up localized messages in resource bundles. This is the primary
	 * identifier for retrieving the proper message format.
	 */
	private final String messageKey;

	/**
	 * The field or property name that this constraint violation applies to. This
	 * identifies which part of the validated object failed validation.
	 */
	private final String name;

	/**
	 * Creates a new constraint violation.
	 * @param name the field or property name that this constraint violation applies to
	 * @param messageKey the key used to look up localized messages
	 * @param defaultMessageFormat the default message format to use when no localized
	 * message is found
	 * @param args the arguments to be used when formatting the message
	 * @param messageFormatter the message formatter to be used
	 * @param locale the locale to be used for message localization
	 * @deprecated Use {@link #builder()} instead for a more fluent and type-safe API
	 */
	@Deprecated
	public ConstraintViolation(String name, String messageKey, String defaultMessageFormat, @Nullable Object[] args,
			@Nullable MessageFormatter messageFormatter, @Nullable Locale locale) {
		this.name = name;
		this.messageKey = messageKey;
		this.defaultMessageFormat = defaultMessageFormat;
		this.args = args == null ? new Object[0] : args;
		this.messageFormatter = messageFormatter == null ? SimpleMessageFormatter.getInstance() : messageFormatter;
		this.locale = locale == null ? Locale.getDefault() : locale;
	}

	/**
	 * Returns the arguments used for message formatting.
	 * @return an array of objects that were provided as formatting arguments
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
	 * Creates and returns a ViolationDetail object that contains structured information
	 * about this constraint violation.
	 *
	 * <p>
	 * The detail object contains the message key, arguments, and formatted message,
	 * providing a more structured representation of the violation.
	 * @return a new ViolationDetail instance representing this constraint violation
	 */
	public ViolationDetail detail() {
		return new ViolationDetail(this.messageKey, this.args, this.message());
	}

	/**
	 * Creates and returns a ViolationDetail object that contains structured information
	 * about this constraint violation, using the provided message formatter.
	 *
	 * <p>
	 * This method allows changing the message formatter at runtime, affecting how the
	 * message in the violation detail is formatted. All other properties (message key,
	 * arguments) remain the same as in the original constraint violation.
	 * @param messageFormatter the message formatter to use for formatting the message
	 * @return a new ViolationDetail instance representing this constraint violation
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
	 * message key, default message format, arguments, and locale.
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
	 * This method allows changing the message formatter at runtime, which is useful when
	 * different formatting strategies need to be applied to the same constraint
	 * violation. All other properties (message key, default message format, arguments,
	 * and locale) remain the same as in the original constraint violation.
	 *
	 * <p>
	 * Example usage: <pre>{@code
	 * // Using a custom message formatter for specific formatting needs
	 * ConstraintViolation violation = // ... obtain violation
	 * CustomMessageFormatter formatter = new CustomMessageFormatter();
	 * String formattedMessage = violation.message(formatter);
	 * }</pre>
	 * @param messageFormatter the message formatter to use for formatting the message
	 * @return the formatted message using the provided formatter
	 * @since 0.16.0
	 */
	public String message(MessageFormatter messageFormatter) {
		return messageFormatter.format(this.messageKey, this.defaultMessageFormat, this.args, this.locale);
	}

	/**
	 * Returns the message key used for looking up localized messages.
	 * @return the message key for internationalization
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
	 * Returns a string representation of this constraint violation.
	 *
	 * <p>
	 * The string representation includes the name, message key, default message format,
	 * and arguments.
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
	 * @param rename the function to transform the current name
	 * @return a new ConstraintViolation with the renamed name
	 * @since 0.7.0
	 */
	public ConstraintViolation rename(Function<? super String, String> rename) {
		final String newName = rename.apply(name);
		final Object[] newArgs = this.args().clone();
		if (newArgs.length > 0) {
			newArgs[0] = newName;
		}
		return new ConstraintViolation(newName, this.messageKey, this.defaultMessageFormat, newArgs,
				this.messageFormatter, this.locale);
	}

	/**
	 * Returns a new ConstraintViolation with the name indexed by appending "[index]" to
	 * the current name.
	 * @param index the index to append to the name
	 * @return a new ConstraintViolation with the indexed name
	 * @since 0.7.0
	 */
	public ConstraintViolation indexed(int index) {
		return this.rename(name -> name + "[" + index + "]");
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
		private String name;

		/** The message key for internationalization */
		private String messageKey;

		/** The default message format if no localized message is found */
		private String defaultMessageFormat;

		/** The arguments to be used when formatting the message */
		private Object[] args;

		/** The message formatter to be used for formatting the message */
		private MessageFormatter messageFormatter;

		/** The locale to be used for message localization */
		private Locale locale;

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
		 * Sets the message key for internationalization lookup.
		 * @param messageKey the key used to look up localized messages
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
			argsWithName[0] = this.name;
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
		public Builder argsWithPrependedNameAndAppendedViolatedValue(Object[] args, ViolatedValue violatedValue) {
			Object[] completeArgs = new Object[args.length + 2];
			completeArgs[0] = this.name;
			System.arraycopy(args, 0, completeArgs, 1, args.length);
			completeArgs[args.length + 1] = violatedValue.value();
			this.args = completeArgs;
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
			return new ConstraintViolation(name, messageKey, defaultMessageFormat, args, messageFormatter, locale);
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
	 * <li>Then, set the message key property ({@link MessageKey})</li>
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
		 */
		interface Name {

			/**
			 * Sets the name of the field or property that this constraint violation
			 * applies to.
			 * @param name the field or property name
			 * @return the next stage of the builder which requires setting the message
			 * key
			 */
			MessageKey name(String name);

		}

		/**
		 * Second stage of the builder which requires setting the message key property.
		 *
		 * <p>
		 * This interface represents the second stage in the staged builder pattern after
		 * the name has been set. The message key is used for internationalization lookup
		 * to find the appropriate localized message template.
		 */
		interface MessageKey {

			/**
			 * The default message key to use when no specific key is needed. This
			 * constant is used as a fallback when internationalization is not required.
			 */
			String DEFAULT_MESSAGE_KEY = "_";

			/**
			 * Sets the message key for internationalization lookup.
			 *
			 * <p>
			 * The message key is used to look up localized messages in resource bundles.
			 * @param messageKey the key used to look up localized messages
			 * @return the next stage of the builder which requires setting the default
			 * message format
			 */
			DefaultMessageFormat messageKey(String messageKey);

			/**
			 * Convenient shortcut builder method that creates a complete constraint
			 * violation in a single call. This method automatically:
			 * <ul>
			 * <li>Sets the message key to the default value
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
		 * the message key has been set. The default message format is the fallback
		 * message template to use when no localized message is found for the message key.
		 */
		interface DefaultMessageFormat {

			/**
			 * Sets the default message format to use when no localized message is found.
			 *
			 * <p>
			 * This format string serves as a fallback when no localized message can be
			 * found for the specified message key. It may contain placeholders that will
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
			 * Sets the message formatter to be used for message formatting.
			 *
			 * <p>
			 * The message formatter is responsible for formatting the constraint
			 * violation message by using the message key to look up localized messages
			 * and applying the arguments to the message template.
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
