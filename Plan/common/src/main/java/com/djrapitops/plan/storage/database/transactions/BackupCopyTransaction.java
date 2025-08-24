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
package com.djrapitops.plan.storage.database.transactions;

import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.LargeFetchQueries;
import com.djrapitops.plan.storage.database.queries.LargeStoreQueries;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;

import java.util.function.Function;

/**
 * Transaction that performs a clear + copy operation to duplicate a source database in the current one.
 *
 * @author AuroraLS3
 */
public class BackupCopyTransaction extends RemoveEverythingTransaction {

    private final Database sourceDB;
    private final Database destinationDB;

    public BackupCopyTransaction(Database sourceDB, Database destinationDB) {
        this.sourceDB = sourceDB;
        this.destinationDB = destinationDB;
    }

    @Override
    protected boolean shouldBeExecuted() {
        return super.shouldBeExecuted() && !sourceDB.equals(destinationDB) && sourceDB.getState() != Database.State.CLOSED;
    }

    @Override
    protected void performOperations() {
        // Clear the database.
        super.performOperations();

        copyPlanServerInformation();
        copyCommonUserInformation();
        copyWorldNames();
        copyTPSData();
        copyWebGroups();
        copyPlanWebUsers();
        copyGeoInformation();
        copyNicknameData();
        copySessionsWithKillAndWorldData();
        copyPerServerUserInformation();
        copyPingData();
    }

    private void copyWebGroups() {
        copy(LargeStoreQueries::storeGroupNames, WebUserQueries.fetchGroupNames());
        copy(LargeStoreQueries::storePermissions, WebUserQueries.fetchAvailablePermissions());
        copy(LargeStoreQueries::storeGroupPermissionRelations, WebUserQueries.fetchAllGroupPermissions());
    }

    private <T> void copy(Function<T, Executable> executableCreator, Query<T> dataQuery) {
        // Creates a new Executable from the queried data of the source database
        execute(executableCreator.apply(sourceDB.query(dataQuery)));
    }

    private void copyPingData() {
        copy(LargeStoreQueries::storeAllPingData, PingQueries.fetchAllPingData());
    }

    private void copyGeoInformation() {
        copy(LargeStoreQueries::storeAllGeoInformation, GeoInfoQueries.fetchAllGeoInformation());
    }

    private void copyNicknameData() {
        copy(LargeStoreQueries::storeAllNicknameData, NicknameQueries.fetchAllNicknameData());
    }

    private void copyPlanWebUsers() {
        copy(LargeStoreQueries::storeAllPlanWebUsers, WebUserQueries.fetchAllUsers());
        copy(LargeStoreQueries::storeAllPreferences, WebUserQueries.fetchAllPreferences());
    }

    private void copyPlanServerInformation() {
        copy(LargeStoreQueries::storeAllPlanServerInformation, ServerQueries.fetchUninstalledServerInformation());
        copy(LargeStoreQueries::storeAllPlanServerInformation, ServerQueries.fetchPlanServerInformationCollection());
    }

    private void copyTPSData() {
        copy(LargeStoreQueries::storeAllTPSData, LargeFetchQueries.fetchAllTPSData());
    }

    private void copyPerServerUserInformation() {
        copy(LargeStoreQueries::storePerServerUserInformation, UserInfoQueries.fetchAllUserInformation());
    }

    private void copyWorldNames() {
        copy(LargeStoreQueries::storeAllWorldNames, LargeFetchQueries.fetchAllWorldNames());
    }

    private void copyCommonUserInformation() {
        copy(LargeStoreQueries::storeAllCommonUserInformation, BaseUserQueries.fetchAllBaseUsers());
    }

    private void copySessionsWithKillAndWorldData() {
        copy(LargeStoreQueries::storeAllSessionsWithKillAndWorldData, SessionQueries.fetchAllSessions());
    }
}