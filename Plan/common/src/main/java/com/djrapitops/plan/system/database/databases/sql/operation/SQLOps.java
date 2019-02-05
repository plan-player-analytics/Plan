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
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.sql.tables.*;

@Deprecated
public class SQLOps {

    protected final SQLDB db;

    protected final UsersTable usersTable;
    protected final UserInfoTable userInfoTable;
    protected final KillsTable killsTable;
    protected final SessionsTable sessionsTable;
    protected final TPSTable tpsTable;
    protected final WorldTimesTable worldTimesTable;
    protected final ServerTable serverTable;
    protected final SettingsTable settingsTable;

    public SQLOps(SQLDB db) {
        this.db = db;

        usersTable = db.getUsersTable();
        userInfoTable = db.getUserInfoTable();
        killsTable = db.getKillsTable();
        sessionsTable = db.getSessionsTable();
        tpsTable = db.getTpsTable();
        worldTimesTable = db.getWorldTimesTable();
        serverTable = db.getServerTable();
        settingsTable = db.getSettingsTable();
    }
}
