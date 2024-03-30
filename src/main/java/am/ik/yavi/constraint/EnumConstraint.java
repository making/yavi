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

	public EnumConstraint<T, E> oneOf(E... values) throws IllegalArgumentException {
		if (values.length == 0) {
			throw new IllegalArgumentException("oneOf must accept at least one value");
		}

		EnumSet<E> enumSet = EnumSet.copyOf(Arrays.asList(values));

		return super.oneOf(enumSet);
	}
}
