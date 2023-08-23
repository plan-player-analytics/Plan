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
package com.djrapitops.plan.delivery.rendering.html;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author AuroraLS3
 */
public enum Html {

    LINK("<a class=\"link\" href=\"${0}\">${1}</a>"),
    LINK_EXTERNAL("<a class=\"link\" rel=\"noopener noreferrer\" target=\"_blank\" href=\"${0}\">${1}</a>");

    private final String html;

    Html(String html) {
        this.html = html;
    }

    /**
     * @return The HTML String
     */
    public String create() {
        return html;
    }

    /**
     * @param replacements The replacement Strings
     * @return The HTML String
     */
    public String create(Serializable... replacements) {
        Map<String, Serializable> replaceMap = new HashMap<>();

        for (int i = 0; i < replacements.length; i++) {
            replaceMap.put(String.valueOf(i), replacements[i]);
        }

        StringSubstitutor sub = new StringSubstitutor(replaceMap);
        sub.setEnableSubstitutionInVariables(false);
        return sub.replace(html);
    }

    public static String encodeToURL(String string) {
        return StringUtils.replace(
                URLEncoder.encode(string, StandardCharsets.UTF_8),
                "+", "%20" // Encoding replaces spaces with +
        );
    }
}
