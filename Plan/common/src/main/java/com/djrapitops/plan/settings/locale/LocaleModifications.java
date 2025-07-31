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
import com.djrapitops.plan.settings.locale.lang.Lang;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

/**
 * @author AuroraLS3
 */
public class LocaleModifications {

    private LocaleModifications() {
        /* Static utility class */
    }

    public static void apply(Locale locale) {
        apply(HtmlLang.QUERY_ARE_PLUGIN_GROUP, locale, new ReplaceString("${group}", "{{group}}"));
        apply(HtmlLang.QUERY_ARE_PLUGIN_GROUP, locale, new ReplaceString("${plugin}", "{{plugin}}"));
        apply(HtmlLang.QUERY_RESULTS_MATCH, locale, new ReplaceString("${resultCount}", "{{resultCount}}"));
        apply(HtmlLang.QUERY_RESULTS, locale, new ReplaceString("<", ""));
        apply(HtmlLang.QUERY_TIME_TO, locale, new ReplaceString("</label>", ""));
        apply(HtmlLang.QUERY_TIME_TO, locale, new ReplaceString(">", ""));
        apply(HtmlLang.QUERY_TIME_FROM, locale, new ReplaceString("</label>", ""));
        apply(HtmlLang.QUERY_TIME_FROM, locale, new ReplaceString(">", ""));
        apply(HtmlLang.QUERY_ACTIVITY_ON, locale, new ReplaceString("<span id=\"activity-date\"></span>", "{{activityDate}}"));
        apply(HtmlLang.TEXT_CONTRIBUTORS_THANKS, locale, "<span col=\"col-theme\">", "<1>");
        apply(HtmlLang.TEXT_CONTRIBUTORS_THANKS, locale, "</span>", "</1>");
        apply(HtmlLang.QUERY_SERVERS_MANY, locale, " {number} ", "{{number}}");
        apply(HtmlLang.HELP_ACTIVITY_INDEX_WEEK, locale, " {}", " {{number}}");
    }

    private static void apply(Lang appliesTo, Locale locale, String replace, String with) {
        apply(appliesTo, locale, new ReplaceString(replace, with));
    }

    private static void apply(Lang appliesTo, Locale locale, Function<String, String> function) {
        locale.put(appliesTo, new Message(function.apply(locale.get(appliesTo).toString())));
    }

    static class ReplaceString implements Function<String, String> {
        private final String replace;
        private final String with;

        public ReplaceString(String replace, String with) {
            this.replace = replace;
            this.with = with;
        }

        @Override
        public String apply(String s) {
            return StringUtils.replace(s, replace, with);
        }
    }
}
