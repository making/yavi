package am.ik.yavi.constraint;

import java.util.Collection;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

public class CollectionConstraint<T, L extends Collection<E>, E>
		extends ContainerConstraintBase<T, L, CollectionConstraint<T, L, E>> {

	@Override
	protected ToIntFunction<L> size() {
		return Collection::size;
	}

	@Override
	public CollectionConstraint<T, L, E> cast() {
		return this;
	}

	public CollectionConstraint<T, L, E> contains(E s) {
		this.predicates()
				.add(new ConstraintPredicate<>(x -> x.contains(s), "collection.contains",
						"\"{0}\" must contain {1}", () -> new Object[] { s },
						NULL_IS_VALID));
		return this;
	}
}
