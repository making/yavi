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
 * Marks a constructor or method whose arguments the annotation processor turns into
 * metadata constants.
 *
 * <p>
 * For a constructor of {@code Foo}, the processor generates {@code _FooArgumentsMeta};
 * for a method {@code bar} of {@code Foo}, it generates {@code _FooBarArgumentsMeta}, in
 * which the receiver object is the first argument. The constants reference an
 * {@code am.ik.yavi.arguments.ArgumentsN} container and are used with the builder passed
 * to {@link am.ik.yavi.builder.ArgumentsValidatorBuilder#of}. The annotation has
 * {@link RetentionPolicy#SOURCE} retention, so it is not present at runtime.
 *
 * @since 0.4.0
 */
@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface ConstraintArguments {

}
