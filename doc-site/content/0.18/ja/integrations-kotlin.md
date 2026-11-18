# Kotlin

Kotlin は Java と同じ公開 API を使います。スキーマは `Yavi.schema()` で構築し、JSpecify の null 性アノテーションは Kotlin コンパイラが読むため、`null` になりうる値は nullable な Kotlin 型でなければなりません。`validator<T>` DSL は従来の `Validator` を構築します。

## Kotlin からスキーマ API を使う

制約の関数は `it` で書き、コンストラクタはメソッド参照で呼びます。

```kotlin
data class Member(val name: String, val nickname: String?)

val memberValidator = Yavi.schema()
    ._string("name") { it.notBlank().lessThanOrEqual(50) }
    ._string("nickname") { it.lessThanOrEqual(20) }.nullable()
    .apply(::Member)
```

`.apply(::Member)` は `ArgumentsNValidator` を返し、`.fromMap(::Member)` は Map を読み取ります。パラメータの Kotlin の型は、Java の場合と同じく、バリデータが受け付けるものをコンパイラに伝えます。

単一値のバリデータは、`am.ik.yavi.validator` のトップレベル関数で構築できます。`Class` リテラルの代わりに、enum またはオブジェクトの型を型引数で与えます。関数は名前でインポートし、`am.ik.yavi.builder` の同名の関数もインポートする場合は一方に別名を付けます。

```kotlin
import am.ik.yavi.validator._enum
import am.ik.yavi.validator._object

val role = _enum<Role>("role") { it.oneOf(Role.ADMIN) }
val id = _object<UUID>("id")
```

## Kotlin DSL

`am.ik.yavi.builder` の `validator<T>` は、すでに存在するオブジェクトのための `Validator<T>` を構築します。DSL は Kotlin リフレクションでプロパティ名を取得するため、実行時クラスパスに `kotlin-reflect` が必要です。

```kotlin
val validator = validator<User> {
    User::name {
        notNull()
        lessThanOrEqual(20)
    }
    User::email {
        notNull()
        email()
    }
}
```

プロパティには別の名前を与えられ、ネストしたオブジェクトとコレクションは `nest` と `forEach` で検証します。

```kotlin
val validator = validator<Order> {
    (Order::customer)("Customer") {
        notNull()
    }
    Order::items forEach {
        OrderItem::name {
            notBlank()
        }
    }
    Order::customer nest {
        Customer::name {
            notBlank()
        }
    }
}
```

* `nestIfPresent` と `forEachIfPresent` は、nullable なネストしたオブジェクト、コレクション、Map、配列を存在する場合にだけ検証します。
* 時刻型と enum のプロパティには専用のオーバーロードがあります。たとえば `past()` や `oneOf(Status.ACTIVE)` です。
* `onCondition` と `onGroup` は条件付きおよびグループ固有の制約を追加し、`unwrap()` は基になる `ValidatorBuilder` を返します。
* DSL が構築するのは[オブジェクトの検証](using-yavi.md)の従来の `Validator` であり、スキーマ API ではありません。`Validator` は存在するオブジェクトを検証し、スキーマはオブジェクトを生成します。

## null 性

YAVI は JSpecify でアノテーションされています。`nullable()` のプロパティは `@Nullable` であり、Kotlin はこれを `String?` として読むため、`apply` や `fromMap` に渡す関数はそのパラメータを nullable と宣言しなければなりません。

```kotlin
data class Member(val name: String, val nickname: String?)

val validator = Yavi.schema()
    ._string("name") { it.notBlank() }
    ._string("nickname") { it.lessThanOrEqual(20) }.nullable()
    .apply(::Member)
```

Kotlin DSL で検証される型は nullable ではありません。`validator<T>`、`nest`、`forEach` は `T : Any` を宣言しています。Kotlin 2.1 以降は JSpecify アノテーションとの不一致をエラーとして報告します。すべてのパラメータと戻り値を検査するには `-Xjspecify-annotations=strict` を指定してコンパイルします。

## コルーチン

`am.ik.yavi.fn` の拡張は `Validation` の結果の扱いを suspend します。`awaitFold`、`awaitBimap`、`awaitMap`、`awaitMapErrors`、`awaitMapError` は `fold`、`bimap`、`map`、`mapErrors`、`mapError` に対応し、`kotlinx-coroutines-core` を必要とします。

```kotlin
import am.ik.yavi.fn.awaitFold

suspend fun createUser(validated: Validated<User>): HttpStatus = validated.awaitFold(
    errorsMapper = { violations -> HttpStatus.BAD_REQUEST },
    mapper = { user -> HttpStatus.OK },
)
```
