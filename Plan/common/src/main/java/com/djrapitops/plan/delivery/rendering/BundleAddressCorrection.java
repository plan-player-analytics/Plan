/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.rendering;

import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * In charge of correcting the root address in the javascript bundle.
 * <p>
 * The javascript bundle assumes everything is hosted at /,
 * but hosting settings affect the address and it could be hosted at a subdirectory like /plan/
 *
 * @author AuroraLS3
 */
@Singleton
public class BundleAddressCorrection {

    private static final String STATIC = "static";
    private static final Pattern JAVASCRIPT_ADDRESS_PATTERN = Pattern.compile("\"(\\./|/?static)(.+?)\\.(json|js|css|png)\"");

    private final PlanConfig config;
    private final Addresses addresses;

    @Inject
    public BundleAddressCorrection(PlanConfig config, Addresses addresses) {
        this.config = config;
        this.addresses = addresses;
    }

    private String getExportBasePath() {
        return addresses.getBasePath(config.get(WebserverSettings.EXTERNAL_LINK));
    }

    private String getWebserverBasePath() {
        String address = addresses.getMainAddress()
                .orElseGet(addresses::getFallbackLocalhostAddress);
        return addresses.getBasePath(address);
    }

    public String correctAddressForWebserver(String content, String fileName) {
        String basePath = getWebserverBasePath();
        return correctAddress(content, fileName, basePath);
    }

    public String correctAddressForExport(String content, String fileName) {
        String basePath = getExportBasePath();
        return correctAddress(content, fileName, basePath);
    }

    // basePath is either empty if the address doesn't have a subdirectory, or a subdirectory.
    @Nullable
    private String correctAddress(String content, String fileName, String basePath) {
        if (fileName.endsWith(".css")) {
            return correctAddressInCss(content, basePath);
        } else if (fileName.endsWith(".js")) {
            return correctAddressInJavascript(content, basePath);
        } else if ("index.html".equals(fileName)) {
            return correctAddressInHtml(content, basePath);
        }
        return content;
    }

    private String correctAddressInHtml(String content, String basePath) {
        String endingSlash = basePath.endsWith("/") ? "" : "/";
        return StringUtils.replaceEach(content,
                new String[]{"src=\"/", "href=\"/"},
                new String[]{"src=\"" + basePath + endingSlash, "href=\"" + basePath + endingSlash});
    }

    private String correctAddressInCss(String content, String basePath) {
        String endingSlash = basePath.endsWith("/") ? "" : "/";
        return Strings.CS.replace(content, "/static", basePath + endingSlash + STATIC);
    }

    private String correctAddressInJavascript(String content, String basePath) {
        int lastIndex = 0;
        StringBuilder output = new StringBuilder();

        Matcher matcher = JAVASCRIPT_ADDRESS_PATTERN.matcher(content);
        while (matcher.find()) {
            String addressStart = matcher.group(1);
            String file = matcher.group(2);
            String extension = matcher.group(3);
            int startIndex = matcher.start();
            int endIndex = matcher.end();

            // If basePath is empty the website is hosted at root of the tree /
            boolean atUrlRoot = basePath.isEmpty();

            // This handles /static and static representation
            boolean startsWithSlash = addressStart.startsWith("/");
            String startingSlash = startsWithSlash ? "/" : "";
            // This handles basePath containing a slash after subdirectory, such as /plan/ instead of /plan
            String endingSlash = basePath.endsWith("/") ? "" : "/";

            // Without subdirectory we can use the address as is, and it doesn't need changes,
            // otherwise we can add the directory to the start.
            String staticReplacement = atUrlRoot
                    ? startingSlash + STATIC
                    : basePath + endingSlash + STATIC;
            String relativeReplacement = atUrlRoot
                    ? "./"
                    : basePath + endingSlash + "static/";

            // Replaces basePath starting slash if the replaced thing does not start with slash
            if (!startsWithSlash && staticReplacement.startsWith("/")) {
                staticReplacement = staticReplacement.substring(1);
            }

            // Replacement examples when basepath is empty, "/plan" or "/plan/"
            // "./Filename-hash.js"       -> "./Filename-hash.js" or "/plan/static/Filename-hash.js"
            // "/static/Filename-hash.js" -> "/static/Filename-hash.js" or "/plan/static/Filename-hash.js"
            // "static/Filename-hash.js"  -> "static/Filename-hash.js" or "plan/static/Filename-hash.js"
            String replacementAddress = Strings.CS.equalsAny(addressStart, "/static", STATIC)
                    ? staticReplacement
                    : relativeReplacement;
            String replacement = '"' + replacementAddress + file + '.' + extension + '"';

            output.append(content, lastIndex, startIndex) // Append non-match
                    .append(replacement); // Append replaced address

            lastIndex = endIndex;
        }
        // Append rest of the content that didn't match
        if (lastIndex < content.length()) {
            output.append(content, lastIndex, content.length());
        }

        return output.toString();
    }

}
