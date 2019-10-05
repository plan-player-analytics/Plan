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
package com.djrapitops.plan.delivery.webserver.response.errors;

import com.djrapitops.plan.delivery.webserver.response.pages.PageResponse;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.version.VersionCheckSystem;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents generic HTTP Error response that has the page style in it.
 *
 * @author Rsl1122
 */
public class ErrorResponse extends PageResponse {

    private String title;
    private String paragraph;

    private VersionCheckSystem versionCheckSystem;

    public ErrorResponse(VersionCheckSystem versionCheckSystem, PlanFiles files) throws IOException {
        this.versionCheckSystem = versionCheckSystem;
        setContent(files.getCustomizableResourceOrDefault("web/error.html").asString());
    }

    public ErrorResponse(String message) {
        setContent(message);
    }

    public void replacePlaceholders() {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("title", title);
        String[] split = StringUtils.split(title, ">", 3);
        placeholders.put("titleText", split.length == 3 ? split[2] : title);
        placeholders.put("paragraph", paragraph);
        placeholders.put("version", versionCheckSystem.getUpdateButton().orElse(versionCheckSystem.getCurrentVersionButton()));
        placeholders.put("updateModal", versionCheckSystem.getUpdateModal());

        setContent(StringSubstitutor.replace(getContent(), placeholders));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }

    @Override
    public void send(HttpExchange exchange, Locale locale, Theme theme) throws IOException {
        translate(locale::replaceLanguageInHtml);
        fixThemeColors(theme);
        super.send(exchange, locale, theme);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorResponse)) return false;
        if (!super.equals(o)) return false;
        ErrorResponse that = (ErrorResponse) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(paragraph, that.paragraph);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, paragraph);
    }
}
