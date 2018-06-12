/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response;

/**
 * Enum for HTTP content-type response header Strings.
 *
 * @author Rsl1122
 */
public enum ResponseType {
    HTML("text/html;charset=utf-8"),
    CSS("text/css"),
    JSON("application/json"),
    JAVASCRIPT("application/javascript");

    private final String type;

    ResponseType(String type) {
        this.type = type;
    }

    public String get() {
        return type;
    }
}
