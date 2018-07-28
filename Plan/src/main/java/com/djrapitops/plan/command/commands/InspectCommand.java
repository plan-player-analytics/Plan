package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.Msg;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.info.InspectCacheRequestProcessor;
import com.djrapitops.plan.system.settings.Permissions;
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

    private final Locale locale;

    public InspectCommand(PlanPlugin plugin) {
        super("inspect", Permissions.INSPECT.getPermission(), CommandType.PLAYER_OR_ARGS);
        setArguments("<player>");

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.INSPECT));
        setInDepthHelp(locale.get(Msg.CMD_HELP_INSPECT).toArray());
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerName(args, sender);

        if (playerName == null) {
            sender.sendMessage(locale.getString(Msg.CMD_FAIL_NO_PERMISSION));
        }

        runInspectTask(playerName, sender);
    }

    private void runInspectTask(String playerName, ISender sender) {
        RunnableFactory.createNew(new AbsRunnable("InspectTask") {
            @Override
            public void run() {
                try {
                    UUID uuid = UUIDUtility.getUUIDOf(playerName);
                    if (uuid == null) {
                        sender.sendMessage(locale.getString(Msg.CMD_FAIL_USERNAME_NOT_VALID));
                        return;
                    }

                    Database activeDB = Database.getActive();
                    if (!activeDB.check().isPlayerRegistered(uuid)) {
                        sender.sendMessage(locale.getString(Msg.CMD_FAIL_USERNAME_NOT_KNOWN));
                        return;
                    }

                    checkWebUserAndNotify(activeDB, sender);
                    Processing.submit(new InspectCacheRequestProcessor(uuid, sender, playerName, locale));
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