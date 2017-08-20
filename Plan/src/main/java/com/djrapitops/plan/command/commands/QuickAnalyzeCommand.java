package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
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
@Deprecated
public class QuickAnalyzeCommand extends SubCommand {

    private final Plan plugin;
    private final AnalysisCacheHandler analysisCache;

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public QuickAnalyzeCommand(Plan plugin) {
        super("qanalyze, qanalyse, qanalysis, qa",
                CommandType.CONSOLE,
                Permissions.QUICK_ANALYZE.getPermission(),
                Locale.get(Msg.CMD_USG_QANALYZE).parse());
        this.plugin = plugin;
        analysisCache = plugin.getAnalysisCache();

    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_PLAN).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(ConditionUtils.pluginHasViewCapability(), Locale.get(Msg.CMD_FAIL_NO_DATA_VIEW).toString(), sender)) {
            return true;
        }
        if (!Check.isTrue(analysisCache.isAnalysisEnabled(), Locale.get(Msg.CMD_INFO_ANALYSIS_TEMP_DISABLE).toString(), sender)
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
                    sender.sendMessage(Locale.get(Msg.CMD_HEADER_ANALYZE) + "");
                    sender.sendMessage(TextUI.getAnalysisMessages());
                    sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER) + "");
                    this.cancel();
                }
                if (timesRun > 10) {
                    Log.debug("Command Timeout Message, QuickAnalyze.");
                    sender.sendMessage(Locale.get(Msg.CMD_FAIL_TIMEOUT).parse("Analysis"));
                    this.cancel();
                }
            }
        }).runTaskTimer(TimeAmount.SECOND.ticks(), 5 * TimeAmount.SECOND.ticks());
    }
}
