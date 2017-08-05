package main.java.com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.database.tables.SecurityTable;
import main.java.com.djrapitops.plan.utilities.Check;
import org.bukkit.ChatColor;

/**
 * Subcommand for checking WebUser permission level.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebCheckCommand extends SubCommand {

    private final Plan plugin;

    public WebCheckCommand(Plan plugin) {
        super("check", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.MANAGE_WEB.getPerm(), "Check a webuser and their permission level.", "<username>");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(args.length >= 1, Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE.parse() + " <username>", sender)) {
            return true;
        }
        SecurityTable table = plugin.getDB().getSecurityTable();
        String user = args[0];

        plugin.getRunnableFactory().createNew(new AbsRunnable("Webuser Check Task: " + user) {
            @Override
            public void run() {
                try {
                    if (!Check.isTrue(table.userExists(user), ChatColor.RED + "[Plan] User Doesn't exist.", sender)) {
                        return;
                    }
                    WebUser info = table.getWebUser(user);
                    sender.sendMessage(info.getName() + ": Permission level: " + info.getPermLevel());
                } catch (Exception ex) {
                    Log.toLog(this.getClass().getName(), ex);
                    sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL.parse());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
        return true;
    }

}
