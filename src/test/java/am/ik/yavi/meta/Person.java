/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

import org.jspecify.annotations.Nullable;

public class Person {

	private final @Nullable String firstName;

	private final @Nullable String lastName;

	private final @Nullable Integer age;

	@ConstraintArguments
	public Person(@Nullable String firstName, @Nullable String lastName, @Nullable Integer age) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}

}
