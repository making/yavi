# YAVI の使い方

この節では YAVI の基本的な使い方を説明します。

## 中核となる `Validator` インスタンスの定義と取得

中核となるバリデータ `am.ik.yavi.core.Validator` は `am.ik.yavi.builder.ValidatorBuilder` で定義し、取得できます。
例を示します。

```java
Validator<User> validator = ValidatorBuilder.<User> of() // or ValidatorBuilder.of(User.class)
    .constraint(User::getName, "name", c -> c.notNull().lessThanOrEqual(20))
    .constraint(User::getEmail, "email", c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50).email())
    .constraint(User::getAge, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200))
    .build();

ConstraintViolations violations = validator.validate(user);
boolean isValid = violations.isValid();
violations.forEach(x -> System.out.println(x.message()));
```

> **ヒント**
>
> YAVI は既定で違反メッセージをすべて蓄積します。検証中に違反が見つかっても、そこで打ち切りません。最初の制約違反が起きた時点で現在の検証から戻りたい場合は、「[フェイルファストモード](#fail-fast-mode)」を使います。

型推論の曖昧さを避けるには、`constraint` の代わりに型ごとの明示的な `_<type>` メソッドを使います。

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    ._string(User::getName, "name", c -> c.notNull().lessThanOrEqual(20))
    ._string(User::getEmail, "email", c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50).email())
    ._integer(User::getAge, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200))
    .build();
```

`constraint` メソッドの第 1 引数は、`java.util.function.Function` であれば「getter」でなくてもかまいません。

たとえば [Records](https://openjdk.java.net/jeps/395) の `Validator` を作るなら、次のように素直に実装できます。

```java
public record User(String name, String email, int age) {
}

Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(User::name, "name", c -> c.notNull().lessThanOrEqual(20))
    .constraint(User::email, "email", c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50).email())
    .constraint(User::age, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200))
    .build();
```

また、対象のフィールドをメソッドから公開したくない場合は、次のように制約を定義できます。

```java
public class User {
    private final String name;

    public final Validator<User> validator = ValidatorBuilder.<User> of()
        ._string(x -> x.name, "name", c -> c.notNull().lessThanOrEqual(20))
        .build();

    public User(String name) {
        this.name = name;
    }
    // Omits others
}
```

他の制約ルールについては「[組み込み制約](built-in-constraints.md)」を参照してください。

> **ヒント**
>
> フィールド名を文字列リテラルで指定したくない場合は、「[アノテーションプロセッサ](annotation-processor.md)」を使って完全に型安全にできます。

## ネストしたオブジェクトへの制約

`nest` メソッドを使うと、ネストしたフィールドに制約を適用できます。
ネストしたフィールド用の `Validator` に委譲してもよいですし、ネストしたフィールドに対する制約の組をその場で定義してもかまいません。

```java
public class Address {
    private Country country;
    private City city;
    // Omits other fields and getters
}

Validator<Country> countryValidator = ValidatorBuilder.<Country> of()
    .constraint(Country::getName, "name", c -> c.notBlank().lessThanOrEqual(20))
    .build();
Validator<City> cityValidator = ValidatorBuilder.<City> of()
    .constraint(City::getName, "name", c -> c.notBlank().lessThanOrEqual(100))
    .build();

Validator<Address> validator = ValidatorBuilder.<Address> of()
    .nest(Address::getCountry, "country", countryValidator)
    .nest(Address::getCity, "city", cityValidator)
    .build();
```

または次のように書きます。

```java
Validator<Address> validator = ValidatorBuilder.<Address> of()
    .nest(Address::getCountry, "country",
        b -> b.constraint(Country::getName, "name", c -> c.notBlank().lessThanOrEqual(20)))
    .nest(Address::getCity, "city",
        b -> b.constraint(City::getName, "name", c -> c.notBlank().lessThanOrEqual(100)))
    .build();
