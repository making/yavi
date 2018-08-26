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

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import am.ik.yavi.constraint.ViolationMessage;

public class ConstraintPredicate<V> {
	private final Predicate<V> predicate;
	private final String messageKey;
	private final String defaultMessageFormat;
	private final Supplier<Object[]> args;
	private final NullValidity nullValidity;

	private ConstraintPredicate(Predicate<V> predicate, ViolationMessage violationMessage,
			Supplier<Object[]> args, NullValidity nullValidity) {
		this.predicate = predicate;
		this.messageKey = violationMessage.messageKey();
		this.defaultMessageFormat = violationMessage.defaultMessageFormat();
		this.args = args;
		this.nullValidity = nullValidity;
	}

	public final Predicate<V> predicate() {
		return this.predicate;
	}

	public Optional<ViolatedValue> violatedValue(V target) {
		Predicate<V> predicate = this.predicate();
		if (predicate.test(target)) {
			return Optional.empty();
		}
		else {
			// violated
			return Optional.of(new ViolatedValue(target));
		}
	}

	public static <V> ConstraintPredicate<V> of(Predicate<V> predicate,
			ViolationMessage violationMessage, Supplier<Object[]> args,
			NullValidity nullValidity) {
		return new ConstraintPredicate<>(predicate, violationMessage, args, nullValidity);
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
