/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.constraint.password;

/**
 * @since 0.7.0
 */
public class ObjectPasswordPoliciesBuilder<T, E>
		extends PasswordPoliciesBuilder<T, E, ObjectPasswordPoliciesBuilder<T, E>> {
	@Override
	protected ObjectPasswordPoliciesBuilder<T, E> cast() {
		return this;
	}
}
