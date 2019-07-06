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
package com.djrapitops.plan.system.info.request;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Map object that holds {@link InfoRequest} objects used for handling incoming requests.
 * <p>
 * Convenience class for Dagger injection.
 *
 * @author Rsl1122
 */
@Singleton
public class InfoRequests {

    private final InfoRequestFactory.Handlers handlerFactory;

    private final Map<String, InfoRequest> requestHandlers;

    @Inject
    public InfoRequests(InfoRequestFactory.Handlers handlerFactory) {
        this.handlerFactory = handlerFactory;
        this.requestHandlers = new HashMap<>();
    }

    public void initializeRequests() {
        putRequest(handlerFactory.cacheInspectPageRequest());
        putRequest(handlerFactory.cacheInspectPluginsTabRequest());

        putRequest(handlerFactory.generateInspectPageRequest());
        putRequest(handlerFactory.generateInspectPluginsTabRequest());

        putRequest(handlerFactory.saveDBSettingsRequest());
        putRequest(handlerFactory.sendDBSettingsRequest());
        putRequest(handlerFactory.checkConnectionRequest());
    }

    private void putRequest(InfoRequest request) {
        requestHandlers.put(request.getClass().getSimpleName().toLowerCase(), request);
    }

    public InfoRequest get(String name) {
        return requestHandlers.get(name);
    }

    public void clear() {
        requestHandlers.clear();
    }
}