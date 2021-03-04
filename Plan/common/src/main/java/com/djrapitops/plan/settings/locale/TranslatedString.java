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

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for translating String.
 * <p>
 * Improves performance by avoiding a double for-each loop since this class can be considered final in the lambda
 * expression in {@link Locale#replaceLanguageInHtml(String)}.
 *
 * @author AuroraLS3
 */
class TranslatedString {
    private static final Pattern LINK_MATCHER = Pattern.compile("http(s|)://[\\w.\\-_%/?$#@!()&=]+");

    private final List<TranslatedString> translating = new LinkedList<>();

    TranslatedString(String translating) {
        final Matcher matcher = LINK_MATCHER.matcher(translating);
        int start = 0;
        while (matcher.find()) {
            String link = translating.substring(matcher.start(), matcher.end());
            String prev = translating.substring(start, matcher.start());
            if (!prev.isEmpty()) {
                this.translating.add(new Translatable(prev));
            }
            start = matcher.end();
            this.translating.add(new LockedString(link));
        }
        String remaining = translating.substring(start);
        if (!remaining.isEmpty()) {
            this.translating.add(new Translatable(remaining));
        }
    }

    TranslatedString() {
    }

    public void translate(String replace, String with) {
        for (TranslatedString sub : translating) {
            sub.translate(replace, with);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }

    public void toString(StringBuilder builder) {
        for (TranslatedString sub : translating) {
            sub.toString(builder);
        }
    }

    public int length() {
        int length = 0;
        for (TranslatedString sub : translating) {
            length += sub.length();
        }
        return length;
    }

    static class Translatable extends TranslatedString {

        private String translating;

        Translatable(String translating) {
            this.translating = translating;
        }

        @Override
        public void translate(String replace, String with) {
            translating = StringUtils.replace(translating, replace, with);
        }

        @Override
        public void toString(StringBuilder builder) {
            builder.append(translating);
        }

        @Override
        public int length() {
            return translating.length();
        }
    }

    static class LockedString extends TranslatedString {
        final String text;

        LockedString(String text) {
            this.text = text;
        }

        @Override
        public void translate(String replace, String with) {
        }

        @Override
        public void toString(StringBuilder builder) {
            builder.append(text);
        }

        @Override
        public int length() {
            return text.length();
        }
    }
}