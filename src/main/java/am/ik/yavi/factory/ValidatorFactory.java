/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.factory;

import java.util.function.Function;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import am.ik.yavi.jsr305.Nullable;
import am.ik.yavi.message.MessageFormatter;

/**
 * Factory for {@link Validator} instances that share a message configuration.
 *
 * <p>
 * The factory keeps the message key separator and the {@link MessageFormatter} used for
 * every validator it creates. This is useful to define that configuration once, for
 * example as a bean in an IoC container:
 *
 * <pre>{@code
 * &#64;Bean
 * public ValidatorFactory validatorFactory(MessageSource messageSource) {
 *     MessageFormatter messageFormatter = new MessageSourceMessageFormatter(messageSource::getMessage);
 *     return new ValidatorFactory(null, messageFormatter);
 * }
 * }</pre>
 *
 * <p>
 * A component can then create a validator without repeating the configuration:
 *
 * <pre>{@code
 * &#64;RestController
 * public class OrderController {
 *     private final Validator<Order> validator;
 *
 *     public OrderController(ValidatorFactory factory) {
 *         this.validator = factory.validator(builder -> builder
 *             .constraint(Order::getCustomerId, "customerId", c -> c.notBlank()));
 *     }
 * }
 * }</pre>
 *
 * <p>
 * The created validators are immutable and thread-safe.
 *
 * @author Toshiaki Maki
 * @since 0.5.0
 * @see ValidatorBuilder
 */
public class ValidatorFactory extends ValidatorFactorySupport {

	/**
	 * Creates a factory that uses the default message key separator ({@code "."}) and the
	 * default message formatter.
	 */
	public ValidatorFactory() {
		this(null, null);
	}

	/**
	 * Creates a factory with the given message configuration.
	 *
	 * <p>
	 * A {@code null} {@code messageKeySeparator} selects the default separator
	 * ({@code "."}), and a {@code null} {@code messageFormatter} selects the default
	 * formatter that uses the built-in message formats without localization.
	 * @param messageKeySeparator the separator between nested property names in message
	 * keys; {@code null} for the default
	 * @param messageFormatter the formatter used to create violation messages;
	 * {@code null} for the default
	 */
	public ValidatorFactory(@Nullable String messageKeySeparator, @Nullable MessageFormatter messageFormatter) {
		super(messageKeySeparator, messageFormatter);
	}

	/**
	 * Creates a validator from the constraints configured by the given function.
	 *
	 * <p>
	 * The function receives a new {@link ValidatorBuilder} initialized with this
	 * factory's message configuration. The returned validator is immutable and can be
	 * reused. Constraints treat {@code null} as valid unless they explicitly reject it,
	 * for example with {@code notNull()}.
	 * @param <T> the type of the object to validate
	 * @param constraints function that adds constraints to the builder; it must return
	 * the given builder or another non-{@code null} builder
	 * @return a new validator
	 * @throws NullPointerException if {@code constraints} is {@code null} or returns
	 * {@code null}
	 */
	public <T> Validator<T> validator(Function<ValidatorBuilder<T>, ValidatorBuilder<T>> constraints) {
		final ValidatorBuilder<T> builder = super.initBuilder();
		return constraints.apply(builder).build();
	}

}
