package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.Msg;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
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

    private final Locale locale;

    public ManageRemoveCommand(PlanPlugin plugin) {
        super("remove|delete", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.MANAGE_REMOVE));
        setArguments("<player>", "[-a]");
        setInDepthHelp(locale.get(Msg.CMD_HELP_MANAGE_REMOVE).toArray());
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString()));

        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.MANAGE);

        if (playerName == null) {
            sender.sendMessage(locale.getString(Msg.CMD_FAIL_NO_PERMISSION));
            return;
        }

        runRemoveTask(playerName, sender, args);
    }

    private void runRemoveTask(String playerName, ISender sender, String[] args) {
        RunnableFactory.createNew(new AbsRunnable("DBRemoveTask " + playerName) {
            @Override
            public void run() {
                try {
                    UUID uuid = UUIDUtility.getUUIDOf(playerName);

                    if (uuid == null) {
                        sender.sendMessage(locale.get(Msg.CMD_FAIL_USERNAME_NOT_VALID).toString());
                        return;
                    }

                    Database database = Database.getActive();
                    if (!database.check().isPlayerRegistered(uuid)) {
                        sender.sendMessage(locale.get(Msg.CMD_FAIL_USERNAME_NOT_KNOWN).toString());
                        return;
                    }

                    if (!Verify.contains("-a", args)) {
                        sender.sendMessage(locale.get(Msg.MANAGE_FAIL_CONFIRM).parse(locale.get(Msg.MANAGE_NOTIFY_REMOVE).parse(database.getName())));
                        return;
                    }

                    sender.sendMessage(locale.get(Msg.MANAGE_INFO_START).parse());

                    database.remove().player(uuid);

                    sender.sendMessage(locale.get(Msg.MANAGE_INFO_REMOVE_SUCCESS).parse(playerName, Database.getActive().getConfigName()));
                } catch (DBOpException e) {
                    Log.toLog(this.getClass(), e);
                    sender.sendMessage(locale.get(Msg.MANAGE_INFO_FAIL).toString());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
