/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

import java.util.function.Function;

/**
 * Metadata for a property, exposed as a constant of a generated meta class.
 *
 * <p>
 * The annotation processor implements this interface anonymously in the {@code _XxxMeta}
 * class generated for {@link ConstraintTarget} or {@link ConstraintArguments}. An
 * application normally passes the generated constants, such as
 * {@code _UserFormMeta.NAME}, to a {@code constraint(...)} method of
 * {@link am.ik.yavi.builder.ValidatorBuilder} instead of implementing this interface.
 *
 * @param <T> the type from which the property value is extracted: a bean type or an
 * {@code am.ik.yavi.arguments.ArgumentsN} container
 * @param <V> the type of the property value
 * @since 0.4.0
 */
public interface ConstraintMeta<T, V> {

	/**
	 * Returns the name of the property.
	 * @return the property name, used as the violation name and as the message argument
	 * {@code {0}}
	 * @since 0.4.0
	 */
	String name();

	/**
	 * Returns a function that extracts the property value from the target.
	 * @return the extractor for the property value
	 * @since 0.4.0
	 */
	Function<T, V> toValue();

}
