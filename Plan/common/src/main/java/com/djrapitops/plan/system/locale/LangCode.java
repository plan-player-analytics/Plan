/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
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
        return "locale_" + name() + ".txt";
    }
}
