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
package com.djrapitops.plan.identification;

import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.utilities.dev.Untrusted;
import org.eclipse.jetty.http.HttpHeader;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Utility for getting server identifier from different sources.
 *
 * @author AuroraLS3
 */
@Singleton
public class Identifiers {

    protected final DBSystem dbSystem;
    private final UUIDUtility uuidUtility;

    @Inject
    public Identifiers(DBSystem dbSystem, UUIDUtility uuidUtility) {
        this.dbSystem = dbSystem;
        this.uuidUtility = uuidUtility;
    }

    /**
     * Obtain UUID of the server.
     *
     * @param request for Request, URIQuery needs a 'server' parameter.
     * @return UUID of the server.
     * @throws BadRequestException If server parameter is not defined or the server is not in the database.
     */
    public ServerUUID getServerUUID(Request request) {
        String identifier = request.getQuery().get("server")
                .orElseThrow(() -> new BadRequestException("'server' parameter was not defined."));

        Optional<ServerUUID> parsed = UUIDUtility.parseFromString(identifier).map(ServerUUID::from);
        return parsed.orElseGet(() -> getServerUUIDFromName(identifier).orElseThrow(
                () -> new BadRequestException("Given 'server' was not found in the database.")
        ));
    }

    public static Optional<Long> getTimestamp(@Untrusted Request request) {
        try {
            long currentTime = System.currentTimeMillis();
            long timestamp = request.getHeader("X-Plan-Timestamp")
                    .map(Long::parseLong)
                    .orElseGet(() -> request.getQuery().get("timestamp")
                            .map(Long::parseLong)
                            .orElse(currentTime));
            if (currentTime + TimeUnit.SECONDS.toMillis(10L) < timestamp) {
                return Optional.empty();
            }
            return Optional.of(timestamp);
        } catch (@Untrusted NumberFormatException nonNumberTimestamp) {
            throw new BadRequestException("'timestamp' was not a number");
        }
    }

    public static Optional<Long> getEtag(Request request) {
        return request.getHeader(HttpHeader.IF_NONE_MATCH.asString())
                .map(tag -> {
                    try {
                        return Long.parseLong(tag);
                    } catch (NumberFormatException notANumber) {
                        throw new BadRequestException("'" + HttpHeader.IF_NONE_MATCH.asString() + "'-header was not a number. Clear browser cache.");
                    }
                });
    }

    public static Optional<String> getStringEtag(Request request) {
        return request.getHeader(HttpHeader.IF_NONE_MATCH.asString());
    }

    /**
     * Obtain UUID of the server.
     *
     * @param identifier Identifier (name or uuid string) of the server
     * @return UUID of the server.
     * @throws BadRequestException If the server is not in the database.
     */
    public Optional<ServerUUID> getServerUUID(@Untrusted String identifier) {
        Optional<ServerUUID> parsed = UUIDUtility.parseFromString(identifier).map(ServerUUID::from);
        if (parsed.isPresent()) return parsed;
        return getServerUUIDFromName(identifier);
    }

    private Optional<ServerUUID> getServerUUIDFromName(@Untrusted String serverName) {
        return dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverName))
                .map(Server::getUuid);
    }

    /**
     * Obtain UUID of the player.
     *
     * @param request for Request, URIQuery needs a 'player' parameter.
     * @return UUID of the player.
     * @throws BadRequestException If player parameter is not defined or the player is not in the database.
     */
    public UUID getPlayerUUID(@Untrusted Request request) {
        @Untrusted String playerIdentifier = request.getQuery().get("player")
                .orElseThrow(() -> new BadRequestException("'player' parameter was not defined.")).trim();

        Optional<UUID> parsed = UUIDUtility.parseFromString(playerIdentifier);
        return parsed.orElseGet(() -> getPlayerUUIDFromName(playerIdentifier));
    }

    @Nullable
    public UUID getPlayerUUID(String name) {
        return uuidUtility.getUUIDOf(name);
    }

    public Optional<Integer> getPlayerUserId(UUID playerUUID) {
        return dbSystem.getDatabase().query(UserIdentifierQueries.fetchUserId(playerUUID));
    }

    private UUID getPlayerUUIDFromName(@Untrusted String playerName) {
        return dbSystem.getDatabase()
                .query(UserIdentifierQueries.fetchPlayerUUIDOf(playerName))
                .orElseThrow(() -> new BadRequestException("Given 'player' was not found in the database."));
    }
}