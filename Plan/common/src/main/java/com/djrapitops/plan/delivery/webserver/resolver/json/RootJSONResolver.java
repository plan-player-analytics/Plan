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
package com.djrapitops.plan.delivery.webserver.resolver.json;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.rendering.json.*;
import com.djrapitops.plan.delivery.web.resolver.CompositeResolver;
import com.djrapitops.plan.delivery.webserver.cache.AsyncJSONResolverService;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.http.WebServer;
import com.djrapitops.plan.delivery.webserver.resolver.json.metadata.*;
import com.djrapitops.plan.delivery.webserver.resolver.json.plugins.ExtensionJSONResolver;
import com.djrapitops.plan.delivery.webserver.resolver.json.plugins.PluginHistoryJSONResolver;
import com.djrapitops.plan.delivery.webserver.resolver.json.query.FiltersJSONResolver;
import com.djrapitops.plan.delivery.webserver.resolver.json.query.QueryJSONResolver;
import com.djrapitops.plan.delivery.webserver.resolver.json.theme.DeleteThemeJSONResolver;
import com.djrapitops.plan.delivery.webserver.resolver.json.theme.SaveThemeJSONResolver;
import com.djrapitops.plan.delivery.webserver.resolver.json.theme.ThemeJSONResolver;
import com.djrapitops.plan.delivery.webserver.resolver.json.webgroup.*;
import com.djrapitops.plan.identification.Identifiers;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Root resolver for JSON requests, resolves /v1/ URLs.
 *
 * @author AuroraLS3
 */
@Singleton
public class RootJSONResolver {

    private final Identifiers identifiers;
    private final AsyncJSONResolverService asyncJSONResolverService;
    private final Lazy<WebServer> webServer;
    private final WebGroupJSONResolver webGroupJSONResolver;
    private final WebGroupPermissionJSONResolver webGroupPermissionJSONResolver;
    private final WebPermissionJSONResolver webPermissionJSONResolver;
    private final WebGroupSaveJSONResolver webGroupSaveJSONResolver;
    private final WebGroupDeleteJSONResolver webGroupDeleteJSONResolver;

    private final CompositeResolver.Builder readOnlyResourcesBuilder;
    private final StorePreferencesJSONResolver storePreferencesJSONResolver;
    private final PluginHistoryJSONResolver pluginHistoryJSONResolver;
    private final SaveThemeJSONResolver saveThemeJSONResolver;
    private final DeleteThemeJSONResolver deleteThemeJSONResolver;
    private CompositeResolver resolver;

