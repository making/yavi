/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

public class User {

	private final String email;

	private final String name;

	private final Role role;

	public enum Role {

		USER, ADMIN, GUEST

	}

	public User(String email, String name, Role role) {
		this.email = email;
		this.name = name;
		this.role = role;
	}

	public String email() {
		return email;
	}

	public String name() {
		return name;
	}

	public Role role() {
		return role;
	}

	@Override
	public String toString() {
		return "User{" + "email='" + email + '\'' + ", name='" + name + '\'' + ", role=" + role + '}';
	}

}
