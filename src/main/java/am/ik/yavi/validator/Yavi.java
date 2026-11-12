/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.validator;

import am.ik.yavi.builder.YaviArguments;

/**
 * Entry point for building argument validators.
 *
 * <p>
 * {@link #arguments()} returns a builder that validates the arguments of a constructor or
 * factory method and produces the result only when every argument is valid. All
 * violations are accumulated, so an invalid input reports every failed constraint at
 * once. The following example builds a validator for a constructor with three arguments:
 *
 * <pre>{@code
 * Arguments3Validator<String, String, Integer, Car> validator = Yavi.arguments()
 *     ._string("manufacturer", c -> c.notNull())
 *     ._string("licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
 *     ._integer("seatCount", c -> c.greaterThanOrEqual(2))
 *     .apply(Car::new);
 * Validated<Car> validated = validator.validate("Toyota", "ABC-123", 5);
 * }</pre>
 *
 * <p>
 * Arguments can be composed: {@code _object(name, validator)} nests another validator,
 * {@code _list} and {@code _map} validate elements, and {@code optional()},
 * {@code nullable()} and {@code defaultValue} make the last added argument accept
 * {@code null}. The violations of nested values are located under the argument name, for
 * example {@code address.street} or {@code roles[0]}. A rule that spans several arguments
 * is added to the created validator with {@code constraintOnTarget}:
 *
 * <pre>{@code
 * Arguments4Validator<String, String, AddressForm, List<String>, User> validator = Yavi.arguments()
 *     ._string("email", c -> c.notBlank().email())
 *     ._string("nickname", c -> c.notBlank()).optional()
 *     ._object("address", addressValidator)
 *     ._list("roles", StringValidatorBuilder.of("", c -> c.notBlank()).build())
 *     .apply(User::new)
 *     .constraintOnTarget(user -> !user.roles().isEmpty() || user.nickname().isEmpty(), "roles",
 *         ViolationMessage.of("user.roles", "\"{0}\" must not be empty when a nickname is given"));
 * }</pre>
 *
 * <p>
 * This class only provides the entry point; {@link YaviArguments} and the returned
 * builders do the work.
 *
 * @since 0.14.0
 */
public final class Yavi {

	/**
	 * Returns a new builder for an arguments validator.
	 *
	 * <p>
	 * Each {@code _xxx} method of the returned {@link YaviArguments} adds one argument
	 * with its name and constraints, and {@code apply} creates the validator. For
	 * example:
	 *
	 * <pre>{@code
	 * Arguments2Validator<String, Integer, Person> validator = Yavi.arguments()
	 *     ._string("name", c -> c.notBlank())
	 *     ._integer("age", c -> c.greaterThanOrEqual(0))
	 *     .apply(Person::new);
	 * }</pre>
	 *
	 * <p>
	 * Every call returns a new, empty builder.
	 * @return a new arguments validator builder
	 * @since 0.14.0
	 */
	public static YaviArguments arguments() {
		return new YaviArguments();
	}

	private Yavi() {

	}

}
