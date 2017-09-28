/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi;

import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;

import java.util.*;

/**
 * @author Fuzzlemann & Rsl1122
 */
public class WebAPIManager {

    private final Map<String, WebAPI> registry;
    private final Set<String> accessKeys;

    /**
     * Constructor used to hide the public constructor
     */
    public WebAPIManager() {
        registry = new HashMap<>();
        accessKeys = new HashSet<>();
    }

    public void registerNewAPI(WebAPI... api) {
        for (WebAPI webAPI : api) {
            registerNewAPI(webAPI);
        }
    }

    public boolean isAuthorized(String key) {
        return accessKeys.contains(key);
    }

    public void authorize(String key) {
        accessKeys.remove(key);
    }

    public String generateNewAccessKey() throws Exception {
        String key = PassEncryptUtil.createHash(UUID.randomUUID().toString()).split(":")[4];
        accessKeys.add(key);
        return key;
    }

    public void registerNewAPI(WebAPI api) {
        registry.put(api.getClass().getSimpleName().toLowerCase(), api);
    }

    public <T extends WebAPI> T getAPI(Class<T> api) {
        WebAPI webAPI = getAPI(api.getSimpleName());
        return (T) webAPI;
    }

    public WebAPI getAPI(String apiName) {
        return registry.get(apiName.toLowerCase());
    }
}
