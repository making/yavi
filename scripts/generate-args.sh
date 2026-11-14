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
	 * No validation is performed and the arguments are passed as they are. An argument
	 * that is {@code null} is passed as {@code null}, so the function must accept
	 * {@code null} for every argument that has not been validated to be present, for
	 * example with {@code notNull()}.
	 *
	 * @param <X> the type of the result
	 * @param mapper the function to apply to the arguments
	 * @return the result of applying the mapper function to the arguments
	 */
	// A raw argument may be null, and the function is trusted to take it: its
	// parameters are not @Nullable so that ArgumentsValidatorBuilder.of(User::new)
	// compiles. Yavi.schema() gives the validated values with their types instead.
	@SuppressWarnings("NullAway")
	public <X extends @Nullable Object> X map(Function${i}<${function_params}, ? extends X> mapper) {
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
 * The rules validate the raw arguments, so an argument is {@code null} unless a rule
 * such as {@code notNull()} rejects it, and the mapper is trusted to take it: a nullness
 * checker cannot tell from the rules which parameter of the mapper may be {@code null}.
 * The builders returned by {@code Yavi.schema()} do tell it: an argument is required
 * there, and {@code nullable()} changes the type of its validated value.
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
		private ValidatorBuilder<Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>> builder = ValidatorBuilder.of();

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
		 * Builds the validator from the configured rules and the mapper. If no rules
		 * were set with {@link #builder}, the validator has no constraints and only
		 * maps the arguments.
		 * @return a new arguments validator
		 * @since 0.3.0
		 */
		public Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> build() {
			return new DefaultArguments${i}Validator<>(this.builder.build(), this.mapper);
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
			}

			@Override
			public Arguments1Validator<A1, X2> named(String name) {
				return Arguments1Validator.this.named(name).map(mapper);
			}"
    untyped_and_then="
			@Override
			public <N> ValueValidator<N, X2> readingWith(InputReader<N> reader) {
				return Arguments1Validator.this.readingWith(reader).andThen(validator);
			}

			@Override
			public Arguments1Validator<A1, X2> preprocess(Function<? super A1, ? extends @Nullable A1> transform) {
				return Arguments1Validator.this.preprocess(transform).andThen(validator);
			}

			@Override
			public Arguments1Validator<A1, X2> named(String name) {
				return Arguments1Validator.this.named(name).andThen(validator.named(name));
			}"
    untyped_described_as="
			@Override
			public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
				return Arguments1Validator.this.readingWith(reader).describedAs(description);
			}

			@Override
			public Arguments1Validator<A1, X> preprocess(Function<? super A1, ? extends @Nullable A1> transform) {
				return Arguments1Validator.this.preprocess(transform).describedAs(description);
			}

			@Override
			public Arguments1Validator<A1, X> named(String name) {
				return Arguments1Validator.this.named(name).describedAs(description.named(name));
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
import am.ik.yavi.core.TargetConstraints;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import org.jspecify.annotations.NonNull;
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
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.TargetConstraints;
import am.ik.yavi.core.Validatable;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
EOD
fi)

/**
 * Validates ${i} argument${argument_suffix} and produces a value of type {@code X}.
 *
 * <p>
 * Validation is applicative: all violations are accumulated in the returned
 * {@link Validated} instead of stopping at the first one, and a value is produced only
 * when every constraint succeeds. Whether a {@code null} argument is accepted depends
 * on how the validator is built: an argument added to the builders of
 * {@code Yavi.schema()} is required unless it is made {@code nullable()} or
 * {@code optional()}, and a validator built with
 * {@link am.ik.yavi.builder.ArgumentsValidatorBuilder} accepts {@code null} unless a
 * configured constraint rejects it (for example {@code notNull()}); see
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
public interface ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X extends @Nullable Object> extends ValueValidator<A1, X> {

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
	static <A1, X extends @Nullable Object> Arguments1Validator<A1, X> from(ValueValidator<A1, X> valueValidator) {
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

			@Override
			public Arguments1Validator<A1, X> named(String name) {
				return Arguments1Validator.from(valueValidator.named(name));
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
	static <A1, X extends @Nullable Object> Arguments1Validator<A1, X> unwrap(Arguments1Validator<Arguments1<A1>, X> validator) {
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
public interface ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X extends @Nullable Object> {

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
	static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X extends @Nullable Object> ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> unwrap(
			Arguments1Validator<${wrapArguments}, X> validator) {
		return ArgumentsValidators.derived((${as}, locale, constraintContext) -> validator.validate(Arguments.of(${as}), locale, constraintContext),
				() -> ${class}.unwrap(validator.lazy()), validator::description);
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
	 * Returns a validator that maps the value produced on success with the given
	 * function. The function is not applied when validation fails.
	 *
	 * @param mapper the function applied to the validated value
	 * @param <X2> the type of the value produced after mapping
	 * @return a validator with the mapped result type
	 * @since 0.17.0
	 */$(if [ "${i}" == "1" ];then echo;echo "	@Override"; fi)
	default <X2 extends @Nullable Object> ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X2> map(Function<? super X, ? extends X2> mapper) {
$(if [ "${i}" == "1" ];then
cat <<EOD
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
EOD
else
cat <<EOD
		return ArgumentsValidators.derived((${as}, locale, constraintContext) -> this.validate(${as}, locale, constraintContext).map(mapper),
				() -> this.lazy()
					.map((Function<Supplier<X>, Supplier<X2>>) xSupplier -> () -> mapper.apply(xSupplier.get())),
				this::description);
EOD
fi)
	}

	/**
	 * Returns a validator that passes the value produced on success to the given
	 * validator.
	 *
	 * <p>
	 * If this validator fails, the given validator is not invoked and its violations are
	 * not collected. The violations of the given validator are reported with the
	 * locale of the validation, also if it does not pass the locale on.
	 *
	 * @param validator the validator applied to the validated value
	 * @param <X2> the type of the value produced by the given validator
	 * @return a chained validator
	 * @since 0.11.0
	 */$(if [ "${i}" == "1" ];then echo;echo "	@Override"; fi)
	default <X2 extends @Nullable Object> ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X2> andThen(ValueValidator<? super @NonNull X, ? extends X2> validator) {
$(if [ "${i}" == "1" ];then
cat <<EOD
		return new ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X2>() {
			@Override
			public Validated<X2> validate($(echo $(for j in `seq 1 ${i}`;do echo -n "@Nullable A${j} a${j}, ";done) | sed 's/,$//'), Locale locale, ConstraintContext constraintContext) {
				return ${class}.this.validate(${as}, locale, constraintContext)
					.flatMap(v -> validator.validate(v, locale, constraintContext).localizedWith(locale));
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
EOD
else
cat <<EOD
		return ArgumentsValidators.derived((${as}, locale, constraintContext) -> this.validate(${as}, locale, constraintContext)
					.flatMap(v -> validator.validate(v, locale, constraintContext).localizedWith(locale)),
				() -> this.lazy().andThen((xSupplier, locale, constraintContext) -> validator
					.validate(Objects.requireNonNull(xSupplier).get(), locale, constraintContext)
					.map(x2 -> () -> x2)),
				this::description);
EOD
fi)
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
	 * <p>
	 * Rules added one after another are all checked on the produced value, and every
	 * violated rule is reported, as {@link ValueValidator#constraintOnTarget} states. A
	 * rule that is checked only when other rules hold is added after {@link #map} or
	 * {@link #andThen(ValueValidator)}.
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
		return ArgumentsValidators.constrained(this,
				TargetConstraints.<X>empty().and(predicate, name, violationMessage));
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
$(if [ "${i}" == "1" ];then
cat <<EOD
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
EOD
else
cat <<EOD
		return ArgumentsValidators.derived(this, () -> this.lazy().describedAs(description), () -> description);
EOD
fi)
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

			@Override
			public Arguments1Validator<A1, X> named(String name) {
				return Arguments1Validator.this.named(name).preprocess(transform);
			}
		};
	}

	/**
	 * Returns a validator that reports the violations of the validated argument under
	 * the given name, as {@link ValueValidator#named(String)} states.
	 *
	 * @param name the name under which the violations of the argument are reported
	 * @return a validator of the argument under the given name
	 * @since 0.18.0
	 */
	@Override
	default Arguments1Validator<A1, X> named(String name) {
		return Arguments1Validator.from(ValueValidator.super.named(name));
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
EOD)
$(if [ "${i}" != "1" ];then
cat <<EOD

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
EOD
fi)
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
	default <A$((${i} + 1)), Y extends @Nullable Object> Arguments$((${i} + 1))Splitting<$(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "A${j}, ";done) | sed 's/,$//'), X, Y> split(ValueValidator<A$((${i} + 1)), Y> validator) {
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
	default <Y extends @Nullable Object> Arguments$((${i} + 1))Combining<A${i}, X, Y> combine(ValueValidator<A${i}, Y> validator) {
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
	default <C extends Collection<X>> Arguments1Validator<Iterable<? extends A1>, C> liftCollection(
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
	default Arguments1Validator<Iterable<? extends A1>, List<X>> liftList() {
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
	default Arguments1Validator<Iterable<? extends A1>, Set<X>> liftSet() {
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
	default Arguments1Validator<Optional<? extends A1>, Optional<@NonNull X>> liftOptional() {
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

$(if [ "${i}" == "1" ];then echo "import java.util.ArrayList;"; echo "import java.util.List;"; fi)
import java.util.Locale;
$(if [ "${i}" == "1" ];then echo "import java.util.function.Function;"; fi)
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;$(if [ "${i}" == "1" ];then printf '\nimport am.ik.yavi.core.ConstraintDescription;\nimport am.ik.yavi.core.ConstraintViolation;\nimport am.ik.yavi.core.InputReader;'; fi)
import am.ik.yavi.core.Validated;
$(if [ "${i}" == "1" ];then echo "import am.ik.yavi.core.ValueDescription;"; fi)
import am.ik.yavi.core.Validator;
$(if [ "${i}" == "1" ];then echo "import am.ik.yavi.core.ValueValidator;"; echo "import am.ik.yavi.core.ViolationMessage;"; fi)
import am.ik.yavi.fn.Function${i};
import org.jspecify.annotations.Nullable;

/**
 * Default {@link ${interface}} that applies a {@link Validator} to an
 * {@link Arguments${i}} container and maps the validated arguments with a
 * {@link Function${i}}.
 *
 * <p>
 * The validator is applied applicatively, so all violations are accumulated, and the
 * mapper is applied only when validation succeeds.
$(if [ "${i}" == "1" ];then
cat <<EOD
 *
 * <p>
 * A validator created with the public constructor validates a raw argument: a
 * {@code null} argument is accepted unless the configured constraints reject it, and is
 * then passed to the mapper as it is. The validators of a single value, which are the
 * subclasses of this class, require the value instead: a {@code null} argument that
 * the constraints accept is rejected with {@code object.notNull}, so the mapper never
 * receives {@code null}. Such a validator accepts {@code null} with
 * {@link ValueValidator#nullable()}, {@link ValueValidator#optional()} or
 * {@link ValueValidator#defaultValue(Object)}.
EOD
else
cat <<EOD
 * A {@code null} argument is accepted unless the configured constraints reject it,
 * and is then passed to the mapper as it is.
EOD
fi)
 *
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
$(for j in `seq 1 ${i}`; do echo " * @param <A${j}> the type of the argument at position ${j}"; done)
 * @param <X> the type of the value produced when validation succeeds
 * @since 0.7.0
 */
public class ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X extends @Nullable Object> implements ${interface}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> {

	/** The validator applied to the {@link Arguments${i}} container. */
  protected final Validator<${arguments}> validator;

	/** The mapper applied to the validated arguments. */
	protected final Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends X> mapper;

$(if [ "${i}" == "1" ];then
cat <<EOD
	private final Class<?> valueType;

	private final boolean required;

	/** The name under which a missing required value is reported. */
	private final String name;


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
		this(validator, mapper, Object.class, null, false);
	}

	/**
	 * Creates a validator of a single value that requires the value and knows its raw
	 * type, which is reported by {@link #description()}. Unlike a validator created with
	 * {@link #DefaultArguments1Validator(Validator, Function1)}, it rejects a
	 * {@code null} argument that the given validator accepts with
	 * {@code object.notNull}.
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
		this(validator, mapper, valueType, transform, true);
	}

	private ${class}(Validator<${arguments}> validator, Function1<? super A1, ? extends X> mapper, Class<?> valueType,
			@Nullable Function<? super A1, ? extends @Nullable A1> transform, boolean required) {
		this.validator = validator;
		this.mapper = mapper;
		this.valueType = valueType;
		this.transform = transform;
				this.required = required;
		// the name of the first constrained property, as ValueDescription.Value.of names
		// the value; the constraints are not described here, which would evaluate their
		// arguments
		final List<String> names = new ArrayList<>();
		validator.forEachPredicates(predicates -> names.add(predicates.name()));
		this.name = names.isEmpty() ? "" : names.get(0);
	}

	/**
	 * Returns the description of the argument. A required value whose constraints
	 * accept {@code null} is described with the {@code object.notNull} constraint that
	 * it is rejected with.
	 */
	private ValueDescription.Value describe() {
		final ValueDescription.Value value = ValueDescription.Value.of(this.valueType, this.validator);
		if (!this.required || !value.acceptsNull()) {
			return value;
		}
		final List<ConstraintDescription> constraints = new ArrayList<>(value.constraints().size() + 1);
		constraints.add(new ConstraintDescription(ViolationMessage.Default.OBJECT_NOT_NULL.messageKey(), List.of(), true));
		constraints.addAll(value.constraints());
		return new ValueDescription.Value(value.name(), value.type(), constraints);
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
				composed(transform, this.transform), this.required);
	}

	/**
	 * Returns a validator with the same constraints that reports the violations of the
	 * argument under the given name instead of the name it was built with, as
	 * {@link ValueValidator#named(String)} states. The name is the one of the
	 * {@linkplain #description() description} too.
	 *
	 * @param name the name under which the violations of the argument are reported
	 * @return a validator of the argument under the given name
	 * @since 0.18.0
	 */
	@Override
	public DefaultArguments1Validator<A1, X> named(String name) {
		return new DefaultArguments1Validator<>(this.renamed(name), this.mapper, this.valueType, this.transform,
				this.required);
	}

	/**
	 * Returns the validator of the argument with the given name for the argument, for
	 * the subclasses to create themselves under another name.
	 *
	 * @param name the name under which the violations of the argument are reported
	 * @return the validator applied to the argument, under the given name
	 * @since 0.18.0
	 */
	protected final Validator<Arguments1<A1>> renamed(String name) {
		final String current = this.name;
		return this.validator.renamed(constrained -> constrained.equals(current) ? name : constrained);
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
	 * the constraints of the validator. A required value whose constraints accept
	 * {@code null} is described with the {@code object.notNull} constraint first.
	 *
	 * @return the description of the argument
	 * @since 0.18.0
	 */
	@Override
	public ValueDescription description() {
		return this.describe();
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
		return ValueValidator.<A1>passThrough()
			.untyped((Class<A1>) this.valueType)
			.readingWith(reader)
			.named(this.name)
			.andThen(this)
			.describedAs(this.describe());
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
		return new ${class}<>(this.validator, ($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//')) -> () -> this.mapper.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//'))$(if [ "${i}" == "1" ];then echo -n ", this.valueType, this.transform, this.required"; fi));
	}

	/**
	 * Validates the arguments with the given validator and maps them with the configured
	 * mapper.$(if [ "${i}" == "1" ];then echo " A required value that is {@code null} is rejected by the constraints";echo "	 * if one of them rejects it, and with {@code object.notNull} otherwise."; fi)
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
		final Validated<Arguments1<A1>> validated = this.validator.applicative()
			.validate(Arguments.of(value), locale, constraintContext);
		if (!this.required) {
			return validated.map(values -> values.map(this.mapper));
		}
		if (!validated.isValid()) {
			return Validated.failureWith(validated.errors());
		}
		if (value == null) {
			return Validated.failureWith(ConstraintViolation.of(ViolationMessage.Default.OBJECT_NOT_NULL, null, locale)
				.prefixed(this.name));
		}
		return Validated.successWith(this.mapper.apply(value));
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

import am.ik.yavi.core.ValidatedCombining${i};
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.fn.Function${i};
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
public class ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j} extends @Nullable Object, ";done) | sed 's/,$//')> {
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
	public <X extends @Nullable Object> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> apply(
			Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
		return ArgumentsValidators.derived(
				($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//'), locale, constraintContext) -> new ValidatedCombining${i}<>(
						$(echo $(for j in `seq 1 ${i}`;do echo -n "v${j}.validate(a${j}, locale, constraintContext), ";done) | sed 's/,$//')).apply(f::apply),
				() -> ($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//'), locale, constraintContext) -> new ValidatedCombining${i}<>(
						$(echo $(for j in `seq 1 ${i}`;do echo -n "v${j}.validate(a${j}, locale, constraintContext), ";done) | sed 's/,$//'))
					.apply(($(echo $(for j in `seq 1 ${i}`;do echo -n "r${j}, ";done) | sed 's/,$//')) -> () -> f.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "r${j}, ";done) | sed 's/,$//'))),
				() -> ValueDescription.Properties.ofArguments($(echo $(for j in `seq 1 ${i}`;do echo -n "v${j}.description(), ";done) | sed 's/,$//')));
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
  echo "	public <A$((${i} + 1)), R$((${i} + 1)) extends @Nullable Object> Arguments$((${i} + 1))Splitting<$(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "R${j}, ";done) | sed 's/,$//')> split(ValueValidator<? super A$((${i} + 1)), ? extends R$((${i} + 1))> v$((${i} + 1))) {"
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
import am.ik.yavi.core.ValidatedCombining${i};
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.fn.Function${i};
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
public class ${class}<A, $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j} extends @Nullable Object, ";done) | sed 's/,$//')> {

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
	public <X extends @Nullable Object> Arguments1Validator<A, X> apply(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
		return new Arguments1Validator<A, X>() {
			@Override
			public Validated<X> validate(@Nullable A a, Locale locale, ConstraintContext constraintContext) {
				return new ValidatedCombining${i}<>($(echo $(for j in `seq 1 ${i}`;do echo -n "${class}.this.v${j}.validate(a, locale, constraintContext), ";done) | sed 's/,$//')).apply(f::apply);
			}

			@Override
			public Arguments1Validator<A, Supplier<X>> lazy() {
				return (a, locale, constraintContext) -> new ValidatedCombining${i}<>(
						$(echo $(for j in `seq 1 ${i}`;do echo -n "${class}.this.v${j}.validate(a, locale, constraintContext), ";done) | sed 's/,$//'))
					.apply(($(echo $(for j in `seq 1 ${i}`;do echo -n "r${j}, ";done) | sed 's/,$//')) -> () -> f.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "r${j}, ";done) | sed 's/,$//')));
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
  echo "	public <R$((${i} + 1)) extends @Nullable Object> Arguments$((${i} + 1))Combining<A,  $(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "R${j}, ";done) | sed 's/,$//')> combine(ValueValidator<? super A, ? extends R$((${i} + 1))> v$((${i} + 1))) {"
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
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.TargetConstraints;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

$(for i in `seq 1 ${nn}`;do
tas=$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')
cat <<EOD
	/**
	 * Returns a validator that checks the value produced by the given validator with
	 * the given rules, and that checks a rule added with {@code constraintOnTarget}
	 * together with them.
	 */
	static <${tas}, X extends @Nullable Object> Arguments${i}Validator<${tas}, X> constrained(Arguments${i}Validator<${tas}, X> validator,
			TargetConstraints<X> constraints) {
		return new Arguments${i}Validator<${tas}, X>() {
			@Override
			public Validated<X> validate($(echo $(for j in `seq 1 ${i}`;do echo -n "@Nullable A${j} a${j}, ";done) | sed 's/,$//'), Locale locale, ConstraintContext constraintContext) {
				return validator.validate($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//'), locale, constraintContext)
					.flatMap(x -> constraints.check(x, locale));
			}

			@Override
			public Arguments${i}Validator<${tas}, Supplier<X>> lazy() {
				return validator.lazy()
					.andThen((xSupplier, locale, constraintContext) -> constraints
						.check(Objects.requireNonNull(xSupplier).get(), locale)
						.map(x -> () -> x));
			}

			@Override
			public ValueDescription description() {
				return validator.description();
			}

			@Override
			public Arguments${i}Validator<${tas}, X> constraintOnTarget(Predicate<? super X> predicate, String name,
					ViolationMessage violationMessage) {
				return constrained(validator, constraints.and(predicate, name, violationMessage));
			}
$(if [ "${i}" == "1" ];then
cat <<EOS

			@Override
			public Arguments1Validator<A1, X> describedAs(ValueDescription description) {
				return constrained(validator.describedAs(description), constraints);
			}

			@Override
			public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
				return constraints.constrain(validator.readingWith(reader));
			}

			@Override
			public Arguments1Validator<A1, X> preprocess(Function<? super A1, ? extends @Nullable A1> transform) {
				return constrained(validator.preprocess(transform), constraints);
			}

			@Override
			public Arguments1Validator<A1, X> named(String name) {
				return constrained(validator.named(name), constraints.prefixed(name));
			}
EOS
fi)
		};
	}

EOD
done)
$(for i in `seq 2 ${nn}`;do cat <<EOD
	/**
	 * Returns a validator that validates with the given validator and takes its lazy
	 * variant and its description from the given suppliers.
	 */
	static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X extends @Nullable Object> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> derived(Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> validator,
			Supplier<Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X>>> lazy, Supplier<ValueDescription> description) {
		return new Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X>() {
			@Override
			public Validated<X> validate($(echo $(for j in `seq 1 ${i}`;do echo -n "@Nullable A${j} a${j}, ";done) | sed 's/,$//'), Locale locale, ConstraintContext constraintContext) {
				return validator.validate($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//'), locale, constraintContext);
			}

			@Override
			public Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X>> lazy() {
				return lazy.get();
			}

			@Override
			public ValueDescription description() {
				return description.get();
			}
		};
	}

EOD
done)
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
	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j} extends @Nullable Object, ";done) | sed 's/,$//')> Arguments${i}Splitting<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> split($(echo $(for j in `seq 1 ${i}`;do echo -n "ValueValidator<? super A${j}, ? extends R${j}> v${j}, ";done) | sed 's/,$//')) {
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
	public static <A, $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j} extends @Nullable Object, ";done) | sed 's/,$//')> Arguments${i}Combining<A, $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> combine($(echo $(for j in `seq 1 ${i}`;do echo -n "ValueValidator<? super A, ? extends R${j}> v${j}, ";done) | sed 's/,$//')) {
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
	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), R extends @Nullable Object, T> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), List<R>> traverse${i}(
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
	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), R extends @Nullable Object> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), List<R>> sequence${i}(
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
	public static <A1, R extends @Nullable Object, C extends Collection<R>> Arguments1Validator<Iterable<? extends A1>, C> liftCollection(
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
	public static <A1, R extends @Nullable Object> Arguments1Validator<Iterable<? extends A1>, List<R>> liftList(
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
	public static <A1, R extends @Nullable Object> Arguments1Validator<Iterable<? extends A1>, Set<R>> liftSet(
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
	public static <A1, R extends @Nullable Object> Arguments1Validator<Optional<? extends A1>, Optional<@NonNull R>> liftOptional(
			ValueValidator<? super A1, ? extends R> validator) {
		return Arguments1Validator.from(ValueValidator.liftOptional(validator));
	}

}
EOF

# The modifiers of the last added argument. Each function prints one method of a stage
# class of the builder; $1 is the stage class of the returned builder, or the builder
# class itself when the argument takes no further modifier.

stage_type() {
  if [ "$1" == "${class}" ];then echo -n "${class}";else echo -n "$1";fi
}

modifier_optional() {
cat <<EOD
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
		public $1<${init_arguments}A${i}, ${init_results}Optional<@NonNull R${i}>> optional() {
			return new $1<>(${init_definitions}this.d${i}.asOptional());
		}
EOD
}

modifier_nullable() {
cat <<EOD
		/**
		 * Accepts a {@code null} value for the last added argument and produces {@code null}
		 * for it.
		 *
		 * <p>
		 * A {@code null} value succeeds with {@code null} and the constraints of the
		 * argument, such as {@code notNull()}, are not applied to it. Any other value is
		 * validated as before. The type of the validated value becomes nullable, so a
		 * nullness checker requires the function given to {@code apply} to accept
		 * {@code null} for this argument.
		 * @return a builder whose last argument accepts {@code null}
		 * @since 0.18.0
		 * @see ValueValidator#nullable()
		 */
		public $1<${init_arguments}A${i}, ${init_results}@Nullable R${i}> nullable() {
			return new $1<>(${init_definitions}this.d${i}.asNullable());
		}
EOD
}

modifier_default_value() {
cat <<EOD
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
		 * @throws NullPointerException if {@code defaultValue} is {@code null} although the
		 * validated value is never {@code null}, as that of constraints, a list or a map
		 * is; {@code nullable()} is what produces {@code null}
		 * @since 0.18.0
		 * @see ValueValidator#defaultValue(Object)
		 */
		public $1<${type_uses}> defaultValue(R${i} defaultValue) {
			return new $1<>(${init_definitions}this.d${i}.withDefault(defaultValue));
		}
EOD
}

modifier_recover() {
cat <<EOD
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
		 * @throws NullPointerException if {@code fallback} is {@code null} although the
		 * validated value is never {@code null}, as that of constraints, a list, a map or
		 * {@code optional()} is; write {@code nullable()} before to recover with
		 * {@code null}
		 * @since 0.18.0
		 * @see ValueValidator#recover(Object)
		 */
		public $1<${type_uses}> recover(R${i} fallback) {
			return new $1<>(${init_definitions}this.d${i}.recovering(fallback));
		}

		/**
		 * Produces the result of the given function when the value of the last added
		 * argument is {@code null} or invalid, so that the argument never fails.
		 *
		 * <p>
		 * The function is given the violations of the invalid value, and no violation for a
		 * {@code null} value, to which the constraints of the argument are not applied. It
		 * must not return {@code null} for a validated value that is never {@code null},
		 * as that of constraints, a list, a map or {@code optional()} is: the validation
		 * then fails with {@code NullPointerException}. Write {@code nullable()} before to
		 * recover with {@code null}.
		 * @param fallback the function that produces a value from the violations
		 * @return a builder whose last argument falls back to the result of the function
		 * @since 0.18.0
		 * @see ValueValidator#recoverWith(Function)
		 */
		public $1<${type_uses}> recoverWith(Function<? super ConstraintViolations, ? extends R${i}> fallback) {
			return new $1<>(${init_definitions}this.d${i}.recoveringWith(fallback));
		}
EOD
}

modifier_presence() {
cat <<EOD
		/**
		 * Tells an absent value of the last added argument from {@code null}, which a
		 * partial update such as a PATCH request needs to leave a property unchanged or to
		 * clear it.
		 *
		 * <p>
		 * With {@code fromMap}, an absent key succeeds with {@link Presence.Absent} and
		 * the argument is not validated, so the key is not required. A key that is present
		 * is validated as before, an explicit {@code null} included, and its value is
		 * given as {@link Presence#ofNullable(Object)}: an explicit {@code null} is a
		 * violation unless the argument is {@code nullable()}. An argument that is
		 * {@code optional()} has no {@code presence()}, which would wrap its
		 * {@code Optional}: {@code nullable()} gives the three states.
		 * A parameter with an empty value, which is read as {@code null}, is present.
		 *
		 * <p>
		 * With {@code apply}, the argument is a {@link Presence} that is validated in the
		 * same way. This is the last modifier of an argument: the returned builder has
		 * none, so {@code optional()}, {@code nullable()}, {@code defaultValue} and
		 * {@code recover} are written before it and apply to the value of a key that is
		 * present.
		 * @return a builder whose last argument is a value that may be absent
		 * @since 0.18.0
		 * @see ValueValidator#liftPresence()
		 */
		public ${class}<${init_arguments}Presence<? extends A${i}>, ${init_results}Presence<@NonNull R${i}>> presence() {
			return new ${class}<>(${init_definitions}this.d${i}.withPresence());
		}
EOD
}

modifier_required() {
cat <<EOD
		/**
		 * Requires the key of the last added argument although its value may be
		 * {@code null}, for a contract such as "{@code deletedAt} must be sent, as
		 * {@code null} if there is none".
		 *
		 * <pre>{@code
		 * ._instant("deletedAt").nullable().required()
		 * }</pre>
		 *
		 * <p>
		 * With {@code fromMap}, an absent key is rejected with {@code object.required}
		 * and the argument is not validated. A key that is present is validated as
		 * before, so an explicit {@code null} is accepted by {@code optional()},
		 * {@code nullable()}, {@code defaultValue} and {@code recover}, which are written
		 * before this modifier. The argument is described as a
		 * {@link am.ik.yavi.core.ValueDescription.Required}, so its JSON Schema is {@code required} and
		 * allows the type {@code null}. An argument that does not accept {@code null}
		 * requires its key anyway, so its builder does not have this modifier.
		 *
		 * <p>
		 * With {@code apply}, the argument has no absent state and is validated as
		 * before. This is the last modifier of an argument: the returned builder has
		 * none.
		 * @return a builder whose last argument requires its key
		 * @since 0.18.0
		 * @see #rejectingNull()
		 */
		public ${class}<${init_arguments}A${i}, ${init_results}R${i}> required() {
			return new ${class}<>(${init_definitions}this.d${i}.withStrictness(ObjectInputValidator.Strictness.REQUIRED));
		}
EOD
}

modifier_rejecting_null() {
cat <<EOD
		/**
		 * Rejects an explicit {@code null} for the last added argument although its key
		 * may be absent, for a contract such as "{@code bio} may be omitted, and is a
		 * text if it is sent".
		 *
		 * <pre>{@code
		 * ._string("bio").optional().rejectingNull() // Optional.empty() only for an absent key
		 * ._integer("page").defaultValue(1).rejectingNull() // 1 only for an absent key
		 * }</pre>
		 *
		 * <p>
		 * With {@code fromMap}, an explicit {@code null} is rejected with
		 * {@code object.notNull} and the argument is not validated. An absent key is
		 * validated as {@code null}, as before, so it is what {@code optional()},
		 * {@code nullable()}, {@code defaultValue} and {@code recover}, which are written
		 * before this modifier, accept: the empty {@code Optional}, the {@code null} or
		 * the default value of the validated argument stands for an absent key only. The
		 * argument is described as a {@link am.ik.yavi.core.ValueDescription.Presence}, so its JSON
		 * Schema is not {@code required} and does not allow the type {@code null}.
		 * A parameter with an empty value, which is read as {@code null}, is present and
		 * therefore rejected.
		 *
		 * <p>
		 * With {@code apply}, the argument has no absent state and is validated as
		 * before: {@code null} is the absent value. This is the last modifier of an
		 * argument: the returned builder has none. To be given the three states, use
		 * {@code presence()} of an argument that is {@code nullable()}.
		 * @return a builder whose last argument rejects an explicit {@code null}
		 * @since 0.18.0
		 * @see #required()
		 */
		public ${class}<${init_arguments}A${i}, ${init_results}R${i}> rejectingNull() {
			return new ${class}<>(${init_definitions}this.d${i}.withStrictness(ObjectInputValidator.Strictness.REJECTING_NULL));
		}
EOD
}

# Prints the modifiers of the messages of an argument read from a key: $1 is the stage
# class that has them, which they return, and $2 its type arguments if they are not those
# of the builder. A stage that is left by a modifier that is written once, such as
# withheld(), overrides them to stay itself: a label does not decide the order.
modifier_messages() {
local uses="${2:-${type_uses}}"
cat <<EOD
		@Override
		public $1<${uses}> label(String label) {
			return new $1<>(${init_definitions}this.d${i}.withMessages(this.d${i}.messages().withLabel(label)));
		}

		@Override
		public $1<${uses}> typeMessage(ViolationMessage message) {
			return new $1<>(${init_definitions}this.d${i}.withMessages(this.d${i}.messages().withType(message)));
		}
EOD
}

# Prints the modifier that withholds an argument read from a key: $1 is the stage class
# that it returns, which does not have it.
modifier_withheld() {
cat <<EOD
		@Override
		public $1<${type_uses}> withheld() {
			return new $1<>(${init_definitions}this.d${i}.asWithheld());
		}
EOD
}

# Prints the modifiers that give the message of a missing value: $1 is what is written
# before ".withMessages" to require the key, and $2 what the javadoc says about the key.
modifier_required_message() {
cat <<EOD
		/**
		 * Requires $2 and reports a
		 * missing value with the given text instead of the message of
		 * {@code object.required}, for a value that only a person can tell how to give,
		 * such as a group of checkboxes that sends no key when no box is checked.
		 *
		 * <pre>{@code
		 * ._list("roles", Yavi._string()).required("Select at least one role")
		 * }</pre>
		 *
		 * <p>
		 * The text is the message: a message formatter that has a message for
		 * {@code object.required} does not replace it (see
		 * {@link ViolationMessage#withText(String)}). The message key
		 * {@code object.required} and the arguments are kept:
		 * <code>{0}</code> is the name or the label of the argument. With
		 * {@code fromMap}, the message is reported for an absent key and for text without
		 * a value. An explicit {@code null} is not missing: it is reported by a
		 * constraint that rejects it, or else with {@code object.notNull}.
		 *
		 * <p>
		 * With {@code apply}, the argument has no absent state and is validated as
		 * before. This is the last modifier of an argument: the returned builder has
		 * none, so one that would accept a missing value cannot follow.
		 * @param messageFormat the text of the message of a missing value
		 * @return a builder whose last argument requires its key
		 * @since 0.18.0
		 */
		public ${class}<${init_arguments}A${i}, ${init_results}R${i}> required(String messageFormat) {
			return this.required(ViolationMessage.Default.OBJECT_REQUIRED.withText(messageFormat));
		}

		/**
		 * Requires $2 and reports a
		 * missing value with the given message instead of the one of
		 * {@code object.required}. Both the message key and the message format are
		 * replaced, so that a resource bundle can hold the message of this argument.
		 * @param message the message of a missing value
		 * @return a builder whose last argument requires its key
		 * @since 0.18.0
		 * @see #required(String)
		 */
		public ${class}<${init_arguments}A${i}, ${init_results}R${i}> required(ViolationMessage message) {
			return new ${class}<>(${init_definitions}this.d${i}$1.withMessages(this.d${i}.messages().withRequired(message)));
		}
EOD
}

# The modifier that reads a list also from text that a pattern separates. A builder of
# Yavi.reversibleSchema() is given the text that joins the elements too, because the
# list is written back as the text of one field.
split_by_pattern_plain() {
cat <<EOD
		/**
		 * Reads the last added list also from text whose elements are separated by the
		 * given pattern, such as one element per line of a {@code textarea}.
		 *
		 * <pre>{@code
		 * ._list("ids", Yavi._integer(c -> c.positive())).splitBy(Pattern.compile("\\\\R"))
		 * }</pre>
		 *
		 * <p>
		 * With {@code fromMap}, the value of the key is read as
		 * {@link ValueValidator#liftList(Pattern)} states: a text is split and so is
		 * every text of a list, so {@code tags=a,b&tags=c} is {@code [a, b, c]}; an
		 * element that was split off is text that stands for a value, so that
		 * {@code 1,2,3} is a list of {@code Integer}; and no element is dropped or
		 * trimmed, except that a separator at the end of the text starts no element.
		 * The violations of an element are located under its index, as those of a
		 * list that is not split are ({@code tags[1]}).
		 *
		 * <p>
		 * With {@code apply}, the argument is a list that holds the elements already
		 * and is validated as before. This is written directly after {@code _list}, or
		 * after its {@code label} and {@code typeMessage}: the returned builder has the
		 * other modifiers of the argument.
		 * @param separator the pattern that separates the elements in a text
		 * @return a builder whose last argument may be given as text
		 * @throws IllegalArgumentException if the pattern matches the empty string
		 * @since 0.18.0
		 */
		public Added<${list_type_uses}> splitBy(Pattern separator) {
			return new Added<>(${init_definitions}this.d${i}.separatedBy(separator));
		}
EOD
}

split_by_pattern_reversible() {
cat <<EOD
		/**
		 * Reads the last added list also from text whose elements are separated by the
		 * given pattern, such as one element per line of a {@code textarea}, and writes
		 * it back as text whose elements are joined by the given delimiter.
		 *
		 * <pre>{@code
		 * ._list("urls", Yavi._string(c -> c.url())).splitBy(Pattern.compile("\\\\s*\\\\R\\\\s*"), "\\n")
		 * }</pre>
		 *
		 * <p>
		 * With {@code fromMap}, the value of the key is read as
		 * {@link ValueValidator#liftList(Pattern)} states: a text is split and so is
		 * every text of a list, so {@code tags=a,b&tags=c} is {@code [a, b, c]}; an
		 * element that was split off is text that stands for a value, so that
		 * {@code 1,2,3} is a list of {@code Integer}; and no element is dropped or
		 * trimmed, except that a separator at the end of the text starts no element.
		 * The violations of an element are located under its index, as those of a
		 * list that is not split are ({@code tags[1]}).
		 *
		 * <p>
		 * A validator built with {@code fromMapReversible} writes the list back as the
		 * text of one field, as it does after {@link #splitBy(String)}: the texts of
		 * the elements joined by the delimiter, and {@code null} for an empty list. A
		 * pattern does not tell the text that it separates, so the delimiter is given
		 * with it. A list that is read from one text and written as a list is not what
		 * a field shows, so this builder has no {@code splitBy} of a pattern alone. An
		 * element whose text contains what the pattern matches is not read back as one
		 * element.
		 *
		 * <p>
		 * With {@code apply}, the argument is a list that holds the elements already
		 * and is validated as before. This is written directly after {@code _list}, or
		 * after its {@code label} and {@code typeMessage}: the returned builder has the
		 * other modifiers of the argument.
		 * @param separator the pattern that separates the elements in a text
		 * @param delimiter the text that joins the elements when the list is written,
		 * which the pattern matches as a whole
		 * @return a builder whose last argument may be given as text
		 * @throws IllegalArgumentException if the pattern matches the empty string, or
		 * if it does not match the delimiter, so that the written text would not be
		 * split where it was joined
		 * @since 0.18.0
		 */
		public Added<${list_type_uses}> splitBy(Pattern separator, String delimiter) {
			return new Added<>(${init_definitions}this.d${i}.separatedBy(separator, delimiter));
		}
EOD
}

# Prints the declaration and the constructor of a stage class: $1 is its name, $2 its
# modifiers, $3 the stage class it extends, $4 its permits clause, if any, and $5
# "messages" if it has the modifiers of the messages of an argument read from a key, or
# "withholdable" if it has the modifier that withholds such an argument.
stage_class() {
cat <<EOD
	 *
	 * @param <A1> type of the first argument$(for j in `seq 1 ${i}`;do if [ "${j}" -gt 1 ];then printf '\n\t * @param <A%s> type of argument at position %s' "${j}" "${j}";fi;done)
	 * @param <R1> type of the validated value of the first argument$(for j in `seq 1 ${i}`;do if [ "${j}" -gt 1 ];then printf '\n\t * @param <R%s> type of the validated value of argument at position %s' "${j}" "${j}";fi;done)
	 * @since 0.18.0
	 */
	public static $2 class $1<${type_decls}> extends $3<${type_uses}>$(if [ "$5" == "messages" ];then echo -n " implements MessageModifiers<$1<${type_uses}>>";elif [ "$5" == "withholdable" ];then echo -n " implements Withholdable<$3<${type_uses}>>";fi)$4 {

		$1(${ctor_params}) {
			super(${ctor_args});
		}
EOD
}


# The terminals of the builders of Yavi.reversibleSchema(), whose arguments all know
# their reverse.
reversible_terminals() {
cat <<EOD

	/**
	 * Builds a {@link ObjectInputValidator.Reversible} that reads the added arguments
	 * like {@link #fromMap} and also writes the result back as the map that it is read
	 * from, with {@link ObjectInputValidator.Reversible#toMap(Object) toMap}. Each
	 * accessor returns the validated value of the argument at its position from the
	 * result, so a record is given its constructor and its accessors:
	 *
	 * <pre>{@code
	 * ObjectInputValidator.Reversible<User> validator = Yavi.reversibleSchema()
	 *     ._string("name", c -> c.notBlank())
	 *     ._integer("age").optional()
	 *     .fromMapReversible(User::new, User::name, User::age);
	 * validator.toMap(new User("Taro", Optional.empty())); // {name=Taro, age=null}
	 * }</pre>
	 *
	 * <p>
	 * Every argument of this builder can be written back, because only an argument of
	 * constraints or of a {@link ReversibleValueValidator} can be added to it. What is
	 * written for an argument is stated in {@link ObjectInputValidator.Reversible}.
	 * @param <X> type of the result
	 * @param f function that creates the result from the validated values$(for j in `seq 1 ${i}`;do printf '\n\t * @param accessor%s returns the validated value of the argument at position %s from\n\t * the result' "${j}" "${j}";done)
	 * @return a new object input validator that also writes the result back
	 * @throws IllegalStateException as {@link #fromMap} does
	 * @throws IllegalArgumentException as {@link #fromMap} does
	 * @since 0.18.0
	 */
	@SuppressWarnings("unchecked")
	public <X> ObjectInputValidator.Reversible.IgnoringUnknown<X> fromMapReversible(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f, $(echo $(for j in `seq 1 ${i}`;do echo -n "Function<? super X, ? extends R${j}> accessor${j}, ";done) | sed 's/,$//')) {
		return ObjectInputValidator.ofReversible(List.of($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}.part(${j}, accessor${j}), ";done) | sed 's/,$//')),
				values -> f.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "(R${j}) values.get($((j - 1))), ";done) | sed 's/,$//')));
	}

	/**
	 * Builds a {@link ObjectInputValidator.Reversible} like {@link #fromMapReversible}
	 * that creates the result with the given function that can fail, as
	 * {@link #flatFromMap} does.
	 * @param <X> type of the result
	 * @param f function that creates the result from the validated values or reports
	 * violations$(for j in `seq 1 ${i}`;do printf '\n\t * @param accessor%s returns the validated value of the argument at position %s from\n\t * the result' "${j}" "${j}";done)
	 * @return a new object input validator that also writes the result back
	 * @throws IllegalStateException as {@link #fromMap} does
	 * @throws IllegalArgumentException as {@link #fromMap} does
	 * @since 0.18.0
	 */
	@SuppressWarnings("unchecked")
	public <X> ObjectInputValidator.Reversible.IgnoringUnknown<X> flatFromMapReversible(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends Validated<? extends X>> f, $(echo $(for j in `seq 1 ${i}`;do echo -n "Function<? super X, ? extends R${j}> accessor${j}, ";done) | sed 's/,$//')) {
		return ObjectInputValidator.flatOfReversible(List.of($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}.part(${j}, accessor${j}), ";done) | sed 's/,$//')),
				values -> f.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "(R${j}) values.get($((j - 1))), ";done) | sed 's/,$//')));
	}
EOD
}

# The terminals of a builder of one argument that create the validator of that argument:
# its validated value is the result.
single_terminals() {
cat <<EOD

	/**
	 * Builds a {@link ObjectInputValidator} that reads the added argument like
	 * {@link #fromMap(Function1)} and returns its validated value as the result, as
	 * {@link #get()} does for a typed argument. This is how one property is defined once,
	 * with its constraints and its modifiers, and embedded in several schemas:
	 *
	 * <pre>{@code
	 * ObjectInputValidator<String> passwordSchema = Yavi.schema()
	 *     ._string("password", c -> c.greaterThanOrEqual(8)).withheld()
	 *     .fromMap();
	 *
	 * Yavi.schema()._string("name")._embedded(passwordSchema).fromMap(Signup::new);
	 * Yavi.schema()._string("name")._embedded(passwordSchema).optional().fromMap(Edit::new);
	 * }</pre>
	 * @return a new object input validator of the validated value of the argument
	 * @throws IllegalStateException as {@link #fromMap(Function1)} does
	 * @since 0.18.0
	 */
	public ObjectInputValidator.IgnoringUnknown<R1> fromMap() {
		return this.<R1>fromMap(value -> value);
	}
EOD
if [ "${track}" == "reversible" ];then
cat <<EOD

	/**
	 * Builds a {@link ObjectInputValidator.Reversible} that reads the added argument
	 * like {@link #fromMapReversible(Function1, Function)} and returns its validated
	 * value as the result, which is also the value that is written back. This is how
	 * one property is defined once and embedded in several schemas, as
	 * {@link #fromMap()} states.
	 * @return a new object input validator of the validated value of the argument that
	 * also writes it back
	 * @throws IllegalStateException as {@link #fromMap(Function1)} does
	 * @since 0.18.0
	 */
	public ObjectInputValidator.Reversible.IgnoringUnknown<R1> fromMapReversible() {
		return this.<R1>fromMapReversible(value -> value, value -> value);
	}
EOD
fi
}

# The terminals of a builder whose only argument is the rest: the map of the validated
# values is the result.
single_complete_terminals() {
cat <<EOD

		/**
		 * Builds a {@link ObjectInputValidator} that reads every key of the map as the
		 * rest and returns the map of the validated values as the result, as
		 * {@link ${class}#fromMap()} does for one argument.
		 *
		 * <pre>{@code
		 * ObjectInputValidator<Map<String, Integer>> scores = Yavi.schema()._rest(Yavi._integer()).fromMap();
		 * }</pre>
		 * @return a new object input validator of the map of the validated values
		 * @throws IllegalStateException as {@link #fromMap(Function1)} does
		 */
		public ObjectInputValidator<R1> fromMap() {
			return this.builder.fromMap();
		}
EOD
if [ "${track}" == "reversible" ];then
cat <<EOD

		/**
		 * Builds a {@link ObjectInputValidator.Reversible} that reads every key of the
		 * map as the rest and returns the map of the validated values as the result,
		 * which is also what is written back, as {@link ${class}#fromMapReversible()}
		 * does for one argument.
		 * @return a new object input validator of the map of the validated values that
		 * also writes it back
		 * @throws IllegalStateException as {@link #fromMap(Function1)} does
		 */
		public ObjectInputValidator.Reversible<R1> fromMapReversible() {
			return this.builder.fromMapReversible();
		}
EOD
fi
}

reversible_complete_terminals() {
cat <<EOD

		/**
		 * Builds a {@link ObjectInputValidator.Reversible} that reads the added arguments
		 * and also writes the result back, as {@link ${class}#fromMapReversible} does.
		 * The rest is written into the same map as the other arguments.
		 * @param <X> type of the result
		 * @param f function that creates the result from the validated values$(for j in `seq 1 ${i}`;do printf '\n\t\t * @param accessor%s returns the validated value of the argument at position %s from\n\t\t * the result' "${j}" "${j}";done)
		 * @return a new object input validator that also writes the result back
		 * @throws IllegalStateException as {@link #fromMap} does
		 * @throws IllegalArgumentException as {@link #fromMap} does
		 */
		public <X> ObjectInputValidator.Reversible<X> fromMapReversible(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f, $(echo $(for j in `seq 1 ${i}`;do echo -n "Function<? super X, ? extends R${j}> accessor${j}, ";done) | sed 's/,$//')) {
			return this.builder.fromMapReversible(f, $(echo $(for j in `seq 1 ${i}`;do echo -n "accessor${j}, ";done) | sed 's/,$//'));
		}

		/**
		 * Builds a {@link ObjectInputValidator.Reversible} like
		 * {@link #fromMapReversible} that creates the result with the given function
		 * that can fail, as {@link ${class}#flatFromMapReversible} does.
		 * @param <X> type of the result
		 * @param f function that creates the result from the validated values or reports
		 * violations$(for j in `seq 1 ${i}`;do printf '\n\t\t * @param accessor%s returns the validated value of the argument at position %s from\n\t\t * the result' "${j}" "${j}";done)
		 * @return a new object input validator that also writes the result back
		 * @throws IllegalStateException as {@link #fromMap} does
		 * @throws IllegalArgumentException as {@link #fromMap} does
		 */
		public <X> ObjectInputValidator.Reversible<X> flatFromMapReversible(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends Validated<? extends X>> f, $(echo $(for j in `seq 1 ${i}`;do echo -n "Function<? super X, ? extends R${j}> accessor${j}, ";done) | sed 's/,$//')) {
			return this.builder.flatFromMapReversible(f, $(echo $(for j in `seq 1 ${i}`;do echo -n "accessor${j}, ";done) | sed 's/,$//'));
		}
EOD
}

# The builders are generated twice from the same template: those of Yavi.schema(), which
# take any validator, and those of Yavi.reversibleSchema(), which take only a validator
# that knows its reverse and therefore have the terminals that write the result back.
for track in plain reversible;do
if [ "${track}" == "reversible" ];then
  prefix="Reversible"
  definition="ReversibleArgumentDefinition"
  value_validator="ReversibleValueValidator"
  null_accepting="ReversibleValueValidator.NullAccepting"
  embedded_validator="ReversibleValueValidator<Map<String, Object>, T>"
  root="reversibleSchema"
  track_doc=' * <p>
 * This is a builder of {@link am.ik.yavi.validator.Yavi#reversibleSchema()}: every
 * argument is one that can be written back, so where a builder of
 * {@link am.ik.yavi.validator.Yavi#schema()} takes a {@link ValueValidator}, this one
 * takes a {@link ReversibleValueValidator}, and a validator that does not know its
 * reverse does not compile. In return, {@code fromMapReversible} builds a validator
 * that also writes the result back as the map it is read from.
 *
'
else
  prefix=""
  definition="ArgumentDefinition"
  value_validator="ValueValidator"
  null_accepting="NullAcceptingValueValidator"
  embedded_validator="ValueValidator<? super Map<String, Object>, T>"
  root="schema"
  track_doc=""
fi
for i in `seq 0 ${n}`;do
if [ "${i}" -gt 0 ];then
  class="${prefix}Arguments${i}ValidatorBuilder"
else
  class="${prefix}YaviSchema";
fi
  next_class="${prefix}Arguments$((i + 1))ValidatorBuilder"
  # "seq 1 0" counts down on macOS, so the ranges that may be empty are guarded
  prev_definitions=""
  init_definitions=""
  init_results=""
  init_arguments=""
  if [ "${i}" -gt 0 ];then
    prev_definitions="$(for j in `seq 1 ${i}`;do echo -n "this.d${j}, ";done)"
  fi
  if [ "${i}" -gt 1 ];then
    init_definitions="$(for j in `seq 1 $((i - 1))`;do echo -n "this.d${j}, ";done)"
    init_results="$(for j in `seq 1 $((i - 1))`;do echo -n "R${j}, ";done)"
    init_arguments="$(for j in `seq 1 $((i - 1))`;do echo -n "A${j}, ";done)"
  fi
  type_decls=""
  type_uses=""
  ctor_params=""
  ctor_args=""
  if [ "${i}" -gt 0 ];then
    type_decls="$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done)$(for j in `seq 1 ${i}`;do echo -n "R${j} extends @Nullable Object, ";done) | sed 's/,$//')"
    type_uses="$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done)$(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')"
    ctor_params="$(echo $(for j in `seq 1 ${i}`;do echo -n "${definition}<A${j}, R${j}> d${j}, ";done) | sed 's/,$//')"
    ctor_args="$(echo $(for j in `seq 1 ${i}`;do echo -n "d${j}, ";done) | sed 's/,$//')"
    # the stage of a list: the last argument is a list of E validated as a list of T
    list_type_decls="${init_arguments}E, $(if [ "${i}" -gt 1 ];then for j in `seq 1 $((i - 1))`;do echo -n "R${j} extends @Nullable Object, ";done;fi)T extends @Nullable Object"
    list_type_uses="${init_arguments}List<? extends E>, ${init_results}List<T>"
    list_ctor_params="$(if [ "${i}" -gt 1 ];then for j in `seq 1 $((i - 1))`;do echo -n "${definition}<A${j}, R${j}> d${j}, ";done;fi)${definition}<List<? extends E>, List<T>> d${i}"
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
$(if [ "${i}" -gt 0 ];then
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
$(if [ "${i}" -gt 0 ];then
cat <<EOD
import java.util.regex.Pattern;
EOD
fi)

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
import am.ik.yavi.constraint.ZonedDateTimeConstraint;$(if [ "${i}" -gt 0 ];then printf '\nimport am.ik.yavi.core.ConstraintViolations;\nimport am.ik.yavi.core.InputReader;'; fi)
import am.ik.yavi.core.NullAcceptingValueValidator;$(if [ "${i}" -gt 0 ];then printf '\nimport am.ik.yavi.core.Presence;\nimport am.ik.yavi.core.Validated;'; fi)
$(if [ "${track}" == "reversible" ];then printf 'import am.ik.yavi.core.ReversibleValueValidator;\n';fi)import am.ik.yavi.core.ValueValidator;
$(if [ "${i}" -gt 0 ];then
cat <<EOD
import am.ik.yavi.core.ViolationMessage;
import am.ik.yavi.fn.Function${i};
import am.ik.yavi.input.ObjectInputValidator;
import org.jspecify.annotations.NonNull;
EOD
fi)
import org.jspecify.annotations.Nullable;

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
 * names. Instances are obtained from {@link ${prefix}YaviSchema} or from the builder of the
 * previous argument count.
 *
 * <p>
 * Besides single values, an argument can be a nested object ({@code _object} with a name
 * and a value validator), a list ({@code _list}) or a map ({@code _map}); their
 * violations are located under the argument name. {@code splitBy} after {@code _list}
 * reads the list also from one text, such as the request parameter {@code tags=a,b}.
 *
 * <p>
 * An argument is required: {@code null} is a violation, and the validated value is
 * given to the function with a type that is not nullable. {@code optional()},
 * {@code nullable()} and {@code defaultValue} apply to the last added argument and make
 * it accept {@code null}, which changes the type of its validated value to
 * {@code Optional<R>} or {@code @Nullable R} unless there is a default value;
 * {@code recover} makes it accept any value, and {@code required()},
 * {@code rejectingNull()} and {@code presence()} tell an absent value from {@code null}.
 * {@code withheld()}, before the modifiers of an argument that is read from a key
 * other than {@code label} and {@code typeMessage}, holds its value back from what
 * shows the values of an object again.
 *
 * <p>
 * With {@code fromMap}, an argument added with {@code _embedded} is read from the same
 * map as the other arguments and one added with {@code _rest} from the keys that no
 * other argument defines.
 *
${track_doc} * <p>
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
 * An empty schema: describes the properties of an input by adding one at a time, and
 * builds a validator that reads them.
 *
 * <p>
 * Each {@code _xxx} method adds the next property and returns the builder for the
 * following one. A property is an argument of the function that creates the result, so
 * the builders call it an argument. The arguments are validated together, so every
 * violation is reported and the result is created only when all arguments are valid. The
 * validator is created after all arguments have been added: with {@code apply} to take
 * them as typed arguments, or with {@code fromMap} to read them from a
 * {@code Map<String, Object>} by their names. Instances are obtained from
 * {@link am.ik.yavi.validator.Yavi#${root}()}. For example: <pre>{@code
 * Arguments3Validator<String, String, Integer, Car> validator = Yavi.${root}()
 *     ._string("manufacturer")
 *     ._string("licensePlate", c -> c.greaterThanOrEqual(2).lessThanOrEqual(14))
 *     ._integer("seatCount", c -> c.greaterThanOrEqual(2))
 *     .apply(Car::new);
 * }</pre>
 *
 * <p>
 * Besides single values, an argument can be a nested object ({@code _object} with a name
 * and a value validator), a list ({@code _list}) or a map ({@code _map}); their
 * violations are located under the argument name. {@code splitBy} after {@code _list}
 * reads the list also from one text, such as the request parameter {@code tags=a,b}.
 *
 * <p>
 * An argument is required: {@code null} is a violation, and the validated value is
 * given to the function with a type that is not nullable. On the returned builders,
 * {@code optional()}, {@code nullable()} and {@code defaultValue} apply to the last added
 * argument and make it accept {@code null}, which changes the type of its validated
 * value to {@code Optional<R>} or {@code @Nullable R} unless there is a default value;
 * {@code recover} makes it accept any value, and {@code required()},
 * {@code rejectingNull()} and {@code presence()} tell an absent value from {@code null}.
 * {@code withheld()}, before the modifiers of an argument that is read from a key
 * other than {@code label} and {@code typeMessage}, holds its value back from what
 * shows the values of an object again.
 *
 * <p>
 * With {@code fromMap}, an argument added with {@code _embedded} is read from the same
 * map as the other arguments and one added with {@code _rest} from the keys that no
 * other argument defines.
 *
${track_doc} * <p>
 * The source of this class is generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.14.0
 */
EOD
fi)
public $(if [ "${i}" -gt 0 ];then echo -n "sealed";else echo -n "final";fi) class ${class}$(if [ "${i}" -gt 0 ];then
cat <<EOD
<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j} extends @Nullable Object, ";done) | sed 's/,$//')>
EOD
fi)
$(if [ "${i}" -gt 0 ];then echo " permits ${class}.Modifiable, ${class}.Recoverable, ${class}.Recovered, ${class}.Keyed, ${class}.NullAccepting, ${class}.OptionalAccepting";fi)
{

$(if [ "${i}" -gt 0 ];then
cat <<EOD
$(for j in `seq 1 ${i}`;do echo "	final ${definition}<A${j}, R${j}> d${j};";echo;done)

	${class}($(echo $(for j in `seq 1 ${i}`;do echo -n "${definition}<A${j}, R${j}> d${j}, ";done) | sed 's/,$//')) {
$(for j in `seq 1 ${i}`;do echo "		this.d${j} = d${j};";done)
	}

EOD
else
cat <<EOD
	/**
	 * Creates a new builder.
	 *
	 * <p>
	 * Instances are normally obtained from
	 * {@link am.ik.yavi.validator.Yavi#${root}()}.
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigDecimal, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _bigDecimal(${value_validator}<BigDecimal, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(BigDecimal.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_bigDecimal(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigDecimal, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _bigDecimal(${null_accepting}<BigDecimal, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(BigDecimal.class, validator));
	}

	/**
	 * Adds the next argument as a {@code BigDecimal} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link BigDecimalConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigDecimal, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) BigDecimal> _bigDecimal(String name,
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, BigDecimal.class, BigDecimalValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code BigDecimal} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigDecimal, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) BigDecimal> _bigDecimal(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigInteger, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _bigInteger(${value_validator}<BigInteger, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(BigInteger.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_bigInteger(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigInteger, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _bigInteger(${null_accepting}<BigInteger, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(BigInteger.class, validator));
	}

	/**
	 * Adds the next argument as a {@code BigInteger} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link BigIntegerConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigInteger, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) BigInteger> _bigInteger(String name,
			Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, BigInteger.class, BigIntegerValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code BigInteger} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigInteger, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) BigInteger> _bigInteger(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Boolean, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _boolean(${value_validator}<Boolean, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(Boolean.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_boolean(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Boolean, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _boolean(${null_accepting}<Boolean, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(Boolean.class, validator));
	}

	/**
	 * Adds the next argument as a {@code Boolean} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link BooleanConstraint}, for example {@code boolean.isTrue}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Boolean, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Boolean> _boolean(String name,
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, Boolean.class, BooleanValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code Boolean} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Boolean, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Boolean> _boolean(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Double, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _double(${value_validator}<Double, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(Double.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_double(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Double, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _double(${null_accepting}<Double, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(Double.class, validator));
	}

	/**
	 * Adds the next argument as a {@code Double} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link DoubleConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Double, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Double> _double(String name,
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, Double.class, DoubleValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code Double} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Double, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Double> _double(String name) {
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
	public <E extends Enum<E>, T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _enum(${value_validator}<E, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(Enum.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_enum(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <E> the enum type of the argument
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <E extends Enum<E>, T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _enum(${null_accepting}<E, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(Enum.class, validator));
	}

	/**
	 * Adds the next argument as a constant of the given enum type with the given name
	 * and configured constraints.
	 *
	 * <p>
	 * The argument knows its enum type: its description holds the constants, and
	 * {@code fromMap} reads it from the name of a constant.
	 * @param <E> the enum type of the argument
	 * @param name the property name used in violation names and messages
	 * @param type the enum type of the argument
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <E extends Enum<E>> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) E> _enum(String name, Class<E> type,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, type, EnumValidatorBuilder.of(name, type, constraints).build()));
	}

	/**
	 * Adds the next argument as a constant of the given enum type with the given name
	 * and no constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * @param <E> the enum type of the argument
	 * @param name the property name recorded for the argument
	 * @param type the enum type of the argument
	 * @return a builder for the next argument
	 * @since 0.18.0
	 * @see #_enum(String, Class, Function)
	 */
	public <E extends Enum<E>> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) E> _enum(String name, Class<E> type) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Float, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _float(${value_validator}<Float, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(Float.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_float(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Float, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _float(${null_accepting}<Float, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(Float.class, validator));
	}

	/**
	 * Adds the next argument as a {@code Float} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link FloatConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Float, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Float> _float(String name,
			Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, Float.class, FloatValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code Float} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Float, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Float> _float(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Instant, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _instant(${value_validator}<Instant, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(Instant.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_instant(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Instant, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _instant(${null_accepting}<Instant, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(Instant.class, validator));
	}

	/**
	 * Adds the next argument as an {@code Instant} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link InstantConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Instant, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Instant> _instant(String name,
			Function<InstantConstraint<Arguments1<Instant>>, InstantConstraint<Arguments1<Instant>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, Instant.class, InstantValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as an {@code Instant} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Instant, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Instant> _instant(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Integer, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _integer(${value_validator}<Integer, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(Integer.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_integer(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Integer, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _integer(${null_accepting}<Integer, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(Integer.class, validator));
	}

	/**
	 * Adds the next argument as an {@code Integer} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link IntegerConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Integer, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Integer> _integer(String name,
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, Integer.class, IntegerValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as an {@code Integer} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Integer, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Integer> _integer(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _localDateTime(${value_validator}<LocalDateTime, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(LocalDateTime.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_localDateTime(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _localDateTime(${null_accepting}<LocalDateTime, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(LocalDateTime.class, validator));
	}

	/**
	 * Adds the next argument as a {@code LocalDateTime} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link LocalDateTimeConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalDateTime> _localDateTime(String name,
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, LocalDateTime.class, LocalDateTimeValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code LocalDateTime} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalDateTime> _localDateTime(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _localTime(${value_validator}<LocalTime, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(LocalTime.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_localTime(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _localTime(${null_accepting}<LocalTime, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(LocalTime.class, validator));
	}

	/**
	 * Adds the next argument as a {@code LocalTime} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link LocalTimeConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalTime> _localTime(String name,
			Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, LocalTime.class, LocalTimeValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code LocalTime} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalTime> _localTime(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDate, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _localDate(${value_validator}<LocalDate, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(LocalDate.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_localDate(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDate, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _localDate(${null_accepting}<LocalDate, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(LocalDate.class, validator));
	}

	/**
	 * Adds the next argument as a {@code LocalDate} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link LocalDateConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDate, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalDate> _localDate(String name,
			Function<LocalDateConstraint<Arguments1<LocalDate>>, LocalDateConstraint<Arguments1<LocalDate>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, LocalDate.class, LocalDateValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code LocalDate} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDate, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalDate> _localDate(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Long, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _long(${value_validator}<Long, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(Long.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_long(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Long, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _long(${null_accepting}<Long, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(Long.class, validator));
	}

	/**
	 * Adds the next argument as a {@code Long} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link LongConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Long, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Long> _long(String name,
			Function<LongConstraint<Arguments1<Long>>, LongConstraint<Arguments1<Long>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, Long.class, LongValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code Long} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Long, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Long> _long(String name) {
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
	public <T1, T2 extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T1, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T2> _object(${value_validator}<T1, T2> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(Object.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_object(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T1> type of the value validated by {@code validator}
	 * @param <T2> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T1, T2 extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T1, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T2> _object(${null_accepting}<T1, T2> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(Object.class, validator));
	}

	/**
	 * Adds the next argument as an object of the given type with the given name and
	 * configured constraints.
	 *
	 * <p>
	 * The argument knows its type, so {@code fromMap} can read it: the value in the map
	 * must be an instance of the type or represent it. Give {@code Object.class} to take
	 * any value as it is. A type that has no class literal, such as
	 * {@code List<String>}, is added with a value validator:
	 * {@code _object(Yavi.<List<String>>_object(name, constraints))}.
	 * @param <T> type of the argument
	 * @param name the property name used in violation names and messages
	 * @param type the type of the argument
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _object(String name, Class<T> type,
			Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, type, ObjectValidatorBuilder.of(name, type, constraints).build()));
	}

	/**
	 * Adds the next argument as an object of the given type with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * @param <T> type of the argument
	 * @param name the property name recorded for the argument
	 * @param type the type of the argument
	 * @return a builder for the next argument
	 * @since 0.18.0
	 * @see #_object(String, Class, Function)
	 */
	public <T> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _object(String name, Class<T> type) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) OffsetDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _offsetDateTime(${value_validator}<OffsetDateTime, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(OffsetDateTime.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_offsetDateTime(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) OffsetDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _offsetDateTime(${null_accepting}<OffsetDateTime, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(OffsetDateTime.class, validator));
	}

	/**
	 * Adds the next argument as an {@code OffsetDateTime} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link OffsetDateTimeConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) OffsetDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) OffsetDateTime> _offsetDateTime(String name,
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, OffsetDateTime.class, OffsetDateTimeValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as an {@code OffsetDateTime} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) OffsetDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) OffsetDateTime> _offsetDateTime(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Short, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _short(${value_validator}<Short, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(Short.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_short(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Short, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _short(${null_accepting}<Short, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(Short.class, validator));
	}

	/**
	 * Adds the next argument as a {@code Short} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link ShortConstraint}, for example {@code numeric.greaterThan}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Short, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Short> _short(String name,
			Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, Short.class, ShortValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code Short} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Short, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Short> _short(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) String, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _string(${value_validator}<String, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(String.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_string(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) String, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _string(${null_accepting}<String, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(String.class, validator));
	}

	/**
	 * Adds the next argument as a {@code String} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link CharSequenceConstraint}, for example {@code charSequence.notBlank}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
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
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) String, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) String> _string(String name,
			Function<? super StringValueConstraint<Arguments1<String>>, ? extends CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, String.class, StringValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code String} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) String, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) String> _string(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) YearMonth, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _yearMonth(${value_validator}<YearMonth, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(YearMonth.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_yearMonth(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) YearMonth, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _yearMonth(${null_accepting}<YearMonth, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(YearMonth.class, validator));
	}

	/**
	 * Adds the next argument as a {@code YearMonth} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link YearMonthConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) YearMonth, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) YearMonth> _yearMonth(String name,
			Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, YearMonth.class, YearMonthValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code YearMonth} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) YearMonth, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) YearMonth> _yearMonth(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Year, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _year(${value_validator}<Year, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(Year.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_year(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Year, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _year(${null_accepting}<Year, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(Year.class, validator));
	}

	/**
	 * Adds the next argument as a {@code Year} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link YearConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Year, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Year> _year(String name,
			Function<YearConstraint<Arguments1<Year>>, YearConstraint<Arguments1<Year>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, Year.class, YearValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code Year} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Year, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Year> _year(String name) {
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
	public <T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) ZonedDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _zonedDateTime(${value_validator}<ZonedDateTime, T> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(ZonedDateTime.class, validator));
	}

	/**
	 * Adds the next argument, validated by the given value validator that accepts
	 * {@code null}.
	 *
	 * <p>
	 * This is {@link #_zonedDateTime(${value_validator})} for a validator that is known by its
	 * type to accept {@code null}: the returned builder offers {@code required()}
	 * and {@code rejectingNull()}, and no modifier that would accept {@code null}
	 * again.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the argument, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) ZonedDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _zonedDateTime(${null_accepting}<ZonedDateTime, T> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.value(ZonedDateTime.class, validator));
	}

	/**
	 * Adds the next argument as a {@code ZonedDateTime} with the given name and configured
	 * constraints.
	 *
	 * <p>
	 * Violations use {@code name} as the property name and the message ids of
	 * {@link ZonedDateTimeConstraint}, for example {@code temporal.past}. A {@code null} value is
	 * a violation: the one of a constraint that rejects it, or else {@code object.notNull}.
	 * @param name the property name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) ZonedDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) ZonedDateTime> _zonedDateTime(String name,
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.value(name, ZonedDateTime.class, ZonedDateTimeValidatorBuilder.of(name, constraints).build()));
	}

	/**
	 * Adds the next argument as a {@code ZonedDateTime} with the given name and no
	 * constraints.
	 *
	 * <p>
	 * Without constraints every value other than {@code null} is accepted.
	 * {@code name} is recorded as the property name of the argument.
	 * @param name the property name recorded for the argument
	 * @return a builder for the next argument
	 * @since 0.14.0
	 */
	public ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) ZonedDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) ZonedDateTime> _zonedDateTime(String name) {
		return this._zonedDateTime(name, Function.identity());
	}

	/**
	 * Adds the next argument as a nested object with the given name, validated by the
	 * given value validator.
	 *
	 * <p>
	 * The violations of {@code validator} are located under {@code name}: a violation
	 * named {@code street} is reported as {@code name.street} with the corresponding
	 * {@link am.ik.yavi.core.ConstraintViolation#path() path}. The validator of a single
	 * value is used under {@code name} instead of the name it was built with, as
	 * {@link ValueValidator#named(String)} states, so that the rule of a value is
	 * defined once: {@code _object("backupEmail", emailValidator)}. A {@code null} value
	 * is rejected with {@code object.notNull} unless {@code optional()},
	 * {@code nullable()} or {@code defaultValue} is applied to the argument, or to
	 * {@code validator} itself.
	 * @param <T1> type of the value validated by {@code validator}
	 * @param <T2> type of the value produced by {@code validator}
	 * @param name the property name that prefixes the violations of {@code validator}
	 * @param validator value validator for the nested object
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T1, T2 extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T1, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T2> _object(String name, ${value_validator}<T1, T2> validator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.object(name, validator));
	}

	/**
	 * Adds the next argument as a nested object with the given name, validated by the
	 * given value validator that accepts {@code null}.
	 *
	 * <p>
	 * This is {@link #_object(String, ${value_validator})} for a validator that is known by
	 * its type to accept {@code null}: {@code null} is given to {@code validator}, which
	 * decides what it produces, and the violations of any other value are located under
	 * {@code name}. The returned builder offers {@code required()} and
	 * {@code rejectingNull()}, and no modifier that would accept {@code null} again.
	 *
	 * <pre>{@code
	 * ._object("address", addressValidator.nullable())
	 * }</pre>
	 * @param <T1> type of the value validated by {@code validator}
	 * @param <T2> type of the value produced by {@code validator}
	 * @param name the property name that prefixes the violations of {@code validator}
	 * @param validator value validator for the nested object, which accepts {@code null}
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T1, T2 extends @Nullable Object> ${next_class}.AddedNullAccepting<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T1, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T2> _object(String name, ${null_accepting}<T1, T2> validator) {
		return new ${next_class}.AddedNullAccepting<>(${prev_definitions}${definition}.object(name, validator));
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
	 * @return a builder for the next argument, which can be told to read the list from
	 * text with {@code splitBy}
	 * @since 0.18.0
	 */
	public <E, T extends @Nullable Object> ${next_class}.KeyedList<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _list(String name, ${value_validator}<E, T> elementValidator) {
		return new ${next_class}.KeyedList<>(${prev_definitions}${definition}.list(name, elementValidator, ContainerConstraints.none()));
	}

	/**
	 * Adds the next argument as a {@code List} with the given name whose elements are
	 * validated by the given value validator and which satisfies the given constraints.
	 *
	 * <p>
	 * The elements are validated like {@link #_list(String, ${value_validator})} does. The
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
	 * @return a builder for the next argument, which can be told to read the list from
	 * text with {@code splitBy}
	 * @since 0.18.0
	 */
	public <E, T extends @Nullable Object> ${next_class}.KeyedList<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _list(String name, ${value_validator}<E, T> elementValidator,
			Function<CollectionConstraint<Arguments1<List<T>>, List<T>, T>, CollectionConstraint<Arguments1<List<T>>, List<T>, T>> constraints) {
		return new ${next_class}.KeyedList<>(${prev_definitions}${definition}.list(name, elementValidator, ContainerConstraints.ofList(constraints)));
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
	public <V, T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Map<String, ? extends V>, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Map<String, T>> _map(String name, ${value_validator}<V, T> valueValidator) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.map(name, valueValidator, ContainerConstraints.none()));
	}

	/**
	 * Adds the next argument as a {@code Map} with the given name whose values are
	 * validated by the given value validator and which satisfies the given constraints.
	 *
	 * <p>
	 * The values are validated like {@link #_map(String, ${value_validator})} does. The
	 * constraints are checked on the map of the validated values, only when every value
	 * is valid, and their violations are reported on {@code name}.
	 *
	 * <pre>{@code
	 * ._map("prices", Yavi._bigDecimal(c -> c.positive()), c -> c.notEmpty())
	 * }</pre>
	 * @param <V> type of the values validated by {@code valueValidator}
	 * @param <T> type of the values produced by {@code valueValidator}
	 * @param name the property name of the map
	 * @param valueValidator value validator for each value
	 * @param constraints function that configures the constraints of the map
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <V, T extends @Nullable Object> ${next_class}.Added<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Map<String, ? extends V>, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Map<String, T>> _map(String name, ${value_validator}<V, T> valueValidator,
			Function<MapConstraint<Arguments1<Map<String, T>>, String, T>, MapConstraint<Arguments1<Map<String, T>>, String, T>> constraints) {
		return new ${next_class}.Added<>(${prev_definitions}${definition}.map(name, valueValidator, ContainerConstraints.ofMap(constraints)));
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
	 * them has a value; see {@link am.ik.yavi.input.ObjectInputValidator}. With
	 * {@code apply}, the argument is a map validated by the validator.
	 * @param <T> type of the value produced by {@code validator}
	 * @param validator value validator for the map
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <T extends @Nullable Object> ${next_class}.Modifiable<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Map<String, Object>, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _embedded(${embedded_validator} validator) {
		return new ${next_class}.Modifiable<>(${prev_definitions}${definition}.embedded(validator));
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
	 * validated, and {@code null} is an empty map.
	 *
	 * <p>
	 * The rest is therefore never {@code null}: the returned builder has no
	 * {@code optional()}, {@code nullable()} or {@code defaultValue}, only
	 * {@code recover} to replace the rest when a value is invalid.
	 *
	 * <p>
	 * The rest is the last argument: the returned builder adds no argument, so the rest
	 * is defined once, and the validator that {@code fromMap} creates cannot reject
	 * unknown keys, of which there is none.
	 * @param <V> type of the values validated by {@code valueValidator}
	 * @param <T> type of the values produced by {@code valueValidator}
	 * @param valueValidator value validator for each value
	 * @return a builder for the next argument
	 * @since 0.18.0
	 */
	public <V, T extends @Nullable Object> ${next_class}.Rest<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Map<String, ? extends V>, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Map<String, T>> _rest(${value_validator}<V, T> valueValidator) {
		return new ${next_class}.Rest<>(new ${next_class}<>(${prev_definitions}${definition}.rest(valueValidator)));
	}

	/**
	 * Adds the next argument as a {@code Map} of the properties that no other argument
	 * is defined for, whose values are taken as they are.
	 * @return a builder that creates the validator
	 * @since 0.18.0
	 * @see #_rest(${value_validator})
	 */
	public ${next_class}.Rest<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Map<String, ?>, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Map<String, @Nullable Object>> _rest() {
		return new ${next_class}.Rest<>(new ${next_class}<>(${prev_definitions}${definition}.rest()));
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
	 * @throws IllegalArgumentException if two arguments have the same name
	 * @since 0.14.0
	 */
	public <X extends @Nullable Object> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> apply(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
		return ArgumentsValidators.split($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}.validator(), ";done) | sed 's/,$//')).<X>apply(f)
			.describedAs(${definition}.describe($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}, ";done) | sed 's/,$//')));
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
	 * {@link Validated#failureWith(am.ik.yavi.core.ViolationMessage, Object...)}
	 * creates, is reported on the arguments as a whole.
	 * The function is not given the locale of the validation, so its violations
	 * are reported with that locale, whichever locale they were created with.
	 * @param <X> type of the result
	 * @param f function that creates the result from the validated values or reports
	 * violations
	 * @return a new arguments validator
	 * @since 0.18.0
	 */
	public <X extends @Nullable Object> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> flatApply(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends Validated<? extends X>> f) {
		return ArgumentsValidators.split($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}.validator(), ";done) | sed 's/,$//')).<Validated<? extends X>>apply(f)
			.<X>andThen((validated, locale, constraintContext) -> Validated.of(Objects.requireNonNull(validated)))
			.describedAs(${definition}.describe($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}, ";done) | sed 's/,$//')));
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
	public <X extends @Nullable Object> Arguments1Validator<A1, X> apply(Function1<? super R1, ? extends X> f) {
		return Arguments1Validator.from(this.d1.validator().<X>map(f::apply));
	}

	/**
	 * Builds an {@link Arguments1Validator} from the added argument and the given
	 * function, which validates the argument further and returns the result or the
	 * violations. This is {@link #apply} for a function that can fail.
	 *
	 * <p>
	 * The function is applied only when the argument is valid. A violation it reports
	 * is located relative to the argument: one with an empty name, as
	 * {@link Validated#failureWith(am.ik.yavi.core.ViolationMessage, Object...)}
	 * creates, is reported on the argument itself.
	 * The function is not given the locale of the validation, so its violations
	 * are reported with that locale, whichever locale they were created with.
	 * @param <X> type of the result
	 * @param f function that creates the result from the validated value or reports
	 * violations
	 * @return a new arguments validator
	 * @since 0.18.0
	 */
	public <X extends @Nullable Object> Arguments1Validator<A1, X> flatApply(Function1<? super R1, ? extends Validated<? extends X>> f) {
		return Arguments1Validator.from(this.d1.validator()
			.<Validated<? extends X>>map(f::apply)
			.<X>andThen((validated, locale, constraintContext) -> Validated.of(Objects.requireNonNull(validated))));
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
	 * Builds a {@link ObjectInputValidator} that reads the added arguments from a
	 * {@code Map<String, Object>}, such as a parsed JSON object, by their names and
	 * creates the result with the given function.
	 *
	 * <p>
	 * Each value of the map must represent the type of its argument (see
	 * {@link ValueValidator#untyped(Class)}) and is then validated like the argument. An
	 * absent key is validated like {@code null}, unless the argument is added with
	 * {@code required()}, {@code rejectingNull()} or {@code presence()}, and an unknown
	 * key is ignored; see
	 * {@link ObjectInputValidator} for the violations of absent keys and for rejecting
	 * unknown keys. An {@code _embedded} argument reads the map itself and a
	 * {@code _rest} argument the keys that no other argument defines. A nested
	 * {@code _object}, {@code _list} or
	 * {@code _map} argument reads a map or a list whose content is checked by the
	 * {@linkplain ValueValidator#untyped() untyped} counterpart of its validator, so a
	 * nested object is added with another {@code ObjectInputValidator}.
	 * @param <X> type of the result
	 * @param f function that creates the result from the validated values
	 * @return a new object input validator, which ignores unknown keys unless it is told
	 * to reject them
	 * @throws IllegalStateException if an argument has no name, or if it is added with a
	 * value validator that does not know its input type
	 * @throws IllegalArgumentException if two arguments define a property of the same
	 * name
	 * @since 0.18.0
	 */
	@SuppressWarnings("unchecked")
	public <X extends @Nullable Object> ObjectInputValidator.IgnoringUnknown<X> fromMap(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
		return ObjectInputValidator.of(List.of($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}.part(${j}), ";done) | sed 's/,$//')),
				values -> f.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "(R${j}) values.get($((j - 1))), ";done) | sed 's/,$//')));
	}

	/**
	 * Builds a {@link ObjectInputValidator} that reads the added arguments like
	 * {@link #fromMap} and creates the result with the given function, which validates
	 * the arguments together and returns the result or the violations. This is
	 * {@code fromMap} for a function that can fail, such as a factory method that
	 * checks a rule across the properties.
	 *
	 * <p>
	 * The function is applied only when all arguments are valid. A violation it
	 * reports is located relative to the map: one with an empty name, as
	 * {@link Validated#failureWith(am.ik.yavi.core.ViolationMessage, Object...)}
	 * creates, is reported on the map itself and takes the name of the property the
	 * validator is applied to. Read another tree model with
	 * {@code flatFromMap(f).readingWith(reader)}.
	 * The function is not given the locale of the validation, so its violations
	 * are reported with that locale, whichever locale they were created with.
	 * @param <X> type of the result
	 * @param f function that creates the result from the validated values or reports
	 * violations
	 * @return a new object input validator
	 * @throws IllegalStateException as {@link #fromMap} does
	 * @throws IllegalArgumentException as {@link #fromMap} does
	 * @since 0.18.0
	 */
	@SuppressWarnings("unchecked")
	public <X extends @Nullable Object> ObjectInputValidator.IgnoringUnknown<X> flatFromMap(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends Validated<? extends X>> f) {
		return ObjectInputValidator.flatOf(List.of($(echo $(for j in `seq 1 ${i}`;do echo -n "this.d${j}.part(${j}), ";done) | sed 's/,$//')),
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
	public <N, X extends @Nullable Object> ValueValidator<N, X> from(InputReader<N> reader, Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
		return this.<X>fromMap(f).readingWith(reader);
	}
$(if [ "${track}" == "reversible" ];then reversible_terminals;fi)
$(if [ "${i}" == "1" ];then single_terminals;fi)
EOD
fi)
$(if [ "${i}" -gt 0 ];then
cat <<EOD

	/**
	 * The builder after an argument that reads the same map as the other arguments is
	 * added with {@code _embedded}: the modifiers apply to that argument.
	 *
	 * <p>
	 * Each of {@code optional()}, {@code nullable()}, {@code defaultValue},
	 * {@code recover} and {@code recoverWith} decides what {@code null} produces, before
	 * the argument is validated, so only the last one written would. The builders
	 * returned by the modifiers therefore offer only what still has an effect:
	 * {@code recover} after {@code optional()} or {@code nullable()}, to replace an
	 * invalid value with an empty {@code Optional} or {@code null}, and
	 * {@code optional()}, {@code nullable()} or {@code defaultValue} after
	 * {@code recover}, to produce something else than the fallback for {@code null}.
$(stage_class Modifiable final "${class}")

$(modifier_optional Recoverable)

$(modifier_nullable Recoverable)

$(modifier_default_value "${class}")

$(modifier_recover Recovered)

	}

	/**
	 * The builder after {@code optional()} or {@code nullable()} of an argument that is
	 * not read from a key: an invalid value can still be replaced with {@code recover}.
$(stage_class Recoverable final "${class}")

$(modifier_recover "${class}")

	}

	/**
	 * The builder after {@code recover} or {@code recoverWith} of an argument that is
	 * not read from a key: what {@code null} produces can still be changed, while an
	 * invalid value is replaced with the fallback.
$(stage_class Recovered final "${class}")

$(modifier_optional "${class}")

$(modifier_nullable "${class}")

$(modifier_default_value "${class}")

	}

	/**
	 * The builder after an argument that is read from a key is added: the modifiers
	 * apply to that argument. Its absence can be told from {@code null} with
	 * {@link #presence()}, and with {@link NullAccepting#required()} and
	 * {@link NullAccepting#rejectingNull()} once the argument accepts {@code null}.
	 *
	 * <p>
	 * Each of {@code optional()}, {@code nullable()}, {@code defaultValue},
	 * {@code recover} and {@code recoverWith} decides what {@code null} produces, before
	 * the argument is validated, so only the last one written would. The builders
	 * returned by the modifiers therefore offer only what still has an effect:
	 * {@code recover} after {@code optional()} or {@code nullable()}, to replace an
	 * invalid value with an empty {@code Optional} or {@code null}, and
	 * {@code optional()}, {@code nullable()} or {@code defaultValue} after
	 * {@code recover}, to produce something else than the fallback for {@code null}.
$(stage_class Keyed sealed "${class}" " permits Added" messages)

$(modifier_messages Keyed)

$(modifier_required_message "" "the last added argument, which it is unless a modifier accepts {@code null}")

$(modifier_optional KeyedOptional)

$(modifier_nullable KeyedRecoverable)

$(modifier_default_value NullAccepting)

$(modifier_recover KeyedRecovered)

$(modifier_presence)

	}

	/**
	 * The builder after an argument that is read from a key is added, until a modifier
	 * other than {@code label} and {@code typeMessage} is written: before the other
	 * modifiers of {@link Keyed}, {@link #withheld()} holds the value of the argument
	 * back from what shows the values of an object again. It is said once, so the
	 * builder that it returns does not have it.
$(stage_class Added sealed Keyed " permits KeyedList" withholdable)

$(modifier_withheld Keyed)

$(modifier_messages Added)

	}

	/**
	 * The builder after a list that is read from a key is added, until a modifier other
	 * than {@code label} and {@code typeMessage} is written: the modifiers of
	 * {@link Added} apply to the list, and before them {@code splitBy} tells that the
	 * list may be given as one text, such as the request parameter
	 * {@code tags=java,yavi}.
	 *
$(if [ "${i}" -gt 1 ];then for j in `seq 1 $((i - 1))`;do printf '\t * @param <A%s> type of argument at position %s\n\t * @param <R%s> type of the validated value of argument at position %s\n' "${j}" "${j}" "${j}" "${j}";done;fi)
	 * @param <E> type of the elements of the list
	 * @param <T> type of the validated elements of the list
	 * @since 0.18.0
	 */
	public static final class KeyedList<${list_type_decls}> extends Added<${list_type_uses}> {

		KeyedList(${list_ctor_params}) {
			super(${ctor_args});
		}

$(modifier_messages KeyedList "${init_arguments}E, ${init_results}T")

		/**
		 * Reads the last added list also from text whose elements are separated by the
		 * given separator, as {@link #splitBy($(if [ "${track}" == "reversible" ];then echo -n "Pattern, String";else echo -n "Pattern";fi))} states.$(if [ "${track}" == "reversible" ];then printf ' A validator built with\n\t\t * {@code fromMapReversible} writes the list back as that text: the texts of the\n\t\t * elements, joined by the separator. An empty list is written as {@code null},\n\t\t * because text without an element is not sent, so it is read back only if the\n\t\t * list accepts {@code null} ({@code defaultValue(List.of())},\n\t\t * {@code nullable()}). An element whose text contains the separator is not read\n\t\t * back as one element.';fi)
		 *
		 * <pre>{@code
		 * ._list("tags", Yavi._string(c -> c.notBlank())).splitBy(",") // tags=java,yavi
		 * }</pre>
		 * @param separator the text that separates the elements, taken literally
		 * @return a builder whose last argument may be given as text
		 * @throws IllegalArgumentException if the separator is empty
		 * @since 0.18.0
		 */
		public Added<${list_type_uses}> splitBy(String separator) {
			return $(if [ "${track}" == "reversible" ];then echo -n "this.splitBy(Pattern.compile(Pattern.quote(separator)), separator)";else echo -n "this.splitBy(Pattern.compile(Pattern.quote(separator)))";fi);
		}

$(if [ "${track}" == "reversible" ];then split_by_pattern_reversible;else split_by_pattern_plain;fi)

	}

	/**
	 * The builder after {@code nullable()} of an argument that is read from a key: an
	 * invalid value can still be replaced with {@code recover}.
$(stage_class KeyedRecoverable final NullAccepting)

$(modifier_recover NullAccepting)

	}

	/**
	 * The builder after {@code optional()} of an argument that is read from a key: an
	 * invalid value can still be replaced with {@code recover}.
$(stage_class KeyedOptional final OptionalAccepting)

$(modifier_recover OptionalAccepting)

	}

	/**
	 * The builder after an argument that is read from a key produces an
	 * {@code Optional}: an absent key and an explicit {@code null}, which are both
	 * accepted, can be told apart with {@link #required()} and {@link #rejectingNull()}.
	 * There is no {@code presence()}, which would wrap the {@code Optional} in a
	 * {@code Presence}: {@code nullable().presence()} gives the three states.
$(stage_class OptionalAccepting sealed "${class}" " permits KeyedOptional" messages)

$(modifier_messages OptionalAccepting)

$(modifier_required)

$(modifier_required_message ".withStrictness(ObjectInputValidator.Strictness.REQUIRED)" "the key of the last added argument, as {@link #required()} does")

$(modifier_rejecting_null)

	}

	/**
	 * The builder after {@code recover} or {@code recoverWith} of an argument that is
	 * read from a key: what {@code null} produces can still be changed, while an invalid
	 * value is replaced with the fallback.
$(stage_class KeyedRecovered final NullAccepting)

$(modifier_optional OptionalAccepting)

$(modifier_nullable NullAccepting)

$(modifier_default_value NullAccepting)

	}

	/**
	 * The builder after an argument that is read from a key accepts {@code null}, with
	 * {@code nullable()}, {@code defaultValue} or {@code recover}, or with a
	 * {@link NullAcceptingValueValidator}: an absent key and an explicit
	 * {@code null}, which are both accepted, can be told apart with {@link #required()},
	 * {@link #rejectingNull()} and {@link #presence()}.
$(stage_class NullAccepting sealed "${class}" " permits KeyedRecoverable, KeyedRecovered, AddedNullAccepting" messages)

$(modifier_messages NullAccepting)

$(modifier_required)

$(modifier_required_message ".withStrictness(ObjectInputValidator.Strictness.REQUIRED)" "the key of the last added argument, as {@link #required()} does")

$(modifier_rejecting_null)

$(modifier_presence)

	}

	/**
	 * The builder directly after an argument that is read from a key is added with a
	 * validator that accepts {@code null}: before the modifiers of
	 * {@link NullAccepting}, {@link #withheld()} holds the value of the argument back
	 * from what shows the values of an object again. It is said once, so the builder
	 * that it returns does not have it; {@code label} and {@code typeMessage} return
	 * this builder.
$(stage_class AddedNullAccepting final NullAccepting "" withholdable)

$(modifier_withheld NullAccepting)

$(modifier_messages AddedNullAccepting)

	}

	/**
	 * The builder after the last argument, which {@code _rest} adds: it creates the
	 * validator as {@link ${class}} does and adds no argument, so the rest is defined
	 * once.
	 *
	 * @param <A1> type of the first argument$(for j in `seq 1 ${i}`;do if [ "${j}" -gt 1 ];then printf '\n\t * @param <A%s> type of argument at position %s' "${j}" "${j}";fi;done)
	 * @param <R1> type of the validated value of the first argument$(for j in `seq 1 ${i}`;do if [ "${j}" -gt 1 ];then printf '\n\t * @param <R%s> type of the validated value of argument at position %s' "${j}" "${j}";fi;done)
	 * @since 0.18.0
	 */
	public static sealed class Complete<${type_decls}> permits Rest {

		final ${class}<${type_uses}> builder;

		Complete(${class}<${type_uses}> builder) {
			this.builder = builder;
		}

		/**
		 * Builds an {@link Arguments${i}Validator} from the added arguments and the given
		 * function, as {@link ${class}#apply} does. The rest is an argument: a map whose
		 * values are validated, and an empty map if it is {@code null}.
		 * @param <X> type of the result
		 * @param f function that creates the result from the validated values
		 * @return a new arguments validator
		 * @throws IllegalArgumentException if two arguments have the same name
		 */
		public <X extends @Nullable Object> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> apply(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
			return this.builder.apply(f);
		}

		/**
		 * Builds an {@link Arguments${i}Validator} from the added arguments and the given
		 * function that can fail, as {@link ${class}#flatApply} does.
		 * @param <X> type of the result
		 * @param f function that creates the result from the validated values or reports
		 * violations
		 * @return a new arguments validator
		 * @throws IllegalArgumentException if two arguments have the same name
		 */
		public <X extends @Nullable Object> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> flatApply(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends Validated<? extends X>> f) {
			return this.builder.flatApply(f);
		}

		/**
		 * Builds a {@link ObjectInputValidator} that reads the added arguments from a
		 * {@code Map<String, Object>}, as {@link ${class}#fromMap} does. The rest is read
		 * from the keys that no other argument defines, so no key is unknown: the
		 * validator has no {@code rejectingUnknownProperties()}.
		 * @param <X> type of the result
		 * @param f function that creates the result from the validated values
		 * @return a new object input validator
		 * @throws IllegalStateException as {@link ${class}#fromMap} does, and if an
		 * embedded validator does not describe its properties, so that the rest cannot
		 * be told
		 * @throws IllegalArgumentException as {@link ${class}#fromMap} does
		 */
		public <X extends @Nullable Object> ObjectInputValidator<X> fromMap(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
			return this.builder.fromMap(f);
		}

		/**
		 * Builds a {@link ObjectInputValidator} that reads the added arguments like
		 * {@link #fromMap} and creates the result with the given function that can fail,
		 * as {@link ${class}#flatFromMap} does.
		 * @param <X> type of the result
		 * @param f function that creates the result from the validated values or reports
		 * violations
		 * @return a new object input validator
		 * @throws IllegalStateException as {@link #fromMap} does
		 * @throws IllegalArgumentException as {@link #fromMap} does
		 */
		public <X extends @Nullable Object> ObjectInputValidator<X> flatFromMap(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends Validated<? extends X>> f) {
			return this.builder.flatFromMap(f);
		}

		/**
		 * Builds a validator that reads the added arguments from an object of the given
		 * reader, as {@link ${class}#from} does.
		 * @param <N> the type of the nodes
		 * @param <X> type of the result
		 * @param reader the reader of the nodes
		 * @param f function that creates the result from the validated values
		 * @return a new validator of a node
		 * @throws IllegalStateException as {@link #fromMap} does
		 * @throws UnsupportedOperationException if a nested validator does not know its
		 * input type
		 */
		public <N, X extends @Nullable Object> ValueValidator<N, X> from(InputReader<N> reader, Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
			return this.builder.from(reader, f);
		}
$(if [ "${track}" == "reversible" ];then reversible_complete_terminals;fi)
$(if [ "${i}" == "1" ];then single_complete_terminals;fi)

	}

	/**
	 * The builder after {@code _rest}, which is never {@code null}: the rest can be
	 * replaced with {@code recover} when a value is invalid.
	 *
	 * @param <A1> type of the first argument$(for j in `seq 1 ${i}`;do if [ "${j}" -gt 1 ];then printf '\n\t * @param <A%s> type of argument at position %s' "${j}" "${j}";fi;done)
	 * @param <R1> type of the validated value of the first argument$(for j in `seq 1 ${i}`;do if [ "${j}" -gt 1 ];then printf '\n\t * @param <R%s> type of the validated value of argument at position %s' "${j}" "${j}";fi;done)
	 * @since 0.18.0
	 */
	public static final class Rest<${type_decls}> extends Complete<${type_uses}> {

		Rest(${class}<${type_uses}> builder) {
			super(builder);
		}

		/**
		 * Produces the given value when a value of the rest is invalid, so that the rest
		 * never fails. With {@code apply}, a {@code null} rest produces it too.
		 * @param fallback the value produced instead of a failure
		 * @return a builder that creates the validator
		 * @throws NullPointerException if {@code fallback} is {@code null}
		 * @see ValueValidator#recover(Object)
		 */
		public Complete<${type_uses}> recover(R${i} fallback) {
			return new Complete<>(new ${class}<>($(if [ "${i}" -gt 1 ];then for j in `seq 1 $((i - 1))`;do echo -n "this.builder.d${j}, ";done;fi)this.builder.d${i}.recovering(fallback)));
		}

		/**
		 * Produces the result of the given function when a value of the rest is invalid,
		 * so that the rest never fails. The function is given the violations, and must
		 * not return {@code null}.
		 * @param fallback the function that produces a value from the violations
		 * @return a builder that creates the validator
		 * @see ValueValidator#recoverWith(Function)
		 */
		public Complete<${type_uses}> recoverWith(Function<? super ConstraintViolations, ? extends R${i}> fallback) {
			return new Complete<>(new ${class}<>($(if [ "${i}" -gt 1 ];then for j in `seq 1 $((i - 1))`;do echo -n "this.builder.d${j}, ";done;fi)this.builder.d${i}.recoveringWith(fallback)));
		}

	}
EOD
fi)
}
EOF
if [ "${track}" == "reversible" ];then
  perl -pi -e 's/\@since 0\.1[0-7]\.\d+/\@since 0.18.0/' ${file}
fi
done
done
