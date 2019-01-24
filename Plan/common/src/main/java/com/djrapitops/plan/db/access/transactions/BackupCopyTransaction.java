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
package com.djrapitops.plan.db.access.transactions;

import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.sql.queries.LargeFetchQueries;
import com.djrapitops.plan.db.sql.queries.LargeStoreQueries;
import com.djrapitops.plan.db.sql.tables.UsersTable;

/**
 * Transaction that performs a clear + copy operation to duplicate a source database in the current one.
 *
 * @author Rsl1122
 */
public class BackupCopyTransaction extends RemoveEverythingTransaction {

    private final Database sourceDB;

    public BackupCopyTransaction(Database sourceDB) {
        this.sourceDB = sourceDB;
    }

    @Override
    protected boolean shouldBeExecuted() {
        return !sourceDB.equals(db) && sourceDB.isOpen();
    }

    @Override
    protected void performOperations() {
        super.performOperations();

        copyServers();
        copyUsers();
        copyWorlds();
        copyTPS();
        copyWebUsers();
        copyCommandUsageData();
        copyIPsAndGeolocs();
        copyNicknames();
        copySessions();
        copyUserInfo();
        copyPings();
    }

    private void copyPings() {
        db.getPingTable().insertAllPings(sourceDB.query(LargeFetchQueries.fetchAllPingData()));
    }

    private void copyCommandUsageData() {
        execute(LargeStoreQueries.storeAllCommandUsageData(sourceDB.query(LargeFetchQueries.fetchAllCommandUsageData())));
    }

    private void copyIPsAndGeolocs() {
        execute(LargeStoreQueries.storeAllGeoInfoData(sourceDB.query(LargeFetchQueries.fetchAllGeoInfoData())));
    }

    private void copyNicknames() {
        db.getNicknamesTable().insertNicknames(sourceDB.query(LargeFetchQueries.fetchAllNicknameData()));
    }

    private void copyWebUsers() {
        db.getSecurityTable().addUsers(sourceDB.query(LargeFetchQueries.fetchAllPlanWebUsers()));
    }

    private void copyServers() {
        db.getServerTable().insertAllServers(sourceDB.query(LargeFetchQueries.fetchPlanServerInformation()).values());
    }

    private void copyTPS() {
        db.getTpsTable().insertAllTPS(sourceDB.query(LargeFetchQueries.fetchAllTPSData()));
    }

    private void copyUserInfo() {
        db.getUserInfoTable().insertUserInfo(sourceDB.query(LargeFetchQueries.fetchPerServerUserInformation()));
    }

    private void copyWorlds() {
        db.getWorldTable().saveWorlds(sourceDB.query(LargeFetchQueries.fetchAllWorldNames()));
    }

    private void copyUsers() {
        UsersTable fromTable = db.getUsersTable();
        UsersTable toTable = db.getUsersTable();

        toTable.insertUsers(sourceDB.query(LargeFetchQueries.fetchAllCommonUserInformation()));
        toTable.updateKicked(fromTable.getAllTimesKicked());
    }

    private void copySessions() {
        db.getSessionsTable().insertSessions(sourceDB.query(LargeFetchQueries.fetchAllSessionsWithKillAndWorldData()), true);
    }
}