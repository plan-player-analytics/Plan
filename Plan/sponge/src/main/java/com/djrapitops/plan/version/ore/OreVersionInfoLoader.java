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
package com.djrapitops.plan.version.ore;

import com.djrapitops.plan.version.VersionInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility for loading version information from Ore, Sponge's plugin repository
 */
public class OreVersionInfoLoader {
    private static final String ORE_AUTHENTICATE_URL = "https://ore.spongepowered.org/api/v2/authenticate";
    private static final String ORE_VERSIONS_URL = "https://ore.spongepowered.org/api/v2/projects/plan/versions";
    private static final String ORE_DOWNLOAD_URL = "https://ore.spongepowered.org/AuroraLS3/Plan/versions/%s/download";
    private static final String ORE_CHANGE_LOG_URL = "https://ore.spongepowered.org/AuroraLS3/Plan/versions/%s#change-log";

    private OreVersionInfoLoader() {
        /* Static method class */
    }

    /**
     * Loads version information from Ore, using its Web API.
     *
     * @return List of VersionInfo, newest version first.
     * @throws IOException If API can not be accessed.
     */
    public static List<VersionInfo> load() throws IOException {
        List<VersionInfo> versionInfo = new ArrayList<>();

        String session = newOreSession();

        List<OreVersionDto> versions = loadOreVersions(session);
        versions.forEach(i -> {
            boolean isRelease = i.getTags().stream().anyMatch(t -> t.getName().equals("Channel") && t.getData().equals("Release"));
            String spacedVersion = i.getName().replace('-', ' ');
            String download = String.format(ORE_DOWNLOAD_URL, i.getName());
            String changeLog = String.format(ORE_CHANGE_LOG_URL, i.getName());
            versionInfo.add(new VersionInfo(isRelease, spacedVersion, download, changeLog));
        });

        Collections.sort(versionInfo);
        return versionInfo;
    }

    private static List<OreVersionDto> loadOreVersions(String session) throws IOException {
        URL url = URI.create(ORE_VERSIONS_URL).toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", String.format("OreApi session=\"%s\"", session));
            connection.setRequestProperty("User-Agent", "Player Analytics Update Checker");
            connection.connect();
            try (InputStream in = connection.getInputStream()) {
                JsonArray versions = JsonParser.parseString(readInputFully(in)).getAsJsonObject().get("result").getAsJsonArray();

                return new Gson().getAdapter(new TypeToken<List<OreVersionDto>>() {}).fromJsonTree(versions);
            }
        } finally {
            connection.disconnect();
        }
    }

    private static String newOreSession() throws IOException {
        URL url = URI.create(ORE_AUTHENTICATE_URL).toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Player Analytics Update Checker");
            connection.connect();
            try (InputStream in = connection.getInputStream()) {
                return JsonParser.parseString(readInputFully(in)).getAsJsonObject().get("session").getAsString();
            }
        } finally {
            connection.disconnect();
        }
    }

    private static String readInputFully(InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
}