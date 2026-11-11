/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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
