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
package am.ik.yavi.fn;

/**
 * Generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-applicative.sh
 *
 * @since 0.6.0
 */
public class Combining6<E, T1, T2, T3, T4, T5, T6> {

	protected final Validation<E, T1> v1;

	protected final Validation<E, T2> v2;

	protected final Validation<E, T3> v3;

	protected final Validation<E, T4> v4;

	protected final Validation<E, T5> v5;

	protected final Validation<E, T6> v6;

	public Combining6(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4,
			Validation<E, T5> v5, Validation<E, T6> v6) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;
		this.v5 = v5;
		this.v6 = v6;
	}

	public <R, V extends Validation<E, R>> V apply(Function6<T1, T2, T3, T4, T5, T6, R> f) {
		final Validation<E, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, R>>>>>> apply1 = v1
			.apply(Validation.success(Functions.curry(f)));
		final Validation<E, Function1<T3, Function1<T4, Function1<T5, Function1<T6, R>>>>> apply2 = v2.apply(apply1);
		final Validation<E, Function1<T4, Function1<T5, Function1<T6, R>>>> apply3 = v3.apply(apply2);
		final Validation<E, Function1<T5, Function1<T6, R>>> apply4 = v4.apply(apply3);
		final Validation<E, Function1<T6, R>> apply5 = v5.apply(apply4);
		return v6.apply(apply5);
	}

	public <T7> Combining7<E, T1, T2, T3, T4, T5, T6, T7> combine(Validation<E, T7> v7) {
		return new Combining7<>(v1, v2, v3, v4, v5, v6, v7);
	}

}
