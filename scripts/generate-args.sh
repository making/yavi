#!/bin/bash
set -e
n=16

for i in `seq 1 ${n}`;do
  class="Arguments${i}"
  file="$(dirname $0)/../src/main/java/am/ik/yavi/arguments/${class}.java"
  echo $file

  # Create all firstM methods (for N > 1)
  first_methods=""
  if [ ${i} -gt 1 ]; then
    for j in `seq 1 $((${i} - 1))`; do
      # Create type parameters for first method
      first_type_params=""
      for k in `seq 1 ${j}`; do
        first_type_params="${first_type_params}A${k}"
        if [ ${k} -lt ${j} ]; then
          first_type_params="${first_type_params}, "
        fi
      done

      # Create arguments for first method
      first_args=""
      for k in `seq 1 ${j}`; do
        first_args="${first_args}arg${k}"
        if [ ${k} -lt ${j} ]; then
          first_args="${first_args}, "
        fi
      done

      # Add first method
      first_methods+="
	/**
	 * Returns a new {@link Arguments${j}} containing the arguments at positions 1 to
	 * ${j} of this container.
	 *
	 * <p>
	 * The returned container holds the same argument values; no validation is performed.
	 *
	 * @return an Arguments${j} instance with arguments from arg1 to arg${j}
	 * @since 0.16.0
	 */
	public Arguments${j}<${first_type_params}> first${j}() {
		return new Arguments${j}<>(${first_args});
	}
"
    done
  fi

  # Create all lastM methods (for N > 1)
  last_methods=""
  if [ ${i} -gt 1 ]; then
    for j in `seq 1 $((${i} - 1))`; do
      # Calculate starting position for last method
      start_pos=$((${i} - ${j} + 1))

      # Create type parameters for last method
      last_type_params=""
      for k in `seq ${start_pos} ${i}`; do
        last_type_params="${last_type_params}A${k}"
        if [ ${k} -lt ${i} ]; then
          last_type_params="${last_type_params}, "
        fi
      done

      # Create arguments for last method
      last_args=""
      for k in `seq ${start_pos} ${i}`; do
        last_args="${last_args}arg${k}"
        if [ ${k} -lt ${i} ]; then
          last_args="${last_args}, "
        fi
      done

      # Add last method
      last_methods+="
	/**
	 * Returns a new {@link Arguments${j}} containing the arguments at positions
	 * ${start_pos} to ${i} of this container.
	 *
	 * <p>
	 * The returned container holds the same argument values; no validation is performed.
	 *
	 * @return an Arguments${j} instance with arguments from arg${start_pos} to arg${i}
	 * @since 0.16.0
	 */
	public Arguments${j}<${last_type_params}> last${j}() {
		return new Arguments${j}<>(${last_args});
	}
"
    done
  fi

  # Create append method (only for classes less than max size)
  append_method=""
  if [ ${i} -lt ${n} ]; then
    new_size=$((${i} + 1))
    append_method="
	/**
	 * Returns a new {@link Arguments${new_size}} with the given argument appended after
	 * the existing arguments.
	 *
	 * <p>
	 * The existing argument values are copied by reference; no validation is performed.
	 *
	 * @param <B> the type of the argument to append
	 * @param arg the argument to append, possibly {@code null}
	 * @return a new Arguments${new_size} instance with the additional argument
	 * @since 0.16.0
	 */
	public <B> Arguments${new_size}<"

    # Add type parameters for result
    for j in `seq 1 ${i}`; do
      append_method+="A${j}"
      if [ ${j} -lt ${i} ] || [ ${i} -lt ${new_size} ]; then
        append_method+=", "
      fi
    done
    append_method+="B> append(@Nullable B arg) {
		return new Arguments${new_size}<>("

    # Add arguments for result constructor
    for j in `seq 1 ${i}`; do
      append_method+="this.arg${j}"
      if [ ${j} -lt ${i} ] || [ ${i} -lt ${new_size} ]; then
        append_method+=", "
      fi
    done
    append_method+="arg);
	}
"
  fi

  # Create prepend method (only for classes less than max size)
  prepend_method=""
  if [ ${i} -lt ${n} ]; then
    new_size=$((${i} + 1))
    prepend_method="
	/**
	 * Returns a new {@link Arguments${new_size}} with the given argument prepended before
	 * the existing arguments.
	 *
	 * <p>
	 * The existing argument values are copied by reference; no validation is performed.
	 *
	 * @param <B> the type of the argument to prepend
	 * @param arg the argument to prepend, possibly {@code null}
	 * @return a new Arguments${new_size} instance with the additional argument
	 * @since 0.16.0
	 */
	public <B> Arguments${new_size}<B, "

    # Add type parameters for result (already prepended B)
    for j in `seq 1 ${i}`; do
      prepend_method+="A${j}"
      if [ ${j} -lt ${i} ]; then
        prepend_method+=", "
      fi
    done
    prepend_method+="> prepend(@Nullable B arg) {
		return new Arguments${new_size}<>(arg, "

    # Add arguments for result constructor (arg prepended)
    for j in `seq 1 ${i}`; do
      prepend_method+="this.arg${j}"
      if [ ${j} -lt ${i} ]; then
        prepend_method+=", "
      fi
    done
    prepend_method+=");
	}
"
  fi

  # Create reverse method
  reverse_method=""
  if [ ${i} -gt 1 ]; then
    # Create type parameters for reverse method (in reverse order)
    reverse_type_params=""
    for j in `seq ${i} -1 1`; do
      reverse_type_params="${reverse_type_params}A${j}"
      if [ ${j} -gt 1 ]; then
        reverse_type_params="${reverse_type_params}, "
      fi
    done

    # Create arguments for reverse method (in reverse order)
    reverse_args=""
    for j in `seq ${i} -1 1`; do
      reverse_args="${reverse_args}arg${j}"
      if [ ${j} -gt 1 ]; then
        reverse_args="${reverse_args}, "
      fi
    done

    # Add reverse method
    reverse_method="
	/**
	 * Returns a new {@link Arguments${i}} with the arguments in reverse order.
	 *
	 * <p>
	 * The argument values are copied by reference; no validation is performed.
	 *
	 * @return an Arguments${i} instance with arguments in reverse order
	 * @since 0.16.0
	 */
	public Arguments${i}<${reverse_type_params}> reverse() {
		return new Arguments${i}<>(${reverse_args});
	}
"
  fi

  # Create class type parameters
  class_type_params=""
  for j in `seq 1 ${i}`; do
    class_type_params="${class_type_params}A${j}"
    if [ ${j} -lt ${i} ]; then
      class_type_params="${class_type_params}, "
    fi
  done

  # Create JavaDoc param tags for each type parameter
  type_param_docs=""
  for j in `seq 1 ${i}`; do
    type_param_docs="${type_param_docs}
 * @param <A${j}> the type of the argument at position ${j}"
  done

  # Create JavaDoc param tags for each record component
  component_docs=""
  for j in `seq 1 ${i}`; do
    component_docs="${component_docs}
 * @param arg${j} the argument at position ${j}, possibly {@code null}"
  done

  # Create the variables of a record pattern
  pattern_vars=""
  for j in `seq 1 ${i}`; do
    pattern_vars="${pattern_vars}var arg${j}"
    if [ ${j} -lt ${i} ]; then
      pattern_vars="${pattern_vars}, "
    fi
  done

  # Create constructor parameters
  constructor_params=""
  for j in `seq 1 ${i}`; do
    constructor_params="${constructor_params}@Nullable A${j} arg${j}"
    if [ ${j} -lt ${i} ]; then
      constructor_params="${constructor_params}, "
    fi
  done

  # Create function parameters
  function_params=""
  for j in `seq 1 ${i}`; do
    function_params="${function_params}? super A${j}"
    if [ ${j} -lt ${i} ]; then
      function_params="${function_params}, "
    fi
  done

  # Create map function arguments
  map_args=""
  for j in `seq 1 ${i}`; do
    map_args="${map_args}arg${j}"
    if [ ${j} -lt ${i} ]; then
      map_args="${map_args}, "
    fi
  done

  # Build the class definition (no inheritance now)
  class_definition="${class}<${class_type_params}>"

  if [ "${i}" == "1" ]; then
    argument_suffix=""
  else
    argument_suffix="s"
  fi

  cat <<EOF > ${file}
/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import am.ik.yavi.fn.Function${i};
import org.jspecify.annotations.Nullable;

/**
 * An immutable container of ${i} argument${argument_suffix}.
 *
 * <p>
 * Instances are created with {@link Arguments#of} or with the constructor. The
 * {@code wrap()} and {@code unwrap(...)} methods of {@link Arguments${i}Validator}
 * convert between validating the arguments individually and validating this container
 * as a single value. This record only stores, compares and maps the argument values; it
 * performs no validation, and an argument may be {@code null}.
 *
 * <p>
 * As a record it is deconstructed by a record pattern, for example in a {@code switch}
 * over a {@link am.ik.yavi.core.Validated}: <pre>{@code
 * case Validated.Valid(${class}(${pattern_vars})) -> ...
 * }</pre>
 *
 * <p>
 * The {@code first*}, {@code last*}, {@code append}, {@code prepend} and
 * {@code reverse} methods derive another container from the same argument values.
 *
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh${type_param_docs}${component_docs}
 * @since 0.3.0
 */
public record ${class_definition}(${constructor_params}) {

	/**
	 * Applies the given function to every argument contained in this container and
	 * returns its result.
	 *
	 * <p>
	 * No validation is performed and the arguments are passed as they are, possibly
	 * {@code null}.
	 *
	 * @param <X> the type of the result
	 * @param mapper the function to apply to the arguments
	 * @return the result of applying the mapper function to the arguments
	 */
	public <X> X map(Function${i}<${function_params}, ? extends X> mapper) {
		return mapper.apply(${map_args});
	}${first_methods}${last_methods}${append_method}${prepend_method}${reverse_method}
}
EOF
done

cat << EOF > $(dirname $0)/../src/main/java/am/ik/yavi/builder/ArgumentsValidatorBuilder.java
/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

$(for i in `seq 1 ${n}`;do
echo "import am.ik.yavi.arguments.Arguments${i};"
echo "import am.ik.yavi.arguments.Arguments${i}Validator;"
done)
$(for i in `seq 1 ${n}`;do
echo "import am.ik.yavi.arguments.DefaultArguments${i}Validator;"
done)
$(for i in `seq 1 ${n}`;do
echo "import am.ik.yavi.fn.Function${i};"
done)

import java.util.Objects;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

/**
 * Builds arguments validators that validate a fixed number of arguments and map them to
 * a result.
 *
 * <p>
 * {@link #of} takes the function that creates the result (typically a constructor or a
 * method reference) and returns the builder for the matching number of arguments. The
 * validation rules are then added with the {@link ValidatorBuilder} of the wrapped
 * arguments object. For example: <pre>{@code
 * Arguments3Validator<String, String, Integer, User> validator = ArgumentsValidatorBuilder
 *     .of(User::new)
 *     .builder(b -> b._string(Arguments3::arg1, "name", c -> c.notNull())
 *         ._string(Arguments3::arg2, "email", c -> c.email())
 *         ._integer(Arguments3::arg3, "age", c -> c.notNull()))
 *     .build();
 * }</pre>
 *
 * <p>
 * The built validator validates all arguments and reports a violation for each invalid
 * argument. The mapper is applied only when no violation exists.
 *
 * <p>
 * The source of this class is generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.3.0
 */
public final class ArgumentsValidatorBuilder {

