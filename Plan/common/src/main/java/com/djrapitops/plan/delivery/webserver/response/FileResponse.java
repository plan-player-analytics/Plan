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
package com.djrapitops.plan.delivery.webserver.response;

import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plugin.utilities.Verify;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Response class for returning file contents.
 * <p>
 * Created to remove copy-paste.
 *
 * @author Rsl1122
 */
public class FileResponse extends Response_old {

    public FileResponse(String fileName, PlanFiles files) throws IOException {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(files.getCustomizableResourceOrDefault(fileName).asString());
    }

    public static String format(String fileName) {
        String[] split = StringUtils.split(fileName, '/');
        int i;
        for (i = 0; i < split.length; i++) {
            String s = split[i];
            if (Verify.equalsOne(s, "css", "js", "vendor", "img")) {
                break;
            }
        }
        StringBuilder b = new StringBuilder("web");
        for (int j = i; j < split.length; j++) {
            String s = split[j];
            b.append('/').append(s);
        }
        return b.toString();
    }
}
