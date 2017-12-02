package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.container.Session;
import main.java.com.djrapitops.plan.settings.Permissions;
import main.java.com.djrapitops.plan.settings.locale.Locale;
import main.java.com.djrapitops.plan.settings.locale.Msg;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.cache.SessionCache;
import main.java.com.djrapitops.plan.utilities.Condition;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

import static org.bukkit.Bukkit.getPlayer;

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
        super("remove",
                CommandType.PLAYER_OR_ARGS,
                Permissions.MANAGE.getPermission(),
                Locale.get(Msg.CMD_USG_MANAGE_REMOVE).toString(),
                "<player> [-a]");

        this.plugin = plugin;

    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_MANAGE_REMOVE).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Condition.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString(), sender)) {
            return true;
        }

        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.MANAGE);

        runRemoveTask(playerName, sender, args);
        return true;
    }

    private void runRemoveTask(String playerName, ISender sender, String[] args) {
        RunnableFactory.createNew(new AbsRunnable("DBRemoveTask " + playerName) {
            @Override
            public void run() {
                try {
                    UUID uuid = UUIDUtility.getUUIDOf(playerName);
                    String message = Locale.get(Msg.CMD_FAIL_USERNAME_NOT_VALID).toString();

                    if (!Condition.isTrue(Verify.notNull(uuid), message, sender)) {
                        return;
                    }

                    message = Locale.get(Msg.CMD_FAIL_USERNAME_NOT_KNOWN).toString();
                    if (!Condition.isTrue(plugin.getDB().wasSeenBefore(uuid), message, sender)) {
                        return;
                    }

                    message = Locale.get(Msg.MANAGE_FAIL_CONFIRM).parse(Locale.get(Msg.MANAGE_NOTIFY_REMOVE).parse(plugin.getDB().getConfigName()));
                    if (!Condition.isTrue(Verify.contains("-a", args), message, sender)) {
                        return;
                    }

                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_START).parse());
                    try {
                        plugin.getDB().removeAccount(uuid);

                        DataCache dataCache = plugin.getDataCache();
                        Player player = getPlayer(uuid);
                        if (player != null) {
                            SessionCache.getActiveSessions().remove(uuid);
                            dataCache.cacheSession(uuid, new Session(MiscUtils.getTime(), player.getWorld().getName(), player.getGameMode().name()));
                        }
                        sender.sendMessage(Locale.get(Msg.MANAGE_INFO_REMOVE_SUCCESS).parse(playerName, plugin.getDB().getConfigName()));
                    } catch (SQLException e) {
                        Log.toLog(this.getClass().getName(), e);
                        sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString());
                    }
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
