/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.pages.parsing;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.keys.NetworkKeys;
import com.djrapitops.plan.data.store.mutators.formatting.PlaceholderReplacer;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.pages.parts.NetworkPageContent;
import com.djrapitops.plan.utilities.file.FileUtil;

/**
 * Html String parser for /network page.
 *
 * @author Rsl1122
 */
public class NetworkPage implements Page {

    @Override
    public String toHtml() throws ParseException {
        try {
            Database database = Database.getActive();
            NetworkContainer networkContainer = database.fetch().getNetworkContainer();

            PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer();
            placeholderReplacer.addAllPlaceholdersFrom(networkContainer,
                    NetworkKeys.VERSION, NetworkKeys.NETWORK_NAME, NetworkKeys.TIME_ZONE,
                    NetworkKeys.PLAYERS_ONLINE, NetworkKeys.PLAYERS_ONLINE_SERIES, NetworkKeys.PLAYERS_TOTAL, NetworkKeys.PLAYERS_GRAPH_COLOR,
                    NetworkKeys.REFRESH_TIME_F, NetworkKeys.RECENT_PEAK_TIME_F, NetworkKeys.ALL_TIME_PEAK_TIME_F,
                    NetworkKeys.PLAYERS_ALL_TIME_PEAK, NetworkKeys.PLAYERS_RECENT_PEAK,
                    NetworkKeys.PLAYERS_DAY, NetworkKeys.PLAYERS_WEEK, NetworkKeys.PLAYERS_MONTH,
                    NetworkKeys.PLAYERS_NEW_DAY, NetworkKeys.PLAYERS_NEW_WEEK, NetworkKeys.PLAYERS_NEW_MONTH,
                    NetworkKeys.WORLD_MAP_SERIES, NetworkKeys.WORLD_MAP_HIGH_COLOR, NetworkKeys.WORLD_MAP_LOW_COLOR,
                    NetworkKeys.COUNTRY_CATEGORIES, NetworkKeys.COUNTRY_SERIES,
                    NetworkKeys.HEALTH_INDEX, NetworkKeys.HEALTH_NOTES,
                    NetworkKeys.ACTIVITY_PIE_SERIES, NetworkKeys.ACTIVITY_STACK_SERIES, NetworkKeys.ACTIVITY_STACK_CATEGORIES
            );
            NetworkPageContent networkPageContent = (NetworkPageContent)
                    ResponseCache.loadResponse(PageId.NETWORK_CONTENT.id(), NetworkPageContent::new);
            placeholderReplacer.put("tabContentServers", networkPageContent.getContents());

            return placeholderReplacer.apply(FileUtil.getStringFromResource("web/network.html"));
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
}