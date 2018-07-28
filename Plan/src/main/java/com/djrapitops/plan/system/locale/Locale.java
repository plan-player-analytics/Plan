package com.djrapitops.plan.system.locale;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.locale.lang.Lang;
import com.djrapitops.plan.system.settings.Settings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Represents loaded language information.
 *
 * @author Rsl1122
 */
public class Locale extends HashMap<Lang, Message> {

    public Locale() {
    }

    public static Locale fromSetting() throws IOException {
        return forLangCodeString(Settings.LOCALE.toString());
    }

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
        if (key instanceof Lang) {
            return getOrDefault(key, new Message(((Lang) key).getDefault()));
        } else {
            return super.get(key);
        }
    }

    public String getString(Lang key) {
        return get(key).toString();
    }

    public String getString(Lang key, String... values) {
        return get(key).parse(values);
    }

    public String[] getArray(Lang key) {
        return get(key).toArray();
    }

    public String[] getArray(Lang key, String... values) {
        return get(key).toArray(values);
    }
}