package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.javaplugin.api.TimeAmount;
import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.command.sender.ISender;
import com.djrapitops.javaplugin.task.runnable.RslRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.ui.TextUI;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import com.djrapitops.javaplugin.task.ITask;

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
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.ifTrue(ConditionUtils.pluginHasViewCapability(), Phrase.ERROR_WEBSERVER_OFF_ANALYSIS + "", sender)) {
            return true;
        }
        if (!Check.ifTrue(analysisCache.isAnalysisEnabled(), Phrase.ERROR_ANALYSIS_DISABLED_TEMPORARILY + "", sender)) {
            if (!analysisCache.isCached()) {
                return true;
            }
        }

        sender.sendMessage(Phrase.GRABBING_DATA_MESSAGE + "");
        updateCache();
        runMessageSenderTask(sender);
        return true;
    }

    private void updateCache() {
        if (!analysisCache.isCached() || MiscUtils.getTime() - analysisCache.getData().getRefreshDate() > TimeAmount.MINUTE.ms()) {
            int bootAnID = plugin.getBootAnalysisTaskID();
            if (bootAnID != -1) {
                plugin.getServer().getScheduler().cancelTask(bootAnID);
            }
            analysisCache.updateCache();
        }
    }

    private void runMessageSenderTask(ISender sender) {
        plugin.getRunnableFactory().createNew("AnalysisMessageSenderTask", new RslRunnable() {
            private int timesrun = 0;

            @Override
            public void run() {
                timesrun++;
                if (analysisCache.isCached() && (!analysisCache.isAnalysisBeingRun() || !analysisCache.isAnalysisEnabled())) {
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
        }).runTaskTimer(TimeAmount.SECOND.ticks(), 5 * TimeAmount.SECOND.ticks());
    }

    /**
     * Used to send the message after /plan analysis.
     *
     * Final because
     *
     * @param sender Command sender.
     */
    private void sendAnalysisMessage(ISender sender) {
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
