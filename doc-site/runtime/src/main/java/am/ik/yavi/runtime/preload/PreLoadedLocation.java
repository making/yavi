/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package am.ik.yavi.runtime.preload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Location with preloaded packages. This might be a module, like java.base, or the CLASS_PATH.
 */
public class PreLoadedLocation {

    /**
     * The empty package recursively contains all sub-packages.
     */
    public final PreLoadedPackage emptyPackage = new PreLoadedPackage("");

    /**
     * Stores the file contents in the correct {@link PreLoadedPackage} under {@link #emptyPackage}.
     *
     * @param packageName Name of package
     * @param fileName Name of file in package
     * @param readAllBytes File contents
     */
    public void put(String packageName, String fileName, byte[] readAllBytes) {
        String[] packages = packageName.split("\\.");
        PreLoadedPackage targetPackage = emptyPackage;
        for (String subPackage : packages) {
            // traverse down the package tree
            targetPackage = targetPackage.get(subPackage);
        }
        targetPackage.add(fileName, readAllBytes);
    }

    /**
     * Re-builds package structure in the file system from preloaded data.
     * <p>
     * Creates the necessary directories and fills in all package files.
     */
    public void fillFileSystem(Path basePath) throws IOException {
        Files.createDirectories(basePath);
        emptyPackage.fillFileSystem(basePath);
    }
}
