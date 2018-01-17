/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.webapi.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.webapi.WebAPI;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * @author Fuzzlemann
 */
public class IsOnlineWebAPI extends WebAPI {
    @Override
    public Response onRequest(PlanPlugin plugin, Map<String, String> variables) {
        String uuidS = variables.get("uuid");
        if (uuidS == null) {
            return badRequest("UUID not included");
        }
        UUID uuid = UUID.fromString(uuidS);

        Player player = Plan.getInstance().getServer().getPlayer(uuid);

        if (player != null && player.isOnline()) {
            return success();
        } else {
            return fail("Not Online");
        }
    }

    @Override
    public void sendRequest(String address) throws WebException {
        throw new IllegalStateException("Wrong method call for this WebAPI, call sendRequest(String, UUID, UUID) instead.");
    }

    public void sendRequest(String address, UUID uuid) throws WebException {
        addVariable("uuid", uuid.toString());
        super.sendRequest(address);
    }
}
