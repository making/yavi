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
public class Combining2<E, T1, T2> {

	protected final Validation<E, T1> v1;

	protected final Validation<E, T2> v2;

	public Combining2(Validation<E, T1> v1, Validation<E, T2> v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	public <R, V extends Validation<E, R>> V apply(Function2<T1, T2, R> f) {
		final Validation<E, Function1<T2, R>> apply1 = v1.apply(Validation.success(Functions.curry(f)));
		return v2.apply(apply1);
	}

	public <T3> Combining3<E, T1, T2, T3> combine(Validation<E, T3> v3) {
		return new Combining3<>(v1, v2, v3);
	}

}