	/**
	 * Creates a new builder.
	 *
	 * <p>
	 * Builders are normally obtained with the static {@link #of} methods.
	 * @since 0.3.0
	 */
	public ArgumentsValidatorBuilder() {
	}
$(for i in `seq 1 ${n}`;do
  cat <<EOJ
	/**
	 * Returns a builder for a validator that validates the arguments and maps them to
	 * the result with the given function.
	 *
	 * <p>
	 * The function is applied only after all arguments have been validated.
	 * @param <A1> type of the first argument$(for j in `seq 1 ${i}`;do if [ "${j}" -gt 1 ];then printf '\n\t * @param <A%s> type of argument at position %s' "${j}" "${j}";fi;done)
	 * @param <X> type of the validated result
	 * @param mapper function that creates the result from the validated arguments; must
	 * not be {@code null}
	 * @return a new builder
	 * @throws NullPointerException if {@code mapper} is {@code null}
	 * @since 0.3.0
	 */
	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> Arguments${i}ValidatorBuilder<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> of(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends X> mapper) {
		return new Arguments${i}ValidatorBuilder<>(mapper);
	}
EOJ
done)

$(for i in `seq 1 ${n}`;do
  class="Arguments${i}ValidatorBuilder"
  arguments="${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X>"
  cat <<EOJ
	/**
	 * Builds an {@link Arguments${i}Validator} that validates the arguments and maps
	 * them to the result with the function given to
	 * {@link ArgumentsValidatorBuilder#of}.
	 * @param <A1> type of the first argument$(for j in `seq 1 ${i}`;do if [ "${j}" -gt 1 ];then printf '\n\t * @param <A%s> type of argument at position %s' "${j}" "${j}";fi;done)
	 * @param <X> type of the validated result
	 * @since 0.3.0
	 */
	public static final class ${arguments} {
		private final Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends X> mapper;
		private @Nullable ValidatorBuilder<Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>> builder;

		/**
		 * Creates a builder that maps the validated arguments with the given function.
		 * @param mapper function that creates the result from the validated arguments;
		 * must not be {@code null}
		 * @throws NullPointerException if {@code mapper} is {@code null}
		 * @since 0.3.0
		 */
		public ${class}(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		/**
		 * Sets the validation rules of the arguments with the given function that
		 * configures a new {@link ValidatorBuilder} for {@link Arguments${i}}.
		 *
		 * <p>
		 * Calling this method replaces any rules set previously.
		 * @param definition function that configures the validator builder; must not be
		 * {@code null}
		 * @return this builder
		 * @throws NullPointerException if {@code definition} is {@code null}
		 * @since 0.3.0
		 */
		public ${arguments} builder(
				Function<? super ValidatorBuilder<Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>>, ? extends ValidatorBuilder<Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}

		/**
		 * Builds the validator from the configured rules and the mapper.
		 * @return a new arguments validator
		 * @throws NullPointerException if no rules were set with {@link #builder}
		 * @since 0.3.0
		 */
		public Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> build() {
			return new DefaultArguments${i}Validator<>(Objects.requireNonNull(this.builder, "no rules were set with builder(...)").build(), this.mapper);
		}
	}

EOJ
done)
}
EOF

for i in `seq 1 ${n}`;do
  # Arguments1Validator is a ValueValidator, so its wrappers carry the untyped view
  untyped_map=""
  untyped_and_then=""
  untyped_described_as=""
  if [ "${i}" == "1" ];then
    untyped_map="
			@Override
			public <N> ValueValidator<N, X2> readingWith(InputReader<N> reader) {
				return Arguments1Validator.this.readingWith(reader).map(mapper);
			}

			@Override
			public Arguments1Validator<A1, X2> preprocess(Function<? super A1, ? extends @Nullable A1> transform) {
				return Arguments1Validator.this.preprocess(transform).map(mapper);
			}"
    untyped_and_then="
			@Override
			public <N> ValueValidator<N, X2> readingWith(InputReader<N> reader) {
				return Arguments1Validator.this.readingWith(reader).andThen(validator);
			}

			@Override
			public Arguments1Validator<A1, X2> preprocess(Function<? super A1, ? extends @Nullable A1> transform) {
				return Arguments1Validator.this.preprocess(transform).andThen(validator);
			}"
    untyped_described_as="
			@Override
			public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
				return Arguments1Validator.this.readingWith(reader).describedAs(description);
			}

			@Override
			public Arguments1Validator<A1, X> preprocess(Function<? super A1, ? extends @Nullable A1> transform) {
				return Arguments1Validator.this.preprocess(transform).describedAs(description);
			}"
  fi
  class="Arguments${i}Validator"
  file="$(dirname $0)/../src/main/java/am/ik/yavi/arguments/${class}.java"
  arguments="Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? extends A${j}, ";done) | sed 's/,$//')>"
  wrapArguments="Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>"
  args="$(echo $(for j in `seq 1 ${i}`;do echo -n "@Nullable A${j} a${j}, ";done) | sed 's/,$//')"
  as="$(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//')"
  if [ "${i}" == "1" ]; then
    argument_suffix=""
  else
    argument_suffix="s"
  fi
  echo $file
  cat <<EOF > ${file}
/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

$(if [ "${i}" != "1" ];then
cat <<EOD
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import org.jspecify.annotations.Nullable;
EOD
fi)
$(if [ "${i}" == "1" ];then
cat <<EOD
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.Validatable;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import org.jspecify.annotations.Nullable;
EOD
fi)

/**
 * Validates ${i} argument${argument_suffix} and produces a value of type {@code X}.
 *
 * <p>
 * Validation is applicative: all violations are accumulated in the returned
 * {@link Validated} instead of stopping at the first one, and a value is produced only
 * when every constraint succeeds. A {@code null} argument is accepted unless a configured
 * constraint rejects it (for example {@code notNull()}); see
 * {@link am.ik.yavi.core.NullAs}.
 *
 * <p>
 * Instances are usually created with
 * {@link am.ik.yavi.builder.ArgumentsValidatorBuilder#of} or a typed builder, and with
 * the factories of {@link ArgumentsValidators}. The default methods adapt the input type
 * with {@link #compose}, transform the validated value with {@link #map}, and add more
 * validators through the splitting and combining factories.
 *
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
$(for j in `seq 1 ${i}`; do echo " * @param <A${j}> the type of the argument at position ${j}"; done)
 * @param <X> the type of the value produced when validation succeeds
 * @since 0.3.0
 */
