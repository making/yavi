/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link RuntimeException} that carries {@link ConstraintViolations}.
 *
 * <p>
 * This is the exception type used by {@link ConstraintViolations#throwIfInvalid} when a
 * caller wants to turn violations into an exception.
 *
 * @since 0.3.0
 */
public class ConstraintViolationsException extends RuntimeException {

	/** The constraint violations carried by this exception */
	private final ConstraintViolations violations;

	/**
	 * Creates a new instance with the given message and violations.
	 * @param message the exception message
	 * @param violations the constraint violations
	 */
	public ConstraintViolationsException(String message, List<ConstraintViolation> violations) {
		super(message);
		this.violations = ConstraintViolations.of(violations);
	}

	/**
	 * Creates a new instance and derives the exception message from the given violations.
	 *
	 * <p>
	 * The message starts with {@code Constraint violations found!} followed by one
	 * formatted violation message per line, each prefixed with {@code "* "}.
	 * @param violations the constraint violations
	 */
	public ConstraintViolationsException(List<ConstraintViolation> violations) {
		this("Constraint violations found!" + System.lineSeparator()
				+ ConstraintViolations.of(violations)
					.violations()
					.stream()
					.map(ConstraintViolation::message)
					.map(s -> "* " + s)
					.collect(Collectors.joining(System.lineSeparator())),
				violations);
	}

	/**
	 * Returns the constraint violations carried by this exception.
	 * @return the constraint violations
	 */
	public ConstraintViolations violations() {
		return violations;
	}

}
