/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.ui.webserver.api.bukkit;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.PageCacheHandler;
import main.java.com.djrapitops.plan.ui.webserver.response.Response;
import main.java.com.djrapitops.plan.ui.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.ui.webserver.response.api.SuccessResponse;
import main.java.com.djrapitops.plan.utilities.webserver.api.WebAPI;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

/**
 * @author Fuzzlemann
 */
public class ConfigureWebAPI implements WebAPI {
    @Override
    public Response onResponse(Plan plan, Map<String, String> variables) {
        String key = variables.get("configKey");

        if (key == null) {
            String error = "Config Key null";
            return PageCacheHandler.loadPage(error, () -> new BadRequestResponse(error));
        }

        String value = variables.get("configValue");

        if (value == null) {
            String error = "Config Value null";
            return PageCacheHandler.loadPage(error, () -> new BadRequestResponse(error));
        }

        if (value.equals("null")) {
            value = null;
        }

        FileConfiguration config = plan.getConfig();
        config.set(key, value);
        plan.saveConfig();

        return PageCacheHandler.loadPage("success", SuccessResponse::new);
    }
}