@FunctionalInterface
$(if [ "${i}" == "1" ];then
cat <<EOD
public interface ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> extends ValueValidator<A1, X> {

	/**
	 * Returns an {@link Arguments1Validator} that validates the argument with the given
	 * {@link Validatable} and returns the validated object.
	 *
	 * @param validator the core validator
	 * @param <X> the type of the object to validate
	 * @return an arguments1 validator for the given validatable
	 * @since 0.8.0
	 */
	static <X> Arguments1Validator<X, X> from(Validatable<X> validator) {
		return Arguments1Validator.from(validator.applicative());
	}

	/**
	 * Returns an {@link Arguments1Validator} that delegates to the given
	 * {@link ValueValidator} and keeps its description.
	 *
	 * @param valueValidator the value validator to delegate to
	 * @param <A1> the type of the argument
	 * @param <X> the type of the value produced on success
	 * @return an arguments1 validator for the given value validator
	 * @since 0.8.0
	 */
	static <A1, X> Arguments1Validator<A1, X> from(ValueValidator<A1, X> valueValidator) {
		return new Arguments1Validator<A1, X>() {
			@Override
			public Validated<X> validate(@Nullable A1 a1, Locale locale, ConstraintContext constraintContext) {
				return valueValidator.validate(a1, locale, constraintContext);
			}

			@Override
			public ValueDescription description() {
				return valueValidator.description();
			}

			@Override
			public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
				return valueValidator.readingWith(reader);
			}

			@Override
			public Arguments1Validator<A1, X> preprocess(Function<? super A1, ? extends @Nullable A1> transform) {
				return Arguments1Validator.from(valueValidator.preprocess(transform));
			}
		};
	}

	/**
	 * Returns an {@link Arguments1Validator} that takes the argument directly instead of
	 * an {@link Arguments1} container.
	 *
	 * <p>
	 * The returned validator wraps the argument with {@link Arguments#of} before
	 * delegating to the given validator.
	 *
	 * @param validator a validator for {@code Arguments1<A1>}
	 * @param <A1> the type of the argument
	 * @param <X> the type of the value produced on success
	 * @return arguments1 validator that takes an A1 directly
	 * @since 0.16.0
	 */
	static <A1, X> Arguments1Validator<A1, X> unwrap(Arguments1Validator<Arguments1<A1>, X> validator) {
		return new Arguments1Validator<A1, X>() {
			@Override
			public Validated<X> validate(@Nullable A1 a1, Locale locale, ConstraintContext constraintContext) {
				return validator.validate(Arguments.of(a1), locale, constraintContext);
			}

			@Override
			public Arguments1Validator<A1, Supplier<X>> lazy() {
				return Arguments1Validator.unwrap(validator.lazy());
			}

			@Override
			public ValueDescription description() {
				return validator.description();
			}
		};
	}
EOD
else
cat <<EOD
public interface ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> {

	/**
	 * Returns an {@link Arguments${i}Validator} that takes the arguments directly
	 * instead of an {@link Arguments${i}} container.
	 *
	 * <p>
	 * The returned validator wraps the arguments with {@link Arguments#of} before
	 * delegating to the given validator.
	 *
	 * @param validator a validator for {@code Arguments${i}} and its argument types
$(for j in `seq 1 ${i}`; do echo "	 * @param <A${j}> the type of the argument at position ${j}"; done)
	 * @param <X> the type of the value produced on success
	 * @return arguments${i} validator that takes arguments directly
	 * @since 0.16.0
	 */
	static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> unwrap(
			Arguments1Validator<${wrapArguments}, X> validator) {
		return new ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X>() {
			@Override
			public Validated<X> validate(${args}, Locale locale, ConstraintContext constraintContext) {
				return validator.validate(Arguments.of(${as}), locale, constraintContext);
			}

			@Override
			public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X>> lazy() {
				return ${class}.unwrap(validator.lazy());
			}

			@Override
			public ValueDescription description() {
				return validator.description();
			}
		};
	}
EOD
fi)

$(if [ "${i}" == "1" ];then
cat <<EOD
	/**
	 * Validates the argument and returns the validated value or the accumulated
	 * violations.
	 *
	 * @param a1 the argument to validate, possibly {@code null}
	 * @param locale the locale targeted for the violation messages
	 * @param constraintContext constraint context used by conditional constraints
	 * @return the validated value or the constraint violations
	 */
	@Override
	Validated<X> validate(${args},
			Locale locale, ConstraintContext constraintContext);
EOD
fi)
$(if [ "${i}" != "1" ];then
cat <<EOD
	/**
	 * Validates the arguments and returns the validated value or the accumulated
	 * violations.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param a${j} the argument at position ${j}, possibly {@code null}"; done)
	 * @param locale the locale targeted for the violation messages
	 * @param constraintContext constraint context used by conditional constraints
	 * @return the validated value or the constraint violations
	 */
	Validated<X> validate(${args},
			Locale locale, ConstraintContext constraintContext);
EOD
fi)

	/**
	 * Returns a validator that takes a single {@link Arguments${i}} and validates the
	 * arguments it contains.
	 *
	 * <p>
	 * This is the inverse of {@link #unwrap}. A {@code null} container is rejected with a
	 * {@link NullPointerException} when the returned validator is applied.
	 *
	 * @return a validator that takes an Arguments${i}
	 * @since 0.16.0
	 */
	default Arguments1Validator<${wrapArguments}, X> wrap() {
		return new Arguments1Validator<${wrapArguments}, X>() {
			@Override
			public Validated<X> validate(@Nullable ${wrapArguments} args, Locale locale,
					ConstraintContext constraintContext) {
				final ${arguments} nonNullArgs = Objects.requireNonNull(args);
				return ${class}.this.validate($(echo $(for j in `seq 1 ${i}`;do echo -n "nonNullArgs.arg${j}(), ";done) | sed 's/,$//'),
						locale, constraintContext);
			}

			@Override
			public Arguments1Validator<${wrapArguments}, Supplier<X>> lazy() {
				return ${class}.this.lazy().wrap();
			}

			@Override
			public ValueDescription description() {
				return ${class}.this.description();
			}
		};
	}

	/**
	 * Returns a validator that maps the validated value with the given function.
	 *
	 * @param mapper the function applied to the validated value
	 * @param <X2> the type of the value produced after mapping
	 * @return a validator with the mapped result type
	 * @deprecated Use {@link #map(Function)} instead.
	 * @since 0.7.0
	 */$(if [ "${i}" == "1" ];then echo;echo "	@Override"; fi)
	@Deprecated
	default <X2> ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X2> andThen(Function<? super X, ? extends X2> mapper) {
		return this.map(mapper);
	}

	/**
	 * Returns a validator that maps the value produced on success with the given
	 * function. The function is not applied when validation fails.
	 *
	 * @param mapper the function applied to the validated value
	 * @param <X2> the type of the value produced after mapping
	 * @return a validator with the mapped result type
	 * @since 0.17.0
	 */$(if [ "${i}" == "1" ];then echo;echo "	@Override"; fi)
	default <X2> ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X2> map(Function<? super X, ? extends @Nullable X2> mapper) {
		return new ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X2>() {
			@Override
			public Validated<X2> validate($(echo $(for j in `seq 1 ${i}`;do echo -n "@Nullable A${j} a${j}, ";done) | sed 's/,$//'), Locale locale, ConstraintContext constraintContext) {
				return ${class}.this.validate(${as}, locale, constraintContext).map(mapper);
			}

			@Override
			public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X2>> lazy() {
				return ${class}.this.lazy()
					.map((Function<Supplier<X>, Supplier<X2>>) xSupplier -> () -> mapper.apply(xSupplier.get()));
			}

			@Override
			public ValueDescription description() {
				return ${class}.this.description();
			}
${untyped_map}
		};
	}

	/**
	 * Returns a validator that passes the value produced on success to the given
	 * validator.
	 *
	 * <p>
	 * If this validator fails, the given validator is not invoked and its violations are
	 * not collected.
	 *
	 * @param validator the validator applied to the validated value
	 * @param <X2> the type of the value produced by the given validator
	 * @return a chained validator
	 * @since 0.11.0
	 */$(if [ "${i}" == "1" ];then echo;echo "	@Override"; fi)
	default <X2> ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X2> andThen(ValueValidator<? super X, X2> validator) {
		return new ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X2>() {
			@Override
			public Validated<X2> validate($(echo $(for j in `seq 1 ${i}`;do echo -n "@Nullable A${j} a${j}, ";done) | sed 's/,$//'), Locale locale, ConstraintContext constraintContext) {
				return ${class}.this.validate(${as}, locale, constraintContext)
					.flatMap(v -> validator.validate(v, locale, constraintContext));
			}

			@Override
			public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X2>> lazy() {
				return ${class}.this.lazy().andThen((xSupplier, locale, constraintContext) -> validator
					.validate(Objects.requireNonNull(xSupplier).get(), locale, constraintContext)
					.map(x2 -> () -> x2));
			}

			@Override
			public ValueDescription description() {
				return ${class}.this.description();
			}
${untyped_and_then}
		};
	}

	/**
	 * Returns a validator that additionally checks the value produced by this validator
	 * with the given predicate. This is the place for a rule that spans several
	 * arguments, such as a password confirmation or a date range.
	 *
	 * <p>
	 * The predicate is evaluated only when this validator succeeds. If it returns
	 * {@code false}, a violation with the given name and message is reported; its last
	 * message argument is the produced value.
	 *
	 * @param predicate the rule that the produced value must satisfy
	 * @param name the name of the reported violation
	 * @param violationMessage the message key and the default message format of the
	 * reported violation
	 * @return a validator with the additional rule
	 * @since 0.18.0
	 */$(if [ "${i}" == "1" ];then echo;echo "	@Override"; fi)
	default ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> constraintOnTarget(Predicate<? super X> predicate, String name,
			ViolationMessage violationMessage) {
		return this.andThen(ValueValidator.<X>passThrough().constraintOnTarget(predicate, name, violationMessage));
	}

	/**
	 * Returns a validator that converts its input with the given function before
	 * validating it.
	 *
	 * <p>
	 * The function is not validated; its result is passed to this validator.
$(if [ "${i}" == "1" ];then
cat <<EOD
	 * The {@linkplain #description() description} of the new validator is unknown
	 * because the input changes.
EOD
else
cat <<EOD
	 * The new validator keeps the {@linkplain #description() description} of this
	 * validator: its input is the object that provides the arguments.
EOD
fi)
	 *
	 * @param mapper the function that converts the input of the new validator
	 * @param <A> the input type of the new validator
	 * @return a validator with the adapted input type
	 * @since 0.7.0
	 */
$(if [ "${i}" == "1" ];then
cat <<EOD
	@Override
	default <A extends @Nullable Object> Arguments1Validator<A, X> compose(Function<@Nullable A, ? extends @Nullable A1> mapper) {
		return new Arguments1Validator<A, X>() {
			@Override
			public Validated<X> validate(@Nullable A a, Locale locale, ConstraintContext constraintContext) {
				return Arguments1Validator.this.validate(mapper.apply(a), locale, constraintContext);
			}

			@Override
			public Arguments1Validator<A, Supplier<X>> lazy() {
				return Arguments1Validator.this.lazy().compose(mapper);
			}
		};
	}
EOD
else
cat <<EOD
	default <A extends @Nullable Object> Arguments1Validator<A, X> compose(
			Function<@Nullable A, ? extends @Nullable ${arguments}> mapper) {
		return new Arguments1Validator<A, X>() {
			@Override
			public Validated<X> validate(@Nullable A a, Locale locale, ConstraintContext constraintContext) {
				final ${arguments} args = mapper.apply(a);
				return ${class}.this.validate($(echo $(for j in `seq 1 ${i}`;do echo -n "args.arg${j}(), ";done) | sed 's/,$//'), locale, constraintContext);
			}

			@Override
			public Arguments1Validator<A, Supplier<X>> lazy() {
				return ${class}.this.lazy().compose(mapper);
			}

			@Override
			public ValueDescription description() {
				return ${class}.this.description();
			}
		};
	}
EOD
fi)
$(if [ "${i}" != "1" ];then
cat <<EOD
	/**
	 * Returns a description of the arguments that this validator expects.
	 *
	 * <p>
	 * A validator created by an {@code ArgumentsNValidatorBuilder} is described as
	 * {@link ValueDescription.Properties} with one property per argument. The default
	 * implementation returns {@link ValueDescription.Unknown}. {@link #map},
	 * {@link #andThen(ValueValidator)}, {@link #constraintOnTarget}, {@link #wrap} and
	 * {@link #compose} keep the description.
	 *
	 * @return the description of the arguments
	 * @since 0.18.0
	 * @see #describedAs(ValueDescription)
	 */
	default ValueDescription description() {
		return new ValueDescription.Unknown();
	}

EOD
fi)
	/**
	 * Returns a validator that validates like this validator and describes its
	 * argument${argument_suffix} with the given description.
	 *
	 * @param description the description of the argument${argument_suffix}
	 * @return a validator with the given description
	 * @since 0.18.0
	 * @see #description()
	 */$(if [ "${i}" == "1" ];then echo;echo "	@Override"; fi)
	default ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> describedAs(ValueDescription description) {
		return new ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X>() {
			@Override
			public Validated<X> validate(${args}, Locale locale, ConstraintContext constraintContext) {
				return ${class}.this.validate(${as}, locale, constraintContext);
			}

			@Override
			public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X>> lazy() {
				return ${class}.this.lazy().describedAs(description);
			}

			@Override
			public ValueDescription description() {
				return description;
			}
${untyped_described_as}
		};
	}

$(if [ "${i}" == "1" ];then
cat <<EOD
	/**
	 * Returns a validator that applies the given function to an argument other than
	 * {@code null} and validates the result with this validator, as
	 * {@link ValueValidator#preprocess(Function)} states. The description is kept.
	 *
	 * @param transform the function applied to an argument other than {@code null}
	 * @return a validator that transforms its argument first
	 * @since 0.18.0
	 */
	@Override
	default Arguments1Validator<A1, X> preprocess(Function<? super A1, ? extends @Nullable A1> transform) {
		return new Arguments1Validator<A1, X>() {
			@Override
			public Validated<X> validate(@Nullable A1 a1, Locale locale, ConstraintContext constraintContext) {
				return Arguments1Validator.this.validate(a1 == null ? null : transform.apply(a1), locale,
						constraintContext);
			}

			@Override
			public Arguments1Validator<A1, Supplier<X>> lazy() {
				return Arguments1Validator.this.lazy().preprocess(transform);
			}

			@Override
			public ValueDescription description() {
				return Arguments1Validator.this.description();
			}
		};
	}

EOD
fi)
$(cat <<EOD
	/**
	 * Returns a validator that produces a {@link Supplier} of the validated value.
	 *
	 * <p>
	 * The supplier is created only when validation succeeds; obtaining the value from it
	 * applies the mapper at that time. The default implementation throws
	 * {@link UnsupportedOperationException}; concrete validators override it.
	 *
	 * @return a validator that produces a supplier of the validated value
	 * @since 0.10.0
	 */
	default ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X>> lazy() {
		throw new UnsupportedOperationException("lazy is not implemented!");
	}

	/**
	 * Validates the arguments with the default locale and
	 * {@link ConstraintGroup#DEFAULT}.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param a${j} the argument at position ${j}, possibly {@code null}"; done)
	 * @return the validated value or the constraint violations
	 */
	default Validated<X> validate(${args}) {
		return this.validate(${as}, Locale.getDefault(), ConstraintGroup.DEFAULT);
	}

	/**
	 * Validates the arguments with the given constraint context and the default locale.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param a${j} the argument at position ${j}, possibly {@code null}"; done)
	 * @param constraintContext constraint context used by conditional constraints
	 * @return the validated value or the constraint violations
	 */
	default Validated<X> validate(${args}, ConstraintContext constraintContext) {
		return this.validate(${as}, Locale.getDefault(), constraintContext);
	}

	/**
	 * Validates the arguments with the given locale and
	 * {@link ConstraintGroup#DEFAULT}.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param a${j} the argument at position ${j}, possibly {@code null}"; done)
	 * @param locale the locale targeted for the violation messages
	 * @return the validated value or the constraint violations
	 */
	default Validated<X> validate(${args}, Locale locale) {
		return this.validate(${as}, locale, ConstraintGroup.DEFAULT);
	}

	/**
	 * Validates the arguments with the default locale and
	 * {@link ConstraintGroup#DEFAULT} and returns the validated value.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param a${j} the argument at position ${j}, possibly {@code null}"; done)
	 * @return the validated value
	 * @throws ConstraintViolationsException if validation fails
	 */
	default X validated(${args}) throws ConstraintViolationsException {
		return this.validate(${as}).orElseThrow(ConstraintViolationsException::new);
	}

	/**
	 * Validates the arguments with the given constraint context and the default locale
	 * and returns the validated value.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param a${j} the argument at position ${j}, possibly {@code null}"; done)
	 * @param constraintContext constraint context used by conditional constraints
	 * @return the validated value
	 * @throws ConstraintViolationsException if validation fails
	 */
	default X validated(${args}, ConstraintContext constraintContext)
			throws ConstraintViolationsException {
		return this.validate(${as}, constraintContext)
				.orElseThrow(ConstraintViolationsException::new);
	}

	/**
	 * Validates the arguments with the given locale and
	 * {@link ConstraintGroup#DEFAULT} and returns the validated value.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param a${j} the argument at position ${j}, possibly {@code null}"; done)
	 * @param locale the locale targeted for the violation messages
	 * @return the validated value
	 * @throws ConstraintViolationsException if validation fails
	 */
	default X validated(${args}, Locale locale) throws ConstraintViolationsException {
		return this.validate(${as}, locale).orElseThrow(ConstraintViolationsException::new);
	}

	/**
	 * Validates the arguments with the given locale and constraint context and returns
	 * the validated value.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param a${j} the argument at position ${j}, possibly {@code null}"; done)
	 * @param locale the locale targeted for the violation messages
	 * @param constraintContext constraint context used by conditional constraints
	 * @return the validated value
	 * @throws ConstraintViolationsException if validation fails
	 */
	default X validated(${args}, Locale locale, ConstraintContext constraintContext)
			throws ConstraintViolationsException {
		return this.validate(${as}, locale, constraintContext)
				.orElseThrow(ConstraintViolationsException::new);
	}
EOD)
$(if [ "${i}" == "1" ];then
cat <<EOD
	/**
	 * Returns an {@link Arguments2Splitting} that validates this argument and the
	 * argument accepted by the given validator independently.
	 *
	 * <p>
	 * Call {@link Arguments2Splitting#apply} on the returned instance to create a
	 * validator that combines the validated values.
	 *
	 * @param validator the validator applied to the second argument
	 * @param <A2> the type of the second argument
	 * @param <Y> the type of the value produced by the given validator
	 * @return an arguments2 splitting instance
	 * @since 0.7.0
	 */
	default <A$((${i} + 1)), Y> Arguments$((${i} + 1))Splitting<$(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "A${j}, ";done) | sed 's/,$//'), X, Y> split(ValueValidator<A$((${i} + 1)), Y> validator) {
		return new Arguments$((${i} + 1))Splitting<>(this, validator);
	}

	/**
	 * Returns an {@link Arguments2Combining} that validates the same argument with this
	 * validator and with the given validator.
	 *
	 * <p>
	 * Call {@link Arguments2Combining#apply} on the returned instance to create a
	 * validator that combines the validated values.
	 *
	 * @param validator the additional validator applied to the same argument
	 * @param <Y> the type of the value produced by the given validator
	 * @return an arguments2 combining instance
	 * @since 0.7.0
	 */
	default <Y> Arguments$((${i} + 1))Combining<A${i}, X, Y> combine(ValueValidator<A${i}, Y> validator) {
		return new Arguments$((${i} + 1))Combining<>(this, validator);
	}

	/**
	 * Returns a validator that validates every element of the input iterable with this
	 * validator and collects the validated values into a collection created by the given
	 * factory. The violations of an element are located under its index ({@code [0]},
	 * {@code [0].name}) and the violations of all failing elements are accumulated.
	 * @param factory supplier of the collection that receives the validated values
	 * @param <C> the type of the resulting collection
	 * @return a validator for iterables
	 * @since 0.8.0
	 */
	default <C extends Collection<X>> Arguments1Validator<Iterable<A1>, C> liftCollection(
			Supplier<C> factory) {
		return Arguments1Validator.from(ValueValidator.super.liftCollection(factory));
	}

	/**
	 * Returns a validator that validates every element of the input iterable with this
	 * validator and collects the validated values into a {@link List}. The violations of
	 * an element are located under its index ({@code [0]}, {@code [0].name}) and the
	 * violations of all failing elements are accumulated.
	 * @return a validator for iterables
	 * @since 0.8.0
	 */
	default Arguments1Validator<Iterable<A1>, List<X>> liftList() {
		return Arguments1Validator.from(ValueValidator.super.liftList());
	}

	/**
	 * Returns a validator that validates every element of the input iterable with this
	 * validator and collects the validated values into a {@link Set} that preserves the
	 * iteration order. The violations of an element are located under its index ({@code
	 * [0]}, {@code [0].name}) and the violations of all failing elements are accumulated.
	 * @return a validator for iterables
	 * @since 0.8.0
	 */
	default Arguments1Validator<Iterable<A1>, Set<X>> liftSet() {
		return Arguments1Validator.from(ValueValidator.super.liftSet());
	}

	/**
	 * Returns a validator that validates the value contained in the input
	 * {@link Optional}, if any, and returns an {@link Optional} with the validated
	 * value. An empty optional validates successfully.
	 *
	 * @return a validator for optionals
	 * @since 0.8.0
	 */
	default Arguments1Validator<Optional<A1>, Optional<X>> liftOptional() {
		return Arguments1Validator.from(ValueValidator.super.liftOptional());
	}
EOD
fi)
}
EOF
done

