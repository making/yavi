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

import am.ik.yavi.fn.Function16;
import am.ik.yavi.jsr305.Nullable;

/**
 * A container class that holds 16 arguments, providing type-safe access
 * to each argument and mapping functionality to transform these arguments.
 *
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @param <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16> the types of the arguments
 * @since 0.3.0
 */
public class Arguments16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16> extends Arguments15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15> {

	protected final A16 arg16;

	/**
	 * Creates a new Arguments16 instance with the provided arguments.
	 *
	 * @param arg1 the argument at position 1, arg2 the argument at position 2, arg3 the argument at position 3, arg4 the argument at position 4, arg5 the argument at position 5, arg6 the argument at position 6, arg7 the argument at position 7, arg8 the argument at position 8, arg9 the argument at position 9, arg10 the argument at position 10, arg11 the argument at position 11, arg12 the argument at position 12, arg13 the argument at position 13, arg14 the argument at position 14, arg15 the argument at position 15, arg16 the argument at position 16
	 */
	Arguments16(@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5, @Nullable A6 arg6, @Nullable A7 arg7, @Nullable A8 arg8, @Nullable A9 arg9, @Nullable A10 arg10, @Nullable A11 arg11, @Nullable A12 arg12, @Nullable A13 arg13, @Nullable A14 arg14, @Nullable A15 arg15, @Nullable A16 arg16) {
		super(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15);
		this.arg16 = arg16;
	}

	/**
	 * Returns the argument at position 16.
	 *
	 * @return the argument at position 16
	 */
	@Nullable
	public final A16 arg16() {
		return this.arg16;
	}

	/**
	 * Applies the provided mapping function to all arguments contained in this instance.
	 *
	 * @param <X> the type of the result
	 * @param mapper the function to apply to the arguments
	 * @return the result of applying the mapper function to the arguments
	 */
	public final <X> X map(Function16<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? super A12, ? super A13, ? super A14, ? super A15, ? super A16, ? extends X> mapper) {
		return mapper.apply(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16);
	}
	/**
	 * Returns a new Arguments15 instance containing only the first 15 arguments.
	 *
	 * @return an Arguments15 instance with arguments from arg1 to arg15
	 * @since 0.16.0
	 */
	public final Arguments15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15> first15() {
		return new Arguments15<>(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15);
	}


	/**
	 * Indicates whether some other object is "equal to" this one.
	 *
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
		Arguments16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16> that = (Arguments16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16>) obj;
		return Objects.equals(this.arg1, that.arg1) && Objects.equals(this.arg2, that.arg2) && Objects.equals(this.arg3, that.arg3) && Objects.equals(this.arg4, that.arg4) && Objects.equals(this.arg5, that.arg5) && Objects.equals(this.arg6, that.arg6) && Objects.equals(this.arg7, that.arg7) && Objects.equals(this.arg8, that.arg8) && Objects.equals(this.arg9, that.arg9) && Objects.equals(this.arg10, that.arg10) && Objects.equals(this.arg11, that.arg11) && Objects.equals(this.arg12, that.arg12) && Objects.equals(this.arg13, that.arg13) && Objects.equals(this.arg14, that.arg14) && Objects.equals(this.arg15, that.arg15) && Objects.equals(this.arg16, that.arg16);
	}

	/**
	 * Returns a hash code value for the object.
	 *
	 * @return a hash code value for this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.arg1, this.arg2, this.arg3, this.arg4, this.arg5, this.arg6, this.arg7, this.arg8, this.arg9, this.arg10, this.arg11, this.arg12, this.arg13, this.arg14, this.arg15, this.arg16);
	}

	/**
	 * Returns a string representation of the object.
	 *
	 * @return a string representation of the object
	 */
	@Override
	public String toString() {
		return "Arguments16{" + "arg1=" + this.arg1 + ", " + "arg2=" + this.arg2 + ", " + "arg3=" + this.arg3 + ", " + "arg4=" + this.arg4 + ", " + "arg5=" + this.arg5 + ", " + "arg6=" + this.arg6 + ", " + "arg7=" + this.arg7 + ", " + "arg8=" + this.arg8 + ", " + "arg9=" + this.arg9 + ", " + "arg10=" + this.arg10 + ", " + "arg11=" + this.arg11 + ", " + "arg12=" + this.arg12 + ", " + "arg13=" + this.arg13 + ", " + "arg14=" + this.arg14 + ", " + "arg15=" + this.arg15 + ", " + "arg16=" + this.arg16 + "}";
	}
}
