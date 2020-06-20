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
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.query.QuerySvc;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.ManageLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.SQLiteDB;
import com.djrapitops.plan.storage.database.transactions.BackupCopyTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemovePlayerTransaction;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.L;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Singleton
public class DatabaseCommands {

    private final Locale locale;
    private final Confirmation confirmation;
    private final ColorScheme colors;
    private final PlanFiles files;
    private final DBSystem dbSystem;
    private final SQLiteDB.Factory sqliteFactory;
    private final QuerySvc queryService;
    private final Identifiers identifiers;
    private final PluginStatusCommands statusCommands;
    private final ErrorLogger errorLogger;

    private final Formatter<Long> timestamp;

    @Inject
    public DatabaseCommands(
            Locale locale,
            Confirmation confirmation,
            ColorScheme colors,
            PlanFiles files,
            DBSystem dbSystem,
            SQLiteDB.Factory sqliteFactory,
            QuerySvc queryService,
            Formatters formatters,
            Identifiers identifiers,
            PluginStatusCommands statusCommands,
            ErrorLogger errorLogger
    ) {
        this.locale = locale;
        this.confirmation = confirmation;
        this.colors = colors;
        this.files = files;
        this.dbSystem = dbSystem;
        this.sqliteFactory = sqliteFactory;
        this.queryService = queryService;
        this.identifiers = identifiers;
        this.statusCommands = statusCommands;
        this.errorLogger = errorLogger;

        this.timestamp = formatters.iso8601NoClockLong();
    }