for i in `seq 1 ${n}`;do
  class="DefaultArguments${i}Validator"
  interface="Arguments${i}Validator"
  file="$(dirname $0)/../src/main/java/am/ik/yavi/arguments/${class}.java"
  arguments="Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>"
  args="$(echo $(for j in `seq 1 ${i}`;do echo -n "@Nullable A${j} a${j}, ";done) | sed 's/,$//')"
  as="$(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//')"
  echo $file
  cat <<EOF > ${file}
/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.Locale;
$(if [ "${i}" == "1" ];then echo "import java.util.function.Function;"; fi)
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;$(if [ "${i}" == "1" ];then printf '\nimport am.ik.yavi.core.InputReader;'; fi)
import am.ik.yavi.core.Validated;
$(if [ "${i}" == "1" ];then echo "import am.ik.yavi.core.ValueDescription;"; fi)
import am.ik.yavi.core.Validator;
$(if [ "${i}" == "1" ];then echo "import am.ik.yavi.core.ValueValidator;"; fi)
import am.ik.yavi.fn.Function${i};
import org.jspecify.annotations.Nullable;

/**
 * Default {@link ${interface}} that applies a {@link Validator} to an
 * {@link Arguments${i}} container and maps the validated arguments with a
 * {@link Function${i}}.
 *
 * <p>
 * The validator is applied applicatively, so all violations are accumulated, and the
 * mapper is applied only when validation succeeds. A {@code null} argument is accepted
 * unless the configured constraints reject it.
 *
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
$(for j in `seq 1 ${i}`; do echo " * @param <A${j}> the type of the argument at position ${j}"; done)
 * @param <X> the type of the value produced when validation succeeds
 * @since 0.7.0
 */
public class ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> implements ${interface}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> {

	/** The validator applied to the {@link Arguments${i}} container. */
  protected final Validator<${arguments}> validator;

	/** The mapper applied to the validated arguments. */
	protected final Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends X> mapper;

$(if [ "${i}" == "1" ];then
cat <<EOD
	private final Class<?> valueType;

	/**
	 * The function applied to an argument other than {@code null} before it is
	 * validated, or {@code null} if the argument is validated as it is.
	 *
	 * @since 0.18.0
	 */
	protected final @Nullable Function<? super A1, ? extends @Nullable A1> transform;

EOD
fi)
	/**
	 * Creates a validator that applies the given validator to an {@link Arguments${i}}
	 * built from the arguments and maps the validated arguments with the given function.
	 *
	 * @param validator the validator applied to the arguments
	 * @param mapper the mapper applied to the validated arguments
	 */
	public ${class}(Validator<${arguments}> validator, Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends X> mapper) {
$(if [ "${i}" == "1" ];then
cat <<EOD
		this(validator, mapper, Object.class);
	}

	/**
	 * Creates a validator like
	 * {@link #DefaultArguments1Validator(Validator, Function1)} that also knows the raw
	 * type of the argument, which is reported by {@link #description()}.
	 *
	 * @param validator the validator applied to the argument
	 * @param mapper the mapper applied to the validated argument
	 * @param valueType the raw type of the argument; {@code Object} or {@code Enum} if
	 * it is unknown
	 * @since 0.18.0
	 */
	protected ${class}(Validator<${arguments}> validator, Function1<? super A1, ? extends X> mapper, Class<?> valueType) {
		this(validator, mapper, valueType, null);
	}

	/**
	 * Creates a validator like
	 * {@link #DefaultArguments1Validator(Validator, Function1, Class)} that applies the
	 * given function to an argument other than {@code null} before it validates it.
	 *
	 * @param validator the validator applied to the argument
	 * @param mapper the mapper applied to the validated argument
	 * @param valueType the raw type of the argument; {@code Object} or {@code Enum} if
	 * it is unknown
	 * @param transform the function applied to the argument before it is validated, or
	 * {@code null} to validate it as it is
	 * @since 0.18.0
	 */
	protected ${class}(Validator<${arguments}> validator, Function1<? super A1, ? extends X> mapper, Class<?> valueType,
			@Nullable Function<? super A1, ? extends @Nullable A1> transform) {
		this.validator = validator;
		this.mapper = mapper;
		this.valueType = valueType;
		this.transform = transform;
	}

	/**
	 * Returns a validator that applies the given function to an argument other than
	 * {@code null} before the function of this validator, if any, and then validates
	 * the result, as {@link ValueValidator#preprocess(Function)} states. The type of
	 * the argument is kept, so an untyped input is transformed after its type is
	 * checked.
	 *
	 * @param transform the function applied to an argument other than {@code null}
	 * @return a validator that transforms its argument first
	 * @since 0.18.0
	 */
	@Override
	public DefaultArguments1Validator<A1, X> preprocess(Function<? super A1, ? extends @Nullable A1> transform) {
		return new DefaultArguments1Validator<>(this.validator, this.mapper, this.valueType,
				composed(transform, this.transform));
	}

	/**
	 * Returns the function that applies the first function and then, unless the result
	 * is {@code null}, the second one.
	 *
	 * @param first the function applied first
	 * @param second the function applied to the result, or {@code null} for none
	 * @param <A1> the type of the argument
	 * @return the composed function
	 * @since 0.18.0
	 */
	protected static <A1> Function<? super A1, ? extends @Nullable A1> composed(
			Function<? super A1, ? extends @Nullable A1> first,
			@Nullable Function<? super A1, ? extends @Nullable A1> second) {
		if (second == null) {
			return first;
		}
		return a1 -> {
			final A1 transformed = first.apply(a1);
			return transformed == null ? null : second.apply(transformed);
		};
	}

	/**
	 * Describes the argument as a {@link ValueDescription.Value} with its raw type and
	 * the constraints of the validator.
	 *
	 * @return the description of the argument
	 * @since 0.18.0
	 */
	@Override
	public ValueDescription description() {
		return ValueDescription.Value.of(this.valueType, this.validator);
	}

	/**
	 * Returns a validator that accepts an untyped input read with the given reader and
	 * checks that it represents the raw type of the argument, as
	 * {@link ValueValidator#untyped(Class)} states. A mismatch is reported under the name
	 * of the argument.
	 * @param reader the reader of the nodes
	 * @param <N> the type of the nodes
	 *
	 * @return a value validator for an untyped input
	 * @throws UnsupportedOperationException if the raw type of the argument is unknown
	 * ({@code Object} or {@code Enum})
	 * @since 0.18.0
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
		if (this.valueType == Object.class || this.valueType == Enum.class) {
			return Arguments1Validator.super.readingWith(reader);
		}
		final ValueDescription.Value description = ValueDescription.Value.of(this.valueType, this.validator);
		return ValueValidator.<A1>passThrough()
			.untyped((Class<A1>) this.valueType)
			.readingWith(reader)
			.prefixed(description.name())
			.andThen(this)
			.describedAs(description);
	}
EOD
else
cat <<EOD
		this.validator = validator;
		this.mapper = mapper;
	}
EOD
fi)

	/**
	 * Returns a validator that produces a {@link Supplier} of the mapped value.
	 *
	 * @return a validator that produces a supplier of the validated value
	 * @since 0.10.0
	 */
	@Override
	public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X>> lazy() {
		return new ${class}<>(this.validator, ($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//')) -> () -> this.mapper.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//'))$(if [ "${i}" == "1" ];then echo -n ", this.valueType, this.transform"; fi));
	}

	/**
	 * Validates the arguments with the given validator and maps them with the configured
	 * mapper.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param a${j} the argument at position ${j}, possibly {@code null}"; done)
	 * @param locale the locale targeted for the violation messages
	 * @param constraintContext constraint context used by conditional constraints
	 * @return the mapped value or the constraint violations
	 */
  @Override
	public Validated<X> validate(${args},
			Locale locale, ConstraintContext constraintContext) {
$(if [ "${i}" == "1" ];then
cat <<EOD
		final A1 value = a1 == null || this.transform == null ? a1 : this.transform.apply(a1);
		return this.validator.applicative()
			.validate(Arguments.of(value), locale, constraintContext)
			.map(values -> values.map(this.mapper));
EOD
else
cat <<EOD
		return this.validator.applicative()
		    .validate(Arguments.of(${as}), locale, constraintContext)
				.map(values -> values.map(this.mapper));
EOD
fi)
	}
}
EOF
done

nn=16
for i in `seq 2 ${nn}`;do
  class="Arguments${i}Splitting"
  file="$(dirname $0)/../src/main/java/am/ik/yavi/arguments/${class}.java"
  echo $file
  cat <<EOF > ${file}
/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.Locale;
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.fn.Function${i};
import am.ik.yavi.fn.Validations;
import org.jspecify.annotations.Nullable;

/**
 * Combines ${i} validators that validate distinct arguments into a validator for those
 * arguments.
 *
 * <p>
 * Each argument is validated with its own {@link ValueValidator}. The validators are
 * applied independently: all violations are accumulated, and the function passed to
 * {@link #apply} is applied only when every validation succeeds. Instances are created
 * with {@link ArgumentsValidators#split} or by chaining {@code split} on an existing
 * instance.
 *
 * Generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
$(for j in `seq 1 ${i}`; do echo " * @param <A${j}> the type of the argument at position ${j}"; done)
$(for j in `seq 1 ${i}`; do echo " * @param <R${j}> the type of the value produced for the argument at position ${j}"; done)
 * @since 0.7.0
 */
public class ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> {
$(for j in `seq 1 ${i}`;do echo "	/** The validator applied to the argument at position ${j}. */"; echo "	protected final ValueValidator<? super A${j}, ? extends R${j}> v${j};";echo;done)

	/**
	 * Creates an instance with the given validators, one per argument.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param v${j} the validator applied to the argument at position ${j}"; done)
	 */
	public ${class}($(echo $(for j in `seq 1 ${i}`;do echo -n "ValueValidator<? super A${j}, ? extends R${j}> v${j}, ";done) | sed 's/,$//')) {
$(for j in `seq 1 ${i}`;do echo "		this.v${j} = v${j};";done)
	}

	/**
	 * Returns a validator that applies the validators of this instance to the arguments
	 * and combines the validated values with the given function.
	 *
	 * <p>
	 * All violations are accumulated and the function is applied only when every
	 * validation succeeds.
	 * The validator describes its arguments as
	 * {@link ValueDescription.Properties} with one property per argument, named as
	 * {@link ValueDescription.Properties#ofArguments} states.
	 *
	 * @param f the function that combines the validated values
	 * @param <X> the type of the value produced by the function
	 * @return a validator for the ${i} arguments
	 */
	public <X> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> apply(
			Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
		return new Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X>() {

			@Override
			public Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X>> lazy() {
				return (($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//'), locale, constraintContext) -> Validations.apply(
						($(echo $(for j in `seq 1 ${i}`;do echo -n "r${j}, ";done) | sed 's/,$//')) -> () -> f.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "r${j}, ";done) | sed 's/,$//')),
						$(echo $(for j in `seq 1 ${i}`;do echo -n "v${j}.validate(a${j}, locale, constraintContext), ";done) | sed 's/,$//')));
			}

			@Override
			public Validated<X> validate($(echo $(for j in `seq 1 ${i}`;do echo -n "@Nullable A${j} a${j}, ";done) | sed 's/,$//'), Locale locale,
					ConstraintContext constraintContext) {
				return Validations.apply(f::apply,
						$(echo $(for j in `seq 1 ${i}`;do echo -n "v${j}.validate(a${j}, locale, constraintContext), ";done) | sed 's/,$//'));
			}

			@Override
			public ValueDescription description() {
				return ValueDescription.Properties.ofArguments($(echo $(for j in `seq 1 ${i}`;do echo -n "v${j}.description(), ";done) | sed 's/,$//'));
			}
		};
	}
$(if [ ${i} -lt ${nn} ];then
  echo
  echo "	/**"
  echo "	 * Returns a new {@link Arguments$((${i} + 1))Splitting} that additionally validates the"
  echo "	 * argument at position $((${i} + 1)) with the given validator."
  echo "	 *"
  echo "	 * @param v$((${i} + 1)) the validator applied to the argument at position $((${i} + 1))"
  echo "	 * @param <A$((${i} + 1))> the type of the argument at position $((${i} + 1))"
  echo "	 * @param <R$((${i} + 1))> the type of the value produced for the argument at position $((${i} + 1))"
  echo "	 * @return an arguments$((${i} + 1)) splitting instance"
  echo "	 */"
  echo "	public <A$((${i} + 1)), R$((${i} + 1))> Arguments$((${i} + 1))Splitting<$(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "R${j}, ";done) | sed 's/,$//')> split(ValueValidator<? super A$((${i} + 1)), ? extends R$((${i} + 1))> v$((${i} + 1))) {"
  echo "		return new Arguments$((${i} + 1))Splitting<>($(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "v${j}, ";done) | sed 's/,$//'));"
  echo "	}"
else echo -n "";fi)
}
EOF
done

