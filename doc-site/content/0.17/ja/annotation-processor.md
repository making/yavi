# アノテーションプロセッサ

YAVI はアノテーションプロセッサを提供しています。これは、`am.ik.yavi.meta.ConstraintTarget` または `am.ik.yavi.meta.ConstraintArguments` で注釈した対象ごとに、`am.ik.yavi.meta.ConstraintMeta` を実装するメタクラスを定義したクラスを生成します。

## アノテーションプロセッサの有効化

アノテーションプロセッサは `yavi` アーティファクトに含まれています。
JDK 21 以前では、`mvn clean compile` がコンパイルクラスパス上のプロセッサを自動的に検出します。
JDK 23 以降はクラスパス上で見つかったアノテーションプロセッサを暗黙には実行しないため、`pom.xml` でプロセッサを明示的に宣言します:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>am.ik.yavi</groupId>
                <artifactId>yavi</artifactId>
                <version>0.17.3</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

次のようなクラスがあるときに `mvn clean compile` を実行すると:

```java
package com.example;

import am.ik.yavi.meta.ConstraintTarget;

public class UserForm {

    private String name;

    private String email;

    private Integer age;

    @ConstraintTarget
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ConstraintTarget
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ConstraintTarget
    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
```

`target/generated-sources/annotations/com/example/_UserFormMeta.java` が自動的に生成されます。

> **注意**
>
> `@ConstraintTarget` と `@ConstraintArguments` はコンパイル時のアノテーションであるため、実行時には存在しません。
> これは YAVI の「アノテーションなし！」というコンセプトを壊すことはありません ;)

## `@ConstraintTarget` の付与

`@ConstraintTarget` は、`ValidatorBuilder#constraint` で使用できるメタクラスを生成するために使います。
メソッドまたはコンストラクタ引数に付与できます。

`@ConstraintTarget` を付与した `Xyz` クラスからは `_XyzMeta` が生成されます。

`Validator` は次のように構築できます:

```java
Validator<UserForm> validator = ValidatorBuilder.<UserForm> of()
    .constraint(_UserFormMeta.NAME, c -> c.notBlank())
    .constraint(_UserFormMeta.EMAIL, c -> c.notBlank().email())
    .constraint(_UserFormMeta.AGE, c -> c.greaterThan(0))
    .build();
```

文字列リテラルは不要になり、完全に型安全です。

上の例は、次の 4 つの生成方法で利用できます。

### getter への `@ConstraintTarget` の付与

「Java Beans」スタイルが好みなら、この方法が素直です。

```java
package com.example;

import am.ik.yavi.meta.ConstraintTarget;

public class UserForm {

    private String name;

    private String email;

    private Integer age;

    @ConstraintTarget
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ConstraintTarget
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ConstraintTarget
    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
```

このコードからは次が生成されます:

```java
package com.example;

// Generated at 2026-11-12T12:52:16.335171+09:00
public class _UserFormMeta {

    public static final am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm> NAME = new am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm>() {

        @Override
        public String name() {
            return "name";
        }

        @Override
        public java.util.function.Function<com.example.UserForm, java.lang.String> toValue() {
            return com.example.UserForm::getName;
        }
    };

    public static final am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm> EMAIL = new am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm>() {

        @Override
        public String name() {
            return "email";
        }

        @Override
        public java.util.function.Function<com.example.UserForm, java.lang.String> toValue() {
            return com.example.UserForm::getEmail;
        }
    };

    public static final am.ik.yavi.meta.IntegerConstraintMeta<com.example.UserForm> AGE = new am.ik.yavi.meta.IntegerConstraintMeta<com.example.UserForm>() {

        @Override
        public String name() {
            return "age";
        }

        @Override
        public java.util.function.Function<com.example.UserForm, java.lang.Integer> toValue() {
            return com.example.UserForm::getAge;
        }
    };
}
```

### コンストラクタ引数 + getter の場合

値オブジェクトのような不変クラスが好みなら、コンストラクタ引数に `@ConstraintTarget` を付与します。

