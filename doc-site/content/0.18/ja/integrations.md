# 統合の概要

YAVI は JSpecify 以外の実行時依存を持たず、フレームワークが必要とするインターフェースはライブラリに含まれています。バリデータは関数です。スキーマが入力を読み取り、結果は `Validated` またはその `ConstraintViolations` です。以下の各統合は、その結果を扱う小さなアダプターです。

| 統合 | 提供するもの |
| --- | --- |
| [Spring](integrations-spring.md) | `BindingResult`、`MessageSource`、フォームの再表示、IoC コンテナでのバリデータ管理、WebFlux |
| [Jackson](integrations-jackson.md) | `JsonNodeReader`、重複キーと数値の忠実性のためのマッパー設定、違反のシリアライズ |
| [Kotlin](integrations-kotlin.md) | `validator<T>` DSL、JSpecify による null 性、コルーチン拡張 |
| [NullAway](integrations-nullaway.md) | JSpecify アノテーション、よくある報告と修正、制限 |

入力側は [JSON ツリー](input-json.md)と[リクエストパラメータとフォーム](input-parameters.md)、出力側は[オブジェクトの書き戻し](writing-back.md)で説明します。
