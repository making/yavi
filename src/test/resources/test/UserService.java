/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

import am.ik.yavi.meta.ConstraintArguments;

public class UserService {
	@ConstraintArguments
	public User createUser(String email, String name) {
		return new User(email, name);
	}
}
