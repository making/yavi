package am.ik.yavi.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

import org.graalvm.webimage.api.JS;
import org.graalvm.webimage.api.JSString;

/**
 * Entry point of the Web Image. Compilation and execution are driven from JavaScript:
 * {@code window.YaviRuntime.run(source)} (see the assets binding) loads this image, and
 * this class publishes {@code globalThis.YaviJs}, whose {@code run} function compiles and
 * runs one example.
 * <p>
 * Espresso (the guest JVM) reads the process-wide {@code os.arch} property, which the Web
 * Image sets to its WebAssembly target name; restore the build machine's architecture so
 * Espresso can pick a register convention. Nothing native is loaded in this mode; the
 * value only has to be one Espresso knows.
 */
public final class WebMain {

	/**
	 * Version of the YAVI jar embedded in this image. Filtered in at build time from the
	 * {@code yavi.version} property (declared once, in doc-site/content/versions.yaml).
	 */
	public static final String YAVI_VERSION = loadYaviVersion();

	private static String loadYaviVersion() {
		try (InputStream in = WebMain.class.getResourceAsStream("/yavi-version.properties")) {
			if (in == null) {
				throw new IllegalStateException("Missing yavi-version.properties in the image");
			}
			Properties properties = new Properties();
			properties.load(in);
			String version = properties.getProperty("yavi.version", "").trim();
			if (version.isEmpty() || version.startsWith("${")) {
				throw new IllegalStateException("yavi-version.properties has no yavi.version: '" + version + "'");
			}
			return version;
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/**
	 * Shared across cells: initializing the in-browser file system with the JDK classes
	 * javac needs is the expensive part of a first run.
	 */
	private static JavaRunner runner;

	private WebMain() {
	}

	public static void main(String[] args) {
		System.setProperty("os.arch", BuildInfo.OS_ARCH);
		System.setProperty("polyglot.engine.allowUnsupportedPlatform", "true");
		System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
		// Espresso extracts its class library into the polyglot cache on first start. The
		// Web Image has no real file system; the path only has to be stable.
		System.setProperty("polyglot.engine.userResourceCache", "/yavi-runtime/polyglot-cache");
		install(WebMain::runSource, YAVI_VERSION);
		installWorkerHandler(WebMain::runSource);
	}

	/** Runs one complete compilation unit and returns its output text. */
	static JSString runSource(JSString source) {
		try {
			String text = source == null ? null : source.asString();
			if ("<yavi-ping>".equals(text)) {
				// Handshake of the browser binding: tells the page the worker is ready.
				return JSString.of("yavi-runtime-ping:" + YAVI_VERSION);
			}
			if (text == null || text.isBlank()) {
				return JSString.of("The cell is empty.\n");
			}
			return JSString.of(runner().run(text));
		}
		catch (Throwable ex) {
			System.err.println("YaviRuntime caught: " + ex);
			ex.printStackTrace();
			return JSString.of("The runtime failed: " + ex + "\n");
		}
	}

	private static synchronized JavaRunner runner() {
		if (runner == null) {
			runner = new JavaRunner(YAVI_VERSION);
		}
		return runner;
	}

	@JS.Coerce
	@JS("globalThis.YaviJs = { run: runner, yaviVersion: version };")
	private static native void install(Runner runner, String version);

	/**
	 * When the image is loaded in a Web Worker (see the binding asset), answer run
	 * requests from the page. Workers give the interpreter a deeper stack than the page's
	 * main thread, which the recursive interpreter needs.
	 */
	@JS.Coerce
	@JS("if (typeof WorkerGlobalScope !== 'undefined' && self instanceof WorkerGlobalScope) { self.addEventListener('message', (e) => self.postMessage(runner(e.data))); }")
	private static native void installWorkerHandler(Runner runner);

	/** Callable from JavaScript; never throws so the cell always shows text. */
	@FunctionalInterface
	interface Runner {

		JSString run(JSString source);

	}

}