```

ネストしたフィールドが null になり得る場合は、`nest` の代わりに `nestIfPresent` を使います。

## Collection / Map / Array の要素への制約

`forEach` メソッドを使うと、Collection / Map / Array の各要素に制約を適用できます。
ネストしたフィールドと同様に、各要素を検証する `Validator` に委譲してもよいですし、要素に対する制約の組をその場で定義してもかまいません。

```java
public class History {
	private final int revision;

	public History(int revision) {
		this.revision = revision;
	}

	public int getRevision() {
		return revision;
	}
}

public class Histories {
	private final List<History> value;

	public Histories(List<History> value) {
		this.value = value;
	}

	public List<History> asList() {
		return value;
	}
}

Validator<History> historyValidator = ValidatorBuilder.<History> of()
    .constraint(History::getRevision, "revision", c -> c.notNull().greaterThanOrEqual(1))
    .build();

Validator<Histories> validator = ValidatorBuilder.<Histories> of()
    .forEach(Histories::asList, "histories", historyValidator)
    .build();
```

または次のように書きます。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
    .forEach(Histories::asList, "histories",
        b -> b.constraint(History::getRevision, "revision", c -> c.notNull().greaterThanOrEqual(1)))
    .build();
```

collection / map / array のフィールドが null になり得る場合は、`forEach` の代わりに `forEachIfPresent` を使います。

> **注意**
>
> map の要素への制約は、既定では値にだけ適用できます。
> map のキーに制約を適用したい場合は、map をキーの `Set` に変換し、それをコレクションとして扱います。
>
> ```java
> ToCollection<CodeMap, Collection<String>, String> toCollection = codeMap -> codeMap.asMap().keySet();
> Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
>     .forEach(toCollection, "codeMap", b -> b._string(String::toString, "value", c -> c.notEmpty()))
>     .build();
> ```

## 特定の条件にだけ制約を適用する

`am.ik.yavi.core.ConstraintCondition` を使うと、特定の条件にだけ制約を適用できます。
0.11.0 以降、条件は `ConstraintGroup` ではなく `am.ik.yavi.core.ConstraintContext` を受け取るので、検証呼び出しの任意の属性を読めます。

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraintOnCondition((user, context) -> !user.getName().isEmpty(),
        b -> b.constraint(User::getEmail, "email", c -> c.email().notEmpty()))
    .build();
```

上の email への制約は、name が空でないときにだけ有効になります。

`ConstraintContext` は `Map` またはルックアップ関数から作成し、検証メソッドに渡せます。

```java
ConstraintContext context = ConstraintContext.from(Map.of("country", "IT"));
// or
ConstraintContext context = ConstraintContext.from(key -> lookupAttribute(key));

Validator<User> validator = ValidatorBuilder.<User> of()
    .constraintOnCondition((user, ctx) -> "IT".equals(ctx.attribute("country").value()),
        b -> b.constraint(User::getTaxId, "taxId", c -> c.notBlank()))
    .build();

ConstraintViolations violations = validator.validate(user, context);
```

`ConstraintCondition.hasAttribute(String)` と `ConstraintCondition.hasAttributeWithValue(String, Object)` は、よくある属性の確認を短く書くためのメソッドです。

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraintOnCondition(ConstraintCondition.hasAttributeWithValue("country", "IT"),
        b -> b.constraint(User::getTaxId, "taxId", c -> c.notBlank()))
    .build();
```

属性が存在しない場合、`context.attribute("key").value()` は `null` を返します。

## 特定のグループにだけ制約を適用する

`ConstraintGroup` は名前だけを持つ `ConstraintContext` です。
特定のグループにだけ次のように制約を適用できます。

```java
enum Group implements ConstraintGroup {
    CREATE, UPDATE, DELETE
}

Validator<User> validator = ValidatorBuilder.<User> of()
    .constraintOnCondition(Group.CREATE.toCondition(), b -> b.constraint(User::getId, "id", c -> c.isNull()))
    .constraintOnCondition(Group.UPDATE.toCondition(), b -> b.constraint(User::getId, "id", c -> c.notNull()))
    .build();
```

enum を宣言せず、名前からグループを作ることもできます。

```java
ConstraintGroup create = ConstraintGroup.of("CREATE");
```

