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
package com.djrapitops.plan.commands.subcommands;

import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.MessageBuilder;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.gathering.domain.BaseUser;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.processing.processors.move.DatabaseCopyProcessor;
import com.djrapitops.plan.query.QuerySvc;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DatabaseSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.HelpLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.SQLiteDB;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.commands.*;
import com.djrapitops.plan.storage.database.transactions.patches.BadFabricJoinAddressValuePatch;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.player.UUIDFetcher;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Singleton
public class DatabaseCommands {

    private static final String SUPPORTED_DB_OPTIONS = "<MySQL/SQLite>";

    private final Locale locale;
    private final Confirmation confirmation;
    private final ColorScheme colors;
    private final PlanFiles files;
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final SQLiteDB.Factory sqliteFactory;
    private final QuerySvc queryService;
    private final ServerInfo serverInfo;
    private final Identifiers identifiers;
    private final PluginStatusCommands statusCommands;
    private final ErrorLogger errorLogger;
    private final Processing processing;

    private final Formatter<Long> timestamp;
    private final Formatter<Long> clock;

    @Inject
    public DatabaseCommands(
            Locale locale,
            Confirmation confirmation,
            ColorScheme colors,
            PlanFiles files,
            PlanConfig config,
            DBSystem dbSystem,
            SQLiteDB.Factory sqliteFactory,
            QuerySvc queryService,
            ServerInfo serverInfo,
            Formatters formatters,
            Identifiers identifiers,
            PluginStatusCommands statusCommands,
            ErrorLogger errorLogger,
            Processing processing
    ) {
        this.locale = locale;
        this.confirmation = confirmation;
        this.colors = colors;
        this.files = files;
        this.config = config;
        this.dbSystem = dbSystem;
        this.sqliteFactory = sqliteFactory;
        this.queryService = queryService;
        this.serverInfo = serverInfo;
        this.identifiers = identifiers;
        this.statusCommands = statusCommands;
        this.errorLogger = errorLogger;

        this.timestamp = formatters.iso8601NoClockLong();
        clock = formatters.clockLong();
        this.processing = processing;
    }

