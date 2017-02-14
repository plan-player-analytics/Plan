package main.java.com.djrapitops.plan.command.commands.manage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.CommandType;
import main.java.com.djrapitops.plan.command.SubCommand;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
        super("restore", "plan.restore", Phrase.CMD_USG_MANAGE_RESTORE+"", CommandType.CONSOLE, Phrase.ARG_RESTORE+"");

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
                sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS.parse(Phrase.USE_RESTORE+""));
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
                plugin.logError(db + " was null!");
                return true;
            }
            final Database copyToDB = database;
            BukkitTask asyncRestoreTask = (new BukkitRunnable() {
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
                    List<UserData> allUserData = new ArrayList<>();
                    for (UUID uuid : uuids) {
                        allUserData.add(backupDB.getUserData(uuid));
                    }
                    copyToDB.saveMultipleUserData(allUserData);
                    copyToDB.saveCommandUse(backupDB.getCommandUse());
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
