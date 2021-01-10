/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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

import java.util.function.Function;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.BiValidator;
import am.ik.yavi.core.BiValidator.ErrorHandler;
import am.ik.yavi.jsr305.Nullable;
import am.ik.yavi.message.MessageFormatter;

/**
 * A factory class of <code>BiValidator</code>. It can be used to manage the common
 * configurations of <code>BiValidator</code> in IoC container etc.
 *
 * @param <E> the type of the errors object
 * @author Toshiaki Maki
 * @since 0.5.0
 */
public class BiValidatorFactory<E> extends ValidatorFactorySupport {
	private final BiValidator.ErrorHandler<E> errorHandler;

	public BiValidatorFactory(@Nullable String messageKeySeparator,
			@Nullable MessageFormatter messageFormatter,
			@Nullable ErrorHandler<E> errorHandler) {
		super(messageKeySeparator, messageFormatter);
		this.errorHandler = errorHandler;
	}

	public BiValidatorFactory(@Nullable ErrorHandler<E> errorHandler) {
		this(null, null, errorHandler);
	}

	public <T> BiValidator<T, E> validator(
			Function<ValidatorBuilder<T>, ValidatorBuilder<T>> constraints) {
		if (this.errorHandler == null) {
			throw new IllegalArgumentException("'errorHandler' must not be null.");
		}
		final ValidatorBuilder<T> builder = super.initBuilder();
		return constraints.apply(builder).build(this.errorHandler);
	}
}
