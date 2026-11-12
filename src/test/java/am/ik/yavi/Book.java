/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

import org.jspecify.annotations.Nullable;

public class Book {

	private final @Nullable String isbn;

	public Book(@Nullable String isbn) {
		this.isbn = isbn;
	}

	public @Nullable String isbn() {
		return this.isbn;
	}

}
