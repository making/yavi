/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
