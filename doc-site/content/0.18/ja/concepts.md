# 基本概念

このページでは、すべてのスキーマが使う要素を説明します。入力がオブジェクトになる流れ、スキーマとバリデータの関係、結果と違反の表現、型システムが検査する範囲です。

## 入力と検証済みオブジェクト

入力の読み取りは次の流れです。

1. 入力は呼び出し元または `InputReader` によってパースされます。`Map<String, Object>`、JSON ノード、リクエストパラメータ、型付き引数のいずれかです。
2. 各プロパティは名前で読み取られ、型がプロパティメソッドと照合されます。
3. プロパティの制約が評価されます。
4. すべてのプロパティが有効なとき、検証済みの値が `fromMap(f)` または `apply(f)` に渡された関数に渡され、オブジェクトが生成されます。

どの段階でも失敗すると、そのプロパティの処理は終了します。残りのプロパティは引き続き検査され、すべての違反が収集されます。違反が一つでもある場合、オブジェクトを生成する関数は呼ばれません。

入れ子のオブジェクト、リスト、Map も同じ流れをそれぞれの階層で繰り返します。違反は `address.street`、`roles[0]`、`scores.math` のようにプロパティ名の下に位置づけられます。

## スキーマと型付きバリデータ

`Yavi.schema()` はプロパティを追加するメソッドを持つビルダーを返します。途中のスキーマはバリデータではありません。`fromMap(f)` または `apply(f)` で終えるまで、何も検証できません。ビルダーは不変です。各呼び出しは新しい段階を返し、スキーマが再利用されたり変更されたりすることはありません。`fromMap(f)` は `ObjectInputValidator<X>` で、`apply(f)` は `ArgumentsNValidator<..., X>` で終えます。

どちらも `ValueValidator` です。`ValueValidator<I, X>` は入力型 `I` と生成する型 `X` を知っています。そのため同じ記述に `readingWith(reader)` や `untyped()` で別の入力型を与えられ、別のスキーマのプロパティとして入れ子にもできます。

`Yavi._string("email", c -> c.email())` のような単一値のバリデータは `ValueValidator<String, String>` です。一つの値を検証し、単独でも、リストの要素としても、プロパティとしても使えます。

## Validated と違反

`Validated<T>` は sealed インタフェースです。

* `Validated.Valid<T>` は生成されたオブジェクトを `value()` に持ちます。
* `Validated.Invalid<T>` は `ConstraintViolations` を `errors()` に持ちます。

`validate` は Zod 風の API における `safeParse` の役割を果たします。名前は 0.17 から維持されています。

`ConstraintViolations` は `ConstraintViolation` のリストです。各違反は次を公開します。

* `name()`: `address.street` や `tags[1]` のような平坦な名前。
* `path()`: `PathSegment.Property` と `PathSegment.Index` による構造化されたパス。
* `pointer()`: 同じ位置を表す JSON Pointer（RFC 6901）。たとえば `/address/street`。
* `messageKey()` と `message()`: メッセージキーと整形されたメッセージ。
* `args()` と `violatedValue()`: 位置引数と、失敗した値。

`messagesByName()` と `messagesByPointer()` はフォームや API 応答向けにメッセージをまとめ、`details()` はそのままシリアライズできる `ViolationDetail` レコードを返します。違反した値は detail には含まれません。

## 必須の値と null を許す値

スキーマのプロパティは、修飾がない限り必須です。必須のプロパティでは、キーの不在と明示的な `null` はどちらも違反であり、メッセージキーが異なります（`object.required` と `object.notNull`）。

修飾は検証済みの値の型を変えます。

* `nullable()` は `@Nullable X` を生成します。
* `optional()` は `Optional<X>` を生成します。
* `defaultValue(v)` と `recover(v)` は `X` を生成し、`null` で失敗しません。

コンパイラと nullness チェッカーはこれらの型を認識します。`.nullable().optional()` のように効果のない修飾はコンパイルできません。例外は `presence()` です。これは `Absent`、`PresentNull`、`Present` を区別する `Presence<X>` を生成します。

`ValidatorBuilder` は別のモデルに従います。プロパティが `null` でも、制約が拒否しない限り有効です。

## JSpecify による nullness

YAVI は [JSpecify](https://jspecify.dev) でアノテーションされています。アノテーションは `provided` であるため、YAVI は実行時の依存関係を追加しません。`@Nullable` の値を使うアプリケーションは `org.jspecify:jspecify` を自身で追加します。

NullAway や Kotlin コンパイラを使うと、API 全体でアノテーションが検査されます。

* `fromMap` や `apply` に渡す関数の引数は、対応するプロパティが `nullable()` でない限り非 null です。
* `ValueValidator<String, @Nullable String>` として宣言した値は、null を許すものとして扱わなければなりません。
* `nullable()` のプロパティを `@Nullable` でない引数に渡す `fromMap(User::new)` はコンパイルできません。

検査とその限界は [NullAway](integrations-nullaway.md) を参照してください。

## スレッドセーフ性

スキーマとバリデータは不変です。一度構築したバリデータは `static final` フィールドに保持でき、任意の数のスレッドから同時に使えます。違反と `Validated` の結果は呼び出しごとに生成されます。

`fromMap`、`apply`、制約に渡す関数は検証中に呼ばれます。バリデータを共有する場合、これらの関数自体もスレッドセーフでなければなりません。

## コンパイル時と実行時の検査

コンパイラは次を検査します。

* 値の型に存在する制約かどうか
* 修飾の連鎖に意味があるかどうか。段階によって利用できるメソッドが変わるためです。
* `fromMap` や `apply` に渡す関数が、スキーマの生成する型を受け取れるかどうか。nullness チェッカーが有効な場合は null の扱いも含みます。
* 可逆スキーマが、値を書き戻せるバリデータを受け取っているかどうか。

実行時には次を検査します。

* 各入力値が宣言された型を表しているかどうか
* 各制約が成り立つかどうか
* 必須のキーが存在するかどうか

リフレクションや、実行時に保持されるアノテーションに依存する検査はありません。
