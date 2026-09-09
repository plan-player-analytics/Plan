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

import com.djrapitops.plan.storage.database.sql.tables.TPSTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.AND;

/**
 * @author AuroraLS3
 */
public enum OnlineActivityType {
    ACTIVE(AND + TPSTable.PLAYERS_ONLINE + ">0"),
    IDLE(AND + TPSTable.PLAYERS_ONLINE + "=0"),
    ;

    private final String sql;

    OnlineActivityType(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
