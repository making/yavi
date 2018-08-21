package am.ik.yavi.constraint;

import java.util.Arrays;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.Nullable.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintHolder;

public class ArrayConstraint<T, E>
		extends ContainerConstraintBase<T, E[], ArrayConstraint<T, E>> {

	@Override
	public ArrayConstraint<T, E> cast() {
		return this;
	}

	@Override
	protected ToIntFunction<E[]> size() {
		return x -> x.length;
	}

	public ArrayConstraint<T, E> contains(E s) {
		this.holders()
				.add(new ConstraintHolder<>(x -> Arrays.asList(x).contains(s),
						"array.contains", "\"{0}\" must contain {1}",
						() -> new Object[] { s }, NULL_IS_VALID));
		return this;
	}
}
