package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.utilities.UUIDFetcher;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;

import com.djrapitops.plan.utilities.MiscUtils;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;

/**
 *
 * @author Rsl1122
 */
public class ManageRemoveCommand extends SubCommand {

    private Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageRemoveCommand(Plan plugin) {
        super("remove", "plan.manage", "Remove players's data from the Active Database.", CommandType.CONSOLE_WITH_ARGUMENTS, "<player> [-a]");

        this.plugin = plugin;
    }

    /**
     * Subcommand inspect.
     *
     * Adds player's data from DataCache/DB to the InspectCache for amount of
     * time specified in the config, and clears the data from Cache with a timer
     * task.
     *
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args Player's name or nothing - if empty sender's name is used.
     * @return true in all cases.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE.toString());
            return true;
        }

        String playerName = MiscUtils.getPlayerDisplayname(args, sender);

        UUID uuid;
        try {
            uuid = UUIDFetcher.getUUIDOf(playerName);
            if (uuid == null) {
                throw new Exception("Username doesn't exist.");
            }
        } catch (Exception e) {
            sender.sendMessage(Phrase.USERNAME_NOT_VALID.toString());
            return true;
        }
        OfflinePlayer p = getOfflinePlayer(uuid);
        if (!p.hasPlayedBefore()) {
            sender.sendMessage(Phrase.USERNAME_NOT_SEEN.toString());
            return true;
        }
        if (!plugin.getDB().wasSeenBefore(uuid)) {
            sender.sendMessage(Phrase.USERNAME_NOT_KNOWN.toString());
            return true;
        }
        if (!Arrays.asList(args).contains("-a")) {
            sender.sendMessage(Phrase.COMMAND_ADD_CONFIRMATION_ARGUMENT.toString());
            return true;
        }

        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor hColor = Phrase.COLOR_TER.color();

        (new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getDB().removeAccount(uuid.toString());
                sender.sendMessage(hColor+""+Phrase.ARROWS_RIGHT+" "+oColor+"Data of "+hColor+playerName+oColor+" was removed from Database "+hColor+plugin.getDB().getConfigName()+oColor+".");
                this.cancel();
            }
        }).runTaskAsynchronously(plugin);
        return true;
    }
}
