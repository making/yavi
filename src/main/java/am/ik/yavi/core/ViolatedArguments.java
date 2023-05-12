/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import java.util.function.Predicate;
import java.util.function.Supplier;

import am.ik.yavi.jsr305.Nullable;

@FunctionalInterface
public interface ViolatedArguments<T> extends Supplier<Object[]> {

	/**
	 * returns arguments that can be used to build the violation message<br>
	 * The argument can be access by <code>{1}</code>, <code>{2}</code> , .... <br>
	 * Note that <code>{0}</code> is reserved for the property name and the last index in
	 * reserved for the actual value.<br>
	 * The implementer don't need to include the property name and the actual value.
	 * @param violatedValue the violated value
	 * @return the array of arguments
	 */
	Object[] arguments(@Nullable T violatedValue);

	@Override
	default Object[] get() {
		return this.arguments(null);
	}

	@SuppressWarnings("unchecked")
	static <T> Supplier<Object[]> supplyArguments(Predicate<T> predicate) {
		return (predicate instanceof ViolatedArguments) ? (ViolatedArguments<T>) predicate : () -> new Object[] {};
	}

}
