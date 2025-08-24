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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.sql.tables.webuser.SecurityTable;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreWebGroupTransaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Adds permission level based permission groups to group table.
 * <ul>
 *     <li>permission level 0 = access to any pages (no manage)</li>
 *     <li>permission level 1 = access to /players, /query and /player pages</li>
 *     <li>permission level 2 = access to /player/{linked uuid} page</li>
 * </ul>
 *
 * @author AuroraLS3
 */
public class LegacyPermissionLevelGroupsPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return !hasColumn(SecurityTable.TABLE_NAME, "permission_level")
                || query(WebUserQueries.fetchGroupId("legacy_level_100")).isPresent();
    }

    @Override
    protected void applyPatch() {
        executeOther(new StoreWebGroupTransaction("legacy_level_0", Arrays.stream(new WebPermission[]{
                                WebPermission.PAGE_NETWORK,
                                WebPermission.PAGE_SERVER,
                                WebPermission.ACCESS_QUERY,
                                WebPermission.ACCESS_PLAYERS,
                                WebPermission.PAGE_PLAYER,
                                WebPermission.ACCESS_PLAYER,
                                WebPermission.ACCESS_SERVER,
                                WebPermission.ACCESS_NETWORK
                        })
                        .map(WebPermission::getPermission)
                        .collect(Collectors.toList()))
        );
        executeOther(new StoreWebGroupTransaction("legacy_level_1", Arrays.stream(new WebPermission[]{
                                WebPermission.ACCESS_QUERY,
                                WebPermission.ACCESS_PLAYERS,
                                WebPermission.PAGE_PLAYER,
                                WebPermission.ACCESS_PLAYER
                        })
                        .map(WebPermission::getPermission)
                        .collect(Collectors.toList()))
        );
        executeOther(new StoreWebGroupTransaction("legacy_level_2", Arrays.stream(new WebPermission[]{
                                WebPermission.PAGE_PLAYER,
                                WebPermission.ACCESS_PLAYER_SELF
                        })
                        .map(WebPermission::getPermission)
                        .collect(Collectors.toList()))
        );
        executeOther(new StoreWebGroupTransaction("legacy_level_100", Collections.emptyList()));
    }
}
