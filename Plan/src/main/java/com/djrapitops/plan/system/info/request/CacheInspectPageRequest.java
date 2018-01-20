/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.TransferDatabaseException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.webserver.pages.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.pages.InspectPageResponse;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest used to place HTML of a player to ResponseCache.
 *
 * @author Rsl1122
 */
public class CacheInspectPageRequest implements InfoRequest {

    private final UUID player;
    private final String html;

    private CacheInspectPageRequest() {
        player = null;
        html = null;
    }

    public CacheInspectPageRequest(UUID player, String html) {
        Verify.nullCheck(player, html);
        this.player = player;
        this.html = html;
    }

    public static CacheInspectPageRequest createHandler() {
        return new CacheInspectPageRequest();
    }

    @Override
    public void placeDataToDatabase() throws WebException {
        Verify.nullCheck(player, html);

        String encodedHtml = Base64Util.encode(html);
        try {
            Database.getActive().transfer().storePlayerHtml(player, encodedHtml);
        } catch (DBException e) {
            throw new TransferDatabaseException(e);
        }
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender

        try {
            Map<UUID, String> pages = Database.getActive().transfer().getEncodedPlayerHtml();

            for (Map.Entry<UUID, String> entry : pages.entrySet()) {
                UUID uuid = entry.getKey();
                String html = Base64Util.decode(entry.getValue());

                ResponseCache.cacheResponse(PageId.PLAYER.of(uuid), () -> new InspectPageResponse(uuid, html));
            }
        } catch (DBException e) {
            throw new TransferDatabaseException(e);
        }
        return DefaultResponses.SUCCESS.get();
    }
}