package am.ik.yavi.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.JavaSourcesSubject.assertThat;

class BeanValidationProcessorTest {
	@Test
	void jakartaBasic() {
		assertThat(JavaFileObjects.forResource("test/bv/Car.java"))
				.withCompilerOptions("-Ayavi.enableBeanValidationConverter=true")
				.processedWith(new BeanValidationProcessor()).compilesWithoutError().and()
				.generatesSources(
						JavaFileObjects.forResource("test/bv/Car_Validator.java"));
	}

	@Test
	void javaxBasic() {
		assertThat(JavaFileObjects.forResource("test/bv/UserForm.java"))
				.withCompilerOptions("-Ayavi.enableBeanValidationConverter=true")
				.processedWith(new BeanValidationProcessor()).compilesWithoutError().and()
				.generatesSources(
						JavaFileObjects.forResource("test/bv/UserForm_Validator.java"));
	}

	@Test
	void beanValidation2AllField() {
		assertThat(JavaFileObjects.forResource("test/bv/Bv2AllField.java"))
				.withCompilerOptions("-Ayavi.enableBeanValidationConverter=true")
				.processedWith(new BeanValidationProcessor()).compilesWithoutError().and()
				.generatesSources(JavaFileObjects
						.forResource("test/bv/Bv2AllField_Validator.java"));
	}

	@Test
	void nested() {
		assertThat(JavaFileObjects.forResource("test/bv/Address.java"))
				.withCompilerOptions("-Ayavi.enableBeanValidationConverter=true")
				.processedWith(new BeanValidationProcessor()).compilesWithoutError().and()
				.generatesSources(
						JavaFileObjects.forResource("test/bv/Address_Validator.java"),
						JavaFileObjects
								.forResource("test/bv/Address_Country_Validator.java"),
						JavaFileObjects
								.forResource("test/bv/Address_City_Validator.java"));
	}

	@Test
	void customMessage() {
		assertThat(JavaFileObjects.forResource("test/bv/User.java"))
				.withCompilerOptions("-Ayavi.enableBeanValidationConverter=true")
				.processedWith(new BeanValidationProcessor()).compilesWithoutError().and()
				.generatesSources(
						JavaFileObjects.forResource("test/bv/User_Validator.java"));
	}
}