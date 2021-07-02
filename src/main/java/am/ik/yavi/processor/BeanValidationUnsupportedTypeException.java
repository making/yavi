package am.ik.yavi.processor;

import javax.lang.model.type.TypeMirror;

class BeanValidationUnsupportedTypeException extends RuntimeException {
	BeanValidationUnsupportedTypeException(String typeName) {
		super(String.format("\"%s\" is not supported.", typeName));
	}

	BeanValidationUnsupportedTypeException(TypeMirror type) {
		this(ProcessorUtils.typeName(type));
	}
}
