package main.java.com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.utilities.comparators.WebUserComparator;

import java.util.List;

/**
 * Subcommand for checking WebUser list.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebListUsersCommand extends SubCommand {

    private final IPlan plugin;

    public WebListUsersCommand(IPlan plugin) {
        super("list", CommandType.CONSOLE, Permissions.MANAGE_WEB.getPerm(), "List registered web users & permission levels.");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("Webuser List Task") {
            @Override
            public void run() {
                try {
                    ColorScheme cs = plugin.getColorScheme();
                    String mCol = cs.getMainColor();
                    List<WebUser> users = plugin.getDB().getSecurityTable().getUsers();
                    users.sort(new WebUserComparator());
                    sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).parse() + mCol + " WebUsers (" + users.size() + ")");
                    for (WebUser user : users) {
                        sender.sendMessage("  " + user.getPermLevel() + " : " + user.getName());
                    }
                    sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).parse());
                } catch (Exception ex) {
                    Log.toLog(this.getClass().getName(), ex);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).parse());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
        return true;
    }

}
