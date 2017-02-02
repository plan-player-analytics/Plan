package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.data.ServerData;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.database.databases.SQLiteDB;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.utilities.DataCombineUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class ManageRestoreCommand extends SubCommand {

    private Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageRestoreCommand(Plan plugin) {
        super("restore", "plan.restore", "Restore a database from a backup file", CommandType.CONSOLE, "<Filename.db> <dbTo> [-a]");

        this.plugin = plugin;
    }

    /**
     * Subcommand Manage backup.
     *
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args Player's name or nothing - if empty sender's name is used.
     * @return true in all cases.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        try {
            if (args.length < 2) {
                sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS.toString() + " Use /plan manage restore <Filename.db> <dbTo> [-a]");
                return true;
            }
            String db = args[1].toLowerCase();
            if (!db.equals("mysql") && !db.equals("sqlite")) {
                sender.sendMessage(Phrase.MANAGE_ERROR_INCORRECT_DB + db);
                return true;
            }
            if (!Arrays.asList(args).contains("-a")) {
                sender.sendMessage(Phrase.COMMAND_ADD_CONFIRMATION_ARGUMENT.toString() + " Data in " + args[1] + "-database will be rewritten!");
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
                plugin.logError(db + " was null!");
                return true;
            }
            final Database copyToDB = database;
            (new BukkitRunnable() {
                @Override
                public void run() {
                    String backupDBName = args[0];

                    File backupDBFile = new File(plugin.getDataFolder(), backupDBName + (backupDBName.contains(".db") ? "" : ".db"));
                    if (!backupDBFile.exists()) {
                        sender.sendMessage(Phrase.MANAGE_ERROR_BACKUP_FILE_NOT_FOUND + " "+args[0]);
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
                    copyToDB.removeAllData();
                    Set<UUID> uuids = backupDB.getSavedUUIDs();
                    for (UUID uuid : uuids) {
                        copyToDB.saveUserData(uuid, backupDB.getUserData(uuid));
                    }
                    HashMap<Long, ServerData> serverDataHashMap = backupDB.getServerDataHashMap();
                    copyToDB.saveServerDataHashMap(serverDataHashMap);
                    copyToDB.saveCommandUse(DataCombineUtils.getCommandUse(serverDataHashMap));
                    sender.sendMessage(Phrase.MANAGE_COPY_SUCCESS.toString());
                    this.cancel();
                }
            }).runTaskAsynchronously(plugin);
        } catch (NullPointerException e) {
            sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
        }
        return true;
    }
}
