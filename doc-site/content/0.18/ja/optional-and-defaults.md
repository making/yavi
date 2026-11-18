# 省略と既定値

`Yavi.schema()` のプロパティは必須です。このページでは、キーの不在や `null` を受け入れる修飾、既定値やフォールバックを生成する修飾、キーを再び必須にする修飾を説明します。

## キーの不在と明示的な null

スキーマが Map、リクエストパラメータ、JSON ツリーを読み取る場合、キーの不在は `null` と同じように検証されます。必須のプロパティでは次のようになります。

| 入力 | 違反 |
| --- | --- |
| キーが存在しない | `object.required`: `"name" is required` |
| 値が明示的に `null` | `object.notNull`: `"name" must not be null`、または `null` を拒否する制約の違反 |

`null` を受け入れるプロパティは、キーの不在も受け入れます。キーの不在が「変更しない」、`null` が「消去する」を意味する部分更新は、両者を区別する `presence()` で書きます。

## null の受け入れ

`nullable()`、`optional()`、`defaultValue(...)` は最後に追加したプロパティに適用されます。

```java
record User(String email, @Nullable String nickname, Optional<String> bio, Integer age) {}

ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable()
        ._string("bio", c -> c.lessThanOrEqual(500)).optional()
        ._integer("age", c -> c.greaterThanOrEqual(0)).defaultValue(20)
        .fromMap(User::new);
```

* `nullable()` は `@Nullable String` を生成し、`null` を生成する唯一の修飾です。
* `optional()` は `Optional<String>` を生成します。キーの不在または `null` に対して `Optional.empty()` です。
* `defaultValue(20)` は、キーの不在または `null` に対して既定値を生成します。

次のプログラムは `name` だけを送るため、`nickname` が `null`、`bio` が `Optional.empty()`、`age` が既定値の `User` を出力し、続けて `name` を省略して `age` を負にした入力の違反を出力します。

```java runnable
import java.util.Map;
import java.util.Optional;

import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.validator.Yavi;

public class Main {

    record User(String name, String nickname, Optional<String> bio, Integer age) {
    }

    static final ObjectInputValidator<User> validator = Yavi.schema()
            ._string("name", c -> c.notBlank())
            ._string("nickname", c -> c.lessThanOrEqual(20)).nullable()
            ._string("bio", c -> c.lessThanOrEqual(500)).optional()
            ._integer("age", c -> c.greaterThanOrEqual(0)).defaultValue(20)
            .fromMap(User::new);

    public static void main(String[] args) {
        // Only "name" is sent: nickname is null, bio is empty and age takes its default.
        User user = validator.validated(Map.of("name", "yavi"));
        System.out.println(user);

        // "name" is missing and "age" is out of range.
        ConstraintViolations violations = validator.validate(Map.of("age", -1)).errors();
        for (ConstraintViolation violation : violations) {
            System.out.println(violation.name() + " " + violation.messageKey() + ": " + violation.message());
        }
    }

}
```
<!-- run: expected output
User[name=yavi, nickname=null, bio=Optional.empty, age=20]
name object.required: "name" is required
age numeric.greaterThanOrEqual: "age" must be greater than or equal to 0
-->

`fromMap` に渡す関数は、生成される型を受け入れなければなりません。NullAway や Kotlin を使う場合、`nullable()` のプロパティには `@Nullable` の引数（Kotlin では `String?`）が必要で、`optional()` には `Optional<String>` が必要です。

効果のない修飾を含む連鎖はコンパイルできません。`.nullable().optional()`、`.defaultValue(1).nullable()`、`.defaultValue(1).recover(2)` です。既に `null` を受け入れるバリデータで追加したプロパティ、たとえば `_integer(Yavi._integer("page", c -> c.positive()).defaultValue(1))` は、`null` を受け入れる二つ目の修飾を取りません。

`notNull()` は、`null` の違反に別のメッセージを与える方法として引き続き使えます。`isNull()` には `nullable()` が必要です。`._string("id", c -> c.isNull()).nullable()` は `null` だけを受け入れますが、`nullable()` がないと `null` が欠落した値として拒否されるため、どの値も有効になりません。

## Presence

`presence()` はプロパティを `Presence` として読み取ります。`Present(value)`、`PresentNull`、`Absent` のいずれかです。PATCH リクエストや JSON Merge Patch のように、キーの不在が値を変更せず、`null` が値を消去する場面のための修飾です。

```java
record ProfilePatch(Presence<String> nickname, Presence<@Nullable String> bio) {}

ObjectInputValidator<ProfilePatch> patchValidator = Yavi.schema()
        ._string("nickname", c -> c.lessThanOrEqual(50)).presence()
        ._string("bio", c -> c.lessThanOrEqual(500)).nullable().presence()
        .fromMap(ProfilePatch::new);

ProfilePatch patch = patchValidator.validated(Map.of("bio", "Hello")); // nickname is Absent
patch.nickname().ifPresent(profile::setNickname); // not called
profile.setBio(patch.bio().orElse(profile.getBio())); // null if the key is null
```

