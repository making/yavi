/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * The outcome of a validation as a sealed type: either a {@link Success} holding the
 * validated value or a {@link Failure} holding the {@link Issue}s.
 *
 * <p>
 * Unlike {@link Validated}, the two cases are distinct types, so the compiler checks that
 * both are handled and that the value is only read on success:
 *
 * <pre>{@code
 * switch (validator.validate(input).toParseResult()) {
 * 	case ParseResult.Success<User>(var user) -> save(user);
 * 	case ParseResult.Failure<User>(var issues) -> issues.forEach(issue -> report(issue.path(), issue.code()));
 * }
 * }</pre>
 *
 * @param <T> the type of the validated value
 * @since 0.18.0
 * @see Validated#toParseResult()
 */
public sealed interface ParseResult<T extends @Nullable Object> {

	/**
	 * The validation succeeded.
	 *
	 * @param value the validated value
	 * @param <T> the type of the validated value
	 */
	record Success<T extends @Nullable Object> (T value) implements ParseResult<T> {
	}

	/**
	 * The validation failed.
	 *
	 * @param issues the reasons for the failure, in the order they were detected
	 * @param <T> the type of the validated value
	 */
	record Failure<T extends @Nullable Object> (List<Issue> issues) implements ParseResult<T> {

		/**
		 * Creates a failure. The issues are copied.
		 * @throws IllegalArgumentException if {@code issues} is empty
		 */
		public Failure {
			issues = List.copyOf(issues);
			if (issues.isEmpty()) {
				throw new IllegalArgumentException("issues must not be empty");
			}
		}

	}

}
