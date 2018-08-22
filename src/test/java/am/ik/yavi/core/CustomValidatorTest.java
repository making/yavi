package am.ik.yavi.core;

import java.util.function.Predicate;

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

	enum IsbnConstraint implements CustomConstraint<String> {
		SINGLETON;

		@Override
		public Predicate<String> predicate() {
			return CustomValidatorTest::isISBN13;
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

	enum RangeConstraint implements CustomConstraint<Range>, Predicate<Range> {
		SINGLETON;

		@Override
		public Predicate<Range> predicate() {
			return this;
		}

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

	@Test
	public void predicate() {
		Validator<Book> validator = new Validator<Book>() //
				.constraint(Book::isbn, "isbn", c -> c.notNull() //
						.predicate(CustomValidatorTest::isISBN13, //
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
						c -> c.predicateNullable(v -> v != null && isISBN13(v), //
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
	public void predicateCustom() {
		Validator<Book> validator = new Validator<Book>() //
				.constraint(Book::isbn, "isbn", c -> c.notNull() //
						.predicate(IsbnConstraint.SINGLETON));
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

	@Test
	public void rangeCustom() throws Exception {
		Validator<Range> validator = new Validator<Range>() //
				.constraintForObject(r -> r, "range", c -> c.notNull() //
						.predicateNullable(RangeConstraint.SINGLETON));
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
}
