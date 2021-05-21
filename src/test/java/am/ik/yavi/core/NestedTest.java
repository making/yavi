/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.core;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CharSequenceConstraint;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NestedTest {

  private static Container2 container2(String value) {
    return new Container2(new Container1(value));
  }

  @Test
  public void testContainer2() {

    // Container2 -> Container1
    ConstraintViolations actual = Container2.VALIDATOR.validate(container2(""));

    assertThat(actual.get(0).name()).isEqualTo("container1.value");
  }

  private static Container3 container3(String value) {
    return new Container3(container2(value));
  }

  @Test
  public void testContainer3() {

    // Container3 -> Container2(with ConstraintCondition) -> Container1
    ConstraintViolations actual = Container3.VALIDATOR.validate(container3(""));

    assertThat(actual.get(0).name()).isEqualTo("container2.container1.value");
  }

  private static Container4 container4(String value) {
    return new Container4(container3(value));
  }

  @Test
  public void testContainer4() {

    // Container4 -> Container3 -> Container2(with ConstraintCondition) -> Container1
    ConstraintViolations actual = Container4.VALIDATOR.validate(container4(""));

    assertThat(actual.get(0).name()).isEqualTo("container3.container2.container1.value");
  }

  private static class Container4 {

    private static final Validator<Container4> VALIDATOR = ValidatorBuilder.of(Container4.class)
        .nest(Container4::getContainer3, "container3", Container3.VALIDATOR)
        .build();

    private Container3 container3;

    private Container4(Container3 container3) {
      this.container3 = container3;
    }

    public Container3 getContainer3() {
      return container3;
    }
  }

  private static class Container3 {

    private static final Validator<Container3> VALIDATOR = ValidatorBuilder.of(Container3.class)
        // OK
        //.nest(Container3::getContainer2, "container2", Container2.VALIDATOR)
        // NG
        .nest(Container3::getContainer2, "container2", Container2.VALIDATOR_WITH_CONDITION)
        .build();

    private Container2 container2;

    private Container3(Container2 container2) {
      this.container2 = container2;
    }

    public Container2 getContainer2() {
      return container2;
    }
  }

  private static class Container2 {

    private static final Validator<Container2> VALIDATOR = ValidatorBuilder.of(Container2.class)
        .nest(Container2::getContainer1, "container1", Container1.VALIDATOR)
        .build();

    private static final Validator<Container2> VALIDATOR_WITH_CONDITION = ValidatorBuilder.of(Container2.class)
        .constraintOnCondition((c, g) -> true, VALIDATOR)
        .build();

    private Container1 container1;

    private Container2(Container1 container1) {
      this.container1 = container1;
    }

    public Container1 getContainer1() {
      return container1;
    }
  }

  private static class Container1 {

    private static final Validator<Container1> VALIDATOR = ValidatorBuilder.of(Container1.class)
        .constraint(Container1::getValue, "value", CharSequenceConstraint::notBlank)
        .build();

    private String value;

    private Container1(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}