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
 * @author AuroraLS3
 */
public enum LangCode {

    CUSTOM("Custom", ""),
    EN("English", "AuroraLS3"),
    ES("Español", "Catalina, itaquito, Elguerrero & 4drian3d"),
    CN("\u6C49\u8BED", "f0rb1d (\u4f5b\u58c1\u706f), qsefthuopq, shaokeyibb, Fur_xia, 10935336, SkipM4, TheLittle_Yang, jhqwqmc & liuzhen932"), // Simplified Chinese
    CS("\u010de\u0161tina", "Shadowhackercz, QuakyCZ, MrFriggo & WolverStones"),
    DE("Deutsch", "Eyremba, fuzzlemann, Morsmorse, hallo1142 & DubHacker"),
    FI("suomi", "AuroraLS3, KasperiP"),
    FR("français", "CyanTech, Aurelien, Nogapra & Sniper_TVmc"),
    IT("Italiano", "Malachiel & Mastory_Md5"),
    JA("\u65E5\u672C\u8A9E", "yukieji, inductor, lis2a, yu_solt, Jumala9163, Zaemong & ringoXD"),
    KO("\uD55C\uAD6D\uC5B4", "Guinness_Akihiko"),
    NL("Nederlands", "Sander0542 & julianvdhogen"),
    RU("ру\u0441\u0441к\u0438\u0439", "Saph1s, Perhun_Pak, BratishkaErik & stashenko"),
    TR("Türkçe", "TDJisvan, BruilsiozPro & EyuphanMandiraci"),
    UK("україн\u0441ька \u043cо\u0432а", "xlanyleeet"),
    PT_BR("Português", "jvmuller"),
    ZH_TW("\u6F22\u8A9E", "\u6d1b\u4f0a & zisunny104");

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
        return "locale_" + name() + ".yml";
    }
}
