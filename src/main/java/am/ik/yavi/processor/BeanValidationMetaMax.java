package am.ik.yavi.processor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;

/**
 * @since 0.9.0
 */
class BeanValidationMetaMax implements BeanValidationMeta {
	@Override
	public List<String> constraints(Map<String, ? extends AnnotationValue> elementValues,
			TypeMirror type) {
		final AnnotationValue annotationValue = elementValues.get("value()");
		final long max = (Long) annotationValue.getValue();
		return Collections.singletonList(String.format("lessThanOrEqual(%s)",
				ProcessorUtils.toLiteral(max, type)));
	}
}
