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

import com.djrapitops.plan.delivery.web.resource.WebResource;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents React index.html.
 *
 * @author AuroraLS3
 */
public class ReactPage implements Page {

    private final String basePath;
    private final WebResource reactHtml;

    public ReactPage(String basePath, WebResource reactHtml) {
        this.basePath = basePath;
        this.reactHtml = reactHtml;
    }

    @Override
    public String toHtml() {
        return StringUtils.replaceEach(
                reactHtml.asString(),
                new String[]{"/static", "/pageExtensionApi.js"},
                new String[]{basePath + "/static", basePath + "/pageExtensionApi.js"});
    }

    @Override
    public long lastModified() {
        return reactHtml.getLastModified().orElseGet(System::currentTimeMillis);
    }
}
