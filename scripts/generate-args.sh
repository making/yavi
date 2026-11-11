#!/bin/bash
set -e
n=16

for i in `seq 1 ${n}`;do
  class="Arguments${i}"
  file="$(dirname $0)/../src/main/java/am/ik/yavi/arguments/${class}.java"
  echo $file

  # Create all arg method declarations
  arg_methods=""
  for j in `seq 1 ${i}`; do
    arg_methods+="
	/**
	 * Returns the argument at position ${j}, which may be {@code null}.
	 *
	 * @return the argument at position ${j}, possibly {@code null}
	 */
	@Nullable
	public A${j} arg${j}() {
		return this.arg${j};
	}
"
  done

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

  # Create field declarations for all arguments
  field_declarations=""
  for j in `seq 1 ${i}`; do
    field_declarations+="
	private final A${j} arg${j};"
  done

  # Create constructor parameters
  constructor_params=""
  for j in `seq 1 ${i}`; do
    constructor_params="${constructor_params}@Nullable A${j} arg${j}"
    if [ ${j} -lt ${i} ]; then
      constructor_params="${constructor_params}, "
    fi
  done

  # Create constructor parameter assignments
  constructor_assignments=""
  for j in `seq 1 ${i}`; do
    constructor_assignments+="
		this.arg${j} = arg${j};"
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

  # Create constructor param documentation
  param_docs=""
  for j in `seq 1 ${i}`; do
    param_docs="${param_docs}arg${j} the argument at position ${j}"
    if [ ${j} -lt ${i} ]; then
      param_docs="${param_docs}, "
    fi
  done

  # Create equals method parameters
  equals_params=""
  for j in `seq 1 ${i}`; do
    equals_params="${equals_params}Objects.equals(this.arg${j}, that.arg${j})"
    if [ ${j} -lt ${i} ]; then
      equals_params="${equals_params} && "
    fi
  done

  # Create hashCode method parameters
  hashcode_params=""
  for j in `seq 1 ${i}`; do
    hashcode_params="${hashcode_params}this.arg${j}"
    if [ ${j} -lt ${i} ]; then
      hashcode_params="${hashcode_params}, "
    fi
  done

  # Create toString method parameters
  tostring_params=""
  for j in `seq 1 ${i}`; do
    tostring_params="${tostring_params}\"arg${j}=\" + this.arg${j}"
    if [ ${j} -lt ${i} ]; then
      tostring_params="${tostring_params} + \", \" + "
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

import java.util.Objects;

import am.ik.yavi.fn.Function${i};
import am.ik.yavi.jsr305.Nullable;

/**
 * An immutable container of ${i} argument${argument_suffix}.
 *
 * <p>
 * Instances are created with {@link Arguments#of}. The {@code wrap()} and
 * {@code unwrap(...)} methods of {@link Arguments${i}Validator} convert between
 * validating the arguments individually and validating this container as a single
 * value. This class only stores, compares and maps the argument values; it performs no
 * validation, and an argument may be {@code null}.
 *
 * <p>
 * The {@code first*}, {@code last*}, {@code append}, {@code prepend} and
 * {@code reverse} methods derive another container from the same argument values.
 * Equality and the hash code are based on the argument values and the concrete class.
 *
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh${type_param_docs}
 * @since 0.3.0
 */
public final class ${class_definition} {
${field_declarations}

	/**
	 * Creates a container with the given arguments. The arguments may be {@code null}
	 * and are stored as is.
	 *
	 * @param ${param_docs}
	 */
	${class}(${constructor_params}) {${constructor_assignments}
	}
${arg_methods}
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

