package am.ik.yavi.core;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class ConstraintContextTest {

	@Test
	void fromMap() {
		final ConstraintContext context = ConstraintContext
				.from(singletonMap("country", "IT"));
		final Object country = context.attribute("country");
		assertThat(country).isEqualTo("IT");
		final String typedCountry = context.attribute("country", String.class);
		assertThat(typedCountry).isEqualTo("IT");
	}

	@Test
	void fromFunction() {
		final Map<String, Object> headers = singletonMap("country", "IT");
		final ConstraintContext context = ConstraintContext.from(headers::get);
		final Object country = context.attribute("country");
		assertThat(country).isEqualTo("IT");
		final String typedCountry = context.attribute("country", String.class);
		assertThat(typedCountry).isEqualTo("IT");
	}

	@Test
	void booleanAttribute() {
		final Map<String, Boolean> attributes = new HashMap<>();
		attributes.put("a", true);
		attributes.put("b", false);
		final ConstraintContext context = ConstraintContext.from(attributes);
		assertThat(context.attribute("a", Boolean.class)).isTrue();
		assertThat(context.attribute("b", Boolean.class)).isFalse();
		assertThat(context.attribute("c", Boolean.class)).isFalse();
	}
}