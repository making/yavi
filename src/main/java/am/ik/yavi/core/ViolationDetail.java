/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Arrays;

/**
 * A structured representation of a {@link ConstraintViolation}.
 *
 * <p>
 * This class is intended to be used for JSON serialization (for example with Jackson).
 * Its getters expose the message id, the message arguments and the formatted message of a
 * violation.
 *
 * @since 0.0.3
 */
public class ViolationDetail {

	private final Object[] args;

	private final String defaultMessage;

	private final String key;

	/**
	 * Creates a new instance.
	 * @param key the message id of the violated constraint
	 * @param args the message arguments
	 * @param defaultMessage the formatted violation message
	 */
	public ViolationDetail(String key, Object[] args, String defaultMessage) {
		this.key = key;
		this.args = args;
		this.defaultMessage = defaultMessage;
	}

	/**
	 * Returns the message arguments of the violation.
	 * @return the message arguments
	 */
	public Object[] getArgs() {
		return args;
	}

	/**
	 * Returns the formatted message of the violation.
	 *
	 * <p>
	 * Despite its name, this is the message in which the arguments have already been
	 * substituted.
	 * @return the formatted message
	 */
	public String getDefaultMessage() {
		return defaultMessage;
	}

	/**
	 * Returns the message id of the violated constraint.
	 * @return the message id
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Returns a string representation of this detail.
	 * @return a string representation
	 */
	@Override
	public String toString() {
		return "ViolationDetail{" + "key='" + key + '\'' + ", args=" + Arrays.toString(args) + ", defaultMessage='"
				+ defaultMessage + '\'' + '}';
	}

}
