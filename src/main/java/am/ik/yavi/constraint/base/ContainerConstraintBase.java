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
						"container.lessThan", "\"{0}\" must be less than {1}",
						() -> new Object[] { max }, NULL_IS_VALID));
		return cast();
	}

	public C lessThanOrEquals(int max) {
		this.holders()
				.add(new ConstraintHolder<>(x -> size().applyAsInt(x) <= max,
						"container.lessThanOrEquals", "\"{0}\" must be less than {1}",
						() -> new Object[] { max }, NULL_IS_VALID));
		return cast();
	}

	public C greaterThan(int min) {
		this.holders()
				.add(new ConstraintHolder<>(x -> size().applyAsInt(x) > min,
						"container.greaterThan", "\"{0}\" must be greater than {1}",
						() -> new Object[] { min }, NULL_IS_VALID));
		return cast();
	}

	public C greaterThanOrEquals(int min) {
		this.holders().add(new ConstraintHolder<>(x -> size().applyAsInt(x) >= min,
				"container.greaterThanOrEquals", "\"{0}\" must be greater than {1}",
				() -> new Object[] { min }, NULL_IS_VALID));
		return cast();
	}
}
