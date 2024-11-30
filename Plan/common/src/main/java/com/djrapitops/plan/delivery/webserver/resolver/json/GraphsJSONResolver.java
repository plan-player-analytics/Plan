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
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.rendering.json.graphs.GraphJSONCreator;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.cache.AsyncJSONResolverService;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONStorage;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.utilities.dev.Untrusted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Resolves /v1/graph JSON requests.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/graph")
public class GraphsJSONResolver extends JSONResolver {

    private final Identifiers identifiers;
    private final AsyncJSONResolverService jsonResolverService;
    private final GraphJSONCreator graphJSON;

    @Inject
    public GraphsJSONResolver(
            Identifiers identifiers,
            AsyncJSONResolverService jsonResolverService,
            GraphJSONCreator graphJSON
    ) {
        this.identifiers = identifiers;
        this.jsonResolverService = jsonResolverService;
        this.graphJSON = graphJSON;
    }

    @Override
    public Formatter<Long> getHttpLastModifiedFormatter() {return jsonResolverService.getHttpLastModifiedFormatter();}

    @Override
    public boolean canAccess(Request request) {
        @Untrusted String type = request.getQuery().get("type")
                .orElseThrow(() -> new BadRequestException("'type' parameter was not defined."));
        DataID dataID = getDataID(type);
        boolean forServer = request.getQuery().get("server").isPresent();

        List<WebPermission> requiredPermissionOptions = forServer
                ? getRequiredPermission(dataID)
                : getRequiredNetworkPermission(dataID);

        if (requiredPermissionOptions.isEmpty()) return true;
        WebUser user = request.getUser().orElse(new WebUser(""));
        for (WebPermission permissionOption : requiredPermissionOptions) {
            if (user.hasPermission(permissionOption)) return true;
        }
        return false;
    }

    /**
     * Resolves the request.
     *
     * @param request HTTP request, contains all information necessary to resolve the request.
     * @return JSON response.
     * @throws BadRequestException If 'type' parameter is not defined or supported.
     * @throws BadRequestException If 'server' parameter is not defined or server is not found in database.
     */
    @GET
    @Operation(
            description = "Get graph data",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "type", description = "Type of the graph, see https://github.com/plan-player-analytics/Plan/blob/master/Plan/common/src/main/java/com/djrapitops/plan/delivery/webserver/resolver/json/GraphsJSONResolver.java", required = true, examples = {
                            @ExampleObject(value = "performance", description = "Deprecated, use optimizedPerformance"),
                            @ExampleObject("optimizedPerformance"),
                            @ExampleObject("playersOnline"),
                            @ExampleObject("playersOnlineProxies"),
                            @ExampleObject("uniqueAndNew"),
                            @ExampleObject("hourlyUniqueAndNew"),
                            @ExampleObject("serverCalendar"),
                            @ExampleObject("worldPie"),
                            @ExampleObject("activity"),
                            @ExampleObject("geolocation"),
                            @ExampleObject("aggregatedPing"),
                            @ExampleObject("punchCard"),
                            @ExampleObject("serverPie"),
                            @ExampleObject("joinAddressByDay"),
                    }),
                    @Parameter(in = ParameterIn.QUERY, name = "server", description = "Server identifier to get data for", examples = {
                            @ExampleObject("Server 1"),
                            @ExampleObject("1"),
                            @ExampleObject("1fb39d2a-eb82-4868-b245-1fad17d823b3"),
                    }),
                    @Parameter(in = ParameterIn.QUERY, name = "timestamp", description = "Epoch millisecond for the request, newer value is wanted")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Graph data json", content = @Content()),
                    @ApiResponse(responseCode = "400", description = "'type' parameter not given", content = @Content(examples = {
                            @ExampleObject("{\"status\": 400, \"error\": \"'type' parameter was not defined.\"}")
                    })),
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        @Untrusted String type = request.getQuery().get("type")
                .orElseThrow(() -> new BadRequestException("'type' parameter was not defined."));

        DataID dataID = getDataID(type);

