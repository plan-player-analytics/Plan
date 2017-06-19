package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.task.RslBukkitRunnable;
import com.djrapitops.javaplugin.task.RslTask;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.command.Condition;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.ui.TextUI;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!ConditionUtils.pluginHasViewCapability()) {
            sender.sendMessage(Phrase.ERROR_WEBSERVER_OFF_INSPECT + "");
            return true;
        }
        String playerName = MiscUtils.getPlayerName(args, sender);
        final RslTask inspectTask = new RslBukkitRunnable<Plan>("InspectTask") {
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
                final RslTask inspectMessageSenderTask = new RslBukkitRunnable<Plan>("InspectMessageSenderTask") {
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

                }.runTaskTimer(1 * 20, 5 * 20);
                this.cancel();
            }            
        }.runTaskAsynchronously();
        return true;
    }

    private void sendInspectMsg(CommandSender sender, String playerName, UUID uuid) throws CommandException {
        sender.sendMessage(Phrase.CMD_INSPECT_HEADER + playerName);
        if (Settings.USE_ALTERNATIVE_UI.isTrue()) {
            sender.sendMessage(TextUI.getInspectMessages(uuid));
        } else {
            // Link
            String url = HtmlUtils.getInspectUrlWithProtocol(playerName);
            String message = Phrase.CMD_LINK + "";
            boolean console = !(sender instanceof Player);
            if (console) {
                sender.sendMessage(message + url);
            } else {
                sender.sendMessage(message);
                Player player = (Player) sender;
                Bukkit.getServer().dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "tellraw " + player.getName() + " [\"\",{\"text\":\"" + Phrase.CMD_CLICK_ME + "\",\"underlined\":true,"
                        + "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url + "\"}}]");
            }
        }
        sender.sendMessage(Phrase.CMD_FOOTER + "");
    }
}
