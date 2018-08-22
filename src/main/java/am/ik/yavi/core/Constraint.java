package am.ik.yavi.core;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public interface Constraint<T, V, C extends Constraint<T, V, C>> {

	List<ConstraintPredicate<V>> predicates();

	C cast();

	default C notNull() {
		this.predicates()
				.add(new ConstraintPredicate<>(Objects::nonNull, "object.notNull",
						"\"{0}\" must not be null", () -> new Object[] {},
						NullValidity.NULL_IS_INVALID));
		return this.cast();
	}

	default C isNull() {
		this.predicates()
				.add(new ConstraintPredicate<>(Objects::isNull, "object.isNull",
						"\"{0}\" must be null", () -> new Object[] {},
						NullValidity.NULL_IS_INVALID));
		return this.cast();
	}

	default C predicate(Predicate<V> predicate, String messageKey,
			String defaultMessageFormat) {
		this.predicates().add(new ConstraintPredicate<>(predicate, messageKey,
				defaultMessageFormat, () -> new Object[] {}, NullValidity.NULL_IS_VALID));
		return this.cast();
	}

	default C predicateNullable(Predicate<V> predicate, String messageKey,
			String defaultMessageFormat) {
		this.predicates()
				.add(new ConstraintPredicate<>(predicate, messageKey,
						defaultMessageFormat, () -> new Object[] {},
						NullValidity.NULL_IS_INVALID));
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