    public void onBackup(CMDSender sender, Arguments arguments) {
        String dbName = arguments.get(0)
                .orElse(dbSystem.getDatabase().getType().getName())
                .toLowerCase();

        if (!DBType.exists(dbName)) {
            throw new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, dbName));
        }

        Database fromDB = dbSystem.getActiveDatabaseByName(dbName);
        if (fromDB.getState() != Database.State.OPEN) fromDB.init();

        performBackup(sender, arguments, dbName, fromDB);
        sender.send(locale.getString(ManageLang.PROGRESS_SUCCESS));
    }

    public void performBackup(CMDSender sender, Arguments arguments, String dbName, Database fromDB) {
        Database toDB = null;
        try {
            String timeStamp = timestamp.apply(System.currentTimeMillis());
            String fileName = dbName + "-backup-" + timeStamp;
            sender.send("Creating a backup file '" + fileName + ".db' with contents of " + dbName);
            toDB = sqliteFactory.usingFileCalled(fileName);
            toDB.init();
            toDB.executeTransaction(new BackupCopyTransaction(fromDB, toDB)).get();
        } catch (DBOpException | ExecutionException e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder().related(sender, arguments).build());
        } catch (InterruptedException e) {
            toDB.close();
            Thread.currentThread().interrupt();
        } finally {
            if (toDB != null) {
                toDB.close();
            }
        }
    }

    public void onRestore(String mainCommand, CMDSender sender, Arguments arguments) {
        String backupDbName = arguments.get(0)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, 1, "<backup-file>")));

        boolean containsDBFileExtension = backupDbName.endsWith(".db");
        File backupDBFile = files.getFileFromPluginFolder(backupDbName + (containsDBFileExtension ? "" : ".db"));

        if (!backupDBFile.exists()) {
            throw new IllegalArgumentException(locale.getString(ManageLang.FAIL_FILE_NOT_FOUND, backupDBFile.getAbsolutePath()));
        }

        String dbName = arguments.get(1)
                .orElse(dbSystem.getDatabase().getType().getName())
                .toLowerCase();
        if (!DBType.exists(dbName)) {
            throw new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, dbName));
        }

        Database toDB = dbSystem.getActiveDatabaseByName(dbName);

        // Check against restoring from database.db as it is active database
        if (backupDbName.contains("database") && toDB instanceof SQLiteDB) {
            throw new IllegalArgumentException(locale.getString(ManageLang.FAIL_SAME_DB));
        }

        if (toDB.getState() != Database.State.OPEN) toDB.init();

        if (sender.isPlayer()) {
            sender.buildMessage()
                    .addPart(colors.getMainColor() + "You are about to overwrite data in Plan " + toDB.getType().getName() + " with data in " + backupDBFile.toPath()).newLine()
                    .addPart("Confirm: ").addPart("§2§l[\u2714]").command("/" + mainCommand + " accept")
                    .addPart(" ")
                    .addPart("§4§l[\u2718]").command("/" + mainCommand + " cancel")
                    .send();
        } else {
            sender.buildMessage()
                    .addPart(colors.getMainColor() + "You are about to overwrite data in Plan " + toDB.getType().getName() + " with data in " + backupDBFile.toPath()).newLine()
                    .addPart("Confirm: ").addPart("§a/" + mainCommand + " accept")
                    .addPart(" ")
                    .addPart("§c/" + mainCommand + " cancel")
                    .send();
        }

        confirmation.confirm(sender, choice -> {
            if (choice) {
                performRestore(sender, backupDBFile, toDB);
            } else {
                sender.send(colors.getMainColor() + "Cancelled. No data was changed.");
            }
        });
    }

    public void performRestore(CMDSender sender, File backupDBFile, Database toDB) {
        try {
            SQLiteDB fromDB = sqliteFactory.usingFile(backupDBFile);
            fromDB.init();

            sender.send("Writing to " + toDB.getType().getName() + "..");
            toDB.executeTransaction(new BackupCopyTransaction(fromDB, toDB)).get();
            sender.send(locale.getString(ManageLang.PROGRESS_SUCCESS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (DBOpException | ExecutionException e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder().related(backupDBFile, toDB.getType(), toDB.getState()).build());
            sender.send(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
        }
    }

    public void onMove(String mainCommand, CMDSender sender, Arguments arguments) {
        DBType fromDB = arguments.get(0).flatMap(DBType::getForName)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, arguments.get(0).orElse("<MySQL/SQLite/H2>"))));

        DBType toDB = arguments.get(1).flatMap(DBType::getForName)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, arguments.get(0).orElse("<MySQL/SQLite/H2>"))));

        if (fromDB == toDB) {
            throw new IllegalArgumentException(locale.getString(ManageLang.FAIL_SAME_DB));
        }

        if (sender.isPlayer()) {
            sender.buildMessage()
                    .addPart(colors.getMainColor() + "You are about to overwrite data in Plan " + toDB.getName() + " with data in " + fromDB.getName()).newLine()
                    .addPart("Confirm: ").addPart("§2§l[\u2714]").command("/" + mainCommand + " accept")
                    .addPart(" ")
                    .addPart("§4§l[\u2718]").command("/" + mainCommand + " cancel")
                    .send();
        } else {
            sender.buildMessage()
                    .addPart(colors.getMainColor() + "You are about to overwrite data in Plan " + toDB.getName() + " with data in " + fromDB.getName()).newLine()
                    .addPart("Confirm: ").addPart("§a/" + mainCommand + " accept")
                    .addPart(" ")
                    .addPart("§c/" + mainCommand + " cancel")
                    .send();
        }

        confirmation.confirm(sender, choice -> {
            if (choice) {
                performMove(sender, fromDB, toDB);
            } else {
                sender.send(colors.getMainColor() + "Cancelled. No data was changed.");
            }
        });
    }

    private void performMove(CMDSender sender, DBType fromDB, DBType toDB) {
        Database fromDatabase = dbSystem.getActiveDatabaseByType(fromDB);
        Database toDatabase = dbSystem.getActiveDatabaseByType(toDB);
        fromDatabase.init();
        toDatabase.init();

        try {
            sender.send("Writing to " + toDB.getName() + "..");

            fromDatabase.executeTransaction(new BackupCopyTransaction(fromDatabase, toDatabase)).get();

            sender.send(locale.getString(ManageLang.PROGRESS_SUCCESS));

            boolean movingToCurrentDB = toDatabase.getType() == dbSystem.getDatabase().getType();
            if (movingToCurrentDB) {
                sender.send(locale.getString(ManageLang.HOTSWAP_REMINDER, toDatabase.getType().getConfigName()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder().related(sender, fromDB.getName() + "->" + toDB.getName()).build());
            sender.send(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
        }
    }


    public void onClear(String mainCommand, CMDSender sender, Arguments arguments) {
        DBType fromDB = arguments.get(0).flatMap(DBType::getForName)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, arguments.get(0).orElse("<MySQL/SQLite/H2>"))));

        if (sender.isPlayer()) {
            sender.buildMessage()
                    .addPart(colors.getMainColor() + "You are about to remove all Plan-data in " + fromDB.getName()).newLine()
                    .addPart("Confirm: ").addPart("§2§l[\u2714]").command("/" + mainCommand + " accept")
                    .addPart(" ")
                    .addPart("§4§l[\u2718]").command("/" + mainCommand + " cancel")
                    .send();
        } else {
            sender.buildMessage()
                    .addPart(colors.getMainColor() + "You are about to remove all Plan-data in " + fromDB.getName()).newLine()
                    .addPart("Confirm: ").addPart("§a/" + mainCommand + " accept")
                    .addPart(" ")
                    .addPart("§c/" + mainCommand + " cancel")
                    .send();
        }

        confirmation.confirm(sender, choice -> {
            if (choice) {
                performClear(sender, fromDB);
            } else {
                sender.send(colors.getMainColor() + "Cancelled. No data was changed.");
            }
        });
    }

    private void performClear(CMDSender sender, DBType fromDB) {
        try {
            Database fromDatabase = dbSystem.getActiveDatabaseByType(fromDB);
            fromDatabase.init();

            sender.send("Removing Plan-data from " + fromDB.getName() + "..");

            fromDatabase.executeTransaction(new RemoveEverythingTransaction())
                    .get(); // Wait for completion
            queryService.dataCleared();
            sender.send(locale.getString(ManageLang.PROGRESS_SUCCESS));

            // Reload plugin to register the server into the database
            // Otherwise errors will start.
            statusCommands.onReload(sender, new Arguments(Collections.emptyList()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (DBOpException | ExecutionException e) {
            sender.send(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            errorLogger.log(L.ERROR, e, ErrorContext.builder().related(sender, fromDB.getName()).build());
        }
    }

    public void onRemove(String mainCommand, CMDSender sender, Arguments arguments) {
        String identifier = arguments.concatenate(" ");
        UUID playerUUID = identifiers.getPlayerUUID(identifier);
        if (playerUUID == null) {
            throw new IllegalArgumentException("Player '" + identifier + "' was not found, they have no UUID.");
        }

        Database database = dbSystem.getDatabase();

        if (sender.isPlayer()) {
            sender.buildMessage()
                    .addPart(colors.getMainColor() + "You are about to remove data of " + playerUUID + " from " + database.getType().getName()).newLine()
                    .addPart("Confirm: ").addPart("§2§l[\u2714]").command("/" + mainCommand + " accept")
                    .addPart(" ")
                    .addPart("§4§l[\u2718]").command("/" + mainCommand + " cancel")
                    .send();
        } else {
            sender.buildMessage()
                    .addPart(colors.getMainColor() + "You are about to remove data of " + playerUUID + " from " + database.getType().getName()).newLine()
                    .addPart("Confirm: ").addPart("§a/" + mainCommand + " accept")
                    .addPart(" ")
                    .addPart("§c/" + mainCommand + " cancel")
                    .send();
        }

        confirmation.confirm(sender, choice -> {
            if (choice) {
                performRemoval(sender, database, playerUUID);
            } else {
                sender.send(colors.getMainColor() + "Cancelled. No data was changed.");
            }
        });
    }

    private void performRemoval(CMDSender sender, Database database, UUID playerToRemove) {
        try {
            sender.send("Removing data of " + playerToRemove + " from " + database.getType().getName() + "..");

            queryService.playerRemoved(playerToRemove);
            database.executeTransaction(new RemovePlayerTransaction(playerToRemove))
                    .get(); // Wait for completion

            sender.send(locale.getString(ManageLang.PROGRESS_SUCCESS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (DBOpException | ExecutionException e) {
            sender.send(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            errorLogger.log(L.ERROR, e, ErrorContext.builder().related(sender, database.getType().getName(), playerToRemove).build());
        }
    }
}
