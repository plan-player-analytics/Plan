package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.data.ServerData;
import com.djrapitops.plan.data.UserData;

import com.djrapitops.plan.database.Database;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.utilities.DataCombineUtils;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class ManageCombineCommand extends SubCommand {

    private Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageCombineCommand(Plan plugin) {
        super("combine", "plan.manage", "Copy data from one database to another & combine values", CommandType.CONSOLE_WITH_ARGUMENTS, "<fromDB> <toDB> [-a]");
        this.plugin = plugin;
    }

    /**
     * Subcommand inspect.
     *
     * Adds player's data from DataCache/DB to the InspectCache for amount of
     * time specified in the config, and clears the data from Cache with a timer
     * task.
     *
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args Player's name or nothing - if empty sender's name is used.
     * @return true in all cases.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS.toString() + " Use /plan manage combine <fromDB> <toDB> [-a]");
            return true;
        }
        String fromDB = args[0].toLowerCase();
        String toDB = args[1].toLowerCase();
        if (!fromDB.equals("mysql") && !fromDB.equals("sqlite")) {
            sender.sendMessage(Phrase.MANAGE_ERROR_INCORRECT_DB + fromDB);
            return true;
        }
        if (!toDB.equals("mysql") && !toDB.equals("sqlite")) {
            sender.sendMessage(Phrase.MANAGE_ERROR_INCORRECT_DB + toDB);
            return true;
        }
        if (fromDB.equals(toDB)) {
            sender.sendMessage(Phrase.MANAGE_ERROR_SAME_DB + "");
            return true;
        }
        if (!Arrays.asList(args).contains("-a")) {
            sender.sendMessage(Phrase.COMMAND_ADD_CONFIRMATION_ARGUMENT.toString() + " Data in " + args[1] + "-database will be rewritten!");
            return true;
        }

        Database fromDatabase = null;
        Database toDatabase = null;
        for (Database database : plugin.getDatabases()) {
            if (fromDB.equalsIgnoreCase(database.getConfigName())) {
                fromDatabase = database;
                fromDatabase.init();
            }
            if (toDB.equalsIgnoreCase(database.getConfigName())) {
                toDatabase = database;
                toDatabase.init();
            }
        }
        if (fromDatabase == null) {
            sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
            plugin.logError(fromDB + " was null!");
            return true;
        }
        if (toDatabase == null) {
            sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
            plugin.logError(toDB + " was null!");
            return true;
        }

        OfflinePlayer[] offlinePlayers;
        try {
            offlinePlayers = plugin.getServer().getOfflinePlayers();
        } catch (IndexOutOfBoundsException e) {
            sender.sendMessage(Phrase.MANAGE_ERROR_NO_PLAYERS + " (" + fromDB + ")");
            return true;
        }
        final List<UUID> fromUUIDS = new ArrayList<>();
        final List<UUID> toUUIDS = new ArrayList<>();
        try {
            for (OfflinePlayer p : offlinePlayers) {
                UUID uuid = p.getUniqueId();
                if (fromDatabase.wasSeenBefore(uuid)) {
                    fromUUIDS.add(uuid);
                }
                if (toDatabase.wasSeenBefore(uuid)) {
                    toUUIDS.add(uuid);
                }
            }
            if (fromUUIDS.isEmpty() && toUUIDS.isEmpty()) {
                sender.sendMessage(Phrase.MANAGE_ERROR_NO_PLAYERS + " (" + fromDB + ")");
                return true;
            }

            final Database moveFromDB = fromDatabase;
            final Database moveToDB = toDatabase;
            (new BukkitRunnable() {
                @Override
                public void run() {
                    sender.sendMessage(Phrase.MANAGE_PROCESS_START.parse());
                    HashMap<UUID, UserData> allFromUserData = new HashMap<>();
                    HashMap<UUID, UserData> allToUserData = new HashMap<>();
                    for (UUID uuid : fromUUIDS) {
                        allFromUserData.put(uuid, moveFromDB.getUserData(uuid));
                    }
                    for (UUID uuid : toUUIDS) {
                        allToUserData.put(uuid, moveToDB.getUserData(uuid));
                    }
                    Set<UUID> uuids = new HashSet<>();
                    uuids.addAll(toUUIDS);
                    uuids.addAll(fromUUIDS);

                    List<UserData> combinedUserData = DataCombineUtils.combineUserDatas(allFromUserData, allToUserData, uuids);

                    HashMap<Long, ServerData> fromServerData = moveFromDB.getServerDataHashMap();
                    HashMap<Long, ServerData> toServerData = moveToDB.getServerDataHashMap();
                    HashMap<Long, ServerData> combinedServerData = DataCombineUtils.combineServerDatas(fromServerData, toServerData);

                    HashMap<String, Integer> commandUse = DataCombineUtils.combineCommandUses(DataCombineUtils.getCommandUse(fromServerData), DataCombineUtils.getCommandUse(toServerData));

                    moveToDB.removeAllData();

                    moveToDB.saveServerDataHashMap(combinedServerData);
                    moveToDB.saveMultipleUserData(combinedUserData);
                    moveToDB.saveCommandUse(commandUse);

                    sender.sendMessage(Phrase.MANAGE_MOVE_SUCCESS + "");
                    if (!toDB.equals(plugin.getDB().getConfigName())) {
                        sender.sendMessage(Phrase.MANAGE_DB_CONFIG_REMINDER + "");
                    }
                    this.cancel();
                }

            }).runTaskAsynchronously(plugin);

        } catch (NullPointerException e) {
            sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
        }
        return true;
    }

}
