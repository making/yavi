/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 0.3.0
 */
public class ConstraintViolationsException extends RuntimeException {

	private final ConstraintViolations violations;

	public ConstraintViolationsException(String message, List<ConstraintViolation> violations) {
		super(message);
		this.violations = ConstraintViolations.of(violations);
	}

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

	public ConstraintViolations violations() {
		return violations;
	}

}
