package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.database.databases.SQLiteDB;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class ManageBackupCommand extends SubCommand {

    private Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageBackupCommand(Plan plugin) {
        super("backup", "plan.manage", Phrase.CMD_USG_MANAGE_BACKUP+"", CommandType.CONSOLE, "<DB>");

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
            if (args.length < 1) {
                sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS.parse(Phrase.USE_BACKUP+""));
                return true;
            }
            String db = args[0].toLowerCase();
            if (!db.equals("mysql") && !db.equals("sqlite")) {
                sender.sendMessage(Phrase.MANAGE_ERROR_INCORRECT_DB + db);
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
            final Database copyFromDB = database;
            (new BukkitRunnable() {
                @Override
                public void run() {
                    Date now = new Date();
                    SQLiteDB backupDB = new SQLiteDB(plugin, 
                            args[0]+"-backup-"+now.toString().substring(4, 10).replaceAll(" ", "-").replaceAll(":", "-"));
                    
                    if (!backupDB.init()) {
                        sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
                        this.cancel();
                        return;
                    }
                    sender.sendMessage(Phrase.MANAGE_PROCESS_START.parse());
                    backupDB.removeAllData();
                    Set<UUID> uuids = copyFromDB.getSavedUUIDs();                    
                    List<UserData> allUserData = new ArrayList<>();
                    for (UUID uuid : uuids) {
                        allUserData.add(copyFromDB.getUserData(uuid));
                    }
                    backupDB.saveMultipleUserData(allUserData);
                    backupDB.saveCommandUse(copyFromDB.getCommandUse());
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
