/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.TransferDatabaseException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.webserver.response.Response;
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
            Database.getActive().transfer().playerHtml(player, encodedHtml);
        } catch (DBException e) {
            throw new TransferDatabaseException(e);
        }
    }

    @Override
    public Response handleRequest(Map<String, String> variables) {
        return null;
    }
}