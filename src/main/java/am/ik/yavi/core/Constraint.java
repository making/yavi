package am.ik.yavi.core;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public interface Constraint<T, V, C extends Constraint<T, V, C>> {

	List<ConstraintHolder<V>> holders();

	C cast();

	default C notNull() {
		this.holders()
				.add(new ConstraintHolder<>(Objects::nonNull, "object.notNull",
						"\"{0}\" must not be null", () -> new Object[] {},
						NullValidity.NULL_IS_INVALID));
		return this.cast();
	}

	default C isNull() {
		this.holders()
				.add(new ConstraintHolder<>(Objects::isNull, "object.isNull",
						"\"{0}\" must be null", () -> new Object[] {},
						NullValidity.NULL_IS_INVALID));
		return this.cast();
	}

	default C predicate(Predicate<V> predicate, String messageKey,
			String defaultMessageFormat) {
		this.holders().add(new ConstraintHolder<>(predicate, messageKey,
				defaultMessageFormat, () -> new Object[] {}, NullValidity.NULL_IS_VALID));
		return this.cast();
	}

	default C predicateNullable(Predicate<V> predicate, String messageKey,
			String defaultMessageFormat) {
		this.holders()
				.add(new ConstraintHolder<>(predicate, messageKey, defaultMessageFormat,
						() -> new Object[] {}, NullValidity.NULL_IS_INVALID));
		return this.cast();
	}

	default C predicate(CustomConstraint<V> constraint) {
		return this.predicate(constraint.predicate(), constraint.messageKey(),
				constraint.defaultMessageFormat());
	}

	default C predicateNullable(CustomConstraint<V> constraint) {
		return this.predicateNullable(constraint.predicate(), constraint.messageKey(),
				constraint.defaultMessageFormat());
	}
}
