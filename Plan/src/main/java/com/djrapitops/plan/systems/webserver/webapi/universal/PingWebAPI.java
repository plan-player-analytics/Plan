/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.universal;

import com.djrapitops.plugin.utilities.Compatibility;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.systems.info.BukkitInformationManager;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.io.IOException;
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
        } else if (!plugin.getInfoManager().isUsingAnotherWebServer()) {
            try {
                String webAddress = variables.get("webAddress");
                if (webAddress != null) {
                    ((Plan) plugin).getServerInfoManager().saveBungeeConnectionAddress(webAddress);
                }

                ((BukkitInformationManager) plugin.getInfoManager()).updateConnection();
            } catch (IOException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
        return success();
    }

    @Override
    public void sendRequest(String address) throws WebAPIException {
        if (Compatibility.isBukkitAvailable()) {
            super.sendRequest(address);
        } else {
            addVariable("webAddress", PlanBungee.getInstance().getWebServer().getAccessAddress());
            super.sendRequest(address);
        }
    }

    public void sendRequest(String address, String accessCode) throws WebAPIException {
        addVariable("accessKey", accessCode);
        addVariable("version", MiscUtils.getIPlan().getVersion());
        sendRequest(address);
    }
}