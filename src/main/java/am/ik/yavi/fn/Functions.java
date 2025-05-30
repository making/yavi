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
public class Functions {

	public static <T1, R> Function1<T1, R> curry(Function1<T1, R> f) {
		return t1 -> f.apply(t1);
	}

	public static <T1, T2, R> Function1<T1, Function1<T2, R>> curry(Function2<T1, T2, R> f) {
		return t1 -> t2 -> f.apply(t1, t2);
	}

	public static <T1, T2, T3, R> Function1<T1, Function1<T2, Function1<T3, R>>> curry(Function3<T1, T2, T3, R> f) {
		return t1 -> t2 -> t3 -> f.apply(t1, t2, t3);
	}

	public static <T1, T2, T3, T4, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, R>>>> curry(
			Function4<T1, T2, T3, T4, R> f) {
		return t1 -> t2 -> t3 -> t4 -> f.apply(t1, t2, t3, t4);
	}

	public static <T1, T2, T3, T4, T5, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, R>>>>> curry(
			Function5<T1, T2, T3, T4, T5, R> f) {
		return t1 -> t2 -> t3 -> t4 -> t5 -> f.apply(t1, t2, t3, t4, t5);
	}

	public static <T1, T2, T3, T4, T5, T6, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, R>>>>>> curry(
			Function6<T1, T2, T3, T4, T5, T6, R> f) {
		return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> f.apply(t1, t2, t3, t4, t5, t6);
	}

	public static <T1, T2, T3, T4, T5, T6, T7, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, R>>>>>>> curry(
			Function7<T1, T2, T3, T4, T5, T6, T7, R> f) {
		return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> f.apply(t1, t2, t3, t4, t5, t6, t7);
	}

	public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, Function1<T8, R>>>>>>>> curry(
			Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> f) {
		return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> f.apply(t1, t2, t3, t4, t5, t6, t7, t8);
	}

	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, Function1<T8, Function1<T9, R>>>>>>>>> curry(
			Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> f) {
		return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> t9 -> f.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
	}

	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, Function1<T8, Function1<T9, Function1<T10, R>>>>>>>>>> curry(
			Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> f) {
		return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> t9 -> t10 -> f.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9,
				t10);
	}

	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, Function1<T8, Function1<T9, Function1<T10, Function1<T11, R>>>>>>>>>>> curry(
			Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> f) {
		return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> t9 -> t10 -> t11 -> f.apply(t1, t2, t3, t4, t5, t6, t7,
				t8, t9, t10, t11);
	}

	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, Function1<T8, Function1<T9, Function1<T10, Function1<T11, Function1<T12, R>>>>>>>>>>>> curry(
			Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> f) {
		return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> t9 -> t10 -> t11 -> t12 -> f.apply(t1, t2, t3, t4, t5,
				t6, t7, t8, t9, t10, t11, t12);
	}

	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, Function1<T8, Function1<T9, Function1<T10, Function1<T11, Function1<T12, Function1<T13, R>>>>>>>>>>>>> curry(
			Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> f) {
		return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> t9 -> t10 -> t11 -> t12 -> t13 -> f.apply(t1, t2, t3, t4,
				t5, t6, t7, t8, t9, t10, t11, t12, t13);
	}

	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, Function1<T8, Function1<T9, Function1<T10, Function1<T11, Function1<T12, Function1<T13, Function1<T14, R>>>>>>>>>>>>>> curry(
			Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> f) {
		return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> t9 -> t10 -> t11 -> t12 -> t13 -> t14 -> f.apply(t1, t2,
				t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14);
	}

	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, Function1<T8, Function1<T9, Function1<T10, Function1<T11, Function1<T12, Function1<T13, Function1<T14, Function1<T15, R>>>>>>>>>>>>>>> curry(
			Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> f) {
		return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> t9 -> t10 -> t11 -> t12 -> t13 -> t14 -> t15 -> f
			.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
	}

	public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, Function1<T8, Function1<T9, Function1<T10, Function1<T11, Function1<T12, Function1<T13, Function1<T14, Function1<T15, Function1<T16, R>>>>>>>>>>>>>>>> curry(
			Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> f) {
		return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> t9 -> t10 -> t11 -> t12 -> t13 -> t14 -> t15 -> t16 -> f
			.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16);
	}

}
