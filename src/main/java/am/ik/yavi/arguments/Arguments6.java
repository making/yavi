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

import am.ik.yavi.fn.Function6;
import am.ik.yavi.jsr305.Nullable;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.3.0
 */
public class Arguments6<A1, A2, A3, A4, A5, A6> extends Arguments5<A1, A2, A3, A4, A5> {

	protected final A6 arg6;

	Arguments6(@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5,
			@Nullable A6 arg6) {
		super(arg1, arg2, arg3, arg4, arg5);
		this.arg6 = arg6;
	}

	@Nullable
	public final A6 arg6() {
		return this.arg6;
	}

	public final <X> X map(
			Function6<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? super A6, ? extends X> mapper) {
		return mapper.apply(arg1, arg2, arg3, arg4, arg5, arg6);
	}

}
