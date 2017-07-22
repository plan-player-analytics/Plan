package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;

/**
 * Command used to display link to the player list webpage.
 * 
 * Subcommand is not registered if Webserver is not enabled.
 * 
 * @author Rsl1122
 * @since 3.5.2
 */
public class ListCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ListCommand(Plan plugin) {
        super("list", CommandType.CONSOLE, Permissions.INSPECT_OTHER.getPermission(), "List to all cached players", "");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(ConditionUtils.pluginHasViewCapability(), Phrase.ERROR_WEBSERVER_OFF_INSPECT + "", sender)) {
            return true;
        }
        sendListMsg(sender);
        return true;
    }

    private void sendListMsg(ISender sender) {
        sender.sendMessage(Phrase.CMD_FOOTER.parse());

        // Link
        String url = HtmlUtils.getServerAnalysisUrlWithProtocol().replace("server", "players");
        String message = Phrase.CMD_LINK + "";
        boolean console = !CommandUtils.isPlayer(sender);
        if (console) {
            sender.sendMessage(message + url);
        } else {
            sender.sendMessage(message);
            sendLink(sender, url);
        }
        sender.sendMessage(Phrase.CMD_FOOTER + "");
    }

    @Deprecated // TODO Will be rewritten to the RslPlugin abstractions in the future.
    private void sendLink(ISender sender, String url) throws CommandException {
        plugin.getServer().dispatchCommand(
                Bukkit.getConsoleSender(),
                "tellraw " + sender.getName() + " [\"\",{\"text\":\"" + Phrase.CMD_CLICK_ME + "\",\"underlined\":true,"
                + "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url + "\"}}]");
    }
}
