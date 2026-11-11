/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import am.ik.yavi.jsr305.Nullable;

/**
 * Factory for {@link Arguments1} to {@link Arguments16} containers.
 *
 * <p>
 * The overloads create an immutable container from the given arguments. Arguments may be
 * {@code null}; the containers only store, compare and map them and perform no
 * validation. Use an {@link Arguments1Validator} (or the corresponding
 * {@code ArgumentsNValidator}) to validate the arguments.
 *
 * @since 0.3.0
 */
public final class Arguments {

	/**
	 * Creates an instance. Use the static {@link #of} factory methods instead.
	 */
	public Arguments() {
	}

	/**
	 * Returns a container of 1 argument.
	 * @param <A1> the type of the argument at position 1
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @return an Arguments1 instance holding the given argument
	 */
	public static <A1> Arguments1<A1> of(@Nullable A1 arg1) {
		return new Arguments1<>(arg1);
	}

	/**
	 * Returns a container of 2 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @return an Arguments2 instance holding the given arguments
	 */
	public static <A1, A2> Arguments2<A1, A2> of(@Nullable A1 arg1, @Nullable A2 arg2) {
		return new Arguments2<>(arg1, arg2);
	}

	/**
	 * Returns a container of 3 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @return an Arguments3 instance holding the given arguments
	 */
	public static <A1, A2, A3> Arguments3<A1, A2, A3> of(@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3) {
		return new Arguments3<>(arg1, arg2, arg3);
	}

