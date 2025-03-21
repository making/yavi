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

import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

/**
 * @since 0.14.0
 */
public class EnumValidator<E extends Enum<E>, T> extends DefaultArguments1Validator<E, T> {

	@Override
	public <T2> EnumValidator<E, T2> andThen(Function<? super T, ? extends T2> mapper) {
		return new EnumValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public EnumValidator(Validator<Arguments1<E>> validator, Function1<? super E, ? extends T> mapper) {
		super(validator, mapper);
	}

}
