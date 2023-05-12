package am.ik.yavi.core;

import java.util.Map;

import am.ik.yavi.core.ConstraintContext.Attribute;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class ConstraintContextTest {

	@Test
	void fromMap() {
		final ConstraintContext context = ConstraintContext.from(singletonMap("country", "IT"));
		final Attribute attribute = context.attribute("country");
		assertThat(attribute.exists()).isTrue();
		final Object country = attribute.value();
		assertThat(country).isEqualTo("IT");
		final String typedCountry = attribute.value(String.class);
		assertThat(typedCountry).isEqualTo("IT");
		assertThat(context.attribute("foo").exists()).isFalse();
	}

	@Test
	void fromFunction() {
		final Map<String, Object> headers = singletonMap("country", "IT");
		final ConstraintContext context = ConstraintContext.from(headers::get);
		final Attribute attribute = context.attribute("country");
		assertThat(attribute.exists()).isTrue();
		final Object country = attribute.value();
		assertThat(country).isEqualTo("IT");
		final String typedCountry = attribute.value(String.class);
		assertThat(typedCountry).isEqualTo("IT");
		assertThat(context.attribute("foo").exists()).isFalse();
	}

}