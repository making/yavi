/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.function.Function.identity;

/**
 * Generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-applicative.sh
 *
 * @since 0.6.0
 */
public class Validations {
	public static <E, T1> Combining1<E, T1> combine(Validation<E, T1> v1) {
		return new Combining1<>(v1);
	}

	public static <E, T1, T2> Combining2<E, T1, T2> combine(Validation<E, T1> v1,
			Validation<E, T2> v2) {
		return new Combining2<>(v1, v2);
	}

	public static <E, T1, T2, T3> Combining3<E, T1, T2, T3> combine(Validation<E, T1> v1,
			Validation<E, T2> v2, Validation<E, T3> v3) {
		return new Combining3<>(v1, v2, v3);
	}

	public static <E, T1, T2, T3, T4> Combining4<E, T1, T2, T3, T4> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4) {
		return new Combining4<>(v1, v2, v3, v4);
	}

	public static <E, T1, T2, T3, T4, T5> Combining5<E, T1, T2, T3, T4, T5> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5) {
		return new Combining5<>(v1, v2, v3, v4, v5);
	}

	public static <E, T1, T2, T3, T4, T5, T6> Combining6<E, T1, T2, T3, T4, T5, T6> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6) {
		return new Combining6<>(v1, v2, v3, v4, v5, v6);
	}

	public static <E, T1, T2, T3, T4, T5, T6, T7> Combining7<E, T1, T2, T3, T4, T5, T6, T7> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7) {
		return new Combining7<>(v1, v2, v3, v4, v5, v6, v7);
	}

	public static <E, T1, T2, T3, T4, T5, T6, T7, T8> Combining8<E, T1, T2, T3, T4, T5, T6, T7, T8> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8) {
		return new Combining8<>(v1, v2, v3, v4, v5, v6, v7, v8);
	}

	public static <E, T1, T2, T3, T4, T5, T6, T7, T8, T9> Combining9<E, T1, T2, T3, T4, T5, T6, T7, T8, T9> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9) {
		return new Combining9<>(v1, v2, v3, v4, v5, v6, v7, v8, v9);
	}

	public static <E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Combining10<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10) {
		return new Combining10<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
	}

	public static <E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Combining11<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10, Validation<E, T11> v11) {
		return new Combining11<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11);
	}

	public static <E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Combining12<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10, Validation<E, T11> v11, Validation<E, T12> v12) {
		return new Combining12<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12);
	}

	public static <E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Combining13<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10, Validation<E, T11> v11, Validation<E, T12> v12,
			Validation<E, T13> v13) {
		return new Combining13<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
	}

	public static <E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Combining14<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10, Validation<E, T11> v11, Validation<E, T12> v12,
			Validation<E, T13> v13, Validation<E, T14> v14) {
		return new Combining14<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13,
				v14);
	}

	public static <E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Combining15<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10, Validation<E, T11> v11, Validation<E, T12> v12,
			Validation<E, T13> v13, Validation<E, T14> v14, Validation<E, T15> v15) {
		return new Combining15<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13,
				v14, v15);
	}

	public static <E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Combining16<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> combine(
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10, Validation<E, T11> v11, Validation<E, T12> v12,
			Validation<E, T13> v13, Validation<E, T14> v14, Validation<E, T15> v15,
			Validation<E, T16> v16) {
		return new Combining16<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13,
				v14, v15, v16);
	}

	public static <R, E, T1, V extends Validation<E, R>> V apply(Function1<T1, R> f,
			Validation<E, T1> v1) {
		return combine(v1).apply(f);
	}

	public static <R, E, T1, T2, V extends Validation<E, R>> V apply(
			Function2<T1, T2, R> f, Validation<E, T1> v1, Validation<E, T2> v2) {
		return combine(v1, v2).apply(f);
	}

	public static <R, E, T1, T2, T3, V extends Validation<E, R>> V apply(
			Function3<T1, T2, T3, R> f, Validation<E, T1> v1, Validation<E, T2> v2,
			Validation<E, T3> v3) {
		return combine(v1, v2, v3).apply(f);
	}

	public static <R, E, T1, T2, T3, T4, V extends Validation<E, R>> V apply(
			Function4<T1, T2, T3, T4, R> f, Validation<E, T1> v1, Validation<E, T2> v2,
			Validation<E, T3> v3, Validation<E, T4> v4) {
		return combine(v1, v2, v3, v4).apply(f);
	}

	public static <R, E, T1, T2, T3, T4, T5, V extends Validation<E, R>> V apply(
			Function5<T1, T2, T3, T4, T5, R> f, Validation<E, T1> v1,
			Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4,
			Validation<E, T5> v5) {
		return combine(v1, v2, v3, v4, v5).apply(f);
	}

	public static <R, E, T1, T2, T3, T4, T5, T6, V extends Validation<E, R>> V apply(
			Function6<T1, T2, T3, T4, T5, T6, R> f, Validation<E, T1> v1,
			Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4,
			Validation<E, T5> v5, Validation<E, T6> v6) {
		return combine(v1, v2, v3, v4, v5, v6).apply(f);
	}

	public static <R, E, T1, T2, T3, T4, T5, T6, T7, V extends Validation<E, R>> V apply(
			Function7<T1, T2, T3, T4, T5, T6, T7, R> f, Validation<E, T1> v1,
			Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4,
			Validation<E, T5> v5, Validation<E, T6> v6, Validation<E, T7> v7) {
		return combine(v1, v2, v3, v4, v5, v6, v7).apply(f);
	}

	public static <R, E, T1, T2, T3, T4, T5, T6, T7, T8, V extends Validation<E, R>> V apply(
			Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> f, Validation<E, T1> v1,
			Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4,
			Validation<E, T5> v5, Validation<E, T6> v6, Validation<E, T7> v7,
			Validation<E, T8> v8) {
		return combine(v1, v2, v3, v4, v5, v6, v7, v8).apply(f);
	}

	public static <R, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, V extends Validation<E, R>> V apply(
			Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> f, Validation<E, T1> v1,
			Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4,
			Validation<E, T5> v5, Validation<E, T6> v6, Validation<E, T7> v7,
			Validation<E, T8> v8, Validation<E, T9> v9) {
		return combine(v1, v2, v3, v4, v5, v6, v7, v8, v9).apply(f);
	}

	public static <R, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, V extends Validation<E, R>> V apply(
			Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> f,
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10) {
		return combine(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10).apply(f);
	}

	public static <R, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, V extends Validation<E, R>> V apply(
			Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> f,
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10, Validation<E, T11> v11) {
		return combine(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11).apply(f);
	}

	public static <R, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, V extends Validation<E, R>> V apply(
			Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> f,
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10, Validation<E, T11> v11, Validation<E, T12> v12) {
		return combine(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12).apply(f);
	}

	public static <R, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, V extends Validation<E, R>> V apply(
			Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> f,
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10, Validation<E, T11> v11, Validation<E, T12> v12,
			Validation<E, T13> v13) {
		return combine(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13).apply(f);
	}

	public static <R, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, V extends Validation<E, R>> V apply(
			Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> f,
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10, Validation<E, T11> v11, Validation<E, T12> v12,
			Validation<E, T13> v13, Validation<E, T14> v14) {
		return combine(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14)
				.apply(f);
	}

	public static <R, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, V extends Validation<E, R>> V apply(
			Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> f,
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10, Validation<E, T11> v11, Validation<E, T12> v12,
			Validation<E, T13> v13, Validation<E, T14> v14, Validation<E, T15> v15) {
		return combine(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15)
				.apply(f);
	}

	public static <R, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, V extends Validation<E, R>> V apply(
			Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> f,
			Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3,
			Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6,
			Validation<E, T7> v7, Validation<E, T8> v8, Validation<E, T9> v9,
			Validation<E, T10> v10, Validation<E, T11> v11, Validation<E, T12> v12,
			Validation<E, T13> v13, Validation<E, T14> v14, Validation<E, T15> v15,
			Validation<E, T16> v16) {
		return combine(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15,
				v16).apply(f);
	}

	public static <E, T> Validation<E, List<T>> sequence(
			Iterable<? extends Validation<? extends E, ? extends T>> validations) {
		return traverse(validations, identity());
	}

	public static <E, T, U> Validation<E, List<U>> traverse(Iterable<T> values,
			Function<? super T, ? extends Validation<? extends E, ? extends U>> mapper) {
		return traverseIndexed(values, IndexedTraverser.ignoreIndex(mapper));
	}

	/**
	 * @since 0.7.0
	 */
	@FunctionalInterface
	public interface IndexedTraverser<A, R> {
		R apply(A a, int index);

		static <A, R> IndexedTraverser<A, R> ignoreIndex(
				Function<? super A, ? extends R> f) {
			return (a, index) -> f.apply(a);
		}
	}

	/**
	 * @since 0.7.0
	 */
	public static <E, T, U> Validation<E, List<U>> traverseIndexed(Iterable<T> values,
			IndexedTraverser<? super T, ? extends Validation<? extends E, ? extends U>> traverser) {
		return traverseIndexed(values, traverser, ArrayList::new);
	}

	/**
	 * @since 0.8.0
	 */
	public static <E, T, U, C extends Collection<U>> Validation<E, C> traverseIndexed(
			Iterable<T> values,
			IndexedTraverser<? super T, ? extends Validation<? extends E, ? extends U>> traverser,
			Supplier<C> factory) {
		final List<E> errors = new ArrayList<>();
		final C results = factory.get();
		int index = 0;
		for (T value : values) {
			traverser.apply(value, index++).fold(errors::addAll, results::add);
		}
		return errors.isEmpty() ? Validation.success(results)
				: Validation.failure(errors);
	}

	/**
	 * @since 0.8.0
	 */
	@SuppressWarnings("unchecked")
	public static <E, T, U> Validation<E, Optional<U>> traverseOptional(Optional<T> value,
			Function<? super T, ? extends Validation<? extends E, ? extends U>> mapper) {
		return value.map(
				t -> mapper.apply(t).bimap(es -> (List<E>) es, u -> Optional.of((U) u))) //
				.orElse(Validation.success(Optional.empty()));
	}

}
