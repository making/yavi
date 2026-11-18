# アノテーションプロセッサ

アノテーションプロセッサは、`am.ik.yavi.meta.ConstraintTarget` または `am.ik.yavi.meta.ConstraintArguments` を付けた型のメンバーのメタクラスを生成します。メタ定数はプロパティに名前を付け、`Function` で読み取るため、バリデータは名前の文字列リテラルを使いません。

## アノテーションプロセッサの有効化

プロセッサは `yavi` アーティファクトに含まれています。JDK 21 以前では `mvn clean compile` がコンパイルクラスパス上で自動的に見つけます。JDK 23 以降はクラスパス上のアノテーションプロセッサを暗黙に実行しないため、明示的に宣言します。

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>am.ik.yavi</groupId>
                <artifactId>yavi</artifactId>
                <version>0.18.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

`@ConstraintTarget` と `@ConstraintArguments` の保持はソースのみであるため、実行時には存在しません。

## 対象へのアノテーション

`@ConstraintTarget` は getter またはコンストラクタ引数に付けます。プロセッサは型 `UserForm` に対して `_UserFormMeta` を生成し、アノテーションを付けたメンバーごとに定数を作成します。

```java
public class UserForm {

    private String name;

    private Integer age;

    @ConstraintTarget
    public String getName() {
        return name;
    }

    @ConstraintTarget
    public Integer getAge() {
        return age;
    }

}
```

定数は `ValidatorBuilder#constraint` に渡します。

```java
Validator<UserForm> validator = ValidatorBuilder.<UserForm>of()
        .constraint(_UserFormMeta.NAME, c -> c.notBlank())
        .constraint(_UserFormMeta.AGE, c -> c.greaterThan(0))
        .build();
```

コンストラクタ引数では、`@ConstraintTarget(getter = false)` は引数と同じ名前のメソッドを使います。レコードに適しています。`@ConstraintTarget(field = true)` はフィールドを直接読み取ります。フィールドは、同じパッケージにある生成クラスからアクセスできる必要があります。

```java
public record UserForm(@ConstraintTarget(getter = false) String name,
        @ConstraintTarget(getter = false) Integer age) {
}
```

## 引数へのアノテーション

`@ConstraintArguments` はコンストラクタまたはメソッドに付けます。`User` のコンストラクタは `_UserArgumentsMeta` を生成し、`UserService` のメソッド `createUser` は、レシーバを最初の引数とする `_UserServiceCreateUserArgumentsMeta` を生成します。

```java
public class User {

    @ConstraintArguments
    public User(String name, String email, int age) {
        // ...
    }

}
```

定数は `ArgumentsValidatorBuilder#of` のビルダーに渡します。

```java
Arguments3Validator<String, String, Integer, User> validator = ArgumentsValidatorBuilder
        .of(User::new)
        .builder(b -> b.constraint(_UserArgumentsMeta.NAME, c -> c.notBlank())
                .constraint(_UserArgumentsMeta.EMAIL, c -> c.notBlank().email())
                .constraint(_UserArgumentsMeta.AGE, c -> c.greaterThan(0)))
        .build();
```

メソッドの場合は、レシーバが最初の引数として検証されます。

```java
Arguments4Validator<UserService, String, String, Integer, User> validator = ArgumentsValidatorBuilder
        .of(UserService::createUser)
        .builder(b -> b.constraint(_UserServiceCreateUserArgumentsMeta.USERSERVICE, c -> c.notNull())
                .constraint(_UserServiceCreateUserArgumentsMeta.NAME, c -> c.notBlank())
                .constraint(_UserServiceCreateUserArgumentsMeta.EMAIL, c -> c.notBlank().email())
                .constraint(_UserServiceCreateUserArgumentsMeta.AGE, c -> c.greaterThan(0)))
        .build();
```

## 対応するメタ型

プロセッサは、アノテーションを付けたメンバーの宣言型のメタを生成します。`am.ik.yavi.meta` には次のメタ型があります。

* `StringConstraintMeta`
* `BooleanConstraintMeta`、`CharacterConstraintMeta`、`ByteConstraintMeta`、`ShortConstraintMeta`、`IntegerConstraintMeta`、`LongConstraintMeta`、`FloatConstraintMeta`、`DoubleConstraintMeta`
* `BigIntegerConstraintMeta`、`BigDecimalConstraintMeta`
* `ObjectConstraintMeta`
* `LocalDateConstraintMeta`、`LocalTimeConstraintMeta`、`LocalDateTimeConstraintMeta`、`OffsetDateTimeConstraintMeta`、`InstantConstraintMeta`、`YearConstraintMeta`、`YearMonthConstraintMeta`、`ZonedDateTimeConstraintMeta`

enum の対象は `ObjectConstraintMeta` を使います。

## 0.18 の変更

* 生成コードは API と同様に JSpecify (`@org.jspecify.annotations.Nullable`) でアノテーションされます。生成ソースをそのアノテーションに対してコンパイルする場合は、`org.jspecify:jspecify` を依存関係に追加してください。
* `Arguments1` から `Arguments16` はレコードで、メソッドの生成メタは上記のようにレシーバのコンテナを参照します。
* `annotationProcessorPaths` には、プロジェクトに合う YAVI アーティファクトのバージョン (ここでは `0.18.0`) を指定します。
