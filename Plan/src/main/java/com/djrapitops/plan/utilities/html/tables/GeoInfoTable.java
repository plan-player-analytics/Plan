/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
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
public class GeoInfoTable extends TableContainer {

    // TODO
    private boolean displayIP;
    private Formatter<DateHolder> yearFormatter;

    public GeoInfoTable(List<GeoInfo> geoInfo) {
        super("IP", "Geolocation", "Last Used");

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
