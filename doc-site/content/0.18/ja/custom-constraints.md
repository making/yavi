# カスタム制約

組み込みの制約が対象としない規則は、述語として書きます。述語は値の制約の `predicate` で適用し、違反は組み込みの制約と同じように報告されます。

## 述語による制約

`predicate` は述語、メッセージキー、メッセージフォーマットを取ります。

```java
Validator<Book> validator = ValidatorBuilder.<Book>of()
        ._string(Book::title, "title", c -> c.notBlank().lessThanOrEqual(64))
        ._string(Book::isbn, "isbn", c -> c.notBlank()
                .predicate(ISBNValidator::isISBN13, "string.isbn13", "\"{0}\" must be ISBN13 format"))
        .build();
```

`{0}` はプロパティ名で、最後のプレースホルダは制約に違反した値です。この間に引数を追加するには、後述するカスタム制約の `arguments` を上書きします。

`predicateNullable` は `null` を受け入れる述語のための同じメソッドです。述語の実行前に `null` を拒否しません。

## カスタム制約クラス

`am.ik.yavi.core.CustomConstraint<V>` は述語にメッセージキー、既定のメッセージフォーマット、引数を組み合わせるため、1 つの制約を一度定義して複数の場所で適用できます。

```java
public final class Isbn13Constraint implements CustomConstraint<String> {

    @Override
    public boolean test(String isbn) {
        if (isbn == null || !isbn.matches("[0-9]{13}")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = isbn.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return (10 - sum % 10) % 10 == isbn.charAt(12) - '0';
    }

    @Override
    public String messageKey() {
        return "string.isbn13";
    }

    @Override
    public String defaultMessageFormat() {
        return "\"{0}\" must be ISBN13 format";
    }

}
```

`predicate` で適用します。述語は、その規則のメッセージとともに、対象オブジェクトの規則でも使えます。

```java
Validator<Book> validator = ValidatorBuilder.<Book>of()
        ._string(Book::isbn, "isbn", c -> c.notBlank().predicate(new Isbn13Constraint()))
        .build();

Validator<Book> checked = ValidatorBuilder.<Book>of()
        ._string(Book::isbn, "isbn", c -> c.notBlank())
        .constraintOnTarget(book -> new Isbn13Constraint().test(book.isbn()), "isbn",
                "string.isbn13", "\"{0}\" must be ISBN13 format")
        .build();
```

## メッセージキーと引数

メッセージキーは違反を識別します。既定のメッセージフォーマットは、`MessageFormatter` がそのキーのメッセージを持たない場合に使われ、`{0}` はプロパティ名です。追加の引数が必要な制約は `arguments` を上書きします。引数は名前と違反した値の間に挿入され、違反した値は最後の引数として追加されます。

```java
public final class InstantRangeConstraint implements CustomConstraint<Instant> {

    private final Instant start;

    private final Instant end;

    public InstantRangeConstraint(Instant start, Instant end) {
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
    }

    @Override
    public Object[] arguments(Instant violatedValue) {
        return new Object[] { this.start /* {1} */, this.end /* {2} */ };
    }

    @Override
    public String defaultMessageFormat() {
        return "Instant value \"{0}\" must be between \"{1}\" and \"{2}\".";
    }

    @Override
    public String messageKey() {
        return "instant.range";
    }

    @Override
    public boolean test(Instant instant) {
        return instant.isAfter(this.start) && instant.isBefore(this.end);
    }

}
```

クライアントが `ViolationDetail` から読む独自のメッセージは、引数に名前を付けます。そうすればクライアントはメッセージフォーマットの位置を知らなくても `messageKey` からメッセージを組み立てられます。`{1}` 以降の引数の名前を `paramNames` で上書きします。違反した値は、返す意図がない限り名前を付けません。

```java
@Override
public List<String> paramNames() {
    return List.of("start", "end");
}
```

`ViolationMessage.of(key, format, "start", "end")` で作成した `ViolationMessage` も同じように引数に名前を付けます。

## 型をまたいだ再利用

`CustomConstraint` はテストする値の型でパラメータ化されます。クラスを一度書き、その型の値を制約する場所にインスタンスを渡します。オブジェクトのバリデータでは `_string`、単一値のバリデータでは `Yavi._string`、値が対象オブジェクトそのものである場合は `constraintOnTarget` です。メッセージキーと引数もインスタンスとともに渡ります。

```java
private static final CustomConstraint<String> ISBN = new Isbn13Constraint();

Validator<Book> bookValidator = ValidatorBuilder.<Book>of()
        ._string(Book::isbn, "isbn", c -> c.notBlank().predicate(ISBN))
        .build();

StringValidator<String> isbnValidator = Yavi._string("isbn", c -> c.notBlank().predicate(ISBN));
```

規則だけを共有する場合は、`predicate(Predicate, ViolationMessage)` で同じ述語を、それを使うプロパティのメッセージとともに適用します。
