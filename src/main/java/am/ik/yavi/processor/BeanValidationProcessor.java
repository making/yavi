package am.ik.yavi.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import am.ik.yavi.fn.Pair;

/**
 * @since 0.9.0
 */
@SupportedAnnotationTypes({ //
		// Bean Validation 2.0
		"javax.validation.constraints.AssertTrue", //
		"javax.validation.constraints.AssertFalse", //
		"javax.validation.constraints.Null", //
		"javax.validation.constraints.NotNull", //
		"javax.validation.constraints.NotEmpty", //
		"javax.validation.constraints.NotBlank", //
		"javax.validation.constraints.Min", //
		"javax.validation.constraints.Max", //
		"javax.validation.constraints.DecimalMin", //
		"javax.validation.constraints.DecimalMax", //
		"javax.validation.constraints.Positive", //
		"javax.validation.constraints.PositiveOrZero", //
		"javax.validation.constraints.Negative", //
		"javax.validation.constraints.NegativeOrZero", //
		"javax.validation.constraints.Size", //
		"javax.validation.constraints.Email", //
		// Bean Validation 3.0
		"jakarta.validation.constraints.AssertTrue", //
		"jakarta.validation.constraints.AssertFalse", //
		"jakarta.validation.constraints.Null", //
		"jakarta.validation.constraints.NotNull", //
		"jakarta.validation.constraints.NotEmpty", //
		"jakarta.validation.constraints.NotBlank", //
		"jakarta.validation.constraints.Min", //
		"jakarta.validation.constraints.Max", //
		"jakarta.validation.constraints.DecimalMin", //
		"jakarta.validation.constraints.DecimalMax", //
		"jakarta.validation.constraints.Positive", //
		"jakarta.validation.constraints.PositiveOrZero", //
		"jakarta.validation.constraints.Negative", //
		"jakarta.validation.constraints.NegativeOrZero", //
		"jakarta.validation.constraints.Size", //
		"jakarta.validation.constraints.Email", //
		// Hibernate Validator
		"org.hibernate.validator.constraints.NotEmpty", //
		"org.hibernate.validator.constraints.NotBlank", //
		"org.hibernate.validator.constraints.Email", //
		"org.hibernate.validator.constraints.URL", //
		"org.hibernate.validator.constraints.Range", //
		"org.hibernate.validator.constraints.Length", //
		"org.hibernate.validator.constraints.LuhnCheck", //
})
@SupportedOptions({ BeanValidationProcessor.ENABLE_BEANVALIDATION_CONVERTER })
public class BeanValidationProcessor extends AbstractProcessor {
	private final Map<String, BeanValidationMeta> bvMetaMap;

	static final String ENABLE_BEANVALIDATION_CONVERTER = "yavi.enableBeanValidationConverter";

