package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.FatalDBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.processors.info.InspectCacheRequestProcessor;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.utilities.Condition;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;
import org.bukkit.ChatColor;

import java.util.UUID;

/**
 * This command is used to refresh Inspect page and display link.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class InspectCommand extends SubCommand {

    public InspectCommand() {
        super("inspect",
                CommandType.PLAYER_OR_ARGS,
                Permissions.INSPECT.getPermission(),
                Locale.get(Msg.CMD_USG_INSPECT).toString(),
                "<player>");
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_INSPECT).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerName(args, sender);

        runInspectTask(playerName, sender);
        return true;
    }

    private void runInspectTask(String playerName, ISender sender) {
        RunnableFactory.createNew(new AbsRunnable("InspectTask") {
            @Override
            public void run() {
                try {
                    Database activeDB = Database.getActive();
                    UUID uuid = UUIDUtility.getUUIDOf(playerName);
                    if (!Condition.isTrue(Verify.notNull(uuid), Locale.get(Msg.CMD_FAIL_USERNAME_NOT_VALID).toString(), sender)) {
                        return;
                    }
                    if (!Condition.isTrue(activeDB.check().isPlayerRegistered(uuid), Locale.get(Msg.CMD_FAIL_USERNAME_NOT_KNOWN).toString(), sender)) {
                        return;
                    }
                    if (CommandUtils.isPlayer(sender) && WebServer.getInstance().isAuthRequired()) {
                        boolean senderHasWebUser = activeDB.check().doesWebUserExists(sender.getName());

                        if (!senderHasWebUser) {
                            sender.sendMessage(ChatColor.YELLOW + "[Plan] You might not have a web user, use /plan register <password>");
                        }
                    }
                    new InspectCacheRequestProcessor(uuid, sender, playerName).queue();
                } catch (FatalDBException ex) {
                    Log.toLog(this.getClass(), ex);
                    sender.sendMessage(ChatColor.RED + "Fatal database exception occurred: " + ex.getMessage());
                } catch (DBException ex) {
                    Log.toLog(this.getClass(), ex);
                    sender.sendMessage(ChatColor.YELLOW + "Non-Fatal database exception occurred: " + ex.getMessage());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}