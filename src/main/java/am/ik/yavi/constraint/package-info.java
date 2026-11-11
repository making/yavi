/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Constraints for validating property values.
 *
 * <p>
 * A constraint instance is obtained from a {@code constraint(...)} or {@code _...(...)}
 * method of {@link am.ik.yavi.builder.ValidatorBuilder} and is normally not created
 * directly:
 * </p>
 *
 * <pre>{@code
 * Validator<Car> validator = ValidatorBuilder.<Car>of()
 *     .constraint(Car::manufacturer, "manufacturer", c -> c.notNull())
 *     .constraint(Car::licensePlate, "licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
 *     .build();
 * }</pre>
 *
 * <p>
 * The package contains constraints for the following value categories:
 * </p>
 * <ul>
 * <li>numeric and character values: {@link am.ik.yavi.constraint.IntegerConstraint},
 * {@link am.ik.yavi.constraint.LongConstraint},
 * {@link am.ik.yavi.constraint.ShortConstraint},
 * {@link am.ik.yavi.constraint.ByteConstraint},
 * {@link am.ik.yavi.constraint.DoubleConstraint},
 * {@link am.ik.yavi.constraint.FloatConstraint},
 * {@link am.ik.yavi.constraint.BigIntegerConstraint},
 * {@link am.ik.yavi.constraint.BigDecimalConstraint} and
 * {@link am.ik.yavi.constraint.CharacterConstraint}</li>
 * <li>temporal values: {@link am.ik.yavi.constraint.InstantConstraint},
 * {@link am.ik.yavi.constraint.LocalDateConstraint},
 * {@link am.ik.yavi.constraint.LocalDateTimeConstraint},
 * {@link am.ik.yavi.constraint.LocalTimeConstraint},
 * {@link am.ik.yavi.constraint.OffsetDateTimeConstraint},
 * {@link am.ik.yavi.constraint.YearConstraint},
 * {@link am.ik.yavi.constraint.YearMonthConstraint} and
 * {@link am.ik.yavi.constraint.ZonedDateTimeConstraint}</li>
 * <li>char sequences: {@link am.ik.yavi.constraint.CharSequenceConstraint}</li>
 * <li>other values: {@link am.ik.yavi.constraint.ObjectConstraint},
 * {@link am.ik.yavi.constraint.BooleanConstraint} and
 * {@link am.ik.yavi.constraint.EnumConstraint}</li>
 * <li>containers: {@link am.ik.yavi.constraint.CollectionConstraint} and
 * {@link am.ik.yavi.constraint.MapConstraint}</li>
 * </ul>
 *
 * <p>
 * Subpackages provide constraints on arrays ({@code am.ik.yavi.constraint.array}), char
 * sequence details ({@code am.ik.yavi.constraint.charsequence}), IP addresses
 * ({@code am.ik.yavi.constraint.inetaddress}), password policies
 * ({@code am.ik.yavi.constraint.password}) and the shared base classes
 * ({@code am.ik.yavi.constraint.base}).
 * </p>
 *
 * <p>
 * Unless stated otherwise, a constraint treats a {@code null} value as valid and skips
 * the check for it. Use {@link am.ik.yavi.core.Constraint#notNull()} or
 * {@link am.ik.yavi.core.Constraint#isNull()} to constrain {@code null} itself, and see
 * {@link am.ik.yavi.core.NullAs} for the underlying setting.
 * </p>
 */
@NonNullApi
package am.ik.yavi.constraint;

import am.ik.yavi.jsr305.NonNullApi;
