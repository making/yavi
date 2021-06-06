/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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

import java.util.List;
import java.util.function.Function;

import am.ik.yavi.constraint.base.ConstraintBase;
import am.ik.yavi.constraint.password.ObjectPasswordPoliciesBuilder;
import am.ik.yavi.core.ConstraintPredicate;

public class ObjectConstraint<T, E> extends ConstraintBase<T, E, ObjectConstraint<T, E>> {

	@Override
	public ObjectConstraint<T, E> cast() {
		return this;
	}

	/**
	 * @since 0.7.0
	 */
	public ObjectConstraint<T, E> password(
			Function<ObjectPasswordPoliciesBuilder<T, E>, List<ConstraintPredicate<E>>> builder) {
		final List<ConstraintPredicate<E>> predicates = builder
				.apply(new ObjectPasswordPoliciesBuilder<>());
		this.predicates().addAll(predicates);
		return this;
	}
}
