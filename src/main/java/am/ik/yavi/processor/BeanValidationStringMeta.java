package am.ik.yavi.processor;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;

/**
 * @since 0.9.0
 */
@FunctionalInterface
interface BeanValidationStringMeta extends BeanValidationMeta {
	List<String> constraintsForString(
			Map<String, ? extends AnnotationValue> elementValues, TypeMirror type);

	default List<String> constraints(Map<String, ? extends AnnotationValue> elementValues,
			TypeMirror type) {
		final String typeName = ProcessorUtils.typeName(type);
		if (String.class.getName().equals(typeName)
				|| CharSequence.class.getName().equals(typeName)
				|| StringBuilder.class.getName().equals(typeName)
				|| StringBuffer.class.getName().equals(typeName)
				|| CharBuffer.class.getName().equals(typeName)) {
			return constraintsForString(elementValues, type);
		}
		else {
			throw new BeanValidationUnsupportedTypeException(typeName);
		}
	}
}
