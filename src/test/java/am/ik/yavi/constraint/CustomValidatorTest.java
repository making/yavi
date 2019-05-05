/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.constraint;

import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import am.ik.yavi.core.ViolationMessage;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.Book;
import am.ik.yavi.Range;

public class CustomValidatorTest {

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

	@Test
	public void predicate() {
		Validator<Book> validator = Validator.<Book> builder() //
				.constraint(Book::isbn, "isbn", c -> c.notNull() //
						.predicate(CustomValidatorTest::isISBN13, //
								ViolationMessage.of("custom.isbn13",
										"\"{0}\" must be ISBN13 format")))
				.build();
		{
			ConstraintViolations violations = validator
					.validate(new Book("9784777519699"));
			assertThat(violations.isValid()).isTrue();
		}
		{
			ConstraintViolations violations = validator
					.validate(new Book("1111111111111"));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message())
					.isEqualTo("\"isbn\" must be ISBN13 format");
			assertThat(violations.get(0).messageKey()).isEqualTo("custom.isbn13");
		}
		{
			ConstraintViolations violations = validator.validate(new Book(null));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message())
					.isEqualTo("\"isbn\" must not be null");
			assertThat(violations.get(0).messageKey()).isEqualTo("object.notNull");
		}
	}

	@Test
	public void predicateNullable() {
		Validator<Book> validator = Validator.<Book> builder() //
				.constraint(
						Book::isbn, "isbn", c -> c
								.predicateNullable(v -> v != null && isISBN13(v), //
										ViolationMessage.of("custom.isbn13",
												"\"{0}\" must be ISBN13 format")))
				.build();
		{
			ConstraintViolations violations = validator
					.validate(new Book("9784777519699"));
			assertThat(violations.isValid()).isTrue();
		}
		{
			ConstraintViolations violations = validator
					.validate(new Book("1111111111111"));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message())
					.isEqualTo("\"isbn\" must be ISBN13 format");
			assertThat(violations.get(0).messageKey()).isEqualTo("custom.isbn13");
		}
		{
			ConstraintViolations violations = validator.validate(new Book(null));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message())
					.isEqualTo("\"isbn\" must be ISBN13 format");
		}
	}

	@Test
	public void predicateCustom() {
		Validator<Book> validator = Validator.<Book> builder() //
				.constraint(Book::isbn, "isbn", c -> c.notNull() //
						.predicate(IsbnConstraint.SINGLETON))
				.build();
		{
			ConstraintViolations violations = validator
					.validate(new Book("9784777519699"));
			assertThat(violations.isValid()).isTrue();
		}
		{
			ConstraintViolations violations = validator
					.validate(new Book("1111111111111"));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message())
					.isEqualTo("\"isbn\" must be ISBN13 format");
			assertThat(violations.get(0).messageKey()).isEqualTo("custom.isbn13");
		}
		{
			ConstraintViolations violations = validator.validate(new Book(null));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message())
					.isEqualTo("\"isbn\" must not be null");
			assertThat(violations.get(0).messageKey()).isEqualTo("object.notNull");
		}
	}

	@Test
	public void range() throws Exception {
		Validator<Range> validator = Validator.<Range> builder() //
				.constraintOnObject(r -> r, "range", c -> c.notNull() //
						.predicate(r -> {
							Range range = Range.class.cast(r);
							return range.getFrom() < range.getTo();
						}, ViolationMessage.of("custom.range",
								"\"from\" must be less than \"to\"")))
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
			assertThat(violations.get(0).message())
					.isEqualTo("\"from\" must be less than \"to\"");
			assertThat(violations.get(0).messageKey()).isEqualTo("custom.range");
		}
	}

	@Test
	public void rangeCustom() throws Exception {
		Validator<Range> validator = Validator.<Range> builder() //
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
			assertThat(violations.get(0).message())
					.isEqualTo("\"from\" must be less than \"to\"");
			assertThat(violations.get(0).messageKey()).isEqualTo("custom.range");
		}
	}

	@Test
	public void rangeConstraintOnTarget() throws Exception {
		Validator<Range> validator = Validator.<Range> builder() //
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
			assertThat(violations.get(0).message())
					.isEqualTo("\"from\" must be less than \"to\"");
			assertThat(violations.get(0).messageKey()).isEqualTo("custom.range");
		}
	}

	enum IsbnConstraint implements CustomConstraint<String> {
		SINGLETON;

		@Override
		public boolean test(String s) {
			return isISBN13(s);
		}

		@Override
		public String messageKey() {
			return "custom.isbn13";
		}

		@Override
		public String defaultMessageFormat() {
			return "\"{0}\" must be ISBN13 format";
		}
	}

	enum RangeConstraint implements CustomConstraint<Range> {
		SINGLETON;

		@Override
		public String messageKey() {
			return "custom.range";
		}

		@Override
		public String defaultMessageFormat() {
			return "\"from\" must be less than \"to\"";
		}

		@Override
		public boolean test(Range r) {
			if (r == null) {
				return false;
			}
			return r.getFrom() < r.getTo();
		}
	}
}
