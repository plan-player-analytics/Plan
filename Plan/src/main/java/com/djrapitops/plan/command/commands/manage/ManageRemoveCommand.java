package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.task.RslBukkitRunnable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * This manage subcommand is used to remove a single player's data from the
 * database.
 *
 * @author Rsl1122
 */
public class ManageRemoveCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageRemoveCommand(Plan plugin) {
        super("remove", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE_REMOVE + "", Phrase.ARG_PLAYER + " [-a]");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE.toString());
            return true;
        }

        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.MANAGE);

        (new RslBukkitRunnable<Plan>("DBRemoveTask " + playerName) {
            @Override
            public void run() {
                UUID uuid;
                try {
                    uuid = UUIDUtility.getUUIDOf(playerName);
                    if (uuid == null) {
                        throw new Exception("Username doesn't exist.");
                    }
                } catch (Exception e) {
                    sender.sendMessage(Phrase.USERNAME_NOT_VALID.toString());
                    this.cancel();
                    return;
                }
                if (!plugin.getDB().wasSeenBefore(uuid)) {
                    sender.sendMessage(Phrase.USERNAME_NOT_KNOWN.toString());
                    this.cancel();
                    return;
                }
                if (!Arrays.asList(args).contains("-a")) {
                    sender.sendMessage(Phrase.COMMAND_ADD_CONFIRMATION_ARGUMENT.parse(Phrase.WARN_REMOVE.parse(plugin.getDB().getConfigName())));
                    this.cancel();
                    return;
                }

                sender.sendMessage(Phrase.MANAGE_PROCESS_START.parse());
                try {
                    plugin.getHandler().getDataCache().remove(uuid);
                    if (plugin.getDB().removeAccount(uuid.toString())) {
                        sender.sendMessage(Phrase.MANAGE_REMOVE_SUCCESS.parse(playerName, plugin.getDB().getConfigName()));
                    } else {
                        sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL + "");
                    }
                } catch (SQLException e) {
                    Log.toLog(this.getClass().getName(), e);
                    sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL + "");
                }
                this.cancel();
            }
        }).runTaskAsynchronously(plugin);
        return true;
    }
}
