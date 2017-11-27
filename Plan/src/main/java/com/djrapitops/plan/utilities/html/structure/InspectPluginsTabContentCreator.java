/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.html.structure;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.additional.InspectContainer;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.systems.info.server.BukkitServerInfoManager;
import main.java.com.djrapitops.plan.utilities.comparators.PluginDataNameComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class InspectPluginsTabContentCreator {

    public static String[] createContent(Map<PluginData, InspectContainer> containers) {
        BukkitServerInfoManager serverInfoManager = Plan.getInstance().getServerInfoManager();
        String serverName = serverInfoManager.getServerName();
        String actualServerName = serverName.equals("Plan") ? "Server " + serverInfoManager.getServerID() : serverName;
        String nav = "<li><a class=\"nav-button\" href=\"javascript:void(0)\">" + actualServerName + "</a></li>";

        StringBuilder tab = new StringBuilder();
        tab.append("<div class=\"tab\"><div class=\"row clearfix\">");

        List<PluginData> order = new ArrayList<>(containers.keySet());
        order.sort(new PluginDataNameComparator());

        for (PluginData pluginData : order) {
            InspectContainer container = containers.get(pluginData);
            AnalysisPluginsTabContentCreator.appendThird(pluginData, container, tab);
        }

        tab.append("</div></div>");

        return new String[]{nav, tab.toString()};
    }

}