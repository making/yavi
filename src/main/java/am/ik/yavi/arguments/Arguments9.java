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

import java.util.Objects;

import am.ik.yavi.fn.Function9;
import am.ik.yavi.jsr305.Nullable;

/**
 * A container class that holds 9 arguments, providing type-safe access to each argument
 * and mapping functionality to transform these arguments.
 *
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @param <A1, A2, A3, A4, A5, A6, A7, A8, A9> the types of the arguments
 * @since 0.3.0
 */
public class Arguments9<A1, A2, A3, A4, A5, A6, A7, A8, A9> {

	protected final A1 arg1;

	protected final A2 arg2;

	protected final A3 arg3;

	protected final A4 arg4;

	protected final A5 arg5;

	protected final A6 arg6;

	protected final A7 arg7;

	protected final A8 arg8;

	protected final A9 arg9;

	/**
	 * Creates a new Arguments9 instance with the provided arguments.
	 * @param arg1 the argument at position 1, arg2 the argument at position 2, arg3 the
	 * argument at position 3, arg4 the argument at position 4, arg5 the argument at
	 * position 5, arg6 the argument at position 6, arg7 the argument at position 7, arg8
	 * the argument at position 8, arg9 the argument at position 9
	 */
	Arguments9(@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5,
			@Nullable A6 arg6, @Nullable A7 arg7, @Nullable A8 arg8, @Nullable A9 arg9) {
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.arg3 = arg3;
		this.arg4 = arg4;
		this.arg5 = arg5;
		this.arg6 = arg6;
		this.arg7 = arg7;
		this.arg8 = arg8;
		this.arg9 = arg9;
	}

	/**
	 * Returns the argument at position 1.
	 * @return the argument at position 1
	 */
	@Nullable
	public final A1 arg1() {
		return this.arg1;
	}

	/**
	 * Returns the argument at position 2.
	 * @return the argument at position 2
	 */
	@Nullable
	public final A2 arg2() {
		return this.arg2;
	}

	/**
	 * Returns the argument at position 3.
	 * @return the argument at position 3
	 */
	@Nullable
	public final A3 arg3() {
		return this.arg3;
	}

	/**
	 * Returns the argument at position 4.
	 * @return the argument at position 4
	 */
	@Nullable
	public final A4 arg4() {
		return this.arg4;
	}

	/**
	 * Returns the argument at position 5.
	 * @return the argument at position 5
	 */
	@Nullable
	public final A5 arg5() {
		return this.arg5;
	}

	/**
	 * Returns the argument at position 6.
	 * @return the argument at position 6
	 */
	@Nullable
	public final A6 arg6() {
		return this.arg6;
	}

	/**
	 * Returns the argument at position 7.
	 * @return the argument at position 7
	 */
	@Nullable
	public final A7 arg7() {
		return this.arg7;
	}

	/**
	 * Returns the argument at position 8.
	 * @return the argument at position 8
	 */
	@Nullable
	public final A8 arg8() {
		return this.arg8;
	}

	/**
	 * Returns the argument at position 9.
	 * @return the argument at position 9
	 */
	@Nullable
	public final A9 arg9() {
		return this.arg9;
	}

