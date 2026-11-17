package am.ik.yavi.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import am.ik.yavi.runtime.preload.PreLoadedCompiler;
import am.ik.yavi.runtime.preload.PreLoadedFiles;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Compiles a complete Java compilation unit with the javac that is linked into the image
 * and runs the resulting classes with Espresso (the JVM written in Java) in its pure-Java
 * mode. Both halves share the in-browser file system: the classes javac writes are what
 * Espresso's class loader reads, and the YAVI jar is copied into that file system at
 * startup so examples can import {@code am.ik.yavi.*} without any setup.
 * <p>
 * A run is bounded by a step budget: the compiled classes are rewritten to call
 * {@code Budget.tick()} on every method entry and every jump, so a loop that never ends
 * ends the program with a readable message instead of hanging the page.
 */
public final class JavaRunner {

	private static final String ROOT = "/yavi-runtime";

	private static final String LIBS = ROOT + "/libs";

	private static final String RUNS = ROOT + "/runs";

	private static final String BUDGET_CLASS = "am/ik/yavi/runtime/guest/Budget";

	private static final String MAIN_SIGNATURE = "([Ljava/lang/String;)V";

	private final String yaviVersion;

	private final StandardJavaFileManager fileManager;

	private int runCounter;

	/** The directory of the previous run, deleted when the next one starts. */
	private Path previousRun;

	JavaRunner(String yaviVersion) {
		this.yaviVersion = yaviVersion;
		this.fileManager = initFileManager();
	}

