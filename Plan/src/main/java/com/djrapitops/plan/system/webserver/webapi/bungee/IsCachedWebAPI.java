/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.webapi.bungee;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.connection.NotFoundException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.webapi.WebAPI;

import java.util.Map;
import java.util.UUID;

/**
 * WebAPI for checking if a page is in webserver cache.
 *
 * @author Rsl1122
 */
@Deprecated
public class IsCachedWebAPI extends WebAPI {

    @Override
    public Response onRequest(PlanPlugin plugin, Map<String, String> variables) {
        try {
            return fail("Deprecated");
        } catch (NullPointerException e) {
            return badRequest(e.toString());
        }
    }

    @Override
    public void sendRequest(String address) throws WebException {
        throw new IllegalStateException("Wrong method call for this WebAPI, call sendRequest(String, UUID, UUID) instead.");
    }

    public boolean isInspectCached(String address, UUID uuid) throws WebException {
        addVariable("uuid", uuid.toString());
        addVariable("target", "inspectPage");
        try {
            super.sendRequest(address);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    public boolean isAnalysisCached(String address, UUID serverUUID) throws WebException {
        addVariable("serverUUID", serverUUID.toString());
        addVariable("target", "analysisPage");
        try {
            super.sendRequest(address);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }
}