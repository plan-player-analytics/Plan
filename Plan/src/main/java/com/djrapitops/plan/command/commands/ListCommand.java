package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;

/**
 * Command used to display link to the player list webpage.
 * <p>
 * Subcommand is not registered if Webserver is not enabled.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class ListCommand extends SubCommand {

    /**
     * Class Constructor.
     */
    public ListCommand() {
        super("list, pl", CommandType.CONSOLE, Permissions.INSPECT_OTHER.getPermission(), Locale.get(Msg.CMD_USG_LIST).toString(), "");

    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_LIST).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {

        sendListMsg(sender);
        return true;
    }

    private void sendListMsg(ISender sender) {
        sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).parse());

        // Link
        String url = Plan.getInstance().getInfoManager().getLinkTo("/players");
        String message = Locale.get(Msg.CMD_INFO_LINK).toString();
        boolean console = !CommandUtils.isPlayer(sender);
        if (console) {
            sender.sendMessage(message + url);
        } else {
            sender.sendMessage(message);
            sender.sendLink("   ", Locale.get(Msg.CMD_INFO_CLICK_ME).toString(), url);
        }
        sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).toString());
    }
}