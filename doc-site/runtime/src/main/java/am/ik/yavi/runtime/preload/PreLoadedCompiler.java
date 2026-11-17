/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package am.ik.yavi.runtime.preload;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class PreLoadedCompiler {
    private static final JavaCompiler c = ToolProvider.getSystemJavaCompiler();

    public static JavaCompiler getCompiler() {
        return c;
    }
}
