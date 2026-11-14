/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

/**
 * The rules on the value that a validator produces, added one after another with
 * {@link ValueValidator#constraintOnTarget}. All rules are checked on the same value and
 * every violated rule is reported, so that the violations are seen at once like those of
 * the properties.
 *
 * <p>
 * This is what an implementation of {@link ValueValidator} that overrides
 * {@code constraintOnTarget} to return its own type keeps: it starts with
 * {@link #empty()}, returns itself with {@link #and one more rule} for each call and
 * {@linkplain #check checks} the value that it produced. Instances are immutable.
 *
 * @param <X> the type of the produced value
 * @since 0.18.0
 */
public final class TargetConstraints<X extends @Nullable Object> {

	private static final TargetConstraints<?> EMPTY = new TargetConstraints<>(List.of());

	private final List<Rule<X>> rules;

	/**
	 * A rule and the place of its violation.
	 */
	private record Rule<X extends @Nullable Object> (Predicate<? super X> predicate, ViolationMessage violationMessage,
			Function<ConstraintViolation, ConstraintViolation> located) {
	}

	private TargetConstraints(List<Rule<X>> rules) {
		this.rules = rules;
	}

	/**
	 * Returns no rules, which every value satisfies.
	 * @param <X> the type of the produced value
	 * @return the empty rules
	 */
	@SuppressWarnings("unchecked")
	public static <X extends @Nullable Object> TargetConstraints<X> empty() {
		return (TargetConstraints<X>) EMPTY;
	}

	/**
	 * Returns these rules followed by the given one.
	 * @param predicate the rule that the produced value must satisfy
	 * @param name the name of the reported violation
	 * @param violationMessage the message key and the default message format of the
	 * reported violation
	 * @return the rules with the additional one
	 */
	public TargetConstraints<X> and(Predicate<? super X> predicate, String name, ViolationMessage violationMessage) {
		final List<Rule<X>> appended = new ArrayList<>(this.rules);
		appended.add(new Rule<>(predicate, violationMessage, violation -> violation.prefixed(name)));
		return new TargetConstraints<>(List.copyOf(appended));
	}

	/**
	 * Returns these rules with their violations located under the property with the given
	 * name, as {@link ValueValidator#prefixed(String)} does. A rule added afterwards is
	 * not located there.
	 * @param name the name of the property that contains the produced value
	 * @return the rules with prefixed violation names and paths
	 */
	public TargetConstraints<X> prefixed(String name) {
		final List<Rule<X>> prefixed = new ArrayList<>(this.rules.size());
		for (Rule<X> rule : this.rules) {
			prefixed.add(new Rule<>(rule.predicate, rule.violationMessage,
					rule.located.andThen(violation -> violation.prefixed(name))));
		}
		return new TargetConstraints<>(List.copyOf(prefixed));
	}

	/**
	 * Checks every rule on the given value. The last message argument of a violation is
	 * the value.
	 * @param value the value that a validator produced
	 * @param locale the locale targeted for the violation messages
	 * @return the value, or the violations of all rules that it does not satisfy in the
	 * order of the rules
	 */
	public Validated<X> check(X value, Locale locale) {
		List<ConstraintViolation> violations = null;
		for (Rule<X> rule : this.rules) {
			if (!rule.predicate.test(value)) {
				if (violations == null) {
					violations = new ArrayList<>();
				}
				violations.add(rule.located.apply(ConstraintViolation.of(rule.violationMessage, value, locale)));
			}
		}
		return violations == null ? Validated.successWith(value) : Validated.failureWith(violations);
	}

	/**
	 * Returns a validator that checks the value produced by the given validator with
	 * these rules. A rule added to the returned validator with
	 * {@link ValueValidator#constraintOnTarget} is checked together with these, also
	 * after {@link ValueValidator#named(String)},
	 * {@link ValueValidator#describedAs(ValueDescription)},
	 * {@link ValueValidator#preprocess(Function)} and
	 * {@link ValueValidator#readingWith(InputReader)}, which the returned validator
	 * passes on to the given one.
	 * @param validator the validator whose value is checked
	 * @param <T> the type of the object to be validated
	 * @return a validator with these rules
	 */
	public <T> ValueValidator<T, X> constrain(ValueValidator<T, X> validator) {
		return new Constrained<>(validator, this);
	}

	private record Constrained<T, X extends @Nullable Object> (ValueValidator<T, X> validator,
			TargetConstraints<X> constraints) implements ValueValidator<T, X> {

		@Override
		public Validated<X> validate(@Nullable T t, Locale locale, ConstraintContext constraintContext) {
			return this.validator.validate(t, locale, constraintContext)
				.flatMap(x -> this.constraints.check(x, locale));
		}

		@Override
		public ValueValidator<T, X> constraintOnTarget(Predicate<? super X> predicate, String name,
				ViolationMessage violationMessage) {
			return new Constrained<>(this.validator, this.constraints.and(predicate, name, violationMessage));
		}

		@Override
		public ValueDescription description() {
			return this.validator.description();
		}

		@Override
		public ValueValidator<T, X> describedAs(ValueDescription description) {
			return new Constrained<>(this.validator.describedAs(description), this.constraints);
		}

		@Override
		public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
			return new Constrained<>(this.validator.readingWith(reader), this.constraints);
		}

		@Override
		public ValueValidator<T, X> preprocess(Function<? super T, ? extends @Nullable T> transform) {
			return new Constrained<>(this.validator.preprocess(transform), this.constraints);
		}

		@Override
		public ValueValidator<T, X> named(String name) {
			return new Constrained<>(this.validator.named(name), this.constraints.prefixed(name));
		}

	}

}
