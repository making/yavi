# バリデータの組み合わせ

`ValueValidator<S, T>` は型 `S` の入力を検証し、型 `T` の値を生成します。引数バリデータと適用バリデータも値バリデータなので、より大きなオブジェクトを検証するために分割して組み合わせられます。

## バリデータの分割

`ArgumentsValidators.split` は値バリデータを引数バリデータに組み合わせます。各バリデータは、オブジェクトを作成する関数の 1 つの引数を検証します。

```java
Arguments3Validator<String, String, Integer, Person> validator = ArgumentsValidators
        .split(nameValidator, emailValidator, ageValidator)
        .apply(Person::new);
```

同じ組み合わせは、バリデータを順に繋いでも構築できます。

```java
Arguments3Validator<String, String, Integer, Person> validator = nameValidator
        .split(emailValidator)
        .split(ageValidator)
        .apply(Person::new);
```

`split` の引数は `ValueValidator` なので、`ApplicativeValidator` も同じように組み合わせられます。バリデータが値を変換すれば値オブジェクトを生成できます。

```java
Arguments2Validator<Email, PhoneNumber, ContactInfo> validator = ArgumentsValidators
        .split(emailValidator, phoneNumberValidator)
        .apply(ContactInfo::new);

Validated<ContactInfo> validated = validator.validate(new Email("yavi@example.com"),
        new PhoneNumber("090-123-4567"));
```

すべての違反が蓄積されます。組み合わせはすべての引数が有効な場合にだけ検証されるため、無効な値で関数が呼ばれることはありません。

## バリデータの連結とマッピング

`andThen(ValueValidator)` は、あるバリデータが生成した値を次のバリデータに適用します。

```java
ValueValidator<String, String> checked = emailValidator
        .andThen(ValueValidator.<Email>passThrough().map(Email::value));
```

`map(Function)` は、別のバリデータを適用せずに検証済みの値を変換します。

```java
ValueValidator<String, String> emailValue = emailValidator.map(Email::value);
```

`andThen(Function)` は 0.18 で削除されました。代わりに `map(Function)` を使います。値を書き戻す方法を知るバリデータは `map(f, g)` で構築します。「[オブジェクトの書き戻し](writing-back.md)」を参照してください。

## コレクションへのリフト

`liftList`、`liftSet`、`liftCollection(factory)`、`liftMap`、`liftOptional` は、コンテナの各要素または値にバリデータを適用します。要素の違反はインデックスまたはキーの下に配置されます。

```java
Arguments1Validator<Iterable<? extends String>, List<Email>> emails = emailValidator.liftList();
Validated<List<Email>> validated = emails.validate(List.of("foo@example.com", "bar@example.com"));

Arguments1Validator<Iterable<? extends String>, Set<Email>> emailSet = emailValidator.liftSet();
Arguments1Validator<Iterable<? extends String>, List<Email>> emailList = emailValidator
        .liftCollection(ArrayList::new);
Arguments1Validator<Optional<? extends String>, Optional<Email>> emailOptional = emailValidator
        .liftOptional();
```

0.18 ではリフトしたバリデータはサブタイプのコンテナ `Iterable<? extends String>` を取るため、`Object` のバリデータをリストにリフトすると `List<String>` を検証できます。

リフトした要素の違反は、要素のバリデータの名前より前のインデックスの下に配置されます。

```java
Yavi._string("tag", c -> c.notBlank())
        .liftList()
        .validate(List.of("java", ""));
// [1].tag, was tag[1] in 0.17
```

要素ではなくコレクションに名前を付けるには、`prefixed(String)` を使います。

```java
Yavi._string(c -> c.notBlank())
        .liftList().prefixed("tag")
        .validate(List.of("java", ""));
// tag[1]

addressValidator.applicative().liftList().prefixed("addresses").validate(addresses);
// addresses[1].street
```

`indexed(int)` は削除されました。`ConstraintViolation` または `Validated` の `prefixed(int)` は、インデックスを名前の前に置きます。

```java
validated.prefixed(1); // [1].tag
```

`liftList(separator)` は `1,2,3` のような 1 つのテキストからリストを読むこともできます。

```java
ValueValidator<Iterable<? extends Integer>, List<Integer>> ids = Yavi._integer(c -> c.positive())
        .liftList(Pattern.compile(","));
```

## 対象より先にソースを検証する

`compose` は別の型の値を先に変換して検証します。これにより、リクエストや Map のバリデータが、読み取った値からドメインオブジェクトを作成できます。

```java
Arguments1Validator<HttpServletRequest, Name> requestNameValidator = nameValidator
        .compose(req -> req.getParameter("name"));
Arguments1Validator<HttpServletRequest, Email> requestEmailValidator = emailValidator
        .compose(req -> req.getParameter("email"));
Arguments1Validator<HttpServletRequest, Integer> requestAgeValidator = ageValidator
        .compose(req -> Integer.valueOf(req.getParameter("age")));

Arguments1Validator<HttpServletRequest, Person> personValidator = ArgumentsValidators
        .combine(requestNameValidator, requestEmailValidator, requestAgeValidator)
        .apply(Person::new);

Validated<Person> validated = personValidator.validate(request);
```

型付き引数のバリデータも同じように変換できます。`Arguments.of` は対象のバリデータが期待する引数を作成します。

```java
Arguments1Validator<HttpServletRequest, Person> personValidator = argumentsValidator
        .compose(req -> Arguments.of(req.getParameter("name"), req.getParameter("email"),
                Integer.valueOf(req.getParameter("age"))));
```

ソースは同じ組み合わせの規則で検証されます。すべての違反が蓄積され、ソースが有効な場合にだけ対象が作成されます。
