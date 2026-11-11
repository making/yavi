/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.validator;

import am.ik.yavi.builder.YaviArguments;

/**
 * YAVI Validator Builder
 *
 * @since 0.14.0
 */
public final class Yavi {

	/**
	 * Buider for ArgumentsBuilder<br>
	 * <br>
	 * <strong>Example:</strong>
	 *
	 * <pre>
	 * <code>
	 * Arguments3Validator&lt;String, String, Integer, Car&gt; validator = Yavi.arguments()
	 * 	._string("manufacturer", c -&gt; c.notNull())
	 * 	._string("licensePlate", c -&gt; c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
	 * 	._integer("seatCount", c -&gt; c.greaterThanOrEqual(2))
	 * 	.apply(Car::new);
	 * </code> </pre>
	 * @return {@code YaviArguments} builder
	 */
	public static YaviArguments arguments() {
		return new YaviArguments();
	}

	private Yavi() {

	}

}