	/**
	 * Applies the provided mapping function to all arguments contained in this instance.
	 * @param <X> the type of the result
	 * @param mapper the function to apply to the arguments
	 * @return the result of applying the mapper function to the arguments
	 */
	public final <X> X map(
			Function9<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? extends X> mapper) {
		return mapper.apply(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
	}

	/**
	 * Returns a new Arguments1 instance containing only the first 1 arguments.
	 * @return an Arguments1 instance with arguments from arg1 to arg1
	 * @since 0.16.0
	 */
	public final Arguments1<A1> first1() {
		return new Arguments1<>(arg1);
	}

	/**
	 * Returns a new Arguments2 instance containing only the first 2 arguments.
	 * @return an Arguments2 instance with arguments from arg1 to arg2
	 * @since 0.16.0
	 */
	public final Arguments2<A1, A2> first2() {
		return new Arguments2<>(arg1, arg2);
	}

	/**
	 * Returns a new Arguments3 instance containing only the first 3 arguments.
	 * @return an Arguments3 instance with arguments from arg1 to arg3
	 * @since 0.16.0
	 */
	public final Arguments3<A1, A2, A3> first3() {
		return new Arguments3<>(arg1, arg2, arg3);
	}

	/**
	 * Returns a new Arguments4 instance containing only the first 4 arguments.
	 * @return an Arguments4 instance with arguments from arg1 to arg4
	 * @since 0.16.0
	 */
	public final Arguments4<A1, A2, A3, A4> first4() {
		return new Arguments4<>(arg1, arg2, arg3, arg4);
	}

	/**
	 * Returns a new Arguments5 instance containing only the first 5 arguments.
	 * @return an Arguments5 instance with arguments from arg1 to arg5
	 * @since 0.16.0
	 */
	public final Arguments5<A1, A2, A3, A4, A5> first5() {
		return new Arguments5<>(arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 * Returns a new Arguments6 instance containing only the first 6 arguments.
	 * @return an Arguments6 instance with arguments from arg1 to arg6
	 * @since 0.16.0
	 */
	public final Arguments6<A1, A2, A3, A4, A5, A6> first6() {
		return new Arguments6<>(arg1, arg2, arg3, arg4, arg5, arg6);
	}

	/**
	 * Returns a new Arguments7 instance containing only the first 7 arguments.
	 * @return an Arguments7 instance with arguments from arg1 to arg7
	 * @since 0.16.0
	 */
	public final Arguments7<A1, A2, A3, A4, A5, A6, A7> first7() {
		return new Arguments7<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7);
	}

	/**
	 * Returns a new Arguments8 instance containing only the first 8 arguments.
	 * @return an Arguments8 instance with arguments from arg1 to arg8
	 * @since 0.16.0
	 */
	public final Arguments8<A1, A2, A3, A4, A5, A6, A7, A8> first8() {
		return new Arguments8<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
	}

	/**
	 * Returns a new Arguments1 instance containing only the last 1 arguments.
	 * @return an Arguments1 instance with arguments from arg9 to arg9
	 * @since 0.16.0
	 */
	public final Arguments1<A9> last1() {
		return new Arguments1<>(arg9);
	}

	/**
	 * Returns a new Arguments2 instance containing only the last 2 arguments.
	 * @return an Arguments2 instance with arguments from arg8 to arg9
	 * @since 0.16.0
	 */
	public final Arguments2<A8, A9> last2() {
		return new Arguments2<>(arg8, arg9);
	}

	/**
	 * Returns a new Arguments3 instance containing only the last 3 arguments.
	 * @return an Arguments3 instance with arguments from arg7 to arg9
	 * @since 0.16.0
	 */
	public final Arguments3<A7, A8, A9> last3() {
		return new Arguments3<>(arg7, arg8, arg9);
	}

	/**
	 * Returns a new Arguments4 instance containing only the last 4 arguments.
	 * @return an Arguments4 instance with arguments from arg6 to arg9
	 * @since 0.16.0
	 */
	public final Arguments4<A6, A7, A8, A9> last4() {
		return new Arguments4<>(arg6, arg7, arg8, arg9);
	}

	/**
	 * Returns a new Arguments5 instance containing only the last 5 arguments.
	 * @return an Arguments5 instance with arguments from arg5 to arg9
	 * @since 0.16.0
	 */
	public final Arguments5<A5, A6, A7, A8, A9> last5() {
		return new Arguments5<>(arg5, arg6, arg7, arg8, arg9);
	}

	/**
	 * Returns a new Arguments6 instance containing only the last 6 arguments.
	 * @return an Arguments6 instance with arguments from arg4 to arg9
	 * @since 0.16.0
	 */
	public final Arguments6<A4, A5, A6, A7, A8, A9> last6() {
		return new Arguments6<>(arg4, arg5, arg6, arg7, arg8, arg9);
	}

	/**
	 * Returns a new Arguments7 instance containing only the last 7 arguments.
	 * @return an Arguments7 instance with arguments from arg3 to arg9
	 * @since 0.16.0
	 */
	public final Arguments7<A3, A4, A5, A6, A7, A8, A9> last7() {
		return new Arguments7<>(arg3, arg4, arg5, arg6, arg7, arg8, arg9);
	}

	/**
	 * Returns a new Arguments8 instance containing only the last 8 arguments.
	 * @return an Arguments8 instance with arguments from arg2 to arg9
	 * @since 0.16.0
	 */
	public final Arguments8<A2, A3, A4, A5, A6, A7, A8, A9> last8() {
		return new Arguments8<>(arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * @param obj the reference object with which to compare
	 * @return true if this object is the same as the obj argument; false otherwise
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Arguments9<A1, A2, A3, A4, A5, A6, A7, A8, A9> that = (Arguments9<A1, A2, A3, A4, A5, A6, A7, A8, A9>) obj;
		return Objects.equals(this.arg1, that.arg1) && Objects.equals(this.arg2, that.arg2)
				&& Objects.equals(this.arg3, that.arg3) && Objects.equals(this.arg4, that.arg4)
				&& Objects.equals(this.arg5, that.arg5) && Objects.equals(this.arg6, that.arg6)
				&& Objects.equals(this.arg7, that.arg7) && Objects.equals(this.arg8, that.arg8)
				&& Objects.equals(this.arg9, that.arg9);
	}

	/**
	 * Returns a hash code value for the object.
	 * @return a hash code value for this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.arg1, this.arg2, this.arg3, this.arg4, this.arg5, this.arg6, this.arg7, this.arg8,
				this.arg9);
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object
	 */
	@Override
	public String toString() {
		return "Arguments9{" + "arg1=" + this.arg1 + ", " + "arg2=" + this.arg2 + ", " + "arg3=" + this.arg3 + ", "
				+ "arg4=" + this.arg4 + ", " + "arg5=" + this.arg5 + ", " + "arg6=" + this.arg6 + ", " + "arg7="
				+ this.arg7 + ", " + "arg8=" + this.arg8 + ", " + "arg9=" + this.arg9 + "}";
	}

}
