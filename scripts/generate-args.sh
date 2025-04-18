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
	 * Returns the argument at position ${j}.
	 *
	 * @return the argument at position ${j}
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
	 * Returns a new Arguments${j} instance containing only the first ${j} arguments.
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
	 * Returns a new Arguments${j} instance containing only the last ${j} arguments.
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
	 * Appends an additional argument to create a new, larger Arguments instance.
	 *
	 * @param <B> the type of the argument to append
	 * @param arg the argument to append
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
	 * Prepends an additional argument to create a new, larger Arguments instance.
	 *
	 * @param <B> the type of the argument to prepend
	 * @param arg the argument to prepend
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
	 * Returns a new Arguments${i} instance with the arguments in reverse order.
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
 * @param <A${j}> the type of argument at position ${j}"
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

  cat <<EOF > ${file}
/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.arguments;

import java.util.Objects;

import am.ik.yavi.fn.Function${i};
import am.ik.yavi.jsr305.Nullable;

/**
 * A container class that holds ${i} arguments, providing type-safe access
 * to each argument and mapping functionality to transform these arguments.
 *
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh${type_param_docs}
 * @since 0.3.0
 */
public final class ${class_definition} {
${field_declarations}

	/**
	 * Creates a new Arguments${i} instance with the provided arguments.
	 *
	 * @param ${param_docs}
	 */
	${class}(${constructor_params}) {${constructor_assignments}
	}
${arg_methods}
	/**
	 * Applies the provided mapping function to all arguments contained in this instance.
	 *
	 * @param <X> the type of the result
	 * @param mapper the function to apply to the arguments
	 * @return the result of applying the mapper function to the arguments
	 */
	public <X> X map(Function${i}<${function_params}, ? extends X> mapper) {
		return mapper.apply(${map_args});
	}${first_methods}${last_methods}${append_method}${prepend_method}${reverse_method}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 *
	 * @param obj the reference object with which to compare
	 * @return true if this object is the same as the obj argument; false otherwise
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
	 * Returns a hash code value for the object.
	 *
	 * @return a hash code value for this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(${hashcode_params});
	}

	/**
	 * Returns a string representation of the object.
	 *
	 * @return a string representation of the object
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
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
  echo $file
  cat <<EOF > ${file}
/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.3.0
 */
