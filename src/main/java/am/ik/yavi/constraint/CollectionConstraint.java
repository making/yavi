package am.ik.yavi.constraint;

import java.util.Collection;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintHolder;

public class CollectionConstraint<T, E>
		extends ContainerConstraintBase<T, Collection<E>, CollectionConstraint<T, E>> {

	@Override
	protected ToIntFunction<Collection<E>> size() {
		return Collection::size;
	}

	@Override
	public CollectionConstraint<T, E> cast() {
		return this;
	}

	public CollectionConstraint<T, E> contains(E s) {
		this.holders()
				.add(new ConstraintHolder<>(x -> x.contains(s), "collection.contains",
						"\"{0}\" must contain {1}", () -> new Object[] { s },
						NULL_IS_VALID));
		return this;
	}
}
