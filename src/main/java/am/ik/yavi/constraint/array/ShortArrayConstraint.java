package am.ik.yavi.constraint.array;

import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintHolder;

public class ShortArrayConstraint<T>
		extends ContainerConstraintBase<T, short[], ShortArrayConstraint<T>> {

	@Override
	public ShortArrayConstraint<T> cast() {
		return this;
	}

	@Override
	protected ToIntFunction<short[]> size() {
		return x -> x.length;
	}

	public ShortArrayConstraint<T> contains(short v) {
		this.holders().add(new ConstraintHolder<>(x -> {
			for (short e : x) {
				if (e == v) {
					return true;
				}
			}
			return false;
		}, "array.contains", "\"{0}\" must contain {1}", () -> new Object[] { v },
				NULL_IS_VALID));
		return this;
	}
}
