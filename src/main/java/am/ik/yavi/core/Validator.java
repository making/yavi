package am.ik.yavi.core;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import am.ik.yavi.message.MessageFormatter;

public final class Validator<T> {
	final List<ConstraintPredicates<T, ?>> predicatesList;
	private final MessageFormatter messageFormatter;

	public Validator(List<ConstraintPredicates<T, ?>> predicatesList,
			MessageFormatter messageFormatter) {
		this.predicatesList = Collections.unmodifiableList(predicatesList);
		this.messageFormatter = messageFormatter;
	}

	public static <T> ValidatorBuilder<T> builder() {
		return new ValidatorBuilder<>();
	}

	@SuppressWarnings("unchecked")
	public final ConstraintViolations validate(T target) {
		ConstraintViolations violations = new ConstraintViolations();
		for (ConstraintPredicates<T, ?> predicates : this.predicatesList) {
			if (predicates instanceof NestedConstraintPredicates) {
				NestedConstraintPredicates<T, ?, ?> nested = (NestedConstraintPredicates<T, ?, ?>) predicates;
				Object nestedValue = nested.nestedValue(target);
				if (nestedValue == null) {
					continue;
				}
			}
			for (ConstraintPredicate<?> constraintPredicate : predicates.predicates()) {
				Object v = predicates.toValue().apply(target);
				Predicate<Object> predicate = (Predicate<Object>) constraintPredicate
						.predicate();
				if (v == null && constraintPredicate.nullValidity().skipNull()) {
					continue;
				}
				if (!predicate.test(v)) {
					String name = predicates.name();
					Object[] args = constraintPredicate.args().get();
					violations.add(new ConstraintViolation(name,
							constraintPredicate.messageKey(),
							constraintPredicate.defaultMessageFormat(),
							pad(name, args, v), v, this.messageFormatter));
				}
			}
		}
		return violations;
	}

	private Object[] pad(String name, Object[] args, Object value) {
		Object[] pad = new Object[args.length + 2];
		pad[0] = name;
		System.arraycopy(args, 0, pad, 1, args.length);
		pad[pad.length - 1] = value;
		return pad;
	}
}
