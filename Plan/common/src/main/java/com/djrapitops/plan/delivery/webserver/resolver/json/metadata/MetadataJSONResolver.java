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
package com.djrapitops.plan.delivery.webserver.resolver.json.metadata;

import com.djrapitops.plan.delivery.rendering.html.Contributors;
import com.djrapitops.plan.delivery.web.AssetVersions;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.ProxySettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.java.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

@Singleton
@Path("/v1/metadata")
public class MetadataJSONResolver implements NoAuthResolver {

    private final String mainCommand;
    private final PlanFiles files;
    private final PlanConfig config;
    private final Theme theme;
    private final ServerInfo serverInfo;
    private final AssetVersions assetVersions;
    private final PluginLogger logger;

    @Inject
    public MetadataJSONResolver(
            @Named("mainCommandName") String mainCommand,
            PlanFiles files,
            PlanConfig config,
            Theme theme,
            ServerInfo serverInfo,
            AssetVersions assetVersions,
            PluginLogger logger
    ) {
        this.mainCommand = mainCommand;
        this.files = files;
        this.config = config;
        this.theme = theme;
        this.serverInfo = serverInfo;
        this.assetVersions = assetVersions;
        this.logger = logger;
    }

    @GET
    @Operation(
            description = "Get metadata required for displaying Plan React frontend",
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse());
    }

    private Response getResponse() {
        return Response.builder()
                .setJSONContent(Maps.builder(String.class, Object.class)
                        .put("timestamp", System.currentTimeMillis())
                        .put("timeZoneId", config.getTimeZone().getID())
                        .put("timeZoneOffsetHours", config.getTimeZoneOffsetHours())
                        .put("timeZoneOffsetMinutes", config.getTimeZoneOffsetHours() * 60)
                        .put("contributors", Contributors.getContributors())
                        .put("defaultTheme", config.get(DisplaySettings.THEME))
                        .put("availableThemes", getAvailableThemes())
                        .put("gmPieColors", theme.getWorldPieColors())
                        .put("playerHeadImageUrl", config.get(DisplaySettings.PLAYER_HEAD_IMG_URL))
                        .put("isProxy", serverInfo.getServer().isProxy())
                        .put("serverName", serverInfo.getServer().getIdentifiableName())
                        .put("serverUUID", serverInfo.getServer().getUuid().toString())
                        .put("networkName", serverInfo.getServer().isProxy() ? config.get(ProxySettings.NETWORK_NAME) : null)
                        .put("mainCommand", mainCommand)
                        .put("refreshBarrierMs", config.get(WebserverSettings.REDUCED_REFRESH_BARRIER))
                        .put("registrationDisabled", config.isTrue(WebserverSettings.DISABLED_REGISTRATION))
                        .build())
                .build();
    }

    private List<String> getAvailableThemes() {
        Set<String> foundThemes = new HashSet<>();
        try {
            // Add the themes in the jar
            foundThemes.addAll(assetVersions.getThemeNames());
        } catch (IOException e) {
            logger.warn("Could not read themes from jar: " + e.toString());
        }
        try (Stream<java.nio.file.Path> found = Files.list(files.getThemeDirectory())) {
            found.filter(file -> file.toFile().getAbsolutePath().endsWith(".json"))
                    .map(file -> file.getFileName().toString())
                    .map(fileName -> StringUtils.split(fileName, '.')[0])
                    .forEach(foundThemes::add);
        } catch (IOException e) {
            logger.warn("Could not read web_themes directory: " + e.toString());
        }
        List<String> asList = new ArrayList<>(foundThemes);
        asList.sort(String.CASE_INSENSITIVE_ORDER);
        return asList;
    }
}
