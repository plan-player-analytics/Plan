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
package com.djrapitops.plan.storage.database.queries.objects.lookup;

import java.util.List;

/**
 * @author AuroraLS3
 */
public class IdMapper {

    private IdMapper() {
        // Static method class
    }

    public static void mapUserIds(List<? extends UserIdentifiable> userIdentifiables, LookupTable<Integer> userIdLookupTable) {
        userIdentifiables.forEach(
                userIdentifiable -> userIdentifiable.setUserId(
                        userIdLookupTable.find(userIdentifiable.getUserId())
                                .orElse(userIdentifiable.getUserId()))
        );
    }

    public static void mapServerIds(List<? extends ServerIdentifiable> userIdentifiables, LookupTable<Integer> serverIdLookupTable) {
        userIdentifiables.forEach(
                userIdentifiable -> userIdentifiable.setServerId(
                        serverIdLookupTable.find(userIdentifiable.getServerId())
                                .orElse(userIdentifiable.getServerId()))
        );
    }
}
