/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package am.ik.yavi.runtime.preload;

import java.nio.file.Path;

/**
 * Methods/fields used for handling package names.
 */
public class PackageNamingUtil {

    /**
     * Extracts the package name of a resource name.
     * <p>
     * For example: {@code java/lang/String.class}
     * <p>
     * to {@code java.lang}
     * <p>
     * The META-INF directory is interpreted to be in the empty package.
     *
     * @param fsPath resourceName
     */
    public static String extractPackageName(Path fsPath) {
        if (fsPath.startsWith("META-INF")) {
            return "";
        } else {
            Path packagePath = fsPath.getParent();

            if (packagePath == null) {
                return "";
            }

            return packagePath.toString().replace('/', '.');
        }
    }

    public static String removeExtension(String fileName) {
        int suffixIdx = fileName.lastIndexOf('.');
        assert suffixIdx >= 0 : fileName;
        return fileName.substring(0, suffixIdx);
    }
}
