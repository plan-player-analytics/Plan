package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

import java.util.UUID;

/**
 * This manage subcommand is used to remove a single player's data from the
 * database.
 *
 * @author Rsl1122
 */
public class ManageRemoveCommand extends CommandNode {

    public ManageRemoveCommand() {
        super("remove|delete", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);
        setShortHelp(Locale.get(Msg.CMD_USG_MANAGE_REMOVE).toString());
        setArguments("<player>", "[-a]");
        setInDepthHelp(Locale.get(Msg.CMD_HELP_MANAGE_REMOVE).toArray());
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString()));

        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.MANAGE);

        runRemoveTask(playerName, sender, args);
    }

    private void runRemoveTask(String playerName, ISender sender, String[] args) {
        RunnableFactory.createNew(new AbsRunnable("DBRemoveTask " + playerName) {
            @Override
            public void run() {
                try {
                    UUID uuid = UUIDUtility.getUUIDOf(playerName);

                    if (uuid == null) {
                        sender.sendMessage(Locale.get(Msg.CMD_FAIL_USERNAME_NOT_VALID).toString());
                        return;
                    }

                    Database database = Database.getActive();
                    if (!database.check().isPlayerRegistered(uuid)) {
                        sender.sendMessage(Locale.get(Msg.CMD_FAIL_USERNAME_NOT_KNOWN).toString());
                        return;
                    }

                    if (!Verify.contains("-a", args)) {
                        sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_CONFIRM).parse(Locale.get(Msg.MANAGE_NOTIFY_REMOVE).parse(database.getName())));
                        return;
                    }

                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_START).parse());

                    database.remove().player(uuid);

                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_REMOVE_SUCCESS).parse(playerName, Database.getActive().getConfigName()));
                } catch (DBOpException e) {
                    Log.toLog(this.getClass(), e);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
