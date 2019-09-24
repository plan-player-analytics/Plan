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
import com.djrapitops.plugin.utilities.Format;

/**
 * Html utility for creating navigation link html.
 *
 * @author Rsl1122
 */
public class NavLink {

    private final Icon icon;
    private final String tabName;
    private final boolean collapsed;

    public NavLink(Icon icon, String tabName) {
        this(icon, tabName, false);
    }

    private NavLink(Icon icon, String tabName, boolean collapsed) {
        this.icon = icon;
        this.tabName = tabName;
        this.collapsed = collapsed;
    }

    public static NavLink main(Icon icon, String tabName) {
        return new NavLink(icon, tabName, false);
    }

    public static NavLink collapsed(Icon icon, String tabName) {
        return new NavLink(icon, tabName, true);
    }

    public String toHtml() {
        String tabID = new Format(tabName).justLetters().lowerCase().toString();
        if (collapsed) {
            return "<a class=\"collapse-item nav-button\" href=\"#tab-" + tabID + "\">" +
                    icon.toHtml() + ' ' +
                    tabName + "</a>";
        }
        return "<li class=\"nav-item nav-button\">" +
                "<a class=\"nav-link\" href=\"#tab-" + tabID + "\">" +
                icon.toHtml() +
                "<span>" + tabName + "</span></a>" +
                "</li>";
    }

}
