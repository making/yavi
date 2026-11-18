# はじめる

YAVI へようこそ。このページでは依存関係を追加し、最初のスキーマを書き、入力を検証します。

> このページは https://hibernate.org/validator/documentation/getting-started/ を元にしています。

## 前提

* Java 21 以上。YAVI 0.18 は Java 21 向けにコンパイルされています。
* Apache Maven、または同等の Gradle の設定。

## プロジェクトの設定

`pom.xml` に YAVI を追加します。

```xml
<dependency>
    <groupId>am.ik.yavi</groupId>
    <artifactId>yavi</artifactId>
    <version>{{yavi_release}}</version>
</dependency>
```

## スタイルの選択

YAVI には二つの検証スタイルがあります。検証を始める時点で手元にあるものによって選びます。

| 手元にあるもの | したいこと | 使うもの |
| --- | --- | --- |
| まだオブジェクトではない入力: `Map`、パース済みの JSON、リクエストパラメータ | 検証してからオブジェクトを生成し、不正なオブジェクトを存在させない | `Yavi.schema()` と `fromMap(f)` |
| Bean Validation のように既に生成されたオブジェクト | オブジェクトを検査して違反を得る | `ValidatorBuilder` |

このページの残りでは一つ目のスタイルを説明します。従来のスタイルは最後に示します。

## スキーマの定義

`Yavi.schema()` は入力のプロパティを記述します。各プロパティメソッドは型、名前、制約を与え、`fromMap` がそのプロパティを `Map<String, Object>` から読み取ってコンストラクタを呼ぶバリデータでスキーマを終えます。

```java
package com.example;

import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.validator.Yavi;

public record Car(String manufacturer, String licensePlate, Integer seatCount) {
    public static final ObjectInputValidator<Car> validator = Yavi.schema()
            ._string("manufacturer")
            ._string("licensePlate", c -> c.greaterThanOrEqual(2).lessThanOrEqual(14))
            ._integer("seatCount", c -> c.greaterThanOrEqual(2))
            .fromMap(Car::new);
}
```

このスキーマは次のことを述べています。

* `manufacturer` は文字列でなければならない
* `licensePlate` は 2 文字以上 14 文字以下の文字列でなければならない
* `seatCount` は 2 以上の整数でなければならない

プロパティは必須です。キーの不在は `object.required` で、`null` は `object.notNull` で報告されるため、`notNull()` を書く必要はなく、`Car::new` に `null` が渡ることもありません。`null` を許すプロパティは `nullable()` または `optional()` でそのことを示します。詳しくは [省略と既定値](optional-and-defaults.md) を参照してください。

## 入力の検証

バリデータに Map を渡します。

> 注意: Zod 風の API ではこれを `parse` または `safeParse` と呼びます。YAVI は 0.17 API との後方互換性のため `validate` と `validated` を維持しています。`validate` は入力を検査して `Validated` を返すため、`safeParse` の役割を果たします。

次のプログラムは完全なものです。下の **Run** ボタンを押すと、ブラウザ内でコンパイルして実行できます。結果はプログラムの下に表示されます。初回は WebAssembly ランタイムの読み込みに数秒かかることがあり、その間はページが止まって見える場合があります。2回目以降は速くなります。

入力には `manufacturer` がないため、最初の実行では不足しているプロパティが報告されます。セルは編集できます。`Map` に `"manufacturer", "yavi"` を追加して再度 **Run** を押してください。作成された `Car`（`valid: Car[manufacturer=yavi, ...]`）が出力されます。

