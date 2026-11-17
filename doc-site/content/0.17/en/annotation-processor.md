# Annotation Processor

YAVI provides Annotation Processor to generates classes which defines meta classes
that implement `am.ik.yavi.meta.ConstraintMeta` for each target which is annotated with `am.ik.yavi.meta.ConstraintTarget` or `am.ik.yavi.meta.ConstraintArguments`.

## Enabling the annotation processor

The annotation processor is packaged in the `yavi` artifact.
With JDK 21 and earlier, `mvn clean compile` discovers it on the compile classpath automatically.
JDK 23 and later no longer run annotation processors found on the classpath implicitly, so declare the processor explicitly in `pom.xml`:

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

When you have a class as following, run `mvn clean compile`:

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

You'll see `target/generated-sources/annotations/com/example/_UserFormMeta.java` is automatically generated.

> **Note**
>
> `@ConstraintTarget` and `@ConstraintArguments` are compile-time annotations, so they disappear at runtime.
> This won't break YAVI's "No annotation!" concept ;)

## Annotating `@ConstraintTarget`

`@ConstraintTarget` is used to generate meta classes that can be used with `ValidatorBuilder#constraint`.
It can be annotated on methods or constructor arguments.

`Xyz` class that is annotated with `@ConstraintTarget` will generate `_XyzMeta`.

`Validator` can be built as follows:

```java
Validator<UserForm> validator = ValidatorBuilder.<UserForm> of()
    .constraint(_UserFormMeta.NAME, c -> c.notBlank())
    .constraint(_UserFormMeta.EMAIL, c -> c.notBlank().email())
    .constraint(_UserFormMeta.AGE, c -> c.greaterThan(0))
    .build();
```

No more string literal is required and it's really type-safe.

The example above is available for the following 4 ways of generation.

### `@ConstraintTarget` on getters

If you like "Java Beans" style, this way would be straightforward.

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

will generate

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

### on constructor arguments + getters

If you prefer an immutable class like Value Object, annotate `@ConstraintTarget` on constructor arguments.

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

will generate

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

This is as same as the first example.

### on constructor arguments + non-getters

You may not like "getter" style for immutable objects.
You can use filed name as method name using `getter = false`. This will be a great fit for [Records](https://openjdk.java.net/jeps/395).

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

will generate

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

### on constructor arguments + field access

You may prefer accessing fields directly rather than accessors, then use `field = true`.

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

will generate

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

## Annotating `@ConstraintArguments`

`@ConstraintArguments` is used to generate meta classes that can be used with `ArgumentsValidatorBuilder`.
It can be annotated on constructors or methods.

The constructor of `Xyz` class that is annotated with `@ConstraintArguments` will generate `_XyzArgumentsMeta`.

`doSomething` method of `Xyz` class that is annotated with `@ConstraintArguments` will generate `_XyzDoSomethingArgumentsMeta`.

### Validating Constructor Arguments

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

will generate

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

`ArgumentsNValidator` can be built as follows:

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

### Validating Method Arguments

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

will generate

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

`ArgumentsNValidator` can be built as follows:

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

## Supported meta types

The processor generates a meta for the declared type of the annotated target.
The following meta types are available in `am.ik.yavi.meta`:

* `StringConstraintMeta`
* `BooleanConstraintMeta`, `CharacterConstraintMeta`, `ByteConstraintMeta`, `ShortConstraintMeta`, `IntegerConstraintMeta`, `LongConstraintMeta`, `FloatConstraintMeta`, `DoubleConstraintMeta`
* `BigIntegerConstraintMeta`, `BigDecimalConstraintMeta`
* `ObjectConstraintMeta`
* `LocalDateConstraintMeta`, `LocalTimeConstraintMeta`, `LocalDateTimeConstraintMeta`, `OffsetDateTimeConstraintMeta`, `InstantConstraintMeta`, `YearConstraintMeta`, `YearMonthConstraintMeta`, `ZonedDateTimeConstraintMeta`

There is no dedicated meta type for enums; an enum target uses `ObjectConstraintMeta`.
