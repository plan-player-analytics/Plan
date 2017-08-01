package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.ui.text.TextUI;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

/**
 * This subcommand is used to run the analysis and to view some of the data in
 * game.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class QuickAnalyzeCommand extends SubCommand {

    private final Plan plugin;
    private final AnalysisCacheHandler analysisCache;

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public QuickAnalyzeCommand(Plan plugin) {
        super("qanalyze, qanalyse, qanalysis, qa", CommandType.CONSOLE, Permissions.QUICK_ANALYZE.getPermission(), Phrase.CMD_USG_QANALYZE.parse());
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
                mCol + "Quick Analysis command",
                tCol + "  Used to get in game info about analysis.",
                sCol + "  Has less info than full Analysis web page.",
                sCol + "  Aliases: qanalyze, ganalyse, qanalysis, qa"
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
        plugin.getRunnableFactory().createNew(new AbsRunnable("QanalysisMessageSenderTask") {
            private int timesRun = 0;

            @Override
            public void run() {
                timesRun++;
                if (analysisCache.isCached() && (!analysisCache.isAnalysisBeingRun() || !analysisCache.isAnalysisEnabled())) {
                    sender.sendMessage(Phrase.CMD_ANALYZE_HEADER + "");
                    sender.sendMessage(TextUI.getAnalysisMessages());
                    sender.sendMessage(Phrase.CMD_FOOTER + "");
                    this.cancel();
                }
                if (timesRun > 10) {
                    Log.debug("Command Timeout Message, QuickAnalyze.");
                    sender.sendMessage(Phrase.COMMAND_TIMEOUT.parse("Analysis"));
                    this.cancel();
                }
            }
        }).runTaskTimer(TimeAmount.SECOND.ticks(), 5 * TimeAmount.SECOND.ticks());
    }
}
