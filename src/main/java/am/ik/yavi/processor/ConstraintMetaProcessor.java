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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import static am.ik.yavi.processor.ConstraintMetaTemplate.template;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.ElementKind.PARAMETER;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static javax.lang.model.type.TypeKind.BYTE;
import static javax.lang.model.type.TypeKind.CHAR;
import static javax.lang.model.type.TypeKind.DOUBLE;
import static javax.lang.model.type.TypeKind.FLOAT;
import static javax.lang.model.type.TypeKind.INT;
import static javax.lang.model.type.TypeKind.LONG;
import static javax.lang.model.type.TypeKind.SHORT;

import am.ik.yavi.meta.ConstraintTarget;

@SupportedAnnotationTypes("am.ik.yavi.meta.ConstraintTarget")
public class ConstraintMetaProcessor extends AbstractProcessor {

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		for (TypeElement typeElement : annotations) {
			if (!typeElement.getQualifiedName()
					.contentEquals(ConstraintTarget.class.getName())) {
				continue;
			}
			this.processConstraintTarget(typeElement, roundEnv);
		}
		return true;
	}

	private void processConstraintTarget(TypeElement typeElement,
			RoundEnvironment roundEnv) {
		final Set<? extends Element> elementsAnnotatedWith = roundEnv
				.getElementsAnnotatedWith(typeElement);
		final Map<String, ? extends List<? extends Element>> elementsMap = elementsAnnotatedWith
				.stream()
				.filter(x -> !(x instanceof ExecutableElement)
						|| ((ExecutableElement) x).getTypeParameters().isEmpty())
				.collect(Collectors.groupingBy(k -> {
					String className = "****";
					if (k instanceof VariableElement) {
						className = k.getEnclosingElement().getEnclosingElement()
								.toString();
					}
					else if (k instanceof ExecutableElement) {
						className = k.getEnclosingElement().toString();
					}
					return className;
				}, Collectors.toList()));
		if (elementsMap.isEmpty()) {
			return;
		}
		elementsMap.forEach(this::writeConstraintMetaFile);
	}

	private void writeConstraintMetaFile(String className,
			List<? extends Element> elements) {
		String packageName = "";
		final int lastDot = className.lastIndexOf('.');
		if (lastDot > 0) {
			packageName = className.substring(0, lastDot);
		}

		final String simpleClassName = className.substring(lastDot + 1);
		final String metaClassName = packageName + "._" + simpleClassName + "Meta";
		final String metaSimpleClassName = metaClassName.substring(lastDot + 1);
		try {
			final JavaFileObject builderFile = super.processingEnv.getFiler()
					.createSourceFile(metaClassName);
			try (final PrintWriter out = new PrintWriter(builderFile.openWriter())) {

				if (!packageName.isEmpty()) {
					out.print("package ");
					out.print(packageName);
					out.println(";");
					out.println();
				}

				out.println("// Generated at " + OffsetDateTime.now());
				out.print("public class ");
				out.print(metaSimpleClassName);
				out.println(" {");

				final Map<String, String> metas = new LinkedHashMap<>();
				for (Element element : elements) {
					final ConstraintTarget constraintTarget = element
							.getAnnotation(ConstraintTarget.class);
					final ElementKind kind = element.getKind();
					final String name = element.getSimpleName().toString();
					if (kind == METHOD) {
						final TypeMirror type = ((ExecutableElement) element)
								.getReturnType();
						final String getterPrefix = type.getKind() == BOOLEAN ? "is"
								: "get";
						final String target = beanLowerCamel(constraintTarget.getter()
								? name.replaceFirst("^" + getterPrefix, "")
								: name);
						metas.put(target, template(className, type(type), target, name,
								constraintTarget.field()));
					}
					else if (kind == PARAMETER) {
						final TypeMirror type = element.asType();
						final String getterPrefix = type.getKind() == BOOLEAN ? "is"
								: "get";
						final String method = (constraintTarget.getter()
								? getterPrefix + beanUpperCamel(name)
								: name);
						metas.put(name, template(className, type(type), name, method,
								constraintTarget.field()));
					}
				}
				metas.forEach((k, v) -> out.println("  " + v + ";"));
				out.println("}");
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
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
}