    @Inject
    public RootJSONResolver(
            Identifiers identifiers,
            AsyncJSONResolverService asyncJSONResolverService,
            JSONFactory jsonFactory,
            Lazy<WebServer> webServer,

            GraphsJSONResolver graphsJSONResolver,
            SessionsJSONResolver sessionsJSONResolver,
            PlayersJSONResolver playersJSONResolver,
            PlayersTableJSONResolver playersTableJSONResolver,
            ServerOverviewJSONCreator serverOverviewJSONCreator,
            OnlineActivityOverviewJSONCreator onlineActivityOverviewJSONCreator,
            SessionsOverviewJSONCreator sessionsOverviewJSONCreator,
            PlayerKillsJSONResolver playerKillsJSONResolver,
            PvPPvEJSONCreator pvPPvEJSONCreator,
            PlayerBaseOverviewJSONCreator playerBaseOverviewJSONCreator,
            PerformanceJSONCreator performanceJSONCreator,
            ErrorsJSONResolver errorsJSONResolver,
            LocaleJSONResolver localeJSONResolver,

            PlayerJSONResolver playerJSONResolver,
            NetworkJSONResolver networkJSONResolver,
            FiltersJSONResolver filtersJSONResolver,
            QueryJSONResolver queryJSONResolver,
            VersionJSONResolver versionJSONResolver,
            MetadataJSONResolver metadataJSONResolver,
            NetworkMetadataJSONResolver networkMetadataJSONResolver,
            WhoAmIJSONResolver whoAmIJSONResolver,
            ServerIdentityJSONResolver serverIdentityJSONResolver,
            ExtensionJSONResolver extensionJSONResolver,
            RetentionJSONResolver retentionJSONResolver,
            PlayerJoinAddressJSONResolver playerJoinAddressJSONResolver,
            PluginHistoryJSONResolver pluginHistoryJSONResolver,
            AllowlistJSONResolver allowlistJSONResolver,
            PlayersOnlineJSONResolver playersOnlineJSONResolver,

            ThemeJSONResolver themeJSONResolver,
            SaveThemeJSONResolver saveThemeJSONResolver,
            DeleteThemeJSONResolver deleteThemeJSONResolver,

            PreferencesJSONResolver preferencesJSONResolver,
            StorePreferencesJSONResolver storePreferencesJSONResolver,

            WebGroupJSONResolver webGroupJSONResolver,
            WebGroupPermissionJSONResolver webGroupPermissionJSONResolver,
            WebPermissionJSONResolver webPermissionJSONResolver,
            WebGroupSaveJSONResolver webGroupSaveJSONResolver,
            WebGroupDeleteJSONResolver webGroupDeleteJSONResolver
    ) {
        this.identifiers = identifiers;
        this.asyncJSONResolverService = asyncJSONResolverService;

        readOnlyResourcesBuilder = CompositeResolver.builder()
                .add("players", playersJSONResolver)
                .add("playersTable", playersTableJSONResolver)
                .add("sessions", sessionsJSONResolver)
                .add("kills", playerKillsJSONResolver)
                .add("graph", graphsJSONResolver)
                .add("pingTable", forJSON(DataID.PING_TABLE, jsonFactory::pingPerGeolocation, WebPermission.PAGE_SERVER_GEOLOCATIONS_PING_PER_COUNTRY))
                .add("serverOverview", forJSON(DataID.SERVER_OVERVIEW, serverOverviewJSONCreator, WebPermission.PAGE_SERVER_OVERVIEW_NUMBERS))
                .add("onlineOverview", forJSON(DataID.ONLINE_OVERVIEW, onlineActivityOverviewJSONCreator, WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_OVERVIEW))
                .add("sessionsOverview", forJSON(DataID.SESSIONS_OVERVIEW, sessionsOverviewJSONCreator, WebPermission.PAGE_SERVER_SESSIONS_OVERVIEW))
                .add("playerVersus", forJSON(DataID.PVP_PVE, pvPPvEJSONCreator, WebPermission.PAGE_SERVER_PLAYER_VERSUS_OVERVIEW))
                .add("playerbaseOverview", forJSON(DataID.PLAYERBASE_OVERVIEW, playerBaseOverviewJSONCreator, WebPermission.PAGE_SERVER_PLAYERBASE_OVERVIEW))
                .add("performanceOverview", forJSON(DataID.PERFORMANCE_OVERVIEW, performanceJSONCreator, WebPermission.PAGE_SERVER_PERFORMANCE_OVERVIEW))
                .add("player", playerJSONResolver)
                .add("network", networkJSONResolver.getResolver())
                .add("filters", filtersJSONResolver)
                .add("query", queryJSONResolver)
                .add("errors", errorsJSONResolver)
                .add("version", versionJSONResolver)
                .add("locale", localeJSONResolver)
                .add("metadata", metadataJSONResolver)
                .add("networkMetadata", networkMetadataJSONResolver)
                .add("serverIdentity", serverIdentityJSONResolver)
                .add("whoami", whoAmIJSONResolver)
                .add("extensionData", extensionJSONResolver)
                .add("retention", retentionJSONResolver)
                .add("joinAddresses", playerJoinAddressJSONResolver)
                .add("preferences", preferencesJSONResolver)
                .add("gameAllowlistBounces", allowlistJSONResolver)
                .add("theme", themeJSONResolver)
                .add("playersOnline", playersOnlineJSONResolver);

        this.webServer = webServer;
        // These endpoints require authentication to be enabled.
        this.pluginHistoryJSONResolver = pluginHistoryJSONResolver;
        this.webGroupJSONResolver = webGroupJSONResolver;
        this.webGroupPermissionJSONResolver = webGroupPermissionJSONResolver;
        this.webPermissionJSONResolver = webPermissionJSONResolver;
        this.webGroupSaveJSONResolver = webGroupSaveJSONResolver;
        this.webGroupDeleteJSONResolver = webGroupDeleteJSONResolver;
        this.storePreferencesJSONResolver = storePreferencesJSONResolver;
        this.saveThemeJSONResolver = saveThemeJSONResolver;
        this.deleteThemeJSONResolver = deleteThemeJSONResolver;
    }

    private <T> ServerTabJSONResolver<T> forJSON(DataID dataID, ServerTabJSONCreator<T> tabJSONCreator, WebPermission permission) {
        return new ServerTabJSONResolver<>(dataID, permission, identifiers, tabJSONCreator, asyncJSONResolverService);
    }

    public CompositeResolver getResolver() {
        if (resolver == null) {
            if (webServer.get().isAuthRequired()) {
                resolver = readOnlyResourcesBuilder
                        .add("webGroups", webGroupJSONResolver)
                        .add("groupPermissions", webGroupPermissionJSONResolver)
                        .add("permissions", webPermissionJSONResolver)
                        .add("saveGroupPermissions", webGroupSaveJSONResolver)
                        .add("deleteGroup", webGroupDeleteJSONResolver)
                        .add("storePreferences", storePreferencesJSONResolver)
                        .add("pluginHistory", pluginHistoryJSONResolver)
                        .add("saveTheme", saveThemeJSONResolver)
                        .add("deleteTheme", deleteThemeJSONResolver)
                        .build();
            } else {
                resolver = readOnlyResourcesBuilder.build();
            }
        }

        return resolver;
    }
}