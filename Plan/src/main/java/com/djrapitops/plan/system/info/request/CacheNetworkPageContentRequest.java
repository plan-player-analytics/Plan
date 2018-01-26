/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.TransferDatabaseException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.pages.parts.NetworkPageContent;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest for caching Network page parts to ResponseCache of receiving server.
 *
 * SHOULD NOT BE SENT TO BUKKIT
 *
 * @author Rsl1122
 */
public class CacheNetworkPageContentRequest implements CacheRequest {

    private final UUID serverUUID;
    private final String html;

    public CacheNetworkPageContentRequest(UUID serverUUID, String html) {
        Verify.nullCheck(serverUUID, html);
        this.serverUUID = serverUUID;
        this.html = html;
    }

    private CacheNetworkPageContentRequest() {
        serverUUID = null;
        html = null;
    }

    @Override
    public void placeDataToDatabase() throws WebException {
        try {
            Database.getActive().transfer().storeNetworkPageContent(serverUUID, html);
        } catch (DBException e) {
            throw new TransferDatabaseException(e);
        }
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender

        Map<UUID, String> networkPageHtml;
        Map<UUID, String> serverNames;
        try {
            Database database = Database.getActive();
            networkPageHtml = database.transfer().getEncodedNetworkPageContent();
            serverNames = database.fetch().getServerNames();
        } catch (DBException e) {
            throw new TransferDatabaseException(e);
        }

        for (Map.Entry<UUID, String> entry : networkPageHtml.entrySet()) {
            UUID serverUUID = entry.getKey();
            String serverName = serverNames.getOrDefault(serverUUID, "Unknown");
            String html = entry.getValue();

            NetworkPageContent response = (NetworkPageContent)
                    ResponseCache.loadResponse(PageId.NETWORK_CONTENT.id(), NetworkPageContent::new);
            response.addElement(serverName, html);
        }

        InfoSystem.getInstance().updateNetworkPage();

        return DefaultResponses.SUCCESS.get();
    }

    public static CacheNetworkPageContentRequest createHandler() {
        return new CacheNetworkPageContentRequest();
    }
}