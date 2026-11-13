/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintDescription;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import org.jspecify.annotations.Nullable;

/**
 * Validates every element of a list and then the list of the validated elements. This is
 * what {@code Yavi._list(elementValidator, constraints)} returns: the validator of a list
 * that is not a property of a schema, or that is the property of a schema whose
 * properties are known at run time.
 *
 * <p>
 * The elements are validated like {@link ValueValidator#liftList()} does: the violations
 * of an element are located under its index, and a {@code null} list is validated as an
 * empty list. The constraints are checked only when every element is valid, and their
 * violations are reported on the list itself.
 *
 * <p>
 * With {@link #splitBy(String)}, an untyped input may give the list as one text:
 *
 * <pre>{@code
 * ValueValidator<Object, List<Integer>> ids = Yavi._list(Yavi._integer(c -> c.positive()), c -> c.notEmpty())
 *     .splitBy(",")
 *     .untyped();
 * ids.validate("1,2,3"); // [1, 2, 3]
 * }</pre>
 *
 * @param <E> the type of the elements to validate
 * @param <T> the type of the validated elements
 * @since 0.18.0
 */
public final class ListValidator<E, T extends @Nullable Object>
		implements ValueValidator<Iterable<? extends E>, List<T>> {

	private final ValueValidator<E, T> elementValidator;

	private final @Nullable ValueValidator<List<T>, List<T>> constraints;

	private final List<ConstraintDescription> constraintDescriptions;

	private final @Nullable Pattern separator;

	private final ValueValidator<Iterable<? extends E>, List<T>> elements;

	/**
	 * Creates the validator of a list.
	 *
	 * <p>
	 * Instances are normally obtained from {@code Yavi._list(elementValidator,
	 * constraints)}.
	 * @param elementValidator value validator for each element
	 * @param constraints the validator of the list of the validated elements;
	 * {@code null} if the list has no constraints
	 * @param constraintDescriptions the descriptions of the constraints of the list, in
	 * the order they are checked
	 */
	public ListValidator(ValueValidator<E, T> elementValidator, @Nullable ValueValidator<List<T>, List<T>> constraints,
			List<ConstraintDescription> constraintDescriptions) {
		this(elementValidator, constraints, List.copyOf(constraintDescriptions), null);
	}

	private ListValidator(ValueValidator<E, T> elementValidator, @Nullable ValueValidator<List<T>, List<T>> constraints,
			List<ConstraintDescription> constraintDescriptions, @Nullable Pattern separator) {
		this.elementValidator = elementValidator;
		this.constraints = constraints;
		this.constraintDescriptions = constraintDescriptions;
		this.separator = separator;
		this.elements = separator == null ? elementValidator.liftList() : elementValidator.liftList(separator);
	}

	/**
	 * Returns a validator that also reads the list from text whose elements are separated
	 * by the given separator, such as {@code java,yavi}, as {@link #splitBy(Pattern)}
	 * states.
	 * @param separator the text that separates the elements, taken literally
	 * @return a validator of a list that may be given as text
	 * @throws IllegalArgumentException if the separator is empty
	 */
	public ListValidator<E, T> splitBy(String separator) {
		return this.splitBy(Pattern.compile(Pattern.quote(separator)));
	}

	/**
	 * Returns a validator that also reads the list from text whose elements are separated
	 * by the given pattern, such as one element per line with
	 * {@code Pattern.compile("\\R")}. It matters to an untyped input ({@link #untyped()},
	 * {@link #readingWith(InputReader)}), which is read as
	 * {@link ValueValidator#liftList(Pattern)} states: a text is split, a repeated
	 * request parameter too, an element that was split off is text that stands for a
	 * value, and no element is dropped or trimmed. A typed list is validated as before.
	 * @param separator the pattern that separates the elements in a text
	 * @return a validator of a list that may be given as text
	 * @throws IllegalArgumentException if the pattern matches the empty string
	 */
	public ListValidator<E, T> splitBy(Pattern separator) {
		return new ListValidator<>(this.elementValidator, this.constraints, this.constraintDescriptions, separator);
	}

	@Override
	public Validated<List<T>> validate(@Nullable Iterable<? extends E> values, Locale locale,
			ConstraintContext constraintContext) {
		return this.constrain(this.elements).validate(values, locale, constraintContext);
	}

	@Override
	public ValueDescription description() {
		return new ValueDescription.Elements(this.elementValidator.description(), this.constraintDescriptions,
				this.separator == null ? null : this.separator.pattern());
	}

	@Override
	public <N> ValueValidator<N, List<T>> readingWith(InputReader<N> reader) {
		return this.constrain(this.elements.readingWith(reader)).describedAs(this.description());
	}

	private <A> ValueValidator<A, List<T>> constrain(ValueValidator<A, List<T>> elements) {
		return this.constraints == null ? elements : elements.andThen(this.constraints);
	}

}
