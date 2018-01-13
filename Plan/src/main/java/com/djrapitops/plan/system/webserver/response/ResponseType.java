/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.response;

/**
 * //TODO Class Javadoc Comment
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
