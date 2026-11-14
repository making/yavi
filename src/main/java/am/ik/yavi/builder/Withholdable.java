/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

/**
 * The modifier of an argument read from a key that holds its value back from what shows
 * the values of an object again. The builders returned by {@code Yavi.schema()} and
 * {@code Yavi.reversibleSchema()} have it directly after such an argument is added.
 *
 * <pre>{@code
 * ObjectInputValidator.Reversible<User> userValidator = Yavi.reversibleSchema()
 *     ._integer("id")
 *     ._string("name")
 *     ._string("password_hash").withheld()
 *     .fromMapReversible(User::new, User::id, User::name, User::passwordHash);
 *
 * userValidator.toMap(user); // {id=1, name=Taro, password_hash=...}: the reverse, as an INSERT binds it
 * userValidator.excluding("name").toMap(user); // {id=1}
 * userValidator.including("id", "password_hash").toMap(user); // {id=1, password_hash=...}: it is named
 * }</pre>
 *
 * <p>
 * It is stated once, where the argument is defined, so that the code that shows an object
 * does not have to remember it: a password as it is sent, a token, a hash, or any other
 * value that is read but not shown.
 *
 * @param <B> the type of the builder that the modifier returns, which does not have it
 * @since 0.18.0
 * @see am.ik.yavi.core.ValueDescription.Property#withheld()
 */
public interface Withholdable<B> {

	/**
	 * Holds the value of the last added argument back from what shows the values of an
	 * object again. The argument is read and validated as before, and the validator
	 * describes it as {@linkplain am.ik.yavi.core.ValueDescription.Property#withheld()
	 * withheld}:
	 * <ul>
	 * <li>A {@link am.ik.yavi.input.PropertyView view} writes it only if it is named in
	 * {@code including(...)}: a view that excludes does not write it, at any depth, and
	 * neither does the name of the nested object that has it.
	 * {@link am.ik.yavi.input.ObjectInputValidator.Reversible#toMap(Object) toMap} of the
	 * validator itself is the reverse and writes every property.</li>
	 * <li>{@link am.ik.yavi.input.ParameterTree#toMap(am.ik.yavi.core.ValueDescription)
	 * ParameterTree#toMap} and {@code toParameters} write it as a property that was not
	 * sent, so a form that is shown again does not hold what was sent for it.</li>
	 * <li>Its JSON Schema is {@code writeOnly}.</li>
	 * </ul>
	 *
	 * <p>
	 * The violations of the argument are reported as before, with the rejected value.
	 * This is written directly after the argument is added, and after {@code splitBy} of
	 * a list: the returned builder has the other modifiers of the argument and not this
	 * one.
	 * @return a builder whose last argument is withheld
	 */
	B withheld();

}
