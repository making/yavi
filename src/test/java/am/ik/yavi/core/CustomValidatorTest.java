package am.ik.yavi.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.Book;
import am.ik.yavi.Range;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

public class CustomValidatorTest {

	@Test
	public void predicate() {
		Validator<Book> validator = new Validator<Book>() //
				.constraint(Book::isbn, "isbn", c -> c.notNull() //
						.predicate(v -> isISBN13(v.toString()), //
								"custom.isbn13", "\"{0}\" must be ISBN13 format"));
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
		}
		{
			ConstraintViolations violations = validator.validate(new Book(null));
			assertThat(violations.isValid()).isFalse();
			assertThat(violations.size()).isEqualTo(1);
			assertThat(violations.get(0).message())
					.isEqualTo("\"isbn\" must not be null");
		}
	}

	@Test
	public void predicateNullable() {
		Validator<Book> validator = new Validator<Book>() //
				.constraint(Book::isbn, "isbn",
						c -> c.predicateNullable(v -> v != null && isISBN13(v.toString()), //
								"custom.isbn13", "\"{0}\" must be ISBN13 format"));
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
	public void range() throws Exception {
		Validator<Range> validator = new Validator<Range>() //
				.constraintForObject(r -> r, "range", c -> c.notNull() //
						.predicate(r -> {
							Range range = Range.class.cast(r);
							return range.getFrom() < range.getTo();
						}, "custom.range", "\"from\" must be less than \"to\""));
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
		}
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
}