for i in `seq 2 ${nn}`;do
  class="Arguments${i}Combining"
  file="$(dirname $0)/../src/main/java/am/ik/yavi/arguments/${class}.java"
  echo $file
  cat <<EOF > ${file}
/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.fn.Function${i};
import am.ik.yavi.fn.Validations;
import java.util.Locale;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * Combines ${i} validators that validate the same input into a single validator.
 *
 * <p>
 * This is useful for cross-field validation: every validator receives the same input.
 * The validators are applied independently, all violations are accumulated, and the
 * function passed to {@link #apply} is applied only when every validation succeeds.
 * Instances are created with {@link ArgumentsValidators#combine} or by chaining
 * {@code combine} on an existing instance.
 *
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @param <A> the type of the input validated by all validators
$(for j in `seq 1 ${i}`; do echo " * @param <R${j}> the type of the value produced by the validator at position ${j}"; done)
 * @since 0.7.0
 */
public class ${class}<A, $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> {

$(for j in `seq 1 ${i}`;do echo "	/** The validator applied to the input at position ${j}. */"; echo "	protected final ValueValidator<? super A, ? extends R${j}> v${j};";done)

	/**
	 * Creates an instance with the given validators, which all receive the same input.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param v${j} the validator applied to the input at position ${j}"; done)
	 */
	public ${class}($(echo $(for j in `seq 1 ${i}`;do echo -n "ValueValidator<? super A, ? extends R${j}> v${j}, ";done) | sed 's/,$//')) {
$(for j in `seq 1 ${i}`;do echo "		this.v${j} = v${j};";done)
	}

	/**
	 * Returns a validator that applies every validator of this instance to the input and
	 * combines the validated values with the given function.
	 *
	 * <p>
	 * All violations are accumulated and the function is applied only when every
	 * validation succeeds.
	 *
	 * @param f the function that combines the validated values
	 * @param <X> the type of the value produced by the function
	 * @return a validator for the input
	 */
	public <X> Arguments1Validator<A, X> apply(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
		return new Arguments1Validator<A, X>() {
			@Override
			public Validated<X> validate(@Nullable A a, Locale locale, ConstraintContext constraintContext) {
				return Validations.apply(f::apply, $(echo $(for j in `seq 1 ${i}`;do echo -n "${class}.this.v${j}.validate(a, locale, constraintContext), ";done) | sed 's/,$//'));
			}

			@Override
			public Arguments1Validator<A, Supplier<X>> lazy() {
				return (a, locale, constraintContext) -> Validations.apply(($(echo $(for j in `seq 1 ${i}`;do echo -n "r${j}, ";done) | sed 's/,$//')) -> () -> f.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "r${j}, ";done) | sed 's/,$//')),
						$(echo $(for j in `seq 1 ${i}`;do echo -n "${class}.this.v${j}.validate(a, locale, constraintContext), ";done) | sed 's/,$//'));
			}
		};
	}
$(if [ ${i} -lt ${nn} ];then
  echo
  echo "	/**"
  echo "	 * Returns a new {@link Arguments$((${i} + 1))Combining} that additionally validates the"
  echo "	 * input with the given validator."
  echo "	 *"
  echo "	 * @param v$((${i} + 1)) the additional validator applied to the input"
  echo "	 * @param <R$((${i} + 1))> the type of the value produced by the additional validator"
  echo "	 * @return an arguments$((${i} + 1)) combining instance"
  echo "	 */"
  echo "	public <R$((${i} + 1))> Arguments$((${i} + 1))Combining<A, $(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "R${j}, ";done) | sed 's/,$//')> combine(ValueValidator<? super A, ? extends R$((${i} + 1))> v$((${i} + 1))) {"
  echo "		return new Arguments$((${i} + 1))Combining<>($(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "v${j}, ";done) | sed 's/,$//'));"
  echo "	}"
else echo -n "";fi)

}
EOF
done

class="ArgumentsValidators"
file="$(dirname $0)/../src/main/java/am/ik/yavi/arguments/${class}.java"
echo $file
cat <<EOF > ${file}
/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueValidator;

import static java.util.function.Function.identity;

/**
 * Factory methods for validators that handle multiple arguments.
 *
 * <p>
 * {@code split} validates distinct arguments with separate validators, while
 * {@code combine} applies several validators to the same input, for example for
 * cross-field validation. {@code traverseN} and {@code sequenceN} apply one or more
 * {@code ArgumentsNValidator} instances to a collection of inputs and collect the
 * results. The {@code lift*} methods lift a {@link ValueValidator} to collections or
 * optionals. The returned validators accumulate all violations.
 *
 * <p>
 * This class only provides static methods; it is not instantiated.
 *
 * Generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class ${class} {

	/**
	 * Creates an instance. Use the static factory methods instead.
	 */
	public ${class}() {
	}

$(for i in `seq 2 ${nn}`;do cat <<EOD
	/**
	 * Returns an {@link Arguments${i}Splitting} that validates the ${i} arguments with the
	 * given validators.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param v${j} the validator applied to the argument at position ${j}"; done)
$(for j in `seq 1 ${i}`; do echo "	 * @param <A${j}> the type of the argument at position ${j}"; done)
$(for j in `seq 1 ${i}`; do echo "	 * @param <R${j}> the type of the value produced for the argument at position ${j}"; done)
	 * @return an arguments${i} splitting instance
	 */
	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> Arguments${i}Splitting<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> split($(echo $(for j in `seq 1 ${i}`;do echo -n "ValueValidator<? super A${j}, ? extends R${j}> v${j}, ";done) | sed 's/,$//')) {
		return new Arguments${i}Splitting<>($(echo $(for j in `seq 1 ${i}`;do echo -n "v${j}, ";done) | sed 's/,$//'));
	}
EOD
done)

$(for i in `seq 2 ${nn}`;do cat <<EOD
	/**
	 * Returns an {@link Arguments${i}Combining} that validates the same input with the
	 * given validators.
	 *
$(for j in `seq 1 ${i}`; do echo "	 * @param v${j} the validator applied to the input at position ${j}"; done)
	 * @param <A> the type of the input validated by all validators
$(for j in `seq 1 ${i}`; do echo "	 * @param <R${j}> the type of the value produced by the validator at position ${j}"; done)
	 * @return an arguments${i} combining instance
	 */
	public static <A, $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> Arguments${i}Combining<A, $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> combine($(echo $(for j in `seq 1 ${i}`;do echo -n "ValueValidator<? super A, ? extends R${j}> v${j}, ";done) | sed 's/,$//')) {
		return new Arguments${i}Combining<>($(echo $(for j in `seq 1 ${i}`;do echo -n "v${j}, ";done) | sed 's/,$//'));
	}
EOD
done)

$(for i in `seq 1 ${nn}`;do cat <<EOD
	/**
	 * Returns a validator that validates each element of the given iterable with the
	 * validator produced by the given function and collects the validated values into a
	 * {@link List}. All elements are validated and the violations of all failing elements
	 * are accumulated.
	 *
	 * @param values the values to validate
	 * @param f the function that produces a validator for an element
$(for j in `seq 1 ${i}`; do echo "	 * @param <A${j}> the type of the argument at position ${j}"; done)
	 * @param <R> the type of the value produced for each element
	 * @param <T> the type of the elements to validate
	 * @return a validator that produces a list of validated values
	 */
	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), R, T> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), List<R>> traverse${i}(
			Iterable<T> values,
			Function<? super T, ? extends Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends R>> f) {
		return ($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//'), locale, constraintContext) ->
			Validated.traverse(values, f.andThen(validator -> validator.validate($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//'), locale, constraintContext)));
	}
EOD
done)

$(for i in `seq 1 ${nn}`;do cat <<EOD
	/**
	 * Returns a validator that applies every given validator to the same arguments and
	 * collects the results into a {@link List}. All validators are applied and the
	 * violations of all failing validations are accumulated.
	 *
	 * @param values the validators to apply
$(for j in `seq 1 ${i}`; do echo "	 * @param <A${j}> the type of the argument at position ${j}"; done)
	 * @param <R> the type of the value produced for each validator
	 * @return a validator that produces a list of validated values
	 */
	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), R> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), List<R>> sequence${i}(
		Iterable<? extends Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends R>> values) {
		return traverse${i}(values, identity());
	}
EOD
done)

	/**
	 * Returns a validator that validates every element of the input iterable with the
	 * given validator and collects the validated values into a collection created by the
	 * given factory. The violations of an element are located under its index
	 * ({@code [0]}, {@code [0].name}) and the violations of all failing elements are
	 * accumulated.
	 * @param validator the validator applied to each element
	 * @param factory supplier of the collection that receives the validated values
	 * @param <A1> the element type of the input iterable
	 * @param <R> the type produced for each element
	 * @param <C> the type of the resulting collection
	 * @return a validator for iterables
	 * @since 0.8.0
	 */
	public static <A1, R, C extends Collection<R>> Arguments1Validator<Iterable<A1>, C> liftCollection(
			ValueValidator<? super A1, ? extends R> validator, Supplier<C> factory) {
		return Arguments1Validator
				.from(ValueValidator.liftCollection(validator, factory));
	}

	/**
	 * Returns a validator that validates every element of the input iterable with the
	 * given validator and collects the validated values into a {@link List}. The
	 * violations of an element are located under its index ({@code [0]}, {@code
	 * [0].name}) and the violations of all failing elements are accumulated.
	 * @param validator the validator applied to each element
	 * @param <A1> the element type of the input iterable
	 * @param <R> the type produced for each element
	 * @return a validator for iterables
	 */
	public static <A1, R> Arguments1Validator<Iterable<A1>, List<R>> liftList(
			ValueValidator<? super A1, ? extends R> validator) {
		return Arguments1Validator.from(ValueValidator.liftList(validator));
	}

	/**
	 * Returns a validator that validates every element of the input iterable with the
	 * given validator and collects the validated values into a {@link Set} that preserves
	 * the iteration order. The violations of an element are located under its index
	 * ({@code [0]}, {@code [0].name}) and the violations of all failing elements are
	 * accumulated.
	 * @param validator the validator applied to each element
	 * @param <A1> the element type of the input iterable
	 * @param <R> the type produced for each element
	 * @return a validator for iterables
	 * @since 0.8.0
	 */
	public static <A1, R> Arguments1Validator<Iterable<A1>, Set<R>> liftSet(
			ValueValidator<? super A1, ? extends R> validator) {
		return Arguments1Validator.from(ValueValidator.liftSet(validator));
	}

	/**
	 * Returns a validator that validates the value contained in the input
	 * {@link Optional}, if any, and returns an {@link Optional} with the validated value.
	 * An empty optional validates successfully.
	 *
	 * @param validator the validator applied to the contained value
	 * @param <A1> the contained type of the input optional
	 * @param <R> the type produced for the contained value
	 * @return a validator for optionals
	 * @since 0.8.0
	 */
	public static <A1, R> Arguments1Validator<Optional<A1>, Optional<R>> liftOptional(
			ValueValidator<? super A1, ? extends R> validator) {
		return Arguments1Validator.from(ValueValidator.liftOptional(validator));
	}

}
EOF

