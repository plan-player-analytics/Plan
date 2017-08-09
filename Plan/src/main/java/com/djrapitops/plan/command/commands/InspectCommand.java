package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.settings.ColorScheme;
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
import org.bukkit.ChatColor;

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
    public String[] addHelp() {
        ColorScheme colorScheme = Plan.getInstance().getColorScheme();

        String mCol = colorScheme.getMainColor();
        String sCol = colorScheme.getSecondaryColor();
        String tCol = colorScheme.getTertiaryColor();

        String[] help = new String[]{
                mCol + "Inspect command",
                tCol + "  Used to get a link to User's inspect page.",
                sCol + "  Own inspect page can be accessed with /plan inspect",
                sCol + "  Alias: /plan <name>"
        };

        return help;
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
                    sender.sendMessage(Phrase.GRABBING_DATA_MESSAGE.toString());
                    if (CommandUtils.isPlayer(sender) && plugin.getUiServer().isAuthRequired()) {
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
            String message = Phrase.CMD_LINK.toString();
            boolean console = !CommandUtils.isPlayer(sender);
            if (console) {
                sender.sendMessage(message + url);
            } else {
                sender.sendMessage(message);
                sender.sendLink("   ", Phrase.CMD_CLICK_ME.toString(), url);
            }
        }

        sender.sendMessage(Phrase.CMD_FOOTER.toString());
    }
}