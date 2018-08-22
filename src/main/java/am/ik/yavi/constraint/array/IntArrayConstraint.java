package am.ik.yavi.constraint.array;

import java.util.Arrays;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintHolder;

public class IntArrayConstraint<T>
		extends ContainerConstraintBase<T, int[], IntArrayConstraint<T>> {

	@Override
	public IntArrayConstraint<T> cast() {
		return this;
	}

	@Override
	protected ToIntFunction<int[]> size() {
		return x -> x.length;
	}

	public IntArrayConstraint<T> contains(int v) {
		this.holders()
				.add(new ConstraintHolder<>(x -> Arrays.stream(x).anyMatch(e -> e == v),
						"array.contains", "\"{0}\" must contain {1}",
						() -> new Object[] { v }, NULL_IS_VALID));
		return this;
	}
}
