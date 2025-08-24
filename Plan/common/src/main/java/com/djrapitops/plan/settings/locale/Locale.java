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

import com.djrapitops.plan.settings.locale.lang.Lang;
import com.djrapitops.plan.storage.file.FileResource;
import com.djrapitops.plan.storage.file.PlanFiles;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents loaded language information.
 *
 * @author AuroraLS3
 */
public class Locale extends HashMap<Lang, Message> {

    public static Locale forLangCodeString(PlanFiles files, String code) throws IOException {
        return forLangCode(LangCode.fromString(code), files);
    }

    public static String getStringNullSafe(Locale locale, Lang lang) {
        return locale != null ? locale.getString(lang) : lang.getDefault();
    }

    private LangCode langCode;

    public Locale() {
        this(LangCode.EN);
    }

    public Locale(LangCode langCode) {
        this.langCode = langCode;
    }

    public static Locale forLangCode(LangCode code, PlanFiles files) throws IOException {
        return new LocaleFileReader(files.getResourceFromJar("locale/" + code.getFileName())).load(code);
    }

    public static Locale fromFile(File file) throws IOException {
        return new LocaleFileReader(new FileResource(file.getName(), file)).load(LangCode.CUSTOM);
    }

    public LangCode getLangCode() {
        return langCode;
    }

    @Override
    public Message get(Object key) {
        Message storedValue = super.get(key);
        if (key instanceof Lang && storedValue == null) {
            return new Message(((Lang) key).getDefault());
        } else {
            return storedValue;
        }
    }

    public Optional<Message> getNonDefault(Object key) {
        Message storedValue = super.get(key);
        if (key instanceof Lang && storedValue == null) {
            return Optional.empty();
        } else {
            return Optional.of(storedValue);
        }
    }

    public String getString(Lang key) {
        return get(key).toString();
    }

    public String getString(Lang key, Serializable... values) {
        return get(key).toString(values);
    }

    public String[] getArray(Lang key) {
        return get(key).toArray();
    }

    public String[] getArray(Lang key, Serializable... values) {
        return get(key).toArray(values);
    }

    public void loadFromAnotherLocale(Locale locale) {
        putAll(locale);
        this.langCode = locale.langCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Locale)) return false;
        if (!super.equals(o)) return false;
        Locale locale = (Locale) o;
        return langCode == locale.langCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), langCode);
    }
}