検証するグループは `validate` メソッドで指定できます。

```java
ConstraintViolations violations = validator.validate(user, ConstraintGroup.of("CREATE"));
// or, with the enum above
violations = validator.validate(user, Group.CREATE);
```

`constraintOnGroup` メソッドというショートカットも使えます。

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraintOnGroup(Group.CREATE, b -> b.constraint(User::getId, "id", c -> c.isNull()))
    .constraintOnGroup(Group.UPDATE, b -> b.constraint(User::getId, "id", c -> c.notNull()))
    .build();
```

> **注意**
>
> 条件のないすべての制約は、どの制約グループでも検証されます。
> また、`validate` メソッドでグループを指定しない場合は `ConstraintGroup.DEFAULT` が使われます。
> グループは名前で照合し、名前が等しい 2 つのグループは等しいとみなします。

## 独自の制約の作成

「[組み込み制約](built-in-constraints.md)」にない制約を適用したい場合は、`am.ik.yavi.core.CustomConstraint` インタフェースを次のように実装して独自の制約を作成できます。

```java
public class IsbnConstraint implements CustomConstraint<String> {

    @Override
    public boolean test(String s) {
        // Delegate processing to another method
        return ISBNValidator.isISBN13(s);
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

作成した独自の制約は `predicate` メソッドで次のように指定できます。

```java
IsbnConstraint isbn = new IsbnConstraint();
Validator<Book> book = ValidatorBuilder.<Book> of()
    .constraint(Book::getTitle, "title", c -> c.notBlank().lessThanOrEqual(64))
    .constraint(Book::getIsbn, "isbn", c -> c.notBlank().predicate(isbn))
    .build();
```

`CustomConstraint` クラスを定義せず、制約ルールを `predicate` メソッドに直接書くこともできます。

```java
Validator<Book> book = ValidatorBuilder.<Book> of()
    .constraint(Book::getTitle, "title", c -> c.notBlank().lessThanOrEqual(64))
    .constraint(Book::getIsbn, "isbn", c -> c.notBlank()
        .predicate(s -> ISBNValidator.isISBN13(s), "string.isbn13", "\"{0}\" must be ISBN13 format"))
    .build();
```

違反メッセージの第 1 引数はフィールド名です。また、最後の引数は違反した値です。

別の引数を使いたい場合は、`arguments` メソッドを次のように上書きします。

```java
public class InstantRangeConstraint implements CustomConstraint<Instant> {

    private final Instant end;

    private final Instant start;

    InstantRangeConstraint(Instant start, Instant end) {
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
    }

    @Override
    public Object[] arguments(Instant violatedValue) {
        return new Object[] { this.start /* {1} */, this.end /* {2} */};
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

## フィールドをまたぐ検証

対象のクラス自体に制約を適用したい場合は、`constraintOnTarget` を使えます。
フィールドをまたぐ制約を適用したいときに、次のように使います。

```java
Validator<Range> validator = ValidatorBuilder.<Range> of()
    .constraint(range::getFrom, "from", c -> c.greaterThan(0))
    .constraint(range::getTo, "to", c -> c.greaterThan(0))
    .constraintOnTarget(range -> range.getTo() > range.getFrom(), "to", "to.isGreaterThanFrom", "\"to\" must be greater than \"from\"")
    .build();
```

フィールドをまたぐ検証用の独自の制約も、次のように作成できます。

```java
public class RangeConstraint implements CustomConstraint<Range> {
    @Override
    public String defaultMessageFormat() {
        return "\"to\" must be greater than \"from\"";
    }

    @Override
    public String messageKey() {
        return "to.isGreaterThanFrom";
    }

    @Override
    public boolean test(Range range) {
        return range.getTo() > range.getFrom();
    }
}

RangeConstraint range = new RangeConstraint();
Validator<Range> validator = ValidatorBuilder.<Range> of()
        .constraintOnTarget(range, "to")
        .build();
```

## 違反メッセージの上書き

各制約の既定の違反メッセージは「[組み込み制約](built-in-constraints.md)」に定義されています。

違反メッセージをカスタマイズしたい場合は、対象の制約に `message` メソッドを次のように追加します。

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(User::getName, "name", c -> c.notNull().message("{0} is required!")
        .greaterThanOrEqual(1).message("{0} is too small!")
        .lessThanOrEqual(20).message("{0} is too large!"))
    .build();
```

`message` は、メッセージキーと既定のメッセージ形式を持つ `am.ik.yavi.core.ViolationMessage` も受け取ります。たとえば `ViolationMessage.of("string.isbn13", "\"{0}\" must be ISBN13 format")` です。
0.17.2 以降、制約のメッセージを上書きしても、メッセージの引数に違反した値が保持されます。

## Value バリデータ

`am.ik.yavi.core.ValueValidator` は入力を検証し、検証済みの値を生成します。
これは `Validator` のアプリカティブな対応物です。value バリデータの組み合わせについては「[バリデータの組み合わせ](combining-validators.md)」を、`Validated` の結果型については「[検証結果の組み合わせ](combining-validation-results.md)」を参照してください。

value バリデータは、検証済みの値を `map` で変換します。

```java
ValueValidator<String, String> trimmed = ValueValidator.<String> passThrough().map(String::trim);
```

`andThen(Function)` は 0.17.0 以降で非推奨です。代わりに `map(Function)` を使ってください。
`andThen(ValueValidator)` は引き続きサポートされ、バリデータを連鎖させます。前のバリデータが生成した値に次のバリデータを適用します。

```java
ValueValidator<String, Integer> length = trimmed
    .andThen(ValueValidator.<String> passThrough().map(String::length));
```

`validated(...)` は入力を検証して値を返し、違反があると `am.ik.yavi.core.ConstraintViolationsException` を投げます。
同じ動作は `Validation#throwIfInvalid` と `ConstraintViolations#throwIfInvalid` でも利用できます。

```java
try {
    String value = trimmed.validated(input);
}
catch (ConstraintViolationsException e) {
    // handle the violations
}

ConstraintViolations violations = validator.validate(user);
violations.throwIfInvalid(ConstraintViolationsException::new);
```

value バリデータは `Validatable.from(ValueValidator)` で通常の `Validator` に変換できます。生成された値は捨て、違反だけを報告します。

```java
Validatable<String> validator = Validatable.from(trimmed);
```

## 違反結果の扱い

`ConstraintViolation` は 0.15.0 以降、段階的に組み立てるビルダーを持ちます。
`name`、`messageKey`、`defaultMessageFormat` の各段階は必須で、引数、フォーマッタ、ロケールのメソッドは任意です。

```java
ConstraintViolation violation = ConstraintViolation.builder()
    .name("email")
    .messageKey("email.invalid")
    .defaultMessageFormat("\"{0}\" must be a valid email address")
    .argsWithPrependedNameAndAppendedViolatedValue(new Object[] {}, new ViolatedValue(input))
    .messageFormatter(SimpleMessageFormatter.getInstance())
    .locale(Locale.JAPAN)
    .build();
```

`ConstraintViolation#violatedValue()` は、制約に違反した値を返します。

`SimpleMessageFormatter` は `SimpleMessageFormatter.getInstance()` で取得できます。public な引数なしのコンストラクタも引き続き利用できますが、0.16.0 以降で非推奨です。

## メッセージフォーマッタ

YAVI は違反メッセージを組み立てるための `am.ik.yavi.message.MessageFormatter` インタフェースを提供します。

既定では `am.ik.yavi.message.SimpleMessageFormatter` が使われ、`java.text.MessageFormat` を使ってメッセージを補間するだけです。
共有のインスタンスは `SimpleMessageFormatter.getInstance()` で取得できます。public な引数なしのコンストラクタは後方互換性のために残されていますが、非推奨です。
メッセージキーと既定のメッセージ形式の一覧は「[組み込み制約](built-in-constraints.md)」にあります。

> **注意**
>
> エラーメッセージの機能として、Bean Validation と比べて次の点をサポートしています。
>
> * 既定でフィールド名をエラーメッセージに含める
> * 違反した値をエラーメッセージに含められる
>
> メッセージの最初のプレースホルダ `{0}` にはフィールド名が、最後のプレースホルダには違反した値が設定されます。
>
> 特に 2 点目は一般的な検証ライブラリでサポートされていません。たとえば「xyz は 100 文字以下にしてください」というエラーメッセージが返っても、実際に何文字入力されているのか、文字数を数えられているのかわからないので、少しずつ文字を削って試すことがあります。既定では次のメッセージが表示されるので、利用者はそのような無駄な作業をしなくて済みます。
>
> ![](https://user-images.githubusercontent.com/106908/44784067-4b010600-abc7-11e8-8878-930d017405bb.png)

メッセージの補間をカスタマイズしたい場合は、`MessageFormatter` を実装します。
例として、`messages.properties` のメッセージを読み込む実装を示します。

```java
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import am.ik.yavi.message.MessageFormatter;

public enum ResourceBundleMessageFormatter implements MessageFormatter {
    SINGLETON;

    @Override
    public String format(String messageKey, String defaultMessageFormat, Object[] args,
            Locale locale) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", locale);
        String format;
        try {
            format = resourceBundle.getString(messageKey);
        }
        catch (MissingResourceException e) {
            format = defaultMessageFormat;
        }
        try {
            String target = resourceBundle.getString((String) args[0] /* field name */);
            args[0] = target;
        }
        catch (MissingResourceException e) {
        }
        return new MessageFormat(format, locale).format(args);
    }
}
```

`MessageFormatter` を置き換えたい場合は、次のように設定できます。

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .messageFormatter(ResourceBundleMessageFormatter.SINGLETON)
    // ...
    .build();
```

## フェイルファストモード

フェイルファストモードを使うと、YAVI は最初の制約違反が起きた時点で現在の検証から戻ります。
制約違反がそもそも存在するかを素早く確認したいだけの、大きなオブジェクトグラフの検証で役立ちます。

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(User::getName, "name", c -> c.notNull().lessThanOrEqual(20))
    .constraint(User::getEmail, "email", c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50).email())
    .constraint(User::getAge, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200))
    .failFast(true) // <-- Enable the fail fast mode
    .build();
```

既存の `Validator` をフェイルファストモードに切り替えることもできます。

```java
Validator<User> failFastValidator = validator.failFast(true);
```

`failFast(boolean)` と `isFailFast()` メソッドは `am.ik.yavi.core.Validatable` にも宣言されているので、このインタフェースを通じてモードを読み取ったり切り替えたりできます。

```java
boolean failFast = failFastValidator.isFailFast();
Validatable<User> validatable = validator.failFast(true);
```

0.13.0 以降、ネストしたバリデータ、コレクションのバリデータ、条件付きバリデータは親のフェイルファストモードに従います。
モードはネストしたバリデータと要素のバリデータに引き継がれるので、それらのバリデータでも最初の違反で検証が止まります。

> **注意**
>
> この機能は [Hibernate Validator のフェイルファストモード](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-fail-fast) に由来します。

## 競合する制約名の解決

同じ制約名が複数回登録された場合、バリデータの構築時にどうするかを `ValidatorBuilder#conflictStrategy(ConflictStrategy)` が決めます。
既定の `am.ik.yavi.builder.ConflictStrategy.NOOP` は既存の制約を残して新しい制約を追加するので、対象はすべての制約で検証されます。
`ConflictStrategy.OVERRIDE` は既存の制約を削除し、新しい制約だけを残します。

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .conflictStrategy(ConflictStrategy.OVERRIDE)
    .constraint(User::getName, "name", c -> c.notNull())
    .constraint(User::getName, "name", c -> c.notBlank()) // replaces the previous "name" constraint
    .build();
```

独自の戦略は既存の述語のリストを変更できます。新しい述語は戦略が戻った後に追加されます。

```java
ConflictStrategy keepFirst = new ConflictStrategy() {
    @Override
    public <T> void resolveConflict(List<ConstraintPredicates<T, ?>> predicatesList,
            ConstraintPredicates<T, ?> predicates) {
        while (predicatesList.size() > 1) {
            predicatesList.remove(predicatesList.size() - 1);
        }
    }
};
```

## Kotlin サポート

Kotlin を使っている場合は、次のように DSL で `Validator` を定義できます。
DSL は Kotlin のリフレクションでプロパティ名を取得するので、`kotlin-reflect` を実行時クラスパスに置く必要があります。

```kotlin
val validator = validator<User> {
	User::name {
		notNull()
		lessThanOrEqual(20)
	}
	User::email {
		notNull()
		greaterThanOrEqual(5)
		lessThanOrEqual(50)
		email()
	}
	User::age {
		notNull()
		greaterThanOrEqual(0)
		lessThanOrEqual(100)
	}
}
```

フィールド名は次のように上書きできます。

```kotlin
val validator = validator<User> {
	(User::name)("Name") {
		notNull()
		lessThanOrEqual(20)
	}
	(User::email)("Email") {
		notNull()
		greaterThanOrEqual(5)
		lessThanOrEqual(50)
		email()
	}
	(User::age)("Age") {
		notNull()
		greaterThanOrEqual(0)
		lessThanOrEqual(100)
	}
}
```

`forEach` の例:

```kotlin
val validator = validator<DemoForEachCollection> {
    DemoForEachCollection::x forEach {
        DemoString::x {
            greaterThan(1)
            lessThan(5)
        }
    }
}
```

`nest` の例:

```kotlin
val validator = validator<DemoNested> {
    DemoNested::x nest {
        DemoString::x {
            greaterThan(1)
            lessThan(5)
        }
    }
}
```

`nest` の委譲の例:

```kotlin
val validator = validator<DemoNested> {
    DemoNested::x nest demoStringValidator
}
```

`nestIfPresent` と `forEachIfPresent` は、null になり得るネストしたオブジェクト、コレクション、マップ、配列を、存在する場合にだけ検証します。

```kotlin
val validator = validator<Order> {
    Order::customer nestIfPresent {
        Customer::name {
            notBlank()
        }
    }
    Order::items forEachIfPresent {
        OrderItem::name {
            notBlank()
        }
    }
}
```

日時と enum のプロパティには専用のオーバーロードがあります。
たとえば `LocalDate`、`LocalTime`、`LocalDateTime`、`ZonedDateTime`、`OffsetDateTime`、`Instant`、`Year`、`YearMonth` のプロパティには日時の制約を、enum のプロパティには `oneOf` を使えます。
利用できる制約については「[組み込み制約](built-in-constraints.md)」を参照してください。

```kotlin
enum class Status { ACTIVE, INACTIVE }

val validator = validator<Account> {
    Account::createdAt {
        notNull()
        past()
    }
    Account::status {
        notNull()
        oneOf(Status.ACTIVE)
    }
}
```

`onCondition` と `onGroup` は条件付きの制約とグループ固有の制約を追加し、`unwrap` は基になる `ValidatorBuilder` へのアクセスを提供します。

```kotlin
val validator = validator<User> {
    onCondition({ _, context -> "IT".equals(context.attribute("country").value()) }) {
        User::taxId {
            notNull()
        }
    }
    onGroup(ConstraintGroup.of("CREATE")) {
        User::id {
            isNull()
        }
    }
    unwrap().failFast(true)
}
```

### コルーチンのサポート

`am.ik.yavi.fn.ValidationExtensions` の拡張は、検証結果の処理を中断可能にします。
`awaitFold`、`awaitBimap`、`awaitMap`、`awaitMapErrors`、`awaitMapError` は `fold`、`bimap`、`map`、`mapErrors`、`mapError` に対応します。
これらには `kotlinx-coroutines-core` が必要です。

```kotlin
import am.ik.yavi.fn.awaitFold

suspend fun createUser(validated: Validated<User>): HttpStatus = validated.awaitFold(
    errorsMapper = { violations -> HttpStatus.BAD_REQUEST },
    mapper = { user -> HttpStatus.OK },
)
```
