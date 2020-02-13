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
import com.djrapitops.plan.delivery.rendering.html.Contributors;
import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.version.VersionCheckSystem;

/**
 * Page to display error stacktrace.
 *
 * @author Rsl1122
 */
public class ErrorMessagePage implements Page {

    private final String template;
    private final String errorTitle;
    private final String errorMsg;

    private final VersionCheckSystem versionCheckSystem;

    public ErrorMessagePage(
            String template, String errorTitle, String errorMsg,
            VersionCheckSystem versionCheckSystem
    ) {
        this.template = template;
        this.errorTitle = errorTitle;
        this.errorMsg = errorMsg;
        this.versionCheckSystem = versionCheckSystem;
    }

    @Override
    public String toHtml() {

        PlaceholderReplacer placeholders = new PlaceholderReplacer();
        placeholders.put("title", Icon.called("exclamation-circle") + " " + errorTitle);
        placeholders.put("titleText", errorTitle);
        placeholders.put("paragraph", errorMsg);
        placeholders.put("version", versionCheckSystem.getUpdateButton().orElse(versionCheckSystem.getCurrentVersionButton()));
        placeholders.put("updateModal", versionCheckSystem.getUpdateModal());
        placeholders.put("contributors", Contributors.generateContributorHtml());
        return placeholders.apply(template);
    }
}