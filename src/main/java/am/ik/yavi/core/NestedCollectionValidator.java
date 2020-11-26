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
package am.ik.yavi.core;

import java.util.Collection;
import java.util.function.Function;

import am.ik.yavi.jsr305.Nullable;

public class NestedCollectionValidator<T, C extends Collection<E>, E, N> extends CollectionValidator<T, C, E> {

    private final Function<T, N> nested;

    public NestedCollectionValidator(Function<T, C> toCollection, String name, Validator<E> validator, Function<T, N> nested) {
        super(toCollection, name, validator);
        this.nested = nested;
    }

    @Nullable
    public N nestedCollection(T target) {
        return nested.apply(target);
    }
}
