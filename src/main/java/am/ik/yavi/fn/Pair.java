/*
*Copyright (C) 2018-2019 Toshiaki Maki <makingx@gmail.com>
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
package am.ik.yavi.fn;

import java.util.Objects;

public final class Pair<F, S> {
	private final F first;
	private final S second;

	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	public F first() {
		return this.first;
	}

	public S second() {
		return this.second;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}
}
