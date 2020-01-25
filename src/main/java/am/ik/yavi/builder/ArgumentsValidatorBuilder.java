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

import java.util.Objects;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.Arguments10;
import am.ik.yavi.arguments.Arguments10Validator;
import am.ik.yavi.arguments.Arguments11;
import am.ik.yavi.arguments.Arguments11Validator;
import am.ik.yavi.arguments.Arguments12;
import am.ik.yavi.arguments.Arguments12Validator;
import am.ik.yavi.arguments.Arguments13;
import am.ik.yavi.arguments.Arguments13Validator;
import am.ik.yavi.arguments.Arguments14;
import am.ik.yavi.arguments.Arguments14Validator;
import am.ik.yavi.arguments.Arguments15;
import am.ik.yavi.arguments.Arguments15Validator;
import am.ik.yavi.arguments.Arguments16;
import am.ik.yavi.arguments.Arguments16Validator;
import am.ik.yavi.arguments.Arguments1Validator;
import am.ik.yavi.arguments.Arguments2;
import am.ik.yavi.arguments.Arguments2Validator;
import am.ik.yavi.arguments.Arguments3;
import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.arguments.Arguments4;
import am.ik.yavi.arguments.Arguments4Validator;
import am.ik.yavi.arguments.Arguments5;
import am.ik.yavi.arguments.Arguments5Validator;
import am.ik.yavi.arguments.Arguments6;
import am.ik.yavi.arguments.Arguments6Validator;
import am.ik.yavi.arguments.Arguments7;
import am.ik.yavi.arguments.Arguments7Validator;
import am.ik.yavi.arguments.Arguments8;
import am.ik.yavi.arguments.Arguments8Validator;
import am.ik.yavi.arguments.Arguments9;
import am.ik.yavi.arguments.Arguments9Validator;
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;

/**
 * @since 0.3.0
 */