for i in `seq 0 ${n}`;do
if [ "${i}" -gt 0 ];then
  class="Arguments${i}ValidatorBuilder"
else
  class="YaviArguments";
fi
  next_class="Arguments$((i + 1))ValidatorBuilder"
  # "seq 1 0" counts down on macOS, so the ranges that may be empty are guarded
  prev_definitions=""
  init_definitions=""
  init_results=""
  if [ "${i}" -gt 0 ];then
    prev_definitions="$(for j in `seq 1 ${i}`;do echo -n "this.d${j}, ";done)"
  fi
  if [ "${i}" -gt 1 ];then
    init_definitions="$(for j in `seq 1 $((i - 1))`;do echo -n "this.d${j}, ";done)"
    init_results="$(for j in `seq 1 $((i - 1))`;do echo -n "R${j}, ";done)"
  fi
  file="$(dirname $0)/../src/main/java/am/ik/yavi/builder/${class}.java"
  echo $file
  cat <<EOF > ${file}
/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
$(if [ "${i}" != "${n}" ];then
cat <<EOD
import java.util.List;
import java.util.Map;
EOD
elif [ "${i}" -gt 0 ];then
cat <<EOD
import java.util.List;
EOD
fi)
$(if [ "${i}" -gt 1 ];then
cat <<EOD
import java.util.Objects;
EOD
fi)
$(if [ "${i}" -gt 0 ];then
cat <<EOD
import java.util.Optional;
EOD
fi)
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
$(if [ "${i}" -gt 1 ];then
cat <<EOD
import am.ik.yavi.arguments.ArgumentsValidators;
EOD
fi)
$(if [ "${i}" -gt 0 ];then
cat <<EOD
import am.ik.yavi.arguments.Arguments${i}Validator;
EOD
fi)
import am.ik.yavi.constraint.BigDecimalConstraint;
import am.ik.yavi.constraint.BigIntegerConstraint;
import am.ik.yavi.constraint.BooleanConstraint;
import am.ik.yavi.constraint.CharSequenceConstraint;$(if [ "${i}" != "${n}" ];then printf '\nimport am.ik.yavi.constraint.CollectionConstraint;'; fi)
import am.ik.yavi.constraint.DoubleConstraint;
import am.ik.yavi.constraint.EnumConstraint;
import am.ik.yavi.constraint.FloatConstraint;
import am.ik.yavi.constraint.InstantConstraint;
import am.ik.yavi.constraint.IntegerConstraint;
import am.ik.yavi.constraint.LocalDateTimeConstraint;
import am.ik.yavi.constraint.LocalDateConstraint;
import am.ik.yavi.constraint.LocalTimeConstraint;
import am.ik.yavi.constraint.LongConstraint;$(if [ "${i}" != "${n}" ];then printf '\nimport am.ik.yavi.constraint.MapConstraint;'; fi)
import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.constraint.OffsetDateTimeConstraint;
import am.ik.yavi.constraint.ShortConstraint;
import am.ik.yavi.constraint.StringValueConstraint;
import am.ik.yavi.constraint.YearConstraint;
import am.ik.yavi.constraint.YearMonthConstraint;
import am.ik.yavi.constraint.ZonedDateTimeConstraint;$(if [ "${i}" -gt 0 ];then printf '\nimport am.ik.yavi.core.ConstraintViolations;\nimport am.ik.yavi.core.InputReader;\nimport am.ik.yavi.core.Validated;'; fi)
import am.ik.yavi.core.ValueValidator;
$(if [ "${i}" -gt 0 ];then
cat <<EOD
import am.ik.yavi.fn.Function${i};
import am.ik.yavi.input.MapInputValidator;
EOD
fi)

/**
$(if [ "${i}" -gt 0 ];then
cat <<EOD
 * Builds an {@link Arguments${i}Validator} by adding one validated argument at a time.
 *
 * <p>
 * Each {@code _xxx} method adds the next argument and returns the builder for the
 * following argument. The arguments are validated together, so every violation is
 * reported and the result is created only when all arguments are valid. The validator is
 * created with {@code apply} after all arguments have been added, or with
 * {@code fromMap} to read the arguments from a {@code Map<String, Object>} by their
 * names. Instances are obtained from {@link YaviArguments} or from the builder of the
 * previous argument count.
 *
 * <p>
 * Besides single values, an argument can be a nested object ({@code _object} with a name
 * and a value validator), a list ({@code _list}) or a map ({@code _map}); their
 * violations are located under the argument name. {@code optional()},
 * {@code nullable()} and {@code defaultValue} apply to the last added argument and make
 * it accept {@code null}; {@code recover} makes it accept any value.
 *
 * <p>
 * With {@code fromMap}, an argument added with {@code _embedded} is read from the same
 * map as the other arguments and one added with {@code _rest} from the keys that no
 * other argument defines.
 *
 * <p>
 * The source of this class is generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @param <A1> type of the first argument$(for j in `seq 1 ${i}`;do if [ "${j}" -gt 1 ];then printf '\n * @param <A%s> type of argument at position %s' "${j}" "${j}";fi;done)
 * @param <R1> type of the validated value of the first argument$(for j in `seq 1 ${i}`;do if [ "${j}" -gt 1 ];then printf '\n * @param <R%s> type of the validated value of argument at position %s' "${j}" "${j}";fi;done)
 * @since 0.14.0
 */
