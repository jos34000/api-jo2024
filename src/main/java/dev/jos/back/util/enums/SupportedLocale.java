package dev.jos.back.util.enums;

import java.util.Locale;

public enum SupportedLocale {
    FR("fr", Locale.FRENCH),
    EN("en", Locale.ENGLISH),
    DE("de", Locale.GERMAN),
    ES("es", Locale.of("es"));

    public final String code;
    public final Locale javaLocale;
    public static final SupportedLocale DEFAULT = FR;

    SupportedLocale(String code, Locale javaLocale) {
        this.code = code;
        this.javaLocale = javaLocale;
    }

    /**
     * Returns the SupportedLocale matching the first language tag in {@code raw}.
     * Splits on {@code ,}, {@code ;}, and {@code -} and matches the first token.
     * Returns {@link #DEFAULT} for null, blank, or unrecognised input.
     */
    public static SupportedLocale from(String raw) {
        if (raw == null || raw.isBlank()) return DEFAULT;
        String tag = raw.split("[,;\\-]")[0].trim().toLowerCase();
        for (SupportedLocale sl : values()) {
            if (sl.code.equals(tag)) return sl;
        }
        return DEFAULT;
    }
}
