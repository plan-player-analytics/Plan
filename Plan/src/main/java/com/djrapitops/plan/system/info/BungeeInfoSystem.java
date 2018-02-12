/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.api.exceptions.connection.WebFailException;
import com.djrapitops.plan.system.info.connection.BungeeConnectionSystem;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.pages.parsing.NetworkPage;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.pages.AnalysisPageResponse;

/**
 * InfoSystem for Bungee.
 *
 * @author Rsl1122
 */
public class BungeeInfoSystem extends InfoSystem {

    public BungeeInfoSystem() {
        super(new BungeeConnectionSystem());
    }

    @Override
    public void runLocally(InfoRequest infoRequest) throws WebException {
        // runLocally is called when ConnectionSystem has no servers.
        throw new NoServersException("No servers were available to process this request (Local attempt): " + infoRequest.getClass().getSimpleName());
    }

    @Override
    public void updateNetworkPage() throws WebException {
        try {
            String html = new NetworkPage().toHtml();
            ResponseCache.cacheResponse(PageId.SERVER.of(ServerInfo.getServerUUID()), () -> new AnalysisPageResponse(html));
        } catch (ParseException e) {
            throw new WebFailException("Exception during Network Page Parsing", e);
        }
    }
}