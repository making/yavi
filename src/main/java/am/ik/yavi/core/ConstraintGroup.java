/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Objects;

/**
 * A {@link ConstraintContext} that only carries a name and has no attributes.
 *
 * <p>
 * Constraint groups select constraints by a named group such as {@code CREATE} or
 * {@code UPDATE}. Register a group with
 * {@link am.ik.yavi.builder.ValidatorBuilder#constraintOnGroup(ConstraintGroup, Validator)}
 * and pass the group to a validation method to apply its constraints. Two groups are
 * equal when their names are equal.
 *
 * <p>
 * Example: <pre>{@code
 * ConstraintGroup CREATE = ConstraintGroup.of("CREATE");
 * ConstraintViolations violations = validator.validate(target, CREATE);
 * }</pre>
 *
 * @since 0.0.18
 * @see ConstraintContext
 * @see ConstraintCondition
 */
@FunctionalInterface
public interface ConstraintGroup extends ConstraintContext {

	/**
	 * The default constraint group, named {@code DEFAULT}. It is used when no group is
	 * specified.
	 */
	ConstraintGroup DEFAULT = ConstraintGroup.of("DEFAULT");

	/**
	 * Returns a constraint group with the given name.
	 * @param name the group name
	 * @return a constraint group with the given name
	 */
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

	/**
	 * Returns an attribute that has no value, since a constraint group has no attributes.
	 * @param key the attribute key
	 * @return an attribute whose value is always {@code null}
	 */
	@Override
	default Attribute attribute(String key) {
		return () -> null;
	}

	/**
	 * Returns a condition that is satisfied when the name of the constraint context
	 * passed to the validation equals this group's name.
	 * @param <T> the type of the target object
	 * @return a condition that matches this group
	 */
	default <T> ConstraintCondition<T> toCondition() {
		return (target, group) -> Objects.equals(name(), group.name());
	}

}
