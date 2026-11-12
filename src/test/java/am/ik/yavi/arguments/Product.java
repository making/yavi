/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import am.ik.yavi.core.ConstraintViolationsException;

public class Product {

	private final @Nullable String name;

	private final int price;

	static final Arguments2Validator<String, Integer, Product> validator = ArgumentsValidatorBuilder
		.<String, Integer, Product>of(
				(name, price) -> new Product(Objects.requireNonNull(name), Objects.requireNonNull(price))) //
		.builder(b -> b //
			._string(Arguments2::arg1, "name", c -> c.notEmpty())
			._integer(Arguments2::arg2, "price", c -> c.greaterThan(0)))
		.build();

	public Product(@Nullable String name, int price) {
		validator.lazy().validate(name, price).throwIfInvalid(ConstraintViolationsException::new);
		this.name = name;
		this.price = price;
	}

	@Override
	public String toString() {
		return "Product{" + "name='" + name + '\'' + ", price=" + price + '}';
	}

}
