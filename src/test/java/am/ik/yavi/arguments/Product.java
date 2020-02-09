package am.ik.yavi.arguments;

import am.ik.yavi.builder.ArgumentsValidatorBuilder;

public class Product {
	private final String name;
	private final int price;

	static final Arguments2Validator<String, Integer, Product> validator = ArgumentsValidatorBuilder
			.of(Product::new) //
			.builder(b -> b //
					._string(Arguments1::arg1, "name", c -> c.notEmpty())
					._integer(Arguments2::arg2, "price", c -> c.greaterThan(0)))
			.build();

	public Product(String name, int price) {
		validator.validateAndThrowIfInvalid(name, price);
		this.name = name;
		this.price = price;
	}

}