public final class ArgumentsValidatorBuilder {
	public static <A1, X> Arguments1ValidatorBuilder<A1, X> of(
			Arguments1.Mapper<A1, X> mapper) {
		return new Arguments1ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, X> Arguments2ValidatorBuilder<A1, A2, X> of(
			Arguments2.Mapper<A1, A2, X> mapper) {
		return new Arguments2ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, X> Arguments3ValidatorBuilder<A1, A2, A3, X> of(
			Arguments3.Mapper<A1, A2, A3, X> mapper) {
		return new Arguments3ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, X> Arguments4ValidatorBuilder<A1, A2, A3, A4, X> of(
			Arguments4.Mapper<A1, A2, A3, A4, X> mapper) {
		return new Arguments4ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, A5, X> Arguments5ValidatorBuilder<A1, A2, A3, A4, A5, X> of(
			Arguments5.Mapper<A1, A2, A3, A4, A5, X> mapper) {
		return new Arguments5ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, A5, A6, X> Arguments6ValidatorBuilder<A1, A2, A3, A4, A5, A6, X> of(
			Arguments6.Mapper<A1, A2, A3, A4, A5, A6, X> mapper) {
		return new Arguments6ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, X> Arguments7ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, X> of(
			Arguments7.Mapper<A1, A2, A3, A4, A5, A6, A7, X> mapper) {
		return new Arguments7ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, X> Arguments8ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, X> of(
			Arguments8.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, X> mapper) {
		return new Arguments8ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, X> Arguments9ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, X> of(
			Arguments9.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, X> mapper) {
		return new Arguments9ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, X> Arguments10ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, X> of(
			Arguments10.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, X> mapper) {
		return new Arguments10ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, X> Arguments11ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, X> of(
			Arguments11.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, X> mapper) {
		return new Arguments11ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X> Arguments12ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X> of(
			Arguments12.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X> mapper) {
		return new Arguments12ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, X> Arguments13ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, X> of(
			Arguments13.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, X> mapper) {
		return new Arguments13ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, X> Arguments14ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, X> of(
			Arguments14.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, X> mapper) {
		return new Arguments14ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, X> Arguments15ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, X> of(
			Arguments15.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, X> mapper) {
		return new Arguments15ValidatorBuilder<>(mapper);
	}

	public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, X> Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, X> of(
			Arguments16.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, X> mapper) {
		return new Arguments16ValidatorBuilder<>(mapper);
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments10ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, X> {
		private final Arguments10.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, X> mapper;
		private ValidatorBuilder<Arguments10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10>> builder;

		public Arguments10ValidatorBuilder(
				Arguments10.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments10Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, X> build() {
			final ValidatorBuilder<Arguments10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments10Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments10ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, X> builder(
				Function<ValidatorBuilder<Arguments10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10>>, ValidatorBuilder<Arguments10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments11ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, X> {
		private final Arguments11.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, X> mapper;
		private ValidatorBuilder<Arguments11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11>> builder;

		public Arguments11ValidatorBuilder(
				Arguments11.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments11Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, X> build() {
			final ValidatorBuilder<Arguments11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments11Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments11ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, X> builder(
				Function<ValidatorBuilder<Arguments11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11>>, ValidatorBuilder<Arguments11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments12ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X> {
		private final Arguments12.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X> mapper;
		private ValidatorBuilder<Arguments12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12>> builder;

		public Arguments12ValidatorBuilder(
				Arguments12.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X> build() {
			final ValidatorBuilder<Arguments12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments12Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments12ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X> builder(
				Function<ValidatorBuilder<Arguments12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12>>, ValidatorBuilder<Arguments12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments13ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, X> {
		private final Arguments13.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, X> mapper;
		private ValidatorBuilder<Arguments13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13>> builder;

		public Arguments13ValidatorBuilder(
				Arguments13.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments13Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, X> build() {
			final ValidatorBuilder<Arguments13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments13Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments13ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, X> builder(
				Function<ValidatorBuilder<Arguments13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13>>, ValidatorBuilder<Arguments13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments14ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, X> {
		private final Arguments14.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, X> mapper;
		private ValidatorBuilder<Arguments14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14>> builder;

		public Arguments14ValidatorBuilder(
				Arguments14.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments14Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, X> build() {
			final ValidatorBuilder<Arguments14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments14Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments14ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, X> builder(
				Function<ValidatorBuilder<Arguments14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14>>, ValidatorBuilder<Arguments14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments15ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, X> {
		private final Arguments15.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, X> mapper;
		private ValidatorBuilder<Arguments15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15>> builder;

		public Arguments15ValidatorBuilder(
				Arguments15.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments15Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, X> build() {
			final ValidatorBuilder<Arguments15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments15Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments15ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, X> builder(
				Function<ValidatorBuilder<Arguments15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15>>, ValidatorBuilder<Arguments15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, X> {
		private final Arguments16.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, X> mapper;
		private ValidatorBuilder<Arguments16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16>> builder;

		public Arguments16ValidatorBuilder(
				Arguments16.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments16Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, X> build() {
			final ValidatorBuilder<Arguments16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments16Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments16ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, X> builder(
				Function<ValidatorBuilder<Arguments16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16>>, ValidatorBuilder<Arguments16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments1ValidatorBuilder<A1, X> {
		private final Arguments1.Mapper<A1, X> mapper;
		private ValidatorBuilder<Arguments1<A1>> builder;

		public Arguments1ValidatorBuilder(Arguments1.Mapper<A1, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments1Validator<A1, X> build() {
			final ValidatorBuilder<Arguments1<A1>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments1Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments1ValidatorBuilder<A1, X> builder(
				Function<ValidatorBuilder<Arguments1<A1>>, ValidatorBuilder<Arguments1<A1>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments2ValidatorBuilder<A1, A2, X> {
		private final Arguments2.Mapper<A1, A2, X> mapper;
		private ValidatorBuilder<Arguments2<A1, A2>> builder;

		public Arguments2ValidatorBuilder(Arguments2.Mapper<A1, A2, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments2Validator<A1, A2, X> build() {
			final ValidatorBuilder<Arguments2<A1, A2>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments2Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments2ValidatorBuilder<A1, A2, X> builder(
				Function<ValidatorBuilder<Arguments2<A1, A2>>, ValidatorBuilder<Arguments2<A1, A2>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments3ValidatorBuilder<A1, A2, A3, X> {
		private final Arguments3.Mapper<A1, A2, A3, X> mapper;
		private ValidatorBuilder<Arguments3<A1, A2, A3>> builder;

		public Arguments3ValidatorBuilder(Arguments3.Mapper<A1, A2, A3, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments3Validator<A1, A2, A3, X> build() {
			final ValidatorBuilder<Arguments3<A1, A2, A3>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments3Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments3ValidatorBuilder<A1, A2, A3, X> builder(
				Function<ValidatorBuilder<Arguments3<A1, A2, A3>>, ValidatorBuilder<Arguments3<A1, A2, A3>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments4ValidatorBuilder<A1, A2, A3, A4, X> {
		private final Arguments4.Mapper<A1, A2, A3, A4, X> mapper;
		private ValidatorBuilder<Arguments4<A1, A2, A3, A4>> builder;

		public Arguments4ValidatorBuilder(Arguments4.Mapper<A1, A2, A3, A4, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments4Validator<A1, A2, A3, A4, X> build() {
			final ValidatorBuilder<Arguments4<A1, A2, A3, A4>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments4Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments4ValidatorBuilder<A1, A2, A3, A4, X> builder(
				Function<ValidatorBuilder<Arguments4<A1, A2, A3, A4>>, ValidatorBuilder<Arguments4<A1, A2, A3, A4>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments5ValidatorBuilder<A1, A2, A3, A4, A5, X> {
		private final Arguments5.Mapper<A1, A2, A3, A4, A5, X> mapper;
		private ValidatorBuilder<Arguments5<A1, A2, A3, A4, A5>> builder;

		public Arguments5ValidatorBuilder(
				Arguments5.Mapper<A1, A2, A3, A4, A5, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments5Validator<A1, A2, A3, A4, A5, X> build() {
			final ValidatorBuilder<Arguments5<A1, A2, A3, A4, A5>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments5Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments5ValidatorBuilder<A1, A2, A3, A4, A5, X> builder(
				Function<ValidatorBuilder<Arguments5<A1, A2, A3, A4, A5>>, ValidatorBuilder<Arguments5<A1, A2, A3, A4, A5>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments6ValidatorBuilder<A1, A2, A3, A4, A5, A6, X> {
		private final Arguments6.Mapper<A1, A2, A3, A4, A5, A6, X> mapper;
		private ValidatorBuilder<Arguments6<A1, A2, A3, A4, A5, A6>> builder;

		public Arguments6ValidatorBuilder(
				Arguments6.Mapper<A1, A2, A3, A4, A5, A6, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments6Validator<A1, A2, A3, A4, A5, A6, X> build() {
			final ValidatorBuilder<Arguments6<A1, A2, A3, A4, A5, A6>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments6Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments6ValidatorBuilder<A1, A2, A3, A4, A5, A6, X> builder(
				Function<ValidatorBuilder<Arguments6<A1, A2, A3, A4, A5, A6>>, ValidatorBuilder<Arguments6<A1, A2, A3, A4, A5, A6>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments7ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, X> {
		private final Arguments7.Mapper<A1, A2, A3, A4, A5, A6, A7, X> mapper;
		private ValidatorBuilder<Arguments7<A1, A2, A3, A4, A5, A6, A7>> builder;

		public Arguments7ValidatorBuilder(
				Arguments7.Mapper<A1, A2, A3, A4, A5, A6, A7, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments7Validator<A1, A2, A3, A4, A5, A6, A7, X> build() {
			final ValidatorBuilder<Arguments7<A1, A2, A3, A4, A5, A6, A7>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments7Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments7ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, X> builder(
				Function<ValidatorBuilder<Arguments7<A1, A2, A3, A4, A5, A6, A7>>, ValidatorBuilder<Arguments7<A1, A2, A3, A4, A5, A6, A7>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments8ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, X> {
		private final Arguments8.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, X> mapper;
		private ValidatorBuilder<Arguments8<A1, A2, A3, A4, A5, A6, A7, A8>> builder;

		public Arguments8ValidatorBuilder(
				Arguments8.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments8Validator<A1, A2, A3, A4, A5, A6, A7, A8, X> build() {
			final ValidatorBuilder<Arguments8<A1, A2, A3, A4, A5, A6, A7, A8>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments8Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments8ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, X> builder(
				Function<ValidatorBuilder<Arguments8<A1, A2, A3, A4, A5, A6, A7, A8>>, ValidatorBuilder<Arguments8<A1, A2, A3, A4, A5, A6, A7, A8>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}

	/**
	 * @since 0.3.0
	 */
	public static final class Arguments9ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, X> {
		private final Arguments9.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, X> mapper;
		private ValidatorBuilder<Arguments9<A1, A2, A3, A4, A5, A6, A7, A8, A9>> builder;

		public Arguments9ValidatorBuilder(
				Arguments9.Mapper<A1, A2, A3, A4, A5, A6, A7, A8, A9, X> mapper) {
			this.mapper = Objects.requireNonNull(mapper, "'mapper' must not be null.");
		}

		public Arguments9Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, X> build() {
			final ValidatorBuilder<Arguments9<A1, A2, A3, A4, A5, A6, A7, A8, A9>> builder = this.builder != null
					? this.builder
					: ValidatorBuilder.of();
			final MessageFormatter messageFormatter = builder.messageFormatter == null
					? new SimpleMessageFormatter()
					: builder.messageFormatter;
			return new Arguments9Validator<>(builder.messageKeySeparator,
					builder.predicatesList, builder.collectionValidators,
					builder.conditionalValidators, messageFormatter, mapper);
		}

		public Arguments9ValidatorBuilder<A1, A2, A3, A4, A5, A6, A7, A8, A9, X> builder(
				Function<ValidatorBuilder<Arguments9<A1, A2, A3, A4, A5, A6, A7, A8, A9>>, ValidatorBuilder<Arguments9<A1, A2, A3, A4, A5, A6, A7, A8, A9>>> definition) {
			this.builder = definition.apply(ValidatorBuilder.of());
			return this;
		}
	}
}
