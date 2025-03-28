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

import java.util.function.BiConsumer;

/**
 * Deprecated in favor of {@link Validatable#toBiConsumer(am.ik.yavi.core.ErrorHandler)}
 * and {@link ValueValidator#toBiConsumer(am.ik.yavi.core.ErrorHandler)}
 *
 * <code>BiValidator</code> is a wrapper class of <code>Validator</code> that takes target
 * object to validate and arbitrary errors object. <br>
 * The result of validation is contained in the errors object instead of returning it.
 * This class is useful for a library to adapt both YAVI and other validation library such
 * as Spring Framework's <code>org.springframework.validation.Validator</code>. Validator
 * can be wrapped as follows:
 *
 * <pre>
 * Validator&lt;CartItem&gt; validator = ValidatorBuilder.&lt;CartItem&gt; of()
 *                        .constraint(CartItem::getQuantity, "quantity", c -&gt; c.greaterThan(0))
 *                        .constraint(...)
 *                        .build();
 * BiValidator&lt;CartItem, Errors&gt; validator = new BiValidator&lt;&gt;(validator, Errors::rejectValue);
 * </pre>
 *
 * @param <T> the type of the instance to validate
 * @param <E> the type of the errors object
 * @author Toshiaki Maki
 * @since 0.5.0
 */
@Deprecated
public class BiValidator<T, E> implements BiConsumer<T, E> {

	private final ValueValidator<T, ?> validator;

	private final ErrorHandler<E> errorHandler;

	/**
	 * Create a {@link BiValidator} from a {@link ValueValidator}
	 * @param validator value validator
	 * @param errorHandler error handler
	 * @since 0.13.0
	 */
	public BiValidator(ValueValidator<T, ?> validator, ErrorHandler<E> errorHandler) {
		this.validator = validator;
		this.errorHandler = errorHandler;
	}

	public BiValidator(Validator<T> validator, ErrorHandler<E> errorHandler) {
		this.validator = validator.applicative();
		this.errorHandler = errorHandler;
	}

	public void accept(T target, E errors) {
		final Validated<?> validated = this.validator.validate(target);
		if (!validated.isValid()) {
			final ConstraintViolations violations = validated.errors();
			violations.apply((name, messageKey, args, defaultMessage) -> this.errorHandler.handleError(errors, name,
					messageKey, args, defaultMessage));
		}
	}

	@FunctionalInterface
	@Deprecated
	/**
	 * Deprecated in favor of {@link am.ik.yavi.core.ErrorHandler}
	 */
	public interface ErrorHandler<E> {

		void handleError(E errors, String name, String messageKey, Object[] args, String defaultMessage);

	}

}