        JSONStorage.StoredJSON storedJSON = getGraphJSON(request, dataID);
        return getCachedOrNewResponse(request, storedJSON);
    }

    private JSONStorage.StoredJSON getGraphJSON(@Untrusted Request request, DataID dataID) {
        Optional<Long> timestamp = Identifiers.getTimestamp(request);

        JSONStorage.StoredJSON storedJSON;
        if (request.getQuery().get("server").isPresent()) {
            ServerUUID serverUUID = identifiers.getServerUUID(request); // Can throw BadRequestException
            Function<ServerUUID, Object> generationFunction = theServerUUID -> generateGraphDataJSONOfType(dataID, theServerUUID, request.getQuery());
            if (dataID.isCacheable()) {
                storedJSON = jsonResolverService.resolve(timestamp, dataID, serverUUID, generationFunction);
            } else {
                storedJSON = JSONStorage.StoredJSON.fromObject(generationFunction.apply(serverUUID), System.currentTimeMillis());
            }
        } else {
            // Assume network
            Supplier<Object> generationFunction = () -> generateGraphDataJSONOfType(dataID, request.getQuery());
            if (dataID.isCacheable()) {
                storedJSON = jsonResolverService.resolve(
                        timestamp, dataID, generationFunction
                );
            } else {
                storedJSON = JSONStorage.StoredJSON.fromObject(generationFunction.get(), System.currentTimeMillis());
            }
        }
        return storedJSON;
    }

    private DataID getDataID(@Untrusted String type) {
        switch (type) {
            case "performance":
                return DataID.GRAPH_PERFORMANCE;
            case "optimizedPerformance":
                return DataID.GRAPH_OPTIMIZED_PERFORMANCE;
            case "playersOnline":
                return DataID.GRAPH_ONLINE;
            case "playersOnlineProxies":
                return DataID.GRAPH_ONLINE_PROXIES;
            case "uniqueAndNew":
                return DataID.GRAPH_UNIQUE_NEW;
            case "hourlyUniqueAndNew":
                return DataID.GRAPH_HOURLY_UNIQUE_NEW;
            case "serverCalendar":
                return DataID.GRAPH_CALENDAR;
            case "worldPie":
                return DataID.GRAPH_WORLD_PIE;
            case "activity":
                return DataID.GRAPH_ACTIVITY;
            case "geolocation":
                return DataID.GRAPH_WORLD_MAP;
            case "aggregatedPing":
                return DataID.GRAPH_PING;
            case "punchCard":
                return DataID.GRAPH_PUNCHCARD;
            case "serverPie":
                return DataID.GRAPH_SERVER_PIE;
            case "joinAddressByDay":
                return DataID.JOIN_ADDRESSES_BY_DAY;
            default:
                throw new BadRequestException("unknown 'type' parameter.");
        }
    }

    private List<WebPermission> getRequiredPermission(DataID dataID) {
        switch (dataID) {
            case GRAPH_PERFORMANCE:
                return List.of(WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_PLAYERS_ONLINE,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_TPS,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_CPU,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_RAM,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_CHUNKS,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_DISK
                );
            case GRAPH_PING:
                return List.of(WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_PING,
                        WebPermission.PAGE_NETWORK_PERFORMANCE
                );
            case GRAPH_OPTIMIZED_PERFORMANCE:
                return List.of(WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_PLAYERS_ONLINE,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_TPS,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_CPU,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_RAM,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_CHUNKS,
                        WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS_DISK,
                        WebPermission.PAGE_NETWORK_PERFORMANCE
                );
            case GRAPH_ONLINE:
                return List.of(WebPermission.PAGE_SERVER_OVERVIEW_PLAYERS_ONLINE_GRAPH, WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_ONLINE);
            case GRAPH_UNIQUE_NEW:
                return List.of(WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_DAY_BY_DAY);
            case GRAPH_HOURLY_UNIQUE_NEW:
                return List.of(WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_HOUR_BY_HOUR);
            case GRAPH_CALENDAR:
                return List.of(WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_CALENDAR);
            case GRAPH_PUNCHCARD:
                return List.of(WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_PUNCHCARD);
            case GRAPH_WORLD_PIE:
                return List.of(WebPermission.PAGE_SERVER_SESSIONS_WORLD_PIE);
            case GRAPH_ACTIVITY:
                return List.of(WebPermission.PAGE_SERVER_PLAYERBASE_GRAPHS);
            case GRAPH_WORLD_MAP:
                return List.of(WebPermission.PAGE_SERVER_GEOLOCATIONS_MAP);
            case JOIN_ADDRESSES_BY_DAY:
                return List.of(WebPermission.PAGE_SERVER_JOIN_ADDRESSES_GRAPHS_TIME);
            default:
                return List.of();
        }
    }

    private List<WebPermission> getRequiredNetworkPermission(DataID dataID) {
        switch (dataID) {
            case GRAPH_PERFORMANCE:
            case GRAPH_OPTIMIZED_PERFORMANCE:
            case GRAPH_PING:
                return List.of(WebPermission.PAGE_NETWORK_PERFORMANCE);
            case GRAPH_ACTIVITY:
                return List.of(WebPermission.PAGE_NETWORK_PLAYERBASE_GRAPHS);
            case GRAPH_UNIQUE_NEW:
                return List.of(WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_DAY_BY_DAY);
            case GRAPH_HOURLY_UNIQUE_NEW:
                return List.of(WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_HOUR_BY_HOUR);
            case GRAPH_CALENDAR:
                return List.of(WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_CALENDAR);
            case GRAPH_SERVER_PIE:
                return List.of(WebPermission.PAGE_NETWORK_SESSIONS_SERVER_PIE);
            case GRAPH_WORLD_MAP:
                return List.of(WebPermission.PAGE_NETWORK_GEOLOCATIONS_MAP);
            case GRAPH_ONLINE_PROXIES:
                return List.of(WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_ONLINE);
            case JOIN_ADDRESSES_BY_DAY:
                return List.of(WebPermission.PAGE_NETWORK_JOIN_ADDRESSES_GRAPHS_TIME);
            default:
                return List.of();
        }
    }

    private Object generateGraphDataJSONOfType(DataID id, ServerUUID serverUUID, @Untrusted URIQuery query) {
        switch (id) {
            case GRAPH_PERFORMANCE:
                return graphJSON.performanceGraphJSON(serverUUID);
            case GRAPH_OPTIMIZED_PERFORMANCE:
                return graphJSON.optimizedPerformanceGraphJSON(serverUUID);
            case GRAPH_ONLINE:
                return graphJSON.playersOnlineGraph(serverUUID);
            case GRAPH_UNIQUE_NEW:
                return graphJSON.uniqueAndNewGraphJSON(serverUUID);
            case GRAPH_HOURLY_UNIQUE_NEW:
                return graphJSON.hourlyUniqueAndNewGraphJSON(serverUUID);
            case GRAPH_CALENDAR:
                return graphJSON.serverCalendarJSON(serverUUID);
            case GRAPH_WORLD_PIE:
                return graphJSON.serverWorldPieJSONAsMap(serverUUID);
            case GRAPH_ACTIVITY:
                return graphJSON.activityGraphsJSONAsMap(serverUUID);
            case GRAPH_WORLD_MAP:
                return graphJSON.geolocationGraphsJSONAsMap(serverUUID);
            case GRAPH_PING:
                return graphJSON.pingGraphsJSON(serverUUID);
            case GRAPH_PUNCHCARD:
                return graphJSON.punchCardJSONAsMap(serverUUID);
            case JOIN_ADDRESSES_BY_DAY:
                return joinAddressGraph(serverUUID, query);
            default:
                throw new BadRequestException("Graph type not supported with server-parameter (" + id.name() + ")");
        }
    }

    private Map<String, Object> joinAddressGraph(ServerUUID serverUUID, @Untrusted URIQuery query) {
        try {
            Long after = query.get("after").map(Long::parseLong).orElse(0L);
            Long before = query.get("before").map(Long::parseLong).orElse(System.currentTimeMillis());
            @Untrusted List<String> addressFilter = query.get("addresses").map(s -> StringUtils.split(s, ','))
                    .map(Arrays::asList).orElse(List.of());
            return graphJSON.joinAddressesByDay(serverUUID, after, before, addressFilter);
        } catch (@Untrusted NumberFormatException e) {
            throw new BadRequestException("'after' or 'before' is not a epoch millisecond (number)");
        }
    }

    private Object generateGraphDataJSONOfType(DataID id, @Untrusted URIQuery query) {
        switch (id) {
            case GRAPH_ACTIVITY:
                return graphJSON.activityGraphsJSONAsMap();
            case GRAPH_UNIQUE_NEW:
                return graphJSON.uniqueAndNewGraphJSON();
            case GRAPH_HOURLY_UNIQUE_NEW:
                return graphJSON.hourlyUniqueAndNewGraphJSON();
            case GRAPH_CALENDAR:
                return graphJSON.networkCalendarJSON();
            case GRAPH_SERVER_PIE:
                return graphJSON.serverPreferencePieJSONAsMap();
            case GRAPH_WORLD_MAP:
                return graphJSON.geolocationGraphsJSONAsMap();
            case GRAPH_ONLINE_PROXIES:
                return graphJSON.proxyPlayersOnlineGraphs();
            case JOIN_ADDRESSES_BY_DAY:
                return joinAddressGraph(query);
            default:
                throw new BadRequestException("Graph type not supported without server-parameter (" + id.name() + ")");
        }
    }

    private Map<String, Object> joinAddressGraph(URIQuery query) {
        try {
            Long after = query.get("after").map(Long::parseLong).orElse(0L);
            Long before = query.get("before").map(Long::parseLong).orElse(System.currentTimeMillis());
            @Untrusted List<String> addressFilter = query.get("addresses").map(s -> StringUtils.split(s, ','))
                    .map(Arrays::asList).orElse(List.of());
            return graphJSON.joinAddressesByDay(after, before, addressFilter);
        } catch (@Untrusted NumberFormatException e) {
            throw new BadRequestException("'after' or 'before' is not a epoch millisecond (number)");
        }
    }
}