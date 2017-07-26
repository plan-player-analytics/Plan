package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.ui.text.TextUI;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;

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
        if (!Check.isTrue(ConditionUtils.pluginHasViewCapability(), Phrase.ERROR_WEBSERVER_OFF_ANALYSIS.toString(), sender)) {
            return true;
        }
        if (!Check.isTrue(analysisCache.isAnalysisEnabled(), Phrase.ERROR_ANALYSIS_DISABLED_TEMPORARILY.toString(), sender)) {
            if (!analysisCache.isCached()) {
                return true;
            }
        }

        sender.sendMessage(Phrase.GRABBING_DATA_MESSAGE + "");
        plugin.getRunnableFactory().createNew(new AbsRunnable("WebUser exist check task") {
            @Override
            public void run() {
                try {
                    if (CommandUtils.isPlayer(sender)) {
                        boolean senderHasWebUser = plugin.getDB().getSecurityTable().userExists(sender.getName());
                        if (!senderHasWebUser) {
                            sender.sendMessage(ChatColor.YELLOW + "[Plan] You might not have a web user, use /plan register <password>");
                        }
                    }
                } catch (Exception e) {
                    Log.toLog(this.getClass().getName() + getName(), e);
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
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
        plugin.getRunnableFactory().createNew("AnalysisMessageSenderTask", new AbsRunnable() {
            private int timesRun = 0;

            @Override
            public void run() {
                timesRun++;
                if (analysisCache.isCached() && (!analysisCache.isAnalysisBeingRun() || !analysisCache.isAnalysisEnabled())) {
                    sendAnalysisMessage(sender);
                    this.cancel();
                    return;
                }
                if (timesRun > 10) {
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
        sender.sendMessage(Phrase.CMD_ANALYZE_HEADER.toString());
        if (textUI) {
            sender.sendMessage(TextUI.getAnalysisMessages());
        } else {
            // Link
            String url = HtmlUtils.getServerAnalysisUrlWithProtocol();
            String message = Phrase.CMD_LINK.toString();
            boolean console = !CommandUtils.isPlayer(sender);
            if (console) {
                sender.sendMessage(message + url);
            } else {
                sender.sendMessage(message);
                sendLink(sender, url);
            }
        }
        sender.sendMessage(Phrase.CMD_FOOTER.toString());
    }

    @Deprecated // TODO Will be rewritten to the RslPlugin abstractions in the future.
    private void sendLink(ISender sender, String url) throws CommandException {
        plugin.getServer().dispatchCommand(
                Bukkit.getConsoleSender(),
                "tellraw " + sender.getName() + " [\"\",{\"text\":\"" + Phrase.CMD_CLICK_ME + "\",\"underlined\":true,"
                + "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url + "\"}}]");
    }
}
