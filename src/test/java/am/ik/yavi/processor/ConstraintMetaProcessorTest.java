/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.processor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.testing.compile.JavaFileObjects;

import static com.google.testing.compile.JavaSourcesSubject.assertThat;

class ConstraintMetaProcessorTest {

	@Test
	void processBean() {
		assertThat(JavaFileObjects.forResource("test/CarBean.java"))
				.processedWith(new ConstraintMetaProcessor()) //
				.compilesWithoutError().and()
				.generatesSources(JavaFileObjects.forResource("test/_CarBeanMeta.java"));
	}

	@Test
	void processImmutable() {
		assertThat(JavaFileObjects.forResource("test/Car.java"))
				.processedWith(new ConstraintMetaProcessor()) //
				.compilesWithoutError().and()
				.generatesSources(JavaFileObjects.forResource("test/_CarMeta.java"));
	}

	@Test
	void processFiled() {
		assertThat(JavaFileObjects.forResource("test/CarField.java"))
				.processedWith(new ConstraintMetaProcessor()) //
				.compilesWithoutError().and()
				.generatesSources(JavaFileObjects.forResource("test/_CarFieldMeta.java"));
	}

	@Test
	void allTypesBean() {
		assertThat(JavaFileObjects.forResource("test/AllTypesBean.java"))
				.processedWith(new ConstraintMetaProcessor()) //
				.compilesWithoutError().and().generatesSources(
						JavaFileObjects.forResource("test/_AllTypesBeanMeta.java"));
	}

	@Test
	void allTypesImmutable() {
		assertThat(JavaFileObjects.forResource("test/AllTypesImmutable.java"))
				.processedWith(new ConstraintMetaProcessor()) //
				.compilesWithoutError().and().generatesSources(
						JavaFileObjects.forResource("test/_AllTypesImmutableMeta.java"));
	}

	@Test
	void allTypesField() {
		assertThat(JavaFileObjects.forResource("test/AllTypesField.java"))
				.processedWith(new ConstraintMetaProcessor()) //
				.compilesWithoutError().and().generatesSources(
						JavaFileObjects.forResource("test/_AllTypesFieldMeta.java"));
	}

	@Test
	void processConstructorArguments() {
		assertThat(JavaFileObjects.forResource("test/Car2.java"))
				.processedWith(new ConstraintMetaProcessor()) //
				.compilesWithoutError().and().generatesSources(
						JavaFileObjects.forResource("test/_Car2ArgumentsMeta.java"));
	}

	@Test
	void processMethodArguments() {
		assertThat(JavaFileObjects.forResource("test/UserService.java"))
				.processedWith(new ConstraintMetaProcessor()) //
				.compilesWithoutError().and().generatesSources(JavaFileObjects
						.forResource("test/_UserServiceArgumentsMeta.java"));
	}

	@Test
	void testBeanLowerCamel() {
		Assertions.assertThat(ConstraintMetaProcessor.beanLowerCamel("Name"))
				.isEqualTo("name");
		Assertions.assertThat(ConstraintMetaProcessor.beanLowerCamel("NAme"))
				.isEqualTo("NAme");
		Assertions.assertThat(ConstraintMetaProcessor.beanLowerCamel("NAME"))
				.isEqualTo("NAME");
	}

	@Test
	void testBeanUpperCamel() {
		Assertions.assertThat(ConstraintMetaProcessor.beanUpperCamel("name"))
				.isEqualTo("Name");
		Assertions.assertThat(ConstraintMetaProcessor.beanUpperCamel("NAme"))
				.isEqualTo("NAme");
		Assertions.assertThat(ConstraintMetaProcessor.beanUpperCamel("NAME"))
				.isEqualTo("NAME");
	}

}