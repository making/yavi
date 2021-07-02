package am.ik.yavi.processor;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;

/**
 * @since 0.9.0
 */
@FunctionalInterface
interface BeanValidationBooleanMeta extends BeanValidationMeta {
	List<String> constraintsForBoolean(
			Map<String, ? extends AnnotationValue> elementValues, TypeMirror type);

	default List<String> constraints(Map<String, ? extends AnnotationValue> elementValues,
			TypeMirror type) {
		final String typeName = ProcessorUtils.typeName(type);
		if ("boolean".equals(typeName) || Boolean.class.getName().equals(typeName)) {
			return constraintsForBoolean(elementValues, type);
		}
		else {
			throw new BeanValidationUnsupportedTypeException(typeName);
		}
	}
}
