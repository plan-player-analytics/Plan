package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.command.ConditionUtils;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
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
        super("analyze, analyse, analysis, a",
                CommandType.CONSOLE,
                Permissions.ANALYZE.getPermission(),
                Locale.get(Msg.CMD_USG_ANALYZE).parse());
        this.plugin = plugin;
        analysisCache = plugin.getAnalysisCache();
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_ANALYZE).toArray();
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

        sender.sendMessage(Locale.get(Msg.CMD_INFO_FETCH_DATA).toString());
        if (plugin.getUiServer().isAuthRequired() && CommandUtils.isPlayer(sender)) {
            plugin.getRunnableFactory().createNew(new AbsRunnable("WebUser exist check task") {
                @Override
                public void run() {
                    try {
                        boolean senderHasWebUser = plugin.getDB().getSecurityTable().userExists(sender.getName());
                        if (!senderHasWebUser) {
                            sender.sendMessage(ChatColor.YELLOW + "[Plan] You might not have a web user, use /plan register <password>");
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
                    sender.sendMessage(Locale.get(Msg.CMD_FAIL_TIMEOUT).parse("Analysis"));
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
        sender.sendMessage(Locale.get(Msg.CMD_HEADER_ANALYZE).toString());
        if (textUI) {
            sender.sendMessage(TextUI.getAnalysisMessages());
        } else {
            // Link
            String url = HtmlUtils.getServerAnalysisUrlWithProtocol();
            String message = Locale.get(Msg.CMD_INFO_LINK).toString();
            boolean console = !CommandUtils.isPlayer(sender);
            if (console) {
                sender.sendMessage(message + url);
            } else {
                sender.sendMessage(message);
                sender.sendLink("   ", Locale.get(Msg.CMD_INFO_CLICK_ME).toString(), url);
            }
        }
        sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).toString());
    }
}
