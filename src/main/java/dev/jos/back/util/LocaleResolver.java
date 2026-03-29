package dev.jos.back.util;

import dev.jos.back.util.enums.SupportedLocale;

public final class LocaleResolver {

    private LocaleResolver() {}

    public static String resolve(String acceptLanguage) {
        return SupportedLocale.from(acceptLanguage).code;
    }
}
