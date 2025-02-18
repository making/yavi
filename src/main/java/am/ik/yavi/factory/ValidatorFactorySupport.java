/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.factory;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.jsr305.Nullable;
import am.ik.yavi.message.MessageFormatter;

abstract class ValidatorFactorySupport {

	protected final String messageKeySeparator;

	protected final MessageFormatter messageFormatter;

	protected ValidatorFactorySupport(@Nullable String messageKeySeparator,
			@Nullable MessageFormatter messageFormatter) {
		this.messageKeySeparator = messageKeySeparator;
		this.messageFormatter = messageFormatter;
	}

	protected <T> ValidatorBuilder<T> initBuilder() {
		final ValidatorBuilder<T> builder = this.messageKeySeparator == null ? new ValidatorBuilder<>()
				: new ValidatorBuilder<>(this.messageKeySeparator);
		if (this.messageFormatter != null) {
			builder.messageFormatter(messageFormatter);
		}
		return builder;
	}

}
