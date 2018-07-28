package com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.Msg;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.utilities.comparators.WebUserComparator;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.settings.ColorScheme;
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

    private final PlanPlugin plugin;
    private final Locale locale;

    public WebListUsersCommand(PlanPlugin plugin) {
        super("list", Permissions.MANAGE_WEB.getPerm(), CommandType.CONSOLE);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.WEB_LIST));
        this.plugin = plugin;
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        RunnableFactory.createNew(new AbsRunnable("Webuser List Task") {
            @Override
            public void run() {
                try {
                    ColorScheme cs = plugin.getColorScheme();
                    String mCol = cs.getMainColor();
                    List<WebUser> users = Database.getActive().fetch().getWebUsers();
                    users.sort(new WebUserComparator());
                    sender.sendMessage(locale.get(Msg.CMD_CONSTANT_FOOTER).parse() + mCol + " WebUsers (" + users.size() + ")");
                    for (WebUser user : users) {
                        sender.sendMessage("  " + user.getPermLevel() + " : " + user.getName());
                    }
                    sender.sendMessage(locale.get(Msg.CMD_CONSTANT_FOOTER).parse());
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
