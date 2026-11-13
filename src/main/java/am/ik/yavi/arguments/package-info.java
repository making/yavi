/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Argument containers and validators for constructing validated objects from multiple
 * arguments.
 *
 * <p>
 * {@link am.ik.yavi.arguments.Arguments} creates immutable containers of one to sixteen
 * arguments. The generated {@code ArgumentsNValidator} types validate such arguments
 * applicatively and produce a result only when all constraints succeed; all violations
 * are accumulated. A {@code null} argument is accepted unless a constraint such as
 * {@code notNull()} rejects it.
 *
 * <p>
 * Nullness is annotated with JSpecify. An argument given to a validator may be
 * {@code null}, so the parameters of the {@code validate} methods and the components of
 * the containers are {@code @Nullable}. The function that creates the result receives the
 * validated arguments with their declared types and is not obliged to accept
 * {@code null}: an argument is {@code null} only if its constraints accept {@code null}.
 *
 * <p>
 * {@link am.ik.yavi.arguments.ArgumentsValidators} and the {@code split} and
 * {@code combine} methods of a validator compose multiple validators: {@code split}
 * validates distinct arguments with separate validators, while {@code combine} applies
 * several validators to the same input, for example for cross-field validation. The
 * {@code DefaultArgumentsNValidator} types adapt a core {@link am.ik.yavi.core.Validator}
 * to an {@code ArgumentsNValidator}.
 *
 * <p>
 * Typed single-value validators such as {@link am.ik.yavi.arguments.StringValidator} and
 * {@link am.ik.yavi.arguments.IntegerValidator} are special cases for one argument.
 * Validators are usually built with {@link am.ik.yavi.builder.ArgumentsValidatorBuilder}.
 *
 * <p>
 * Example: <pre>{@code
 * Arguments2Validator<String, Integer, Person> validator = ArgumentsValidatorBuilder.of(Person::new)
 *     .builder(b -> b._string(Arguments2::arg1, "name", c -> c.notBlank())
 *         ._integer(Arguments2::arg2, "age", c -> c.greaterThanOrEqual(0)))
 *     .build();
 * Validated<Person> validated = validator.validate("Alice", 20);
 * }</pre>
 */
@NullMarked
package am.ik.yavi.arguments;

import org.jspecify.annotations.NullMarked;
