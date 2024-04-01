/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	 * </code>
	 * </pre>
	 *
	 * @return {@code YaviArguments} builder
	 */
	public static YaviArguments arguments() {
		return new YaviArguments();
	}

	private Yavi() {

	}
}
