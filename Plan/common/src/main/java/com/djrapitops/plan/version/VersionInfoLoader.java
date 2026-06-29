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
package com.djrapitops.plan.version;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * Utility for loading version information from github.
 *
 * @author AuroraLS3
 */
public class VersionInfoLoader {

    private static final String VERSION_TXT_URL =
            "https://raw.githubusercontent.com/plan-player-analytics/Plan/master/versions.txt";

    private VersionInfoLoader() {
        /* Static method class */
    }

    /**
     * Loads version information from github.
     *
     * @return List of VersionInfo, newest version first.
     * @throws IOException                    If site can not be accessed.
     * @throws java.net.MalformedURLException If VERSION_TXT_URL is not valid.
     */
    public static List<VersionInfo> load() throws IOException {
        URL url = URI.create(VERSION_TXT_URL).toURL();

        List<VersionInfo> versionInfo = new ArrayList<>();

        try (Scanner websiteScanner = new Scanner(url.openStream())) {
            while (websiteScanner.hasNextLine()) {
                checkLine(websiteScanner).ifPresent(lineParts -> {
                    boolean release = lineParts[0].equals("REL");
                    String version = lineParts[1];
                    String downloadUrl = lineParts[2];
                    String changeLogUrl = lineParts[3];

                    versionInfo.add(new VersionInfo(release, version, downloadUrl, changeLogUrl));
                });
            }
        }

        Collections.sort(versionInfo);
        return versionInfo;
    }

    private static Optional<String[]> checkLine(Scanner websiteScanner) {
        String line = websiteScanner.nextLine();
        if (!line.startsWith("REL") && !line.startsWith("DEV")) {
            return Optional.empty();
        }
        String[] parts = StringUtils.split(line, '|');
        if (parts.length < 4) {
            return Optional.empty();
        }
        return Optional.of(parts);
    }

}