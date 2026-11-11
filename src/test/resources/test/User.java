/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package test;

public class User {
	private final String email;
	private final String name;

	public User(String email, String name) {
		this.email = email;
		this.name = name;
	}

	@Override
	public String toString() {
		return "User{" + "email='" + email + '\'' + ", name='" + name + '\'' + '}';
	}
}
