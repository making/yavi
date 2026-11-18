# 概要

YAVI（発音は jɑ-vάɪ）は、Java で Zod のように入力の記述と検証を行うためのライブラリです。0.18 では、コードで書いたスキーマが型なしの入力を読み取り、型付きのオブジェクトを生成するか、すべての違反をその位置とともに報告します。同じ考え方は TypeScript の [Zod](https://zod.dev) や Java の [Raoh](https://github.com/raoh-project/raoh-java) にもあります。[Zod との比較](coming-from-zod.md) は両者の API を対応づけます。

[GitHub でプロジェクトを見る](https://github.com/making/yavi)

## 設計方針

* 入力優先です。入力を一度だけ記述し、型付き引数、Map、JSON ツリー、リクエストパラメータから読み取ります。オブジェクトはすべてのプロパティが有効な場合にだけ生成します。
* 構築による型安全性です。必須の値と null を許す値は Java の型が異なるため、NullAway や Kotlin コンパイラが検証済みの値の使い方を検査できます。`Validated` は sealed であり、可逆バリデータはコンパイル時に検査されます。
* リフレクションを使いません。プロパティはクラスを検査するのではなく、メソッド参照で読み取ります。
* 実行時のアノテーションを使いません。制約は Java コードとして書くため、コンパイラが検査します。
* 実行時の依存関係は必須ではありません。YAVI が使う JSpecify のアノテーションは `provided` のため推移せず、YAVI の jar だけでアプリケーションは動作します。`@Nullable` を宣言するコードのコンパイルや、NullAway などの null 検査の実行には `org.jspecify:jspecify` を追加してください。静的検査は推奨ですが必須ではありません。
* 段階的なビルダーです。効果のない修飾、たとえば `.nullable().optional()` はコンパイルできません。

既存のオブジェクトには従来の `ValidatorBuilder` も利用できますが、スキーマ優先の API が出発点として推奨されます。

## 0.18 で加わったもの

YAVI 0.18 は Java 21 向けにコンパイルされ、スキーマ優先の API を追加します。

* `Yavi.schema()` は入力を一度だけ記述し、`Map`、JSON ツリー、リクエストパラメータ、型付き引数から読み取ります。
* `fromMap(f)` はすべてのプロパティが有効な場合にだけオブジェクトを生成するため、不正なオブジェクトは存在しません。
* プロパティは、入れ子のオブジェクト、リスト、Map、ユニオン、自身を含む構造にできます。
* 検証済みのプロパティは値オブジェクトにマップでき、可逆スキーマで書き戻せます。
* 複数のプロパティにまたがる規則は `constraintOnTarget` と失敗しうるファクトリで書きます。
* `description()` と `JsonSchema` は入力を記述し、ドキュメントやクライアント生成に使えます。

既存のオブジェクトを検証する従来の `ValidatorBuilder` API も利用できます。リリース間の変更は [0.17 からの移行](migration-0.17-to-0.18.md) を参照してください。

## 対象読者

このドキュメントは、JSON のリクエストボディ、送信されたフォーム、データベースから読み取った行など、自分では制御できない入力を受け取り、型付きのオブジェクトまたはすべての違反の一覧を得たい開発者向けです。Java 21 と、動作する Maven または Gradle のビルドを前提とします。

Zod や Raoh から来た読者は [Zod との比較](coming-from-zod.md) から始めてください。Bean Validation は既に存在するオブジェクトを検査する別の問題を解決します。そのモデルは `ValidatorBuilder` で利用でき、[オブジェクトの検証](using-yavi.md) で説明します。

## ドキュメントの構成

* はじめに
  * [YAVI とは](introduction.md): 検証のモデルとライブラリの保証。
  * [はじめる](getting-started.md): 依存関係、最初のスキーマ、結果。
  * [基本概念](concepts.md): 入力、スキーマ、`Validated`、違反、null の扱い。
* スキーマ
  * [スキーマの定義](defining-a-schema.md): プロパティ、制約、名前、変換。
  * [省略と既定値](optional-and-defaults.md): キーの不在、`null`、既定値、復旧。
  * [値オブジェクトと変換](value-objects.md): 値のドメイン型へのマッピング。
  * [オブジェクトとコレクション](objects-and-collections.md): 入れ子のオブジェクト、リスト、Map、埋め込み、再帰。
  * [ユニオン](unions.md): 代替案と判別ユニオン。
  * [オブジェクト全体の規則](cross-field-rules.md): 複数のプロパティにまたがる規則と失敗しうるファクトリ。
  * [スキーマの記述](describing-a-schema.md): 記述と JSON Schema。
* 入力と結果
  * [Map と型なしの値](input-maps.md)、[JSON ツリー](input-json.md)、[リクエストパラメータとフォーム](input-parameters.md)、[型付き引数](input-arguments.md)。
  * [結果の扱い](result-handling.md)、[違反](violations.md)、[メッセージとローカライズ](messages.md)。
  * [オブジェクトの書き戻し](writing-back.md): 可逆スキーマ。
* リファレンス
  * [組み込みの制約](built-in-constraints.md) はすべての制約を一覧します。
  * [オブジェクトの検証](using-yavi.md) は従来の `ValidatorBuilder` API を扱います。
  * [Bean Validation との比較](coming-from-bean-validation.md) はアノテーションを YAVI の制約に対応づけます。
