# はじめに

YAVI は日本語の俗語「ヤバイ（YABAI）」と同じ響きで、文脈によって「すごい」とも「ひどい」ともいいます（英語の "Crazy" に近い）。
YAVI を使えば、その意味が前者であることがすぐにわかります。

設計方針は次のとおりです。

* リフレクションを使いません
* （実行時の）アノテーションを使いません
* Java Beans に限りません
* 依存関係はゼロです

[Bean Validation](https://beanvalidation.org/) が好みでないなら、YAVI は優れた代替になります。

YAVI には次の機能があります。

* 型安全な制約。サポートされていない制約を誤った型に適用できません
* 流れるように書ける直感的な API
* 任意のオブジェクトへの制約。Java Beans、[Records](https://openjdk.java.net/jeps/395)、[Protocol Buffers](https://developers.google.com/protocol-buffers)、[Immutables](https://immutables.github.io/) など
* 日時や enum の制約を含む、多数の強力な組み込み制約
* 独自の制約を簡単に定義できます
* 制約グループとコンテキストごとの検証、条件付き検証、競合時の戦略
* オブジェクトを生成する前の引数の検証と、不正なオブジェクトを生成できなくする「Always Valid モデル」
* コルーチン拡張を含む Kotlin 連携
* 関数型プログラミングの考え方を取り入れた、検証結果とバリデータを組み合わせる API

Bean Validation からの移行については、[ガイド](https://github.com/making/yavi/blob/develop/docs/FromBeanValidationToYAVI.md) を参照してください。
