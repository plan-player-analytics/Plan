/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.info.parsing;

/**
 * Used for parsing URL strings.
 *
 * @author Rsl1122
 */
public class UrlParser {

    private final String webServerAddress;
    private StringBuilder url;

    public UrlParser(String address) {
        webServerAddress = address;
        url = new StringBuilder();
    }

    public UrlParser relativeProtocol() {
        url.append("//");
        return this;
    }

    public UrlParser httpProtocol() {
        if (url.length() == 0) {
            url.append("http://");
        } else {
            String current = url.toString();
            url = new StringBuilder("./");
            url.append(current.substring(current.indexOf("/")));
        }
        return this;
    }

    public UrlParser httpsProtocol() {
        if (url.length() == 0) {
            url.append("https://");
        } else {
            String current = url.toString();
            url = new StringBuilder("./");
            url.append(current.substring(current.indexOf("/")));
        }
        return this;
    }

    public UrlParser relative() {
        if (url.length() == 0) {
            url.append("./");
        } else {
            String current = url.toString();
            url = new StringBuilder("./");
            url.append(current.substring(current.indexOf("/")));
        }
        return this;
    }

    public UrlParser webAddress() {
        url.append(webServerAddress);
        return this;
    }

    public UrlParser target(String target) {
        if (!target.startsWith("/") && url.charAt(url.length() - 1) != '/') {
            url.append("/");
        }
        url.append(target);
        return this;
    }

    @Override
    public String toString() {
        return url.toString();
    }
}