# スキーマの記述

バリデータは、期待する入力を記述します。このページでは記述ツリー、そこから生成される JSON Schema、記述できないものを示します。

## 記述

`description()` は読み取り専用の `ValueDescription` を返します。プロパティを持つオブジェクト、リスト、Map、代替案、制約を持つ値のツリーです。ツールは、バリデータの構築に使ったクラスなしでこれをたどれます。

```java
ValueDescription description = userValidator.description();

ValueDescription.Properties object = (ValueDescription.Properties) description;
for (ValueDescription.Property property : object.properties()) {
    System.out.println(property.name() + ": " + property.value());
}
```

制約はメッセージキーと引数で記述されます。たとえば `container.lessThanOrEqual` と `[14]` です。`withheld()` のプロパティはそのように印されます。記述はプロパティの名前と、入れ子の値の構造を持ちます。

記述は不変であり、入力ではなくスキーマから構築されます。Map、JSON ツリー、リクエストパラメータを読み取るスキーマの記述は一つです。プロパティが同じだからです。

## JSON Schema

`JsonSchema` は記述の利用者です。JSON ライブラリを必要としません。

```java
Map<String, Object> schema = JsonSchema.of(userValidator.description());
String json = JsonSchema.toJson(userValidator.description());
```

出力は JSON Schema draft 2020-12 に従います。

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "email": { "type": "string", "minLength": 1, "format": "email" },
    "nickname": { "type": ["string", "null"], "minLength": 1 },
    "age": { "type": ["integer", "null"], "minimum": 0, "default": 20 },
    "tags": { "type": ["array", "null"], "items": { "type": "string", "minLength": 1 }, "default": [] }
  },
  "required": ["email"]
}
```

* プロパティは、キーの不在を受け入れる場合を除いて `required` です。`presence()` のプロパティは決して `required` になりません。
* `null` を受け入れるプロパティは型に `null` を含め、`defaultValue(...)` や `recover(...)` は `default` として書かれます。
* 制約が `null` を受け入れる必須の値は、制約 `object.notNull` で記述されます。
* 未知のプロパティを拒否するバリデータは `additionalProperties: false` として、rest を読むバリデータはその値のスキーマとして書かれます。
* リストや Map の制約は `minItems`、`maxItems`、`uniqueItems`、`minProperties`、`maxProperties` として書かれます。
* `withheld()` のプロパティは `writeOnly` です。
* 判別ユニオンの代替案は `AnyOf` であり、その `discriminator` は一つを選ぶキーの名前です。

生成されるスキーマはバリデータの入力を記述します。バリデータが生成するオブジェクトのスキーマではないため、`map` の前のプロパティのキーと型を持ちます。

## 再帰的な定義と共有される定義

再帰構造は、自身を含む場所で `ValueDescription.Reference` として記述されます。`$ref` として書かれ、記述対象のバリデータ自身には `#`、それ以外は `ValueValidator.lazy(name, ...)` または `ValueValidator.recursive(name, ...)` に与えた名前の `$defs` になります。

```java
ObjectInputValidator<Comment> commentValidator = ValueValidator.recursive("comment", self -> Yavi.schema()
        ._string("body", c -> c.notBlank())
        ._list("replies", self).defaultValue(List.of())
        .fromMap(Comment::new));

JsonSchema.toJson(commentValidator.description());
// {"$defs":{"comment":{...}},"$ref":"#/$defs/comment"}
```

記述対象のバリデータ自身への参照は `#` として書かれます。`lazy` や `recursive` に与えた名前は、複数の場所から参照される定義に使われます。

## 制限

* ラムダと `ValueValidator.compose` の結果は不明です。`describedAs(description)` で記述します。
* 条件付きの制約と `constraintOnTarget` の規則は記述されません。入力の形の一部ではないか、複数のプロパティにまたがるためです。
* JSON Schema のキーワードを持たない制約は省略されます。たとえば `unique(key)` にはキーワードがなく、書かれません。`unique()` は `uniqueItems` として書かれます。
* `ValidatorBuilder` で構築した bean の `Validator` は記述されません。入力には `Yavi.schema()` を、オブジェクトには従来のバリデータを使います。
* `renaming` ビューと `including` や `excluding` のビューは、バリデータの記述の一部ではありません。それらはオブジェクトを書き出し、記述は入力を記述したままです。
* 記述はバリデータが検査する内容を反映します。検証の代わりにはなりません。スキーマを信頼するクライアントでも、サーバーではバリデータが必要です。
