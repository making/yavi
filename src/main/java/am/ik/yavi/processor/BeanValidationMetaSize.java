package am.ik.yavi.processor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;

/**
 * @since 0.9.0
 */
class BeanValidationMetaSize implements BeanValidationMeta {
	@Override
	public List<String> constraints(Map<String, ? extends AnnotationValue> elementValues,
			TypeMirror type) {
		final int min = (Integer) elementValues.get("min()").getValue();
		final int max = (Integer) elementValues.get("max()").getValue();
		return Arrays.asList(String.format("greaterThanOrEqual(%d)", min),
				String.format("lessThanOrEqual(%d)", max));
	}
}
