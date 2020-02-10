/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.processor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

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
		}
	};

	static String template(String className, String type, String target, String method,
			boolean useField) {
		final String simpleType = simpleType(type);
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
				"\t}", simpleType, className, target.toUpperCase(), simpleType, className,
				target, className,
				"Object".equals(simpleType) ? "java.lang.Object" : type,
				useField ? "x  -> x." + target : className + "::" + method);
	}

	static String templateArgument(String className, String type, String target,
			int position) {
		final String simpleType = simpleType(type);
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
				"\t}", simpleType, className, target.toUpperCase(), simpleType, className,
				target, className,
				"Object".equals(simpleType) ? "java.lang.Object" : type,
				"am.ik.yavi.arguments.Arguments" + position + "::arg" + position);
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
