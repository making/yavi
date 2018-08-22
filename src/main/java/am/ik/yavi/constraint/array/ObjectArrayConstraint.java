package am.ik.yavi.constraint.array;

import java.util.Arrays;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

public class ObjectArrayConstraint<T, E>
		extends ContainerConstraintBase<T, E[], ObjectArrayConstraint<T, E>> {

	@Override
	public ObjectArrayConstraint<T, E> cast() {
		return this;
	}

	@Override
	protected ToIntFunction<E[]> size() {
		return x -> x.length;
	}

	public ObjectArrayConstraint<T, E> contains(E s) {
		this.predicates()
				.add(new ConstraintPredicate<>(x -> Arrays.asList(x).contains(s),
						"array.contains", "\"{0}\" must contain {1}",
						() -> new Object[] { s }, NULL_IS_VALID));
		return this;
	}
}
