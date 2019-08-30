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
package com.djrapitops.plan.system.delivery.webserver.response.errors;

import com.djrapitops.plan.system.delivery.rendering.html.Html;
import com.djrapitops.plan.system.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.system.storage.file.PlanFiles;
import com.djrapitops.plan.system.update.VersionCheckSystem;

import java.io.IOException;

/**
 * @author Rsl1122
 */
public class InternalErrorResponse extends ErrorResponse {

    public InternalErrorResponse(String cause, Throwable e, VersionCheckSystem versionCheckSystem, PlanFiles files) throws IOException {
        super(versionCheckSystem, files);
        super.setHeader("HTTP/1.1 500 Internal Error");

        super.setTitle(Icon.called("bug") + " 500 Internal Error occurred");

        StringBuilder paragraph = new StringBuilder();
        paragraph.append("Please report this issue here: ");
        paragraph.append(Html.LINK.parse("https://github.com/Rsl1122/Plan-PlayerAnalytics/issues", "Issues"));
        paragraph.append("<br><br><pre>");
        paragraph.append(e).append(" | ").append(cause);

        for (StackTraceElement element : e.getStackTrace()) {
            paragraph.append("<br>");
            paragraph.append("    ").append(element);
        }
        if (e.getCause() != null) {
            appendCause(e.getCause(), paragraph);
        }

        paragraph.append("</pre>");

        super.setParagraph(paragraph.toString());
        super.replacePlaceholders();
    }

    private void appendCause(Throwable cause, StringBuilder paragraph) {
        paragraph.append("<br>Caused by: ").append(cause);
        for (StackTraceElement element : cause.getStackTrace()) {
            paragraph.append("<br>");
            paragraph.append("    ").append(element);
        }
        if (cause.getCause() != null) {
            appendCause(cause.getCause(), paragraph);
        }
    }
}