```java
package com.example;

import am.ik.yavi.meta.ConstraintTarget;

public class UserForm {

    private final String name;

    private final String email;

    private final Integer age;

    public UserForm(@ConstraintTarget String name,
                    @ConstraintTarget String email,
                    @ConstraintTarget Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAge() {
        return age;
    }
}
```

このコードからは次が生成されます:

```java
package com.example;

// Generated at 2026-11-12T12:52:16.335171+09:00
public class _UserFormMeta {

    public static final am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm> NAME = new am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm>() {

        @Override
        public String name() {
            return "name";
        }

        @Override
        public java.util.function.Function<com.example.UserForm, java.lang.String> toValue() {
            return com.example.UserForm::getName;
        }
    };

    public static final am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm> EMAIL = new am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm>() {

        @Override
        public String name() {
            return "email";
        }

        @Override
        public java.util.function.Function<com.example.UserForm, java.lang.String> toValue() {
            return com.example.UserForm::getEmail;
        }
    };

    public static final am.ik.yavi.meta.IntegerConstraintMeta<com.example.UserForm> AGE = new am.ik.yavi.meta.IntegerConstraintMeta<com.example.UserForm>() {

        @Override
        public String name() {
            return "age";
        }

        @Override
        public java.util.function.Function<com.example.UserForm, java.lang.Integer> toValue() {
            return com.example.UserForm::getAge;
        }
    };
}
```

これは最初の例と同じです。

### コンストラクタ引数 + getter 以外の場合

