package main.java.com.djrapitops.plan.command.commands.manage;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.CommandType;
import main.java.com.djrapitops.plan.command.SubCommand;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.utilities.ManageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * This manage subcommand is used to restore a backup.db file in the
 * /plugins/Plan folder.
 *
 * @author Rsl1122
 */
public class ManageRestoreCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageRestoreCommand(Plan plugin) {
        super("restore", Permissions.MANAGE, Phrase.CMD_USG_MANAGE_RESTORE + "", CommandType.CONSOLE, Phrase.ARG_RESTORE + "");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        try {
            if (args.length < 2) {
                sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS.parse(Phrase.USE_RESTORE + ""));
                return true;
            }
            String db = args[1].toLowerCase();
            if (!db.equals("mysql") && !db.equals("sqlite")) {
                sender.sendMessage(Phrase.MANAGE_ERROR_INCORRECT_DB + db);
                return true;
            }
            if (!Arrays.asList(args).contains("-a")) {
                sender.sendMessage(Phrase.COMMAND_ADD_CONFIRMATION_ARGUMENT.parse(Phrase.WARN_REWRITE.parse(args[1])));
                return true;
            }
            Database database = null;
            for (Database sqldb : plugin.getDatabases()) {
                if (db.equalsIgnoreCase(sqldb.getConfigName())) {
                    database = sqldb;
                    if (!database.init()) {
                        sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
                        return true;
                    }
                }
            }
            if (database == null) {
                sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
                Log.error(db + " was null!");
                return true;
            }
            final Database copyToDB = database;
            BukkitTask asyncRestoreTask = new BukkitRunnable() {
                @Override
                public void run() {
                    String backupDBName = args[0];

                    File backupDBFile = new File(plugin.getDataFolder(), backupDBName + (backupDBName.contains(".db") ? "" : ".db"));
                    if (!backupDBFile.exists()) {
                        sender.sendMessage(Phrase.MANAGE_ERROR_BACKUP_FILE_NOT_FOUND + " " + args[0]);
                        this.cancel();
                        return;
                    }
                    if (backupDBName.contains(".db")) {
                        backupDBName = backupDBName.replace(".db", "");
                    }
                    SQLiteDB backupDB = new SQLiteDB(plugin, backupDBName);
                    if (!backupDB.init()) {
                        sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
                        this.cancel();
                        return;
                    }
                    sender.sendMessage(Phrase.MANAGE_PROCESS_START.parse());
                    final Collection<UUID> uuids = ManageUtils.getUUIDS(backupDB);
                    if (uuids.isEmpty()) {
                        sender.sendMessage(Phrase.MANAGE_ERROR_NO_PLAYERS + " (" + backupDBName + ")");
                        this.cancel();
                        return;
                    }
                    if (ManageUtils.clearAndCopy(copyToDB, backupDB, uuids)) {
                        sender.sendMessage(Phrase.MANAGE_COPY_SUCCESS.toString());
                    } else {
                        sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL.toString());
                    }
                    this.cancel();
                }
            }.runTaskAsynchronously(plugin);
        } catch (NullPointerException e) {
            sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
        }
        return true;
    }
}
