package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.*;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.ui.text.TextUI;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.ChatColor;

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
        super("analyze, analyse, analysis, a", CommandType.CONSOLE, Permissions.ANALYZE.getPermission(), Phrase.CMD_USG_ANALYZE.parse());
        this.plugin = plugin;
        analysisCache = plugin.getAnalysisCache();
        setHelp(plugin);
    }

    private void setHelp(Plan plugin) {
        ColorScheme colorScheme = plugin.getColorScheme();

        String mCol = colorScheme.getMainColor();
        String sCol = colorScheme.getSecondaryColor();
        String tCol = colorScheme.getTertiaryColor();

        String[] help = new String[]{
                mCol + "Analysis Command",
                tCol + "  Used to Refresh analysis cache & Access the result page",
                sCol + "  /plan status can be used to check status of analysis while it is running.",
                sCol + "  Aliases: analyze, analyse, analysis, a"
        };

        setInDepthHelp(help);
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(ConditionUtils.pluginHasViewCapability(), Phrase.ERROR_WEBSERVER_OFF_ANALYSIS.toString(), sender)) {
            return true;
        }

        if (!Check.isTrue(analysisCache.isAnalysisEnabled(), Phrase.ERROR_ANALYSIS_DISABLED_TEMPORARILY.toString(), sender)
                && !analysisCache.isCached()) {
            return true;
        }

        sender.sendMessage(Phrase.GRABBING_DATA_MESSAGE + "");
        if (plugin.getUiServer().isAuthRequired()) {
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
        }
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
     * <p>
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
                sender.sendLink("   ", Phrase.CMD_CLICK_ME.toString(), url);
            }
        }
        sender.sendMessage(Phrase.CMD_FOOTER.toString());
    }
}
