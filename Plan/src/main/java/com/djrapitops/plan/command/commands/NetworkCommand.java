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
public class NetworkCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     */
    public NetworkCommand(Plan plugin) {
        super("network, n, netw",
                CommandType.CONSOLE,
                Permissions.ANALYZE.getPermission(),
                "Get the link to the network page");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (plugin.getInfoManager().isUsingAnotherWebServer()) {
            sender.sendMessage("§cNot using Bungee WebServer!");
            return true;
        }
        sendNetworkMsg(sender);
        return true;
    }

    private void sendNetworkMsg(ISender sender) {
        sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).parse());

        // Link
        String url = Plan.getInstance().getInfoManager().getLinkTo("/network/");
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