EOD
else
cat <<EOD
 * Builds an arguments validator by adding one validated argument at a time.
 *
 * <p>
 * Each {@code _xxx} method adds the next argument and returns the builder for the
 * following argument. The arguments are validated together, so every violation is
 * reported and the result is created only when all arguments are valid. The validator is
 * created with {@code apply} after all arguments have been added. Instances are obtained
 * from {@link am.ik.yavi.validator.Yavi#arguments()}. For example: <pre>{@code
 * Arguments3Validator<String, String, Integer, Car> validator = Yavi.arguments()
 *     ._string("manufacturer", c -> c.notNull())
 *     ._string("licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
 *     ._integer("seatCount", c -> c.greaterThanOrEqual(2))
 *     .apply(Car::new);
 * }</pre>
 *
 * <p>
 * Besides single values, an argument can be a nested object ({@code _object} with a name
 * and a value validator), a list ({@code _list}) or a map ({@code _map}); their
 * violations are located under the argument name. On the returned builders,
 * {@code optional()}, {@code nullable()} and {@code defaultValue} apply to the last added
 * argument and make it accept {@code null}; {@code recover} makes it accept any value.
 *
 * <p>
 * With {@code fromMap}, an argument added with {@code _embedded} is read from the same
 * map as the other arguments and one added with {@code _rest} from the keys that no
 * other argument defines.
 *
 * <p>
 * The source of this class is generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.14.0
 */
EOD
fi)
public final class ${class}$(if [ "${i}" -gt 0 ];then
cat <<EOD
<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')>
EOD
fi)
{

$(if [ "${i}" -gt 0 ];then
cat <<EOD
$(for j in `seq 1 ${i}`;do echo "	final ArgumentDefinition<A${j}, R${j}> d${j};";echo;done)

	${class}($(echo $(for j in `seq 1 ${i}`;do echo -n "ArgumentDefinition<A${j}, R${j}> d${j}, ";done) | sed 's/,$//')) {
$(for j in `seq 1 ${i}`;do echo "		this.d${j} = d${j};";done)
	}

	/**
	 * Accepts a {@code null} value for the last added argument as an absent value.
	 *
	 * <p>
	 * A {@code null} value succeeds with an empty {@link Optional} and the constraints of
	 * the argument, such as {@code notNull()}, are not applied to it. Any other value is
	 * validated as before and wrapped in an {@link Optional}.
	 * @return a builder whose last argument produces an optional value
	 * @since 0.18.0
	 * @see ValueValidator#optional()
	 */
	public ${class}<$(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done)${init_results}Optional<R${i}>> optional() {
		return new ${class}<>(${init_definitions}this.d${i}.asOptional());
	}

	/**
	 * Accepts a {@code null} value for the last added argument and produces {@code null}
	 * for it.
	 *
	 * <p>
	 * A {@code null} value succeeds with {@code null} and the constraints of the
	 * argument, such as {@code notNull()}, are not applied to it. Any other value is
	 * validated as before.
	 * @return a builder whose last argument accepts {@code null}
	 * @since 0.18.0
	 * @see ValueValidator#nullable()
	 */
	public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> nullable() {
		return new ${class}<>(${init_definitions}this.d${i}.asNullable());
	}

	/**
	 * Produces the given value when the value of the last added argument is
	 * {@code null}.
	 *
	 * <p>
	 * A {@code null} value succeeds with the default value and the constraints of the
	 * argument are not applied to it; the default value itself is not validated. Any
	 * other value is validated as before.
	 * @param defaultValue the value produced for a {@code null} value
	 * @return a builder whose last argument falls back to the default value
	 * @since 0.18.0
	 * @see ValueValidator#defaultValue(Object)
	 */
	public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> defaultValue(R${i} defaultValue) {
		return new ${class}<>(${init_definitions}this.d${i}.withDefault(defaultValue));
	}

	/**
	 * Produces the given value when the value of the last added argument is {@code null}
	 * or invalid, so that the argument never fails.
	 *
	 * <p>
	 * A {@code null} value succeeds with the fallback and the constraints of the argument
	 * are not applied to it; the fallback itself is not validated. Every violation of
	 * any other value is replaced with the fallback, including the one of a value read
	 * from a map that does not represent the type of the argument.
	 * @param fallback the value produced instead of a failure
	 * @return a builder whose last argument falls back to the given value
	 * @since 0.18.0
	 * @see ValueValidator#recover(Object)
	 */
	public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> recover(R${i} fallback) {
		return new ${class}<>(${init_definitions}this.d${i}.recovering(fallback));
	}

	/**
	 * Produces the result of the given function when the value of the last added
	 * argument is {@code null} or invalid, so that the argument never fails.
	 *
	 * <p>
	 * The function is given the violations of the invalid value, and no violation for a
	 * {@code null} value, to which the constraints of the argument are not applied.
	 * @param fallback the function that produces a value from the violations
	 * @return a builder whose last argument falls back to the result of the function
	 * @since 0.18.0
	 * @see ValueValidator#recoverWith(Function)
	 */
	public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> recoverWith(Function<? super ConstraintViolations, ? extends R${i}> fallback) {
		return new ${class}<>(${init_definitions}this.d${i}.recoveringWith(fallback));
	}
EOD
else
cat <<EOD
	/**
	 * Creates a new builder.
	 *
	 * <p>
	 * Instances are normally obtained from
	 * {@link am.ik.yavi.validator.Yavi#arguments()}.
	 * @since 0.14.0
	 */
	public ${class}() {
	}
EOD
fi)

$(if [ "${i}" != "${n}" ];then
cat <<EOD
	/**
	 * Adds the next argument, validated as a {@code BigDecimal} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigDecimal, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _bigDecimal(ValueValidator<BigDecimal, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(BigDecimal.class, validator));
	}

	/**
	 * Adds the next argument as a {@code BigDecimal} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link BigDecimalConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigDecimal, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) BigDecimal> _bigDecimal(String name,
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, BigDecimal.class, BigDecimalValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code BigDecimal} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigDecimal, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) BigDecimal> _bigDecimal(String name) {
		return this._bigDecimal(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code BigInteger} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigInteger, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _bigInteger(ValueValidator<BigInteger, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(BigInteger.class, validator));
	}

	/**
	 * Adds the next argument as a {@code BigInteger} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link BigIntegerConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigInteger, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) BigInteger> _bigInteger(String name,
			Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, BigInteger.class, BigIntegerValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code BigInteger} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigInteger, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) BigInteger> _bigInteger(String name) {
		return this._bigInteger(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code Boolean} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Boolean, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _boolean(ValueValidator<Boolean, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(Boolean.class, validator));
	}

	/**
	 * Adds the next argument as a {@code Boolean} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link BooleanConstraint}, for example {@code boolean.isTrue}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Boolean, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Boolean> _boolean(String name,
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, Boolean.class, BooleanValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code Boolean} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Boolean, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Boolean> _boolean(String name) {
		return this._boolean(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code Double} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Double, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _double(ValueValidator<Double, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(Double.class, validator));
	}

	/**
	 * Adds the next argument as a {@code Double} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link DoubleConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Double, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Double> _double(String name,
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, Double.class, DoubleValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code Double} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Double, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Double> _double(String name) {
		return this._double(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as an enum constant by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <E> the enum type of the argument
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <E extends Enum<E>, T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _enum(ValueValidator<E, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(Enum.class, validator));
	}

	/**
	 * Adds the next argument as an enum constant with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link EnumConstraint}, for example {@code object.oneOf}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param <E> the enum type of the argument
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <E extends Enum<E>> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) E> _enum(String name,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, EnumValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as an enum constant with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param <E> the enum type of the argument
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <E extends Enum<E>> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) E> _enum(String name) {
		return this._enum(name, Function.identity());
	}

	/**
	 * Adds the next argument as a constant of the given enum type with the given name
	 * and configured constraints.
	 *
	 * <p>
	 * Unlike {@link #_enum(String, Function)}, the argument knows its enum type: its
	 * description holds the constants, and {@code fromMap} reads it from the name of a
	 * constant.
	 * @param <E> the enum type of the argument
	 * @param name the property name used in violation names and messages
	 * @param type the enum type of the argument
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <E extends Enum<E>> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) E> _enum(String name, Class<E> type,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, type, EnumValidatorBuilder.of(name, type, constraints).build()));
	}

	/**
	 * Adds the next argument as a constant of the given enum type with the given name
	 * and no constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * @param <E> the enum type of the argument
	 * @param name the property name recorded for the argument
	 * @param type the enum type of the argument
	 * @return a builder for the next argument
	 * @since 0.18.0
	 * @see #_enum(String, Class, Function)
	 */
	public <E extends Enum<E>> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) E> _enum(String name, Class<E> type) {
		return this._enum(name, type, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code Float} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Float, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _float(ValueValidator<Float, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(Float.class, validator));
	}

	/**
	 * Adds the next argument as a {@code Float} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link FloatConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Float, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Float> _float(String name,
			Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, Float.class, FloatValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code Float} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Float, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Float> _float(String name) {
		return this._float(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as an {@code Instant} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Instant, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _instant(ValueValidator<Instant, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(Instant.class, validator));
	}

	/**
	 * Adds the next argument as an {@code Instant} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link InstantConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Instant, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Instant> _instant(String name,
			Function<InstantConstraint<Arguments1<Instant>>, InstantConstraint<Arguments1<Instant>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, Instant.class, InstantValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as an {@code Instant} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Instant, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Instant> _instant(String name) {
		return this._instant(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as an {@code Integer} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Integer, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _integer(ValueValidator<Integer, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(Integer.class, validator));
	}

	/**
	 * Adds the next argument as an {@code Integer} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link IntegerConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Integer, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Integer> _integer(String name,
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, Integer.class, IntegerValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as an {@code Integer} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Integer, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Integer> _integer(String name) {
		return this._integer(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code LocalDateTime} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _localDateTime(ValueValidator<LocalDateTime, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(LocalDateTime.class, validator));
	}

	/**
	 * Adds the next argument as a {@code LocalDateTime} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link LocalDateTimeConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalDateTime> _localDateTime(String name,
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, LocalDateTime.class, LocalDateTimeValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code LocalDateTime} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalDateTime> _localDateTime(String name) {
		return this._localDateTime(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code LocalTime} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _localTime(ValueValidator<LocalTime, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(LocalTime.class, validator));
	}

	/**
	 * Adds the next argument as a {@code LocalTime} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link LocalTimeConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalTime> _localTime(String name,
			Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, LocalTime.class, LocalTimeValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code LocalTime} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalTime> _localTime(String name) {
		return this._localTime(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code LocalDate} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDate, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _localDate(ValueValidator<LocalDate, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(LocalDate.class, validator));
	}

	/**
	 * Adds the next argument as a {@code LocalDate} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link LocalDateConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDate, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalDate> _localDate(String name,
			Function<LocalDateConstraint<Arguments1<LocalDate>>, LocalDateConstraint<Arguments1<LocalDate>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, LocalDate.class, LocalDateValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code LocalDate} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDate, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalDate> _localDate(String name) {
		return this._localDate(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code Long} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Long, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _long(ValueValidator<Long, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(Long.class, validator));
	}

	/**
	 * Adds the next argument as a {@code Long} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link LongConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Long, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Long> _long(String name,
			Function<LongConstraint<Arguments1<Long>>, LongConstraint<Arguments1<Long>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, Long.class, LongValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code Long} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Long, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Long> _long(String name) {
		return this._long(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as an object by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T1> type of the value validated by {@code validator}
	 * @param <T2> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T1, T2> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T1, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T2> _object(ValueValidator<T1, T2> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(Object.class, validator));
	}

	/**
	 * Adds the next argument as an object with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link ObjectConstraint}, for example {@code object.notNull}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param <T> type of the argument
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _object(String name,
			Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, ObjectValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as an object with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param <T> type of the argument
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _object(String name) {
		return this._object(name, Function.identity());
	}

	/**
	 * Adds the next argument as an object of the given type with the given name and
	 * configured constraints.
	 *
	 * <p>
	 * Unlike {@link #_object(String, Function)}, the argument knows its type, so
	 * {@code fromMap} can read it: the value in the map must be an instance of the type
	 * or represent it. Give {@code Object.class} to take any value as it is.
	 * @param <T> type of the argument
	 * @param name the property name used in violation names and messages
	 * @param type the type of the argument
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _object(String name, Class<T> type,
			Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, type, ObjectValidatorBuilder.of(name, type, constraints).build()));
	}

	/**
	 * Adds the next argument as an object of the given type with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * @param <T> type of the argument
	 * @param name the property name recorded for the argument
	 * @param type the type of the argument
	 * @return a builder for the next argument
	 * @since 0.18.0
	 * @see #_object(String, Class, Function)
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _object(String name, Class<T> type) {
		return this._object(name, type, Function.identity());
	}

	/**
	 * Adds the next argument, validated as an {@code OffsetDateTime} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) OffsetDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _offsetDateTime(ValueValidator<OffsetDateTime, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(OffsetDateTime.class, validator));
	}

	/**
	 * Adds the next argument as an {@code OffsetDateTime} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link OffsetDateTimeConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) OffsetDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) OffsetDateTime> _offsetDateTime(String name,
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, OffsetDateTime.class, OffsetDateTimeValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as an {@code OffsetDateTime} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) OffsetDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) OffsetDateTime> _offsetDateTime(String name) {
		return this._offsetDateTime(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code Short} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Short, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _short(ValueValidator<Short, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(Short.class, validator));
	}

	/**
	 * Adds the next argument as a {@code Short} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link ShortConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Short, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Short> _short(String name,
			Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, Short.class, ShortValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code Short} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Short, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Short> _short(String name) {
		return this._short(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code String} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) String, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _string(ValueValidator<String, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(String.class, validator));
	}

	/**
	 * Adds the next argument as a {@code String} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link CharSequenceConstraint}, for example {@code charSequence.notBlank}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 *
	 * <p>
	 * The value can be transformed before it is constrained with the transforms of
	 * {@link StringValueConstraint}, for example
	 * {@code c -> c.trim().toLowerCase().notBlank().email()}. The constraints and the
	 * validated value of the argument see the transformed value.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the transforms and the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) String, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) String> _string(String name,
			Function<? super StringValueConstraint<Arguments1<String>>, ? extends CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, String.class, StringValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code String} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) String, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) String> _string(String name) {
		return this._string(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code YearMonth} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) YearMonth, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _yearMonth(ValueValidator<YearMonth, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(YearMonth.class, validator));
	}

	/**
	 * Adds the next argument as a {@code YearMonth} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link YearMonthConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) YearMonth, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) YearMonth> _yearMonth(String name,
			Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, YearMonth.class, YearMonthValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code YearMonth} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) YearMonth, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) YearMonth> _yearMonth(String name) {
		return this._yearMonth(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code Year} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Year, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _year(ValueValidator<Year, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(Year.class, validator));
	}

	/**
	 * Adds the next argument as a {@code Year} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link YearConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Year, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Year> _year(String name,
			Function<YearConstraint<Arguments1<Year>>, YearConstraint<Arguments1<Year>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, Year.class, YearValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code Year} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Year, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Year> _year(String name) {
		return this._year(name, Function.identity());
	}

	/**
	 * Adds the next argument, validated as a {@code ZonedDateTime} by the given value
	 * validator.
	 *
	 * <p>
	 * The validator defines both the validation and the result type of the
	 * argument, so {@code validator} also provides the argument name and its
	 * violations.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) ZonedDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _zonedDateTime(ValueValidator<ZonedDateTime, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(ZonedDateTime.class, validator));
	}

	/**
	 * Adds the next argument as a {@code ZonedDateTime} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link ZonedDateTimeConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * accepted unless the constraints require otherwise.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) ZonedDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) ZonedDateTime> _zonedDateTime(String name,
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.value(name, ZonedDateTime.class, ZonedDateTimeValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code ZonedDateTime} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value, including {@code null}, is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) ZonedDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) ZonedDateTime> _zonedDateTime(String name) {
		return this._zonedDateTime(name, Function.identity());
	}

	/**
	 * Adds the next argument as a nested object with the given name, validated by the
	 * given value validator.
	 *
	 * <p>
	 * The violations of {@code validator} are located under {@code name}: a violation
	 * named {@code street} is reported as {@code name.street} with the corresponding
	 * {@link am.ik.yavi.core.ConstraintViolation#path() path}. A {@code null} value is
	 * rejected with {@code object.notNull} unless {@code optional()}, {@code nullable()}
	 * or {@code defaultValue} is applied to the argument.
	 * @param <T1> type of the value validated by {@code validator}
	 * @param <T2> type of the value produced by {@code validator}
	 * @param name the property name that prefixes the violations of {@code validator}
	 * @param validator value validator for the nested object
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T1, T2> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T1, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T2> _object(String name, ValueValidator<T1, T2> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.object(name, validator));
	}

	/**
	 * Adds the next argument as a {@code List} with the given name whose elements are
	 * validated by the given value validator.
	 *
	 * <p>
	 * Every element is validated and the violations of all elements are accumulated. The
	 * violations of the element at position {@code i} are located under
	 * {@code name[i]}: a violation named {@code street} is reported as
	 * {@code name[i].street}, and one with an empty name on the element itself as
	 * {@code name[i]}. A {@code null} list is rejected with
	 * {@code object.notNull} unless {@code optional()}, {@code nullable()} or
	 * {@code defaultValue} is applied to the argument.
	 * @param <E> type of the elements validated by {@code elementValidator}
	 * @param <T> type of the values produced by {@code elementValidator}
	 * @param name the property name that prefixes the violations of the elements
	 * @param elementValidator value validator for each element
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <E, T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) List<E>, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) List<T>> _list(String name, ValueValidator<E, T> elementValidator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.list(name, elementValidator, ContainerConstraints.none()));
	}

	/**
	 * Adds the next argument as a {@code List} with the given name whose elements are
	 * validated by the given value validator and which satisfies the given constraints.
	 *
	 * <p>
	 * The elements are validated like {@link #_list(String, ValueValidator)} does. The
	 * constraints are checked on the list of the validated elements, only when every
	 * element is valid, and their violations are reported on {@code name}.
	 *
	 * <pre>{@code
	 * ._list("tags", Yavi._string(c -> c.trim().notBlank()), c -> c.notEmpty().lessThanOrEqual(10).unique())
	 * }</pre>
	 * @param <E> type of the elements validated by {@code elementValidator}
	 * @param <T> type of the values produced by {@code elementValidator}
	 * @param name the property name of the list
	 * @param elementValidator value validator for each element
	 * @param constraints function that configures the constraints of the list
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <E, T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) List<E>, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) List<T>> _list(String name, ValueValidator<E, T> elementValidator,
			Function<CollectionConstraint<Arguments1<List<T>>, List<T>, T>, CollectionConstraint<Arguments1<List<T>>, List<T>, T>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.list(name, elementValidator, ContainerConstraints.ofList(constraints)));
	}

	/**
	 * Adds the next argument as a {@code Map} with the given name whose values are
	 * validated by the given value validator.
	 *
	 * <p>
	 * Every value is validated and the violations of all values are accumulated. The
	 * violations of the value for the key {@code key} are located under
	 * {@code name.key}; give {@code valueValidator} an empty name to report a violation
	 * on the value itself. A {@code null} map is rejected with {@code object.notNull}
	 * unless {@code optional()}, {@code nullable()} or {@code defaultValue} is applied
	 * to the argument.
	 * @param <V> type of the values validated by {@code valueValidator}
	 * @param <T> type of the values produced by {@code valueValidator}
	 * @param name the property name that prefixes the violations of the values
	 * @param valueValidator value validator for each value
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <V, T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Map<String, V>, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Map<String, T>> _map(String name, ValueValidator<V, T> valueValidator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.map(name, valueValidator, ContainerConstraints.none()));
	}

	/**
	 * Adds the next argument as a {@code Map} with the given name whose values are
	 * validated by the given value validator and which satisfies the given constraints.
	 *
	 * <p>
	 * The values are validated like {@link #_map(String, ValueValidator)} does. The
	 * constraints are checked on the map of the validated values, only when every value
	 * is valid, and their violations are reported on {@code name}.
	 *
	 * <pre>{@code
	 * ._map("prices", Yavi._bigDecimal(c -> c.notNull().positive()), c -> c.notEmpty())
	 * }</pre>
	 * @param <V> type of the values validated by {@code valueValidator}
	 * @param <T> type of the values produced by {@code valueValidator}
	 * @param name the property name of the map
	 * @param valueValidator value validator for each value
	 * @param constraints function that configures the constraints of the map
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <V, T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Map<String, V>, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Map<String, T>> _map(String name, ValueValidator<V, T> valueValidator,
			Function<MapConstraint<Arguments1<Map<String, T>>, String, T>, MapConstraint<Arguments1<Map<String, T>>, String, T>> constraints) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.map(name, valueValidator, ContainerConstraints.ofMap(constraints)));
	}

	/**
	 * Adds the next argument, validated by the given validator of the object that the
	 * other arguments are read from.
	 *
	 * <p>
	 * With {@code fromMap}, the validator reads the same map as the other arguments
	 * instead of the value of a key, and its violations are reported as they are. Its
	 * properties are defined for the built validator, and with {@code optional()},
	 * {@code nullable()} or {@code defaultValue} it is given {@code null} when none of
	 * them has a value; see {@link am.ik.yavi.input.MapInputValidator}. With
	 * {@code apply}, the argument is a map validated by the validator.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the map
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Map<String, Object>, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _embedded(ValueValidator<? super Map<String, Object>, T> validator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.embedded(validator));
	}

	/**
	 * Adds the next argument as a {@code Map} of the properties that no other argument
	 * is defined for, whose values are validated by the given value validator.
	 *
	 * <p>
	 * With {@code fromMap}, the argument is read from the keys of the map that neither
	 * an argument nor an embedded validator defines, and it is an empty map if there is
	 * none. The violations of the value for the key {@code key} are located under
	 * {@code key}; give {@code valueValidator} an empty name to report a violation on
	 * the value itself. With {@code apply}, the argument is a map whose values are
	 * validated.
	 * @param <V> type of the values validated by {@code valueValidator}
	 * @param <T> type of the values produced by {@code valueValidator}
	 * @param valueValidator value validator for each value
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <V, T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Map<String, V>, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Map<String, T>> _rest(ValueValidator<V, T> valueValidator) {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.rest(valueValidator));
	}

	/**
	 * Adds the next argument as a {@code Map} of the properties that no other argument
	 * is defined for, whose values are taken as they are.
	 * @return a builder for the next argument
	 * @since 0.18.0
	 * @see #_rest(ValueValidator)
	 */
	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Map<String, Object>, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Map<String, Object>> _rest() {
		return new ${next_class}<>(${prev_definitions}ArgumentDefinition.rest());
	}
EOD
fi)

