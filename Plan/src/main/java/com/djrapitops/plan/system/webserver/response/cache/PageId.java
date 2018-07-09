/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response.cache;

import java.util.UUID;

/**
 * Enum class for "magic" ResponseCache identifier values.
 *
 * @author Rsl1122
 */
public enum PageId {

    SERVER("serverPage:"),
    PLAYER("playerPage:"),
    PLAYERS("playersPage"),

    ERROR("error:"),
    FORBIDDEN(ERROR.of("Forbidden")),
    NOT_FOUND(ERROR.of("Not Found")),
    TRUE("true"),
    FALSE("false"),

    JS("js:"),
    CSS("css:"),

    FAVICON_REDIRECT("Redirect:Favicon"),
    AUTH_PROMPT("PromptAuth"),

    PLAYER_PLUGINS_TAB("playerPluginsTab:"),
    NETWORK_CONTENT("networkContent");

    private final String id;

    PageId(String id) {
        this.id = id;
    }

    public String of(String additionalInfo) {
        return id + additionalInfo;
    }

    public String of(UUID uuid) {
        return of(uuid.toString());
    }

    public String id() {
        return id;
    }
}
