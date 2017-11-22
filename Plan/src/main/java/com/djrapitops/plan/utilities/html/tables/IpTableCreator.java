/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.html.tables;

import main.java.com.djrapitops.plan.data.GeoInfo;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.html.Html;

import java.util.List;

/**
 * Utility Class for creating Actions Table for inspect page.
 *
 * @author Rsl1122
 */
public class IpTableCreator {


    public IpTableCreator() {
        throw new IllegalStateException("Utility class");
    }

    public static String createTable(List<GeoInfo> geoInfo) {
        StringBuilder html = new StringBuilder();
        if (geoInfo.isEmpty()) {
            html.append(Html.TABLELINE_3.parse("No Connections", "-", "-"));
        } else {
            int i = 0;
            for (GeoInfo info : geoInfo) {
                html.append(Html.TABLELINE_3.parse(
                        FormatUtils.formatIP(info.getIp()),
                        info.getGeolocation(),
                        FormatUtils.formatTimeStampYear(info.getDate())
                ));

                i++;
            }
        }
        return html.toString();
    }
}