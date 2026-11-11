/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import am.ik.yavi.jsr305.Nullable;

public class ConstraintPredicate<V> {

	private final Supplier<Object[]> args;

	private final String defaultMessageFormat;

	private final String messageKey;

	private final NullAs nullAs;

	private final Predicate<V> predicate;

	private ConstraintPredicate(Predicate<V> predicate, ViolationMessage violationMessage, Supplier<Object[]> args,
			NullAs nullAs) {
		this(predicate, violationMessage.messageKey(), violationMessage.defaultMessageFormat(), args, nullAs);
	}

	private ConstraintPredicate(Predicate<V> predicate, String messageKey, String defaultMessageFormat,
			Supplier<Object[]> args, NullAs nullAs) {
		this.predicate = predicate;
		this.messageKey = messageKey;
		this.defaultMessageFormat = defaultMessageFormat;
		this.args = args;
		this.nullAs = nullAs;
	}

	public static <V> ConstraintPredicate<V> of(Predicate<V> predicate, ViolationMessage violationMessage,
			Supplier<Object[]> args, NullAs nullAs) {
		return new ConstraintPredicate<>(predicate, violationMessage, args, nullAs);
	}

	public static <V> ConstraintPredicate<V> withViolatedValue(Function<V, Optional<ViolatedValue>> violatedValue,
			ViolationMessage violationMessage, Supplier<Object[]> args, NullAs nullAs) {
		return new ConstraintPredicate<V>(v -> !violatedValue.apply(v).isPresent(), violationMessage, args, nullAs) {
			@Override
			public Optional<ViolatedValue> violatedValue(@Nullable V target) {
				return violatedValue.apply(target);
			}
		};
	}

	public Supplier<Object[]> args() {
		return this.args;
	}

	public final String defaultMessageFormat() {
		return this.defaultMessageFormat;
	}

	public String messageKey() {
		return this.messageKey;
	}

	public final NullAs nullValidity() {
		return this.nullAs;
	}

	public ConstraintPredicate<V> overrideMessage(ViolationMessage message) {
		return new ConstraintPredicate<V>(this.predicate, message, this.args, this.nullAs) {
			@Override
			public Optional<ViolatedValue> violatedValue(@Nullable V target) {
				return ConstraintPredicate.this.violatedValue(target);
			}
		};
	}

	public ConstraintPredicate<V> overrideMessage(String message) {
		return new ConstraintPredicate<V>(this.predicate, this.messageKey, message, this.args, this.nullAs) {
			@Override
			public Optional<ViolatedValue> violatedValue(@Nullable V target) {
				return ConstraintPredicate.this.violatedValue(target);
			}
		};
	}

	public final Predicate<V> predicate() {
		return this.predicate;
	}

	public Optional<ViolatedValue> violatedValue(@Nullable V target) {
		Predicate<V> predicate = this.predicate();
		if (predicate.test(target)) {
			return Optional.empty();
		}
		else {
			// violated
			return Optional.of(new ViolatedValue(target));
		}
	}

}
