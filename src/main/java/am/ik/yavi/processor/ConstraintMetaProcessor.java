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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import static am.ik.yavi.processor.ConstraintMetaTemplate.template;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.CLASS;
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

import am.ik.yavi.fn.Pair;
import am.ik.yavi.meta.ConstraintArguments;
import am.ik.yavi.meta.ConstraintTarget;

/**
 * @since 0.4.0
 */
@SupportedAnnotationTypes({ "am.ik.yavi.meta.ConstraintTarget",
		"am.ik.yavi.meta.ConstraintArguments" })
public class ConstraintMetaProcessor extends AbstractProcessor {

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		for (TypeElement typeElement : annotations) {
			final Name qualifiedName = typeElement.getQualifiedName();
			if (qualifiedName.contentEquals(ConstraintTarget.class.getName())) {
				this.processConstraintTarget(typeElement, roundEnv);
			}
			if (qualifiedName.contentEquals(ConstraintArguments.class.getName())) {
				this.processConstraintArguments(typeElement, roundEnv);
			}
		}
		return true;
	}

	private void processConstraintTarget(TypeElement typeElement,
			RoundEnvironment roundEnv) {
		final Set<? extends Element> elementsAnnotatedWith = roundEnv
				.getElementsAnnotatedWith(typeElement);
		final Map<String, List<Pair<Element, Integer>>> elementsMap = elementsAnnotatedWith
				.stream()
				.filter(x -> !(x instanceof ExecutableElement)
						|| ((ExecutableElement) x).getTypeParameters().isEmpty())
				.map(x -> new Pair<Element, Integer>(x, -1)).collect(groupingBy(k -> {
					String className = "****";
					final Element element = k.first();
					if (element instanceof VariableElement) {
						className = element.getEnclosingElement().getEnclosingElement()
								.toString();
					}
					else if (element instanceof ExecutableElement) {
						className = element.getEnclosingElement().toString();
					}
					return className;
				}, toList()));
		if (elementsMap.isEmpty()) {
			return;
		}
		elementsMap.forEach(this::writeConstraintMetaFile);
	}

	private void processConstraintArguments(TypeElement typeElement,
			RoundEnvironment roundEnv) {
		final Set<? extends Element> elementsAnnotatedWith = roundEnv
				.getElementsAnnotatedWith(typeElement);

		for (Element element : elementsAnnotatedWith) {
			final List<Element> parameters = new ArrayList<>(
					((ExecutableElement) element).getParameters());
			String className = element.getEnclosingElement().toString();
			if (element.getKind() == METHOD) {
				parameters.add(0, element.getEnclosingElement());
				className = className
						+ beanUpperCamel(element.getSimpleName().toString());
			}
			final String argumentsClass = "am.ik.yavi.arguments.Arguments"
					+ parameters.size() + "<" + parameters.stream()
							.map(x -> type(x.asType())).collect(Collectors.joining(", "))
					+ ">";
			final List<Pair<Element, Integer>> pairs = parameters.stream()
					.map(x -> new Pair<>(x, parameters.indexOf(x))).collect(toList());
			this.writeConstraintArgumentMetaFile(className + "Arguments", argumentsClass,
					pairs);
		}
	}

	private void writeConstraintMetaFile(String className,
			List<Pair<Element, Integer>> elements) {
		this.writeMetaFile(className, elements, (pair, metas) -> {
			final Element element = pair.first();
			final ConstraintTarget constraintTarget = element
					.getAnnotation(ConstraintTarget.class);
			final ElementKind kind = element.getKind();
			final String name = element.getSimpleName().toString();
			if (kind == METHOD) {
				final TypeMirror type = ((ExecutableElement) element).getReturnType();
				final String target = beanLowerCamel(constraintTarget.getter()
						? name.replaceFirst("^" + getterPrefix(type), "")
						: name);
				metas.put(target, template(className, type(type), target, name,
						constraintTarget.field()));
			}
			else if (kind == PARAMETER) {
				final TypeMirror type = element.asType();
				final String method = (constraintTarget.getter()
						? getterPrefix(type) + beanUpperCamel(name)
						: name);
				metas.put(name, template(className, type(type), name, method,
						constraintTarget.field()));
			}
		});
	}

	private void writeConstraintArgumentMetaFile(String className, String argumentsClass,
			List<Pair<Element, Integer>> elements) {
		this.writeMetaFile(className, elements, (pair, metas) -> {
			final Element element = pair.first();
			final TypeMirror type = element.asType();
			final int position = pair.second() + 1;
			final String name = element.getSimpleName().toString();
			metas.put(name,
					ConstraintMetaTemplate.templateArgument(argumentsClass, type(type),
							element.getKind() == CLASS ? beanLowerCamel(name) : name,
							position));
		});
	}

	private void writeMetaFile(String className, List<Pair<Element, Integer>> elements,
			BiConsumer<Pair<Element, Integer>, Map<String, String>> processElement) {
		final Pair<String, String> pair = splitClassName(className);
		final String packageName = pair.first();
		final String simpleClassName = pair.second();
		final String metaSimpleClassName = "_" + simpleClassName.replace('.', '_')
				+ "Meta";
		final String metaClassName = packageName + "." + metaSimpleClassName;
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
				for (Pair<Element, Integer> element : elements) {
					processElement.accept(element, metas);
				}
				metas.forEach((k, v) -> out.println("  " + v + ";"));
				out.println("}");
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

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
}
