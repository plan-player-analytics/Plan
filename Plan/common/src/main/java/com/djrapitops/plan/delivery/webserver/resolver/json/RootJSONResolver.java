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
import com.djrapitops.plan.identification.Identifiers;

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
    private final CompositeResolver resolver;

    @Inject
    public RootJSONResolver(
            Identifiers identifiers,
            AsyncJSONResolverService asyncJSONResolverService,
            JSONFactory jsonFactory,

            GraphsJSONResolver graphsJSONResolver,
            SessionsJSONResolver sessionsJSONResolver,
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

            WebGroupJSONResolver webGroupJSONResolver,
            WebGroupPermissionJSONResolver webGroupPermissionJSONResolver,
            WebPermissionJSONResolver webPermissionJSONResolver,
            WebGroupSaveJSONResolver webGroupSaveJSONResolver,
            WebGroupDeleteJSONResolver webGroupDeleteJSONResolver
    ) {
        this.identifiers = identifiers;
        this.asyncJSONResolverService = asyncJSONResolverService;

        resolver = CompositeResolver.builder()
                .add("players", playersTableJSONResolver)
                .add("sessions", sessionsJSONResolver)
                .add("kills", playerKillsJSONResolver)
                .add("graph", graphsJSONResolver)
                .add("pingTable", forJSON(DataID.PING_TABLE, jsonFactory::pingPerGeolocation, WebPermission.PAGE_SERVER_GEOLOCATIONS_PING_PER_COUNTRY))
                .add("serverOverview", forJSON(DataID.SERVER_OVERVIEW, serverOverviewJSONCreator, WebPermission.PAGE_SERVER_OVERVIEW))
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
                .add("webGroups", webGroupJSONResolver)
                .add("groupPermissions", webGroupPermissionJSONResolver)
                .add("permissions", webPermissionJSONResolver)
                .add("saveGroupPermissions", webGroupSaveJSONResolver)
                .add("deleteGroup", webGroupDeleteJSONResolver)
                .build();
    }

    private <T> ServerTabJSONResolver<T> forJSON(DataID dataID, ServerTabJSONCreator<T> tabJSONCreator, WebPermission permission) {
        return new ServerTabJSONResolver<>(dataID, permission, identifiers, tabJSONCreator, asyncJSONResolverService);
    }

    public CompositeResolver getResolver() {
        return resolver;
    }
}