	/**
	 * Runs one program and returns everything the cell should show: compiler diagnostics,
	 * the program's standard output and error, or the exception that ended it.
	 * @param source a complete compilation unit
	 * @return the output text, never {@code null}
	 */
	public String run(String source) {
		StringBuilder out = new StringBuilder();
		try {
			int run = ++this.runCounter;
			Path runDir = Path.of(RUNS, Integer.toString(run));
			Path classes = runDir.resolve("classes");
			if (this.previousRun != null) {
				deleteRecursively(this.previousRun);
			}
			this.previousRun = runDir;
			Files.createDirectories(classes);
			List<Diagnostic<? extends JavaFileObject>> diagnostics = new ArrayList<>();
			Compiled compiled = compile(source, classes, diagnostics);
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
				out.append(formatDiagnostic(diagnostic)).append('\n');
			}
			if (compiled.mainClass() == null) {
				if (out.length() == 0) {
					out.append("No public class with a public static void main(String[]) method was found.\n");
				}
				return out.toString();
			}
			for (Path classFile : compiled.classFiles()) {
				Files.write(classFile, Instrumenter.instrument(Files.readAllBytes(classFile)));
			}
			writeBudgetClasses();
			out.append(execute(compiled.mainClass(), classes));
		}
		catch (Throwable ex) {
			out.append("The runtime failed: ").append(ex).append('\n');
		}
		return out.toString();
	}

	private StandardJavaFileManager initFileManager() {
		try {
			return (StandardJavaFileManager) PreLoadedFiles.initFileSystem();
		}
		catch (IOException ex) {
			throw new IllegalStateException("Cannot initialize the in-browser file system", ex);
		}
	}

	private Compiled compile(String source, Path classes, List<Diagnostic<? extends JavaFileObject>> diagnostics)
			throws IOException {
		Path sourceFile = classes.getParent().resolve("src/Main.java");
		Files.createDirectories(sourceFile.getParent());
		Files.writeString(sourceFile, source, StandardCharsets.UTF_8);
		writeYaviClasses();
		// No --release: the in-image javac reads the preloaded JDK image, which has no
		// ct.sym, and the guest runs the same Java version anyway.
		List<String> options = List.of("-classpath", path(LIBS + "/yavi-classes", LIBS + "/budget"), "-d",
				classes.toString(), "-proc:none");
		List<JavaFileObject> units = new ArrayList<>();
		for (JavaFileObject unit : this.fileManager.getJavaFileObjectsFromPaths(List.of(sourceFile))) {
			units.add(unit);
		}
		JavaCompiler compiler = PreLoadedCompiler.getCompiler();
		DiagnosticListener<JavaFileObject> listener = diagnostics::add;
		Boolean success = compiler.getTask(null, this.fileManager, listener, options, null, units).call();
		if (success == null || !success) {
			return new Compiled(List.of(), null);
		}
		List<Path> classFiles = new ArrayList<>();
		try (Stream<Path> files = Files.walk(classes)) {
			classFiles.addAll(files.filter((p) -> p.toString().endsWith(".class"))
				.filter((p) -> !p.getFileName().toString().equals("module-info.class"))
				.toList());
		}
		MainClass main = new MainClass();
		for (Path classFile : classFiles) {
			new ClassReader(Files.readAllBytes(classFile)).accept(main, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
		}
		return new Compiled(classFiles, main.name);
	}

	/**
	 * Copies the YAVI classes embedded in the image into the in-browser file system so
	 * that javac can compile against them and Espresso can load them. The class list comes
	 * from an index written at build time.
	 */
	private void writeYaviClasses() throws IOException {
		Path root = Path.of(LIBS, "yavi-classes");
		if (Files.exists(root.resolve("am/ik/yavi/core/Validator.class"))) {
			return;
		}
		String index;
		try (InputStream in = JavaRunner.class.getResourceAsStream("/yavi-classes.index")) {
			if (in == null) {
				throw new IllegalStateException("The YAVI " + this.yaviVersion + " classes are not embedded in the image");
			}
			index = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		for (String line : index.split("\\R")) {
			String entry = line.trim();
			if (entry.isEmpty()) {
				continue;
			}
			Path target = root.resolve(entry.substring("yavi-classes/".length()));
			Files.createDirectories(target.getParent());
			try (InputStream in = JavaRunner.class.getResourceAsStream("/" + entry)) {
				if (in == null) {
					throw new IllegalStateException("Missing embedded resource " + entry);
				}
				Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	private void writeBudgetClasses() throws IOException {
		for (String name : List.of(BUDGET_CLASS, BUDGET_CLASS + "$BudgetExceededError")) {
			Path target = Path.of(LIBS, "budget", name + ".class");
			Files.createDirectories(target.getParent());
			try (InputStream in = JavaRunner.class.getResourceAsStream("/" + name + ".class")) {
				if (in == null) {
					throw new IllegalStateException("Missing class resource " + name);
				}
				Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	/**
	 * Runs the example's {@code main} method in a fresh Espresso context and returns the
	 * captured output.
	 */
	private String execute(String mainClass, Path classes) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		String classPath = path(LIBS + "/yavi-classes", LIBS + "/budget", classes.toString());
		try (Context context = Context.newBuilder()
			.allowIO(true)
			.allowExperimentalOptions(true)
			.option("java.Classpath", classPath)
			.option("log.java.level", "SEVERE")
			.option("engine.WarnInterpreterOnly", "false")
			.out(out)
			.err(err)
			.build()) {
			Value clazz = context.getBindings("java").getMember(mainClass);
			if (clazz == null) {
				return text(out, err) + "No class " + mainClass + " was loaded\n";
			}
			clazz.invokeMember("main", new Object[] { null });
		}
		catch (PolyglotException ex) {
			StringBuilder sb = new StringBuilder();
			appendException(sb, ex);
			sb.append(text(out, err));
			return sb.toString();
		}
		return text(out, err);
	}

	private static void appendException(StringBuilder sb, PolyglotException ex) {
		String message = ex.getMessage();
		// The polyglot layer formats guest exceptions through their toString; Espresso
		// reports a failure of that call instead of the exception's own message.
		if (message != null && message.startsWith("Error in toString(): ")) {
			message = message.substring("Error in toString(): ".length());
		}
		sb.append("Exception: ").append(message != null ? message : ex.toString()).append('\n');
		for (PolyglotException.StackFrame frame : ex.getPolyglotStackTrace()) {
			if (!frame.isHostFrame()) {
				sb.append("\tat ").append(frame.toString().replace("<java> ", "")).append('\n');
			}
		}
	}

	private static String text(ByteArrayOutputStream out, ByteArrayOutputStream err) {
		StringBuilder sb = new StringBuilder();
		sb.append(out.toString(StandardCharsets.UTF_8));
		sb.append(err.toString(StandardCharsets.UTF_8));
		return sb.toString();
	}

	private static String formatDiagnostic(Diagnostic<? extends JavaFileObject> diagnostic) {
		StringBuilder sb = new StringBuilder();
		if (diagnostic.getSource() != null) {
			sb.append(name(diagnostic.getSource())).append(':');
		}
		if (diagnostic.getLineNumber() != Diagnostic.NOPOS) {
			sb.append(diagnostic.getLineNumber()).append(':');
			if (diagnostic.getColumnNumber() != Diagnostic.NOPOS) {
				sb.append(diagnostic.getColumnNumber()).append(':');
			}
		}
		if (sb.length() > 0) {
			sb.append(' ');
		}
		sb.append(diagnostic.getKind().toString().toLowerCase(Locale.ROOT)).append(": ");
		sb.append(diagnostic.getMessage(Locale.ROOT));
		return sb.toString();
	}

	private static String name(JavaFileObject source) {
		String name = source.getName();
		int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
		return slash >= 0 ? name.substring(slash + 1) : name;
	}

	private static void deleteRecursively(Path directory) throws IOException {
		if (!Files.exists(directory)) {
			return;
		}
		try (Stream<Path> files = Files.walk(directory)) {
			for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
				Files.deleteIfExists(file);
			}
		}
	}

	private static String path(String... entries) {
		return String.join(":", entries);
	}

	/** The result of one compilation. */
	private record Compiled(List<Path> classFiles, String mainClass) {
	}

	/** Finds the class that declares {@code public static void main(String[])}. */
	private static final class MainClass extends ClassVisitor {

		private String currentClass;

		private String name;

		private MainClass() {
			super(Opcodes.ASM9);
		}

		@Override
		public void visit(int version, int access, String className, String signature, String superName,
				String[] interfaces) {
			this.currentClass = className.replace('/', '.');
		}

		@Override
		public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature,
				String[] exceptions) {
			boolean isMain = methodName.equals("main") && descriptor.equals(MAIN_SIGNATURE)
					&& (access & Opcodes.ACC_STATIC) != 0 && (access & Opcodes.ACC_PUBLIC) != 0;
			if (isMain && this.name == null) {
				this.name = this.currentClass;
			}
			return null;
		}

	}

}
