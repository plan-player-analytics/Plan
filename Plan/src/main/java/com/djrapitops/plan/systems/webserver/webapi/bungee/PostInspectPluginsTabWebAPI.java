/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bungee;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.systems.info.BungeeInformationManager;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.api.SuccessResponse;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;

import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class PostInspectPluginsTabWebAPI extends WebAPI {
    @Override
    public Response onResponse(IPlan plugin, Map<String, String> variables) {
        String uuidS = variables.get("uuid");
        String sender = variables.get("sender");
        if (Verify.notNull(uuidS, sender)) {
            String error = "uuid or sender not included";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        UUID uuid = UUID.fromString(uuidS);
        UUID serverUUID = UUID.fromString(sender);
        String html = variables.get("html");

        ((BungeeInformationManager) plugin.getInfoManager()).cachePluginsTabContent(serverUUID, uuid, html);

        return PageCache.loadPage("success", SuccessResponse::new);
    }

    @Override
    public void sendRequest(String address) throws WebAPIException {
        throw new IllegalStateException("Wrong method call for this WebAPI, call sendRequest(String, UUID, UUID) instead.");
    }

    public void sendPluginsTab(String address, UUID uuid, String html) throws WebAPIException {
        addVariable("uuid", uuid.toString());
        addVariable("html", html);
        super.sendRequest(address);
    }
}