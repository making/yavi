package am.ik.yavi.constraint.array;

import java.util.Arrays;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

public class DoubleArrayConstraint<T>
		extends ContainerConstraintBase<T, double[], DoubleArrayConstraint<T>> {

	@Override
	public DoubleArrayConstraint<T> cast() {
		return this;
	}

	@Override
	protected ToIntFunction<double[]> size() {
		return x -> x.length;
	}

	public DoubleArrayConstraint<T> contains(double v) {
		this.predicates().add(new ConstraintPredicate<>(
				x -> Arrays.stream(x).anyMatch(e -> e == v), "array.contains",
				"\"{0}\" must contain {1}", () -> new Object[] { v }, NULL_IS_VALID));
		return this;
	}
}
