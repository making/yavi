package am.ik.yavi.core;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class ConstraintContextTest {

	@Test
	void fromMap() {
		final ConstraintContext context = ConstraintContext
				.from(singletonMap("country", "IT"));
		final Object country = context.attribute("country").value();
		assertThat(country).isEqualTo("IT");
		final String typedCountry = context.attribute("country").value(String.class);
		assertThat(typedCountry).isEqualTo("IT");
	}

	@Test
	void fromFunction() {
		final Map<String, Object> headers = singletonMap("country", "IT");
		final ConstraintContext context = ConstraintContext.from(headers::get);
		final Object country = context.attribute("country").value();
		assertThat(country).isEqualTo("IT");
		final String typedCountry = context.attribute("country").value(String.class);
		assertThat(typedCountry).isEqualTo("IT");
	}
}