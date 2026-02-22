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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author AuroraLS3
 */
public class IdMapper {

    private IdMapper() {
        // Static method class
    }

    public static void mapUserIds(Collection<? extends UserIdentifiable> rows, LookupTable<Integer> userIdLookupTable) {
        rows.forEach(
                userIdentifiable -> userIdentifiable.setUserId(
                        userIdLookupTable.find(userIdentifiable.getUserId())
                                .orElseGet(userIdentifiable::getUserId))
        );
    }

    public static void mapServerIds(Collection<? extends ServerIdentifiable> rows, LookupTable<Integer> serverIdLookupTable) {
        rows.forEach(
                userIdentifiable -> userIdentifiable.setServerId(
                        serverIdLookupTable.find(userIdentifiable.getServerId())
                                .orElseGet(userIdentifiable::getServerId))
        );
    }

    public static void mapJoinAddressIds(Collection<? extends JoinAddressIdentifiable> rows, LookupTable<Integer> joinAddressLookupTable) {
        rows.forEach(
                userIdentifiable -> userIdentifiable.setJoinAddressId(
                        joinAddressLookupTable.find(userIdentifiable.getJoinAddressId())
                                .orElseGet(userIdentifiable::getJoinAddressId))
        );
    }

    public static void mapWorldIds(Collection<? extends WorldIdentifiable> rows, LookupTable<Integer> worldIdLookupTable) {
        rows.forEach(
                userIdentifiable -> userIdentifiable.setWorldId(
                        worldIdLookupTable.find(userIdentifiable.getWorldId())
                                .orElseGet(userIdentifiable::getWorldId))
        );
    }

    public static Map<Integer, List<Integer>> mapGroupPermissionIds(Map<Integer, List<Integer>> permissionsByGroup, LookupTable<Integer> webGroupLookupTable, LookupTable<Integer> webPermissionLookupTable) {
        return permissionsByGroup.entrySet().stream()
                .filter(entry -> webGroupLookupTable.contains(entry.getKey()))
                .collect(Collectors.toMap(groupId -> webGroupLookupTable.find(groupId.getKey()).orElse(null),
                        value -> value.getValue().stream()
                                .map(id -> webPermissionLookupTable.find(id).orElse(null))
                                .filter(Objects::nonNull)
                                .sorted()
                                .collect(Collectors.toList())));
    }

    public static void mapGroupIds(Collection<? extends GroupIdentifiable> rows, LookupTable<Integer> groupIdLookupTable) {
        rows.forEach(
                userIdentifiable -> userIdentifiable.setGroupId(
                        groupIdLookupTable.find(userIdentifiable.getGroupId())
                                .orElseGet(userIdentifiable::getGroupId))
        );
    }
}
