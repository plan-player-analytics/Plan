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
import com.djrapitops.plan.storage.database.transactions.webuser.StoreWebGroupTransaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Adds default groups to plan_web_group table.
 *
 * @author AuroraLS3
 */
public class WebGroupDefaultGroupsPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return query(WebUserQueries.fetchGroupId("no_access")).isPresent();
    }

    @Override
    protected void applyPatch() {
        executeOther(new StoreWebGroupTransaction("admin", Arrays.stream(new WebPermission[]{
                                WebPermission.PAGE,
                                WebPermission.ACCESS,
                                WebPermission.MANAGE_GROUPS,
                        WebPermission.MANAGE_USERS,
                        WebPermission.MANAGE_THEMES
                        })
                        .map(WebPermission::getPermission)
                        .collect(Collectors.toList()))
        );
        executeOther(new StoreWebGroupTransaction("read_all", Arrays.stream(new WebPermission[]{
                                WebPermission.PAGE_NETWORK,
                                WebPermission.PAGE_SERVER,
                                WebPermission.ACCESS_QUERY,
                                WebPermission.ACCESS_PLAYERS,
                                WebPermission.PAGE_PLAYER,
                                WebPermission.ACCESS_PLAYER,
                                WebPermission.ACCESS_RAW_PLAYER_DATA,
                                WebPermission.ACCESS_SERVER,
                                WebPermission.ACCESS_NETWORK
                        })
                        .map(WebPermission::getPermission)
                        .collect(Collectors.toList()))
        );
        executeOther(new StoreWebGroupTransaction("player_analyst", Arrays.stream(new WebPermission[]{
                                WebPermission.ACCESS_QUERY,
                                WebPermission.ACCESS_PLAYERS,
                                WebPermission.PAGE_PLAYER,
                                WebPermission.ACCESS_PLAYER,
                                WebPermission.ACCESS_RAW_PLAYER_DATA
                        })
                        .map(WebPermission::getPermission)
                        .collect(Collectors.toList()))
        );
        executeOther(new StoreWebGroupTransaction("player", Arrays.stream(new WebPermission[]{
                                WebPermission.PAGE_PLAYER,
                                WebPermission.ACCESS_PLAYER_SELF,
                                WebPermission.ACCESS_RAW_PLAYER_DATA
                        })
                        .map(WebPermission::getPermission)
                        .collect(Collectors.toList()))
        );
        executeOther(new StoreWebGroupTransaction("no_access", Collections.emptyList()));
    }
}
