/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public interface Constraint<T, V, C extends Constraint<T, V, C>> {

	List<ConstraintPredicate<V>> predicates();

	C cast();

	default C notNull() {
		this.predicates()
				.add(new ConstraintPredicate<>(Objects::nonNull, "object.notNull",
						"\"{0}\" must not be null", () -> new Object[] {},
						NullValidity.NULL_IS_INVALID));
		return this.cast();
	}

	default C isNull() {
		this.predicates()
				.add(new ConstraintPredicate<>(Objects::isNull, "object.isNull",
						"\"{0}\" must be null", () -> new Object[] {},
						NullValidity.NULL_IS_INVALID));
		return this.cast();
	}

	default C predicate(Predicate<V> predicate, String messageKey,
			String defaultMessageFormat) {
		this.predicates().add(new ConstraintPredicate<>(predicate, messageKey,
				defaultMessageFormat, () -> new Object[] {}, NullValidity.NULL_IS_VALID));
		return this.cast();
	}

	default C predicateNullable(Predicate<V> predicate, String messageKey,
			String defaultMessageFormat) {
		this.predicates()
				.add(new ConstraintPredicate<>(predicate, messageKey,
						defaultMessageFormat, () -> new Object[] {},
						NullValidity.NULL_IS_INVALID));
		return this.cast();
	}

	default C predicate(CustomConstraint<V> constraint) {
		return this.predicate(constraint.predicate(), constraint.messageKey(),
				constraint.defaultMessageFormat());
	}

	default C predicateNullable(CustomConstraint<V> constraint) {
		return this.predicateNullable(constraint.predicate(), constraint.messageKey(),
				constraint.defaultMessageFormat());
	}
}
