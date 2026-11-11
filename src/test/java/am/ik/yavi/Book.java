/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

public class Book {

	private final String isbn;

	public Book(String isbn) {
		this.isbn = isbn;
	}

	public String isbn() {
		return this.isbn;
	}

}
