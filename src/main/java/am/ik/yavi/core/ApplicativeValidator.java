/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

/**
 * A {@link ValueValidator} that validates a value of type {@code T} and returns it
 * unchanged when validation succeeds.
 *
 * <p>
 * Instances are obtained from {@link Validatable#applicative()} or by converting a
 * {@link Validatable} with the generated {@code am.ik.yavi.arguments.ArgumentsNValidator}
 * types. The returned {@link Validated} can be combined with other validations in
 * applicative style, for example to build a composite object from several validated
 * components.
 *
 * <p>
 * Example: <pre>{@code
 * ApplicativeValidator<Person> applicative = validator.applicative();
 * Validated<Person> result = applicative.validate(person);
 * }</pre>
 *
 * @param <T> Target class
 * @since 0.6.0
 * @see ValueValidator
 * @see Validated
 */
@FunctionalInterface
public interface ApplicativeValidator<T> extends ValueValidator<T, T> {

}
