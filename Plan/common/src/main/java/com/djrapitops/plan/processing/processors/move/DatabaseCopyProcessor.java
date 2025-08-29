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
package com.djrapitops.plan.processing.processors.move;

import com.djrapitops.plan.gathering.domain.BaseUser;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.LargeStoreQueries;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.lookup.LookupTable;
import com.djrapitops.plan.storage.database.queries.objects.lookup.LookupTableQueries;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author AuroraLS3
 */
public class DatabaseCopyProcessor implements Runnable {
    private static final int ROW_LIMIT = 500;

    private final Locale locale;
    private final Database fromDB;
    private final Database toDB;
    private final boolean clearDestinationDatabase;
    private final boolean changeServerUUID;
    private final Consumer<String> feedback;

    public DatabaseCopyProcessor(Locale locale, Database fromDB, Database toDB, boolean clearDestinationDatabase, boolean changeServerUUID, Consumer<String> feedback) {
        this.locale = locale;
        this.fromDB = fromDB;
        this.toDB = toDB;
        this.clearDestinationDatabase = clearDestinationDatabase;
        this.changeServerUUID = changeServerUUID;
        this.feedback = feedback;
    }

    @Override
    public void run() {
        if (fromDB.getState() != Database.State.OPEN) {
            feedback.accept("Source database is " + fromDB.getState() + ", could not begin operation.");
            return;
        }
        if (toDB.getState() != Database.State.OPEN) {
            feedback.accept("Destination database is " + toDB.getState() + ", could not begin operation.");
            return;
        }
        if (clearDestinationDatabase) {
            feedback.accept("Clearing destination database..");
            toDB.executeTransaction(new RemoveEverythingTransaction()).join();
            feedback.accept("Cleared destination database.");
        }

        feedback.accept("Beginning database copy process..");

        LookupTable<Integer> serverIdLookupTable = copyMissingServers();
        LookupTable<Integer> userIdLookupTable = copyMissingUsers();
        // TODO
        // https://github.com/plan-player-analytics/Plan/wiki/Database-Schema
        // copy plan_user_info
        // copy join addresses
        // merge geolocations
        // merge nicknames
        // copy ping
        // copy sessions
        // merge user info
        // copy worlds
        // copy world times
        // copy kills
        // copy tps
        // copy allow list bounce
        // copy plugin versions
        // copy web groups
        // copy permissions
        // copy group to permission
        // copy security table
        // copy user preferences
        // plan how to copy extension data
    }

    private LookupTable<Integer> copyMissingServers() {
        LookupTable<ServerUUID> serverLookupTable = toDB.query(LookupTableQueries.serverLookupTable());
        List<Server> servers = fromDB.query(ServerQueries.fetchAllServers());
        feedback.accept("Copying plan_servers, " + servers.size() + " rows");
        for (Server server : servers) {
            if (serverLookupTable.find(server.getUuid()).isEmpty()) {
                toDB.executeTransaction(new StoreServerInformationTransaction(server)).join();
            } // TODO change server UUID if necessary
        }
        return toDB.query(LookupTableQueries.serverLookupTable())
                .constructIdToIdLookupTable(fromDB.query(LookupTableQueries.serverLookupTable()));
    }

    private LookupTable<Integer> copyMissingUsers() {
        LookupTable<UUID> playerLookupTable = toDB.query(LookupTableQueries.playerLookupTable());
        int currentId = 0;
        while (currentId >= 0) {
            List<BaseUser> found = fromDB.query(BaseUserQueries.fetchBaseUsers(currentId, ROW_LIMIT));
            List<BaseUser> copied = found.stream()
                    .filter(user -> playerLookupTable.find(user.getUuid()).isEmpty())
                    .collect(Collectors.toList());
            // TODO merge register dates and times_kicked
            toDB.executeInTransaction(LargeStoreQueries.storeAllCommonUserInformation(copied)).join();
            if (found.isEmpty()) {
                currentId = -1;
            } else {
                currentId += ROW_LIMIT;
            }
        }

        return toDB.query(LookupTableQueries.playerLookupTable())
                .constructIdToIdLookupTable(fromDB.query(LookupTableQueries.playerLookupTable()));
    }
}
