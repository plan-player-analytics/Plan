package com.djrapitops.plan.system.locale;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.locale.lang.Lang;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents loaded language information.
 *
 * @author Rsl1122
 */
public class Locale extends HashMap<Lang, Message> {

    public static Locale forLangCodeString(String code) throws IOException {
        return forLangCode(LangCode.fromString(code));
    }

    public static Locale forLangCode(LangCode code) throws IOException {
        return new LocaleFileReader(PlanPlugin.getInstance(), code.getFileName()).load();
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

        // Longest first so that entries that contain each other don't partially replace.
        List<Entry<Lang, Message>> entries = entrySet().stream().sorted(
                (one, two) -> Integer.compare(two.getKey().getIdentifier().length(), one.getKey().getIdentifier().length())
        ).collect(Collectors.toList());

        for (Entry<Lang, Message> entry : entries) {
            String defaultValue = entry.getKey().getDefault();
            String replacement = entry.getValue().toString();

            replaced = replaced.replace(defaultValue, replacement);
        }
        return replaced;
    }
}