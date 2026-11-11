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
 * A factory class of <code>Validator</code>. It can be used to manage the common
 * configurations of <code>Validator</code> in IoC container etc.<br>
 *
 * In case of Spring Framework, you can define <code>BiValidatorFactory</code> as follows:
 *
 * <pre>
 *{@literal @Bean}
 * public ValidatorFactory validatorFactory(MessageSource messageSource) {
 *   MessageFormatter messageFormatter = new MessageSourceMessageFormatter(messageSource::getMessage);
 *   return new ValidatorFactory(null, messageFormatter);
 * }
 * </pre>
 *
 * A component can create a validator like following:
 *
 * <pre>
 *{@literal @RestController}
 * public class OrderController {
 *     private final Validator&lt;CartItem&gt; validator;
 *
 *     public OrderController(ValidatorFactory factory) {
 *         this.validator = factory.validator(builder -&gt; builder.constraint(...));
 *     }
 * }
 * </pre>
 *
 * @author Toshiaki Maki
 * @since 0.5.0
 */
public class ValidatorFactory extends ValidatorFactorySupport {

	public ValidatorFactory() {
		this(null, null);
	}

	public ValidatorFactory(@Nullable String messageKeySeparator, @Nullable MessageFormatter messageFormatter) {
		super(messageKeySeparator, messageFormatter);
	}

	public <T> Validator<T> validator(Function<ValidatorBuilder<T>, ValidatorBuilder<T>> constraints) {
		final ValidatorBuilder<T> builder = super.initBuilder();
		return constraints.apply(builder).build();
	}

}
