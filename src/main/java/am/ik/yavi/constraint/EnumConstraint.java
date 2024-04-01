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
package am.ik.yavi.constraint;

import am.ik.yavi.constraint.base.ConstraintBase;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * class for enum constraints
 * @since 0.14.0
 */
public class EnumConstraint<T, E extends Enum<E>>
		extends ConstraintBase<T, E, EnumConstraint<T, E>> {

	@Override
	public EnumConstraint<T, E> cast() {
		return this;
	}

	@SafeVarargs
	public final EnumConstraint<T, E> oneOf(E... values) throws IllegalArgumentException {
		if (values.length == 0) {
			throw new IllegalArgumentException("oneOf must accept at least one value");
		}

		EnumSet<E> enumSet = EnumSet.copyOf(Arrays.asList(values));

		return super.oneOf(enumSet);
	}
}
