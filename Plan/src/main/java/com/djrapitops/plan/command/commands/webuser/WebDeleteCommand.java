package main.java.com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.database.tables.SecurityTable;
import main.java.com.djrapitops.plan.settings.Permissions;
import main.java.com.djrapitops.plan.settings.locale.Locale;
import main.java.com.djrapitops.plan.settings.locale.Msg;
import main.java.com.djrapitops.plan.utilities.Condition;
import net.md_5.bungee.api.ChatColor;

/**
 * Subcommand for deleting a WebUser.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebDeleteCommand extends SubCommand {

    private final IPlan plugin;

    public WebDeleteCommand(IPlan plugin) {
        super("delete, remove",
                CommandType.PLAYER_OR_ARGS,
                Permissions.MANAGE_WEB.getPerm(),
                Locale.get(Msg.CMD_USG_WEB_DELETE).toString(),
                "<username>");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Condition.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).parse() + " <username>", sender)) {
            return true;
        }
        SecurityTable table = plugin.getDB().getSecurityTable();
        String user = args[0];

        RunnableFactory.createNew(new AbsRunnable("Webuser Delete Task: " + user) {
            @Override
            public void run() {
                try {
                    if (!Condition.isTrue(table.userExists(user), ChatColor.RED + "[Plan] User Doesn't exist.", sender)) {
                        return;
                    }
                    table.removeUser(user);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_SUCCESS).parse());
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
