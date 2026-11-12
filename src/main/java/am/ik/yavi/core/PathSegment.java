/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One step of the structured path from the validated root to the value that an
 * {@link Issue} refers to.
 *
 * <p>
 * A path is a list of segments. For example the violation named {@code forms[0].street}
 * has the path {@code [Property("forms"), Index(0), Property("street")]}. Unlike the flat
 * name, the path does not depend on the configurable message key separator and a property
 * name that contains the separator stays a single segment.
 *
 * @since 0.18.0
 * @see ConstraintViolation#path()
 */
public sealed interface PathSegment {

	/**
	 * A step into a named property of an object.
	 *
	 * @param name the property name
	 */
	record Property(String name) implements PathSegment {
	}

	/**
	 * A step into an element of a collection or an array.
	 *
	 * @param index the zero-based position of the element
	 */
	record Index(int index) implements PathSegment {
	}

	/**
	 * Returns the path of a property with the given name directly under the root. An
	 * empty name refers to the root itself and yields an empty path.
	 * @param name the property name
	 * @return a path with a single {@link Property}, or an empty path
	 */
	static List<PathSegment> of(String name) {
		return name.isEmpty() ? List.of() : List.of(new Property(name));
	}

	/**
	 * Concatenates two paths.
	 * @param parent the leading segments
	 * @param child the trailing segments
	 * @return an unmodifiable path consisting of {@code parent} followed by {@code child}
	 */
	static List<PathSegment> concat(List<PathSegment> parent, List<PathSegment> child) {
		if (parent.isEmpty()) {
			return child;
		}
		if (child.isEmpty()) {
			return parent;
		}
		final List<PathSegment> path = new ArrayList<>(parent.size() + child.size());
		path.addAll(parent);
		path.addAll(child);
		return Collections.unmodifiableList(path);
	}

}
