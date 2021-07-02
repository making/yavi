/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import static am.ik.yavi.processor.ConstraintMetaTemplate.template;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.ElementKind.PARAMETER;

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
				className = className + ProcessorUtils
						.beanUpperCamel(element.getSimpleName().toString());
			}
			final String argumentsClass = "am.ik.yavi.arguments.Arguments"
					+ parameters.size() + "<"
					+ parameters.stream().map(x -> ProcessorUtils.type(x.asType()))
							.collect(Collectors.joining(", "))
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
				final String target = ProcessorUtils
						.beanLowerCamel(constraintTarget.getter()
								? name.replaceFirst(
										"^" + ProcessorUtils.getterPrefix(type), "")
								: name);
				metas.put(target, template(className, ProcessorUtils.type(type), target,
						name, constraintTarget.field()));
			}
			else if (kind == PARAMETER) {
				final TypeMirror type = element.asType();
				final String method = (constraintTarget.getter()
						? ProcessorUtils.getterPrefix(type)
								+ ProcessorUtils.beanUpperCamel(name)
						: name);
				metas.put(name, template(className, ProcessorUtils.type(type), name,
						method, constraintTarget.field()));
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
					ConstraintMetaTemplate.templateArgument(argumentsClass,
							ProcessorUtils.type(type),
							element.getKind() == CLASS
									? ProcessorUtils.beanLowerCamel(name)
									: name,
							position));
		});
	}

	private void writeMetaFile(String className, List<Pair<Element, Integer>> elements,
			BiConsumer<Pair<Element, Integer>, Map<String, String>> processElement) {
		final Pair<String, String> pair = ProcessorUtils.splitClassName(className);
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

}
