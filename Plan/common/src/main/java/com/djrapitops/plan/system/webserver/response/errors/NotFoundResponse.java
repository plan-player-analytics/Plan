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
package com.djrapitops.plan.system.webserver.response.errors;

import com.djrapitops.plan.system.storage.file.PlanFiles;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.io.IOException;

/**
 * Generic 404 response.
 *
 * @author Rsl1122
 */
public class NotFoundResponse extends ErrorResponse {

    public NotFoundResponse(String msg, VersionCheckSystem versionCheckSystem, PlanFiles files) throws IOException {
        super(versionCheckSystem, files);
        super.setHeader("HTTP/1.1 404 Not Found");
        super.setTitle(Icon.called("map-signs") + " 404 Not Found");
        super.setParagraph(msg);
        super.replacePlaceholders();
    }

}