	/**
	 * Returns whether the given object is an {@code Arguments${i}} with equal argument
	 * values. Two containers are equal only if they have the same concrete class and
	 * every argument is equal, using {@link java.util.Objects#equals(Object, Object)}.
	 *
	 * @param obj the reference object with which to compare
	 * @return true if the given object holds equal argument values; false otherwise
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		${class}<${class_type_params}> that = (${class}<${class_type_params}>) obj;
		return ${equals_params};
	}

	/**
	 * Returns a hash code consistent with {@link #equals(Object)}, computed from all
	 * argument values.
	 *
	 * @return a hash code value for this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(${hashcode_params});
	}

	/**
	 * Returns a string representation of this container including all argument values.
	 *
	 * @return a string representation of this object
	 */
	@Override
	public String toString() {
		return "${class}{" + ${tostring_params} + "}";
	}
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
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.3.0
 */
public final class ArgumentsValidatorBuilder {
$(for i in `seq 1 ${n}`;do
  cat <<EOJ
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
	 * @since 0.3.0
	 */
	public static final class ${arguments} {
		private final Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends X> mapper;
		private ValidatorBuilder<Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>> builder;

		public ${class}(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public ${arguments} builder(
				Function<? super ValidatorBuilder<Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>>, ? extends ValidatorBuilder<Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}

		public Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> build() {
			return new DefaultArguments${i}Validator<>(this.builder.build(), this.mapper);
		}
	}

EOJ
done)
}
EOF

for i in `seq 1 ${n}`;do
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
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.jsr305.Nullable;
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
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validatable;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.jsr305.Nullable;
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
	 * {@link ValueValidator}.
	 *
	 * @param valueValidator the value validator to delegate to
	 * @param <A1> the type of the argument
	 * @param <X> the type of the value produced on success
	 * @return an arguments1 validator for the given value validator
	 * @since 0.8.0
	 */
	static <A1, X> Arguments1Validator<A1, X> from(ValueValidator<A1, X> valueValidator) {
		return valueValidator::validate;
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
			public Validated<X> validate(A1 a1, Locale locale, ConstraintContext constraintContext) {
				return validator.validate(Arguments.of(a1), locale, constraintContext);
			}

			@Override
			public Arguments1Validator<A1, Supplier<X>> lazy() {
				return Arguments1Validator.unwrap(validator.lazy());
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
			public Validated<X> validate(${wrapArguments} args, Locale locale,
					ConstraintContext constraintContext) {
				final ${arguments} nonNullArgs = Objects.requireNonNull(args);
				return ${class}.this.validate($(echo $(for j in `seq 1 ${i}`;do echo -n "nonNullArgs.arg${j}(), ";done) | sed 's/,$//'),
						locale, constraintContext);
			}

			@Override
			public Arguments1Validator<${wrapArguments}, Supplier<X>> lazy() {
				return ${class}.this.lazy().wrap();
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
	default <X2> ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X2> map(Function<? super X, ? extends X2> mapper) {
		return new ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X2>() {
			@Override
			public Validated<X2> validate($(echo $(for j in `seq 1 ${i}`;do echo -n "A${j} a${j}, ";done) | sed 's/,$//'), Locale locale, ConstraintContext constraintContext) {
				return ${class}.this.validate(${as}, locale, constraintContext).map(mapper);
			}

			@Override
			public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X2>> lazy() {
				return ${class}.this.lazy()
					.map((Function<Supplier<X>, Supplier<X2>>) xSupplier -> () -> mapper.apply(xSupplier.get()));
			}
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
			public Validated<X2> validate($(echo $(for j in `seq 1 ${i}`;do echo -n "A${j} a${j}, ";done) | sed 's/,$//'), Locale locale, ConstraintContext constraintContext) {
				return ${class}.this.validate(${as}, locale, constraintContext)
					.flatMap(v -> validator.validate(v, locale, constraintContext));
			}

			@Override
			public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X2>> lazy() {
				return ${class}.this.lazy().andThen((xSupplier, locale, constraintContext) -> validator
					.validate(Objects.requireNonNull(xSupplier).get(), locale, constraintContext)
					.map(x2 -> () -> x2));
			}
		};
	}

	/**
	 * Returns a validator that converts its input with the given function before
	 * validating it.
	 *
	 * <p>
	 * The function is not validated; its result is passed to this validator.
	 *
	 * @param mapper the function that converts the input of the new validator
	 * @param <A> the input type of the new validator
	 * @return a validator with the adapted input type
	 * @since 0.7.0
	 */
$(if [ "${i}" == "1" ];then
cat <<EOD
	@Override
	default <A> Arguments1Validator<A, X> compose(Function<? super A, ? extends A1> mapper) {
		return new Arguments1Validator<A, X>() {
			@Override
			public Validated<X> validate(A a, Locale locale, ConstraintContext constraintContext) {
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
	default <A> Arguments1Validator<A, X> compose(
			Function<? super A, ? extends ${arguments}> mapper) {
		return new Arguments1Validator<A, X>() {
			@Override
			public Validated<X> validate(A a, Locale locale, ConstraintContext constraintContext) {
				final ${arguments} args = mapper.apply(a);
				return ${class}.this.validate($(echo $(for j in `seq 1 ${i}`;do echo -n "args.arg${j}(), ";done) | sed 's/,$//'), locale, constraintContext);
			}

			@Override
			public Arguments1Validator<A, Supplier<X>> lazy() {
				return ${class}.this.lazy().compose(mapper);
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
	 * Returns a validator that appends {@code [index]} to every violation name, so that
	 * violations of elements in a collection can be told apart.
	 *
	 * @param index the index to append to violation names
	 * @return a validator with indexed violation names
	 * @since 0.7.0
	 */
	@Override
	default Arguments1Validator<A1, X> indexed(int index) {
		return (a1, locale, constraintContext) -> Arguments1Validator.this
				.validate(a1, locale, constraintContext).indexed(index);
	}

	/**
	 * Returns a validator that validates every element of the input iterable with this
	 * validator indexed by its position and collects the validated values into a
	 * collection created by the given factory. Violations of all failing elements are
	 * accumulated.
	 *
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
	 * validator indexed by its position and collects the validated values into a
	 * {@link List}.
	 *
	 * @return a validator for iterables
	 * @since 0.8.0
	 */
	default Arguments1Validator<Iterable<A1>, List<X>> liftList() {
		return Arguments1Validator.from(ValueValidator.super.liftList());
	}

	/**
	 * Returns a validator that validates every element of the input iterable with this
	 * validator indexed by its position and collects the validated values into a
	 * {@link Set} that preserves the iteration order.
	 *
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
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function${i};
import am.ik.yavi.jsr305.Nullable;

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

	/**
	 * Creates a validator that applies the given validator to an {@link Arguments${i}}
	 * built from the arguments and maps the validated arguments with the given function.
	 *
	 * @param validator the validator applied to the arguments
	 * @param mapper the mapper applied to the validated arguments
	 */
	public ${class}(Validator<${arguments}> validator, Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends X> mapper) {
		this.validator = validator;
		this.mapper = mapper;
	}

	/**
	 * Returns a validator that produces a {@link Supplier} of the mapped value.
	 *
	 * @return a validator that produces a supplier of the validated value
	 * @since 0.10.0
	 */
	@Override
	public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X>> lazy() {
		return new ${class}<>(this.validator, ($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//')) -> () -> this.mapper.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//')));
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
		return this.validator.applicative()
		    .validate(Arguments.of(${as}), locale, constraintContext)
				.map(values -> values.map(this.mapper));
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
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.fn.Function${i};
import am.ik.yavi.fn.Validations;
import am.ik.yavi.jsr305.Nullable;

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
			public Validated<X> validate(A a, Locale locale, ConstraintContext constraintContext) {
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
	 * given validator indexed by its position and collects the validated values into a
	 * collection created by the given factory. Violations of all failing elements are
	 * accumulated.
	 *
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
	 * given validator indexed by its position and collects the validated values into a
	 * {@link List}.
	 *
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
	 * given validator indexed by its position and collects the validated values into a
	 * {@link Set} that preserves the iteration order.
	 *
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
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.constraint.DoubleConstraint;
import am.ik.yavi.constraint.EnumConstraint;
import am.ik.yavi.constraint.FloatConstraint;
import am.ik.yavi.constraint.InstantConstraint;
import am.ik.yavi.constraint.IntegerConstraint;
import am.ik.yavi.constraint.LocalDateTimeConstraint;
import am.ik.yavi.constraint.LocalDateConstraint;
import am.ik.yavi.constraint.LocalTimeConstraint;
import am.ik.yavi.constraint.LongConstraint;
import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.constraint.OffsetDateTimeConstraint;
import am.ik.yavi.constraint.ShortConstraint;
import am.ik.yavi.constraint.YearConstraint;
import am.ik.yavi.constraint.YearMonthConstraint;
import am.ik.yavi.constraint.ZonedDateTimeConstraint;
import am.ik.yavi.core.ValueValidator;
$(if [ "${i}" -gt 0 ];then
cat <<EOD
import am.ik.yavi.fn.Function${i};
EOD
fi)

/**
 * Generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.14.0
 */
public final class ${class}$(if [ "${i}" -gt 0 ];then
cat <<EOD
<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')>
EOD
fi)
{

$(if [ "${i}" -gt 0 ];then
cat <<EOD
$(for j in `seq 1 ${i}`;do echo "	final ValueValidator<A${j}, R${j}> v${j};";echo;done)

	public ${class}($(echo $(for j in `seq 1 ${i}`;do echo -n "ValueValidator<A${j}, R${j}> v${j}, ";done) | sed 's/,$//')) {
$(for j in `seq 1 ${i}`;do echo "		this.v${j} = v${j};";done)
	}
EOD
fi)

$(if [ "${i}" != "${n}" ];then
cat <<EOD
	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigDecimal, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _bigDecimal(ValueValidator<BigDecimal, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigDecimal, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) BigDecimal> _bigDecimal(String name,
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		return this._bigDecimal(BigDecimalValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigDecimal, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) BigDecimal> _bigDecimal(String name) {
		return this._bigDecimal(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigInteger, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _bigInteger(ValueValidator<BigInteger, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigInteger, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) BigInteger> _bigInteger(String name,
			Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
		return this._bigInteger(BigIntegerValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) BigInteger, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) BigInteger> _bigInteger(String name) {
		return this._bigInteger(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Boolean, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _boolean(ValueValidator<Boolean, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Boolean, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Boolean> _boolean(String name,
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		return this._boolean(BooleanValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Boolean, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Boolean> _boolean(String name) {
		return this._boolean(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Double, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _double(ValueValidator<Double, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Double, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Double> _double(String name,
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		return this._double(DoubleValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Double, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Double> _double(String name) {
		return this._double(name, Function.identity());
	}

	public <E extends Enum<E>, T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _enum(ValueValidator<E, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public <E extends Enum<E>> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) E> _enum(String name,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return this._enum(EnumValidatorBuilder.of(name, constraints).build());
	}

	public <E extends Enum<E>> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) E, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) E> _enum(String name) {
		return this._enum(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Float, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _float(ValueValidator<Float, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Float, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Float> _float(String name,
			Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		return this._float(FloatValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Float, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Float> _float(String name) {
		return this._float(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Instant, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _instant(ValueValidator<Instant, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Instant, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Instant> _instant(String name,
			Function<InstantConstraint<Arguments1<Instant>>, InstantConstraint<Arguments1<Instant>>> constraints) {
		return this._instant(InstantValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Instant, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Instant> _instant(String name) {
		return this._instant(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Integer, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _integer(ValueValidator<Integer, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Integer, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Integer> _integer(String name,
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
		return this._integer(IntegerValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Integer, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Integer> _integer(String name) {
		return this._integer(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _localDateTime(ValueValidator<LocalDateTime, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalDateTime> _localDateTime(String name,
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		return this._localDateTime(LocalDateTimeValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalDateTime> _localDateTime(String name) {
		return this._localDateTime(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _localTime(ValueValidator<LocalTime, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalTime> _localTime(String name,
			Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
		return this._localTime(LocalTimeValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalTime> _localTime(String name) {
		return this._localTime(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDate, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _localDate(ValueValidator<LocalDate, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDate, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalDate> _localDate(String name,
			Function<LocalDateConstraint<Arguments1<LocalDate>>, LocalDateConstraint<Arguments1<LocalDate>>> constraints) {
		return this._localDate(LocalDateValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) LocalDate, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) LocalDate> _localDate(String name) {
		return this._localDate(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Long, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _long(ValueValidator<Long, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Long, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Long> _long(String name,
			Function<LongConstraint<Arguments1<Long>>, LongConstraint<Arguments1<Long>>> constraints) {
		return this._long(LongValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Long, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Long> _long(String name) {
		return this._long(name, Function.identity());
	}

	public <T1, T2> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T1, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T2> _object(ValueValidator<T1, T2> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _object(String name,
			Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return this._object(ObjectValidatorBuilder.of(name, constraints).build());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) T, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _object(String name) {
		return this._object(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) OffsetDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _offsetDateTime(ValueValidator<OffsetDateTime, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) OffsetDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) OffsetDateTime> _offsetDateTime(String name,
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		return this._offsetDateTime(OffsetDateTimeValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) OffsetDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) OffsetDateTime> _offsetDateTime(String name) {
		return this._offsetDateTime(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Short, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _short(ValueValidator<Short, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Short, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Short> _short(String name,
			Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		return this._short(ShortValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Short, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Short> _short(String name) {
		return this._short(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) String, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _string(ValueValidator<String, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) String, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) String> _string(String name,
			Function<CharSequenceConstraint<Arguments1<String>, String>, CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return this._string(StringValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) String, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) String> _string(String name) {
		return this._string(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) YearMonth, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _yearMonth(ValueValidator<YearMonth, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) YearMonth, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) YearMonth> _yearMonth(String name,
			Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
		return this._yearMonth(YearMonthValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) YearMonth, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) YearMonth> _yearMonth(String name) {
		return this._yearMonth(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Year, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _year(ValueValidator<Year, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Year, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Year> _year(String name,
			Function<YearConstraint<Arguments1<Year>>, YearConstraint<Arguments1<Year>>> constraints) {
		return this._year(YearValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) Year, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) Year> _year(String name) {
		return this._year(name, Function.identity());
	}

	public <T> ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) ZonedDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) T> _zonedDateTime(ValueValidator<ZonedDateTime, T> validator) {
		return new ${next_class}<>($(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done); fi) validator);
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) ZonedDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) ZonedDateTime> _zonedDateTime(String name,
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		return this._zonedDateTime(ZonedDateTimeValidatorBuilder.of(name, constraints).build());
	}

	public ${next_class}<$(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done);fi) ZonedDateTime, $(if [ "${i}" -gt 0 ];then echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done);fi) ZonedDateTime> _zonedDateTime(String name) {
		return this._zonedDateTime(name, Function.identity());
	}
EOD
fi)

$(if [ "${i}" -gt 1 ];then
cat <<EOD
	public <X> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> apply(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super R${j}, ";done) | sed 's/,$//'), ? extends X> f) {
		return ArgumentsValidators.split($(echo $(for j in `seq 1 ${i}`;do echo -n "this.v${j}, ";done) | sed 's/,$//')).apply(f);
	}
EOD
fi)
$(if [ "${i}" == "1" ];then
cat <<EOD
	public <X> Arguments1Validator<A1, X> apply(Function1<? super R1, ? extends X> f) {
		return Arguments1Validator.from(this.v1.andThen(f::apply));
	}

	public Arguments1Validator<A1, R1> get() {
		return Arguments1Validator.from(this.v1);
	}
EOD
fi)
}
EOF
done