```java runnable
import java.util.Map;

import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validated;
import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.validator.Yavi;

public class Main {

    record Car(String manufacturer, String licensePlate, Integer seatCount) {
    }

    static final ObjectInputValidator<Car> validator = Yavi.schema()
            ._string("manufacturer")
            ._string("licensePlate", c -> c.greaterThanOrEqual(2).lessThanOrEqual(14))
            ._integer("seatCount", c -> c.greaterThanOrEqual(2))
            .fromMap(Car::new);

    public static void main(String[] args) {
        switch (validator.validate(Map.of("licensePlate", "DD-AB-123", "seatCount", 4))) {
            case Validated.Valid<Car>(Car car) -> System.out.println("valid: " + car);
            case Validated.Invalid<Car>(ConstraintViolations violations) -> {
                System.out.println("invalid");
                System.out.println(violations.size());
                System.out.println(violations.get(0).message());
            }
        }
    }
}
```
<!-- run: expected output
invalid
1
"manufacturer" is required
-->

プロパティの型を表さない値は `object.type` で報告されます。たとえば文字列 `"four"` に対して `"seatCount" must be of type integer` です。不正な入力でも例外は投げられません。

## 結果の扱い

`validate` は `Validated<Car>` を返し、`Car` または `ConstraintViolations` のどちらかを持ちます。すべての違反が蓄積されるため、不正な入力は失敗したすべての規則を一度に報告します。`Validated` は sealed インタフェースなので、レコードの分解を伴う網羅的な `switch` で両方のケースを扱え、コンパイラがどちらも忘れていないことを検査します。

```java
switch (Car.validator.validate(map)) {
    case Validated.Valid<Car>(Car car) -> save(car);
    case Validated.Invalid<Car>(ConstraintViolations violations) ->
        violations.forEach(violation -> report(violation.name(), violation.message()));
}
```

オブジェクトまたは例外を求める呼び出し元では、`validated(map)` が `Car` 自体を返し、入力が不正なときは `ConstraintViolationsException` を投げます。`violations.messagesByName()` はプロパティごとにまとめたメッセージを返し、`violations.details()` は API 応答用に JSON ライブラリがそのままシリアライズできるレコードを返します。[結果の扱い](result-handling.md) と [違反](violations.md) を参照してください。

## 同じスキーマを別の入力に使う

スキーマは `Map` に結びついていません。同じバリデータは `readingWith(reader)` で JSON ツリーを、`readingParameters()` でリクエストパラメータを読み取ります。`fromMap(f)` の代わりに `apply(f)` を使えば型付き引数を検証します。[JSON ツリー](input-json.md)、[リクエストパラメータとフォーム](input-parameters.md)、[型付き引数](input-arguments.md) を参照してください。

## 既存のオブジェクトの検証（従来 API）

オブジェクトが既に存在する場合は、`ValidatorBuilder` でそのプロパティに制約を宣言します。従来 API はインスタンスに対して `ConstraintViolations` を返します。

```java
package com.example;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

public class CarValidation {

    record Car(String manufacturer, String licensePlate, Integer seatCount) {
    }

    static final Validator<Car> validator = ValidatorBuilder.<Car>of()
            .constraint(Car::manufacturer, "manufacturer", c -> c.notNull())
            .constraint(Car::licensePlate, "licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
            .constraint(Car::seatCount, "seatCount", c -> c.greaterThanOrEqual(2))
            .build();

    public static void main(String[] args) {
        ConstraintViolations violations = validator.validate(new Car(null, "D", 1));
        System.out.println(violations.size()); // 3
        System.out.println(violations.get(0).message()); // "manufacturer" must not be null
    }
}
```

`ValidatorBuilder.constraint` は `Car` インスタンスの getter が返す値に制約を適用します。スキーマのプロパティと違い、`null` は `notNull()` などの制約が拒否しない限り有効です。[オブジェクトの検証](using-yavi.md) を参照してください。

## 次のステップ

* [基本概念](concepts.md) はこれらの呼び出しの背後にある処理の流れを説明します。
* [スキーマの定義](defining-a-schema.md) はプロパティメソッドと制約を一覧します。
* [省略と既定値](optional-and-defaults.md) はキーの不在、`null`、既定値を扱います。
* [オブジェクトとコレクション](objects-and-collections.md) は入れ子のオブジェクト、リスト、Map を扱います。
