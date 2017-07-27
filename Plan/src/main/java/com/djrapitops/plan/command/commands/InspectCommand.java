package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.*;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.ui.text.TextUI;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;

import java.sql.SQLException;
import java.util.UUID;

/**
 * This command is used to cache UserData to InspectCache and display the link.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class InspectCommand extends SubCommand {

    private final Plan plugin;
    private final InspectCacheHandler inspectCache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public InspectCommand(Plan plugin) {
        super("inspect", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.INSPECT.getPermission(), Phrase.CMD_USG_INSPECT.toString(), Phrase.ARG_PLAYER.toString());

        this.plugin = plugin;
        inspectCache = plugin.getInspectCache();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(ConditionUtils.pluginHasViewCapability(), Phrase.ERROR_WEBSERVER_OFF_INSPECT.toString(), sender)) {
            return true;
        }

        String playerName = MiscUtils.getPlayerName(args, sender);

        runInspectTask(playerName, sender);
        return true;
    }

    private void runInspectTask(String playerName, ISender sender) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("InspectTask") {
            @Override
            public void run() {
                try {
                    UUID uuid = UUIDUtility.getUUIDOf(playerName);
                    if (!Check.isTrue(Verify.notNull(uuid), Phrase.USERNAME_NOT_VALID.toString(), sender)) {
                        return;
                    }
                    if (!Check.isTrue(ConditionUtils.playerHasPlayed(uuid), Phrase.USERNAME_NOT_SEEN.toString(), sender)) {
                        return;
                    }
                    if (!Check.isTrue(plugin.getDB().wasSeenBefore(uuid), Phrase.USERNAME_NOT_KNOWN.toString(), sender)) {
                        return;
                    }
                    sender.sendMessage(Phrase.GRABBING_DATA_MESSAGE + "");
                    if (CommandUtils.isPlayer(sender)) {
                        boolean senderHasWebUser = plugin.getDB().getSecurityTable().userExists(sender.getName());
                        if (!senderHasWebUser) {
                            sender.sendMessage(ChatColor.YELLOW + "[Plan] You might not have a web user, use /plan register <password>");
                        }
                    }
                    inspectCache.cache(uuid);
                    runMessageSenderTask(uuid, sender, playerName);
                } catch (SQLException ex) {
                    Log.toLog(this.getClass().getName(), ex);
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }

    private void runMessageSenderTask(UUID uuid, ISender sender, String playerName) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("InspectMessageSenderTask") {
            private int timesrun = 0;

            @Override
            public void run() {
                timesrun++;
                if (inspectCache.isCached(uuid)) {
                    sendInspectMsg(sender, playerName, uuid);
                    this.cancel();
                    return;
                }
                if (timesrun > 10) {
                    Log.debug("Command Timeout Message, Inspect.");
                    sender.sendMessage(Phrase.COMMAND_TIMEOUT.parse("Inspect"));
                    this.cancel();
                }
            }

        }).runTaskTimer(TimeAmount.SECOND.ticks(), 5 * TimeAmount.SECOND.ticks());
    }

    private void sendInspectMsg(ISender sender, String playerName, UUID uuid) {

        boolean usingTextUI = Settings.USE_ALTERNATIVE_UI.isTrue();

        sender.sendMessage(Phrase.CMD_INSPECT_HEADER + playerName);

        if (usingTextUI) {
            sender.sendMessage(TextUI.getInspectMessages(uuid));
        } else {
            // Link
            String url = HtmlUtils.getInspectUrlWithProtocol(playerName);
            String message = Phrase.CMD_LINK + "";
            boolean console = !CommandUtils.isPlayer(sender);
            if (console) {
                sender.sendMessage(message + url);
            } else {
                sender.sendMessage(message);
                sendLink(sender, url);
            }
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
