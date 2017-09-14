/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit;

import com.djrapitops.plugin.config.fileconfig.IFileConfig;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.api.SuccessResponse;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;

import java.io.IOException;
import java.util.Map;

/**
 * @author Fuzzlemann
 */
public class ConfigureWebAPI implements WebAPI {
    @Override
    public Response onResponse(IPlan plugin, Map<String, String> variables) {
        String key = variables.get("configKey");

        if (key == null) {
            String error = "Config Key null";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        String value = variables.get("configValue");

        if (value == null) {
            String error = "Config Value null";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        if (value.equals("null")) {
            value = null;
        }

        IFileConfig config = null;
        try {
            config = plugin.getIConfig().getConfig();
            config.set(key, value);
            plugin.getIConfig().save();
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }

        return PageCache.loadPage("success", SuccessResponse::new);
    }
}
