package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;

import java.sql.SQLException;
import java.util.UUID;

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
        super("remove", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE_REMOVE.toString(), Phrase.ARG_PLAYER + " [-a]");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(args.length >= 1, Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE.toString(), sender)) {
            return true;
        }

        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.MANAGE);

        runRemoveTask(playerName, sender, args);
        return true;
    }

    private void runRemoveTask(String playerName, ISender sender, String[] args) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("DBRemoveTask " + playerName) {
            @Override
            public void run() {
                try {
                    UUID uuid = UUIDUtility.getUUIDOf(playerName);
                    String message = Phrase.USERNAME_NOT_VALID.toString();
                    if (!Check.isTrue(Verify.notNull(uuid), message, sender)) {
                        return;
                    }
                    message = Phrase.USERNAME_NOT_KNOWN.toString();
                    if (!Check.isTrue(plugin.getDB().wasSeenBefore(uuid), message, sender)) {
                        return;
                    }
                    message = Phrase.COMMAND_ADD_CONFIRMATION_ARGUMENT.parse(Phrase.WARN_REMOVE.parse(plugin.getDB().getConfigName()));
                    if (!Check.isTrue(Verify.contains("-a", args), message, sender)) {
                        return;
                    }

                    sender.sendMessage(Phrase.MANAGE_PROCESS_START.parse());
                    try {
                        plugin.getHandler().getDataCache().remove(uuid);
                        if (plugin.getDB().removeAccount(uuid.toString())) {
                            sender.sendMessage(Phrase.MANAGE_REMOVE_SUCCESS.parse(playerName, plugin.getDB().getConfigName()));
                        } else {
                            sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL.toString());
                        }
                    } catch (SQLException e) {
                        Log.toLog(this.getClass().getName(), e);
                        sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL.toString());
                    }
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
