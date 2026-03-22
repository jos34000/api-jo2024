package dev.jos.back.util;

import java.util.Set;

public final class LocaleResolver {

    private static final Set<String> SUPPORTED = Set.of("fr", "en", "de", "es");
    private static final String DEFAULT = "fr";

    private LocaleResolver() {}

    public static String resolve(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) return DEFAULT;
        String tag = acceptLanguage.split("[,;\\-]")[0].trim().toLowerCase();
        return SUPPORTED.contains(tag) ? tag : DEFAULT;
    }
}
