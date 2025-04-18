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

import am.ik.yavi.fn.Function11;
import am.ik.yavi.jsr305.Nullable;

/**
 * A container class that holds 11 arguments, providing type-safe access to each argument
 * and mapping functionality to transform these arguments.
 *
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @param <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11> the types of the arguments
 * @since 0.3.0
 */
public class Arguments11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11> {

	protected final A1 arg1;

	protected final A2 arg2;

	protected final A3 arg3;

	protected final A4 arg4;

	protected final A5 arg5;

	protected final A6 arg6;

	protected final A7 arg7;

	protected final A8 arg8;

	protected final A9 arg9;

	protected final A10 arg10;

	protected final A11 arg11;

	/**
	 * Creates a new Arguments11 instance with the provided arguments.
	 * @param arg1 the argument at position 1, arg2 the argument at position 2, arg3 the
	 * argument at position 3, arg4 the argument at position 4, arg5 the argument at
	 * position 5, arg6 the argument at position 6, arg7 the argument at position 7, arg8
	 * the argument at position 8, arg9 the argument at position 9, arg10 the argument at
	 * position 10, arg11 the argument at position 11
	 */
	Arguments11(@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5,
			@Nullable A6 arg6, @Nullable A7 arg7, @Nullable A8 arg8, @Nullable A9 arg9, @Nullable A10 arg10,
			@Nullable A11 arg11) {
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.arg3 = arg3;
		this.arg4 = arg4;
		this.arg5 = arg5;
		this.arg6 = arg6;
		this.arg7 = arg7;
		this.arg8 = arg8;
		this.arg9 = arg9;
		this.arg10 = arg10;
		this.arg11 = arg11;
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
	 * Returns the argument at position 10.
	 * @return the argument at position 10
	 */
	@Nullable
	public final A10 arg10() {
		return this.arg10;
	}

	/**
	 * Returns the argument at position 11.
	 * @return the argument at position 11
	 */
	@Nullable
	public final A11 arg11() {
		return this.arg11;
	}

	/**
	 * Applies the provided mapping function to all arguments contained in this instance.
	 * @param <X> the type of the result
	 * @param mapper the function to apply to the arguments
	 * @return the result of applying the mapper function to the arguments
	 */
	public final <X> X map(
			Function11<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? extends X> mapper) {
		return mapper.apply(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
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
	 * Returns a new Arguments9 instance containing only the first 9 arguments.
	 * @return an Arguments9 instance with arguments from arg1 to arg9
	 * @since 0.16.0
	 */
	public final Arguments9<A1, A2, A3, A4, A5, A6, A7, A8, A9> first9() {
		return new Arguments9<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
	}

	/**
	 * Returns a new Arguments10 instance containing only the first 10 arguments.
	 * @return an Arguments10 instance with arguments from arg1 to arg10
	 * @since 0.16.0
	 */
	public final Arguments10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10> first10() {
		return new Arguments10<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
	}

	/**
	 * Returns a new Arguments1 instance containing only the last 1 arguments.
	 * @return an Arguments1 instance with arguments from arg11 to arg11
	 * @since 0.16.0
	 */
	public final Arguments1<A11> last1() {
		return new Arguments1<>(arg11);
	}

	/**
	 * Returns a new Arguments2 instance containing only the last 2 arguments.
	 * @return an Arguments2 instance with arguments from arg10 to arg11
	 * @since 0.16.0
	 */
	public final Arguments2<A10, A11> last2() {
		return new Arguments2<>(arg10, arg11);
	}

	/**
	 * Returns a new Arguments3 instance containing only the last 3 arguments.
	 * @return an Arguments3 instance with arguments from arg9 to arg11
	 * @since 0.16.0
	 */
	public final Arguments3<A9, A10, A11> last3() {
		return new Arguments3<>(arg9, arg10, arg11);
	}

	/**
	 * Returns a new Arguments4 instance containing only the last 4 arguments.
	 * @return an Arguments4 instance with arguments from arg8 to arg11
	 * @since 0.16.0
	 */
	public final Arguments4<A8, A9, A10, A11> last4() {
		return new Arguments4<>(arg8, arg9, arg10, arg11);
	}

	/**
	 * Returns a new Arguments5 instance containing only the last 5 arguments.
	 * @return an Arguments5 instance with arguments from arg7 to arg11
	 * @since 0.16.0
	 */
	public final Arguments5<A7, A8, A9, A10, A11> last5() {
		return new Arguments5<>(arg7, arg8, arg9, arg10, arg11);
	}

	/**
	 * Returns a new Arguments6 instance containing only the last 6 arguments.
	 * @return an Arguments6 instance with arguments from arg6 to arg11
	 * @since 0.16.0
	 */
	public final Arguments6<A6, A7, A8, A9, A10, A11> last6() {
		return new Arguments6<>(arg6, arg7, arg8, arg9, arg10, arg11);
	}

	/**
	 * Returns a new Arguments7 instance containing only the last 7 arguments.
	 * @return an Arguments7 instance with arguments from arg5 to arg11
	 * @since 0.16.0
	 */
	public final Arguments7<A5, A6, A7, A8, A9, A10, A11> last7() {
		return new Arguments7<>(arg5, arg6, arg7, arg8, arg9, arg10, arg11);
	}

	/**
	 * Returns a new Arguments8 instance containing only the last 8 arguments.
	 * @return an Arguments8 instance with arguments from arg4 to arg11
	 * @since 0.16.0
	 */
	public final Arguments8<A4, A5, A6, A7, A8, A9, A10, A11> last8() {
		return new Arguments8<>(arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
	}

	/**
	 * Returns a new Arguments9 instance containing only the last 9 arguments.
	 * @return an Arguments9 instance with arguments from arg3 to arg11
	 * @since 0.16.0
	 */
	public final Arguments9<A3, A4, A5, A6, A7, A8, A9, A10, A11> last9() {
		return new Arguments9<>(arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
	}

	/**
	 * Returns a new Arguments10 instance containing only the last 10 arguments.
	 * @return an Arguments10 instance with arguments from arg2 to arg11
	 * @since 0.16.0
	 */
	public final Arguments10<A2, A3, A4, A5, A6, A7, A8, A9, A10, A11> last10() {
		return new Arguments10<>(arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
	}

	/**
	 * Appends an additional argument to create a new, larger Arguments instance.
	 * @param <B> the type of the argument to append
	 * @param arg the argument to append
	 * @return a new Arguments12 instance with the additional argument
	 * @since 0.16.0
	 */
	public final <B> Arguments12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, B> append(@Nullable B arg) {
		return new Arguments12<>(this.arg1, this.arg2, this.arg3, this.arg4, this.arg5, this.arg6, this.arg7, this.arg8,
				this.arg9, this.arg10, this.arg11, arg);
	}

	/**
	 * Prepends an additional argument to create a new, larger Arguments instance.
	 * @param <B> the type of the argument to prepend
	 * @param arg the argument to prepend
	 * @return a new Arguments12 instance with the additional argument
	 * @since 0.16.0
	 */
	public final <B> Arguments12<B, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11> prepend(@Nullable B arg) {
		return new Arguments12<>(arg, this.arg1, this.arg2, this.arg3, this.arg4, this.arg5, this.arg6, this.arg7,
				this.arg8, this.arg9, this.arg10, this.arg11);
	}

	/**
	 * Returns a new Arguments11 instance with the arguments in reverse order.
	 * @return an Arguments11 instance with arguments in reverse order
	 * @since 0.16.0
	 */
	public final Arguments11<A11, A10, A9, A8, A7, A6, A5, A4, A3, A2, A1> reverse() {
		return new Arguments11<>(arg11, arg10, arg9, arg8, arg7, arg6, arg5, arg4, arg3, arg2, arg1);
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
		Arguments11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11> that = (Arguments11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11>) obj;
		return Objects.equals(this.arg1, that.arg1) && Objects.equals(this.arg2, that.arg2)
				&& Objects.equals(this.arg3, that.arg3) && Objects.equals(this.arg4, that.arg4)
				&& Objects.equals(this.arg5, that.arg5) && Objects.equals(this.arg6, that.arg6)
				&& Objects.equals(this.arg7, that.arg7) && Objects.equals(this.arg8, that.arg8)
				&& Objects.equals(this.arg9, that.arg9) && Objects.equals(this.arg10, that.arg10)
				&& Objects.equals(this.arg11, that.arg11);
	}

	/**
	 * Returns a hash code value for the object.
	 * @return a hash code value for this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.arg1, this.arg2, this.arg3, this.arg4, this.arg5, this.arg6, this.arg7, this.arg8,
				this.arg9, this.arg10, this.arg11);
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object
	 */
	@Override
	public String toString() {
		return "Arguments11{" + "arg1=" + this.arg1 + ", " + "arg2=" + this.arg2 + ", " + "arg3=" + this.arg3 + ", "
				+ "arg4=" + this.arg4 + ", " + "arg5=" + this.arg5 + ", " + "arg6=" + this.arg6 + ", " + "arg7="
				+ this.arg7 + ", " + "arg8=" + this.arg8 + ", " + "arg9=" + this.arg9 + ", " + "arg10=" + this.arg10
				+ ", " + "arg11=" + this.arg11 + "}";
	}

}
