package dev.jos.back.util.enums;

import org.junit.jupiter.api.Test;
import java.util.Locale;
import static org.assertj.core.api.Assertions.assertThat;

class SupportedLocaleTest {

    @Test
    void from_returnsDefaultForNull() {
        assertThat(SupportedLocale.from(null)).isEqualTo(SupportedLocale.FR);
    }

    @Test
    void from_returnsDefaultForBlank() {
        assertThat(SupportedLocale.from("   ")).isEqualTo(SupportedLocale.FR);
    }

    @Test
    void from_parsesBareTwoLetterCode() {
        assertThat(SupportedLocale.from("en")).isEqualTo(SupportedLocale.EN);
        assertThat(SupportedLocale.from("de")).isEqualTo(SupportedLocale.DE);
        assertThat(SupportedLocale.from("es")).isEqualTo(SupportedLocale.ES);
        assertThat(SupportedLocale.from("fr")).isEqualTo(SupportedLocale.FR);
    }

    @Test
    void from_parsesBcp47WithRegion() {
        assertThat(SupportedLocale.from("en-US")).isEqualTo(SupportedLocale.EN);
        assertThat(SupportedLocale.from("fr-FR")).isEqualTo(SupportedLocale.FR);
    }

    @Test
    void from_parsesAcceptLanguageHeader() {
        assertThat(SupportedLocale.from("fr,en;q=0.9")).isEqualTo(SupportedLocale.FR);
        assertThat(SupportedLocale.from("en;q=0.8,de;q=0.7")).isEqualTo(SupportedLocale.EN);
    }

    @Test
    void from_isCaseInsensitive() {
        assertThat(SupportedLocale.from("EN")).isEqualTo(SupportedLocale.EN);
        assertThat(SupportedLocale.from("FR")).isEqualTo(SupportedLocale.FR);
    }

    @Test
    void from_returnsDefaultForUnsupportedLocale() {
        assertThat(SupportedLocale.from("zh")).isEqualTo(SupportedLocale.FR);
        assertThat(SupportedLocale.from("pt-BR")).isEqualTo(SupportedLocale.FR);
    }

    @Test
    void javaLocale_matchesExpected() {
        assertThat(SupportedLocale.FR.javaLocale).isEqualTo(Locale.FRENCH);
        assertThat(SupportedLocale.EN.javaLocale).isEqualTo(Locale.ENGLISH);
        assertThat(SupportedLocale.DE.javaLocale).isEqualTo(Locale.GERMAN);
        assertThat(SupportedLocale.ES.javaLocale.getLanguage()).isEqualTo("es");
    }

    @Test
    void code_matchesTwoLetterTag() {
        assertThat(SupportedLocale.FR.code).isEqualTo("fr");
        assertThat(SupportedLocale.EN.code).isEqualTo("en");
    }
}
