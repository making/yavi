/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package am.ik.yavi.runtime.preload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores the file contents of a package in byte arrays and recursively stores all sub-packages.
 */
public class PreLoadedPackage {

    /**
     * Unqualified package name.
     */
    public final String packageName;

    /**
     * Maps the unqualified sub-package name to its {@link PreLoadedPackage} instance.
     */
    private final Map<String, PreLoadedPackage> subPackages = new HashMap<>();

    /**
     * Maps each file name in this package to the file contents.
     */
    private final Map<String, byte[]> fileContents = new HashMap<>();

    public PreLoadedPackage(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Get (and optionally create) a sub-package.
     */
    public PreLoadedPackage get(String subPackageName) {
        return subPackages.computeIfAbsent(subPackageName, PreLoadedPackage::new);
    }

    public void add(String fileName, byte[] readAllBytes) {
        fileContents.put(fileName, readAllBytes);
    }

    /**
     * Writes the file contents of this package and all sub-packages into the file system relative
     * to {@code basePath}.
     */
    public void fillFileSystem(Path basePath) throws IOException {
        Path packagePath = basePath.resolve(packageName);
        for (Map.Entry<String, byte[]> entry : fileContents.entrySet()) {
            Path filePath = packagePath.resolve(entry.getKey());
            Path parent = filePath.getParent();
            assert parent != null;
            Files.createDirectories(parent);
            Files.createFile(filePath);
            Files.write(filePath, entry.getValue());
        }
        for (PreLoadedPackage subPackage : subPackages.values()) {
            subPackage.fillFileSystem(packagePath);
        }
    }
}
