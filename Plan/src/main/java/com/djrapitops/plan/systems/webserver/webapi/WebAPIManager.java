/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fuzzlemann & Rsl1122
 */
public class WebAPIManager {

    private final Map<String, WebAPI> registry;

    /**
     * Constructor used to hide the public constructor
     */
    public WebAPIManager() {
        registry = new HashMap<>();
    }

    public void registerNewAPI(WebAPI api) {
        registry.put(api.getClass().getSimpleName().toLowerCase(), api);
    }

    public WebAPI getAPI(Class api) {
        return getAPI(api.getSimpleName());
    }

    public WebAPI getAPI(String apiName) {
        return registry.get(apiName.toLowerCase());
    }
}
