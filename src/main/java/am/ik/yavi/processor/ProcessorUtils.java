package am.ik.yavi.processor;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import am.ik.yavi.fn.Pair;

import static javax.lang.model.type.TypeKind.BOOLEAN;
import static javax.lang.model.type.TypeKind.BYTE;
import static javax.lang.model.type.TypeKind.CHAR;
import static javax.lang.model.type.TypeKind.DOUBLE;
import static javax.lang.model.type.TypeKind.FLOAT;
import static javax.lang.model.type.TypeKind.INT;
import static javax.lang.model.type.TypeKind.LONG;
import static javax.lang.model.type.TypeKind.SHORT;

/**
 * @since 0.9.0
 */
class ProcessorUtils {
	static Pair<String, String> splitClassName(String className) {
		String packageName = "";
		final int p = firstUpperPosition(className);
		if (p > 0) {
			packageName = className.substring(0, p - 1);
		}
		final String simpleClassName = className.substring(p);
		return new Pair<>(packageName, simpleClassName);
	}

	static int firstUpperPosition(String s) {
		final String lower = s.toLowerCase();
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != lower.charAt(i)) {
				return i;
			}
		}
		return -1;
	}

	static String getterPrefix(TypeMirror type) {
		return type.getKind() == BOOLEAN ? "is" : "get";
	}

	static String type(final TypeMirror typeMirror) {
		final TypeKind kind = typeMirror.getKind();
		if (kind.isPrimitive()) {
			if (kind == BOOLEAN) {
				return Boolean.class.getName();
			}
			else if (kind == BYTE) {
				return Byte.class.getName();
			}
			else if (kind == SHORT) {
				return Short.class.getName();
			}
			else if (kind == INT) {
				return Integer.class.getName();
			}
			else if (kind == LONG) {
				return Long.class.getName();
			}
			else if (kind == CHAR) {
				return Character.class.getName();
			}
			else if (kind == FLOAT) {
				return Float.class.getName();
			}
			else if (kind == DOUBLE) {
				return Double.class.getName();
			}
		}
		return typeMirror.toString();
	}

	static String beanLowerCamel(String s) {
		if (s.length() >= 2) {
			final String firstTwo = s.substring(0, 2);
			if (firstTwo.equals(firstTwo.toUpperCase())) {
				return s;
			}
		}
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	static String beanUpperCamel(String s) {
		if (s.length() >= 2) {
			final String firstTwo = s.substring(0, 2);
			if (firstTwo.equals(firstTwo.toUpperCase())) {
				return s;
			}
		}
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	static String lastElement(String[] array) {
		return array[array.length - 1];
	}

	static String typeName(TypeMirror type) {
		return lastElement(type.toString().split(" "));
	}

	static String toLiteral(Number number, TypeMirror type) {
		final String typeName = typeName(type);
		if (Integer.class.getName().equals(typeName) || "int".equals(typeName)) {
			return String.format("%s", number);
		}
		else if (Byte.class.getName().equals(typeName) || "byte".equals(typeName)) {
			return String.format("(byte) %s", number);
		}
		else if (Short.class.getName().equals(typeName) || "short".equals(typeName)) {
			return String.format("(short) %s", number);
		}
		else if (Long.class.getName().equals(typeName) || "long".equals(typeName)) {
			return String.format("%sl", number);
		}
		else if (Float.class.getName().equals(typeName) || "float".equals(typeName)) {
			return String.format("%sf", number);
		}
		else if (Double.class.getName().equals(typeName) || "double".equals(typeName)) {
			return String.format("%sd", number);
		}
		else if (BigInteger.class.getName().equals(typeName)) {
			return String.format("new java.math.BigInteger(\"%s\")", number);
		}
		else if (BigDecimal.class.getName().equals(typeName)) {
			return String.format("new java.math.BigDecimal(\"%s\")", number);
		}
		throw new BeanValidationUnsupportedTypeException(typeName);
	}
}
