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
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.sql.queries.LargeFetchQueries;
import com.djrapitops.plan.db.sql.queries.LargeStoreQueries;

import java.util.function.Function;

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
        // Clear the database.
        super.performOperations();

        copyPlanServerInformation();
        copyCommonUserInformation();
        copyWorldNames();
        copyTPSData();
        copyPlanWebUsers();
        copyCommandUsageData();
        copyGeoInformation();
        copyNicknameData();
        copySessionsWithKillAndWorldData();
        copyPerServerUserInformation();
        copyPingData();
    }

    private <T> void copy(Function<T, Executable> executableCreator, Query<T> dataQuery) {
        // Creates a new Executable from the queried data of the source database
        execute(executableCreator.apply(sourceDB.query(dataQuery)));
    }

    private void copyPingData() {
        copy(LargeStoreQueries::storeAllPingData, LargeFetchQueries.fetchAllPingData());
    }

    private void copyCommandUsageData() {
        copy(LargeStoreQueries::storeAllCommandUsageData, LargeFetchQueries.fetchAllCommandUsageData());
    }

    private void copyGeoInformation() {
        copy(LargeStoreQueries::storeAllGeoInformation, LargeFetchQueries.fetchAllGeoInformation());
    }

    private void copyNicknameData() {
        copy(LargeStoreQueries::storeAllNicknameData, LargeFetchQueries.fetchAllNicknameData());
    }

    private void copyPlanWebUsers() {
        copy(LargeStoreQueries::storeAllPlanWebUsers, LargeFetchQueries.fetchAllPlanWebUsers());
    }

    private void copyPlanServerInformation() {
        copy(LargeStoreQueries::storeAllPlanServerInformation, LargeFetchQueries.fetchPlanServerInformationCollection());
    }

    private void copyTPSData() {
        copy(LargeStoreQueries::storeAllTPSData, LargeFetchQueries.fetchAllTPSData());
    }

    private void copyPerServerUserInformation() {
        copy(LargeStoreQueries::storePerServerUserInformation, LargeFetchQueries.fetchPerServerUserInformation());
    }

    private void copyWorldNames() {
        copy(LargeStoreQueries::storeAllWorldNames, LargeFetchQueries.fetchAllWorldNames());
    }

    private void copyCommonUserInformation() {
        copy(LargeStoreQueries::storeAllCommonUserInformation, LargeFetchQueries.fetchAllCommonUserInformation());
    }

    private void copySessionsWithKillAndWorldData() {
        copy(LargeStoreQueries::storeAllSessionsWithKillAndWorldData, LargeFetchQueries.fetchAllSessionsFlatWithKillAndWorldData());
    }
}