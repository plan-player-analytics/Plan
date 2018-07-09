package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.info.InspectCacheRequestProcessor;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import java.util.UUID;

/**
 * This command is used to refresh Inspect page and display link.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class InspectCommand extends CommandNode {

    public InspectCommand() {
        super("inspect", Permissions.INSPECT.getPermission(), CommandType.PLAYER_OR_ARGS);
        setArguments("<player>");
        setShortHelp(Locale.get(Msg.CMD_USG_INSPECT).toString());
        setInDepthHelp(Locale.get(Msg.CMD_HELP_INSPECT).toArray());
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerName(args, sender);

        runInspectTask(playerName, sender);
    }

    private void runInspectTask(String playerName, ISender sender) {
        RunnableFactory.createNew(new AbsRunnable("InspectTask") {
            @Override
            public void run() {
                try {
                    UUID uuid = UUIDUtility.getUUIDOf(playerName);
                    if (uuid == null) {
                        sender.sendMessage(Locale.get(Msg.CMD_FAIL_USERNAME_NOT_VALID).toString());
                        return;
                    }

                    Database activeDB = Database.getActive();
                    if (!activeDB.check().isPlayerRegistered(uuid)) {
                        sender.sendMessage(Locale.get(Msg.CMD_FAIL_USERNAME_NOT_KNOWN).toString());
                        return;
                    }

                    checkWebUserAndNotify(activeDB, sender);
                    Processing.submit(new InspectCacheRequestProcessor(uuid, sender, playerName));
                } catch (DBOpException e) {
                    if (e.isFatal()) {
                        sender.sendMessage("§cFatal database exception occurred: " + e.getMessage());
                    } else {
                        sender.sendMessage("§eNon-Fatal database exception occurred: " + e.getMessage());
                    }
                    Log.toLog(this.getClass(), e);
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }

    private void checkWebUserAndNotify(Database activeDB, ISender sender) {
        if (CommandUtils.isPlayer(sender) && WebServer.getInstance().isAuthRequired()) {
            boolean senderHasWebUser = activeDB.check().doesWebUserExists(sender.getName());

            if (!senderHasWebUser) {
                sender.sendMessage("§e[Plan] You might not have a web user, use /plan register <password>");
            }
        }
    }
}