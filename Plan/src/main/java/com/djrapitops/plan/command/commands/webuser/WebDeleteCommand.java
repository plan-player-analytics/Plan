package com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.settings.Permissions;
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

    private final Locale locale;

    public WebDeleteCommand(PlanPlugin plugin) {
        super("delete|remove", Permissions.MANAGE_WEB.getPerm(), CommandType.PLAYER_OR_ARGS);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.WEB_DELETE));
        setArguments("<username>");
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        Database database = Database.getActive();
        String user = args[0];

        RunnableFactory.createNew("Webuser Delete Task: " + user, new AbsRunnable() {
            @Override
            public void run() {
                try {
                    if (!database.check().doesWebUserExists(user)) {
                        sender.sendMessage("§c[Plan] User Doesn't exist.");
                        return;
                    }
                    database.remove().webUser(user);
                    sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
                } catch (Exception e) {
                    Log.toLog(this.getClass(), e);
                    sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }

}
