/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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

/**
 * @since 0.3.0
 */
public final class Arguments {
	public static <A1> Arguments1<A1> of(A1 arg1) {
		return new Arguments1<>(arg1);
	}

	public static <A1, A2> Arguments2<A1, A2> of(A1 arg1, A2 arg2) {
		return new Arguments2<>(arg1, arg2);
	}

	public static <A1, A2, A3> Arguments3<A1, A2, A3> of(A1 arg1, A2 arg2, A3 arg3) {
		return new Arguments3<>(arg1, arg2, arg3);
	}

	public static <A1, A2, A3, A4> Arguments4<A1, A2, A3, A4> of(A1 arg1, A2 arg2,
			A3 arg3, A4 arg4) {
		return new Arguments4<>(arg1, arg2, arg3, arg4);
	}

	public static <A1, A2, A3, A4, A5> Arguments5<A1, A2, A3, A4, A5> of(A1 arg1, A2 arg2,
			A3 arg3, A4 arg4, A5 arg5) {
		return new Arguments5<>(arg1, arg2, arg3, arg4, arg5);
	}

	public static <A1, A2, A3, A4, A5, A6> Arguments6<A1, A2, A3, A4, A5, A6> of(A1 arg1,
			A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6) {
		return new Arguments6<>(arg1, arg2, arg3, arg4, arg5, arg6);
	}

	public static <A1, A2, A3, A4, A5, A6, A7> Arguments7<A1, A2, A3, A4, A5, A6, A7> of(
			A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7) {
		return new Arguments7<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8> Arguments8<A1, A2, A3, A4, A5, A6, A7, A8> of(
			A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8) {
		return new Arguments8<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9> Arguments9<A1, A2, A3, A4, A5, A6, A7, A8, A9> of(
			A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8,
			A9 arg9) {
		return new Arguments9<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10> Arguments10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10> of(
			A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8,
			A9 arg9, A10 arg10) {
		return new Arguments10<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
				arg10);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11> Arguments11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11> of(
			A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8,
			A9 arg9, A10 arg10, A11 arg11) {
		return new Arguments11<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
				arg10, arg11);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12> Arguments12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12> of(
			A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8,
			A9 arg9, A10 arg10, A11 arg11, A12 arg12) {
		return new Arguments12<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
				arg10, arg11, arg12);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13> Arguments13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13> of(
			A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8,
			A9 arg9, A10 arg10, A11 arg11, A12 arg12, A13 arg13) {
		return new Arguments13<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
				arg10, arg11, arg12, arg13);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14> Arguments14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14> of(
			A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8,
			A9 arg9, A10 arg10, A11 arg11, A12 arg12, A13 arg13, A14 arg14) {
		return new Arguments14<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
				arg10, arg11, arg12, arg13, arg14);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15> Arguments15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15> of(
			A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8,
			A9 arg9, A10 arg10, A11 arg11, A12 arg12, A13 arg13, A14 arg14, A15 arg15) {
		return new Arguments15<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
				arg10, arg11, arg12, arg13, arg14, arg15);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16> Arguments16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16> of(
			A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8,
			A9 arg9, A10 arg10, A11 arg11, A12 arg12, A13 arg13, A14 arg14, A15 arg15,
			A16 arg16) {
		return new Arguments16<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
				arg10, arg11, arg12, arg13, arg14, arg15, arg16);
	}
}
