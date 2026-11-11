/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.processor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Builds the source of the metadata constants written by {@link ConstraintMetaProcessor}.
 *
 * <p>
 * A constant is an anonymous implementation of the {@link am.ik.yavi.meta.ConstraintMeta}
 * subtype that matches the property type, or of
 * {@link am.ik.yavi.meta.ObjectConstraintMeta} for a type without a dedicated subtype.
 *
 * @since 0.4.0
 */
class ConstraintMetaTemplate {

	private static final Set<String> supportedTypes = new HashSet<String>() {
		{
			add(BigDecimal.class.getName());
			add(BigInteger.class.getName());
			add(Boolean.class.getName());
			add(Byte.class.getName());
			add(Character.class.getName());
			add(Double.class.getName());
			add(Float.class.getName());
			add(Integer.class.getName());
			add(Long.class.getName());
			add(Short.class.getName());
			add(String.class.getName());
			add(LocalDate.class.getName());
			add(LocalTime.class.getName());
			add(LocalDateTime.class.getName());
			add(OffsetDateTime.class.getName());
			add(ZonedDateTime.class.getName());
			add(Instant.class.getName());
		}
	};

	/**
	 * Returns the source of a property metadata constant for a bean.
	 * @param className the fully qualified name of the annotated type
	 * @param type the type of the property value
	 * @param target the property name and, without getter naming, the method name
	 * @param method the method or getter that extracts the value
	 * @param useField whether the extractor reads the field instead of the method
	 * @return the source of the constant
	 */
	static String template(String className, String type, String target, String method, boolean useField) {
		final String simpleType = simpleType(type);
		final String metaType = "Object".equals(simpleType) ? className + ", " + type : className;
		return String.format("\n" + //
				"\tpublic static final am.ik.yavi.meta.%sConstraintMeta<%s> %s = new am.ik.yavi.meta.%sConstraintMeta<%s>() {\n"
				+ "\n" + //
				"\t\t@Override\n" + //
				"\t\tpublic String name() {\n" + //
				"\t\t\treturn \"%s\";\n" + //
				"\t\t}\n" + //
				"\n" + //
				"\t\t@Override\n" //
				+ "\t\tpublic java.util.function.Function<%s, %s> toValue() {\n" //
				+ "\t\t\treturn %s;\n" + //
				"\t\t}\n" + //
				"\t}", simpleType, metaType, target.toUpperCase(), simpleType, metaType, target, className, type,
				useField ? "x  -> x." + target : className + "::" + method);
	}

	/**
	 * Returns the source of a metadata constant for a constructor or method argument.
	 * @param className the type of the {@code ArgumentsN} container
	 * @param type the type of the argument value
	 * @param target the property name
	 * @param position the one-based position of the argument in the container
	 * @return the source of the constant
	 */
	static String templateArgument(String className, String type, String target, int position) {
		final String simpleType = simpleType(type);
		final String metaType = "Object".equals(simpleType) ? className + ", " + type : className;
		return String.format("\n" + //
				"\tpublic static final am.ik.yavi.meta.%sConstraintMeta<%s> %s = new am.ik.yavi.meta.%sConstraintMeta<%s>() {\n"
				+ "\n" + //
				"\t\t@Override\n" + //
				"\t\tpublic String name() {\n" + //
				"\t\t\treturn \"%s\";\n" + //
				"\t\t}\n" + //
				"\n" + //
				"\t\t@Override\n" //
				+ "\t\tpublic java.util.function.Function<%s, %s> toValue() {\n" //
				+ "\t\t\treturn %s;\n" + //
				"\t\t}\n" + //
				"\t}", simpleType, metaType, target.toUpperCase(), simpleType, metaType, target, className, type,
				className.replaceAll("<(.+)>", "") + "::arg" + position);
	}

	private static String simpleType(String type) {
		if (supportedTypes.contains(type)) {
			final int lastDot = type.lastIndexOf('.');
			return type.substring(lastDot + 1);
		}
		else {
			return "Object";
		}
	}

}
