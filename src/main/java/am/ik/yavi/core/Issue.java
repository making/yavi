/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * A machine-readable description of one reason why a value was rejected.
 *
 * <p>
 * Every issue has a structured {@link #path()} to the rejected value, a stable
 * {@link #code()} and a formatted {@link #message()}. The interface is sealed so that the
 * kinds of issues can be handled exhaustively with pattern matching:
 *
 * <pre>{@code
 * switch (issue) {
 * 	case Issue.Violation(var path, var code, var message, var args) -> ...
 * }
 * }</pre>
 *
 * @since 0.18.0
 * @see ParseResult.Failure
 * @see ConstraintViolation#issue()
 */
public sealed interface Issue {

	/**
	 * Returns the path from the validated root to the rejected value.
	 * @return the path; empty if the issue refers to the root itself
	 */
	List<PathSegment> path();

	/**
	 * Returns the stable identifier of the kind of failure.
	 * @return the issue code
	 */
	String code();

	/**
	 * Returns the formatted, localized message.
	 * @return the message
	 */
	String message();

	/**
	 * A value violated a constraint.
	 *
	 * @param path the path from the validated root to the rejected value
	 * @param code the message key of the violated constraint, for example
	 * {@code charSequence.notBlank}
	 * @param message the formatted, localized message
	 * @param args the message arguments; by convention the first element is the flat name
	 * and the last element is the rejected value, which may be {@code null}
	 */
	record Violation(List<PathSegment> path, String code, String message,
			List<@Nullable Object> args) implements Issue {

		/**
		 * Creates a violation issue. The path and the arguments are copied.
		 */
		public Violation {
			path = List.copyOf(path);
			args = Collections.unmodifiableList(new ArrayList<>(args));
		}

	}

}
