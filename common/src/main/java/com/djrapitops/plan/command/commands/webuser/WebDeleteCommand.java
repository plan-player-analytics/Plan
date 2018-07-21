package com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Arrays;

/**
 * Subcommand for deleting a WebUser.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebDeleteCommand extends CommandNode {

    public WebDeleteCommand() {
        super("delete|remove", Permissions.MANAGE_WEB.getPerm(), CommandType.PLAYER_OR_ARGS);
        setShortHelp(Locale.get(Msg.CMD_USG_WEB_DELETE).toString());
        setArguments("<username>");
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(Locale.get(Msg.CMD_FAIL_REQ_ARGS).parse(Arrays.toString(this.getArguments()))));

        Database database = Database.getActive();
        String user = args[0];

        RunnableFactory.createNew(new AbsRunnable("Webuser Delete Task: " + user) {
            @Override
            public void run() {
                try {
                    if (!database.check().doesWebUserExists(user)) {
                        sender.sendMessage("§c[Plan] User Doesn't exist.");
                        return;
                    }
                    database.remove().webUser(user);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_SUCCESS).parse());
                } catch (Exception ex) {
                    Log.toLog(this.getClass(), ex);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).parse());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }

}
