/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.settings.locale;

/**
 * Language enum of supported languages, follows ISO 639-1 for language codes.
 *
 * @author Rsl1122
 */
public enum LangCode {

    CUSTOM("Custom", ""),
    EN("English", "Rsl1122"),
    ES("Spanish", "Catalina & itaquito & Elguerrero"),
    CN("Simplified Chinese", "f0rb1d (\u4f5b\u58c1\u706f) & qsefthuopq & shaokeyibb & Fur_xia"),
    DE("Deutsch", "Eyremba & fuzzlemann & Morsmorse & hallo1142"),
    FI("Finnish", "Rsl1122"),
    FR("French", "CyanTech & Aurelien & Nogapra"),
    IT("Italian", "Malachiel & Mastory_Md5"),
    JA("Japanese", "yukieji"),
    RU("Russian", "Saph1s"),
    TR("Turkish", "TDJisvan"),
    PT_BR("Portuguese (Brazil)", "jvmuller");

    private final String name;
    private final String authors;

    LangCode(String name, String authors) {
        this.name = name;
        this.authors = authors;
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

    public String getAuthors() {
        return authors;
    }

    public String getFileName() {
        return "locale_" + name() + ".txt";
    }
}
