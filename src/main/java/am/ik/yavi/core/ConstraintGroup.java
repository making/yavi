/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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

import java.util.Objects;

/**
 * <code>ConstraintGroup</code> is a specialized <code>ConstraintContext</code> with only
 * the name, attributes is empty.
 */
@FunctionalInterface
public interface ConstraintGroup extends ConstraintContext {
	ConstraintGroup DEFAULT = ConstraintGroup.of("DEFAULT");

	static ConstraintGroup of(String name) {
		return new ConstraintGroup() {
			@Override
			public boolean equals(Object obj) {
				if (!(obj instanceof ConstraintGroup)) {
					return false;
				}
				ConstraintGroup cg = (ConstraintGroup) obj;
				return Objects.equals(this.name(), cg.name());
			}

			@Override
			public int hashCode() {
				return Objects.hashCode(this.name());
			}

			@Override
			public String name() {
				return name;
			}
		};
	}

	@Override
	default Attribute attribute(String key) {
		return () -> null;
	}

	default <T> ConstraintCondition<T> toCondition() {
		return (target, group) -> Objects.equals(name(), group.name());
	}
}
