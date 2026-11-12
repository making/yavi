/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

import org.jspecify.annotations.Nullable;

public class User {

	private @Nullable Integer age;

	private @Nullable String email;

	private boolean enabled = true;

	private @Nullable String name;

	public User(@Nullable String name, @Nullable String email, @Nullable Integer age) {
		this.name = name;
		this.email = email;
		this.age = age;
	}

	public @Nullable Integer getAge() {
		return age;
	}

	public void setAge(@Nullable Integer age) {
		this.age = age;
	}

	public @Nullable String getEmail() {
		return email;
	}

	public void setEmail(@Nullable String email) {
		this.email = email;
	}

	public @Nullable String getName() {
		return name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
