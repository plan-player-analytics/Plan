package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.settings.DefaultMessages;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.ui.text.TextUI;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;

import java.util.UUID;

/**
 * This command is used to cache UserData to InspectCache and to view some of
 * the data in game.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class QuickInspectCommand extends SubCommand {

    private final Plan plugin;
    private final InspectCacheHandler inspectCache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public QuickInspectCommand(Plan plugin) {
        super("qinspect, qi", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.QUICK_INSPECT.getPermission(), Phrase.CMD_USG_QINSPECT + "", Phrase.ARG_PLAYER + "");

        this.plugin = plugin;
        inspectCache = plugin.getInspectCache();
        setHelp(plugin);
    }

    private void setHelp(Plan plugin) {
        ColorScheme colorScheme = plugin.getColorScheme();

        String ball = DefaultMessages.BALL.toString();

        String mCol = colorScheme.getMainColor();
        String sCol = colorScheme.getSecondaryColor();
        String tCol = colorScheme.getTertiaryColor();

        String[] help = new String[]{
                mCol + "Quick Inspect command",
                tCol + "  Used to get some inspect info in game.",
                sCol + "  Has less info than full Inspect web page.",
                sCol + "  Alias: /plan qi"
        };
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.QUICK_INSPECT_OTHER);
        plugin.getRunnableFactory().createNew(new AbsRunnable("QinspectTask") {
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
                    inspectCache.cache(uuid);
                    runMessageSenderTask(uuid, sender, playerName);
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
        return true;
    }

    private void runMessageSenderTask(UUID uuid, ISender sender, String playerName) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("QinspectMessageSenderTask") {
            private int timesrun = 0;

            @Override
            public void run() {
                timesrun++;
                if (inspectCache.isCached(uuid)) {
                    sender.sendMessage(Phrase.CMD_INSPECT_HEADER + playerName);
                    sender.sendMessage(TextUI.getInspectMessages(uuid));
                    sender.sendMessage(Phrase.CMD_FOOTER + "");
                    this.cancel();
                }
                if (timesrun > 10) {
                    Log.debug("Command Timeout Message, QuickInspect.");
                    sender.sendMessage(Phrase.COMMAND_TIMEOUT.parse("Qinspect"));
                    this.cancel();
                }
            }
        }).runTaskTimer(TimeAmount.SECOND.ticks(), 5 * TimeAmount.SECOND.ticks());
    }
}
