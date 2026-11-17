# はじめる

> **注意**
>
> この内容は https://hibernate.org/validator/documentation/getting-started/ をもとにしています。

YAVI へようこそ。

以降では、YAVI をアプリケーションに組み込むための最初の手順を説明します。

## 前提

* [Java Runtime](http://www.oracle.com/technetwork/java/index.html) 8 以上
* [Apache Maven](http://maven.apache.org/)

## プロジェクトの設定

Maven プロジェクトで YAVI を使うには、`pom.xml` に次の依存関係を追加します。

```xml

<dependency>
    <groupId>am.ik.yavi</groupId>
    <artifactId>yavi</artifactId>
    <version>0.17.3</version>
</dependency>
```

このチュートリアルでは JUnit 5 と AssertJ を使います。
必要に応じて次の依存関係を追加します。

```xml

<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.14.4</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.27.7</version>
    <scope>test</scope>
</dependency>
```

## 制約の適用

制約を適用する方法を例で見ていきます。

`src/main/java/com/example/Car.java` を作成し、次のコードを書きます。

```java
package com.example;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;

public class Car {
    private final String manufacturer;

    private final String licensePlate;

    private final int seatCount;

    public static final Validator<Car> validator = ValidatorBuilder.<Car>of()
            .constraint(Car::getManufacturer, "manufacturer", c -> c.notNull())
            .constraint(Car::getLicensePlate, "licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
            .constraint(Car::getSeatCount, "seatCount", c -> c.greaterThanOrEqual(2))
            .build();

    public Car(String manufacturer, String licencePlate, int seatCount) {
        this.manufacturer = manufacturer;
        this.licensePlate = licencePlate;
        this.seatCount = seatCount;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public int getSeatCount() {
        return seatCount;
    }
}
```

`ValidatorBuilder#constraint` は、`Car` インスタンスの getter の戻り値に適用する制約を宣言するために使います。

* `manufacturer` は null であってはなりません
* `licensePlate` は null であってはならず、2 文字以上 14 文字以下でなければなりません
* `seatCount` は 2 以上でなければなりません

> **ヒント**
>
> [完全なソースコード](https://github.com/making/gs-yavi) は GitHub で公開しています。

## 制約の検証

これらの制約を検証するには、`Validator` インスタンスを使います。
簡単な単体テストで確かめます。

`src/test/java/com/example/CarTest.java` を作成し、次のコードを書きます。

```java
package com.example;

import am.ik.yavi.core.ConstraintViolations;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarTest {

    @Test
    void manufacturerIsNull() {
        final Car car = new Car(null, "DD-AB-123", 4);
        final ConstraintViolations violations = Car.validator.validate(car);

        assertThat(violations.isValid()).isFalse();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).message()).isEqualTo("\"manufacturer\" must not be null");
    }

    @Test
    void licensePlateTooShort() {
        final Car car = new Car("Morris", "D", 4);
        final ConstraintViolations violations = Car.validator.validate(car);

        assertThat(violations.isValid()).isFalse();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).message()).isEqualTo("The size of \"licensePlate\" must be greater than or equal to 2. The given size is 1");
    }

    @Test
    void seatCountTooLow() {
        final Car car = new Car("Morris", "DD-AB-123", 1);
        final ConstraintViolations violations = Car.validator.validate(car);

        assertThat(violations.isValid()).isFalse();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).message()).isEqualTo("\"seatCount\" must be greater than or equal to 2");
    }

    @Test
    void carIsValid() {
        final Car car = new Car("Morris", "DD-AB-123", 2);
        final ConstraintViolations violations = Car.validator.validate(car);

        assertThat(violations.isValid()).isTrue();
        assertThat(violations).hasSize(0);
    }
}
```

`Validator` インスタンスはスレッドセーフであり、何度でも再利用できます。

`validate()` メソッドは `ConstraintViolations` インスタンスを返すので、これを反復すればどの検証エラーが起きたかがわかります。
最初の 3 つのテストメソッドは、想定される制約違反を示しています。

* `manufacturerIsNull()` では `manufacturer` の `notNull()` 制約に違反します
* `licensePlateTooShort()` では `licensePlate` の `greaterThanOrEqual(int)` 制約に違反します
* `seatCountTooLow()` では `seatCount` の `greaterThanOrEqual(int)` 制約に違反します

オブジェクトが検証に成功した場合、`carIsValid()` の例のように、`validate()` は空の `ConstraintViolations` を返します。
検証が成功したかどうかは `ConstraintViolations.isValid` メソッドでも確認できます。

## 例を実行する

次のプログラムは完全なコンパイル単位です。同じ検証を定義し、不正な `Car` を検証して見つかった違反を出力します。Run を押すとブラウザでコンパイル・実行され、結果がセルの下に表示されます。

```java runnable
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

public class Main {

    record Car(String manufacturer, String licensePlate, int seatCount) {
    }

    static final Validator<Car> validator = ValidatorBuilder.<Car>of()
            .constraint(Car::manufacturer, "manufacturer", c -> c.notNull())
            .constraint(Car::licensePlate, "licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
            .constraint(Car::seatCount, "seatCount", c -> c.greaterThanOrEqual(2))
            .build();

    public static void main(String[] args) {
        ConstraintViolations violations = validator.validate(new Car(null, "D", 1));
        System.out.println("Validation failed: " + violations.size() + " constraint violations");
        for (ConstraintViolation violation : violations) {
            System.out.println("- " + violation.name() + ": " + violation.message());
        }
    }
}
```

## Always Valid モデル

上の例では `Car` インスタンスを生成してから検証するため、不正な `Car` が一時的に存在し得ます。
YAVI は、コンストラクタの実行前に引数を検証し、すべての引数が有効なときだけ結果を返すことで、不正なオブジェクトを生成できなくすることもできます。

コンストラクタを private にし、`Validated<Car>` を返す static ファクトリメソッドを公開します。

```java
package com.example;

import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.core.Validated;
import am.ik.yavi.validator.Yavi;

public final class Car {

    private static final Arguments3Validator<String, String, Integer, Car> validator = Yavi.arguments()
        ._string("manufacturer", c -> c.notNull())
        ._string("licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
        ._integer("seatCount", c -> c.greaterThanOrEqual(2))
        .apply(Car::new);

    private final String manufacturer;

    private final String licensePlate;

    private final Integer seatCount;

    public static Validated<Car> of(String manufacturer, String licensePlate, Integer seatCount) {
        return validator.validate(manufacturer, licensePlate, seatCount);
    }

    private Car(String manufacturer, String licensePlate, Integer seatCount) {
        this.manufacturer = manufacturer;
        this.licensePlate = licensePlate;
        this.seatCount = seatCount;
    }

    public String manufacturer() {
        return manufacturer;
    }

    public String licensePlate() {
        return licensePlate;
    }

    public Integer seatCount() {
        return seatCount;
    }
}
```

`Car.of(...)` は引数を検証し、結果を `Validated` に包んで返します。
コンストラクタが private なので、`Car` は検証結果からしか取得できません。
検証に失敗した場合は、`Validated#errors()` で違反を調べます。

```java
Validated<Car> validated = Car.of(null, "DD-AB-123", 4);
if (!validated.isValid()) {
    ConstraintViolations violations = validated.errors();
    // ...
}
```

record を使い、コンストラクタの中で検証したい場合は、`lazy()` を使い、コンパクトコンストラクタから `validated(...)` を呼びます。

```java
package com.example;

import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.validator.Yavi;

public record Car(String manufacturer, String licensePlate, Integer seatCount) {

    private static final Arguments3Validator<String, String, Integer, Car> validator = Yavi.arguments()
        ._string("manufacturer", c -> c.notNull())
        ._string("licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
        ._integer("seatCount", c -> c.greaterThanOrEqual(2))
        .apply(Car::new);

    public Car {
        validator.lazy().validated(manufacturer, licensePlate, seatCount);
    }
}
```

`lazy()` は、マッパーを呼ぶ代わりに `Supplier` を生成するバリデータを返すので、`Car::new` を再び呼ばずに検証が実行されます。
`lazy()` を使わない場合、コンストラクタが実行中にバリデータが `Car` を生成しようとして再帰し、`StackOverflowError` になるまで続きます。

不正な引数があると、コンパクトコンストラクタは `ConstraintViolationsException` を投げます。
その `violations()` メソッドが違反を返します。

```java
assertThatThrownBy(() -> new Car(null, "DD-AB-123", 4))
    .isInstanceOf(ConstraintViolationsException.class)
    .satisfies(e -> {
        ConstraintViolations violations = ((ConstraintViolationsException) e).violations();
        assertThat(violations).hasSize(1);
    });
```

`Yavi.arguments()` ビルダーと、引数バリデータを定義する他の方法については [引数の検証](validating-arguments.md) を参照してください。
