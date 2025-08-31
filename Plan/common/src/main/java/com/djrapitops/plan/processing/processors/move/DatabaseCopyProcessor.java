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
import com.djrapitops.plan.processing.CriticalRunnable;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.LargeStoreQueries;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.storage.database.queries.objects.lookup.IdMapper;
import com.djrapitops.plan.storage.database.queries.objects.lookup.LookupTable;
import com.djrapitops.plan.storage.database.queries.objects.lookup.LookupTableQueries;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.init.CreateTemporarySessionIdLookupTable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

/**
 * @author AuroraLS3
 */
public class DatabaseCopyProcessor implements CriticalRunnable {
    private static final int ROW_LIMIT = 1000;

    private final Locale locale;
    private final Database fromDB;
    private final Database toDB;
    private final Set<Strategy> strategies = Collections.newSetFromMap(new EnumMap<>(Strategy.class));
    private final Consumer<String> feedback;

    private final Map<ServerUUID, ServerUUID> serverUuidLookupTable = new HashMap<>();

    public DatabaseCopyProcessor(Locale locale, Database fromDB, Database toDB, Consumer<String> feedback, Strategy... strategies) {
        this.locale = locale;
        this.fromDB = fromDB;
        this.toDB = toDB;
        this.strategies.addAll(Arrays.asList(strategies));
        this.feedback = feedback;
    }

