package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.command.CommandUtils;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.ui.TextUI;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * This subcommand is used to run the analysis and access the /server link.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class AnalyzeCommand extends SubCommand {

    private final Plan plugin;
    private final AnalysisCacheHandler analysisCache;

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public AnalyzeCommand(Plan plugin) {
        super("analyze, analyse, analysis", CommandType.CONSOLE, Permissions.ANALYZE.getPermission(), Phrase.CMD_USG_ANALYZE.parse());
        this.plugin = plugin;
        analysisCache = plugin.getAnalysisCache();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!CommandUtils.pluginHasViewCapability()) {
            sender.sendMessage(Phrase.ERROR_WEBSERVER_OFF_ANALYSIS + "");
            return true;
        }
        sender.sendMessage(Phrase.GRABBING_DATA_MESSAGE + "");
        if (!analysisCache.isCached() || MiscUtils.getTime() - analysisCache.getData().getRefreshDate() > 60000) {
            int bootAnID = plugin.getBootAnalysisTaskID();
            if (bootAnID != -1) {
                plugin.getServer().getScheduler().cancelTask(bootAnID);
            }
            analysisCache.updateCache();
        }
        final BukkitTask analysisMessageSenderTask = new BukkitRunnable() {
            private int timesrun = 0;

            @Override
            public void run() {
                timesrun++;
                if (analysisCache.isCached()) {
                    sendAnalysisMessage(sender);
                    this.cancel();
                    return;
                }
                if (timesrun > 10) {
                    Log.debug("Command Timeout Message, Analysis.");
                    sender.sendMessage(Phrase.COMMAND_TIMEOUT.parse("Analysis"));
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 1 * 20, 5 * 20);
        return true;
    }

    /**
     * Used to send the message after /plan analysis.
     *
     * Final because
     *
     * @param sender Command sender.
     */
    final public void sendAnalysisMessage(CommandSender sender) {
        boolean textUI = Settings.USE_ALTERNATIVE_UI.isTrue();
        sender.sendMessage(Phrase.CMD_ANALYZE_HEADER + "");
        if (textUI) {
            sender.sendMessage(TextUI.getAnalysisMessages());
        } else {
            // Link
            String url = HtmlUtils.getServerAnalysisUrlWithProtocol();
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
