/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fuzzlemann
 */
public class WebAPIManager {

    private final Map<String, WebAPI> registry;

    /**
     * Constructor used to hide the public constructor
     */
    public WebAPIManager() {
        registry = new HashMap<>();
    }

    public void registerNewAPI(String method, WebAPI api) {
        registry.put(method.toLowerCase(), api);
    }

    public WebAPI getAPI(String method) {
        return registry.get(method.toLowerCase());
    }
}
