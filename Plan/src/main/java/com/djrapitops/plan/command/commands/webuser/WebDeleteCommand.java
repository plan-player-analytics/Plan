package com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.utilities.Condition;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import net.md_5.bungee.api.ChatColor;

/**
 * Subcommand for deleting a WebUser.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebDeleteCommand extends SubCommand {

    public WebDeleteCommand() {
        super("delete, remove",
                CommandType.PLAYER_OR_ARGS,
                Permissions.MANAGE_WEB.getPerm(),
                Locale.get(Msg.CMD_USG_WEB_DELETE).toString(),
                "<username>");
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Condition.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).parse() + " <username>", sender)) {
            return true;
        }
        Database database = Database.getActive();
        String user = args[0];

        RunnableFactory.createNew(new AbsRunnable("Webuser Delete Task: " + user) {
            @Override
            public void run() {
                try {
                    if (!Condition.isTrue(database.check().doesWebUserExists(user), ChatColor.RED + "[Plan] User Doesn't exist.", sender)) {
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
        return true;
    }

}
