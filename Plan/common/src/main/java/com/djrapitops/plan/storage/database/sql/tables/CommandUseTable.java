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
package com.djrapitops.plan.storage.database.sql.tables;

/**
 * Table information about 'plan_commandusages'.
 *
 * Patches affecting this table:
 * {@link com.djrapitops.plan.storage.database.transactions.patches.Version10Patch}
 *
 * @author Rsl1122
 * @deprecated TODO DELETE AFTER DROPPING TABLE
 */
@Deprecated
public class CommandUseTable {

    public static final String TABLE_NAME = "plan_commandusages";

    private CommandUseTable() {
        /* Static information class */
    }

}
