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
package com.djrapitops.plan.delivery.rendering.html.structure;

import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import org.apache.commons.lang3.StringUtils;

/**
 * Html utility for creating navigation link html.
 *
 * @author AuroraLS3
 */
public class NavLink {

    private final Icon icon;
    private final String tabID;
    private final String tabName;
    private final boolean collapsed;

    private NavLink(Icon icon, String tabID, String tabName, boolean collapsed) {
        this.icon = icon;
        this.tabID = tabID;
        this.tabName = tabName;
        this.collapsed = collapsed;
    }

    public static NavLink main(Icon icon, String tabName) {
        return new NavLink(icon, null, tabName, false);
    }

    public static NavLink main(Icon icon, String tabID, String tabName) {
        return new NavLink(icon, tabID, tabName, false);
    }

    public static NavLink collapsed(Icon icon, String tabName) {
        return new NavLink(icon, null, tabName, true);
    }

    public static NavLink collapsed(Icon icon, String tabID, String tabName) {
        return new NavLink(icon, tabID, tabName, true);
    }

    public static String format(String id) {
        return StringUtils.replaceChars(StringUtils.lowerCase(id), ' ', '-');
    }

    public String toHtml() {
        String usedId = getUsedTabId();
        if (collapsed) {
            return "<a class=\"collapse-item nav-button\" href=\"#tab-" + usedId + "\">" +
                    icon.toHtml() + ' ' +
                    tabName + "</a>";
        }
        return "<li class=\"nav-item nav-button\">" +
                "<a class=\"nav-link\" href=\"#tab-" + usedId + "\">" +
                icon.toHtml() +
                "<span>" + tabName + "</span></a>" +
                "</li>";
    }

    private String getUsedTabId() {
        return format(tabID != null ? tabID : tabName);
    }
}