不変オブジェクトで「getter」スタイルを使いたくないこともあります。
`getter = false` を使うと、フィールド名をメソッド名として使えます。[Records](https://openjdk.java.net/jeps/395) によく合います。

```java
package com.example;

import am.ik.yavi.meta.ConstraintTarget;

public class UserForm {

    private final String name;

    private final String email;

    private final Integer age;

    public UserForm(@ConstraintTarget(getter = false) String name,
                    @ConstraintTarget(getter = false) String email,
                    @ConstraintTarget(getter = false) Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }


    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public Integer age() {
        return age;
    }
}
```

このコードからは次が生成されます:

```java
package com.example;

// Generated at 2026-11-12T12:52:16.333598+09:00
public class _UserFormMeta {

    public static final am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm> NAME = new am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm>() {

        @Override
        public String name() {
            return "name";
        }

        @Override
        public java.util.function.Function<com.example.UserForm, java.lang.String> toValue() {
            return com.example.UserForm::name;
        }
    };

    public static final am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm> EMAIL = new am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm>() {

        @Override
        public String name() {
            return "email";
        }

        @Override
        public java.util.function.Function<com.example.UserForm, java.lang.String> toValue() {
            return com.example.UserForm::email;
        }
    };

    public static final am.ik.yavi.meta.IntegerConstraintMeta<com.example.UserForm> AGE = new am.ik.yavi.meta.IntegerConstraintMeta<com.example.UserForm>() {

        @Override
        public String name() {
            return "age";
        }

        @Override
        public java.util.function.Function<com.example.UserForm, java.lang.Integer> toValue() {
            return com.example.UserForm::age;
        }
    };
}
```

### コンストラクタ引数 + フィールドアクセスの場合

アクセサではなくフィールドに直接アクセスしたい場合は、`field = true` を使います。

```java
package com.example;

import am.ik.yavi.meta.ConstraintTarget;

public class UserForm {

    final String name;

    final String email;

    final Integer age;

    public UserForm(@ConstraintTarget(field = true) String name,
                    @ConstraintTarget(field = true) String email,
                    @ConstraintTarget(field = true) Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
}
```

このコードからは次が生成されます:

```java
package com.example;

// Generated at 2026-11-12T12:52:16.331344+09:00
public class _UserFormMeta {

    public static final am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm> NAME = new am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm>() {

        @Override
        public String name() {
            return "name";
        }

        @Override
        public java.util.function.Function<com.example.UserForm, java.lang.String> toValue() {
            return x  -> x.name;
        }
    };

    public static final am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm> EMAIL = new am.ik.yavi.meta.StringConstraintMeta<com.example.UserForm>() {

        @Override
        public String name() {
            return "email";
        }

        @Override
        public java.util.function.Function<com.example.UserForm, java.lang.String> toValue() {
            return x  -> x.email;
        }
    };

    public static final am.ik.yavi.meta.IntegerConstraintMeta<com.example.UserForm> AGE = new am.ik.yavi.meta.IntegerConstraintMeta<com.example.UserForm>() {

        @Override
        public String name() {
            return "age";
        }

        @Override
        public java.util.function.Function<com.example.UserForm, java.lang.Integer> toValue() {
            return x  -> x.age;
        }
    };
}
```

## `@ConstraintArguments` の付与

`@ConstraintArguments` は、`ArgumentsValidatorBuilder` で使用できるメタクラスを生成するために使います。
コンストラクタまたはメソッドに付与できます。

`@ConstraintArguments` を付与した `Xyz` クラスのコンストラクタからは `_XyzArgumentsMeta` が生成されます。

`@ConstraintArguments` を付与した `Xyz` クラスの `doSomething` メソッドからは `_XyzDoSomethingArgumentsMeta` が生成されます。

### コンストラクタ引数の検証

```java
package com.example;

import am.ik.yavi.meta.ConstraintArguments;

public class User {

    private final String name;

    private final String email;

    private final int age;

    @ConstraintArguments
    public User(String name, String email, int age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
}
```

このコードからは次が生成されます:

```java
package com.example;

// Generated at 2026-11-12T12:52:16.325765+09:00
public class _UserArgumentsMeta {

    public static final am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>> NAME = new am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>>() {

        @Override
        public String name() {
            return "name";
        }

        @Override
        public java.util.function.Function<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>, java.lang.String> toValue() {
            return am.ik.yavi.arguments.Arguments3::arg1;
        }
    };

    public static final am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>> EMAIL = new am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>>() {

        @Override
        public String name() {
            return "email";
        }

        @Override
        public java.util.function.Function<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>, java.lang.String> toValue() {
            return am.ik.yavi.arguments.Arguments3::arg2;
        }
    };

    public static final am.ik.yavi.meta.IntegerConstraintMeta<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>> AGE = new am.ik.yavi.meta.IntegerConstraintMeta<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>>() {

        @Override
        public String name() {
            return "age";
        }

        @Override
        public java.util.function.Function<am.ik.yavi.arguments.Arguments3<java.lang.String, java.lang.String, java.lang.Integer>, java.lang.Integer> toValue() {
            return am.ik.yavi.arguments.Arguments3::arg3;
        }
    };
}
```

`ArgumentsNValidator` は次のように構築できます:

```java
Arguments3Validator<String, String, Integer, User> validator = ArgumentsValidatorBuilder
    .of(User::new)
    .builder(b -> b
        .constraint(_UserArgumentsMeta.NAME, c -> c.notBlank())
        .constraint(_UserArgumentsMeta.EMAIL, c -> c.notBlank().email())
        .constraint(_UserArgumentsMeta.AGE, c -> c.greaterThan(0))
    )
    .build();
```

### メソッド引数の検証

```java
package com.example;

import am.ik.yavi.meta.ConstraintArguments;

public class UserService {

    @ConstraintArguments
    public User createUser(String name, String email, int age) {
        return new User(name, email, age);
    }
}
```

このコードからは次が生成されます:

```java
package com.example;

// Generated at 2026-11-12T12:52:16.329116+09:00
public class _UserServiceCreateUserArgumentsMeta {

    public static final am.ik.yavi.meta.ObjectConstraintMeta<am.ik.yavi.arguments.Arguments4<com.example.UserService, java.lang.String, java.lang.String, java.lang.Integer>, com.example.UserService> USERSERVICE = new am.ik.yavi.meta.ObjectConstraintMeta<am.ik.yavi.arguments.Arguments4<com.example.UserService, java.lang.String, java.lang.String, java.lang.Integer>, com.example.UserService>() {

        @Override
        public String name() {
            return "userService";
        }

        @Override
        public java.util.function.Function<am.ik.yavi.arguments.Arguments4<com.example.UserService, java.lang.String, java.lang.String, java.lang.Integer>, com.example.UserService> toValue() {
            return am.ik.yavi.arguments.Arguments4::arg1;
        }
    };

    public static final am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments4<com.example.UserService, java.lang.String, java.lang.String, java.lang.Integer>> NAME = new am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments4<com.example.UserService, java.lang.String, java.lang.String, java.lang.Integer>>() {

        @Override
        public String name() {
            return "name";
        }

        @Override
        public java.util.function.Function<am.ik.yavi.arguments.Arguments4<com.example.UserService, java.lang.String, java.lang.String, java.lang.Integer>, java.lang.String> toValue() {
            return am.ik.yavi.arguments.Arguments4::arg2;
        }
    };

    public static final am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments4<com.example.UserService, java.lang.String, java.lang.String, java.lang.Integer>> EMAIL = new am.ik.yavi.meta.StringConstraintMeta<am.ik.yavi.arguments.Arguments4<com.example.UserService, java.lang.String, java.lang.String, java.lang.Integer>>() {

        @Override
        public String name() {
            return "email";
        }

        @Override
        public java.util.function.Function<am.ik.yavi.arguments.Arguments4<com.example.UserService, java.lang.String, java.lang.String, java.lang.Integer>, java.lang.String> toValue() {
            return am.ik.yavi.arguments.Arguments4::arg3;
        }
    };

    public static final am.ik.yavi.meta.IntegerConstraintMeta<am.ik.yavi.arguments.Arguments4<com.example.UserService, java.lang.String, java.lang.String, java.lang.Integer>> AGE = new am.ik.yavi.meta.IntegerConstraintMeta<am.ik.yavi.arguments.Arguments4<com.example.UserService, java.lang.String, java.lang.String, java.lang.Integer>>() {

        @Override
        public String name() {
            return "age";
        }

        @Override
        public java.util.function.Function<am.ik.yavi.arguments.Arguments4<com.example.UserService, java.lang.String, java.lang.String, java.lang.Integer>, java.lang.Integer> toValue() {
            return am.ik.yavi.arguments.Arguments4::arg4;
        }
    };
}
```

`ArgumentsNValidator` は次のように構築できます:

```java
Arguments4Validator<UserService, String, String, Integer, User> validator = ArgumentsValidatorBuilder
    .of(UserService::createUser)
    .builder(b -> b
        .constraint(_UserServiceCreateUserArgumentsMeta.USERSERVICE, c -> c.notNull())
        .constraint(_UserServiceCreateUserArgumentsMeta.NAME, c -> c.notBlank())
        .constraint(_UserServiceCreateUserArgumentsMeta.EMAIL, c -> c.notBlank().email())
        .constraint(_UserServiceCreateUserArgumentsMeta.AGE, c -> c.greaterThan(0))
    )
    .build();
```

## サポートされるメタ型

プロセッサは、注釈した対象の宣言された型に対するメタを生成します。
`am.ik.yavi.meta` では次のメタ型を利用できます:

* `StringConstraintMeta`
* `BooleanConstraintMeta`, `CharacterConstraintMeta`, `ByteConstraintMeta`, `ShortConstraintMeta`, `IntegerConstraintMeta`, `LongConstraintMeta`, `FloatConstraintMeta`, `DoubleConstraintMeta`
* `BigIntegerConstraintMeta`, `BigDecimalConstraintMeta`
* `ObjectConstraintMeta`
* `LocalDateConstraintMeta`, `LocalTimeConstraintMeta`, `LocalDateTimeConstraintMeta`, `OffsetDateTimeConstraintMeta`, `InstantConstraintMeta`, `YearConstraintMeta`, `YearMonthConstraintMeta`, `ZonedDateTimeConstraintMeta`

enum 専用のメタ型はありません。enum の対象には `ObjectConstraintMeta` が使われます。
