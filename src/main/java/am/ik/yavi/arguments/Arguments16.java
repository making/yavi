/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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

import am.ik.yavi.fn.Function16;
import am.ik.yavi.jsr305.Nullable;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.3.0
 */
public class Arguments16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16>
		extends Arguments15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15> {

	protected final A16 arg16;

	Arguments16(@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5,
			@Nullable A6 arg6, @Nullable A7 arg7, @Nullable A8 arg8, @Nullable A9 arg9, @Nullable A10 arg10,
			@Nullable A11 arg11, @Nullable A12 arg12, @Nullable A13 arg13, @Nullable A14 arg14, @Nullable A15 arg15,
			@Nullable A16 arg16) {
		super(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15);
		this.arg16 = arg16;
	}

	@Nullable
	public final A16 arg16() {
		return this.arg16;
	}

	public final <X> X map(
			Function16<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? super A7, ? super A8, ? super A9, ? super A10, ? super A11, ? super A12, ? super A13, ? super A14, ? super A15, ? super A16, ? extends X> mapper) {
		return mapper.apply(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14,
				arg15, arg16);
	}

}
