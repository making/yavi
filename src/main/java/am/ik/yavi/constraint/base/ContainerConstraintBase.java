package am.ik.yavi.constraint.base;

import java.util.function.ToIntFunction;

import static am.ik.yavi.core.Nullable.NULL_IS_INVALID;
import static am.ik.yavi.core.Nullable.NULL_IS_VALID;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintHolder;

public abstract class ContainerConstraintBase<T, V, C extends Constraint<T, V, C>>
		extends ConstraintBase<T, V, C> {

	abstract protected ToIntFunction<V> size();

	public C notEmpty() {
		this.holders()
				.add(new ConstraintHolder<>(x -> x != null && size().applyAsInt(x) != 0,
						"container.notEmpty", "\"{0}\" must not be empty",
						() -> new Object[] {}, NULL_IS_INVALID));
		return cast();
	}

	public C lessThan(int max) {
		this.holders()
				.add(new ConstraintHolder<>(x -> size().applyAsInt(x) < max,
						"container.lessThan", "The size of \"{0}\" must be less than {1}",
						() -> new Object[] { max }, NULL_IS_VALID));
		return cast();
	}

	public C lessThanOrEquals(int max) {
		this.holders().add(new ConstraintHolder<>(x -> size().applyAsInt(x) <= max,
				"container.lessThanOrEquals", "The size of \"{0}\" must be less than {1}",
				() -> new Object[] { max }, NULL_IS_VALID));
		return cast();
	}

	public C greaterThan(int min) {
		this.holders().add(new ConstraintHolder<>(x -> size().applyAsInt(x) > min,
				"container.greaterThan", "The size of \"{0}\" must be greater than {1}",
				() -> new Object[] { min }, NULL_IS_VALID));
		return cast();
	}

	public C greaterThanOrEquals(int min) {
		this.holders()
				.add(new ConstraintHolder<>(x -> size().applyAsInt(x) >= min,
						"container.greaterThanOrEquals",
						"The size of \"{0}\" must be greater than {1}",
						() -> new Object[] { min }, NULL_IS_VALID));
		return cast();
	}

	public C fixedSize(int size) {
		this.holders()
				.add(new ConstraintHolder<>(x -> size().applyAsInt(x) == size,
						"container.fixedSize", "The size of \"{0}\" must be {1}",
						() -> new Object[] { size }, NULL_IS_VALID));
		return cast();
	}
}
