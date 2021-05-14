#!/bin/bash
set -e
n=16

for i in `seq 1 ${n}`;do
  class="Function${i}"
  file="$(dirname $0)/../src/main/java/am/ik/yavi/fn/${class}.java"
  echo $file
  cat <<EOF > ${file}
/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.fn;

/**
 * Generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-applicative.sh
 *
 * @since 0.6.0
 */
public interface ${class}<$(echo $(for j in `seq 1 ${i}`;do echo -n "T${j}, ";done) | sed 's/,$//'), R> {

	R apply($(echo $(for j in `seq 1 ${i}`;do echo -n "T${j} t${j}, ";done) | sed 's/,$//'));
}
EOF
done

for i in `seq 1 ${n}`;do
  class="Composing${i}"
  file="$(dirname $0)/../src/main/java/am/ik/yavi/fn/${class}.java"
  echo $file
  cat <<EOF > ${file}
/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.fn;

import java.util.List;

/**
 * Generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-applicative.sh
 *
 * @since 0.6.0
 */
public class ${class}<E, $(echo $(for j in `seq 1 ${i}`;do echo -n "T${j}, ";done) | sed 's/,$//')> {
$(for j in `seq 1 ${i}`;do echo "	protected final Validation<E, T${j}> v${j};";echo;done)

	public ${class}($(echo $(for j in `seq 1 ${i}`;do echo -n "Validation<E, T${j}> v${j}, ";done) | sed 's/,$//')) {
$(for j in `seq 1 ${i}`;do echo "		this.v${j} = v${j};";done)
	}

	public <R> Validation<E, R> apply(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "T${j}, ";done) | sed 's/,$//'), R> f) {
		return $(echo $(for j in `seq ${i} 1`;do echo -n "v${j}.apply(";done) | sed 's/,$//')Validation.success(Functions.curry(f))$(echo $(for j in `seq 1 ${i}`;do echo -n ")";done));
	}
$(if [ ${i} -lt ${n} ];then echo;echo "	public <T$((${i} + 1))> Composing$((${i} + 1))<E, $(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "T${j}, ";done) | sed 's/,$//')> compose(Validation<E, T$((${i} + 1))> v$((${i} + 1))) {"; echo "		return new Composing$((${i} + 1))<>($(echo $(for j in `seq 1 $((${i} + 1))`;do echo -n "v${j}, ";done) | sed 's/,$//'));"; echo "	}"; else echo -n "";fi)
}
EOF
done

class="Validations"
file="$(dirname $0)/../src/main/java/am/ik/yavi/fn/${class}.java"
echo $file
cat <<EOF > ${file}
/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.fn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-applicative.sh
 *
 * @since 0.6.0
 */
public class ${class} {
$(for i in `seq 1 ${n}`;do echo "	public static <E, $(echo $(for j in `seq 1 ${i}`;do echo -n "T${j}, ";done) | sed 's/,$//')> Composing${i}<E, $(echo $(for j in `seq 1 ${i}`;do echo -n "T${j}, ";done) | sed 's/,$//')> compose($(echo $(for j in `seq 1 ${i}`;do echo -n "Validation<E, T${j}> v${j}, ";done) | sed 's/,$//')) {"; echo "		return new Composing${i}<>($(echo $(for j in `seq 1 ${i}`;do echo -n "v${j}, ";done) | sed 's/,$//'));"; echo "	}";echo;done)

$(for i in `seq 1 ${n}`;do echo "	public static <R, E, $(echo $(for j in `seq 1 ${i}`;do echo -n "T${j}, ";done) | sed 's/,$//')> Validation<E, R> apply(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "T${j}, ";done) | sed 's/,$//'), R> f, $(echo $(for j in `seq 1 ${i}`;do echo -n "Validation<E, T${j}> v${j}, ";done) | sed 's/,$//')) {"; echo "		return compose($(echo $(for j in `seq 1 ${i}`;do echo -n "v${j}, ";done) | sed 's/,$//')).apply(f);"; echo "	}";echo;done)

	public static <E, T> Validation<E, List<T>> sequence(
			Iterable<Validation<E, T>> validations) {
		final List<E> errors = new ArrayList<>();
		final List<T> values = new ArrayList<>();
		for (Validation<E, T> validation : validations) {
			if (!validation.isValid()) {
				errors.addAll(validation.errors());
			}
			else if (errors.isEmpty()) {
				values.add(validation.value());
			}
		}
		return errors.isEmpty() ? Validation.success(values) : Validation.failure(errors);
	}

	public static <E, T, U> Validation<E, List<U>> traverse(Iterable<T> values,
			Function<T, Validation<E, U>> mapper) {
		return sequence(StreamSupport.stream(values.spliterator(), false).map(mapper)
				.collect(Collectors.toList()));
	}
}
EOF

class="Functions"
file="$(dirname $0)/../src/main/java/am/ik/yavi/fn/${class}.java"
echo $file
cat <<EOF > ${file}
/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.fn;

/**
 * Generated by
 * https://github.com/making/yavi/blob/develop/scripts/generate-applicative.sh
 *
 * @since 0.6.0
 */
public class ${class} {
$(for i in `seq 1 ${n}`;do echo "	public static <$(echo $(for j in `seq 1 ${i}`;do echo -n "T${j}, ";done)) R> $(echo $(for j in `seq 1 ${i}`;do echo -n "Function1<T${j}, ";done)) R$(echo $(for j in `seq 1 ${i}`;do echo -n ">";done)) curry(Function${i}<$(echo $(for j in `seq 1 ${i}`;do echo -n "T${j}, ";done)) R> f) {"; echo "		return $(echo $(for j in `seq 1 ${i}`;do echo -n "t${j} -> ";done)) f.apply($(echo $(for j in `seq 1 ${i}`;do echo -n "t${j}, ";done) | sed 's/,$//'));"; echo "	}";echo;done)
}
EOF