# 違反

`ConstraintViolation` は失敗した 1 つのルールです。`ConstraintViolations` はそのリストであり、`messagesByName()`、`messagesByPointer()`、`details()`、`apply(...)` はフォームと API レスポンスに必要な形式です。

## ConstraintViolation

違反は名前をパースしなくてもその位置を知っています。

```java
ConstraintViolation violation = violations.get(0);
violation.name();      // tags[1]
violation.path();      // [Property[name=tags], Index[index=1]]
violation.pointer();   // /tags/1, a JSON Pointer (RFC 6901)
violation.messageKey();    // charSequence.notBlank
violation.message();       // "tags[1]" must not be blank
violation.args();          // positional message arguments
violation.params();        // arguments by name
violation.violatedValue(); // the value that failed
```

`path()` は `PathSegment.Property` と `PathSegment.Index` のリストです。`label()` はプロパティに与えられたラベルです（ある場合）。ラベルによって名前、パス、ポインタは変わりません。

違反は不変です。返されるコレクションは変更できません。

## 名前、パス、ポインタ

名前は入力の構造に従います。`address.street`、`tags[0]`、`items[1].name`、`addresses[0].street` です。名前のない値の違反は空の名前を持ち、メッセージでは "The value" と表示されます。ポインタは同じパスを JSON Pointer にしたもので、`/address/street`、`/tags/0` です。`messagesByPointer()` はこれをキーに使い、RFC 9457 の problem detail の `errors[].pointer` が保持する値でもあります。

`anyOf` の代替の違反はリストに混ぜられません。結果は 1 つの `ConstraintViolation.NoAlternativeMatched` で、`alternatives()` が代替ごとに 1 つの `ConstraintViolations` を保持します。

## ViolationDetail

`details()` は、JSON ライブラリがそのままシリアライズする `ViolationDetail` レコードを返します。

```java
List<ViolationDetail> details = violations.details();
// [{"name": "items[1].quantity", "pointer": "/items/1/quantity", "messageKey": "numeric.lessThanOrEqual",
//   "message": "\"items[1].quantity\" must be less than or equal to 99", "params": {"max": 99}}]
```

`ViolationDetail` は `ViolationDetail.Simple` と、`alternatives` を持つ `ViolationDetail.NoAlternativeMatched` の sealed インターフェースです。detail に対する `switch` に `default` は不要です。

`params` は違反の引数を名前で保持します。名前は制約メソッドのパラメータ名です。制約が値から計算するものにも名前があります（`actualSize`、`duplicates`、`missing`、`notAllowed`）。独自のメッセージは `ViolationMessage.of(key, format, names...)` で `{1}` 以降の引数に名前を付けます。名前がなければ違反にパラメータはありません。フォーマットは引数を位置で参照し続けます。

| メッセージキー | `params` の名前 |
| --- | --- |
| `numeric.lessThanOrEqual` | `max` |
| `container.greaterThanOrEqual` | `min`, `actualSize` |
| `object.type` | `type` |
| `collection.unique` | `duplicates` |

違反した値は detail に含まれません。パスワードやトークンの場合があり、クライアントはすでに送信しているからです。サーバー側では `violation.args()` と `violation.violatedValue()` が保持します。

## メッセージの Map

```java
Map<String, List<String>> byName = violations.messagesByName();
// {"email": ["\"email\" must not be blank"], "items[1].name": ["..."]}

Map<String, List<String>> byPointer = violations.messagesByPointer();
// {"/email": [...], "/items/1/name": [...]}
```

ルートのメッセージは `messagesByName()` では空の名前の下にあります。どちらの Map も変更できず、違反の順にメッセージを保持します。

## 不変性と代替

`details()` は変更できないリストを返し、`messagesByName()` と `messagesByPointer()` の Map とリストも変更できません。レスポンスに必要なものはコピーに追加します。

`anyOf` の要約の代わりに特定のメッセージを示すには、違反が最も少ない代替を選びます。他は表示しません。

```java
ConstraintViolations closest = unmatched.alternatives().stream()
        .min(Comparator.comparingInt(ConstraintViolations::size))
        .orElseThrow();
```