* キーの不在は `Absent` であり、検証されません。そのためプロパティが必須になることはありません。
* 存在するキーは `presence()` がない場合と同じように検証されます。明示的な `null` は、プロパティが `nullable()` でない限り違反です。`nullable()` の場合は `PresentNull` になります。
* `presence()` は最後の修飾です。`nullable()`、`defaultValue(...)`、`recover(...)` はその前に書き、存在するキーの値に適用します。
* `presence()` は、キーから読み取らない `_embedded` や `_rest` のプロパティには使えません。`optional()` の後にも使えません。`Optional` が `Presence` で包まれてしまうためです。
* `apply(f)` では引数自体が `Presence` です。単一値のバリデータでは `liftPresence()` が組み合わせです。
* リクエストパラメータでは、値が空のパラメータ（`bio=`）は存在し、`null` です。

## 既定値

`defaultValue(value)` は、キーの不在と `null` に対してその値を生成します。既定値は検証されません。それがプロパティの値になります。

```java
._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
._boolean("includeArchived").defaultValue(false)
._list("tags", Yavi._string(c -> c.notBlank())).defaultValue(List.of())
```

既定値はプロパティの型です。そのため `null` の既定値は `nullable()` で書きます。制約の値、リスト、Map、`Optional` のように決して `null` にならない値に対する `defaultValue(null)` は、スキーマの構築時に `NullPointerException` で拒否されます。

## 復旧

`recover(fallback)` はプロパティを決して失敗させません。`null` と不正な値に対してフォールバックを生成します。Map、リクエストパラメータ、JSON ツリーから読み取る場合、型を表さない値もフォールバックを生成します。`recoverWith(violations -> ...)` は違反からフォールバックを計算します。たとえばログを取るためです。`null` に対しては違反が渡されません。

```java
._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
._integer("page", c -> c.greaterThanOrEqual(0)).recover(0)
```

`optional()` や `nullable()` の後の `recover` は不正な値を復旧します。`recover(...)` の後の `optional()`、`nullable()`、`defaultValue(...)` は `null` が生成するものを変えます。`null` の復旧値は `.nullable().recover(null)` と書きます。

## キーの必須化と null の拒否

部分更新ではない契約では、二つの状態の一方だけを受け入れることがよくあります。`required()` と `rejectingNull()` は、値の型を変えずにどちらかを示します。

```java
record Account(@Nullable Instant deletedAt, Optional<String> bio, int page) {}

ObjectInputValidator<Account> accountValidator = Yavi.schema()
        ._instant("deletedAt").nullable().required()      // must be sent, as null if there is none
        ._string("bio").optional().rejectingNull()        // may be omitted, a text if it is sent
        ._integer("page").defaultValue(1).rejectingNull() // 1 only if omitted
        .fromMap(Account::new);

accountValidator.validate(Map.of());                                  // "deletedAt" is required
accountValidator.validate(Collections.singletonMap("deletedAt", null)); // deletedAt=null, bio=Optional.empty, page=1
```

これらは `optional()`、`nullable()`、`defaultValue(...)`、`recover(...)` の後に書きます。型付き引数（`apply`）には不在の状態がないため、`null` はこれらがない場合と同じように検証されます。

| プロパティ | キーの不在 | 明示的な `null` |
| --- | --- | --- |
| `_string("x")` | `object.required` | `object.notNull` |
| `.nullable()`、`.optional()`、`.defaultValue(...)`、`.recover(...)` | 受け入れる | 受け入れる |
| その後に `.required()` | `object.required` | 受け入れる |
| その後に `.rejectingNull()` | 受け入れる | `object.notNull` |
| `.presence()` | `Absent` | プロパティが検証する |

## nullness と型の変更

検証済みの値の型が、`null` を取りうるかどうかを示します。

| 修飾 | 生成される型 |
| --- | --- |
| なし | `String` |
| `nullable()` | `@Nullable String` |
| `optional()` | `Optional<String>` |
| `defaultValue(v)` | `String` |
| `recover(v)` | `String`。null を許す値に対する `recover(null)` では `@Nullable String` |
| `presence()` | `Presence<String>`。`nullable()` の後では `Presence<@Nullable String>` |

JSpecify を読む nullness チェッカー（NullAway、Kotlin）を使う場合、`nullable()` の引数を `@Nullable` でない引数に渡す `fromMap(User::new)` はコンパイルできません。これが 0.18 のモデルが加える検査です。`nullable()` の書き忘れは、実行時の `NullPointerException` ではなく、呼び出し側のコンパイルエラーになります。

## 動作のまとめ

* キーの不在は `null` と同じように検証され、欠落した値としてコンストラクタに渡されることはありません。
* `null` を受け入れる修飾が、生成するもの（`Optional.empty()`、`null`、既定値）を決めます。
* `required()` はキーの不在を拒否し、`rejectingNull()` は明示的な `null` を拒否します。どちらも型を変えません。
* `presence()` は部分更新の二つの状態のためのもので、プロパティの最後の修飾です。
* 効果のない修飾を含む連鎖はコンパイルできません。