@FunctionalInterface
$(if [ "${i}" == "1" ];then
cat <<EOD
public interface ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> extends ValueValidator<A1, X> {

	/**
	 * Convert {@link Validatable} instance into {@link Arguments1Validator}
	 *
	 * @param validator core validator
	 * @param <X> target class
	 * @return arguments1 validator
	 * @since 0.8.0
	 */
	static <X> Arguments1Validator<X, X> from(Validatable<X> validator) {
		return Arguments1Validator.from(validator.applicative());
	}

	/**
	 * Convert {@link ValueValidator} instance into {@link Arguments1Validator}
	 *
	 * @param valueValidator value validator
	 * @param <A1> class of argument1
	 * @param <X> target class
	 * @return arguments1 validator
	 * @since 0.8.0
	 */
	static <A1, X> Arguments1Validator<A1, X> from(ValueValidator<A1, X> valueValidator) {
		return valueValidator::validate;
	}

	/**
	 * Convert an Arguments1Validator that validates Arguments1&lt;A1&gt; to an Arguments1Validator&lt;A1, X&gt;
	 *
	 * @param validator validator for Arguments1&lt;A1&gt;
	 * @param <A1> class of argument1
	 * @param <X> target class
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
	 * Convert an Arguments1Validator that validates Arguments${i} to an ${class}
	 *
	 * @param validator validator for Arguments${i}
	 * @param <A1> type of first argument
$(if [ ${i} -gt 1 ]; then for j in $(seq 2 ${i}); do echo "	 * @param <A${j}> type of argument at position ${j}"; done; fi)
	 * @param <X> target result type
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
  @Override
	Validated<X> validate(${args},
			Locale locale, ConstraintContext constraintContext);
EOD
fi)
$(if [ "${i}" != "1" ];then
cat <<EOD
	Validated<X> validate(${args},
			Locale locale, ConstraintContext constraintContext);
EOD
fi)

	/**
	 * Convert this validator to one that validates Arguments${i} as a single object.
	 *
	 * @return a validator that takes an Arguments${i}
	 * @since 0.16.0
	 */
	default Arguments1Validator<${wrapArguments}, X> wrap() {
		${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X>> lazy = this.lazy();
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
				return lazy.wrap();
			}
		};
	}

	/**
	 * @since 0.7.0
	 */$(if [ "${i}" == "1" ];then echo;echo "	@Override"; fi)
	default <X2> ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X2> andThen(Function<? super X, ? extends X2> mapper) {
		return (${as}, locale, constraintContext) -> ${class}.this
				.validate(${as}, locale, constraintContext).map(mapper);
	}

	/**
	 * @since 0.11.0
	 */$(if [ "${i}" == "1" ];then echo;echo "	@Override"; fi)
	default <X2> ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X2> andThen(ValueValidator<? super X, X2> validator) {
		return (${as}, locale, constraintContext) -> ${class}.this
				.validate(${as}, locale, constraintContext)
				.flatMap(v -> validator.validate(v, locale, constraintContext));
	}

	/**
	 * @since 0.7.0
	 */
$(if [ "${i}" == "1" ];then
cat <<EOD
	@Override
	default <A> ${class}<A, X> compose(
			Function<? super A, ? extends A1> mapper) {
		return (a, locale, constraintContext) -> ${class}.this
				.validate(mapper.apply(a), locale, constraintContext);
	}
EOD
else
cat <<EOD
	default <A> Arguments1Validator<A, X> compose(
			Function<? super A, ? extends ${arguments}> mapper) {
		return (a, locale, constraintContext) -> {
			final ${arguments} args = mapper.apply(a);
			return ${class}.this.validate($(echo $(for j in `seq 1 ${i}`;do echo -n "args.arg${j}(), ";done) | sed 's/,$//'), locale, constraintContext);
		};
	}
EOD
fi)
$(cat <<EOD
	/**
	 * @since 0.10.0
	 */
	default ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X>> lazy() {
		throw new UnsupportedOperationException("lazy is not implemented!");
	}

	default Validated<X> validate(${args}) {
		return this.validate(${as}, Locale.getDefault(), ConstraintGroup.DEFAULT);
	}

	default Validated<X> validate(${args}, ConstraintContext constraintContext) {
		return this.validate(${as}, Locale.getDefault(), constraintContext);
	}

	default Validated<X> validate(${args}, Locale locale) {
		return this.validate(${as}, locale, ConstraintGroup.DEFAULT);
	}

	default X validated(${args}) throws ConstraintViolationsException {
		return this.validate(${as}).orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(${args}, ConstraintContext constraintContext)
			throws ConstraintViolationsException {
		return this.validate(${as}, constraintContext)
				.orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(${args}, Locale locale) throws ConstraintViolationsException {
		return this.validate(${as}, locale).orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(${args}, Locale locale, ConstraintContext constraintContext)
			throws ConstraintViolationsException {
		return this.validate(${as}, locale, constraintContext)
				.orElseThrow(ConstraintViolationsException::new);
	}
EOD)
$(if [ "${i}" == "1" ];then
cat <<EOD
	/**
	 * @since 0.7.0
	 */
	default <A$((${i} + 1)), Y> Arguments$((${i} + 1))Splitting<$(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "A${j}, ";done) | sed 's/,$//'), X, Y> split(ValueValidator<A$((${i} + 1)), Y> validator) {
		return new Arguments$((${i} + 1))Splitting<>(this, validator);
	}

	/**
	 * @since 0.7.0
	 */
	default <Y> Arguments$((${i} + 1))Combining<A${i}, X, Y> combine(ValueValidator<A${i}, Y> validator) {
		return new Arguments$((${i} + 1))Combining<>(this, validator);
	}

	/**
	 * @since 0.7.0
	 */
	@Override
	default Arguments1Validator<A1, X> indexed(int index) {
		return (a1, locale, constraintContext) -> Arguments1Validator.this
				.validate(a1, locale, constraintContext).indexed(index);
	}

	/**
	 * @since 0.8.0
	 */
	default <C extends Collection<X>> Arguments1Validator<Iterable<A1>, C> liftCollection(
			Supplier<C> factory) {
		return Arguments1Validator.from(ValueValidator.super.liftCollection(factory));
	}

	/**
	 * @since 0.8.0
	 */
	default Arguments1Validator<Iterable<A1>, List<X>> liftList() {
		return Arguments1Validator.from(ValueValidator.super.liftList());
	}

	/**
	 * @since 0.8.0
	 */
	default Arguments1Validator<Iterable<A1>, Set<X>> liftSet() {
		return Arguments1Validator.from(ValueValidator.super.liftSet());
	}

	/**
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
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> implements ${interface}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> {
  protected final Validator<${arguments}> validator;
	protected final Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends X> mapper;

	public ${class}(Validator<${arguments}> validator, Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends X> mapper) {
		this.validator = validator;
		this.mapper = mapper;
	}

	/**
	 * @since 0.10.0
	 */
	@Override
	public ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), Supplier<X>> lazy() {
		return new ${class}<>(this.validator, ($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//')) -> () -> this.mapper.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//')));
	}

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
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * Generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> {
$(for j in `seq 1 ${i}`;do echo "	protected final ValueValidator<? super A${j}, ? extends R${j}> v${j};";echo;done)

	public ${class}($(echo $(for j in `seq 1 ${i}`;do echo -n "ValueValidator<? super A${j}, ? extends R${j}> v${j}, ";done) | sed 's/,$//')) {
$(for j in `seq 1 ${i}`;do echo "		this.v${j} = v${j};";done)
	}

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
$(if [ ${i} -lt ${nn} ];then echo;echo "	public <A$((${i} + 1)), R$((${i} + 1))> Arguments$((${i} + 1))Splitting<$(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "R${j}, ";done) | sed 's/,$//')> split(ValueValidator<? super A$((${i} + 1)), ? extends R$((${i} + 1))> v$((${i} + 1))) {"; echo "		return new Arguments$((${i} + 1))Splitting<>($(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "v${j}, ";done) | sed 's/,$//'));"; echo "	}"; else echo -n "";fi)
}
EOF
done

for i in `seq 2 ${nn}`;do
  class="Arguments${i}Combining"
  file="$(dirname $0)/../src/main/java/am/ik/yavi/arguments/${class}.java"
  echo $file
  cat <<EOF > ${file}
/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class ${class}<A, $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> {

$(for j in `seq 1 ${i}`;do echo "	protected final ValueValidator<? super A, ? extends R${j}> v${j};";done)

	public ${class}($(echo $(for j in `seq 1 ${i}`;do echo -n "ValueValidator<? super A, ? extends R${j}> v${j}, ";done) | sed 's/,$//')) {
$(for j in `seq 1 ${i}`;do echo "		this.v${j} = v${j};";done)
	}

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
$(if [ ${i} -lt ${nn} ];then echo; echo "	public <R$((${i} + 1))> Arguments$((${i} + 1))Combining<A, $(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "R${j}, ";done) | sed 's/,$//')> combine(ValueValidator<? super A, ? extends R$((${i} + 1))> v$((${i} + 1))) {"; echo "		return new Arguments$((${i} + 1))Combining<>($(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "v${j}, ";done) | sed 's/,$//'));"; echo "	}"; else echo -n "";fi)

}
EOF
done

class="ArgumentsValidators"
file="$(dirname $0)/../src/main/java/am/ik/yavi/arguments/${class}.java"
echo $file
cat <<EOF > ${file}
/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * Generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class ${class} {

$(for i in `seq 2 ${nn}`;do cat <<EOD
	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> Arguments${i}Splitting<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> split($(echo $(for j in `seq 1 ${i}`;do echo -n "ValueValidator<? super A${j}, ? extends R${j}> v${j}, ";done) | sed 's/,$//')) {
		return new Arguments${i}Splitting<>($(echo $(for j in `seq 1 ${i}`;do echo -n "v${j}, ";done) | sed 's/,$//'));
	}
EOD
done)

$(for i in `seq 2 ${nn}`;do cat <<EOD
	public static <A, $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> Arguments${i}Combining<A, $(echo $(for j in `seq 1 ${i}`;do echo -n "R${j}, ";done) | sed 's/,$//')> combine($(echo $(for j in `seq 1 ${i}`;do echo -n "ValueValidator<? super A, ? extends R${j}> v${j}, ";done) | sed 's/,$//')) {
		return new Arguments${i}Combining<>($(echo $(for j in `seq 1 ${i}`;do echo -n "v${j}, ";done) | sed 's/,$//'));
	}
EOD
done)

$(for i in `seq 1 ${nn}`;do cat <<EOD
	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), R, T> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), List<R>> traverse${i}(
			Iterable<T> values,
			Function<? super T, ? extends Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends R>> f) {
		return ($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//'), locale, constraintContext) ->
			Validated.traverse(values, f.andThen(validator -> validator.validate($(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//'), locale, constraintContext)));
	}
EOD
done)

$(for i in `seq 1 ${nn}`;do cat <<EOD
	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), R> Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), List<R>> sequence${i}(
		Iterable<? extends Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "? super A${j}, ";done) | sed 's/,$//'), ? extends R>> values) {
		return traverse${i}(values, identity());
	}
EOD
done)

	/**
	 * @since 0.8.0
	 */
	public static <A1, R, C extends Collection<R>> Arguments1Validator<Iterable<A1>, C> liftCollection(
			ValueValidator<? super A1, ? extends R> validator, Supplier<C> factory) {
		return Arguments1Validator
				.from(ValueValidator.liftCollection(validator, factory));
	}

	public static <A1, R> Arguments1Validator<Iterable<A1>, List<R>> liftList(
			ValueValidator<? super A1, ? extends R> validator) {
		return Arguments1Validator.from(ValueValidator.liftList(validator));
	}

	/**
	 * @since 0.8.0
	 */
	public static <A1, R> Arguments1Validator<Iterable<A1>, Set<R>> liftSet(
			ValueValidator<? super A1, ? extends R> validator) {
		return Arguments1Validator.from(ValueValidator.liftSet(validator));
	}

	/**
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
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.builder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
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
