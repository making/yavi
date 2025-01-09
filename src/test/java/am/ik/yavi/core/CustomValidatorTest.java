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
package am.ik.yavi.core;

import java.time.Instant;
import java.util.Objects;

import am.ik.yavi.Book;
import am.ik.yavi.Range;
import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomValidatorTest {

	@Test
	public void predicate() {
		Validator<Book> validator = ValidatorBuilder.<Book>of() //
			.constraint(Book::isbn, "isbn", c -> c.notNull() //
				.predicate(CustomValidatorTest::isISBN13, //
						"custom.isbn13", "\"{0}\" must be ISBN13 format"))
			.build();
		{
			ConstraintViolations violations = validator.validate(new Book("9784777519699"));
			assertThat(violations.isValid()).isTrue();
		}
		{
			ConstraintViolations violations = validator.validate(new Book("1111111111111"));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("\"isbn\" must be ISBN13 format");
			assertThat(violations.get(0).messageKey()).isEqualTo("custom.isbn13");
		}
		{
			ConstraintViolations violations = validator.validate(new Book(null));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("\"isbn\" must not be null");
			assertThat(violations.get(0).messageKey()).isEqualTo("object.notNull");
		}
	}

	@Test
	public void predicateCustom() {
		Validator<Book> validator = ValidatorBuilder.<Book>of() //
			.constraint(Book::isbn, "isbn", c -> c.notNull() //
				.predicate(IsbnConstraint.SINGLETON))
			.build();
		{
			ConstraintViolations violations = validator.validate(new Book("9784777519699"));
			assertThat(violations.isValid()).isTrue();
		}
		{
			ConstraintViolations violations = validator.validate(new Book("1111111111111"));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("\"isbn\" must be ISBN13 format");
			assertThat(violations.get(0).messageKey()).isEqualTo("custom.isbn13");
		}
		{
			ConstraintViolations violations = validator.validate(new Book(null));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("\"isbn\" must not be null");
			assertThat(violations.get(0).messageKey()).isEqualTo("object.notNull");
		}
	}

	@Test
	public void predicateNullable() {
		Validator<Book> validator = ValidatorBuilder.<Book>of() //
			.constraint(Book::isbn, "isbn", c -> c.predicateNullable(v -> v != null && isISBN13(v), //
					"custom.isbn13", "\"{0}\" must be ISBN13 format"))
			.build();
		{
			ConstraintViolations violations = validator.validate(new Book("9784777519699"));
			assertThat(violations.isValid()).isTrue();
		}
		{
			ConstraintViolations violations = validator.validate(new Book("1111111111111"));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("\"isbn\" must be ISBN13 format");
			assertThat(violations.get(0).messageKey()).isEqualTo("custom.isbn13");
		}
		{
			ConstraintViolations violations = validator.validate(new Book(null));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("\"isbn\" must be ISBN13 format");
		}
	}

	@Test
	public void range() throws Exception {
		Validator<Range> validator = ValidatorBuilder.<Range>of() //
			.constraintOnObject(r -> r, "range", c -> c.notNull() //
				.predicate(r -> {
					Range range = Range.class.cast(r);
					return range.getFrom() < range.getTo();
				}, "range.cross", "\"from\" must be less than \"to\""))
			.build();
		{
			Range range = new Range(0, 10);
			ConstraintViolations violations = validator.validate(range);
			assertThat(violations.isValid()).isTrue();
		}
		{
			Range range = new Range(11, 10);
			ConstraintViolations violations = validator.validate(range);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("\"from\" must be less than \"to\"");
			assertThat(violations.get(0).messageKey()).isEqualTo("range.cross");
		}
	}

	@Test
	public void rangeConstraintOnTarget() throws Exception {
		Validator<Range> validator = ValidatorBuilder.<Range>of() //
			.constraintOnTarget(RangeConstraint.SINGLETON, "range") //
			.build();
		{
			Range range = new Range(0, 10);
			ConstraintViolations violations = validator.validate(range);
			assertThat(violations.isValid()).isTrue();
		}
		{
			Range range = new Range(11, 10);
			ConstraintViolations violations = validator.validate(range);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("\"from\" must be less than \"to\"");
			assertThat(violations.get(0).messageKey()).isEqualTo("range.cross");
		}
	}

	@Test
	public void rangeCustom() throws Exception {
		Validator<Range> validator = ValidatorBuilder.<Range>of() //
			.constraintOnObject(r -> r, "range", c -> c.notNull() //
				.predicateNullable(RangeConstraint.SINGLETON))
			.build();
		{
			Range range = new Range(0, 10);
			ConstraintViolations violations = validator.validate(range);
			assertThat(violations.isValid()).isTrue();
		}
		{
			Range range = new Range(11, 10);
			ConstraintViolations violations = validator.validate(range);
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message()).isEqualTo("\"from\" must be less than \"to\"");
			assertThat(violations.get(0).messageKey()).isEqualTo("range.cross");
		}
	}

	@Test
	public void instantRangeCustom_gh36() throws Exception {
		final InstantRangeConstraint constraint = new InstantRangeConstraint(Instant.parse("2020-01-15T00:00:00Z"),
				Instant.parse("2020-01-16T00:00:00Z"));
		final Validator<Instant> validator = ValidatorBuilder.of(Instant.class)
			.constraintOnTarget(constraint, "instant")
			.build();

		final Instant instant = Instant.parse("2020-01-17T00:00:00Z");
		final ConstraintViolations violations = validator.validate(instant);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo(
				"Instant value \"instant\" must be between \"2020-01-15T00:00:00Z\" and \"2020-01-16T00:00:00Z\".");
		assertThat(violations.get(0).messageKey()).isEqualTo("string.instant");
	}

	// This logic is written in
	// http://en.wikipedia.org/wiki/International_Standard_Book_Number
	static boolean isISBN13(String isbn) {
		if (isbn.length() != 13) {
			return false;
		}
		int check = 0;
		try {
			for (int i = 0; i < 12; i += 2) {
				check += Integer.parseInt(isbn.substring(i, i + 1));
			}
			for (int i = 1; i < 12; i += 2) {
				check += Integer.parseInt(isbn.substring(i, i + 1)) * 3;
			}
			check += Integer.parseInt(isbn.substring(12));
		}
		catch (NumberFormatException e) {
			return false;
		}
		return check % 10 == 0;
	}

	enum IsbnConstraint implements CustomConstraint<String> {

		SINGLETON;

		@Override
		public String defaultMessageFormat() {
			return "\"{0}\" must be ISBN13 format";
		}

		@Override
		public String messageKey() {
			return "custom.isbn13";
		}

		@Override
		public boolean test(String s) {
			return isISBN13(s);
		}

	}

	public enum RangeConstraint implements CustomConstraint<Range> {

		SINGLETON;

		@Override
		public String defaultMessageFormat() {
			return "\"from\" must be less than \"to\"";
		}

		@Override
		public String messageKey() {
			return "range.cross";
		}

		@Override
		public boolean test(Range r) {
			if (r == null) {
				return false;
			}
			return r.getFrom() < r.getTo();
		}

	}

	static class InstantRangeConstraint implements CustomConstraint<Instant> {

		private final Instant end;

		private final Instant start;

		InstantRangeConstraint(Instant start, Instant end) {
			this.start = Objects.requireNonNull(start);
			this.end = Objects.requireNonNull(end);
		}

		@Override
		public Object[] arguments(Instant violatedValue) {
			return new Object[] { this.start, this.end };
		}

		@Override
		public String defaultMessageFormat() {
			return "Instant value \"{0}\" must be between \"{1}\" and \"{2}\".";
		}

		@Override
		public String messageKey() {
			return "string.instant";
		}

		@Override
		public boolean test(Instant instant) {
			return instant.isAfter(this.start) && instant.isBefore(this.end);
		}

	}

}
