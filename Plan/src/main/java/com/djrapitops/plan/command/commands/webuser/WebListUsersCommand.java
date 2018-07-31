package com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.utilities.comparators.WebUserComparator;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import java.util.List;

/**
 * Subcommand for checking WebUser list.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebListUsersCommand extends CommandNode {

    private final Locale locale;

    public WebListUsersCommand(PlanPlugin plugin) {
        super("list", Permissions.MANAGE_WEB.getPerm(), CommandType.CONSOLE);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.WEB_LIST));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        RunnableFactory.createNew(new AbsRunnable("Web user List Task") {
            @Override
            public void run() {
                try {
                    List<WebUser> users = Database.getActive().fetch().getWebUsers();
                    users.sort(new WebUserComparator());
                    sender.sendMessage(locale.getString(CommandLang.HEADER_WEB_USERS, users.size()));
                    for (WebUser user : users) {
                        sender.sendMessage(locale.getString(CommandLang.WEB_USER_LIST, user.getName(), user.getPermLevel()));
                    }
                    sender.sendMessage(">");
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
