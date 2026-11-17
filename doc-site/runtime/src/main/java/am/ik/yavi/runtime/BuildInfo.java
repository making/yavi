package am.ik.yavi.runtime;

/**
 * Values captured while the image is built. Espresso reads {@code os.arch} at run time to
 * select its register conventions; the Web Image sets it to the WebAssembly target name
 * ("ECMAScript 2015"), which Espresso does not know, so the runtime restores the build
 * machine's architecture before Espresso starts. Espresso runs in its pure-Java mode on
 * the Web Image anyway, where the value only has to name a known architecture.
 * <p>
 * The class is initialized at build time (see the native-image configuration).
 */
public final class BuildInfo {

	/** The architecture of the machine that built the image. */
	public static final String OS_ARCH = System.getProperty("os.arch");

	private BuildInfo() {
	}

}
