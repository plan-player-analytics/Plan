package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.Msg;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;

/**
 * Command used to display link to the player list webpage.
 * <p>
 * Subcommand is not registered if Webserver is not enabled.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class NetworkCommand extends SubCommand {

    private final PlanPlugin plugin;

    /**
     * Class Constructor.
     */
    public NetworkCommand(PlanPlugin plugin) {
        super("network, n, netw",
                CommandType.CONSOLE,
                Permissions.ANALYZE.getPermission(),
                "Get the link to the network page");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        sendNetworkMsg(sender);
        return true;
    }

    private void sendNetworkMsg(ISender sender) {
        sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).parse());

        // Link
        String url = plugin.getInfoManager().getLinkTo("/network/");
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