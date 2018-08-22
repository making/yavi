/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConstraintPredicate<V> {
	private final Predicate<V> predicate;
	private final String messageKey;
	private final String defaultMessageFormat;
	private final Supplier<Object[]> args;
	private final NullValidity nullValidity;

	public ConstraintPredicate(Predicate<V> predicate, String messageKey,
			String defaultMessageFormat, Supplier<Object[]> args,
			NullValidity nullValidity) {
		this.predicate = predicate;
		this.messageKey = messageKey;
		this.defaultMessageFormat = defaultMessageFormat;
		this.args = args;
		this.nullValidity = nullValidity;
	}

	public final Predicate<V> predicate() {
		return this.predicate;
	}

	public String messageKey() {
		return this.messageKey;
	}

	public final String defaultMessageFormat() {
		return this.defaultMessageFormat;
	}

	public Supplier<Object[]> args() {
		return this.args;
	}

	public final NullValidity nullValidity() {
		return this.nullValidity;
	}
}
