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

public class ConstraintViolation {

	private final Object[] args;

	private final String defaultMessageFormat;

	private final Locale locale;

	private final MessageFormatter messageFormatter;

	private final String messageKey;

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

	public Object[] args() {
		return this.args;
	}

	public String defaultMessageFormat() {
		return this.defaultMessageFormat;
	}

	public ViolationDetail detail() {
		return new ViolationDetail(this.messageKey, this.args, this.message());
	}

	public Locale locale() {
		return this.locale;
	}

	public String message() {
		return this.messageFormatter.format(this.messageKey, this.defaultMessageFormat, this.args, this.locale);
	}

	public String messageKey() {
		return this.messageKey;
	}

	public String name() {
		return this.name;
	}

	@Override
	public String toString() {
		return "ConstraintViolation{" + "name='" + name + '\'' + ", messageKey='" + messageKey + '\''
				+ ", defaultMessageFormat='" + defaultMessageFormat + '\'' + ", args=" + Arrays.toString(args) + '}';
	}

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
	 * Container interface for the staged builder interfaces. This interface hierarchy
	 * enables a type-safe builder pattern that enforces required properties to be set in
	 * a specific order before optional properties.
	 *
	 * @since 0.15.0
	 */
	public interface StagedBuilders {

		/**
		 * First stage of the builder which requires setting the name property.
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
		 */
		interface MessageKey {

			String DEFAULT_MESSAGE_KEY = "_";

			/**
			 * Sets the message key for internationalization lookup.
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
		 */
		interface DefaultMessageFormat {

			/**
			 * Sets the default message format to use when no localized message is found.
			 * @param defaultMessageFormat the default message format string
			 * @return the final stage of the builder where all remaining properties are
			 * optional
			 */
			Optionals defaultMessageFormat(String defaultMessageFormat);

		}

		/**
		 * Final stage of the builder where all remaining properties are optional. The
		 * build() method can be called at any point from this stage.
		 */
		interface Optionals {

			/**
			 * Sets the arguments to be used when formatting the message.
			 * @param args the arguments to be used in message formatting
			 * @return this builder for method chaining
			 */
			Optionals args(Object... args);

			/**
			 * Sets the arguments to be used when formatting the message, automatically
			 * including the name as the first argument.
			 * 
			 * This method is more convenient than {@link #args(Object...)} when the name
			 * needs to be the first argument in the message, which is a common pattern
			 * for constraint violations.
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
			 * This method provides a complete solution for the most common constraint
			 * violation formatting pattern by automatically handling both the name and
			 * violated value positioning.
			 * @param args optional arguments to be used in message formatting (excluding
			 * both name and violated value)
			 * @param violatedValue the value object that violated the constraint, actual
			 * value is retrieved by calling value() method
			 * @return this builder for method chaining
			 */
			Optionals argsWithPrependedNameAndAppendedViolatedValue(Object[] args, ViolatedValue violatedValue);

			/**
			 * Sets the message formatter to be used for message formatting. If not
			 * prepending the name as the first argument and appending the violatedValue
			 * as the last argument.
			 * 
			 * This method provides a complete solution for the most common constraint
			 * violation formatting pattern by automatically handling both the name and
			 * violated value positioning.
			 * @return this builder for method chaining
			 */
			Optionals messageFormatter(MessageFormatter messageFormatter);

			/**
			 * Sets the locale to be used for message localization. If not specified, the
			 * system default locale will be used.
			 * @param locale the locale to use for message localization
			 * @return this builder for method chaining
			 */
			Optionals locale(Locale locale);

			/**
			 * Builds a new {@link ConstraintViolation} instance with the configured
			 * properties.
			 * @return a new {@link ConstraintViolation} instance
			 */
			ConstraintViolation build();

		}

	}

}
