/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bungee;

import com.djrapitops.plugin.utilities.Compatibility;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfo;
import main.java.com.djrapitops.plan.systems.processing.Processor;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.api.SuccessResponse;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit.RequestInspectPluginsTabBukkitWebAPI;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class RequestPluginsTabWebAPI extends WebAPI {
    @Override
    public Response onResponse(IPlan plugin, Map<String, String> variables) {
        if (!Compatibility.isBungeeAvailable()) {
            String error = "Called a Bukkit Server";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        String uuidS = variables.get("uuid");
        if (uuidS == null) {
            String error = "UUID not included";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }
        UUID uuid = UUID.fromString(uuidS);

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
        return PageCache.loadPage("success", SuccessResponse::new);
    }

    @Override
    public void sendRequest(String address) throws WebAPIException {
        throw new IllegalStateException("Wrong method call for this WebAPI, call sendRequest(String, UUID, UUID) instead.");
    }

    public void sendRequest(String address, UUID uuid) throws WebAPIException {
        addVariable("uuid", uuid.toString());
        super.sendRequest(address);
    }
}