/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a getter method or a constructor parameter whose value the annotation processor
 * turns into a metadata constant.
 *
 * <p>
 * For a type {@code Foo}, the processor generates {@code _FooMeta} in the same package,
 * with one constant per annotated member. The constants are passed to the
 * {@code constraint(...)} methods of {@link am.ik.yavi.builder.ValidatorBuilder}. The
 * annotation has {@link RetentionPolicy#SOURCE} retention, so it is not present at
 * runtime.
 *
 * @since 0.4.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.SOURCE)
public @interface ConstraintTarget {

	/**
	 * Returns whether the annotated member follows the JavaBean getter convention.
	 *
	 * <p>
	 * When {@code true}, the processor strips the {@code get} or {@code is} prefix of an
	 * annotated method to derive the property name and, for an annotated parameter, looks
	 * up a method such as {@code getName} or {@code isActive}. When {@code false}, the
	 * property name is the member name and the extractor calls a method with the same
	 * name as the property.
	 * @return whether getter naming is assumed
	 * @since 0.4.0
	 */
	boolean getter() default true;

	/**
	 * Returns whether the generated extractor accesses the field directly.
	 *
	 * <p>
	 * When {@code true}, the extractor reads the field with the property name instead of
	 * calling a method. The field must be accessible from the generated class, which is
	 * placed in the same package as the annotated type.
	 * @return whether the field is accessed directly
	 * @since 0.4.0
	 */
	boolean field() default false;

}
