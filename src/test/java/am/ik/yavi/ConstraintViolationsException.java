/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

import am.ik.yavi.core.ConstraintViolations;

public class ConstraintViolationsException extends RuntimeException {

	private final ConstraintViolations violations;

	public ConstraintViolationsException(ConstraintViolations violations) {
		this.violations = violations;
	}

	public ConstraintViolations getViolations() {
		return this.violations;
	}

}
