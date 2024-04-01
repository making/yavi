/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.factory;

import java.util.function.BiConsumer;
import java.util.function.Function;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ErrorHandler;
import am.ik.yavi.core.Validator;
import am.ik.yavi.jsr305.Nullable;
import am.ik.yavi.message.MessageFormatter;

/**
 * A factory class of <code>BiConsumer</code>. It can be used to manage the common
 * configurations of <code>BiConsumer</code> in IoC container etc.<br>
 * In case of Spring Framework, you can define <code>BiConsumerFactory</code> as follows:
 *
 * <pre>
 * {@literal @Bean}
 * public BiConsumerFactory&lt;Errors&gt; biConsumerFactory(MessageSource messageSource) {
 *   MessageFormatter messageFormatter = new MessageSourceMessageFormatter(messageSource::getMessage);
 *   return new BiConsumerFactory&lt;&gt;(null, messageFormatter, Errors::rejectValues);
 * }
 * </pre>
 *
 * A component can create a validator like following:
 *
 * <pre>
 * {@literal @RestController}
 * public class OrderController {
 *     private final BiConsumer&lt;CartItem, Errors&gt; validator;
 *     public OrderController(BiConsumerFactory&lt;Errors&gt; factory) {
 *         this.validator = factory.validator(builder -&gt; builder.constraint(...));
 *     }
 * }
 * </pre>
 *
 * @param <E> the type of the errors object
 * @author Toshiaki Maki
 * @since 0.13.0
 */
public class BiConsumerFactory<E> extends ValidatorFactorySupport {
	private final ErrorHandler<E> errorHandler;

	public BiConsumerFactory(@Nullable String messageKeySeparator,
			@Nullable MessageFormatter messageFormatter,
			@Nullable ErrorHandler<E> errorHandler) {
		super(messageKeySeparator, messageFormatter);
		this.errorHandler = errorHandler;
	}

	public BiConsumerFactory(@Nullable ErrorHandler<E> errorHandler) {
		this(null, null, errorHandler);
	}

	public <T> BiConsumer<T, E> validator(
			Function<ValidatorBuilder<T>, ValidatorBuilder<T>> constraints) {
		if (this.errorHandler == null) {
			throw new IllegalArgumentException("'errorHandler' must not be null.");
		}
		final ValidatorBuilder<T> builder = super.initBuilder();
		final Validator<T> validator = constraints.apply(builder).build();
		return validator.toBiConsumer(this.errorHandler);
	}
}
