package am.ik.yavi.core;

import java.util.List;
import java.util.Objects;

public interface Constraint<T, V, C extends Constraint<T, V, C>> {

	List<ConstraintHolder<V>> holders();

	C cast();

	default C notNull() {
		this.holders()
				.add(new ConstraintHolder<>(Objects::nonNull, "object.notNull",
						"\"{0}\" must not be null", () -> new Object[] {},
						Nullable.NULL_IS_INVALID));
		return this.cast();
	}

	default C isNull() {
		this.holders().add(new ConstraintHolder<>(Objects::isNull, "object.isNull",
				"\"{0}\" must be null", () -> new Object[] {}, Nullable.NULL_IS_INVALID));
		return this.cast();
	}
}
