package am.ik.yavi.processor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;

/**
 * @since 0.9.0
 */
class BeanValidationMetaDecimalMin implements BeanValidationMeta {
	@Override
	public List<String> constraints(Map<String, ? extends AnnotationValue> elementValues,
			TypeMirror type) {
		final String min = (String) elementValues.get("value()").getValue();
		final boolean inclusive = (Boolean) elementValues.get("inclusive()").getValue();
		return Collections.singletonList(
				String.format("%s(%s)", inclusive ? "greaterThanOrEqual" : "greaterThan",
						ProcessorUtils.toLiteral(new BigDecimal(min), type)));
	}
}
