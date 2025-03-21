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

import am.ik.yavi.fn.Function5;
import am.ik.yavi.jsr305.Nullable;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.3.0
 */
public class Arguments5<A1, A2, A3, A4, A5> extends Arguments4<A1, A2, A3, A4> {

	protected final A5 arg5;

	Arguments5(@Nullable A1 arg1, @Nullable A2 arg2, @Nullable A3 arg3, @Nullable A4 arg4, @Nullable A5 arg5) {
		super(arg1, arg2, arg3, arg4);
		this.arg5 = arg5;
	}

	@Nullable
	public final A5 arg5() {
		return this.arg5;
	}

	public final <X> X map(Function5<? super A1, ? super A2, ? super A3, ? super A4, ? super A5, ? extends X> mapper) {
		return mapper.apply(arg1, arg2, arg3, arg4, arg5);
	}

}
