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
package am.ik.yavi.core;

import java.util.function.BiConsumer;

/**
 * <code>BiValidator</code> is a wrapper class of <code>Validator</code> that takes target
 * object to validate and arbitrary errors object. <br>
 * The result of validation is contained in the errors object instead of returning it.
 * This class is useful for a library to adapt both YAVI and other validation library such
 * as Spring Framework's <code>org.springframework.validation.Validator</code>.
 *
 * @param <T> the type of the instance to validate
 * @param <E> the type of the errors object
 * @author Toshiaki Maki
 * @since 0.5.0
 */
public class BiValidator<T, E> implements BiConsumer<T, E> {
	private final Validator<T> validator;

	private final ErrorHandler<E> errorHandler;

	public BiValidator(Validator<T> validator, ErrorHandler<E> errorHandler) {
		this.validator = validator;
		this.errorHandler = errorHandler;
	}

	public void accept(T target, E errors) {
		final ConstraintViolations violations = this.validator.validate(target);
		violations.apply((name, messageKey, args, defaultMessage) -> this.errorHandler
				.handleError(errors, name, messageKey, args, defaultMessage));
	}

	@FunctionalInterface
	public interface ErrorHandler<E> {
		void handleError(E errors, String name, String messageKey, Object[] args,
				String defaultMessage);
	}
}
