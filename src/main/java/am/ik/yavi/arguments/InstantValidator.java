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

import java.time.Instant;
import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

/**
 * @since 0.10.0
 */
public class InstantValidator<T> extends DefaultArguments1Validator<Instant, T> {

	@Override
	public <T2> InstantValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new InstantValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public InstantValidator(Validator<Arguments1<Instant>> validator, Function1<? super Instant, ? extends T> mapper) {
		super(validator, mapper);
	}

}
