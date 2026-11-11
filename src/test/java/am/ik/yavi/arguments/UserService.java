/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

public class UserService {

	public User createUser(String email, String name, User.Role role) {
		return new User(email, name, role);
	}

}
