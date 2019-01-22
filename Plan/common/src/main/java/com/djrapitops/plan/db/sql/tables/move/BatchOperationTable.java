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
package com.djrapitops.plan.db.sql.tables.move;

import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.sql.queries.LargeFetchQueries;
import com.djrapitops.plan.db.sql.tables.Table;
import com.djrapitops.plan.db.sql.tables.UsersTable;

/**
 * A Fake table used to store a lot of big table operations.
 * <p>
 * To use this table create a new BatchOperationTable with both SQLDB objects.
 * {@code SQLDB from; SQLDB to;}
 * {@code fromT = new BatchOperationTable(from);}
 * {@code toT = new BatchOperationTable(to);}
 * {@code fromT.copy(toT);}
 * <p>
 * The copy methods assume that the table has been cleared, or that no duplicate data will be entered for a user.
 * <p>
 * clearTable methods can be used to clear the table beforehand.
 * <p>
 * Server and User tables should be copied first.
 *
 * @author Rsl1122
 */
public class BatchOperationTable extends Table {

    /**
     * Constructor.
     * <p>
     * Call to access copy functionality.
     *
     * @param database Database to copy things from
     * @throws IllegalStateException if database.init has not been called.
     * @throws ClassCastException    if database is not SQLDB.
     */
    public BatchOperationTable(SQLDB database) {
        super("", database);
        if (!db.isOpen()) {
            throw new IllegalStateException("Given Database had not been initialized.");
        }
    }

    @Override
    public void createTable() {
        throw new IllegalStateException("Method not supposed to be used on this table.");
    }

    public void clearTable(Table table) {
        table.removeAllData();
    }

    @Override
    public void removeAllData() {
        db.remove().everything();
    }

    public void copyEverything(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.removeAllData();

        copyServers(toDB);
        copyUsers(toDB);
        copyWorlds(toDB);
        copyTPS(toDB);
        copyWebUsers(toDB);
        copyCommandUse(toDB);
        copyIPsAndGeolocs(toDB);
        copyNicknames(toDB);
        copySessions(toDB);
        copyUserInfo(toDB);
        copyPings(toDB);
    }

    public void copyPings(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getPingTable().insertAllPings(db.query(LargeFetchQueries.fetchAllPingData()));
    }

    public void copyCommandUse(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getCommandUseTable().insertCommandUsage(db.query(LargeFetchQueries.fetchAllCommandUsageData()));
    }

    public void copyIPsAndGeolocs(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getGeoInfoTable().insertAllGeoInfo(db.query(LargeFetchQueries.fetchAllGeoInfoData()));
    }

    public void copyNicknames(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getNicknamesTable().insertNicknames(db.query(LargeFetchQueries.fetchAllNicknameData()));
    }

    public void copyWebUsers(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getSecurityTable().addUsers(db.query(LargeFetchQueries.fetchAllPlanWebUsers()));
    }

    public void copyServers(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getServerTable().insertAllServers(db.query(LargeFetchQueries.fetchPlanServerInformation()).values());
    }

    public void copyTPS(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getTpsTable().insertAllTPS(db.query(LargeFetchQueries.fetchAllTPSData()));
    }

    public void copyUserInfo(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getUserInfoTable().insertUserInfo(db.query(LargeFetchQueries.fetchPerServerUserInformation()));
    }

    public void copyWorlds(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getWorldTable().saveWorlds(db.query(LargeFetchQueries.fetchAllWorldNames()));
    }

    public void copyUsers(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        UsersTable fromTable = db.getUsersTable();
        UsersTable toTable = toDB.db.getUsersTable();

        toTable.insertUsers(db.query(LargeFetchQueries.fetchAllCommonUserInformation()));
        toTable.updateKicked(fromTable.getAllTimesKicked());
    }

    public void copySessions(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getSessionsTable().insertSessions(db.query(LargeFetchQueries.fetchAllSessionsWithKillAndWorldData()), true);
    }
}
