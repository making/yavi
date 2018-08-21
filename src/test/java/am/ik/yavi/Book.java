package am.ik.yavi;

public class Book {
	private final String isbn;

	public Book(String isbn) {
		this.isbn = isbn;
	}

	public String isbn() {
		return this.isbn;
	}
}
