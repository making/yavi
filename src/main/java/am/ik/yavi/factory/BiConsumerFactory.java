/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.factory;

import java.util.function.BiConsumer;
import java.util.function.Function;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ErrorHandler;
import am.ik.yavi.core.Validator;
import org.jspecify.annotations.Nullable;
import am.ik.yavi.message.MessageFormatter;

/**
 * Factory for {@link BiConsumer} validators that share a message configuration and an
 * {@link ErrorHandler}.
 *
 * <p>
 * The returned {@code BiConsumer} validates its first argument and passes every
 * constraint violation to the error handler with the second argument. This is useful when
 * violations must be recorded in an application specific error object, for example during
 * request binding.
 *
 * <p>
 * The factory keeps the message key separator, the {@link MessageFormatter} and the error
 * handler used for every validator it creates. This is useful to define that
 * configuration once, for example as a bean in an IoC container:
 *
 * <pre>{@code
 * &#64;Bean
 * public BiConsumerFactory<Errors> biConsumerFactory(MessageSource messageSource) {
 *     MessageFormatter messageFormatter = new MessageSourceMessageFormatter(messageSource::getMessage);
 *     return new BiConsumerFactory<>(null, messageFormatter, Errors::rejectValues);
 * }
 * }</pre>
 *
 * <p>
 * A component can then create a validator without repeating the configuration:
 *
 * <pre>{@code
 * &#64;RestController
 * public class OrderController {
 *     private final BiConsumer<Order, Errors> validator;
 *
 *     public OrderController(BiConsumerFactory<Errors> factory) {
 *         this.validator = factory.validator(builder -> builder
 *             .constraint(Order::getCustomerId, "customerId", c -> c.notBlank()));
 *     }
 * }
 * }</pre>
 *
 * @param <E> the type of the errors object
 * @author Toshiaki Maki
 * @since 0.13.0
 * @see Validator#toBiConsumer(ErrorHandler)
 */
public class BiConsumerFactory<E> extends ValidatorFactorySupport {

	private final @Nullable ErrorHandler<E> errorHandler;

	/**
	 * Creates a factory with the given message configuration and error handler.
	 *
	 * <p>
	 * A {@code null} {@code messageKeySeparator} selects the default separator
	 * ({@code "."}), and a {@code null} {@code messageFormatter} selects the default
	 * formatter that uses the built-in message formats without localization.
	 * @param messageKeySeparator the separator between nested property names in message
	 * keys; {@code null} for the default
	 * @param messageFormatter the formatter used to create violation messages;
	 * {@code null} for the default
	 * @param errorHandler the handler that receives the violations; may be {@code null},
	 * but then {@link #validator(Function)} throws {@link IllegalArgumentException}
	 */
	public BiConsumerFactory(@Nullable String messageKeySeparator, @Nullable MessageFormatter messageFormatter,
			@Nullable ErrorHandler<E> errorHandler) {
		super(messageKeySeparator, messageFormatter);
		this.errorHandler = errorHandler;
	}

	/**
	 * Creates a factory with the given error handler and the default message
	 * configuration.
	 *
	 * <p>
	 * The factory uses the default message key separator ({@code "."}) and the default
	 * message formatter.
	 * @param errorHandler the handler that receives the violations; may be {@code null},
	 * but then {@link #validator(Function)} throws {@link IllegalArgumentException}
	 */
	public BiConsumerFactory(@Nullable ErrorHandler<E> errorHandler) {
		this(null, null, errorHandler);
	}

	/**
	 * Creates a {@code BiConsumer} validator from the constraints configured by the given
	 * function.
	 *
	 * <p>
	 * The function receives a new {@link ValidatorBuilder} initialized with this
	 * factory's message configuration. When the returned {@code BiConsumer} is accepted,
	 * it validates the target and passes each violation to the error handler; nothing is
	 * passed when the target is valid. No exception is thrown for invalid input.
	 * @param <T> the type of the object to validate
	 * @param constraints function that adds constraints to the builder; it must return
	 * the given builder or another non-{@code null} builder
	 * @return a new {@code BiConsumer} validator
	 * @throws IllegalArgumentException if this factory was created without an error
	 * handler
	 * @throws NullPointerException if {@code constraints} is {@code null} or returns
	 * {@code null}
	 */
	public <T> BiConsumer<T, E> validator(Function<ValidatorBuilder<T>, ValidatorBuilder<T>> constraints) {
		if (this.errorHandler == null) {
			throw new IllegalArgumentException("'errorHandler' must not be null.");
		}
		final ValidatorBuilder<T> builder = super.initBuilder();
		final Validator<T> validator = constraints.apply(builder).build();
		return validator.toBiConsumer(this.errorHandler);
	}

}
