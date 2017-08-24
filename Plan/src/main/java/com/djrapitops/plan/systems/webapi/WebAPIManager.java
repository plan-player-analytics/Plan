/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webapi;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fuzzlemann
 */
public class WebAPIManager {

    /**
     * Constructor used to hide the public constructor
     */
    private WebAPIManager() {
        throw new IllegalStateException("Utility class");
    }

    private static final Map<String, WebAPI> registry = new HashMap<>();

    public static void registerNewAPI(String method, WebAPI api) {
        registry.put(method.toLowerCase(), api);
    }

    public static WebAPI getAPI(String method) {
        return registry.get(method.toLowerCase());
    }
}