	public BeanValidationProcessor() {
		this.bvMetaMap = new HashMap<>();
		this.bvMetaMap.put("AssertTrue", new BeanValidationMetaAssertTrue());
		this.bvMetaMap.put("AssertFalse", new BeanValidationMetaAssertFalse());
		this.bvMetaMap.put("Null", new BeanValidationMetaNull());
		this.bvMetaMap.put("NotNull", new BeanValidationMetaNotNull());
		this.bvMetaMap.put("NotEmpty", new BeanValidationMetaNotEmpty());
		this.bvMetaMap.put("NotBlank", new BeanValidationMetaNotBlank());
		this.bvMetaMap.put("Min", new BeanValidationMetaMin());
		this.bvMetaMap.put("Max", new BeanValidationMetaMax());
		this.bvMetaMap.put("DecimalMin", new BeanValidationMetaDecimalMin());
		this.bvMetaMap.put("DecimalMax", new BeanValidationMetaDecimalMax());
		this.bvMetaMap.put("Positive", new BeanValidationMetaPositive());
		this.bvMetaMap.put("PositiveOrZero", new BeanValidationMetaPositiveOrZero());
		this.bvMetaMap.put("Negative", new BeanValidationMetaNegative());
		this.bvMetaMap.put("NegativeOrZero", new BeanValidationMetaNegativeOrZero());
		this.bvMetaMap.put("Size", new BeanValidationMetaSize());
		this.bvMetaMap.put("Pattern", new BeanValidationMetaPattern());
		this.bvMetaMap.put("Email", new BeanValidationMetaEmail());
		this.bvMetaMap.put("Length", new BeanValidationMetaLength());
		this.bvMetaMap.put("URL", new BeanValidationMetaURL());
		this.bvMetaMap.put("Range", new BeanValidationMetaRange());
		this.bvMetaMap.put("LuhnCheck", new BeanValidationMetaLuhnCheck());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		final String enabled = this.processingEnv.getOptions()
				.get(ENABLE_BEANVALIDATION_CONVERTER);
		if (!Boolean.parseBoolean(enabled)) {
			return true;
		}
		final Elements elementUtils = this.processingEnv.getElementUtils();
		final Map<String, Map<String, Set<String>>> metaMapMap = new LinkedHashMap<>();
		for (TypeElement annotation : annotations) {
			String beanFqcn;
			final Set<? extends Element> elements = roundEnv
					.getElementsAnnotatedWith(annotation);
			for (Element element : elements) {
				beanFqcn = element.getEnclosingElement().toString();
				TypeMirror type;
				String method;
				String fieldName;
				if (element.getKind() == ElementKind.METHOD) {
					type = ((ExecutableElement) element).getReturnType();
					method = beanFqcn + "::" + element.getSimpleName();
					fieldName = ProcessorUtils.beanLowerCamel(
							element.getSimpleName().toString().replaceFirst(
									"^" + ProcessorUtils.getterPrefix(type), ""));
				}
				else if (element.getKind() == ElementKind.FIELD) {
					type = element.asType();
					method = beanFqcn + "::" + ProcessorUtils.getterPrefix(type)
							+ ProcessorUtils
									.beanUpperCamel(element.getSimpleName().toString());
					fieldName = element.getSimpleName().toString();
				}
				else {
					continue;
				}
				final Map<String, Set<String>> metaMap = metaMapMap
						.computeIfAbsent(beanFqcn, k -> new LinkedHashMap<>());
				element.getAnnotationMirrors().forEach(annotationMirror -> {
					final Map<String, ? extends AnnotationValue> elementValues = elementUtils
							.getElementValuesWithDefaults(annotationMirror).entrySet()
							.stream().collect(Collectors.toMap(x -> x.getKey().toString(),
									Entry::getValue));
					final String annotationType = annotationMirror.getAnnotationType()
							.toString();
					final String key = ProcessorUtils.splitClassName(annotationType)
							.second();
					final BeanValidationMeta bvMeta = this.bvMetaMap.get(key);
					if (bvMeta != null) {
						final Set<String> list = metaMap.computeIfAbsent(
								method + ", \"" + fieldName + "\"",
								k -> new LinkedHashSet<>());
						try {
							list.addAll(
									bvMeta.constraintsWithMessage(elementValues, type));
						}
						catch (BeanValidationUnsupportedTypeException e) {
							this.processingEnv.getMessager().printMessage(Kind.ERROR,
									String.format("@%s: %s", key, e.getMessage()),
									element);
						}
					}
				});
			}
		}

		metaMapMap.forEach((beanFqcn, metaMap) -> {
			final Pair<String, String> splitBeanFqcn = ProcessorUtils
					.splitClassName(beanFqcn);
			final String validatorFqcn = String.format("%s.%s_Validator",
					splitBeanFqcn.first(), splitBeanFqcn.second().replace('.', '_'));
			try {
				final JavaFileObject builderFile = super.processingEnv.getFiler()
						.createSourceFile(validatorFqcn);
				try (final PrintWriter out = new PrintWriter(builderFile.openWriter())) {
					final Pair<String, String> splitClassName = ProcessorUtils
							.splitClassName(validatorFqcn);
					final String packageName = splitClassName.first();
					final String validatorClassName = splitClassName.second();
					final List<String> constraints = metaMap.entrySet().stream()
							.map(e -> "constraint(" + e.getKey() + ", c -> c."
									+ String.join(".", e.getValue()) + ")")
							.collect(Collectors.toList());
					if (!constraints.isEmpty()) {
						out.printf("package %s;%n", packageName);
						out.printf("%n");
						out.printf("// Generated at " + OffsetDateTime.now() + "%n");
						out.printf("public final class %s {%n", validatorClassName);
						final List<String> builder = new ArrayList<>();
						builder.add(String.format(
								"\tprivate static final am.ik.yavi.builder.ValidatorBuilder<%s> BUILDER = am.ik.yavi.builder.ValidatorBuilder.<%s> of()",
								beanFqcn, beanFqcn));
						builder.addAll(constraints);
						out.println(String.join("\n\t\t.", builder) + ";");
						out.printf("%n");
						out.printf(
								"\tpublic static final am.ik.yavi.core.Validator<%s> INSTANCE = BUILDER.build();%n",
								beanFqcn);
						out.printf("%n");
						out.printf(
								"\tpublic static am.ik.yavi.builder.ValidatorBuilder<%s> builder() {\n",
								beanFqcn);
						out.printf("\t\treturn BUILDER.clone();%n");
						out.printf("\t}%n");
						out.printf("}%n");
					}
				}
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		return true;
	}
}
