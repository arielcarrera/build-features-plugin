package io.github.arielcarrera.build.features.utils;

import java.util.Locale;

public class OSPlatform {
    private static Boolean windows;

    public static boolean isWindows() {
        if (windows == null) {
            windows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows");
        }
        return windows;
    }
}