$(if [ "${i}" -gt 1 ];then
cat <<EOD
	/**
	 * Builds an {@link Arguments${i}Validator} from the added arguments and the given
	 * function.
	 *
	 * <p>
	 * The function is applied only when all arguments are valid. The validator describes
	 * its arguments as {@link am.ik.yavi.core.ValueDescription.Properties} with one
	 * property per argument; an argument added without a name is named as
	 * {@link am.ik.yavi.core.ValueDescription.Properties#ofArguments} states.
	 * @param <X> type of the result
	 * @param f function that creates the result from the validated values
	 * @return a new arguments validator
	 * @since 0.14.0
	 */
	public <X> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> apply(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
		return ArgumentsValidators.split($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}.validator(), ";done) | sed 's/,$//')).<X>apply(f)
			.describedAs(ArgumentDefinition.describe($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}, ";done) | sed 's/,$//')));
	}

	/**
	 * Builds an {@link Arguments${i}Validator} from the added arguments and the given
	 * function, which validates the arguments together and returns the result or the
	 * violations. This is {@link #apply} for a function that can fail, such as a
	 * factory method that checks a rule across the arguments.
	 *
	 * <p>
	 * The function is applied only when all arguments are valid. A violation it
	 * reports is located relative to the arguments: one with an empty name, as
	 * {@link Validated#failureWith(am.ik.yavi.core.ViolationMessage, Object, java.util.Locale, Object...)}
	 * creates, is reported on the arguments as a whole.
	 * @param <X> type of the result
	 * @param f function that creates the result from the validated values or reports
	 * violations
	 * @return a new arguments validator
	 * @since 0.18.0
	 */
	public <X> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> flatApply(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends Validated<? extends X>> f) {
		return ArgumentsValidators.split($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}.validator(), ";done) | sed 's/,$//')).<Validated<? extends X>>apply(f)
			.<X>andThen((validated, locale, constraintContext) -> Validated.of(Objects.requireNonNull(validated)))
			.describedAs(ArgumentDefinition.describe($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}, ";done) | sed 's/,$//')));
	}
EOD
fi)
$(if [ "${i}" == "1" ];then
cat <<EOD
	/**
	 * Builds an {@link Arguments1Validator} from the added argument and the given
	 * function.
	 *
	 * <p>
	 * The function is applied only when the argument is valid.
	 * @param <X> type of the result
	 * @param f function that creates the result from the validated value
	 * @return a new arguments validator
	 * @since 0.14.0
	 */
	public <X> Arguments1Validator<A1, X> apply(Function1<? super R1, ? extends X> f) {
		return Arguments1Validator.from(this.d1.validator().andThen(f::apply));
	}

	/**
	 * Builds an {@link Arguments1Validator} from the added argument and the given
	 * function, which validates the argument further and returns the result or the
	 * violations. This is {@link #apply} for a function that can fail.
	 *
	 * <p>
	 * The function is applied only when the argument is valid. A violation it reports
	 * is located relative to the argument: one with an empty name, as
	 * {@link Validated#failureWith(am.ik.yavi.core.ViolationMessage, Object, java.util.Locale, Object...)}
	 * creates, is reported on the argument itself.
	 * @param <X> type of the result
	 * @param f function that creates the result from the validated value or reports
	 * violations
	 * @return a new arguments validator
	 * @since 0.18.0
	 */
	public <X> Arguments1Validator<A1, X> flatApply(Function1<? super R1, ? extends Validated<? extends X>> f) {
		return Arguments1Validator.from(this.d1.validator()
			.<X>andThen((value, locale, constraintContext) -> Validated.of(f.apply(value))));
	}

	/**
	 * Returns a validator that validates the added argument and returns its validated
	 * value as the result.
	 * @return a new arguments validator
	 * @since 0.14.0
	 */
	public Arguments1Validator<A1, R1> get() {
		return Arguments1Validator.from(this.d1.validator());
	}
EOD
fi)
$(if [ "${i}" -gt 0 ];then
cat <<EOD

	/**
	 * Builds a {@link MapInputValidator} that reads the added arguments from a
	 * {@code Map<String, Object>}, such as a parsed JSON object, by their names and
	 * creates the result with the given function.
	 *
	 * <p>
	 * Each value of the map must represent the type of its argument (see
	 * {@link ValueValidator#untyped(Class)}) and is then validated like the argument. An
	 * absent key is validated like {@code null} and an unknown key is ignored; see
	 * {@link MapInputValidator} for the violations of absent keys and for rejecting
	 * unknown keys. An {@code _embedded} argument reads the map itself and a
	 * {@code _rest} argument the keys that no other argument defines. A nested
	 * {@code _object}, {@code _list} or
	 * {@code _map} argument reads a map or a list whose content is checked by the
	 * {@linkplain ValueValidator#untyped() untyped} counterpart of its validator, so a
	 * nested object is added with another {@code MapInputValidator}.
	 * @param <X> type of the result
	 * @param f function that creates the result from the validated values
	 * @return a new map input validator
	 * @throws IllegalStateException if an argument has no name, or if its input type is
	 * unknown: an {@code _enum} or {@code _object} argument added without a type, or a
	 * value validator that does not know its input type; also if there is a
	 * {@code _rest} argument and an embedded validator does not describe its properties
	 * @throws IllegalArgumentException if two arguments define a property of the same
	 * name, or if two are added with {@code _rest}
	 * @since 0.18.0
	 */
	@SuppressWarnings("unchecked")
	public <X> MapInputValidator<X> fromMap(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
		return MapInputValidator.of(List.of($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}.part(${j}), ";done) | sed 's/,$//')),
				values -> f.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "(R${j}) values.get($((j - 1))), ";done) | sed 's/,$//')));
	}

	/**
	 * Builds a {@link MapInputValidator} that reads the added arguments like
	 * {@link #fromMap} and creates the result with the given function, which validates
	 * the arguments together and returns the result or the violations. This is
	 * {@code fromMap} for a function that can fail, such as a factory method that
	 * checks a rule across the properties.
	 *
	 * <p>
	 * The function is applied only when all arguments are valid. A violation it
	 * reports is located relative to the map: one with an empty name, as
	 * {@link Validated#failureWith(am.ik.yavi.core.ViolationMessage, Object, java.util.Locale, Object...)}
	 * creates, is reported on the map itself and takes the name of the property the
	 * validator is applied to. Read another tree model with
	 * {@code flatFromMap(f).readingWith(reader)}.
	 * @param <X> type of the result
	 * @param f function that creates the result from the validated values or reports
	 * violations
	 * @return a new map input validator
	 * @throws IllegalStateException as {@link #fromMap} does
	 * @throws IllegalArgumentException as {@link #fromMap} does
	 * @since 0.18.0
	 */
	@SuppressWarnings("unchecked")
	public <X> MapInputValidator<X> flatFromMap(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends Validated<? extends X>> f) {
		return MapInputValidator.flatOf(List.of($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}.part(${j}), ";done) | sed 's/,$//')),
				values -> f.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "(R${j}) values.get($((j - 1))), ";done) | sed 's/,$//')));
	}

	/**
	 * Builds a validator that reads the added arguments from an object of the given
	 * reader, such as a node of the tree model of a JSON library, by their names and
	 * creates the result with the given function. The tree is read as it is, without
	 * being copied to a map.
	 *
	 * <p>
	 * This is {@code fromMap(f).readingWith(reader)}: the rules are those of
	 * {@link #fromMap}, and the reader is applied to the nested validators too, whatever
	 * input they were built for.
	 * @param <N> the type of the nodes
	 * @param <X> type of the result
	 * @param reader the reader of the nodes
	 * @param f function that creates the result from the validated values
	 * @return a new validator of a node
	 * @throws IllegalStateException if an argument has no name, or if its input type is
	 * unknown
	 * @throws UnsupportedOperationException if a nested validator does not know its
	 * input type
	 * @since 0.18.0
	 * @see ValueValidator#readingWith(InputReader)
	 */
	public <N, X> ValueValidator<N, X> from(InputReader<N> reader, Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
		return this.<X>fromMap(f).readingWith(reader);
	}
EOD
fi)
}
EOF
done
