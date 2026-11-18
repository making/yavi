# NullAway

0.18.0 以降、検証済みの値の型は `null` になりうるかどうかを示します。`ValueValidator<String, String>` は `null` で成功せず、`ValueValidator<String, @Nullable String>` は成功しえます。JSpecify モードの NullAway はこれを検査するため、`NullPointerException` が起きる前に、`nullable()` の付け忘れをコンパイラが報告します。

## JSpecify アノテーション

YAVI は [JSpecify](https://jspecify.dev)（`@NullMarked`、`@Nullable`）でアノテーションされています。パッケージ `am.ik.yavi.jsr305` は削除されました。YAVI は `org.jspecify:jspecify` を持たないため、依存関係に追加してください。

```xml
<dependency>
    <groupId>org.jspecify</groupId>
    <artifactId>jspecify</artifactId>
    <version>1.0.1</version>
</dependency>
```

検証を通った値は宣言どおりの型で渡されるため、`apply` に渡す関数には `@Nullable` のパラメータも `Objects.requireNonNull` も必要ありません。

## NullAway の設定

[`nullability-maven-plugin`](https://github.com/making/nullability-maven-plugin) は、`maven-compiler-plugin` のコンパイラ引数を拡張して Error Prone と NullAway（`NullAway:JSpecifyMode`、`OnlyNullMarked` を含む）を設定します。

```xml
<plugin>
    <groupId>am.ik.maven</groupId>
    <artifactId>nullability-maven-plugin</artifactId>
    <version>0.5.2</version>
    <extensions>true</extensions>
    <executions>
        <execution>
            <goals>
                <goal>configure</goal>
                <goal>generate-package-info</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <!-- main or tests; tests checks the test sources too -->
        <checking>tests</checking>
    </configuration>
</plugin>
```

プラグインがコンパイラフラグの唯一の定義元です。`maven-compiler-plugin` 側で重複して指定しないでください。`generate-package-info` は各パッケージに `@NullMarked` を付けた `package-info.java` を生成するため、クラスにアノテーションを付けずに検査されます。

Kotlin ではコンパイラオプション `-Xjspecify-annotations=strict` が同じアノテーションを読みます。Kotlin コンパイラには後述の制限はありません。

## よくある報告と修正

`nullable()` のプロパティを受け取るパラメータは `@Nullable` と宣言する必要があります。

```java
record Member(String name, @Nullable String nickname) {}

Arguments2Validator<String, @Nullable String, Member> validator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable()
        .apply(Member::new);
```

```
[NullAway] parameter nickname of referenced method is @NonNull, but parameter in functional interface method am.ik.yavi.fn.Function2.apply(String,@Nullable String) is @Nullable
```

宣言するバリデータは nullable な型引数を使います。

```java
ValueValidator<String, @Nullable String> memo = Yavi._string("memo", c -> c.lessThanOrEqual(100)).nullable();
```

関数の他のパラメータに `@Nullable` は不要です。nullable な値を生成するのは `nullable()` のプロパティだけです。`isNull()` には `nullable()` が必要です。そうでなければ `null` は欠落した値だからです。

## 制限

以下はすべて誤検知であり、NullAway が正しいコードを報告しています。いずれも `null` を通し逃すものではなく、`@SuppressWarnings` なしで書ける方法があります。メッセージは JSpecify モードと `OnlyNullMarked` を指定した NullAway 0.14.2 および 0.14.3 によるものです。`-Xjspecify-annotations=strict` を指定した Kotlin コンパイラにはこれらの制限はありません。

* **nullable な値のバリデータを実装するラムダ。** NullAway は `X` を置き換えずに `Validated<@Nullable String>` と `Validated<X>` を比較します。バリデータを匿名クラスとして書くか、既存のものから構築します（`Yavi._string(c -> c.trim()).nullable()`）。
* **ジェネリックメソッドの引数としての `Objects.requireNonNull`。** NullAway は nullable なパラメータから型引数を推論します。検査した値を先に変数へ代入するか、式の中で `null` を処理します。`null` にならない値を検証するラムダは影響を受けません。
* **`null` を返す関数を伴う、対象の型のない `map`。** チェーンの途中ではマップ後の型が nullable でないとみなされます。型引数（`.<@Nullable String>map(...)`）を与えるか、バリデータを宣言した型に代入します。
* **コレクションから推論される、nullable な値のバリデータであるラムダのパラメータ。** NullAway は nullable な型引数を落とします。結果に `var` を使うか、パラメータの型を宣言するか、ループを使います。

これら以外で NullAway が正しいコードを報告する場合は、まずバリデータの型を確認してください。`nullable()` は型引数を変え、`null` を生成する唯一の修飾子です。
