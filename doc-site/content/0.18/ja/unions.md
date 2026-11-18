# ユニオン

ユニオンは複数の形のいずれかを受け入れます。YAVI は代替案を順に試すバリデータと、判別子によって一つを選ぶバリデータを提供します。

## 代替案

`ValueValidator.anyOf(a, b)` は代替案を順に試し、最初に有効だった値を生成します。

```java
ValueValidator<Object, Object> identifier = ValueValidator.anyOf(
        Yavi._string(c -> c.uuid()).untyped(),
        Yavi._integer(c -> c.positive()).untyped());
```

入力型が異なる代替案は `untyped()` で型なしにします。代替案は出力型を共有します。sealed インタフェースなどの共通の親型をユニオンに宣言し、各代替案がそのサブタイプの一つを生成します。

ユニオンはプロパティ、要素、ルートのバリデータにできます。

```java
ObjectInputValidator<Contact> contactValidator = Yavi.schema()
        ._object("contact", ValueValidator.anyOf(emailContact, phoneContact))
        .fromMap(contact -> contact);
```

代替案に独自の名前は不要です。ユニオンは、使われた場所（`contact`）の下で違反を報告します。

## 判別ユニオン

`DiscriminatedUnionValidator` はキーの値によって代替案を選びます。sealed インタフェースのサブタイプのためのバリデータです。

```java
ValueValidator<Map<String, Object>, Shape> shapeValidator = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Yavi.schema()._integer("radius", c -> c.positive()).fromMap(Circle::new))
        .when("square", Yavi.schema()._integer("side", c -> c.positive()).fromMap(Square::new));
```

* 選ばれた代替案だけが検証されます。他の代替案のキーは必須ではありません。
* 判別子がない場合は `object.required`、想定外の値は `object.oneOf` で報告されます。
* 判別子は独自のキーから読み取られ、代替案のプロパティではありません。
* 判別ユニオンはオブジェクトのバリデータと同じように別のスキーマに埋め込め、`liftList()` や `prefixed(name)` でリストにもできます。

`DiscriminatedUnionValidator.Reversible` は、選ばれたオブジェクトを判別子とともに書き戻します。

```java
ObjectInputValidator.Reversible<Circle> circleValidator = Yavi.reversibleSchema()
        ._integer("radius", c -> c.positive())
        .fromMapReversible(Circle::new, Circle::radius);
ObjectInputValidator.Reversible<Square> squareValidator = Yavi.reversibleSchema()
        ._integer("side", c -> c.positive())
        .fromMapReversible(Square::new, Square::side);

DiscriminatedUnionValidator.Reversible<Shape> reversible =
        DiscriminatedUnionValidator.<Shape>of("type")
                .when("circle", Circle.class, circleValidator)
                .when("square", Square.class, squareValidator);

reversible.toMap(new Circle(1.5)); // {type=circle, radius=1.5}
```

## 違反の報告

`anyOf` のどの代替案も一致しない場合、結果はキー `object.anyOf` の一つの違反です。その違反は `ConstraintViolation.NoAlternativeMatched` で、`alternatives()` は代替案ごとに一つの `ConstraintViolations` を、試した順に返します。

```java
ConstraintViolations violations = contactValidator
        .validate(Map.of("contact", Map.of("kind", "email", "value", "x")))
        .errors();

violations.messagesByName();
// {contact=["contact" must satisfy at least one of the 2 alternatives]}

if (violations.get(0) instanceof ConstraintViolation.NoAlternativeMatched unmatched) {
    unmatched.alternatives().get(0).messagesByName();
    // {contact.value=["contact.value" must be a valid email address]}
    unmatched.alternatives().get(1).messagesByName();
    // {contact.kind=["contact.kind" must be equal to phone], ...}
}
```

代替案の違反は最上位のリストに混ぜられません。そこでは、どの代替案の違反かがわからないためです。具体的なメッセージを表示するクライアントは、違反が最も少ない代替案を選べます。

```java
ConstraintViolations closest = unmatched.alternatives().stream()
        .min(Comparator.comparingInt(ConstraintViolations::size))
        .orElseThrow();
```

JSON では、`violations.details()` が `ViolationDetail.NoAlternativeMatched` を返し、その `alternatives` が各代替案の detail を持ちます。それ以外の detail は `ViolationDetail.Simple` です。

判別ユニオンは、選ばれた代替案の違反をそのプロパティ名の下で直接報告します。判別子がない場合や未知の場合は、代替案のリストではなく、ユニオンに対する一つの違反になります。
