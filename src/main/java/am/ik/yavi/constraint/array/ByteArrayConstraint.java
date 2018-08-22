package am.ik.yavi.constraint.array;

import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

public class ByteArrayConstraint<T>
		extends ContainerConstraintBase<T, byte[], ByteArrayConstraint<T>> {

	@Override
	public ByteArrayConstraint<T> cast() {
		return this;
	}

	@Override
	protected ToIntFunction<byte[]> size() {
		return x -> x.length;
	}

	public ByteArrayConstraint<T> contains(byte v) {
		this.predicates().add(new ConstraintPredicate<>(x -> {
			for (byte e : x) {
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
