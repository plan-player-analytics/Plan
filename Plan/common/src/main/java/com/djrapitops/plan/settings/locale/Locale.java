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

import com.djrapitops.plan.settings.locale.lang.HtmlLang;
import com.djrapitops.plan.settings.locale.lang.JSLang;
import com.djrapitops.plan.settings.locale.lang.Lang;
import com.djrapitops.plan.storage.file.FileResource;
import com.djrapitops.plan.storage.file.PlanFiles;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents loaded language information.
 *
 * @author AuroraLS3
 */
public class Locale extends HashMap<Lang, Message> {

    private static final Pattern FIND_SCRIPT = Pattern.compile("(<script id=[\"|'].*[\"|']>[\\s\\S]*?</script>|<script>[\\s\\S]*?</script>|<script src=[\"|'].*[\"|']></script>|<link [\\s\\S]*?>)");

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

    public String replaceLanguageInHtml(String from) {
        if (isEmpty()) {
            return from;
        }

        Matcher scriptMatcher = FIND_SCRIPT.matcher(from);
        List<String> foundScripts = new ArrayList<>();
        while (scriptMatcher.find()) {
            foundScripts.add(scriptMatcher.toMatchResult().group(0));
        }

        TranslatedString translated = new TranslatedString(from);
        Arrays.stream(HtmlLang.values())
                // Longest first so that entries that contain each other don't partially replace.
                .sorted((one, two) -> Integer.compare(
                        two.getIdentifier().length(),
                        one.getIdentifier().length()
                ))
                .forEach(lang -> getNonDefault(lang).ifPresent(replacement ->
                        translated.translate(lang.getDefault(), replacement.toString()))
                );

        StringBuilder complete = new StringBuilder(translated.length());

        String[] parts = FIND_SCRIPT.split(translated.toString());
        for (int i = 0; i < parts.length; i++) {
            complete.append(parts[i]);
            if (i < parts.length - 1) {
                complete.append(replaceLanguageInJavascript(foundScripts.get(i)));
            }
        }

        return complete.toString();
    }

    public String replaceLanguageInJavascript(String from) {
        if (isEmpty()) {
            return from;
        }

        TranslatedString translated = new TranslatedString(from);
        Arrays.stream(JSLang.values())
                // Longest first so that entries that contain each other don't partially replace.
                .sorted((one, two) -> Integer.compare(
                        two.getIdentifier().length(),
                        one.getIdentifier().length()
                ))
                .forEach(lang -> getNonDefault(lang).ifPresent(replacement ->
                        translated.translate(lang.getDefault(), replacement.toString()))
                );

        for (Lang extra : new Lang[]{
                HtmlLang.UNIT_NO_DATA,
                HtmlLang.TITLE_WORLD_PLAYTIME,
//                HtmlLang.LABEL_OPERATOR,
//                HtmlLang.LABEL_BANNED,
                HtmlLang.SIDE_SESSIONS,
                HtmlLang.LABEL_PLAYTIME,
                HtmlLang.LABEL_AFK_TIME,
                HtmlLang.LABEL_LONGEST_SESSION,
                HtmlLang.LABEL_SESSION_MEDIAN,
                HtmlLang.LABEL_PLAYER_KILLS,
                HtmlLang.LABEL_MOB_KILLS,
                HtmlLang.LABEL_DEATHS,
                HtmlLang.LABEL_PLAYERS_ONLINE,
                HtmlLang.LABEL_REGISTERED,
                HtmlLang.TITLE_SERVER,
                HtmlLang.TITLE_LENGTH,
                HtmlLang.TITLE_AVG_PING,
                HtmlLang.TITLE_BEST_PING,
                HtmlLang.TITLE_WORST_PING,
                HtmlLang.LABEL_FREE_DISK_SPACE,
                HtmlLang.LABEL_NEW_PLAYERS,
                HtmlLang.LABEL_UNIQUE_PLAYERS,
                HtmlLang.LABEL_ACTIVE_PLAYTIME,
                HtmlLang.LABEL_AFK_TIME,
                HtmlLang.LABEL_AVG_SESSION_LENGTH,
                HtmlLang.LABEL_AVG_PLAYTIME,
                HtmlLang.LABEL_AVG_ACTIVE_PLAYTIME,
                HtmlLang.LABEL_AVG_AFK_TIME,
                HtmlLang.LABEL_AVG_PLAYTIME,
                HtmlLang.SIDE_GEOLOCATIONS,
                HtmlLang.LABEL_PER_PLAYER,
                HtmlLang.TITLE_JOIN_ADDRESSES

        }) {
            getNonDefault(extra).ifPresent(replacement ->
                    translated.translate(extra.getDefault(), replacement.toString()));
        }

        return translated.toString();
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