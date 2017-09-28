/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.universal;

import com.djrapitops.plugin.utilities.Compatibility;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.systems.info.pluginchannel.BukkitPluginChannelListener;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;

import java.util.Map;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class PingWebAPI extends WebAPI {
    @Override
    public Response onRequest(IPlan plugin, Map<String, String> variables) {
        if (Compatibility.isBungeeAvailable()) {
            ((PlanBungee) plugin).getServerInfoManager().serverConnected(UUID.fromString(variables.get("sender")));
        }
        if (Compatibility.isBukkitAvailable() && !plugin.getInfoManager().isUsingAnotherWebServer()) {
            plugin.getInfoManager().attemptConnection();
        }
        return success();
    }

    @Override
    public void sendRequest(String address) throws WebAPIException {
        if (Compatibility.isBukkitAvailable()) {
            String accessKey = BukkitPluginChannelListener.getAccessKey();
            if (accessKey != null) {
                addVariable("accessKey", accessKey);
            }

            super.sendRequest(address);

            if (accessKey != null) {
                BukkitPluginChannelListener.usedAccessKey();
            }
        } else {
            super.sendRequest(address);
        }
    }
}