    public void onBackup(CMDSender sender, @Untrusted Arguments arguments) {
        String dbName = arguments.get(0)
                .orElse(dbSystem.getDatabase().getType().getName())
                .toLowerCase();

        if (!DBType.exists(dbName)) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_INCORRECT_DB, dbName));
        }

        Database fromDB = dbSystem.getActiveDatabaseByName(dbName);
        if (fromDB.getState() != Database.State.OPEN) fromDB.init();

        performBackup(sender, arguments, dbName, fromDB);
    }

    public void performBackup(CMDSender sender, @Untrusted Arguments arguments, String dbName, Database fromDB) {
        Database toDB = null;
        try {
            String timeStamp = timestamp.apply(System.currentTimeMillis());
            String fileName = dbName + "-backup-" + timeStamp;
            sender.send(locale.getString(CommandLang.DB_BACKUP_CREATE, fileName, dbName));
            toDB = sqliteFactory.usingFileCalled(fileName);
            toDB.init();

            DatabaseCopyProcessor databaseCopyProcessor = new DatabaseCopyProcessor(locale, errorLogger, fromDB, toDB, sender::send);
            processing.submit(databaseCopyProcessor);
        } catch (DBOpException e) {
            errorLogger.error(e, ErrorContext.builder().related(sender, arguments).build());
        } finally {
            if (toDB != null) {
                toDB.close();
            }
        }
    }

    public void onRestore(CMDSender sender, @Untrusted Arguments arguments) {
        @Untrusted String backupDbName = arguments.get(0)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, 1, "<" + locale.getString(HelpLang.ARG_BACKUP_FILE) + ">")));

        boolean containsDBFileExtension = backupDbName.endsWith(".db");
        File backupDBFile = files.getFileFromPluginFolder(backupDbName + (containsDBFileExtension ? "" : ".db"));

        if (!backupDBFile.exists()) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_FILE_NOT_FOUND, backupDBFile.getAbsolutePath()));
        }

        String dbName = arguments.get(1)
                .orElse(dbSystem.getDatabase().getType().getName())
                .toLowerCase();
        if (!DBType.exists(dbName)) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_INCORRECT_DB, dbName));
        }

        Database toDB = dbSystem.getActiveDatabaseByName(dbName);

        // Check against restoring from database.db as it is active database
        if (backupDbName.contains("database") && toDB instanceof SQLiteDB) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_SAME_DB));
        }

        if (toDB.getState() != Database.State.OPEN) toDB.init();

        String prompt = locale.getString(CommandLang.CONFIRM_OVERWRITE_DB,
                toDB.getType().getName(),
                backupDBFile.toPath().toString());

        confirmation.confirm(sender, prompt, choice -> {
            if (Boolean.TRUE.equals(choice)) {
                performRestore(sender, backupDBFile, toDB);
            } else {
                sender.send(colors.getMainColor() + locale.getString(CommandLang.CONFIRM_CANCELLED_DATA));
            }
        });
    }

    public void performRestore(CMDSender sender, File backupDBFile, Database toDB) {
        try {
            SQLiteDB fromDB = sqliteFactory.usingFile(backupDBFile);
            fromDB.init();

            sender.send(locale.getString(CommandLang.DB_WRITE, toDB.getType().getName()));
            DatabaseCopyProcessor databaseCopyProcessor = new DatabaseCopyProcessor(locale, errorLogger, fromDB, toDB, sender::send, DatabaseCopyProcessor.Strategy.CLEAR_DESTINATION_DATABASE);
            processing.submit(databaseCopyProcessor);
        } catch (DBOpException e) {
            errorLogger.error(e, ErrorContext.builder().related(backupDBFile, toDB.getType(), toDB.getState()).build());
            sender.send(locale.getString(CommandLang.PROGRESS_FAIL, e.getMessage()));
        }
    }

    public void onMove(CMDSender sender, @Untrusted Arguments arguments) {
        DBType fromDB = arguments.get(0).flatMap(DBType::getForName)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_INCORRECT_DB, arguments.get(0).orElse(SUPPORTED_DB_OPTIONS))));

        DBType toDB = arguments.get(1).flatMap(DBType::getForName)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_INCORRECT_DB, arguments.get(0).orElse(SUPPORTED_DB_OPTIONS))));

        if (fromDB == toDB) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_SAME_DB));
        }

        String prompt = locale.getString(CommandLang.CONFIRM_OVERWRITE_DB,
                toDB.getName(),
                fromDB.getName());

        confirmation.confirm(sender, prompt, choice -> {
            if (Boolean.TRUE.equals(choice)) {
                performMove(sender, fromDB, toDB, DatabaseCopyProcessor.Strategy.CLEAR_DESTINATION_DATABASE);
            } else {
                sender.send(colors.getMainColor() + locale.getString(CommandLang.CONFIRM_CANCELLED_DATA));
            }
        });
    }

    public void onMerge(CMDSender sender, @Untrusted Arguments arguments) {
        DBType fromDB = arguments.get(0).flatMap(DBType::getForName)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_INCORRECT_DB, arguments.get(0).orElse(SUPPORTED_DB_OPTIONS))));

        DBType toDB = arguments.get(1).flatMap(DBType::getForName)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_INCORRECT_DB, arguments.get(0).orElse(SUPPORTED_DB_OPTIONS))));

        if (fromDB == toDB) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_SAME_DB));
        }

        List<DatabaseCopyProcessor.Strategy> strategies = new ArrayList<>();
        if (arguments.contains("--on-conflict-delete")) {
            strategies.add(DatabaseCopyProcessor.Strategy.SERVER_UUID_CONFLICT_DELETE_SERVER);
        }
        if (arguments.contains("--on-conflict-swap")) {
            strategies.add(DatabaseCopyProcessor.Strategy.SERVER_UUID_CONFLICT_SWAP_UUID);
        }
        if (strategies.size() >= 2) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_DB_TOO_MANY_STRATEGIES));
        }

        DatabaseCopyProcessor.Strategy[] chosenStrategies = strategies.toArray(new DatabaseCopyProcessor.Strategy[0]);

        String prompt = locale.getString(CommandLang.CONFIRM_OVERWRITE_DB,
                toDB.getName(),
                fromDB.getName());

        confirmation.confirm(sender, prompt, choice -> {
            if (Boolean.TRUE.equals(choice)) {
                performMove(sender, fromDB, toDB, chosenStrategies);
            } else {
                sender.send(colors.getMainColor() + locale.getString(CommandLang.CONFIRM_CANCELLED_DATA));
            }
        });
    }

    private void performMove(CMDSender sender, DBType fromDB, DBType toDB, DatabaseCopyProcessor.Strategy... chosenStrategies) {
        try {
            Database fromDatabase = dbSystem.getActiveDatabaseByType(fromDB);
            Database toDatabase = dbSystem.getActiveDatabaseByType(toDB);
            fromDatabase.init();
            toDatabase.init();

            sender.send(locale.getString(CommandLang.DB_WRITE, toDB.getName()));

            DatabaseCopyProcessor databaseCopyProcessor = new DatabaseCopyProcessor(locale, errorLogger, fromDatabase, toDatabase, sender::send, chosenStrategies);
            processing.submit(databaseCopyProcessor);

            boolean movingToCurrentDB = toDatabase.getType() == dbSystem.getDatabase().getType();
            if (movingToCurrentDB) {
                sender.send(locale.getString(CommandLang.HOTSWAP_REMINDER, toDatabase.getType().getConfigName()));
            }
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(sender, fromDB.getName() + "->" + toDB.getName()).build());
            sender.send(locale.getString(CommandLang.PROGRESS_FAIL, e.getMessage()));
        }
    }


    public void onClear(CMDSender sender, @Untrusted Arguments arguments) {
        DBType fromDB = arguments.get(0).flatMap(DBType::getForName)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_INCORRECT_DB, arguments.get(0).orElse(SUPPORTED_DB_OPTIONS))));

        String prompt = locale.getString(CommandLang.CONFIRM_CLEAR_DB, fromDB.getName());

        confirmation.confirm(sender, prompt, choice -> {
            if (Boolean.TRUE.equals(choice)) {
                performClear(sender, fromDB);
            } else {
                sender.send(colors.getMainColor() + locale.getString(CommandLang.CONFIRM_CANCELLED_DATA));
            }
        });
    }

    private void performClear(CMDSender sender, DBType fromDB) {
        try {
            Database fromDatabase = dbSystem.getActiveDatabaseByType(fromDB);
            fromDatabase.init();

            sender.send(locale.getString(CommandLang.DB_REMOVAL, fromDB.getName()));

            fromDatabase.executeTransaction(new RemoveEverythingTransaction())
                    .get(); // Wait for completion
            queryService.dataCleared();
            sender.send(locale.getString(CommandLang.PROGRESS_SUCCESS));

            // Reload plugin to register the server into the database
            // Otherwise errors will start.
            statusCommands.onReload(sender);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (DBOpException | ExecutionException e) {
            sender.send(locale.getString(CommandLang.PROGRESS_FAIL, e.getMessage()));
            errorLogger.error(e, ErrorContext.builder().related(sender, fromDB.getName()).build());
        }
    }

    public void onFixFabricJoinAddresses(CMDSender sender, @Untrusted Arguments arguments) {
        @Untrusted String identifier = arguments.concatenate(" ");
        Optional<ServerUUID> serverUUID = identifiers.getServerUUID(identifier);
        if (serverUUID.isEmpty()) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_SERVER_NOT_FOUND, identifier));
        }

        Database database = dbSystem.getDatabase();

        String prompt = locale.getString(CommandLang.CONFIRM_JOIN_ADDRESS_REMOVAL, identifier, database.getType().getName());

        confirmation.confirm(sender, prompt, choice -> {
            if (Boolean.TRUE.equals(choice)) {
                performJoinAddressRemoval(sender, serverUUID.get(), database);
            } else {
                sender.send(colors.getMainColor() + locale.getString(CommandLang.CONFIRM_CANCELLED_DATA));
            }
        });
    }

    private void performJoinAddressRemoval(CMDSender sender, ServerUUID serverUUID, Database database) {
        try {
            sender.send(locale.getString(CommandLang.DB_WRITE, database.getType().getName()));
            database.executeTransaction(new BadFabricJoinAddressValuePatch(serverUUID))
                    .thenRunAsync(() -> sender.send(locale.getString(CommandLang.PROGRESS_SUCCESS)))
                    .exceptionally(error -> {
                        sender.send(locale.getString(CommandLang.PROGRESS_FAIL, error.getMessage()));
                        return null;
                    });
        } catch (DBOpException e) {
            sender.send(locale.getString(CommandLang.PROGRESS_FAIL, e.getMessage()));
            errorLogger.error(e, ErrorContext.builder().related(sender, database.getType().getName()).build());
        }
    }

    public void onRemove(CMDSender sender, @Untrusted Arguments arguments) {
        @Untrusted String identifier = arguments.concatenate(" ");
        UUID playerUUID = identifiers.getPlayerUUID(identifier);
        if (playerUUID == null) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_PLAYER_NOT_FOUND, identifier));
        }

        Database database = dbSystem.getDatabase();

        String prompt = locale.getString(CommandLang.CONFIRM_REMOVE_PLAYER_DB, playerUUID, database.getType().getName());

        confirmation.confirm(sender, prompt, choice -> {
            if (Boolean.TRUE.equals(choice)) {
                performRemoval(sender, database, playerUUID);
            } else {
                sender.send(colors.getMainColor() + locale.getString(CommandLang.CONFIRM_CANCELLED_DATA));
            }
        });
    }

    private void performRemoval(CMDSender sender, Database database, UUID playerToRemove) {
        try {
            sender.send(locale.getString(CommandLang.DB_REMOVAL_PLAYER, playerToRemove, database.getType().getName()));

            queryService.playerRemoved(playerToRemove);
            database.executeTransaction(new RemovePlayerTransaction(playerToRemove))
                    .get(); // Wait for completion

            sender.send(locale.getString(CommandLang.PROGRESS_SUCCESS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (DBOpException | ExecutionException e) {
            sender.send(locale.getString(CommandLang.PROGRESS_FAIL, e.getMessage()));
            errorLogger.error(e, ErrorContext.builder().related(sender, database.getType().getName(), playerToRemove).build());
        }
    }

    private void ensureDatabaseIsOpen() {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
        }
    }

    public void onUninstalled(CMDSender sender, @Untrusted Arguments arguments) {
        ensureDatabaseIsOpen();
        @Untrusted String identifier = arguments.concatenate(" ");
        Server server = dbSystem.getDatabase()
                .query(ServerQueries.fetchServerMatchingIdentifier(identifier))
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_SERVER_NOT_FOUND, identifier)));

        if (server.getUuid().equals(serverInfo.getServerUUID())) {
            throw new IllegalArgumentException(locale.getString(CommandLang.UNINSTALLING_SAME_SERVER));
        }

        dbSystem.getDatabase().executeTransaction(new SetServerAsUninstalledTransaction(server.getUuid()));
        sender.send(locale.getString(CommandLang.PROGRESS_SUCCESS));
        sender.send(locale.getString(CommandLang.DB_UNINSTALLED));
    }

    public void onHotswap(CMDSender sender, @Untrusted Arguments arguments) {
        DBType toDB = arguments.get(0).flatMap(DBType::getForName)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_INCORRECT_DB, arguments.get(0).orElse(SUPPORTED_DB_OPTIONS))));

        try {
            Database database = dbSystem.getActiveDatabaseByType(toDB);
            database.init();

            if (database.getState() == Database.State.CLOSED) {
                return;
            }

            config.set(DatabaseSettings.TYPE, toDB.getName());
            config.save();
        } catch (DBOpException | IOException e) {
            errorLogger.warn(e, ErrorContext.builder().related(toDB).build());
            sender.send(locale.getString(CommandLang.PROGRESS_FAIL, e.getMessage()));
            return;
        }
        statusCommands.onReload(sender);
    }

    public void onOnlineConversion(CMDSender sender, @Untrusted Arguments arguments) {
        boolean removeOfflinePlayers = arguments.get(0)
                .map("--remove_offline"::equals)
                .orElse(false);
        sender.send(locale.getString(CommandLang.PROGRESS_PREPARING));
        processing.submitNonCritical(() -> {
            Map<UUID, BaseUser> baseUsersByUUID = dbSystem.getDatabase().query(BaseUserQueries.fetchAllBaseUsersByUUID());
            List<String> playerNames = baseUsersByUUID.values().stream().map(BaseUser::getName).collect(Collectors.toList());
            sender.send("Performing lookup for " + playerNames.size() + " uuids from Mojang..");
            sender.send("Preparation estimated complete at: " + clock.apply(System.currentTimeMillis() + playerNames.size() * 100L) + " (due to request rate limiting)");
            Map<String, UUID> onlineUUIDsOfPlayers = getUUIDViaUUIDFetcher(playerNames);

            if (onlineUUIDsOfPlayers.isEmpty()) {
                sender.send(locale.getString(CommandLang.PROGRESS_FAIL, "Did not get any UUIDs from Mojang."));
                return;
            }

            int totalProfiles = baseUsersByUUID.size();
            int offlineOnlyUsers = 0;
            int combine = 0;
            int move = 0;

            List<Transaction> transactions = new ArrayList<>();

            for (BaseUser user : baseUsersByUUID.values()) {
                String playerName = user.getName();
                UUID recordedUUID = user.getUuid();
                UUID actualUUID = onlineUUIDsOfPlayers.get(playerName);

                if (actualUUID == null) {
                    offlineOnlyUsers++;
                    if (removeOfflinePlayers) transactions.add(new RemovePlayerTransaction(recordedUUID));
                }
                if (actualUUID == null || recordedUUID.equals(actualUUID)) {
                    continue;
                }
                BaseUser alreadyExistingProfile = baseUsersByUUID.get(actualUUID);
                if (alreadyExistingProfile == null) {
                    move++;
                    transactions.add(new ChangeUserUUIDTransaction(recordedUUID, actualUUID));
                } else {
                    combine++;
                    transactions.add(new CombineUserTransaction(recordedUUID, actualUUID));
                }
            }

            MessageBuilder prompt = sender.buildMessage()
                    .addPart(colors.getMainColor() + "Moving to online-only UUIDs (irreversible):").newLine()
                    .addPart(colors.getSecondaryColor() + "  Total players in database: " + totalProfiles).newLine()
                    .addPart(colors.getSecondaryColor() + (removeOfflinePlayers ? "Removing (no online UUID): " : "  Offline only (no online UUID): ") + offlineOnlyUsers).newLine()
                    .addPart(colors.getSecondaryColor() + "  Moving to new UUID: " + move).newLine()
                    .addPart(colors.getSecondaryColor() + "  Combining offline and online profiles: " + combine).newLine()
                    .newLine()
                    .addPart(colors.getSecondaryColor() + "  Estimated online UUID players in database after: " + (totalProfiles - combine - offlineOnlyUsers) + (removeOfflinePlayers ? "" : " (+" + offlineOnlyUsers + " offline)")).newLine();

            confirmation.confirm(sender, prompt, choice -> {
                if (Boolean.TRUE.equals(choice)) {
                    transactions.forEach(dbSystem.getDatabase()::executeTransaction);
                    dbSystem.getDatabase().executeTransaction(new Transaction() {
                        @Override
                        protected void performOperations() {
                            sender.send(locale.getString(CommandLang.PROGRESS_SUCCESS));
                        }
                    });
                } else {
                    sender.send(colors.getMainColor() + locale.getString(CommandLang.CONFIRM_CANCELLED_DATA));
                }
            });

        });
    }

    private Map<String, UUID> getUUIDViaUUIDFetcher(List<String> playerNames) {
        try {
            return new UUIDFetcher(playerNames).call();
        } catch (Exception | NoClassDefFoundError failure) {
            errorLogger.error(failure, ErrorContext.builder()
                    .related("Migrating offline uuids to online uuids")
                    .build());
            return new HashMap<>();
        }
    }
}