	/**
	 * Returns a container of 4 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @return an Arguments4 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4> Arguments4<A1, A2, A3, A4> of(@Nullable A1 arg1, @Nullable A2 arg2,
			@Nullable A3 arg3, @Nullable A4 arg4) {
		return new Arguments4<>(arg1, arg2, arg3, arg4);
	}

	/**
	 * Returns a container of 5 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param <A5> the type of the argument at position 5
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @param arg5 the argument at position 5, possibly {@code null}
	 * @return an Arguments5 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4, A5> Arguments5<A1, A2, A3, A4, A5> of(@Nullable A1 arg1, @Nullable A2 arg2,
			@Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5) {
		return new Arguments5<>(arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 * Returns a container of 6 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param <A5> the type of the argument at position 5
	 * @param <A6> the type of the argument at position 6
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @param arg5 the argument at position 5, possibly {@code null}
	 * @param arg6 the argument at position 6, possibly {@code null}
	 * @return an Arguments6 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4, A5, A6> Arguments6<A1, A2, A3, A4, A5, A6> of(@Nullable A1 arg1, @Nullable A2 arg2,
			@Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5, @Nullable A6 arg6) {
		return new Arguments6<>(arg1, arg2, arg3, arg4, arg5, arg6);
	}

	/**
	 * Returns a container of 7 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param <A5> the type of the argument at position 5
	 * @param <A6> the type of the argument at position 6
	 * @param <A7> the type of the argument at position 7
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @param arg5 the argument at position 5, possibly {@code null}
	 * @param arg6 the argument at position 6, possibly {@code null}
	 * @param arg7 the argument at position 7, possibly {@code null}
	 * @return an Arguments7 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4, A5, A6, A7> Arguments7<A1, A2, A3, A4, A5, A6, A7> of(@Nullable A1 arg1,
			@Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5, @Nullable A6 arg6,
			@Nullable A7 arg7) {
		return new Arguments7<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7);
	}

	/**
	 * Returns a container of 8 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param <A5> the type of the argument at position 5
	 * @param <A6> the type of the argument at position 6
	 * @param <A7> the type of the argument at position 7
	 * @param <A8> the type of the argument at position 8
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @param arg5 the argument at position 5, possibly {@code null}
	 * @param arg6 the argument at position 6, possibly {@code null}
	 * @param arg7 the argument at position 7, possibly {@code null}
	 * @param arg8 the argument at position 8, possibly {@code null}
	 * @return an Arguments8 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4, A5, A6, A7, A8> Arguments8<A1, A2, A3, A4, A5, A6, A7, A8> of(@Nullable A1 arg1,
			@Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5, @Nullable A6 arg6,
			@Nullable A7 arg7, @Nullable A8 arg8) {
		return new Arguments8<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
	}

	/**
	 * Returns a container of 9 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param <A5> the type of the argument at position 5
	 * @param <A6> the type of the argument at position 6
	 * @param <A7> the type of the argument at position 7
	 * @param <A8> the type of the argument at position 8
	 * @param <A9> the type of the argument at position 9
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @param arg5 the argument at position 5, possibly {@code null}
	 * @param arg6 the argument at position 6, possibly {@code null}
	 * @param arg7 the argument at position 7, possibly {@code null}
	 * @param arg8 the argument at position 8, possibly {@code null}
	 * @param arg9 the argument at position 9, possibly {@code null}
	 * @return an Arguments9 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9> Arguments9<A1, A2, A3, A4, A5, A6, A7, A8, A9> of(
			@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5,
			@Nullable A6 arg6, @Nullable A7 arg7, @Nullable A8 arg8, @Nullable A9 arg9) {
		return new Arguments9<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
	}

	/**
	 * Returns a container of 10 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param <A5> the type of the argument at position 5
	 * @param <A6> the type of the argument at position 6
	 * @param <A7> the type of the argument at position 7
	 * @param <A8> the type of the argument at position 8
	 * @param <A9> the type of the argument at position 9
	 * @param <A10> the type of the argument at position 10
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @param arg5 the argument at position 5, possibly {@code null}
	 * @param arg6 the argument at position 6, possibly {@code null}
	 * @param arg7 the argument at position 7, possibly {@code null}
	 * @param arg8 the argument at position 8, possibly {@code null}
	 * @param arg9 the argument at position 9, possibly {@code null}
	 * @param arg10 the argument at position 10, possibly {@code null}
	 * @return an Arguments10 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10> Arguments10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10> of(
			@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5,
			@Nullable A6 arg6, @Nullable A7 arg7, @Nullable A8 arg8, @Nullable A9 arg9, @Nullable A10 arg10) {
		return new Arguments10<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
	}

	/**
	 * Returns a container of 11 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param <A5> the type of the argument at position 5
	 * @param <A6> the type of the argument at position 6
	 * @param <A7> the type of the argument at position 7
	 * @param <A8> the type of the argument at position 8
	 * @param <A9> the type of the argument at position 9
	 * @param <A10> the type of the argument at position 10
	 * @param <A11> the type of the argument at position 11
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @param arg5 the argument at position 5, possibly {@code null}
	 * @param arg6 the argument at position 6, possibly {@code null}
	 * @param arg7 the argument at position 7, possibly {@code null}
	 * @param arg8 the argument at position 8, possibly {@code null}
	 * @param arg9 the argument at position 9, possibly {@code null}
	 * @param arg10 the argument at position 10, possibly {@code null}
	 * @param arg11 the argument at position 11, possibly {@code null}
	 * @return an Arguments11 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11> Arguments11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11> of(
			@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5,
			@Nullable A6 arg6, @Nullable A7 arg7, @Nullable A8 arg8, @Nullable A9 arg9, @Nullable A10 arg10,
			@Nullable A11 arg11) {
		return new Arguments11<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
	}

	/**
	 * Returns a container of 12 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param <A5> the type of the argument at position 5
	 * @param <A6> the type of the argument at position 6
	 * @param <A7> the type of the argument at position 7
	 * @param <A8> the type of the argument at position 8
	 * @param <A9> the type of the argument at position 9
	 * @param <A10> the type of the argument at position 10
	 * @param <A11> the type of the argument at position 11
	 * @param <A12> the type of the argument at position 12
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @param arg5 the argument at position 5, possibly {@code null}
	 * @param arg6 the argument at position 6, possibly {@code null}
	 * @param arg7 the argument at position 7, possibly {@code null}
	 * @param arg8 the argument at position 8, possibly {@code null}
	 * @param arg9 the argument at position 9, possibly {@code null}
	 * @param arg10 the argument at position 10, possibly {@code null}
	 * @param arg11 the argument at position 11, possibly {@code null}
	 * @param arg12 the argument at position 12, possibly {@code null}
	 * @return an Arguments12 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12> Arguments12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12> of(
			@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5,
			@Nullable A6 arg6, @Nullable A7 arg7, @Nullable A8 arg8, @Nullable A9 arg9, @Nullable A10 arg10,
			@Nullable A11 arg11, @Nullable A12 arg12) {
		return new Arguments12<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12);
	}

	/**
	 * Returns a container of 13 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param <A5> the type of the argument at position 5
	 * @param <A6> the type of the argument at position 6
	 * @param <A7> the type of the argument at position 7
	 * @param <A8> the type of the argument at position 8
	 * @param <A9> the type of the argument at position 9
	 * @param <A10> the type of the argument at position 10
	 * @param <A11> the type of the argument at position 11
	 * @param <A12> the type of the argument at position 12
	 * @param <A13> the type of the argument at position 13
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @param arg5 the argument at position 5, possibly {@code null}
	 * @param arg6 the argument at position 6, possibly {@code null}
	 * @param arg7 the argument at position 7, possibly {@code null}
	 * @param arg8 the argument at position 8, possibly {@code null}
	 * @param arg9 the argument at position 9, possibly {@code null}
	 * @param arg10 the argument at position 10, possibly {@code null}
	 * @param arg11 the argument at position 11, possibly {@code null}
	 * @param arg12 the argument at position 12, possibly {@code null}
	 * @param arg13 the argument at position 13, possibly {@code null}
	 * @return an Arguments13 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13> Arguments13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13> of(
			@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5,
			@Nullable A6 arg6, @Nullable A7 arg7, @Nullable A8 arg8, @Nullable A9 arg9, @Nullable A10 arg10,
			@Nullable A11 arg11, @Nullable A12 arg12, @Nullable A13 arg13) {
		return new Arguments13<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13);
	}

	/**
	 * Returns a container of 14 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param <A5> the type of the argument at position 5
	 * @param <A6> the type of the argument at position 6
	 * @param <A7> the type of the argument at position 7
	 * @param <A8> the type of the argument at position 8
	 * @param <A9> the type of the argument at position 9
	 * @param <A10> the type of the argument at position 10
	 * @param <A11> the type of the argument at position 11
	 * @param <A12> the type of the argument at position 12
	 * @param <A13> the type of the argument at position 13
	 * @param <A14> the type of the argument at position 14
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @param arg5 the argument at position 5, possibly {@code null}
	 * @param arg6 the argument at position 6, possibly {@code null}
	 * @param arg7 the argument at position 7, possibly {@code null}
	 * @param arg8 the argument at position 8, possibly {@code null}
	 * @param arg9 the argument at position 9, possibly {@code null}
	 * @param arg10 the argument at position 10, possibly {@code null}
	 * @param arg11 the argument at position 11, possibly {@code null}
	 * @param arg12 the argument at position 12, possibly {@code null}
	 * @param arg13 the argument at position 13, possibly {@code null}
	 * @param arg14 the argument at position 14, possibly {@code null}
	 * @return an Arguments14 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14> Arguments14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14> of(
			@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5,
			@Nullable A6 arg6, @Nullable A7 arg7, @Nullable A8 arg8, @Nullable A9 arg9, @Nullable A10 arg10,
			@Nullable A11 arg11, @Nullable A12 arg12, @Nullable A13 arg13, @Nullable A14 arg14) {
		return new Arguments14<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13,
				arg14);
	}

	/**
	 * Returns a container of 15 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param <A5> the type of the argument at position 5
	 * @param <A6> the type of the argument at position 6
	 * @param <A7> the type of the argument at position 7
	 * @param <A8> the type of the argument at position 8
	 * @param <A9> the type of the argument at position 9
	 * @param <A10> the type of the argument at position 10
	 * @param <A11> the type of the argument at position 11
	 * @param <A12> the type of the argument at position 12
	 * @param <A13> the type of the argument at position 13
	 * @param <A14> the type of the argument at position 14
	 * @param <A15> the type of the argument at position 15
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @param arg5 the argument at position 5, possibly {@code null}
	 * @param arg6 the argument at position 6, possibly {@code null}
	 * @param arg7 the argument at position 7, possibly {@code null}
	 * @param arg8 the argument at position 8, possibly {@code null}
	 * @param arg9 the argument at position 9, possibly {@code null}
	 * @param arg10 the argument at position 10, possibly {@code null}
	 * @param arg11 the argument at position 11, possibly {@code null}
	 * @param arg12 the argument at position 12, possibly {@code null}
	 * @param arg13 the argument at position 13, possibly {@code null}
	 * @param arg14 the argument at position 14, possibly {@code null}
	 * @param arg15 the argument at position 15, possibly {@code null}
	 * @return an Arguments15 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15> Arguments15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15> of(
			@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5,
			@Nullable A6 arg6, @Nullable A7 arg7, @Nullable A8 arg8, @Nullable A9 arg9, @Nullable A10 arg10,
			@Nullable A11 arg11, @Nullable A12 arg12, @Nullable A13 arg13, @Nullable A14 arg14, @Nullable A15 arg15) {
		return new Arguments15<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13,
				arg14, arg15);
	}

	/**
	 * Returns a container of 16 arguments.
	 * @param <A1> the type of the argument at position 1
	 * @param <A2> the type of the argument at position 2
	 * @param <A3> the type of the argument at position 3
	 * @param <A4> the type of the argument at position 4
	 * @param <A5> the type of the argument at position 5
	 * @param <A6> the type of the argument at position 6
	 * @param <A7> the type of the argument at position 7
	 * @param <A8> the type of the argument at position 8
	 * @param <A9> the type of the argument at position 9
	 * @param <A10> the type of the argument at position 10
	 * @param <A11> the type of the argument at position 11
	 * @param <A12> the type of the argument at position 12
	 * @param <A13> the type of the argument at position 13
	 * @param <A14> the type of the argument at position 14
	 * @param <A15> the type of the argument at position 15
	 * @param <A16> the type of the argument at position 16
	 * @param arg1 the argument at position 1, possibly {@code null}
	 * @param arg2 the argument at position 2, possibly {@code null}
	 * @param arg3 the argument at position 3, possibly {@code null}
	 * @param arg4 the argument at position 4, possibly {@code null}
	 * @param arg5 the argument at position 5, possibly {@code null}
	 * @param arg6 the argument at position 6, possibly {@code null}
	 * @param arg7 the argument at position 7, possibly {@code null}
	 * @param arg8 the argument at position 8, possibly {@code null}
	 * @param arg9 the argument at position 9, possibly {@code null}
	 * @param arg10 the argument at position 10, possibly {@code null}
	 * @param arg11 the argument at position 11, possibly {@code null}
	 * @param arg12 the argument at position 12, possibly {@code null}
	 * @param arg13 the argument at position 13, possibly {@code null}
	 * @param arg14 the argument at position 14, possibly {@code null}
	 * @param arg15 the argument at position 15, possibly {@code null}
	 * @param arg16 the argument at position 16, possibly {@code null}
	 * @return an Arguments16 instance holding the given arguments
	 */
	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16> Arguments16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16> of(
			@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5,
			@Nullable A6 arg6, @Nullable A7 arg7, @Nullable A8 arg8, @Nullable A9 arg9, @Nullable A10 arg10,
			@Nullable A11 arg11, @Nullable A12 arg12, @Nullable A13 arg13, @Nullable A14 arg14, @Nullable A15 arg15,
			@Nullable A16 arg16) {
		return new Arguments16<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13,
				arg14, arg15, arg16);
	}

}
