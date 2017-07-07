package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.javaplugin.api.TimeAmount;
import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.CommandUtils;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.command.sender.ISender;
import com.djrapitops.javaplugin.task.runnable.RslRunnable;
import com.djrapitops.javaplugin.utilities.Verify;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.ui.TextUI;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;

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
        super("inspect", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.INSPECT.getPermission(), Phrase.CMD_USG_INSPECT + "", Phrase.ARG_PLAYER + "");

        this.plugin = plugin;
        inspectCache = plugin.getInspectCache();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.ifTrue(ConditionUtils.pluginHasViewCapability(), Phrase.ERROR_WEBSERVER_OFF_INSPECT + "", sender)) {
            return true;
        }

        String playerName = MiscUtils.getPlayerName(args, sender);

        runInspectTask(playerName, sender);
        return true;
    }

    private void runInspectTask(String playerName, ISender sender) {
        plugin.getRunnableFactory().createNew(new RslRunnable("InspectTask") {
            @Override
            public void run() {
                try {
                    UUID uuid = ConditionUtils.getUUID(playerName);
                    if (!Check.ifTrue(Verify.notNull(uuid), Phrase.USERNAME_NOT_VALID.toString(), sender)) {
                        return;
                    }
                    if (!Check.ifTrue(ConditionUtils.playerHasPlayed(uuid), Phrase.USERNAME_NOT_SEEN.toString(), sender)) {
                        return;
                    }
                    if (!Check.ifTrue(plugin.getDB().wasSeenBefore(uuid), Phrase.USERNAME_NOT_KNOWN.toString(), sender)) {
                        return;
                    }
                    sender.sendMessage(Phrase.GRABBING_DATA_MESSAGE + "");

                    inspectCache.cache(uuid);
                    runMessageSenderTask(uuid, sender, playerName);
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }

    private void runMessageSenderTask(UUID uuid, ISender sender, String playerName) {
        plugin.getRunnableFactory().createNew(new RslRunnable("InspectMessageSenderTask") {
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
