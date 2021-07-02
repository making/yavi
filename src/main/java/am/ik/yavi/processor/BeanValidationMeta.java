package am.ik.yavi.processor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;

/**
 * @since 0.9.0
 */
@FunctionalInterface
interface BeanValidationMeta {
	List<String> constraints(Map<String, ? extends AnnotationValue> elementValues,
			TypeMirror type);

	default List<String> constraintsWithMessage(
			Map<String, ? extends AnnotationValue> elementValues, TypeMirror type) {
		final String message = (String) elementValues.get("message()").getValue();
		final List<String> constraints = this.constraints(elementValues, type);
		if (message.startsWith("{") && message.endsWith(".message}")) {
			return constraints;
		}
		else {
			return constraints.stream()
					.map(c -> String.format("%s.message(\"%s\")", c, message))
					.collect(Collectors.toList());
		}
	}
}
