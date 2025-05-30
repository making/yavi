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

import am.ik.yavi.fn.Function4;
import am.ik.yavi.jsr305.Nullable;

/**
 * A container class that holds 4 arguments, providing type-safe access to each argument
 * and mapping functionality to transform these arguments.
 *
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @param <A1> the type of argument at position 1
 * @param <A2> the type of argument at position 2
 * @param <A3> the type of argument at position 3
 * @param <A4> the type of argument at position 4
 * @since 0.3.0
 */
public final class Arguments4<A1, A2, A3, A4> {

	private final A1 arg1;

	private final A2 arg2;

	private final A3 arg3;

	private final A4 arg4;

	/**
	 * Creates a new Arguments4 instance with the provided arguments.
	 * @param arg1 the argument at position 1, arg2 the argument at position 2, arg3 the
	 * argument at position 3, arg4 the argument at position 4
	 */
	Arguments4(@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4) {
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.arg3 = arg3;
		this.arg4 = arg4;
	}

	/**
	 * Returns the argument at position 1.
	 * @return the argument at position 1
	 */
	@Nullable
	public A1 arg1() {
		return this.arg1;
	}

	/**
	 * Returns the argument at position 2.
	 * @return the argument at position 2
	 */
	@Nullable
	public A2 arg2() {
		return this.arg2;
	}

	/**
	 * Returns the argument at position 3.
	 * @return the argument at position 3
	 */
	@Nullable
	public A3 arg3() {
		return this.arg3;
	}

	/**
	 * Returns the argument at position 4.
	 * @return the argument at position 4
	 */
	@Nullable
	public A4 arg4() {
		return this.arg4;
	}

	/**
	 * Applies the provided mapping function to all arguments contained in this instance.
	 * @param <X> the type of the result
	 * @param mapper the function to apply to the arguments
	 * @return the result of applying the mapper function to the arguments
	 */
	public <X> X map(Function4<? super A1, ? super A2, ? super A3, ? super A4, ? extends X> mapper) {
		return mapper.apply(arg1, arg2, arg3, arg4);
	}

	/**
	 * Returns a new Arguments1 instance containing only the first 1 arguments.
	 * @return an Arguments1 instance with arguments from arg1 to arg1
	 * @since 0.16.0
	 */
	public Arguments1<A1> first1() {
		return new Arguments1<>(arg1);
	}

	/**
	 * Returns a new Arguments2 instance containing only the first 2 arguments.
	 * @return an Arguments2 instance with arguments from arg1 to arg2
	 * @since 0.16.0
	 */
	public Arguments2<A1, A2> first2() {
		return new Arguments2<>(arg1, arg2);
	}

	/**
	 * Returns a new Arguments3 instance containing only the first 3 arguments.
	 * @return an Arguments3 instance with arguments from arg1 to arg3
	 * @since 0.16.0
	 */
	public Arguments3<A1, A2, A3> first3() {
		return new Arguments3<>(arg1, arg2, arg3);
	}

	/**
	 * Returns a new Arguments1 instance containing only the last 1 arguments.
	 * @return an Arguments1 instance with arguments from arg4 to arg4
	 * @since 0.16.0
	 */
	public Arguments1<A4> last1() {
		return new Arguments1<>(arg4);
	}

	/**
	 * Returns a new Arguments2 instance containing only the last 2 arguments.
	 * @return an Arguments2 instance with arguments from arg3 to arg4
	 * @since 0.16.0
	 */
	public Arguments2<A3, A4> last2() {
		return new Arguments2<>(arg3, arg4);
	}

	/**
	 * Returns a new Arguments3 instance containing only the last 3 arguments.
	 * @return an Arguments3 instance with arguments from arg2 to arg4
	 * @since 0.16.0
	 */
	public Arguments3<A2, A3, A4> last3() {
		return new Arguments3<>(arg2, arg3, arg4);
	}

	/**
	 * Appends an additional argument to create a new, larger Arguments instance.
	 * @param <B> the type of the argument to append
	 * @param arg the argument to append
	 * @return a new Arguments5 instance with the additional argument
	 * @since 0.16.0
	 */
	public <B> Arguments5<A1, A2, A3, A4, B> append(@Nullable B arg) {
		return new Arguments5<>(this.arg1, this.arg2, this.arg3, this.arg4, arg);
	}

	/**
	 * Prepends an additional argument to create a new, larger Arguments instance.
	 * @param <B> the type of the argument to prepend
	 * @param arg the argument to prepend
	 * @return a new Arguments5 instance with the additional argument
	 * @since 0.16.0
	 */
	public <B> Arguments5<B, A1, A2, A3, A4> prepend(@Nullable B arg) {
		return new Arguments5<>(arg, this.arg1, this.arg2, this.arg3, this.arg4);
	}

	/**
	 * Returns a new Arguments4 instance with the arguments in reverse order.
	 * @return an Arguments4 instance with arguments in reverse order
	 * @since 0.16.0
	 */
	public Arguments4<A4, A3, A2, A1> reverse() {
		return new Arguments4<>(arg4, arg3, arg2, arg1);
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
		Arguments4<A1, A2, A3, A4> that = (Arguments4<A1, A2, A3, A4>) obj;
		return Objects.equals(this.arg1, that.arg1) && Objects.equals(this.arg2, that.arg2)
				&& Objects.equals(this.arg3, that.arg3) && Objects.equals(this.arg4, that.arg4);
	}

	/**
	 * Returns a hash code value for the object.
	 * @return a hash code value for this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.arg1, this.arg2, this.arg3, this.arg4);
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object
	 */
	@Override
	public String toString() {
		return "Arguments4{" + "arg1=" + this.arg1 + ", " + "arg2=" + this.arg2 + ", " + "arg3=" + this.arg3 + ", "
				+ "arg4=" + this.arg4 + "}";
	}

}
