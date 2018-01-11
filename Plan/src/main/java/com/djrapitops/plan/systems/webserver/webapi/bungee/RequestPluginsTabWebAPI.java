/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.webserver.webapi.bungee;


import com.djrapitops.plan.api.IPlan;
import com.djrapitops.plan.api.exceptions.WebAPIException;
import com.djrapitops.plan.systems.info.server.ServerInfo;
import com.djrapitops.plan.systems.processing.Processor;
import com.djrapitops.plan.systems.webserver.response.Response;
import com.djrapitops.plan.systems.webserver.webapi.WebAPI;
import com.djrapitops.plan.systems.webserver.webapi.bukkit.RequestInspectPluginsTabBukkitWebAPI;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * WebAPI for requesting Bungee Server to request Plugins tab contents from every server.
 * <p>
 * Call: Bukkit to Bungee
 * <p>
 * Bad Requests:
 * - Called a Bukkit Server
 * - Did not include uuid variable
 *
 * @author Rsl1122
 */
public class RequestPluginsTabWebAPI extends WebAPI {
    @Override
    public Response onRequest(IPlan plugin, Map<String, String> variables) {
        if (!Check.isBungeeAvailable()) {
            return badRequest("Called a Bukkit Server");
        }

        String uuidS = variables.get("uuid");
        if (uuidS == null) {
            return badRequest("UUID not included");
        }
        UUID uuid = UUID.fromString(uuidS);

        sendRequestsToBukkitServers(plugin, uuid);
        return success();
    }

    @Override
    public void sendRequest(String address) throws WebAPIException {
        throw new IllegalStateException("Wrong method call for this WebAPI, call sendRequest(String, UUID, UUID) instead.");
    }

    public void sendRequest(String address, UUID uuid) throws WebAPIException {
        addVariable("uuid", uuid.toString());
        super.sendRequest(address);
    }

    public void sendRequestsToBukkitServers(IPlan plugin, UUID uuid) {
        plugin.addToProcessQueue(new Processor<UUID>(uuid) {
            @Override
            public void process() {
                try {
                    List<ServerInfo> bukkitServers = plugin.getDB().getServerTable().getBukkitServers();
                    for (ServerInfo server : bukkitServers) {
                        String webAddress = server.getWebAddress();
                        try {
                            plugin.getWebServer().getWebAPI().getAPI(RequestInspectPluginsTabBukkitWebAPI.class).sendRequest(webAddress, uuid);
                        } catch (WebAPIException ignore) {
                            /* ignored */
                        }
                    }
                } catch (SQLException e) {
                    Log.toLog(this.getClass().getName(), e);
                }
            }
        });
    }
}