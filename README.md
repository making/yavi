## YAVI (Yet Another ValIdation)
YAVI (pronounced jɑ-vάɪ) 
is a lambda based type safe validation for Java. 
 

### Usage

Add the following dependency in your `pom.xml`

```xml
<dependency>
    <groupId>am.ik.yavi</groupId>
    <artifactId>yavi</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Example


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
violations.isValid();
violations.forEach(x -> System.out.println(x.message()));
```
