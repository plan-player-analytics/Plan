package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;
import org.bukkit.ChatColor;

import java.sql.SQLException;
import java.util.UUID;

/**
 * This command is used to cache UserInfo to InspectCache and display the link.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class InspectCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public InspectCommand(Plan plugin) {
        super("inspect",
                CommandType.CONSOLE_WITH_ARGUMENTS,
                Permissions.INSPECT.getPermission(),
                Locale.get(Msg.CMD_USG_INSPECT).toString(),
                "<player>");

        this.plugin = plugin;

    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_INSPECT).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(ConditionUtils.pluginHasViewCapability(), Locale.get(Msg.CMD_FAIL_NO_DATA_VIEW).toString(), sender)) {
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
                    if (!Check.isTrue(Verify.notNull(uuid), Locale.get(Msg.CMD_FAIL_USERNAME_NOT_VALID).toString(), sender)) {
                        return;
                    }
                    if (!Check.isTrue(ConditionUtils.playerHasPlayed(uuid), Locale.get(Msg.CMD_FAIL_USERNAME_NOT_SEEN).toString(), sender)) {
                        return;
                    }
                    if (!Check.isTrue(plugin.getDB().wasSeenBefore(uuid), Locale.get(Msg.CMD_FAIL_USERNAME_NOT_KNOWN).toString(), sender)) {
                        return;
                    }
                    sender.sendMessage(Locale.get(Msg.CMD_INFO_FETCH_DATA).toString());
                    if (CommandUtils.isPlayer(sender) && plugin.getWebServer().isAuthRequired()) {
                        boolean senderHasWebUser = plugin.getDB().getSecurityTable().userExists(sender.getName());
                        if (!senderHasWebUser) {
                            sender.sendMessage(ChatColor.YELLOW + "[Plan] You might not have a web user, use /plan register <password>");
                        }
                    }
                    //TODO Inspect Request.
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
                // TODO better message sending.
                if (timesrun > 10) {
                    Log.debug("Command Timeout Message, Inspect.");
                    sender.sendMessage(Locale.get(Msg.CMD_FAIL_TIMEOUT).parse("Inspect"));
                    this.cancel();
                }
            }

        }).runTaskTimer(TimeAmount.SECOND.ticks(), 5 * TimeAmount.SECOND.ticks());
    }

    private void sendInspectMsg(ISender sender, String playerName, UUID uuid) {
        sender.sendMessage(Locale.get(Msg.CMD_HEADER_INSPECT) + " " + playerName);
        // Link
        String url = HtmlUtils.getInspectUrlWithProtocol(playerName);
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