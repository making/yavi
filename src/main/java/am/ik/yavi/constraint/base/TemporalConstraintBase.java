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
package am.ik.yavi.constraint.base;

import java.time.temporal.Temporal;
import java.util.function.Predicate;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintPredicate;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_AFTER;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_BEFORE;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_ON_OR_AFTER;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_ON_OR_BEFORE;

public abstract class TemporalConstraintBase<T, V extends Temporal, C extends Constraint<T, V, C>>
		extends ConstraintBase<T, V, C> {

	public C after(V min) {
		this.predicates().add(ConstraintPredicate.of(this.isAfter(min),
				TEMPORAL_AFTER, () -> new Object[]{min}, VALID));
		return cast();
	}

	public C onOrAfter(V min) {
		this.predicates().add(ConstraintPredicate.of(this.isOnOrAfter(min),
				TEMPORAL_ON_OR_AFTER, () -> new Object[]{min}, VALID));
		return cast();
	}

	public C before(V max) {
		this.predicates().add(ConstraintPredicate.of(this.isBefore(max),
				TEMPORAL_BEFORE, () -> new Object[]{max}, VALID));
		return cast();
	}

	public C onOrBefore(V max) {
		this.predicates().add(ConstraintPredicate.of(this.isOnOrBefore(max),
				TEMPORAL_ON_OR_BEFORE, () -> new Object[]{max}, VALID));
		return cast();
	}

	protected abstract Predicate<V> isAfter(V min);

	protected abstract Predicate<V> isOnOrAfter(V min);

	protected abstract Predicate<V> isBefore(V max);

	protected abstract Predicate<V> isOnOrBefore(V max);
}
