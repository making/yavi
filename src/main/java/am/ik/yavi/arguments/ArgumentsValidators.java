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
package am.ik.yavi.arguments;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueValidator;

import static java.util.function.Function.identity;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class ArgumentsValidators {

	public static <A1, A2, R1, R2> Arguments2Splitting<A1, A2, R1, R2> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2) {
		return new Arguments2Splitting<>(v1, v2);
	}

	public static <A1, A2, A3, R1, R2, R3> Arguments3Splitting<A1, A2, A3, R1, R2, R3> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3) {
		return new Arguments3Splitting<>(v1, v2, v3);
	}

	public static <A1, A2, A3, A4, R1, R2, R3, R4> Arguments4Splitting<A1, A2, A3, A4, R1, R2, R3, R4> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4) {
		return new Arguments4Splitting<>(v1, v2, v3, v4);
	}

	public static <A1, A2, A3, A4, A5, R1, R2, R3, R4, R5> Arguments5Splitting<A1, A2, A3, A4, A5, R1, R2, R3, R4, R5> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5) {
		return new Arguments5Splitting<>(v1, v2, v3, v4, v5);
	}

	public static <A1, A2, A3, A4, A5, A6, R1, R2, R3, R4, R5, R6> Arguments6Splitting<A1, A2, A3, A4, A5, A6, R1, R2, R3, R4, R5, R6> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6) {
		return new Arguments6Splitting<>(v1, v2, v3, v4, v5, v6);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, R1, R2, R3, R4, R5, R6, R7> Arguments7Splitting<A1, A2, A3, A4, A5, A6, A7, R1, R2, R3, R4, R5, R6, R7> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6,
			ValueValidator<? super A7, ? extends R7> v7) {
		return new Arguments7Splitting<>(v1, v2, v3, v4, v5, v6, v7);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, R1, R2, R3, R4, R5, R6, R7, R8> Arguments8Splitting<A1, A2, A3, A4, A5, A6, A7, A8, R1, R2, R3, R4, R5, R6, R7, R8> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6,
			ValueValidator<? super A7, ? extends R7> v7,
			ValueValidator<? super A8, ? extends R8> v8) {
		return new Arguments8Splitting<>(v1, v2, v3, v4, v5, v6, v7, v8);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, R1, R2, R3, R4, R5, R6, R7, R8, R9> Arguments9Splitting<A1, A2, A3, A4, A5, A6, A7, A8, A9, R1, R2, R3, R4, R5, R6, R7, R8, R9> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6,
			ValueValidator<? super A7, ? extends R7> v7,
			ValueValidator<? super A8, ? extends R8> v8,
			ValueValidator<? super A9, ? extends R9> v9) {
		return new Arguments9Splitting<>(v1, v2, v3, v4, v5, v6, v7, v8, v9);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10> Arguments10Splitting<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6,
			ValueValidator<? super A7, ? extends R7> v7,
			ValueValidator<? super A8, ? extends R8> v8,
			ValueValidator<? super A9, ? extends R9> v9,
			ValueValidator<? super A10, ? extends R10> v10) {
		return new Arguments10Splitting<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11> Arguments11Splitting<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6,
			ValueValidator<? super A7, ? extends R7> v7,
			ValueValidator<? super A8, ? extends R8> v8,
			ValueValidator<? super A9, ? extends R9> v9,
			ValueValidator<? super A10, ? extends R10> v10,
			ValueValidator<? super A11, ? extends R11> v11) {
		return new Arguments11Splitting<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12> Arguments12Splitting<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6,
			ValueValidator<? super A7, ? extends R7> v7,
			ValueValidator<? super A8, ? extends R8> v8,
			ValueValidator<? super A9, ? extends R9> v9,
			ValueValidator<? super A10, ? extends R10> v10,
			ValueValidator<? super A11, ? extends R11> v11,
			ValueValidator<? super A12, ? extends R12> v12) {
		return new Arguments12Splitting<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13> Arguments13Splitting<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6,
			ValueValidator<? super A7, ? extends R7> v7,
			ValueValidator<? super A8, ? extends R8> v8,
			ValueValidator<? super A9, ? extends R9> v9,
			ValueValidator<? super A10, ? extends R10> v10,
			ValueValidator<? super A11, ? extends R11> v11,
			ValueValidator<? super A12, ? extends R12> v12,
			ValueValidator<? super A13, ? extends R13> v13) {
		return new Arguments13Splitting<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v13);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14> Arguments14Splitting<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6,
			ValueValidator<? super A7, ? extends R7> v7,
			ValueValidator<? super A8, ? extends R8> v8,
			ValueValidator<? super A9, ? extends R9> v9,
			ValueValidator<? super A10, ? extends R10> v10,
			ValueValidator<? super A11, ? extends R11> v11,
			ValueValidator<? super A12, ? extends R12> v12,
			ValueValidator<? super A13, ? extends R13> v13,
			ValueValidator<? super A14, ? extends R14> v14) {
		return new Arguments14Splitting<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v13, v14);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15> Arguments15Splitting<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6,
			ValueValidator<? super A7, ? extends R7> v7,
			ValueValidator<? super A8, ? extends R8> v8,
			ValueValidator<? super A9, ? extends R9> v9,
			ValueValidator<? super A10, ? extends R10> v10,
			ValueValidator<? super A11, ? extends R11> v11,
			ValueValidator<? super A12, ? extends R12> v12,
			ValueValidator<? super A13, ? extends R13> v13,
			ValueValidator<? super A14, ? extends R14> v14,
			ValueValidator<? super A15, ? extends R15> v15) {
		return new Arguments15Splitting<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v13, v14, v15);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, R16> Arguments16Splitting<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, R16> split(
			ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6,
			ValueValidator<? super A7, ? extends R7> v7,
			ValueValidator<? super A8, ? extends R8> v8,
			ValueValidator<? super A9, ? extends R9> v9,
			ValueValidator<? super A10, ? extends R10> v10,
			ValueValidator<? super A11, ? extends R11> v11,
			ValueValidator<? super A12, ? extends R12> v12,
			ValueValidator<? super A13, ? extends R13> v13,
			ValueValidator<? super A14, ? extends R14> v14,
			ValueValidator<? super A15, ? extends R15> v15,
			ValueValidator<? super A16, ? extends R16> v16) {
		return new Arguments16Splitting<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v13, v14, v15, v16);
	}

	public static <A, R1, R2> Arguments2Combining<A, R1, R2> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2) {
		return new Arguments2Combining<>(v1, v2);
	}

	public static <A, R1, R2, R3> Arguments3Combining<A, R1, R2, R3> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3) {
		return new Arguments3Combining<>(v1, v2, v3);
	}

	public static <A, R1, R2, R3, R4> Arguments4Combining<A, R1, R2, R3, R4> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4) {
		return new Arguments4Combining<>(v1, v2, v3, v4);
	}

	public static <A, R1, R2, R3, R4, R5> Arguments5Combining<A, R1, R2, R3, R4, R5> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5) {
		return new Arguments5Combining<>(v1, v2, v3, v4, v5);
	}

	public static <A, R1, R2, R3, R4, R5, R6> Arguments6Combining<A, R1, R2, R3, R4, R5, R6> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5,
			ValueValidator<? super A, ? extends R6> v6) {
		return new Arguments6Combining<>(v1, v2, v3, v4, v5, v6);
	}

	public static <A, R1, R2, R3, R4, R5, R6, R7> Arguments7Combining<A, R1, R2, R3, R4, R5, R6, R7> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5,
			ValueValidator<? super A, ? extends R6> v6,
			ValueValidator<? super A, ? extends R7> v7) {
		return new Arguments7Combining<>(v1, v2, v3, v4, v5, v6, v7);
	}

	public static <A, R1, R2, R3, R4, R5, R6, R7, R8> Arguments8Combining<A, R1, R2, R3, R4, R5, R6, R7, R8> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5,
			ValueValidator<? super A, ? extends R6> v6,
			ValueValidator<? super A, ? extends R7> v7,
			ValueValidator<? super A, ? extends R8> v8) {
		return new Arguments8Combining<>(v1, v2, v3, v4, v5, v6, v7, v8);
	}

	public static <A, R1, R2, R3, R4, R5, R6, R7, R8, R9> Arguments9Combining<A, R1, R2, R3, R4, R5, R6, R7, R8, R9> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5,
			ValueValidator<? super A, ? extends R6> v6,
			ValueValidator<? super A, ? extends R7> v7,
			ValueValidator<? super A, ? extends R8> v8,
			ValueValidator<? super A, ? extends R9> v9) {
		return new Arguments9Combining<>(v1, v2, v3, v4, v5, v6, v7, v8, v9);
	}

	public static <A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10> Arguments10Combining<A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5,
			ValueValidator<? super A, ? extends R6> v6,
			ValueValidator<? super A, ? extends R7> v7,
			ValueValidator<? super A, ? extends R8> v8,
			ValueValidator<? super A, ? extends R9> v9,
			ValueValidator<? super A, ? extends R10> v10) {
		return new Arguments10Combining<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
	}

	public static <A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11> Arguments11Combining<A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5,
			ValueValidator<? super A, ? extends R6> v6,
			ValueValidator<? super A, ? extends R7> v7,
			ValueValidator<? super A, ? extends R8> v8,
			ValueValidator<? super A, ? extends R9> v9,
			ValueValidator<? super A, ? extends R10> v10,
			ValueValidator<? super A, ? extends R11> v11) {
		return new Arguments11Combining<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11);
	}

	public static <A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12> Arguments12Combining<A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5,
			ValueValidator<? super A, ? extends R6> v6,
			ValueValidator<? super A, ? extends R7> v7,
			ValueValidator<? super A, ? extends R8> v8,
			ValueValidator<? super A, ? extends R9> v9,
			ValueValidator<? super A, ? extends R10> v10,
			ValueValidator<? super A, ? extends R11> v11,
			ValueValidator<? super A, ? extends R12> v12) {
		return new Arguments12Combining<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12);
	}

	public static <A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13> Arguments13Combining<A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5,
			ValueValidator<? super A, ? extends R6> v6,
			ValueValidator<? super A, ? extends R7> v7,
			ValueValidator<? super A, ? extends R8> v8,
			ValueValidator<? super A, ? extends R9> v9,
			ValueValidator<? super A, ? extends R10> v10,
			ValueValidator<? super A, ? extends R11> v11,
			ValueValidator<? super A, ? extends R12> v12,
			ValueValidator<? super A, ? extends R13> v13) {
		return new Arguments13Combining<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v13);
	}

	public static <A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14> Arguments14Combining<A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5,
			ValueValidator<? super A, ? extends R6> v6,
			ValueValidator<? super A, ? extends R7> v7,
			ValueValidator<? super A, ? extends R8> v8,
			ValueValidator<? super A, ? extends R9> v9,
			ValueValidator<? super A, ? extends R10> v10,
			ValueValidator<? super A, ? extends R11> v11,
			ValueValidator<? super A, ? extends R12> v12,
			ValueValidator<? super A, ? extends R13> v13,
			ValueValidator<? super A, ? extends R14> v14) {
		return new Arguments14Combining<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v13, v14);
	}

	public static <A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15> Arguments15Combining<A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5,
			ValueValidator<? super A, ? extends R6> v6,
			ValueValidator<? super A, ? extends R7> v7,
			ValueValidator<? super A, ? extends R8> v8,
			ValueValidator<? super A, ? extends R9> v9,
			ValueValidator<? super A, ? extends R10> v10,
			ValueValidator<? super A, ? extends R11> v11,
			ValueValidator<? super A, ? extends R12> v12,
			ValueValidator<? super A, ? extends R13> v13,
			ValueValidator<? super A, ? extends R14> v14,
			ValueValidator<? super A, ? extends R15> v15) {
		return new Arguments15Combining<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v13, v14, v15);
	}

	public static <A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, R16> Arguments16Combining<A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, R16> combine(
			ValueValidator<? super A, ? extends R1> v1,
			ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3,
			ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5,
			ValueValidator<? super A, ? extends R6> v6,
			ValueValidator<? super A, ? extends R7> v7,
			ValueValidator<? super A, ? extends R8> v8,
			ValueValidator<? super A, ? extends R9> v9,
			ValueValidator<? super A, ? extends R10> v10,
			ValueValidator<? super A, ? extends R11> v11,
			ValueValidator<? super A, ? extends R12> v12,
			ValueValidator<? super A, ? extends R13> v13,
			ValueValidator<? super A, ? extends R14> v14,
			ValueValidator<? super A, ? extends R15> v15,
			ValueValidator<? super A, ? extends R16> v16) {
		return new Arguments16Combining<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12, v13, v14, v15, v16);
	}

	public static <A1, R, T> Arguments1Validator<A1, List<R>> traverse1(
			Iterable<T> values,
			Function<? super T, ? extends Arguments1Validator<? super A1, ? extends R>> f) {
		return (a1, locale, constraintContext) -> Validated.traverse(values, f
				.andThen(validator -> validator.validate(a1, locale, constraintContext)));
	}

	public static <A1, A2, R, T> Arguments2Validator<A1, A2, List<R>> traverse2(
			Iterable<T> values,
			Function<? super T, ? extends Arguments2Validator<? super A1, ? super A2, ? extends R>> f) {
		return (a1, a2, locale, constraintContext) -> Validated.traverse(values,
				f.andThen(validator -> validator.validate(a1, a2, locale,
						constraintContext)));
	}

	public static <A1, A2, A3, R, T> Arguments3Validator<A1, A2, A3, List<R>> traverse3(
			Iterable<T> values,
			Function<? super T, ? extends Arguments3Validator<? super A1, ? super A2, ? super A3, ? extends R>> f) {
		return (a1, a2, a3, locale, constraintContext) -> Validated.traverse(values,
				f.andThen(validator -> validator.validate(a1, a2, a3, locale,
						constraintContext)));
	}

	public static <A1, A2, A3, A4, R, T> Arguments4Validator<A1, A2, A3, A4, List<R>> traverse4(
			Iterable<T> values,
			Function<? super T, ? extends Arguments4Validator<? super A1, ? super A2, ? super A3, ? super A4, ? extends R>> f) {
		return (a1, a2, a3, a4, locale, constraintContext) -> Validated.traverse(values,
				f.andThen(validator -> validator.validate(a1, a2, a3, a4, locale,
						constraintContext)));
	}

	public static <A1, A2, A3, A4, A5, R, T> Arguments5Validator<A1, A2, A3, A4, A5, List<R>> traverse5(
			Iterable<T> values,
			Function<? super T, ? extends Arguments5Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? extends R>> f) {
		return (a1, a2, a3, a4, a5, locale, constraintContext) -> Validated
				.traverse(values, f.andThen(validator -> validator.validate(a1, a2, a3,
						a4, a5, locale, constraintContext)));
	}

	public static <A1, A2, A3, A4, A5, A6, R, T> Arguments6Validator<A1, A2, A3, A4, A5, A6, List<R>> traverse6(
			Iterable<T> values,
			Function<? super T, ? extends Arguments6Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? extends R>> f) {
		return (a1, a2, a3, a4, a5, a6, locale, constraintContext) -> Validated
				.traverse(values, f.andThen(validator -> validator.validate(a1, a2, a3,
						a4, a5, a6, locale, constraintContext)));
	}

	public static <A1, A2, A3, A4, A5, A6, A7, R, T> Arguments7Validator<A1, A2, A3, A4, A5, A6, A7, List<R>> traverse7(
			Iterable<T> values,
			Function<? super T, ? extends Arguments7Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? extends R>> f) {
		return (a1, a2, a3, a4, a5, a6, a7, locale, constraintContext) -> Validated
				.traverse(values, f.andThen(validator -> validator.validate(a1, a2, a3,
						a4, a5, a6, a7, locale, constraintContext)));
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, R, T> Arguments8Validator<A1, A2, A3, A4, A5, A6, A7, A8, List<R>> traverse8(
			Iterable<T> values,
			Function<? super T, ? extends Arguments8Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? extends R>> f) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, locale, constraintContext) -> Validated
				.traverse(values, f.andThen(validator -> validator.validate(a1, a2, a3,
						a4, a5, a6, a7, a8, locale, constraintContext)));
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, R, T> Arguments9Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, List<R>> traverse9(
			Iterable<T> values,
			Function<? super T, ? extends Arguments9Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? extends R>> f) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, a9, locale,
				constraintContext) -> Validated.traverse(values,
						f.andThen(validator -> validator.validate(a1, a2, a3, a4, a5, a6,
								a7, a8, a9, locale, constraintContext)));
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R, T> Arguments10Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, List<R>> traverse10(
			Iterable<T> values,
			Function<? super T, ? extends Arguments10Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? extends R>> f) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, locale,
				constraintContext) -> Validated.traverse(values,
						f.andThen(validator -> validator.validate(a1, a2, a3, a4, a5, a6,
								a7, a8, a9, a10, locale, constraintContext)));
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R, T> Arguments11Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, List<R>> traverse11(
			Iterable<T> values,
			Function<? super T, ? extends Arguments11Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? extends R>> f) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, locale,
				constraintContext) -> Validated.traverse(values,
						f.andThen(validator -> validator.validate(a1, a2, a3, a4, a5, a6,
								a7, a8, a9, a10, a11, locale, constraintContext)));
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R, T> Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, List<R>> traverse12(
			Iterable<T> values,
			Function<? super T, ? extends Arguments12Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? super A12, ? extends R>> f) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, locale,
				constraintContext) -> Validated.traverse(values,
						f.andThen(validator -> validator.validate(a1, a2, a3, a4, a5, a6,
								a7, a8, a9, a10, a11, a12, locale, constraintContext)));
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R, T> Arguments13Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, List<R>> traverse13(
			Iterable<T> values,
			Function<? super T, ? extends Arguments13Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? super A12, ? super A13, ? extends R>> f) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, locale,
				constraintContext) -> Validated.traverse(values,
						f.andThen(validator -> validator.validate(a1, a2, a3, a4, a5, a6,
								a7, a8, a9, a10, a11, a12, a13, locale,
								constraintContext)));
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, R, T> Arguments14Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, List<R>> traverse14(
			Iterable<T> values,
			Function<? super T, ? extends Arguments14Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? super A12, ? super A13, ? super A14, ? extends R>> f) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, locale,
				constraintContext) -> Validated.traverse(values,
						f.andThen(validator -> validator.validate(a1, a2, a3, a4, a5, a6,
								a7, a8, a9, a10, a11, a12, a13, a14, locale,
								constraintContext)));
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, R, T> Arguments15Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, List<R>> traverse15(
			Iterable<T> values,
			Function<? super T, ? extends Arguments15Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? super A12, ? super A13, ? super A14, ? super A15, ? extends R>> f) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, locale,
				constraintContext) -> Validated.traverse(values,
						f.andThen(validator -> validator.validate(a1, a2, a3, a4, a5, a6,
								a7, a8, a9, a10, a11, a12, a13, a14, a15, locale,
								constraintContext)));
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, R, T> Arguments16Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, List<R>> traverse16(
			Iterable<T> values,
			Function<? super T, ? extends Arguments16Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? super A12, ? super A13, ? super A14, ? super A15, ? super A16, ? extends R>> f) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16,
				locale, constraintContext) -> Validated.traverse(values,
						f.andThen(validator -> validator.validate(a1, a2, a3, a4, a5, a6,
								a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, locale,
								constraintContext)));
	}

	public static <A1, R> Arguments1Validator<A1, List<R>> sequence1(
			Iterable<? extends Arguments1Validator<? super A1, ? extends R>> values) {
		return traverse1(values, identity());
	}

	public static <A1, A2, R> Arguments2Validator<A1, A2, List<R>> sequence2(
			Iterable<? extends Arguments2Validator<? super A1, ? super A2, ? extends R>> values) {
		return traverse2(values, identity());
	}

	public static <A1, A2, A3, R> Arguments3Validator<A1, A2, A3, List<R>> sequence3(
			Iterable<? extends Arguments3Validator<? super A1, ? super A2, ? super A3, ? extends R>> values) {
		return traverse3(values, identity());
	}

	public static <A1, A2, A3, A4, R> Arguments4Validator<A1, A2, A3, A4, List<R>> sequence4(
			Iterable<? extends Arguments4Validator<? super A1, ? super A2, ? super A3, ? super A4, ? extends R>> values) {
		return traverse4(values, identity());
	}

	public static <A1, A2, A3, A4, A5, R> Arguments5Validator<A1, A2, A3, A4, A5, List<R>> sequence5(
			Iterable<? extends Arguments5Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? extends R>> values) {
		return traverse5(values, identity());
	}

	public static <A1, A2, A3, A4, A5, A6, R> Arguments6Validator<A1, A2, A3, A4, A5, A6, List<R>> sequence6(
			Iterable<? extends Arguments6Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? extends R>> values) {
		return traverse6(values, identity());
	}

	public static <A1, A2, A3, A4, A5, A6, A7, R> Arguments7Validator<A1, A2, A3, A4, A5, A6, A7, List<R>> sequence7(
			Iterable<? extends Arguments7Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? extends R>> values) {
		return traverse7(values, identity());
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, R> Arguments8Validator<A1, A2, A3, A4, A5, A6, A7, A8, List<R>> sequence8(
			Iterable<? extends Arguments8Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? extends R>> values) {
		return traverse8(values, identity());
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, R> Arguments9Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, List<R>> sequence9(
			Iterable<? extends Arguments9Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? extends R>> values) {
		return traverse9(values, identity());
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R> Arguments10Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, List<R>> sequence10(
			Iterable<? extends Arguments10Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? extends R>> values) {
		return traverse10(values, identity());
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R> Arguments11Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, List<R>> sequence11(
			Iterable<? extends Arguments11Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? extends R>> values) {
		return traverse11(values, identity());
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R> Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, List<R>> sequence12(
			Iterable<? extends Arguments12Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? super A12, ? extends R>> values) {
		return traverse12(values, identity());
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R> Arguments13Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, List<R>> sequence13(
			Iterable<? extends Arguments13Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? super A12, ? super A13, ? extends R>> values) {
		return traverse13(values, identity());
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, R> Arguments14Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, List<R>> sequence14(
			Iterable<? extends Arguments14Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? super A12, ? super A13, ? super A14, ? extends R>> values) {
		return traverse14(values, identity());
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, R> Arguments15Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, List<R>> sequence15(
			Iterable<? extends Arguments15Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? super A12, ? super A13, ? super A14, ? super A15, ? extends R>> values) {
		return traverse15(values, identity());
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, R> Arguments16Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, List<R>> sequence16(
			Iterable<? extends Arguments16Validator<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? super A12, ? super A13, ? super A14, ? super A15, ? super A16, ? extends R>> values) {
		return traverse16(values, identity());
	}

	/**
	 * @since 0.8.0
	 */
	public static <A1, R, C extends Collection<R>> Arguments1Validator<Iterable<A1>, C> liftCollection(
			ValueValidator<? super A1, ? extends R> validator, Supplier<C> factory) {
		return Arguments1Validator
				.from(ValueValidator.liftCollection(validator, factory));
	}

	public static <A1, R> Arguments1Validator<Iterable<A1>, List<R>> liftList(
			ValueValidator<? super A1, ? extends R> validator) {
		return Arguments1Validator.from(ValueValidator.liftList(validator));
	}

	/**
	 * @since 0.8.0
	 */
	public static <A1, R> Arguments1Validator<Iterable<A1>, Set<R>> liftSet(
			ValueValidator<? super A1, ? extends R> validator) {
		return Arguments1Validator.from(ValueValidator.liftSet(validator));
	}

	/**
	 * @since 0.8.0
	 */
	public static <A1, R> Arguments1Validator<Optional<A1>, Optional<R>> liftOptional(
			ValueValidator<? super A1, ? extends R> validator) {
		return Arguments1Validator.from(ValueValidator.liftOptional(validator));
	}

}
