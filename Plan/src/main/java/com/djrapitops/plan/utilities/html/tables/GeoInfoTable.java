/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.utilities.FormatUtils;

import java.util.List;

/**
 * Utility Class for creating IP Table for inspect page.
 *
 * @author Rsl1122
 */
public class GeoInfoTable extends TableContainer {

    public GeoInfoTable(List<GeoInfo> geoInfo) {
        super("IP", "Geolocation", "Last Used");

        if (geoInfo.isEmpty()) {
            addRow("No Connections");
        } else {
            addValues(geoInfo);
        }
    }

    private void addValues(List<GeoInfo> geoInfo) {
        for (GeoInfo info : geoInfo) {
            long date = info.getLastUsed();
            addRow(
                    FormatUtils.formatIP(info.getIp()),
                    info.getGeolocation(),
                    date != 0 ? FormatUtils.formatTimeStampYear(date) : "-"
            );
        }
    }
}