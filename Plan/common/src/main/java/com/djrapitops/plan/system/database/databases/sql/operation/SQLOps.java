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

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.*;

public class SQLOps {

    protected final SQLDB db;

    protected final UsersTable usersTable;
    protected final UserInfoTable userInfoTable;
    protected final KillsTable killsTable;
    protected final NicknamesTable nicknamesTable;
    protected final SessionsTable sessionsTable;
    protected final GeoInfoTable geoInfoTable;
    protected final CommandUseTable commandUseTable;
    protected final TPSTable tpsTable;
    protected final SecurityTable securityTable;
    protected final WorldTable worldTable;
    protected final WorldTimesTable worldTimesTable;
    protected final ServerTable serverTable;
    protected final TransferTable transferTable;
    protected final PingTable pingTable;

    public SQLOps(SQLDB db) {
        this.db = db;

        usersTable = db.getUsersTable();
        userInfoTable = db.getUserInfoTable();
        killsTable = db.getKillsTable();
        nicknamesTable = db.getNicknamesTable();
        sessionsTable = db.getSessionsTable();
        geoInfoTable = db.getGeoInfoTable();
        commandUseTable = db.getCommandUseTable();
        tpsTable = db.getTpsTable();
        securityTable = db.getSecurityTable();
        worldTable = db.getWorldTable();
        worldTimesTable = db.getWorldTimesTable();
        serverTable = db.getServerTable();
        transferTable = db.getTransferTable();
        pingTable = db.getPingTable();
    }
}
