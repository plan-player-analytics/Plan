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
package com.djrapitops.plan.delivery.domain.datatransfer;

import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.CatchingParsers;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic filter that can be constructed from URIQuery.
 *
 * @author AuroraLS3
 */
public class GenericFilter {

    private final @Nullable Long after;
    private final @Nullable Long before;
    private final @Untrusted List<String> serverIdentifiers;
    private final @Nullable UUID playerUUID;
    private List<ServerUUID> serverUUIDs;

    public GenericFilter(@Untrusted URIQuery query) {
        after = query.get("after", CatchingParsers::parseLong)
                .orElseGet(() -> query.get("afterMillisAgo", CatchingParsers::parseLong)
                        .map(value -> System.currentTimeMillis() - value)
                        .orElse(null));
        before = query.get("before", CatchingParsers::parseLong)
                .orElseGet(() -> query.get("beforeMillisAgo", CatchingParsers::parseLong)
                        .map(value -> System.currentTimeMillis() - value)
                        .orElse(null));
        serverIdentifiers = query.get("server", s -> StringUtils.split(s, ','))
                .map(Arrays::asList)
                .orElse(List.of());
        serverUUIDs = serverIdentifiers.stream()
                .map(CatchingParsers::parseServerUUID)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        playerUUID = query.get("player", CatchingParsers::parsePlayerUUID).orElse(null);
    }

    public static GenericFilter of(@Untrusted URIQuery query) {
        return new GenericFilter(query);
    }

    public Optional<Long> getAfter() {
        return Optional.ofNullable(after);
    }

    public Optional<Long> getBefore() {
        return Optional.ofNullable(before);
    }

    public List<ServerUUID> getServerUUIDs() {
        return serverUUIDs;
    }

    public void setServerUUIDs(List<ServerUUID> serverUUIDs) {
        this.serverUUIDs = serverUUIDs;
    }

    public @Untrusted List<String> getServerIdentifiers() {
        return serverIdentifiers;
    }

    public boolean didAllServerIdentifiersParse() {
        return serverIdentifiers.size() == serverUUIDs.size();
    }

    public Optional<UUID> getPlayerUUID() {
        return Optional.ofNullable(playerUUID);
    }
}
