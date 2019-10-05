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
package com.djrapitops.plan.delivery.webserver.response.pages;

import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.delivery.rendering.pages.DebugPage;
import com.djrapitops.plan.delivery.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.version.VersionCheckSystem;

import java.io.IOException;

/**
 * WebServer response for /debug-page used for easing issue reporting.
 *
 * @author Rsl1122
 */
public class DebugPageResponse extends ErrorResponse {

    public DebugPageResponse(DebugPage debugPage, VersionCheckSystem versionCheckSystem, PlanFiles files) throws IOException {
        super(versionCheckSystem, files);
        super.setHeader("HTTP/1.1 200 OK");
        super.setTitle(Icon.called("bug") + " Debug Information");
        super.setParagraph(debugPage.toHtml());
        replacePlaceholders();
    }

}
