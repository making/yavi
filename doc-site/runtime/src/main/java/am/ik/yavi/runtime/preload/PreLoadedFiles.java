/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package am.ik.yavi.runtime.preload;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeModel;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.MethodElement;
import java.lang.classfile.MethodModel;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;

import am.ik.yavi.runtime.preload.PackageNamingUtil;

/**
 * Responsible for pre-loading all data at build-time that is required to run javac.
 * <p>
 * Loads all the module contents for {@link #PRELOADED_JDK_MODULES}. The data is stored as byte
 * arrays in {@link PreLoadedPackage}:
 * <p>
 * Cannot use java path instances here because this file is initialized at build-time and the
 * build-time path instances are not compatible with the run-time path instances (from JIMFS).
 */
public class PreLoadedFiles {
    public static final String SOURCE_PATH = "/sourcePath";
    public static final String OUTPUT_PATH = "/outputPath";
    public static final String CLASS_PATH = "/classPath";
    public static final String JAVA_HOME = CLASS_PATH + "/jdk";
    public static final String JDK_MODULES = JAVA_HOME + "/lib/modules";

    private static final Map<String, PreLoadedLocation> MODULE_LOCATIONS = new HashMap<>();
    private static final String[] PRELOADED_JDK_MODULES = {"java.base"};

    private static final byte[] CLASS_MAGIC = new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};

    static {
        initModules();
    }

    /**
     * Reads all required modules data into {@link PreLoadedPackage}s.
     * <p>
     * Must be executed during build-time.
     *
     * @see #PRELOADED_JDK_MODULES
     */
    public static void initModules() {
        System.err.println("Start loading selected classes into the image heap");
        try {
            ModuleFinder mf = ModuleFinder.ofSystem();

            for (String moduleName : PRELOADED_JDK_MODULES) {
                Optional<ModuleReference> moduleReference = mf.find(moduleName);
                assert moduleReference.isPresent() : "Cannot find module " + moduleName;
                int numBytes = initModule(moduleName, moduleReference.get());
                System.out.printf("%s: %dB%n", moduleName, numBytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.err.println("Finished loading selected classes into the image heap");
    }

    private static int initModule(String name, ModuleReference moduleReference) throws IOException {
        assert !MODULE_LOCATIONS.containsKey(name);
        PreLoadedLocation preLoadedLocation = new PreLoadedLocation();
        MODULE_LOCATIONS.put(name, preLoadedLocation);

        AtomicInteger totalBytes = new AtomicInteger();

        try (ModuleReader reader = moduleReference.open()) {
            Stream<String> resourceNameStream = reader.list();
            resourceNameStream.forEach(resourceName -> {
                try (InputStream is = reader.open(resourceName).get()) {
                    /*
                     * resourceName is a path relative to the module, for example:
                     * java/lang/String.class
                     */
                    Path resourcePath = Path.of(resourceName);
                    String packageName = PackageNamingUtil.extractPackageName(resourcePath);
                    String fileName;
                    if (resourcePath.startsWith("META-INF")) {
                        // since META-INF is in the empty package, we need the path to
                        // correctly place the files in this directory
                        fileName = resourceName;
                    } else {
                        Path fileNamePath = resourcePath.getFileName();
                        assert fileNamePath != null;
                        fileName = fileNamePath.toString();
                    }
                    byte[] bytes = is.readAllBytes();

                    /*
                     * Class files with the correct magic header (0xCAFEBABE) have their method
                     * bodies erased to reduce the file size. For compilation, javac does not need
                     * the method bodies.
                     */
                    if (resourceName.endsWith(".class") && bytes.length >= 4 && Arrays.equals(bytes, 0, CLASS_MAGIC.length, CLASS_MAGIC, 0, CLASS_MAGIC.length)) {
                        bytes = eraseMethodBodies(bytes);
                    }

                    preLoadedLocation.put(packageName, fileName, bytes);
                    totalBytes.addAndGet(bytes.length);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return totalBytes.get();
    }

    /**
     * Strips method bodies from the given class file (as a byte array) using the Java Class-File
     * API and returns the new class file bytes.
     */
    public static byte[] eraseMethodBodies(byte[] bytes) {
        ClassModel classModel = ClassFile.of().parse(bytes);
        return ClassFile.of().build(classModel.thisClass().asSymbol(), (ClassBuilder classBuilder) -> {
            for (ClassElement ce : classModel) {
                if (ce instanceof MethodModel mm) {
                    classBuilder.withMethod(mm.methodName(), mm.methodType(), mm.flags().flagsMask(), (MethodBuilder methodBuilder) -> {
                        for (MethodElement me : mm) {
                            if (me instanceof CodeModel) {
                                methodBuilder.withCode((CodeBuilder codeBuilder) -> codeBuilder.aconst_null().athrow());
                            } else {
                                methodBuilder.with(me);
                            }
                        }
                    });
                } else {
                    classBuilder.with(ce);
                }
            }
        });
    }

    /**
     * Initializes the virtual file system from preloaded data and returns a {@link JavaFileManager}
     * constructed using the paths used to populate the filesystem.
     */
    @SuppressWarnings("hiding")
    public static JavaFileManager initFileSystem() throws IOException {
        System.setProperty("java.home", PreLoadedFiles.JAVA_HOME);

        long start = System.nanoTime();
        System.err.println("Start loading preloaded class files");
        for (String moduleName : PRELOADED_JDK_MODULES) {
            Path modulePath = Path.of(JDK_MODULES, moduleName);
            MODULE_LOCATIONS.get(moduleName).fillFileSystem(modulePath);
        }

        Files.createDirectories(Path.of(PreLoadedFiles.CLASS_PATH));
        Files.createDirectories(Path.of(PreLoadedFiles.SOURCE_PATH));
        Files.createDirectories(Path.of(PreLoadedFiles.OUTPUT_PATH));
        long elapsed = System.nanoTime() - start;
        System.err.printf("Finished loading preloaded class files in %dms%n", elapsed / 1_000_000);

        StandardJavaFileManager standardFileManager = PreLoadedCompiler.getCompiler().getStandardFileManager(null, Locale.ROOT, null);

        boolean result = standardFileManager.handleOption("-cp", List.of(PreLoadedFiles.CLASS_PATH).iterator());
        assert result : "Failed to apply -cp option";
        result = standardFileManager.handleOption("-d", List.of(PreLoadedFiles.OUTPUT_PATH).iterator());
        assert result : "Failed to apply -d option";
        result = standardFileManager.handleOption("--source-path", List.of(PreLoadedFiles.SOURCE_PATH).iterator());
        assert result : "Failed to apply --source-path option";
        result = standardFileManager.handleOption("--system", List.of(PreLoadedFiles.JAVA_HOME).iterator());
        assert result : "Failed to apply --system option";

        return standardFileManager;
    }
}
