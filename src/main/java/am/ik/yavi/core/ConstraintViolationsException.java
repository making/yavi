package am.ik.yavi.core;

import java.util.stream.Collectors;

/**
 * @since 0.3.0
 */
public class ConstraintViolationsException extends RuntimeException {
	private final ConstraintViolations violations;

	public ConstraintViolationsException(String message,
			ConstraintViolations violations) {
		super(message);
		this.violations = violations;
	}

	public ConstraintViolationsException(ConstraintViolations violations) {
		this("Constraint violations found!" + System.lineSeparator()
				+ violations.violations().stream().map(ConstraintViolation::message)
						.map(s -> "* " + s)
						.collect(Collectors.joining(System.lineSeparator())),
				violations);
	}

	public ConstraintViolations violations() {
		return violations;
	}
}
