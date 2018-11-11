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
package com.djrapitops.plan.system.locale;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.locale.lang.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents loaded language information.
 *
 * @author Rsl1122
 */
public class Locale extends HashMap<Lang, Message> {

    public static Locale forLangCodeString(PlanPlugin plugin, String code) throws IOException {
        return forLangCode(LangCode.fromString(code), plugin);
    }

    public static Locale forLangCode(LangCode code, PlanPlugin plugin) throws IOException {
        return new LocaleFileReader(plugin, code.getFileName()).load();
    }

    public static Locale fromFile(File file) throws IOException {
        return new LocaleFileReader(file).load();
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

    public String getString(Lang key) {
        return get(key).toString();
    }

    public String getString(Lang key, Serializable... values) {
        return get(key).parse(values);
    }

    public String[] getArray(Lang key) {
        return get(key).toArray();
    }

    public String[] getArray(Lang key, Serializable... values) {
        return get(key).toArray(values);
    }

    public void loadFromAnotherLocale(Map<Lang, Message> locale) {
        putAll(locale);
    }

    public String replaceMatchingLanguage(String from) {
        if (isEmpty()) {
            return from;
        }

        String replaced = from;

        Lang[][] langs = new Lang[][]{
                NetworkPageLang.values(),
                PlayerPageLang.values(),
                ServerPageLang.values(),
                CommonHtmlLang.values()
        };

        List<Entry<Lang, Message>> entries = Arrays.stream(langs)
                .flatMap(Arrays::stream)
                .collect(Collectors.toMap(Function.identity(), this::get))
                .entrySet().stream()
                // Longest first so that entries that contain each other don't partially replace.
                .sorted((one, two) -> Integer.compare(
                        two.getKey().getIdentifier().length(),
                        one.getKey().getIdentifier().length()
                )).collect(Collectors.toList());

        for (Entry<Lang, Message> entry : entries) {
            String defaultValue = entry.getKey().getDefault();
            String replacement = entry.getValue().toString();

            replaced = replaced.replace(defaultValue, replacement);
        }
        return replaced;
    }
}