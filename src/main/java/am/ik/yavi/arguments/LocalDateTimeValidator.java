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
package am.ik.yavi.arguments;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * @since 0.10.0
 */
public class LocalDateTimeValidator<T> extends DefaultArguments1Validator<LocalDateTime, T> {

	@Override
	public <T2> LocalDateTimeValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new LocalDateTimeValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public LocalDateTimeValidator(Validator<Arguments1<LocalDateTime>> validator,
			Function1<? super LocalDateTime, ? extends T> mapper) {
		super(validator, mapper);
	}

}
