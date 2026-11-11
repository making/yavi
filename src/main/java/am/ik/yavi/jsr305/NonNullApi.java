/*
 * SPDX-FileCopyrightText: 2002-2018 the original author or authors
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.jsr305;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

/**
 * A common YAVI annotation (similar to Spring one) to declare that parameters and return
 * values are to be considered as non-nullable by default for a given package. Leverages
 * JSR 305 meta-annotations to indicate nullability in Java to common tools with JSR 305
 * support and used by Kotlin to infer nullability of YAVI API.
 *
 * <p>
 * Should be used at package level in association with {@link Nullable} annotations at
 * parameter and return value level.
 *
 * @author Sebastien Deleuze
 * @author Juergen Hoeller
 * @see Nullable
 * @see NonNull
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull
@TypeQualifierDefault({ ElementType.METHOD, ElementType.PARAMETER })
public @interface NonNullApi {

}
