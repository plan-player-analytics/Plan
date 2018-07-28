package com.djrapitops.plan.system.locale;

/**
 * Language enum of supported languages, follows ISO 639-1 for language codes.
 *
 * @author Rsl1122
 */
public enum LangCode {

    CUSTOM("Custom"),
    EN("English"),
    FI("Finnish"),
    DE("Deutch"),
    FR("French"),
    GA("Irish (Gaeilge)"),
    CS("Czech"),
    PT("Portugese"),
    NL("Dutch"),
    NO("Norwegian"),
    PL("Polish"),
    IT("Italian");

    private final String name;

    LangCode(String name) {
        this.name = name;
    }

    public static LangCode fromString(String code) {
        try {
            return LangCode.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LangCode.EN;
        }
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return "locale_" + name + ".txt";
    }
}
