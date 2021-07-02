package am.ik.yavi.processor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;

/**
 * @since 0.9.0
 */
class BeanValidationMetaURL implements BeanValidationStringMeta {
	@Override
	public List<String> constraintsForString(
			Map<String, ? extends AnnotationValue> elementValues, TypeMirror type) {
		// TODO attributes
		return Collections.singletonList("url()");
	}
}
