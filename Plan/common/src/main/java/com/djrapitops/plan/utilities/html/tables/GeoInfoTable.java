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
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
import com.djrapitops.plan.utilities.formatting.Formatter;

import java.util.List;

/**
 * Utility Class for creating IP Table for inspect page.
 *
 * @author Rsl1122
 */
class GeoInfoTable extends TableContainer {

    private final boolean displayIP;
    private final Formatter<DateHolder> yearFormatter;

    GeoInfoTable(List<GeoInfo> geoInfo, boolean displayIP, Formatter<DateHolder> yearFormatter) {
        super("IP", "Geolocation", "Last Used");
        this.displayIP = displayIP;
        this.yearFormatter = yearFormatter;

        if (geoInfo.isEmpty()) {
            addRow("No Connections");
        } else {
            addValues(geoInfo);
        }
    }

    private void addValues(List<GeoInfo> geoInfo) {
        geoInfo.sort(new DateHolderRecentComparator());

        for (GeoInfo info : geoInfo) {
            addRow(
                    displayIP ? info.getIp() : "Hidden (Config)",
                    info.getGeolocation(),
                    yearFormatter.apply(info)
            );
        }
    }
}
