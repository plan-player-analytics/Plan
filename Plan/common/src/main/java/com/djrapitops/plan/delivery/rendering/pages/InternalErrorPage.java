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
package com.djrapitops.plan.delivery.rendering.pages;

import com.djrapitops.plan.delivery.formatting.PlaceholderReplacer;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.exceptions.ExceptionWithContext;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.version.VersionChecker;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.TextStringBuilder;

/**
 * Page to display error stacktrace.
 *
 * @author AuroraLS3
 */
public class InternalErrorPage implements Page {

    private final String template;
    @Untrusted
    private final String errorMsg;
    @Untrusted
    private final Throwable error;

    private final VersionChecker versionChecker;

    public InternalErrorPage(
            String template, String errorMsg, @Untrusted Throwable error,
            VersionChecker versionChecker
    ) {
        this.template = template;
        this.errorMsg = errorMsg;
        this.error = error;
        this.versionChecker = versionChecker;
    }

    @Override
    public String toHtml() {

        PlaceholderReplacer placeholders = new PlaceholderReplacer();
        placeholders.put("title", Icon.called("bug") + " 500 Internal Error occurred");
        placeholders.put("titleText", "500 Internal Error occurred");
        placeholders.put("paragraph", createContent());
        placeholders.put("version", versionChecker.getCurrentVersion());
        return placeholders.apply(template);
    }

    private String createContent() {
        TextStringBuilder paragraph = new TextStringBuilder();
        paragraph.append("Please report this issue here: ");
        paragraph.append(Html.LINK.create("https://github.com/plan-player-analytics/Plan/issues", "Issues"));
        paragraph.append("<br><br><pre>");
        paragraph.append(StringEscapeUtils.escapeHtml4(error.toString())).append(" | ").append(StringEscapeUtils.escapeHtml4(errorMsg));

        if (error instanceof ExceptionWithContext) {
            ((ExceptionWithContext) error).getContext()
                    .ifPresent(context -> paragraph.append(context.getWhatToDo()
                                    .map(whatToDo -> "<br>What to do about it: " + whatToDo)
                                    .orElse("<br>Error message: " + StringEscapeUtils.escapeHtml4(error.getMessage())))
                            .append("<br><br>Related things:<br>")
                            .appendWithSeparators(context.toLines(), "<br>")
                            .append("<br>"));
        }

        for (StackTraceElement element : error.getStackTrace()) {
            paragraph.append("<br>");
            paragraph.append("    ").append(element);
        }
        if (error.getCause() != null) {
            appendCause(error.getCause(), paragraph);
        }

        paragraph.append("</pre>");

        return paragraph.toString();
    }

    private void appendCause(@Untrusted Throwable cause, TextStringBuilder paragraph) {
        paragraph.append("<br>Caused by: ").append(StringEscapeUtils.escapeHtml4(cause.toString()));
        for (StackTraceElement element : cause.getStackTrace()) {
            paragraph.append("<br>");
            paragraph.append("    ").append(element);
        }
        if (cause.getCause() != null) {
            appendCause(cause.getCause(), paragraph);
        }
    }
}