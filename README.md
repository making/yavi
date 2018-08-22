## YAVI (Yet Another ValIdation)
YAVI (pronounced jɑ-vάɪ) 
is a lambda based type safe validation for Java. 
 

### Why YAVI?

YAVI sounds as same as Japanese slung "YABAI" that means awesome or awful depending on the context.

The concepts are

* No more reflection!
* No more Java Beans!
* Zero dependency!

### Usage

Add the following dependency in your `pom.xml`

```xml
<dependency>
    <groupId>am.ik.yavi</groupId>
    <artifactId>yavi</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Add the following repository in order to use snapshots.

```xml
<repository>
    <id>sonatype-snapshots</id>
    <name>Sonatype Snapshots</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
### Example

#### Simple

```java
Validator<User> validator = new Validator<User>() //
            .constraint(User::getName, "name", c -> c.notNull() //
                    .greaterThanOrEquals(1) //
                    .lessThanOrEquals(20)) //
            .constraint(User::getEmail, "email", c -> c.notNull() //
                    .greaterThanOrEquals(1) //
                    .lessThanOrEquals(50) //
                    .email()) //
            .constraint(User::getAge, "age", c -> c.notNull() //
                    .greaterThanOrEquals(0) //
                    .lessThanOrEquals(200));

ConstraintViolations violations = validator.validate(user);
violations.isValid(); // true or false
violations.forEach(x -> System.out.println(x.message()));
```

### Nested

```java

```

### Required

* Java 8+

### License

Licensed under the Apache License, Version 2.0.