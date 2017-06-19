package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.task.RslBukkitRunnable;
import com.djrapitops.javaplugin.task.RslTask;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.command.Condition;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.ui.TextUI;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
        super("qinspect", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.QUICK_INSPECT.getPermission(), Phrase.CMD_USG_QINSPECT + "", Phrase.ARG_PLAYER + "");

        this.plugin = plugin;
        inspectCache = plugin.getInspectCache();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.QUICK_INSPECT_OTHER);
        final RslTask inspectTask = new RslBukkitRunnable<Plan>("QinspectTask") {
            @Override
            public void run() {
                UUID uuid = ConditionUtils.getUUID(playerName);
                Condition[] preConditions = new Condition[]{
                    new Condition(ConditionUtils.uuidIsValid(uuid), Phrase.USERNAME_NOT_VALID.toString()),
                    new Condition(ConditionUtils.playerHasPlayed(uuid), Phrase.USERNAME_NOT_SEEN.toString()),
                    new Condition(plugin.getDB().wasSeenBefore(uuid), Phrase.USERNAME_NOT_KNOWN.toString())
                };

                for (Condition condition : preConditions) {
                    if (!condition.pass()) {
                        sender.sendMessage(condition.getFailMsg());
                        this.cancel();
                        return;
                    }
                }
                sender.sendMessage(Phrase.GRABBING_DATA_MESSAGE + "");
                inspectCache.cache(uuid);
                final RslTask inspectMessageSenderTask = new RslBukkitRunnable<Plan>("QinspectMessageSenderTask") {
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
                }.runTaskTimer(1 * 20, 5 * 20);
                this.cancel();
            }
        }.runTaskAsynchronously();
        return true;
    }
}
