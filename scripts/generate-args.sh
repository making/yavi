#!/bin/bash
set -e
n=16

for i in `seq 1 ${n}`;do
  class="Arguments${i}"
  file="$(dirname $0)/../src/main/java/am/ik/yavi/arguments/${class}.java"
  echo $file
  cat <<EOF > ${file}
/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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

/**
 * @since 0.3.0
 */
public class ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')> $(if [ ${i} -gt 1 ];then echo -n "extends Arguments$((${i} - 1))<$(echo $(for j in `seq 1 $((${i} - 1))`;do echo -n "A${j}, ";done) | sed 's/,$//')>"; else echo -n "";fi) {

	protected final A${i} arg${i};

	${class}($(echo $(for j in `seq 1 ${i}`;do echo -n "A${j} arg${j}, ";done) | sed 's/,$//')) {$(if [ ${i} -gt 1 ];then echo;echo "		super($(echo $(for j in `seq 1 $((${i} - 1))`;do echo -n "arg${j}, ";done) | sed 's/,$//'));";fi)
		this.arg${i} = arg${i};
	}

	public final A${i} arg${i}() {
		return this.arg${i};
	}

	public final <X> X map(${class}.Mapper<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> mapper) {
		return mapper.map($(echo $(for j in `seq 1 ${i}`;do echo -n "arg${j}, ";done) | sed 's/,$//'));
	}

	public interface Mapper<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> {
		X map($(echo $(for j in `seq 1 ${i}`;do echo -n "A${j} arg${j}, ";done) | sed 's/,$//'));
	}
}
EOF
done

cat << EOF > $(dirname $0)/../src/main/java/am/ik/yavi/arguments/Arguments.java
/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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

/**
 * @since 0.3.0
 */
public final class Arguments {
$(for i in `seq 1 ${n}`;do
  cat <<EOJ
	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')> Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')> of($(echo $(for j in `seq 1 ${i}`;do echo -n "A${j} arg${j}, ";done) | sed 's/,$//')) {
		return new Arguments${i}<>($(echo $(for j in `seq 1 ${i}`;do echo -n "arg${j}, ";done) | sed 's/,$//'));
	}

EOJ
done)
}
EOF

cat << EOF > $(dirname $0)/../src/main/java/am/ik/yavi/builder/ArgumentsValidatorBuilder.java
/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;

import java.util.Objects;
import java.util.function.Function;

/**
 * @since 0.3.0
 */
public final class ArgumentsValidatorBuilder {
$(for i in `seq 1 ${n}`;do
  cat <<EOJ
	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> Arguments${i}ValidatorBuilder<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> of(Arguments${i}.Mapper<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> mapper) {
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
		private final Arguments${i}.Mapper<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> mapper;
		private ValidatorBuilder<Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>> builder;

		public ${class}(Arguments${i}.Mapper<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public ${arguments} builder(
				Function<ValidatorBuilder<Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>>, ValidatorBuilder<Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}

		public Arguments${i}Validator<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> build() {
			final ValidatorBuilder<Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments${i}Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}
	}

EOJ
done)
}
EOF

for i in `seq 1 ${n}`;do
  class="Arguments${i}Validator"
  file="$(dirname $0)/../src/main/java/am/ik/yavi/arguments/${class}.java"
  arguments="Arguments${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//')>"
  args="$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j} a${j}, ";done) | sed 's/,$//')"
  as="$(echo $(for j in `seq 1 ${i}`;do echo -n "a${j}, ";done) | sed 's/,$//')"
  echo $file
  cat <<EOF > ${file}
/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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

import java.util.List;
import java.util.Locale;

import am.ik.yavi.core.CollectionValidator;
import am.ik.yavi.core.ConstraintCondition;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.ConstraintPredicates;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import am.ik.yavi.core.ValidatorSubset;
import am.ik.yavi.fn.Either;
import am.ik.yavi.fn.Pair;
import am.ik.yavi.message.MessageFormatter;

/**
 * @since 0.3.0
 */
public final class ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X>
		extends Validator<${arguments}> {
	private final Arguments${i}.Mapper<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> mapper;

	public ${class}(String messageKeySeparator,
			List<ConstraintPredicates<${arguments}, ?>> constraintPredicates,
			List<CollectionValidator<${arguments}, ?, ?>> collectionValidators,
			List<Pair<ConstraintCondition<${arguments}>, ValidatorSubset<${arguments}>>> conditionalValidators,
			MessageFormatter messageFormatter, Arguments${i}.Mapper<$(echo $(for j in `seq 1 ${i}`;do echo -n "A${j}, ";done) | sed 's/,$//'), X> mapper) {
		super(messageKeySeparator, constraintPredicates, collectionValidators,
				conditionalValidators, messageFormatter);
		this.mapper = mapper;
	}

	public ConstraintViolations validateArgs(${args},
			ConstraintGroup constraintGroup) {
		return this.validate(Arguments.of(${as}), Locale.getDefault(),
				constraintGroup);
	}

	public ConstraintViolations validateArgs(${args}, Locale locale) {
		return this.validate(Arguments.of(${as}), locale, ConstraintGroup.DEFAULT);
	}

	public ConstraintViolations validateArgs(${args}, Locale locale,
			ConstraintGroup constraintGroup) {
		return this.validate(Arguments.of(${as}), locale, constraintGroup);
	}

	public ConstraintViolations validateArgs(${args}) {
		return this.validate(Arguments.of(${as}), Locale.getDefault(),
				ConstraintGroup.DEFAULT);
	}

	public Either<ConstraintViolations, X> validateArgsToEither(${args}) {
		return this
				.validateToEither(Arguments.of(${as}), Locale.getDefault(),
						ConstraintGroup.DEFAULT)
				.rightMap(values -> values.map(this.mapper));
	}

	public Either<ConstraintViolations, X> validateArgsToEither(${args},
			ConstraintGroup constraintGroup) {
		return this.validateToEither(Arguments.of(${as}), Locale.getDefault(),
				constraintGroup).rightMap(values -> values.map(this.mapper));
	}

	public Either<ConstraintViolations, X> validateArgsToEither(${args},
			Locale locale) {
		return this
				.validateToEither(Arguments.of(${as}), locale,
						ConstraintGroup.DEFAULT)
				.rightMap(values -> values.map(this.mapper));
	}

	public Either<ConstraintViolations, X> validateArgsToEither(${args},
			Locale locale, ConstraintGroup constraintGroup) {
		ConstraintViolations violations = this.validate(Arguments.of(${as}), locale,
				constraintGroup);
		if (violations.isValid()) {
			return Either.right(Arguments.of(${as}).map(this.mapper));
		}
		else {
			return Either.left(violations);
		}
	}
}
EOF
done
