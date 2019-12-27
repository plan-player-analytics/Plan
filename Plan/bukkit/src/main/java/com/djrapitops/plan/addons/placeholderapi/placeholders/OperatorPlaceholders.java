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
package com.djrapitops.plan.addons.placeholderapi.placeholders;

import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import org.bukkit.entity.Player;

/**
 * Placeholders about operators.
 *
 * @author aidn5, Rsl1122
 */
public class OperatorPlaceholders extends AbstractPlanPlaceHolder {

    private final DBSystem dbSystem;

    public OperatorPlaceholders(DBSystem dbSystem, ServerInfo serverInfo) {
        super(serverInfo);
        this.dbSystem = dbSystem;
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) throws Exception {
        if ("operators_total".equalsIgnoreCase(params)) {
            return dbSystem.getDatabase().query(PlayerCountQueries.operators(serverUUID())).toString();
        }

        return null;
    }
}