    public ServerUUID mapServerUUID(ServerUUID serverUUID) {
        return serverUuidLookupTable.getOrDefault(serverUUID, serverUUID);
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
        if (strategies.contains(Strategy.CLEAR_DESTINATION_DATABASE)) {
            feedback.accept("Clearing destination database..");
            toDB.executeTransaction(new RemoveEverythingTransaction()).join();
            feedback.accept("Cleared destination database.");
        }

        feedback.accept("Beginning database copy process..");

        try {
            LookupTable<Integer> serverIdLookupTable = copyMissingServers();

            if (strategies.contains(Strategy.SERVER_UUID_CONFLICT_DELETE_SERVER)) {
                // TODO delete existing servers' data that are to be copied over if correction wasn't done
                //      this is to prevent issues where copy completed partially and queries expect fresh insert.
            }

            LookupTable<Integer> userIdLookupTable = copyMissingUsers();
            copyUserInfo(serverIdLookupTable, userIdLookupTable);
            LookupTable<Integer> joinAddressLookupTable = copyMissingJoinAddresses();
            copyPing(serverIdLookupTable, userIdLookupTable);
            copyTps(serverIdLookupTable);
            copyPluginVersions(serverIdLookupTable);
            copySessions(serverIdLookupTable, userIdLookupTable, joinAddressLookupTable);
            LookupTable<Integer> worldIdLookupTable = copyWorlds();
            copyWorldTimes(serverIdLookupTable, userIdLookupTable, worldIdLookupTable);
            // TODO
            // https://github.com/plan-player-analytics/Plan/wiki/Database-Schema
            // merge geolocations HARD (last used merging table)
            // merge nicknames HARD (last used merging table)
            // copy worlds
            // copy kills
            // copy allow list bounce
            // copy web groups
            // copy permissions
            // copy group to permission
            // copy security table
            // copy user preferences
            // copy access log
            // plan how to copy extension data

            // TODO deal with CompletionException
        } catch (IllegalStateException e) {
            feedback.accept("Operation was aborted.");
        } finally {
            toDB.executeInTransaction(new ExecStatement(SessionsTable.TemporaryIdLookupTable.DROP_TABLE_STATEMENT) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    // Nothing to prepare
                }
            }).join();
            toDB.executeTransaction(SessionsTable.Row.removeOldIdPatch()).join();
        }
    }

    private LookupTable<Integer> copyMissingServers() {
        LookupTable<ServerUUID> serverLookupTable = toDB.query(LookupTableQueries.serverLookupTable());
        List<Server> servers = fromDB.query(ServerQueries.fetchAllServers());
        feedback.accept("Copying plan_servers, " + servers.size() + " rows");
        for (Server server : servers) {
            if (serverLookupTable.find(server.getUuid()).isEmpty()) {
                toDB.executeTransaction(new StoreServerInformationTransaction(server)).join();
            } else if (strategies.contains(Strategy.SERVER_UUID_CONFLICT_SWAP_UUID)) {
                ServerUUID newUUID = ServerUUID.randomUUID();
                serverUuidLookupTable.put(server.getUuid(), newUUID);
                toDB.executeTransaction(new StoreServerInformationTransaction(
                        new Server(server.getId().orElse(null), newUUID, server.getName(), server.getWebAddress(), server.isProxy(), server.getPlanVersion())
                )).join();
            } else if (!strategies.contains(Strategy.SERVER_UUID_CONFLICT_DELETE_SERVER)) {
                // No strategy to deal with uuid conflict selected.
                feedback.accept("Server with " + server.getUuid() + " already exists. Choose a strategy to deal with it.");
                throw new IllegalStateException();
            }
        }
        return toDB.query(LookupTableQueries.serverLookupTable())
                .constructIdToIdLookupTable(fromDB.query(LookupTableQueries.serverLookupTable()));
    }

    private LookupTable<Integer> copyMissingUsers() {
        LookupTable<UUID> playerLookupTable = toDB.query(LookupTableQueries.playerLookupTable());

        batching(currentId -> {
            List<BaseUser> found = fromDB.query(BaseUserQueries.fetchBaseUsers(currentId, ROW_LIMIT));
            Map<Boolean, List<BaseUser>> copied = found.stream()
                    .collect(Collectors.partitioningBy(user -> playerLookupTable.find(user.getUuid()).isPresent()));
            List<BaseUser> newUsers = copied.get(false);
            toDB.executeInTransaction(LargeStoreQueries.insertBaseUsers(newUsers)).join();
            List<BaseUser> existingUsers = copied.get(true);
            toDB.executeInTransaction(LargeStoreQueries.mergeBaseUsers(existingUsers, playerLookupTable)).join();
            return found.isEmpty();
        });

        return toDB.query(LookupTableQueries.playerLookupTable())
                .constructIdToIdLookupTable(fromDB.query(LookupTableQueries.playerLookupTable()));
    }

    private void batching(IntPredicate batch) {
        int currentId = 0;
        while (currentId >= 0) {
            boolean lastBatch = batch.test(currentId);
            if (lastBatch) {
                currentId = -1;
            } else {
                currentId += ROW_LIMIT;
            }
        }
    }

    private void copyUserInfo(LookupTable<Integer> serverIdLookupTable, LookupTable<Integer> userIdLookupTable) {
        batching(currentId -> {
            List<UserInfoTable.Row> rows = fromDB.query(UserInfoQueries.fetchUserInfoRows(currentId, ROW_LIMIT));
            IdMapper.mapUserIds(rows, userIdLookupTable);
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            toDB.executeInTransaction(LargeStoreQueries.insertUserInfo(rows)).join();
            return rows.isEmpty();
        });
    }

    private LookupTable<Integer> copyMissingJoinAddresses() {
        LookupTable<String> joinAddressLookupTable = toDB.query(JoinAddressQueries.joinAddressLookupTable());
        List<JoinAddressTable.Row> rows = fromDB.query(JoinAddressQueries.fetchRows());
        List<JoinAddressTable.Row> newRows = rows.stream()
                .filter(address -> joinAddressLookupTable.find(address.joinAddress).isEmpty())
                .collect(Collectors.toList());
        toDB.executeInTransaction(LargeStoreQueries.insertJoinAddresses(newRows)).join();

        LookupTable<String> oldLookupTable = new LookupTable<>();
        for (JoinAddressTable.Row row : rows) {
            oldLookupTable.put(row.joinAddress, row.id);
        }

        return toDB.query(JoinAddressQueries.joinAddressLookupTable())
                .constructIdToIdLookupTable(oldLookupTable);
    }

    private void copyPing(LookupTable<Integer> serverIdLookupTable, LookupTable<Integer> userIdLookupTable) {
        batching(currentId -> {
            List<PingTable.Row> rows = fromDB.query(PingQueries.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapUserIds(rows, userIdLookupTable);
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            toDB.executeInTransaction(LargeStoreQueries.insertPing(rows)).join();
            return rows.isEmpty();
        });
    }

    private void copyTps(LookupTable<Integer> serverIdLookupTable) {
        batching(currentId -> {
            List<TPSTable.Row> rows = fromDB.query(TPSQueries.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            toDB.executeInTransaction(LargeStoreQueries.insertTps(rows)).join();
            return rows.isEmpty();
        });
    }

    private void copyPluginVersions(LookupTable<Integer> serverIdLookupTable) {
        batching(currentId -> {
            List<PluginVersionTable.Row> rows = fromDB.query(PluginMetadataQueries.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            toDB.executeInTransaction(LargeStoreQueries.insertPluginVersions(rows)).join();
            return rows.isEmpty();
        });
    }

    private void copySessions(LookupTable<Integer> serverIdLookupTable, LookupTable<Integer> userIdLookupTable, LookupTable<Integer> joinAddressLookupTable) {
        toDB.executeTransaction(new CreateTemporarySessionIdLookupTable()).join();
        toDB.executeTransaction(SessionsTable.Row.addOldIdPatch()).join();

        batching(currentId -> {
            List<SessionsTable.Row> rows = fromDB.query(SessionQueries.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapUserIds(rows, userIdLookupTable);
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            IdMapper.mapJoinAddressIds(rows, joinAddressLookupTable);
            toDB.executeInTransaction(LargeStoreQueries.insertSessionsWithOldIds(rows)).join();
            return rows.isEmpty();
        });

        toDB.executeInTransaction(new ExecStatement(SessionsTable.TemporaryIdLookupTable.INSERT_ALL_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) {
                // Nothing to prepare
            }
        }).join();
    }

    private LookupTable<Integer> copyWorlds() {
        // TODO
        return null;
    }

    private void copyWorldTimes(LookupTable<Integer> serverIdLookupTable, LookupTable<Integer> userIdLookupTable, LookupTable<Integer> worldIdLookupTable) {
        batching(currentId -> {
            List<WorldTimesTable.Row> rows = fromDB.query(WorldTimesQueries.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapUserIds(rows, userIdLookupTable);
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            IdMapper.mapWorldIds(rows, worldIdLookupTable);
            toDB.executeTransaction(LargeStoreQueries.insertWorldTimesWithOldSessionIds(rows)).join();
            return rows.isEmpty();
        });
    }

    public enum Strategy {
        CLEAR_DESTINATION_DATABASE,
        SERVER_UUID_CONFLICT_SWAP_UUID,
        SERVER_UUID_CONFLICT_DELETE_SERVER
    }
}
