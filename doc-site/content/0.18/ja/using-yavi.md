# オブジェクトの検証

`am.ik.yavi.builder.ValidatorBuilder` は、既に存在するオブジェクトを検証する `Validator<T>` を構築します。オブジェクトのプロパティは、`notNull()` のような制約が拒否しない限り `null` でも有効です。これは `Yavi.schema()` のプロパティとの違いです。

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        .constraint(User::name, "name", c -> c.notNull().lessThanOrEqual(20))
        .constraint(User::email, "email", c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50).email())
        .constraint(User::age, "age", c -> c.greaterThanOrEqual(0).lessThanOrEqual(200))
        .build();

ConstraintViolations violations = validator.validate(user);
```

プロパティの型が曖昧になる場合は、`constraint` ではなく型ごとのメソッドを使います。`_string`、`_integer`、`_bigDecimal` などのメソッドはプロパティを読む `Function` を取るため、レコードのアクセサ、getter、ラムダのいずれも使えます。

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        ._string(User::name, "name", c -> c.notNull().lessThanOrEqual(20))
        ._string(User::email, "email", c -> c.notNull().email())
        ._integer(User::age, "age", c -> c.greaterThanOrEqual(0))
        .build();
```

バリデータはスレッドセーフで、繰り返し使えます。既定ではすべての違反を蓄積します。最初の違反で止めるには「[フェイルファストモード](#fail-fast-mode)」を参照してください。

## ネストしたオブジェクト

`nest` はネストしたオブジェクトにバリデータを適用します。ネストしたバリデータは渡すことも、その場で定義することもできます。

```java
Validator<Address> addressValidator = ValidatorBuilder.<Address>of()
        .nest(Address::country, "country", countryValidator)
        .nest(Address::city, "city",
                b -> b._string(City::name, "name", c -> c.notBlank().lessThanOrEqual(100)))
        .build();
```

ネストしたオブジェクトの違反は、その名前の下に配置されます (`country.name`)。`nestIfPresent` はネストしたオブジェクトが `null` でない場合にだけ検証します。

```java
Validator<Order> validator = ValidatorBuilder.<Order>of()
        .nestIfPresent(Order::customer, "customer", customerValidator)
        .build();
```

## コレクション、Map、配列の要素

`forEach` はコレクション、Map の値、配列の各要素にバリデータを適用します。要素の名前は違反名のインデックスになります (`histories[0].revision`)。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories>of()
        .forEach(Histories::value, "histories", historyValidator)
        .build();
```

ネストしたオブジェクトと同様に、その場で定義するバリデータと `forEachIfPresent` も使えます。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories>of()
        .forEach(Histories::value, "histories",
                b -> b._integer(History::revision, "revision", c -> c.greaterThanOrEqual(1)))
        .build();
```

既定で検証できるのは Map の値だけです。Map のキーを検証するには、キーをコレクションとして読みます。

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap>of()
        .forEach(codeMap -> codeMap.asMap().keySet(), "codeMap",
                b -> b._string(k -> k, "value", c -> c.notEmpty()))
        .build();
```

`.` 以外のメッセージキー区切り文字を持つ `ValidatorBuilder` は、`forEach` の下の名前でもそれを使います。

```java
new ValidatorBuilder<Order>("/")
        .forEach(Order::addresses, "addresses", b -> b._string(Address::street, "street", c -> c.notBlank()))
        .build();
// addresses[0]/street
```

次のプログラムはバリデータをネストし、要素のバリデータを適用してから、違反名を出力します。

```java runnable
import java.util.List;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

public class Main {

    record History(int revision) {
    }

    record Histories(List<History> value) {
    }

    static final Validator<History> historyValidator = ValidatorBuilder.<History>of()
            .constraint(History::revision, "revision", c -> c.greaterThanOrEqual(1))
            .build();

    static final Validator<Histories> validator = ValidatorBuilder.<Histories>of()
            .forEach(Histories::value, "histories", historyValidator)
            .build();

    public static void main(String[] args) {
        ConstraintViolations violations = validator
            .validate(new Histories(List.of(new History(0), new History(1), new History(-1))));
        System.out.println("violations: " + violations.size());
        violations.forEach(v -> System.out.println("- " + v.name() + ": " + v.message()));
    }
}
```
<!-- run: expected output
violations: 2
- histories[0].revision: "histories[0].revision" must be greater than or equal to 1
- histories[2].revision: "histories[2].revision" must be greater than or equal to 1
-->

## 条件付きの制約

`constraintOnCondition` は条件が成り立つ場合にだけバリデータを適用します。条件は対象オブジェクトと検証呼び出しの `ConstraintContext` を受け取るため、リクエストの属性を読めます。

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        .constraintOnCondition((user, context) -> "IT".equals(context.attribute("country").value()),
                b -> b._string(User::taxId, "taxId", c -> c.notBlank()))
        .build();

ConstraintContext context = ConstraintContext.from(Map.of("country", "IT"));
ConstraintViolations violations = validator.validate(user, context);
```

`ConstraintCondition.hasAttribute` と `ConstraintCondition.hasAttributeWithValue` は、よくある属性チェックの省略形です。

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        .constraintOnCondition(ConstraintCondition.hasAttributeWithValue("country", "IT"),
                b -> b._string(User::taxId, "taxId", c -> c.notBlank()))
        .build();
```

## 制約グループ

`ConstraintGroup` は名前を持つ `ConstraintContext` です。`constraintOnGroup` で追加した制約は、そのグループを検証した場合にだけ適用されます。

```java
enum Group implements ConstraintGroup {

    CREATE, UPDATE

}

Validator<User> validator = ValidatorBuilder.<User>of()
        .constraintOnGroup(Group.CREATE, b -> b._integer(User::id, "id", c -> c.isNull()))
        .constraintOnGroup(Group.UPDATE, b -> b._integer(User::id, "id", c -> c.notNull()))
        .build();

ConstraintViolations violations = validator.validate(user, Group.CREATE);
```

グループは enum を使わずに `ConstraintGroup.of("CREATE")` でも作成できます。グループを持たない制約はすべてのグループで適用され、`validate` にグループを渡さない場合は `ConstraintGroup.DEFAULT` が使われます。グループは名前で一致します。

## オブジェクト全体の規則

`constraintOnTarget` は複数のプロパティにまたがる規則を追加します。違反は指定した名前の下に報告されます。

```java
Validator<Range> validator = ValidatorBuilder.<Range>of()
        ._integer(Range::from, "from", c -> c.greaterThan(0))
        ._integer(Range::to, "to", c -> c.greaterThan(0))
        .constraintOnTarget(range -> range.to() > range.from(), "to", "to.isGreaterThanFrom",
                "\"to\" must be greater than \"from\"")
        .build();
```

規則はプロパティの後に評価されます。`CustomConstraint<T>` で述語とメッセージを渡すこともできます。「[カスタム制約](custom-constraints.md)」を参照してください。

## メッセージの上書き

`message` は、その直前の制約のメッセージを置き換えます。`{0}` はプロパティ名、その他のプレースホルダは制約の引数、最後の引数は制約に違反した値です。

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        ._string(User::name, "name", c -> c.notNull().message("{0} is required!")
                .greaterThanOrEqual(1).message("{0} is too small!")
                .lessThanOrEqual(20).message("{0} is too large!"))
        .build();
```

`message(String)` に渡したテキストがメッセージになります。`MessageFormatter` はそれをリソースバンドルのメッセージで置き換えず、引数を埋めるだけです。違反はメッセージキーを保持します。独自のメッセージをバンドルでローカライズするには、キーを付けます。

```java
c -> c.notBlank().message(ViolationMessage.of("user.name.notBlank", "Enter your name"))
```

他の制約のメッセージをフォーマットするには、ビルダーに `MessageFormatter` を設定します。

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        .messageFormatter(new MessageSourceMessageFormatter(messageSource::getMessage))
        ._string(User::name, "name", c -> c.notBlank())
        .build();
```

## フェイルファストモード

`failFast(true)` は最初の違反で検証から戻ります。モードはネストしたバリデータ、要素のバリデータ、条件付きバリデータにも引き継がれます。

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        ._string(User::name, "name", c -> c.notNull().lessThanOrEqual(20))
        ._string(User::email, "email", c -> c.notNull().email())
        .failFast(true)
        .build();

Validator<User> failFastValidator = validator.failFast(true);
boolean failFast = failFastValidator.isFailFast();
```

## 制約名の衝突

`conflictStrategy(ConflictStrategy)` は、同じ制約名が複数回追加された場合の動作を決めます。既定の `ConflictStrategy.NOOP` は新しい制約を追加します。`ConflictStrategy.OVERRIDE` は既存の制約を削除します。

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        .conflictStrategy(ConflictStrategy.OVERRIDE)
        ._string(User::name, "name", c -> c.notNull())
        ._string(User::name, "name", c -> c.notBlank()) // replaces the previous "name" constraint
        .build();
```

独自の戦略は既存の述語のリストを変更できます。新しい述語はその後に追加されます。

## 違反の扱い

`validate` は `ConstraintViolations` を返します。これは `List<ConstraintViolation>` であり、オブジェクトが有効な場合は空です。各違反を読むには繰り返します。

```java
for (ConstraintViolation violation : violations) {
    String name = violation.name();          // tags[1]
    String key = violation.messageKey();     // charSequence.notBlank
    String message = violation.message();
    Object[] args = violation.args();
    Object value = violation.violatedValue();
    String pointer = violation.pointer();    // /tags/1
}
```

値そのものを検証した場合、`ConstraintViolation` に名前はありません。そのメッセージは「The value」となります。

`messagesByName` と `messagesByPointer` は、フォームや API レスポンスが必要とする形でメッセージを返します。`details` は JSON ライブラリがそのままシリアライズできるレコードを返します。

```java
Map<String, List<String>> byName = violations.messagesByName();
Map<String, List<String>> byPointer = violations.messagesByPointer();

List<ViolationDetail> details = violations.details();
// [{"name": "items[1].quantity", "pointer": "/items/1/quantity",
//   "messageKey": "numeric.lessThanOrEqual", "message": "...", "params": {"max": 99}}]
```

`ViolationDetail` はレコードの sealed インタフェースです。アクセサは `name()`、`pointer()`、`messageKey()`、`message()`、`params()` で、位置引数は `ConstraintViolation#args()` にあります。制約に違反した値は詳細に含まれないため、詳細はそのまま返せます。`details()` は変更できないリストを返します。
