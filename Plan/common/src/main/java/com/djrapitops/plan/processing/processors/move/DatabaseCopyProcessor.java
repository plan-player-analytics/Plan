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

import com.djrapitops.plan.delivery.domain.World;
import com.djrapitops.plan.gathering.domain.BaseUser;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.processing.CriticalRunnable;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.LargeStoreQueries;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.storage.database.queries.objects.lookup.IdMapper;
import com.djrapitops.plan.storage.database.queries.objects.lookup.LookupTable;
import com.djrapitops.plan.storage.database.queries.objects.lookup.LookupTableQueries;
import com.djrapitops.plan.storage.database.queries.objects.lookup.ServerUUIDIdentifiable;
import com.djrapitops.plan.storage.database.queries.schema.MySQLSchemaQueries;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.sql.tables.webuser.*;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveServerTransaction;
import com.djrapitops.plan.storage.database.transactions.init.CreateTemporarySessionIdLookupTable;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plan.utilities.logging.ProgressTracker;

import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * All the logic for copying or merging two databases into another.
 * <p>
 * Databases given must be in {@link Database.State#OPEN}
 *
 * @author AuroraLS3
 */
public class DatabaseCopyProcessor implements CriticalRunnable {
    private static final int ROW_LIMIT = 100000;
    private static final int DONE_SIGNAL = -1;

    private final Locale locale;
    private final ErrorLogger errorLogger;
    private final Database fromDB;
    private final Database toDB;
    private final Set<Strategy> strategies = Collections.newSetFromMap(new EnumMap<>(Strategy.class));
    private final Consumer<String> feedback;
    private final Runnable doAfter;

    private final Map<ServerUUID, ServerUUID> serverUuidLookupTable = new HashMap<>();
    private final ProgressTracker progressTracker;
    private Map<String, Integer> tableCounts;

    public DatabaseCopyProcessor(Locale locale, ErrorLogger errorLogger, Database fromDB, Database toDB, Consumer<String> feedback, Strategy... strategies) {
        this(locale, errorLogger, fromDB, toDB, feedback, () -> {}, strategies);
    }

    public DatabaseCopyProcessor(Locale locale, ErrorLogger errorLogger, Database fromDB, Database toDB, Consumer<String> feedback, Runnable doAfter, Strategy... strategies) {
        this.locale = locale;
        this.errorLogger = errorLogger;
        this.fromDB = fromDB;
        this.toDB = toDB;
        this.doAfter = doAfter;
        this.strategies.addAll(Arrays.asList(strategies));
        this.feedback = feedback;
        this.progressTracker = new ProgressTracker(0);
    }

    public ServerUUID mapServerUUID(ServerUUID serverUUID) {
        return serverUuidLookupTable.getOrDefault(serverUUID, serverUUID);
    }

    @Override
    public void run() {
        try {
            int i = 0;
            while (fromDB.getState() == Database.State.PATCHING && i < 200) {
                Thread.sleep(500);
                i++;
            }
            while (toDB.getState() == Database.State.PATCHING && i < 200) {
                Thread.sleep(500);
                i++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (fromDB.getState() != Database.State.OPEN) {
            feedback.accept(locale.getString(CommandLang.DB_COPY_SOURCE_STATE, fromDB.getState()));
            return;
        }
        if (toDB.getState() != Database.State.OPEN) {
            feedback.accept(locale.getString(CommandLang.DB_COPY_DESTINATION_STATE, toDB.getState()));
            return;
        }

        tableCounts = fromDB.query(LookupTableQueries.tableCounts());
        feedback.accept(locale.getString(CommandLang.DB_COPY_LIST_TITLE_SOURCE));
        tableCounts.forEach((key, value) -> feedback.accept(locale.getString(CommandLang.DB_COPY_LIST_ROW, key, value)));

        if (strategies.contains(Strategy.CLEAR_DESTINATION_DATABASE)) {
            feedback.accept(locale.getString(CommandLang.DB_COPY_CLEAR_START));
            toDB.executeTransaction(new RemoveEverythingTransaction()).join();
            feedback.accept(locale.getString(CommandLang.DB_COPY_CLEAR_FINISH));
        } else {
            Map<String, Integer> existing = toDB.query(LookupTableQueries.tableCounts());
            feedback.accept(locale.getString(CommandLang.DB_COPY_LIST_TITLE_DESTINATION));
            existing.forEach((key, value) -> feedback.accept(locale.getString(CommandLang.DB_COPY_LIST_ROW, key, value)));
        }

        feedback.accept(locale.getString(CommandLang.PROGRESS_START));

        try {
            removeTemporaryTables();
            LookupTable<Integer> serverIdLookupTable = copyMissingServers();

            LookupTable<Integer> userIdLookupTable = copyMissingUsers();
            copyUserInfo(serverIdLookupTable, userIdLookupTable);
            LookupTable<Integer> joinAddressLookupTable = copyMissingJoinAddresses();
            copyPing(serverIdLookupTable, userIdLookupTable);
            copyTps(serverIdLookupTable);
            copyPluginVersions(serverIdLookupTable);
            copySessions(serverIdLookupTable, userIdLookupTable, joinAddressLookupTable);
            LookupTable<Integer> worldIdLookupTable = copyWorlds();
            copyWorldTimes(serverIdLookupTable, userIdLookupTable, worldIdLookupTable);
            copyKills();
            copyGeolocations(userIdLookupTable);
            copyNicknames();
            copyAllowlistBounces(serverIdLookupTable);
            copyAccessLog();
            LookupTable<Integer> webGroupLookupTable = copyGroups();
            LookupTable<Integer> webPermissionLookupTable = copyPermissions();
            copyGroupsToPermissions(webGroupLookupTable, webPermissionLookupTable);
            LookupTable<Integer> webUserIdLookupTable = copyWebUsers(webGroupLookupTable);
            copyUserPreferences(webUserIdLookupTable);
            // TODO plan how to copy extension data https://github.com/plan-player-analytics/Plan/wiki/Database-Schema

            feedback.accept(locale.getString(CommandLang.PROGRESS_SUCCESS));
        } catch (CompletionException e) {
            feedback.accept(locale.getString(CommandLang.DB_COPY_ERROR, e.getMessage()));
            errorLogger.error(e, ErrorContext.builder()
                    .related("Database copy operation", fromDB.getType(), toDB.getType(), strategies)
                    .build());
        } catch (IllegalStateException e) {
            feedback.accept(locale.getString(CommandLang.DB_COPY_ABORT));
        } finally {
            removeTemporaryTables();
            doAfter.run();
        }
    }

    private void removeTemporaryTables() {
        toDB.executeInTransaction(SessionsTable.TemporaryIdLookupTable.DROP_TABLE_STATEMENT).join();
        toDB.executeInTransaction("DROP TABLE IF EXISTS plan_world_times_batch").join();
        toDB.executeInTransaction("DROP TABLE IF EXISTS plan_kills_batch").join();
        toDB.executeTransaction(SessionsTable.Row.removeOldIdPatch()).join();
        dropUniqueConstraint(toDB.getType(), GeoInfoTable.TABLE_NAME, GeoInfoTable.USER_ID + ',' + GeoInfoTable.GEOLOCATION);
        dropUniqueConstraint(toDB.getType(), NicknamesTable.TABLE_NAME, NicknamesTable.SERVER_UUID + ',' + NicknamesTable.USER_UUID + ',' + NicknamesTable.NICKNAME);
        dropUniqueConstraint(toDB.getType(), AllowlistBounceTable.TABLE_NAME, AllowlistBounceTable.UUID + ',' + AllowlistBounceTable.SERVER_ID);
    }

    private LookupTable<Integer> copyGroups() {
        LookupTable<String> lookupTable = toDB.query(LookupTableQueries.webGroupLookupTable());
        List<String> groups = fromDB.query(LookupTableQueries.webGroupLookupTable()).keySet()
                .stream().filter(Predicate.not(lookupTable::contains))
                .collect(Collectors.toList());
        logCopyMessage(WebGroupTable.TABLE_NAME);
        toDB.executeInTransaction(LargeStoreQueries.storeGroupNames(groups)).join();
        logProgress(tableCounts.get(WebGroupTable.TABLE_NAME), WebGroupTable.TABLE_NAME, false);
        return toDB.query(LookupTableQueries.webGroupLookupTable())
                .constructIdToIdLookupTable(fromDB.query(LookupTableQueries.webGroupLookupTable()));
    }

    private LookupTable<Integer> copyPermissions() {
        LookupTable<String> lookupTable = toDB.query(LookupTableQueries.webPermissionLookupTable());
        List<String> permissions = fromDB.query(LookupTableQueries.webPermissionLookupTable()).keySet()
                .stream().filter(Predicate.not(lookupTable::contains))
                .collect(Collectors.toList());
        logCopyMessage(WebPermissionTable.TABLE_NAME);
        toDB.executeInTransaction(LargeStoreQueries.storePermissions(permissions)).join();
        logProgress(tableCounts.get(WebPermissionTable.TABLE_NAME), WebPermissionTable.TABLE_NAME, false);
        return toDB.query(LookupTableQueries.webPermissionLookupTable())
                .constructIdToIdLookupTable(fromDB.query(LookupTableQueries.webPermissionLookupTable()));
    }

    private LookupTable<Integer> copyMissingServers() {
        LookupTable<ServerUUID> serverLookupTable = toDB.query(LookupTableQueries.serverLookupTable());
        List<Server> servers = fromDB.query(ServerQueries.fetchAllServers());

        logCopyMessage(ServerTable.TABLE_NAME);
        for (Server server : servers) {
            if (serverLookupTable.find(server.getUuid()).isEmpty()) {
                toDB.executeTransaction(new StoreServerInformationTransaction(server)).join();
            } else if (strategies.contains(Strategy.SERVER_UUID_CONFLICT_SWAP_UUID)) {
                ServerUUID newUUID = ServerUUID.randomUUID();
                serverUuidLookupTable.put(server.getUuid(), newUUID);
                feedback.accept(locale.getString(CommandLang.DB_COPY_CONFLICT_SWAP, server.getUuid(), newUUID));
                toDB.executeTransaction(new StoreServerInformationTransaction(
                        new Server(server.getId().orElse(null), newUUID, server.getName(), server.getWebAddress(), server.isProxy(), server.getPlanVersion())
                )).join();
            } else if (strategies.contains(Strategy.SERVER_UUID_CONFLICT_DELETE_SERVER)) {
                feedback.accept(locale.getString(CommandLang.DB_COPY_CONFLICT_DELETE, server.getUuid()));
                toDB.executeTransaction(new RemoveServerTransaction(server.getUuid())).join();
            } else {
                // No strategy to deal with uuid conflict selected.
                feedback.accept(locale.getString(CommandLang.DB_COPY_CONFLICT_INFO_1, server.getUuid()));
                feedback.accept(locale.getString(CommandLang.DB_COPY_CONFLICT_INFO_2));
                feedback.accept(locale.getString(CommandLang.DB_COPY_CONFLICT_INFO_3));
                throw new IllegalStateException();
            }
        }
        if (!servers.isEmpty()) {
            logProgress(servers.size(), ServerTable.TABLE_NAME, false);
        }

        // Replaces Server UUID with the new ones
        LookupTable<ServerUUID> oldIds = new LookupTable<>(fromDB.query(LookupTableQueries.serverLookupTable()), serverUuidLookupTable::get);
        return toDB.query(LookupTableQueries.serverLookupTable())
                .constructIdToIdLookupTable(oldIds);
    }

    private boolean logProgress(int count, String tableName, boolean empty) {
        if (!empty) {
            Integer of = tableCounts.get(tableName);
            int total = progressTracker.getTotal();
            if (of != total || progressTracker.isDone()) {
                progressTracker.reset(of);
            }
            progressTracker.add(count);
            if (progressTracker.shouldShowPercentage() || of != total) {
                feedback.accept(locale.getString(CommandLang.DB_COPY_PROGRESS, tableName, progressTracker.getCount(), progressTracker.getTotal(), progressTracker.getPercentage()));
                progressTracker.percentageShown();
            }
            return progressTracker.getCount() == of;
        }
        return progressTracker.isDone();
    }

    private LookupTable<Integer> copyMissingUsers() {
        LookupTable<UUID> playerLookupTable = toDB.query(LookupTableQueries.playerLookupTable());

        logCopyMessage(UsersTable.TABLE_NAME);
        batching(currentId -> {
            List<BaseUser> found = fromDB.query(BaseUserQueries.fetchBaseUsers(currentId, ROW_LIMIT));
            Map<Boolean, List<BaseUser>> copied = found.stream()
                    .collect(Collectors.partitioningBy(user -> playerLookupTable.find(user.getUuid()).isPresent()));
            List<BaseUser> newUsers = copied.get(false);
            toDB.executeInTransaction(LargeStoreQueries.insertBaseUsers(newUsers)).join();
            List<BaseUser> existingUsers = copied.get(true);
            toDB.executeInTransaction(LargeStoreQueries.mergeBaseUsers(existingUsers, playerLookupTable)).join();
            logProgress(found.size(), UsersTable.TABLE_NAME, found.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : found.get(found.size() - 1).getId();
        });

        return toDB.query(LookupTableQueries.playerLookupTable())
                .constructIdToIdLookupTable(fromDB.query(LookupTableQueries.playerLookupTable()));
    }

    private void batching(IntFunction<Integer> batch) {
        int currentId = 0;
        while (currentId >= 0) {
            currentId = batch.apply(currentId);
        }
    }

    private void copyUserInfo(LookupTable<Integer> serverIdLookupTable, LookupTable<Integer> userIdLookupTable) {
        logCopyMessage(UserInfoTable.TABLE_NAME);
        batching(currentId -> {
            List<UserInfoTable.Row> rows = fromDB.query(UserInfoQueries.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapUserIds(rows, userIdLookupTable);
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            toDB.executeInTransaction(LargeStoreQueries.insertUserInfo(rows)).join();
            logProgress(rows.size(), UserInfoTable.TABLE_NAME, rows.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
    }

    private LookupTable<Integer> copyMissingJoinAddresses() {
        logCopyMessage(JoinAddressTable.TABLE_NAME);
        LookupTable<String> joinAddressLookupTable = toDB.query(LookupTableQueries.joinAddressLookupTable());
        List<JoinAddressTable.Row> rows = fromDB.query(JoinAddressQueries.fetchRows());
        List<JoinAddressTable.Row> newRows = rows.stream()
                .filter(address -> joinAddressLookupTable.find(address.joinAddress).isEmpty())
                .collect(Collectors.toList());
        toDB.executeInTransaction(LargeStoreQueries.insertJoinAddresses(newRows)).join();

        LookupTable<String> oldLookupTable = new LookupTable<>();
        for (JoinAddressTable.Row row : rows) {
            oldLookupTable.put(row.joinAddress, row.id);
        }
        if (!rows.isEmpty()) {
            logProgress(rows.size(), JoinAddressTable.TABLE_NAME, false);
        }

        return toDB.query(LookupTableQueries.joinAddressLookupTable())
                .constructIdToIdLookupTable(oldLookupTable);
    }

    private void logCopyMessage(String tableName) {
        feedback.accept(locale.getString(CommandLang.DB_COPY_TABLE, tableName));
    }

    private void copyPing(LookupTable<Integer> serverIdLookupTable, LookupTable<Integer> userIdLookupTable) {
        logCopyMessage(PingTable.TABLE_NAME);
        batching(currentId -> {
            List<PingTable.Row> rows = fromDB.query(PingQueries.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapUserIds(rows, userIdLookupTable);
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            toDB.executeInTransaction(LargeStoreQueries.insertPing(rows)).join();
            logProgress(rows.size(), PingTable.TABLE_NAME, rows.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
    }

    private void copyTps(LookupTable<Integer> serverIdLookupTable) {
        logCopyMessage(TPSTable.TABLE_NAME);
        batching(currentId -> {
            List<TPSTable.Row> rows = fromDB.query(TPSQueries.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            toDB.executeInTransaction(LargeStoreQueries.insertTps(rows)).join();
            logProgress(rows.size(), TPSTable.TABLE_NAME, rows.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
    }

    private void copyPluginVersions(LookupTable<Integer> serverIdLookupTable) {
        logCopyMessage(PluginVersionTable.TABLE_NAME);
        batching(currentId -> {
            List<PluginVersionTable.Row> rows = fromDB.query(PluginMetadataQueries.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            toDB.executeInTransaction(LargeStoreQueries.insertPluginVersions(rows)).join();
            logProgress(rows.size(), PluginVersionTable.TABLE_NAME, rows.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
    }

    private void copySessions(LookupTable<Integer> serverIdLookupTable, LookupTable<Integer> userIdLookupTable, LookupTable<Integer> joinAddressLookupTable) {
        toDB.executeTransaction(new CreateTemporarySessionIdLookupTable()).join();
        toDB.executeTransaction(SessionsTable.Row.addOldIdPatch()).join();

        logCopyMessage(SessionsTable.TABLE_NAME);
        batching(currentId -> {
            List<SessionsTable.Row> rows = fromDB.query(SessionQueries.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapUserIds(rows, userIdLookupTable);
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            IdMapper.mapJoinAddressIds(rows, joinAddressLookupTable);
            toDB.executeInTransaction(LargeStoreQueries.insertSessionsWithOldIds(rows)).join();
            logProgress(rows.size(), SessionsTable.TABLE_NAME, rows.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });

        toDB.executeInTransaction(SessionsTable.TemporaryIdLookupTable.INSERT_ALL_STATEMENT).join();
    }

    private LookupTable<Integer> copyWorlds() {
        LookupTable<World> worldLookupTable = toDB.query(LookupTableQueries.worldLookupTable());
        logCopyMessage(WorldTable.TABLE_NAME);
        batching(currentId -> {
            List<WorldTable.Row> rows = fromDB.query(WorldTimesQueries.fetchWorldRows(currentId, ROW_LIMIT))
                    .stream().filter(row -> !worldLookupTable.contains(world ->
                            world.getWorldName().equals(row.name)
                                    && world.getServerUUID().equals(row.serverUUID)))
                    .collect(Collectors.toList());
            mapServerUUIDs(rows);
            toDB.executeInTransaction(LargeStoreQueries.insertWorlds(rows)).join();
            logProgress(rows.size(), WorldTable.TABLE_NAME, rows.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
        return toDB.query(LookupTableQueries.worldLookupTable())
                .constructIdToIdLookupTable(fromDB.query(LookupTableQueries.worldLookupTable()));
    }

    private void mapServerUUIDs(List<? extends ServerUUIDIdentifiable> rows) {
        rows.forEach(row -> row.setServerUUID(mapServerUUID(row.getServerUUID())));
    }

    private void copyWorldTimes(LookupTable<Integer> serverIdLookupTable, LookupTable<Integer> userIdLookupTable, LookupTable<Integer> worldIdLookupTable) {
        logCopyMessage(WorldTimesTable.TABLE_NAME);
        batching(currentId -> {
            List<WorldTimesTable.Row> rows = fromDB.query(WorldTimesQueries.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapUserIds(rows, userIdLookupTable);
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            IdMapper.mapWorldIds(rows, worldIdLookupTable);
            toDB.executeTransaction(LargeStoreQueries.insertWorldTimesWithOldSessionIds(rows)).join();
            logProgress(rows.size(), WorldTimesTable.TABLE_NAME, rows.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
    }

    private void copyKills() {
        logCopyMessage(KillsTable.TABLE_NAME);
        batching(currentId -> {
            List<KillsTable.Row> rows = fromDB.query(KillQueries.fetchRows(currentId, ROW_LIMIT));
            mapServerUUIDs(rows);
            toDB.executeTransaction(LargeStoreQueries.insertKillsWithOldSessionIds(rows)).join();
            logProgress(rows.size(), KillsTable.TABLE_NAME, rows.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
    }

    private void copyAccessLog() {
        logCopyMessage(AccessLogTable.TABLE_NAME);
        batching(currentId -> {
            List<AccessLogTable.Row> rows = fromDB.query(AccessLogTable.fetchRows(currentId, ROW_LIMIT));
            toDB.executeInTransaction(LargeStoreQueries.insertAccessLog(rows)).join();
            logProgress(rows.size(), AccessLogTable.TABLE_NAME, rows.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
    }

    private void copyGeolocations(LookupTable<Integer> userIdLookupTable) {
        createUniqueConstraint(GeoInfoTable.TABLE_NAME, GeoInfoTable.USER_ID + ',' + GeoInfoTable.GEOLOCATION);
        logCopyMessage(GeoInfoTable.TABLE_NAME);
        batching(currentId -> {
            List<GeoInfoTable.Row> rows = fromDB.query(GeoInfoTable.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapUserIds(rows, userIdLookupTable);
            toDB.executeInTransaction(LargeStoreQueries.upsertGeoInfo(rows, toDB.getType())).join();
            logProgress(rows.size(), GeoInfoTable.TABLE_NAME, rows.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
    }

    private void copyNicknames() {
        createUniqueConstraint(NicknamesTable.TABLE_NAME, NicknamesTable.SERVER_UUID + ',' + NicknamesTable.USER_UUID + ',' + NicknamesTable.NICKNAME);
        logCopyMessage(NicknamesTable.TABLE_NAME);
        batching(currentId -> {
            List<NicknamesTable.Row> rows = fromDB.query(NicknamesTable.fetchRows(currentId, ROW_LIMIT));
            mapServerUUIDs(rows);
            toDB.executeInTransaction(LargeStoreQueries.upsertNicknames(rows, toDB.getType())).join();
            logProgress(rows.size(), NicknamesTable.TABLE_NAME, rows.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
    }

    private void copyAllowlistBounces(LookupTable<Integer> serverIdLookupTable) {
        createUniqueConstraint(AllowlistBounceTable.TABLE_NAME, AllowlistBounceTable.UUID + ',' + AllowlistBounceTable.SERVER_ID);
        logCopyMessage(AllowlistBounceTable.TABLE_NAME);
        batching(currentId -> {
            List<AllowlistBounceTable.Row> rows = fromDB.query(AllowlistBounceTable.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapServerIds(rows, serverIdLookupTable);
            toDB.executeInTransaction(LargeStoreQueries.upsertAllowlistBounces(rows, toDB.getType())).join();
            logProgress(rows.size(), AllowlistBounceTable.TABLE_NAME, rows.isEmpty());
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
    }

    private void copyGroupsToPermissions(LookupTable<Integer> webGroupLookupTable, LookupTable<Integer> webPermissionLookupTable) {
        Map<Integer, List<Integer>> idsToCopy = IdMapper.mapGroupPermissionIds(fromDB.query(LookupTableQueries.webGroupToPermissionIds()), webGroupLookupTable, webPermissionLookupTable);
        Map<Integer, List<Integer>> existingIds = toDB.query(LookupTableQueries.webGroupToPermissionIds());
        int toCopy = idsToCopy.size();
        // Filter out existing ids
        existingIds.forEach((id, ids) -> {
            if (Objects.equals(idsToCopy.get(id), ids)) {
                idsToCopy.remove(id);
            } else {
                idsToCopy.get(id).removeAll(ids);
            }
        });

        logCopyMessage(WebGroupToPermissionTable.TABLE_NAME);
        toDB.executeInTransaction(LargeStoreQueries.storeGroupPermissionIdRelations(idsToCopy)).join();
        logProgress(tableCounts.get(WebGroupToPermissionTable.TABLE_NAME), WebGroupToPermissionTable.TABLE_NAME, false);
        if (toCopy == 0) {
            feedback.accept(locale.getString(CommandLang.DB_COPY_ALL_DATA_EXISTED));
        }
    }

    private LookupTable<Integer> copyWebUsers(LookupTable<Integer> webGroupLookupTable) {
        LookupTable<String> lookupTable = toDB.query(LookupTableQueries.webUserLookupTable());
        logCopyMessage(SecurityTable.TABLE_NAME);
        batching(currentId -> {
            List<SecurityTable.Row> rows = fromDB.query(WebUserQueries.fetchRows(currentId, ROW_LIMIT));
            IdMapper.mapGroupIds(rows, webGroupLookupTable);
            rows.removeIf(row -> lookupTable.contains(row.username));
            toDB.executeInTransaction(LargeStoreQueries.storeUsers(rows)).join();
            logProgress(tableCounts.get(SessionsTable.TABLE_NAME), SecurityTable.TABLE_NAME, false);
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
        return toDB.query(LookupTableQueries.webUserLookupTable())
                .constructIdToIdLookupTable(fromDB.query(LookupTableQueries.webUserLookupTable()));
    }

    private void copyUserPreferences(LookupTable<Integer> webUserIdLookupTable) {
        logCopyMessage(WebUserPreferencesTable.TABLE_NAME);
        Set<Integer> existingIds = toDB.query(WebUserQueries.fetchPreferencesUserIds());
        batching(currentId -> {
            List<WebUserPreferencesTable.Row> rows = fromDB.query(WebUserPreferencesTable.fetchRows(currentId, ROW_LIMIT));
            int toCopy = rows.size();
            IdMapper.mapUserIds(rows, webUserIdLookupTable);
            rows.removeIf(row -> existingIds.contains(row.webUserId)); // Don't override
            toDB.executeInTransaction(LargeStoreQueries.insertPreferences(rows)).join();
            logProgress(toCopy, WebUserPreferencesTable.TABLE_NAME, toCopy != 0);
            return progressTracker.isDone() ? DONE_SIGNAL : rows.get(rows.size() - 1).id;
        });
    }

    private void createUniqueConstraint(String tableName, String columns) {
        String indexName = "idx_unique_" + tableName + "_" + columns.replace(',', '_');
        String uniqueConstraintMySQL = "ALTER TABLE " + tableName + " ADD UNIQUE INDEX " + indexName + " (" + columns + ")";
        String uniqueConstraintSQLite = "CREATE UNIQUE INDEX IF NOT EXISTS " + indexName + " ON " + tableName + " (" + columns + ")";
        toDB.executeInTransaction(toDB.getType() == DBType.MYSQL ? uniqueConstraintMySQL : uniqueConstraintSQLite).join();
    }

    private void dropUniqueConstraint(DBType dbType, String tableName, String columns) {
        String indexName = "idx_unique_" + tableName + "_" + columns.replace(',', '_');

        boolean isMySQL = dbType == DBType.MYSQL;
        if (isMySQL) {
            boolean indexExists = toDB.query(MySQLSchemaQueries.doesIndexExist(indexName, tableName));
            if (!indexExists) return;
            toDB.executeInTransaction("DROP INDEX " + indexName + " ON " + tableName).join();
        } else {
            toDB.executeInTransaction("DROP INDEX IF EXISTS " + indexName).join();
        }
    }

    public enum Strategy {
        CLEAR_DESTINATION_DATABASE,
        SERVER_UUID_CONFLICT_SWAP_UUID,
        SERVER_UUID_CONFLICT_DELETE_SERVER
    }
}
